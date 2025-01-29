package javax0.jamal.debugger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javax0.jamal.api.*;
import javax0.jamal.tools.ConnectionStringParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.*;

/**
 * Implementation of the {@link Debugger} interface using an HTTP server to interact with
 * a debugging client. This debugger allows controlling the execution of a Jamal processor
 * via HTTP requests, enabling features like stepping through macros, viewing internal
 * states, and setting breakpoints.
 */
public class HttpServerDebugger implements Debugger, AutoCloseable {
    private static final String MIME_PLAIN = "text/plain";
    public static final String MIME_APPLICATION_JSON = "application/json";
    private String client = "";
    private String cors = "";

    private enum Method {
        GET, POST
    }

    /**
     * Enum representing the available debugger commands, their corresponding URL endpoints,
     * and the HTTP method required to invoke them.
     */
    private enum Command {
        ALL("all", Method.GET),
        VERSION("version", Method.GET),
        LEVEL("level", Method.GET),
        ERRORS("errors", Method.GET),
        STATE("state", Method.GET),
        INPUT("input", Method.GET),
        INPUT_BEFORE("inputBefore", Method.GET),
        OUTPUT("output", Method.GET),
        PROCESSING("processing", Method.GET),
        BUILT_IN("macros", Method.GET),
        USER_DEFINED("userDefined", Method.GET),
        EXECUTE("execute", Method.POST),
        RUN("run", Method.POST),
        STEP("step", Method.POST),
        STEP_OUT("stepOut", Method.POST),
        STEP_INTO("stepInto", Method.POST),
        QUIT("quit", Method.POST);
        private final String url;
        private final Method method;

        Command(String url, Method method) {
            this.url = "/" + url;
            this.method = method;
        }
    }

    /**
     * The thread that manages an HTTP request uses this queue to dispatch a task to the Jamal main thread.
     * <p>
     * Given that this server is not designed for heavy-duty use, the queue size is limited to one.
     * There is no need to increase this capacity, as the server is intended to handle only one client, with each
     * request originating from human interaction.
     */
    private final BlockingQueue<Task> requestQueue = new LinkedBlockingQueue<>(1);

    /**
     * The debugger can stop at two places during macro evaluation.
     *
     * <ol>
     *     <li> Before evaluating a macro.
     *     <li> After a macro was evaluated.
     * </ol>
     * <p>
     * To signal which it is, the processor calls  {@link #setBefore(int, CharSequence)},
     * {@link #setAfter(int, CharSequence)}, and {@link #setStart(CharSequence)}.
     * <p>
     * For more explanation consult the Javadoc of these methods as they are defined in the {@link Debugger} interface.
     * (Not in this class.)
     */
    private enum State {
        BEFORE, AFTER
    }

    private State handleState;

    /**
     * Structure describing the task, what the debugger is asked to do.
     */
    private static class Task {
        boolean taskCancelled = false;
        final CountDownLatch waitForIt = new CountDownLatch(1);
        final CountDownLatch ack = new CountDownLatch(1);
        final Command command;
        String messageBuffer;
        int status = 200;
        final Map<String, String> params;
        String contentType = MIME_PLAIN;

        Task(Command command, String message) {
            this(command, (Map<String, String>) null);
            this.messageBuffer = message;
        }

        Task(Command command, final Map<String, String> params) {
            this.command = command;
            this.params = params;
        }

        void cancel() {
            taskCancelled = true;
            waitForIt.countDown();
        }

        boolean isCancelled() {
            return taskCancelled;
        }

        /**
         * After the debugger was done, it may wait for acknowledgement. For example, asking the debugger to quit or run
         * may result in the main Jamal thread to finish before the http server thread services the client sending the
         * response. In that case the client would not get the response. To avoid that these commands call this {@code
         * #waitForAck()} method that will wait until the other thread invokes {@link #acknowledge()}. That is called by
         * the HTTP server when the content was sent to the client and the channel was closed.
         */
        void waitForAck() {
            try {
                ack.await();
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Debugger was interrupted", e);
            }
        }

        /**
         * The web service thread will call this method to wait for the debugger thread to call {@link #done()}. The
         * method {@link #done()} is invoked when the debugger has finished the processing, put all the result into the
         * task, and the http server can serve the client with this information.
         *
         * @throws InterruptedException if the thread was interrupted
         */
        void waitForDone() throws InterruptedException {
            waitForIt.await();
        }

        /**
         * The HTTP server thread calls this method to acknowledge that the client was served and the debugger may even
         * quit.
         */
        void acknowledge() {
            ack.countDown();
        }

