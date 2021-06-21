package javax0.jamal.debugger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Debugger;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.ConnectionStringParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class HttpServerDebugger implements Debugger, AutoCloseable {
    private static final String MIME_PLAIN = "text/plain";
    public static final String MIME_APPLICATION_JSON = "application/json";
    private String client = "";
    private String cors = "";

    private enum Method {
        GET, POST
    }

    private enum Command {
        ALL("all", Method.GET),
        VERSION("version", Method.GET),
        LEVEL("level", Method.GET),
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
     * The thread handling an HTTP request uses this queue to send a task to the Jamal main thread. Since this is not a
     * heavy duty server the size of the queue is limited to one. There is no reason to increase this value because
     * there should only be one client and every request should come from a human interaction.
     */
    private final BlockingQueue<Task> requestQueue = new LinkedBlockingQueue<>(1);

    private String handleState;

    /**
     * Structure describing the task, what the debugger is asked to do.
     */
    private static class Task {
        boolean taskCancelled = false;
        CountDownLatch waitForIt = new CountDownLatch(1);
        CountDownLatch ack = new CountDownLatch(1);
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
         * After the debugger was done it may wait for acknowledgement. For example asking the debugger to quit or run
         * may result the main Jamal thread to finish before the http server thread services the client sending the
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
         * task and the http server can serve the client with this information.
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
         * The debugger thread calls this method to signal that the task is finished and the HTTP server thread can use
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

    private int currentLevel;
    private int stepLevel;
    private RunState state = RunState.STEP_IN;
    private final List<String> breakpoints = new ArrayList<>();
    String inputBefore = "";
    CharSequence input;
    String inputAfter = "";
    String output = "";
    String macros = "";
    int port;

    @Override
    public void setStart(CharSequence macro) {
        if (state != RunState.NODEBUG) {
            macros = macro.toString();
            handleState = "BEFORE";
            handle();
        }
    }

    @Override
    public void setBefore(int level, CharSequence input) {
        if (state != RunState.NODEBUG) {
            currentLevel = level;
            this.input = input;
            this.inputBefore = input.toString();
            macros = "";
            inputAfter = "";
        }
    }

    @Override
    public void setAfter(int level, CharSequence output) {
        if (state != RunState.NODEBUG) {
            currentLevel = level;
            inputAfter = input.toString();
            this.output = output.toString();
            handleState = "AFTER";
            handle();
        }
    }

    private void addToResponse(Task task, Map<String, Object> response, Command command, Object value) {
        final var key = command.url.substring(1);
        if (task.params.containsKey(key)) {
            response.put(key, value);
        }
    }

    volatile boolean isWaiting;

    private void handle() {
        if (state == RunState.RUN && handleState.equals("BEFORE")) {
            for (final var breakpoint : breakpoints) {
                if (breakpoint != null && breakpoint.length() > 0 && macros.contains(breakpoint)) {
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
                final Task task;
                try {
                    task = requestQueue.take();
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Debugger thread was interrupted", e);
                }
                Map<String, Object> response = null;
                switch (task.command) {
                    case ALL:
                        response = new HashMap<>();
                        addToResponse(task, response, Command.LEVEL, "" + currentLevel);
                        addToResponse(task, response, Command.STATE, handleState);
                        addToResponse(task, response, Command.INPUT, inputAfter);
                        addToResponse(task, response, Command.OUTPUT, output);
                        addToResponse(task, response, Command.INPUT_BEFORE, inputBefore);
                        addToResponse(task, response, Command.PROCESSING, macros);
                        addToResponse(task, response, Command.BUILT_IN, getBuiltIns());
                        addToResponse(task, response, Command.USER_DEFINED, getUserDefineds());
                        addToResponse(task, response, Command.VERSION, getJamalVersion());
                        break;
                    case VERSION:
                        response = getJamalVersion();
                        break;
                    case STATE:
                        task.messageBuffer = handleState;
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
                            task.messageBuffer = stub.process(new String(buffer, StandardCharsets.UTF_8));
                            task.contentType = MIME_PLAIN;
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
                    case BUILT_IN: // list built in macros
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
            try {
                Task task;
                while ((task = requestQueue.poll(0, TimeUnit.NANOSECONDS)) != null) {
                    task.cancel();
                }
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Debugger thread was interrupted", e);
            }
        }
    }

    private Map<String, Object> getJamalVersion() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        return Map.of("version", version.getProperty("version"));
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
                        "type", macro.getClass().getName()
                    )));
            }
        }
        return response;
    }

    private Map<String, Object> getBuiltIns() {
        final Map<String, Object> response = new HashMap<>();
        final List<Debuggable.Scope> scopes = stub.getScopeList();
        final var scopeList = new ArrayList<Map<String, ?>>(scopes.size());
        response.put("macros", scopeList);
        for (final var scope : scopes) {
            final var macros = scope.getMacros();
            final var delimiters = scope.getDelimiterPair();
            final List<String> macrosList = new ArrayList<>(macros.size());
            for (final var macro : macros.values()) {
                macrosList.add(macro.getId());
            }
            if (delimiters.open() != null && delimiters.close() != null) {
                scopeList.add(Map.of("delimiters", Map.of("open", delimiters.open(), "close", delimiters.close()),
                    "macros", macrosList));
            }
        }
        return response;
    }

    private Optional<Debuggable.UserDefinedMacro> getDebuggable(Identified macro) {
        final Optional<?> debuggable;
        if (macro instanceof Debuggable
            && (debuggable = ((Debuggable<?>) macro).debuggable()).isPresent()
            && debuggable.get() instanceof Debuggable.UserDefinedMacro) {
            return (Optional<Debuggable.UserDefinedMacro>) debuggable;
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
                if (client == null || client.length() == 0) {
                    respond(e, HTTP_OK, MIME_PLAIN, e.getRemoteAddress().getHostString());
                } else {
                    respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "404");
                }
            }
        );
        createStaticContext(server);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @Override
    public void close() {
        server.stop(1);
    }

    private void createStaticContext(HttpServer server) {
        server.createContext("/", (e) -> {
            if (client != null && client.length() > 0 && !Objects.equals(e.getRemoteAddress().getHostString(), client)) {
                respond(e, HTTP_UNAUTHORIZED, MIME_PLAIN, "");
                return;
            }
            if (!Objects.equals("GET", e.getRequestMethod())) {
                respond(e, HTTP_BAD_METHOD, MIME_PLAIN, "");
                return;
            }
            var file = e.getRequestURI().toString().substring(1);
            if (file.length() == 0) {
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
            final var request = RequestUriParser.parse(e.getRequestURI().toString());
            if (!Objects.equals(contextPath, request.context) &&
                !Objects.equals(contextPath + "/", request.context)) {
                respond(e, HTTP_NOT_FOUND, MIME_PLAIN, "");
                return;
            }
            if (client != null && client.length() > 0 && !Objects.equals(e.getRemoteAddress().getHostString(), client)) {
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