        /**
         * The debugger thread calls this method to signal that the task is finished, and the HTTP server thread can use
         * the task to create the response.
         */
        void done() {
            waitForIt.countDown();
        }

        String getMessage() {
            return messageBuffer;
        }
    }

    private Debugger.Stub stub;

    private int currentLevel = 1;
    private int stepLevel = 0;
    private RunState state = RunState.STEP_IN;
    private final List<String> breakpoints = new ArrayList<>();
    String inputBefore = "";
    CharSequence input;
    String inputAfter = "";
    String output = "";
    String macros = "";
    int port;

    /**
     * Returns true if we are in a debugging state. In other cases the debugger will not stop, just perform the
     * underlying action.
     * <p>
     *
     * <ul>
     *     <li>If the state is {@code NODEBUG}, we do not debug, no matter what.
     *     <li>If the state is {@code STEP_IN}, we debug, no matter what.
     *     <li>In all other cases we debug when we are on the top level or the current level is the same or lower than
     *     the step level.
     * </ul>
     * <p>
     * The last case is when we stepped over a macro, we went whatever deep, and we just returned after the whole
     * shenanigan was evaluated without stepping into the details.
     *
     * @return {@code true} if we debug stopping and {@code false} if we don't stop.
     */
    private boolean weDebug() {
        switch (state) {
            case NODEBUG:
                return false;
            case STEP_IN:
                return true;
            default:
                return (stepLevel == 0 || currentLevel <= stepLevel);
        }
    }

    @Override
    public void setStart(CharSequence macro) {
        if (weDebug()) {
            macros = macro.toString();
            handleState = State.BEFORE;
            handle();
        }
    }

    @Override
    public void setBefore(int level, CharSequence input) {
        currentLevel = level;
        if (weDebug()) {
            this.input = input;
            this.inputBefore = input.toString();
            macros = "";
            inputAfter = "";
        }
    }

    @Override
    public void setAfter(int level, CharSequence output) {
        currentLevel = level;
        if (weDebug()) {
            inputAfter = input == null ? "" : input.toString();
            this.output = output.toString();
            handleState = State.AFTER;
            handle();
        }
    }

    private void addToResponse(Task task, Map<String, Object> response, Command command, Object value) {
        final var key = command.url.substring(1); // url starts with '/'; key is the part that follows
        if (task.params.containsKey(key)) {
            response.put(key, value);
        }
    }

    /**
     * Indicates whether the debugger is currently in a waiting state, ready to accept and process tasks from the HTTP server.
     * <p>
     * The {@code isWaiting} flag serves two primary purposes:
     * <ul>
     *     <li>To signal the HTTP server that the debugger is actively waiting for commands, enabling it to process incoming requests.</li>
     *     <li>To prevent the HTTP server from queuing tasks when the debugger is not in a valid state to handle them.
     *     Instead of queuing commands, the server responds with an error to inform the client that the debugger is busy.
     *     This avoids unintended behavior, such as executing multiple queued commands when the user only intended to issue a single command.
     *     For example, if the debugger is lagging and the user presses the "STEP" button several times, queuing those actions
     *     could result in multiple steps being executed, which may not align with the user's intent.</li>
     * </ul>
     * <p>
     * This field is set to {@code true} when the debugger enters a waiting state (e.g., while handling tasks in
     * the {@link #handle()} method) and is reset to {@code false} when the debugger exits the waiting state
     * (e.g., after completing task processing or encountering an error).
     */
    volatile boolean isWaiting;


    private void handle() {
        if (state == RunState.RUN && handleState.equals(State.BEFORE)) {
            for (final var breakpoint : breakpoints) {
                if (breakpoint != null && !breakpoint.isEmpty() && macros.contains(breakpoint)) {
                    state = RunState.STEP_IN;
                    break;
                }
            }
        }
        if (state != RunState.STEP_IN && (state != RunState.STEP || currentLevel > stepLevel)) {
            return;
        }
        try {
            isWaiting = true;
            while (true) {
                final Task task = fetchNextTaskWaiting();
                Map<String, Object> response = null;
                switch (task.command) {
                    case ALL:
                        response = new HashMap<>();
                        addToResponse(task, response, Command.LEVEL, "" + currentLevel);
                        addToResponse(task, response, Command.ERRORS, new ArrayList<>(stub.errors()));
                        addToResponse(task, response, Command.STATE, handleState.toString());
                        addToResponse(task, response, Command.INPUT, inputAfter);
                        addToResponse(task, response, Command.OUTPUT, output);
                        addToResponse(task, response, Command.INPUT_BEFORE, inputBefore);
                        addToResponse(task, response, Command.PROCESSING, macros);
                        addToResponse(task, response, Command.BUILT_IN, getBuiltIns());
                        addToResponse(task, response, Command.USER_DEFINED, getUserDefineds());
                        addToResponse(task, response, Command.VERSION, getJamalVersion());
                        break;
                    case ERRORS:
                        response = new HashMap<>();
                        addToResponse(task, response, Command.ERRORS, new ArrayList<>(stub.errors()));
                        break;
                    case VERSION:
                        response = getJamalVersion();
                        break;
                    case STATE:
                        task.messageBuffer = handleState.toString();
                        break;
                    case QUIT: // exit the debugger and abort the execution of the processor
                        task.done();
                        task.waitForAck();
                        throw new IllegalArgumentException("Debugger was aborted.");
                    case RUN: // run the processor to the end
                        byte[] breakpointsBuffer = task.messageBuffer.getBytes(StandardCharsets.UTF_8);
                        breakpoints.clear();
                        breakpoints.addAll(List.of(new String(breakpointsBuffer, StandardCharsets.UTF_8).split("\n", -1)));
                        state = RunState.RUN;
                        task.done();
                        task.waitForAck();
                        return;
                    case STEP_OUT:// step to one level higher
                        state = RunState.STEP;
                        if (currentLevel > 1) {
                            stepLevel = currentLevel - 1;
                        } else {
                            stepLevel = 1;
                        }
                        task.done();
                        task.waitForAck();
                        return;
                    case STEP: // step over the macro, do not step into
                        state = RunState.STEP;
                        stepLevel = currentLevel;
                        task.done();
                        task.waitForAck();
                        return;
                    case STEP_INTO: // step into the macro
                        state = RunState.STEP_IN;
                        task.done();
                        task.waitForAck();
                        return;
                    case INPUT: // send the input of the required level to the client
                        task.messageBuffer = inputAfter;
                        break;
                    case INPUT_BEFORE: // send the input after of the required level to the client
                        task.messageBuffer = inputBefore;
                        break;
                    case OUTPUT: // send the output of the required level to the client
                        task.messageBuffer = output;
                        break;
                    case PROCESSING: // send the macro text of the required level to the client
                        task.messageBuffer = macros;
                        break;
                    case LEVEL: // send the current level to the client
                        response = Map.of("level", "" + currentLevel);
                        break;
                    case EXECUTE: // execute a macro in the current processor at the current level
                        byte[] buffer = task.messageBuffer.getBytes(StandardCharsets.UTF_8);
                        final RunState stateSave = state;
                        try {
                            state = RunState.NODEBUG;
                            final var exceptions = stub.errors();
                            final var saveExceptions = new ArrayDeque<>(exceptions);
                            exceptions.clear();
                            task.messageBuffer = stub.process(new String(buffer, StandardCharsets.UTF_8));
                            task.contentType = MIME_PLAIN;
                            final var evalExceptions = new ArrayDeque<>(exceptions);
                            exceptions.clear();
                            exceptions.addAll(saveExceptions);
                            if (!evalExceptions.isEmpty()) {
                                final var nrOfExceptions = evalExceptions.size();
                                final var sb = new StringBuilder(
                                        "There " + (nrOfExceptions == 1 ? "was" : "were")
                                                + " " + nrOfExceptions + " syntax error" + (nrOfExceptions == 1 ? "" : "s") + " processing the Jamal input:\n");
                                int ser = nrOfExceptions;
                                for (final var accumulated : evalExceptions) {
                                    sb.append(ser--).append(". ").append(accumulated.getMessage()).append("\n");
                                }
                                evalExceptions.clear();
                                response = Map.of(
                                        "message", sb.toString(),
                                        "trace", "",
                                        "status-link", "https://http.cat/405"
                                );
                                task.status = HTTP_BAD_METHOD;
                            }
                        } catch (BadSyntax badSyntax) {
                            try (final var baos = new ByteArrayOutputStream(); final var st = new PrintStream(baos)) {
                                badSyntax.printStackTrace(st);
                                response = Map.of(
                                        "message", badSyntax.getMessage(),
                                        "trace", baos.toString(StandardCharsets.UTF_8),
                                        "status-link", "https://http.cat/405"
                                );
                                task.status = HTTP_BAD_METHOD;
                            } catch (IOException e) {
                                throw new IllegalArgumentException("There was an exception composing the stack trace", e);
                            }
                        } finally {
                            state = stateSave;
                        }
                        break;
                    case BUILT_IN: // list built-in macros
                        response = getBuiltIns();
                        break;
                    case USER_DEFINED: // list user defined macros
                        response = getUserDefineds();
                        break;
                    default:
                        throw new IllegalArgumentException("The enum inside the class has a not handled value: " + task.command);
                }
                if (response != null) {
                    task.contentType = MIME_APPLICATION_JSON;
                    task.messageBuffer = JsonConverter.object2Json(response);
                }
                task.done();
            }
        } finally {
            isWaiting = false;
            deleteAllTasksFromTheRequestQueue();
        }
    }

    private Task fetchNextTaskWaiting() {
        try {
            return requestQueue.take();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Debugger thread was interrupted", e);
        }
    }

    /**
     * Fetch the next task from the queue and cancel it.
     *
     * @return {@code true} if the task was cancelled and {@code false} if the queue was empty.
     */
    private boolean cancelNextTask() {
        try {
            return Optional.ofNullable(requestQueue.poll(0, TimeUnit.NANOSECONDS))
                    .map(t -> {
                        t.cancel();
                        return true;
                    })
                    .orElse(false);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Debugger thread was interrupted", e);
        }
    }

    private void deleteAllTasksFromTheRequestQueue() {
        //noinspection StatementWithEmptyBody
        while (cancelNextTask()) ;
    }

    private Map<String, Object> getJamalVersion() {
        return Map.of("version", Processor.jamalVersionString());
    }

    private Map<String, Object> getUserDefineds() {
        final Map<String, Object> response = new HashMap<>();
        final List<Debuggable.Scope> scopes = stub.getScopeList();
        final var udList = new ArrayList<>(scopes.size());
        response.put("scopes", udList);
        for (final var scope : scopes) {
            final var macros = scope.getUdMacros();
            final List<Map<String, Object>> macrosList = new ArrayList<>(macros.size());
            udList.add(macrosList);
            for (final var macro : macros.values()) {
                macrosList.add(
                        getDebuggable(macro).map(ud -> Map.of(
                                        "open", ud.getOpenStr(),
                                        "close", ud.getCloseStr(),
                                        "id", macro.getId(),
                                        "parameters", Arrays.asList(ud.getParameters()),
                                        "content", ud.getContent(),
                                        "type", macro.getClass().getName()
                                )
                        ).orElseGet(() -> Map.of(
                                "id", macro.getId(),
                                "type", macroDebugDisplayString(macro)
                        )));
            }
        }
        return response;
    }

    private String macroDebugDisplayString(Identified macro) {
        if (macro instanceof DebugDisplay) {
            final var dd = (DebugDisplay) macro;
            return dd.debugDisplay();
        } else {
            return macro.getClass().getName();
        }
    }

    private Map<String, Object> getBuiltIns() {
        final var scopeList = new ArrayList<Map<String, ?>>();
        for (final var scope : stub.getScopeList()) {
            final var delimiters = scope.getDelimiterPair();
            final var macrosList = scope.getMacros().values().stream().map(Identified::getId).collect(Collectors.toList());
            if (delimiters.open() != null && delimiters.close() != null) {
                scopeList.add(Map.of("delimiters", Map.of("open", delimiters.open(), "close", delimiters.close()),
                        "macros", macrosList));
            }
        }
        return Map.of("macros", scopeList);
    }

    /**
     * Cast the macro to a debuggable user-defined macro type and return it in an optional if it can be cast.
     *
     * @param macro something that identifiable, like a macro
     * @return an optional with a macro in it guaranteed that this is a user-defined macro and debuggable, or else an
     * empty optional.
     */
    private Optional<Debuggable.UserDefinedMacro> getDebuggable(Identified macro) {
        final Optional<?> debuggable;
        if (macro instanceof Debuggable
                && (debuggable = ((Debuggable<?>) macro).debuggable()).isPresent()
                && debuggable.get() instanceof Debuggable.UserDefinedMacro) {
            return Optional.of((Debuggable.UserDefinedMacro) debuggable.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int affinity(String s) {
        if (s.startsWith("http:")) {
            final var connection = new ConnectionStringParser(s);
            try {
                port = Integer.parseInt(connection.getParameters()[0]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("The debugger connection string '" + s + "' is malformed.", nfe);
            }
            client = connection.getOption("client").orElse("");
            cors = connection.getOption("cors").orElse(null);
            return 1000;
        }
        return -1;
    }

    private HttpServer server;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Properties mimeTypes = new Properties();

    @Override
    public void init(Debugger.Stub stub) throws Exception {
        mimeTypes.load(HttpServerDebugger.class.getClassLoader().getResourceAsStream("mime-types.properties"));
        this.stub = stub;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Could not start server on localhost:" + port, ioe);
        }
        createContext(server, Command.ALL);
        createContext(server, Command.VERSION);
        createContext(server, Command.LEVEL);
        createContext(server, Command.ERRORS);
        createContext(server, Command.STATE);
        createContext(server, Command.INPUT);
        createContext(server, Command.INPUT_BEFORE);
        createContext(server, Command.OUTPUT);
        createContext(server, Command.PROCESSING);
        createContext(server, Command.BUILT_IN);
        createContext(server, Command.USER_DEFINED);
        createContext(server, Command.EXECUTE);
        createContext(server, Command.RUN);
        createContext(server, Command.STEP);
        createContext(server, Command.STEP_INTO);
        createContext(server, Command.STEP_OUT);
        createContext(server, Command.QUIT);
        server.createContext("/client", e -> {
                    if (client == null || client.isEmpty()) {
                        respond(e, HTTP_OK, MIME_PLAIN, e.getRemoteAddress().getHostString());
                    } else {
                        respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "404");
                    }
                }
        );
        createStaticContext(server);
        server.setExecutor(executor);
        server.start();
    }

    @Override
    public void close() {
        server.stop(1);
        executor.shutdownNow();
    }

    private void createStaticContext(HttpServer server) {
        server.createContext("/", (e) -> {
            if (client != null && !client.isEmpty() && !Objects.equals(e.getRemoteAddress().getHostString(), client)) {
                respond(e, HTTP_UNAUTHORIZED, MIME_PLAIN, "");
                return;
            }
            if (!Objects.equals("GET", e.getRequestMethod())) {
                respond(e, HTTP_BAD_METHOD, MIME_PLAIN, "");
                return;
            }
            var file = e.getRequestURI().toString().substring(1);
            if (file.isEmpty()) {
                file = "index.html";
            }
            final var extensionStart = file.lastIndexOf('.');
            final String contentType;
            if (extensionStart == -1) {
                contentType = "text/plain";
            } else {
                final var extension = file.substring(extensionStart + 1);
                contentType = Optional.ofNullable(mimeTypes.getProperty(extension)).orElse("text/plain");
            }
            try (final var is = HttpServerDebugger.class.getClassLoader().getResourceAsStream("ui/" + file)) {
                if (is == null) {
                    respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "ui/" + file + " is not found");
                } else {
                    final var content = is.readAllBytes();
                    e.getResponseHeaders().add("Content-Type", contentType);

                    e.sendResponseHeaders(200, content.length);
                    try (final var out = e.getResponseBody()) {
                        out.write(content);
                        out.flush();
                    }
                }
            } catch (IOException ex) {
                respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "");
            }
        });
    }

    private void createContext(HttpServer server, Command command) {
        server.createContext(command.url, (e) -> {
            final var contextPath = e.getHttpContext().getPath();
            final var request = RequestUriParser.parse(e.getRequestURI());
            if (!Objects.equals(contextPath, request.context) &&
                    !Objects.equals(contextPath + "/", request.context)) {
                respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "");
                return;
            }
            if (client != null && !client.isEmpty() && !Objects.equals(e.getRemoteAddress().getHostString(), client)) {
                respond(e, HTTP_UNAUTHORIZED, MIME_PLAIN, "");
                return;
            }
            if (!Objects.equals(command.method.name(), e.getRequestMethod())) {
                respond(e, HTTP_BAD_METHOD, MIME_PLAIN, "");
                return;
            }
            if (!isWaiting) {
                respond(e, HTTP_UNAVAILABLE, "text/plain", "");
                return;
            }
            final Task t;
            if ("POST".equals(e.getRequestMethod())) {
                t = new Task(command, new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            } else {
                t = new Task(command, request.params);
            }
            if (!requestQueue.offer(t)) {
                respond(e, HTTP_UNAVAILABLE, "text/plain", "");
                return;
            }
            try {
                t.waitForDone();
            } catch (InterruptedException interruptedException) {
                respond(e, HTTP_UNAVAILABLE, MIME_PLAIN, "");
                return;
            }
            if (t.isCancelled()) {
                respond(e, HTTP_UNAVAILABLE, MIME_PLAIN, "");
                return;
            }
            respond(e, HTTP_OK, t.contentType, t.getMessage());
            t.acknowledge();
        });
    }


    private void respond(HttpExchange exchange, int status, String contentType, String bodyString) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        if (cors != null) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", cors);
        }
        if (status != 200) {
            exchange.getResponseHeaders().add("Location", "https://http.cat/" + status);
        }
        byte[] body = bodyString.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length);
        try (final var out = exchange.getResponseBody()) {
            out.write(body);
            out.flush();
        }
    }

}
