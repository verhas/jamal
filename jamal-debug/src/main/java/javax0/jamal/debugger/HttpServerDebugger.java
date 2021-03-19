package javax0.jamal.debugger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Debugger;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class HttpServerDebugger implements Debugger, AutoCloseable {
    private static final String MIME_PLAIN = "text/plain";
    private String secret = "";
    private String client = "";

    private enum Command {
        LEVEL,
        INPUT,
        INPUT_BEFORE,
        OUTPUT,
        PROCESSING,
        BUILT_IN,
        USER_DEFINED,
        EXECUTE,
        RUN,
        STEP,
        STEP_INTO,
        QUIT
    }

    private static class Task {
        Task(Command command, String message) {
            this(command);
            this.messageBuffer = message;
        }

        Task(Command command) {
            this.command = command;
        }

        void waitForAck() {
            try {
                ack.await();
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Debugger was interrupted", e);
            }
        }

        void waitForDone() throws InterruptedException {
            waitForIt.await();
        }

        void acknowledge() {
            ack.countDown();
        }

        void done() {
            waitForIt.countDown();
        }

        CountDownLatch waitForIt = new CountDownLatch(1);
        CountDownLatch ack = new CountDownLatch(1);
        final Command command;
        String messageBuffer;
        int status = 200;
        String contentType = MIME_PLAIN;

        String getMessage() {
            return messageBuffer;
        }
    }

    private Debugger.Stub stub;

    private final BlockingQueue<Task> request = new LinkedBlockingQueue<>(1);
    private int currentLevel;
    private int stepLevel;
    private RunState state = RunState.STEP_IN;
    String input = "";
    String inputAfter = "";
    String output = "";
    String macros = "";
    int port;

    @Override
    public void setStart(CharSequence macro) {
        macros = macro.toString();
    }

    @Override
    public void setBefore(int level, CharSequence input) {
        currentLevel = level;
        this.input = input.toString();
        output = "";
        macros = "";
        inputAfter = "";
    }

    @Override
    public void setAfter(int level, CharSequence input, CharSequence output) {
        currentLevel = level;
        inputAfter = input.toString();
        this.output = output.toString();
        handle();
    }

    private void handle() {
        if (state != RunState.STEP_IN && (state != RunState.STEP || currentLevel > stepLevel)) {
            return;
        }
        while (true) {
            final Task task;
            try {
                task = request.take();
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Debugger thread was interrupted", e);
            }
            Map<String, Object> response = null;
            final List<Debuggable.Scope> scopes;
            switch (task.command) {
                case QUIT: // exit the debugger and abort the execution of the processor

                    task.done();
                    task.waitForAck();
                    throw new IllegalArgumentException("Debugger was aborted.");
                case RUN: // run the processor to the end
                    state = RunState.RUN;
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
                    task.messageBuffer = input;
                    break;
                case INPUT_BEFORE: // send the input after of the required level to the client
                    task.messageBuffer = inputAfter;
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
                    final RunState save = state;
                    state = RunState.RUN;
                    try {
                        stub.process(new String(buffer, StandardCharsets.UTF_8));
                        response = Map.of("status", "OK");
                    } catch (BadSyntax badSyntax) {
                        try (final var baos = new ByteArrayOutputStream();
                             final var st = new PrintStream(baos)) {
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
                    }
                    state = save;
                    break;
                case BUILT_IN: // list built in macros
                    response = new HashMap<>();
                    scopes = stub.getScopeList();
                    final var scopeList = new ArrayList<>(scopes.size());
                    response.put("macros", scopeList);
                    for (final var scope : scopes) {
                        final var macros = scope.getMacros();
                        final List<String> macrosList = new ArrayList<>(macros.size());
                        scopeList.add(macrosList);
                        for (final var macro : macros.values()) {
                            macrosList.add(macro.getId());
                        }
                    }
                    break;
                case USER_DEFINED: // list user defined macros
                    response = new HashMap<>();
                    scopes = stub.getScopeList();
                    final var udList = new ArrayList<>(scopes.size());
                    response.put("scopes", udList);
                    for (final var scope : scopes) {
                        final var macros = scope.getUdMacros();
                        final List<Map<String, Object>> macrosList = new ArrayList<>(macros.size());
                        udList.add(macrosList);
                        for (final var macro : macros.values()) {
                            if (macro instanceof Debuggable.UserDefinedMacro) {
                                final var ud = (Debuggable.UserDefinedMacro) macro;
                                macrosList.add(
                                    Map.of(
                                        "open", ud.getOpenStr(),
                                        "close", ud.getCloseStr(),
                                        "id", macro.getId(),
                                        "parameters", Arrays.asList(ud.getParameters()),
                                        "content", ud.getContent(),
                                        "type", macro.getClass().getName()
                                    )
                                );
                            } else {
                                macrosList.add(
                                    Map.of(
                                        "id", macro.getId(),
                                        "type", macro.getClass().getName()
                                    )
                                );
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("The enum inside the class has a not handled value: " + task.command);
            }
            if (response != null) {
                task.contentType = "application/json";
                task.messageBuffer = JsonConverter.object2Json(response);
            }
            task.done();
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
            secret = connection.getOption("secret").map(secret -> "/" + secret).orElse("");
            client = connection.getOption("client").orElse("");
            return 1000;
        }
        return -1;
    }

    private HttpServer server;

    @Override
    public void init(Debugger.Stub stub) throws Exception {
        this.stub = stub;
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        createContext(server, "/level", "GET", Command.LEVEL);
        createContext(server, "/input", "GET", Command.INPUT);
        createContext(server, "/inputBefore", "GET", Command.INPUT_BEFORE);
        createContext(server, "/output", "GET", Command.OUTPUT);
        createContext(server, "/processing", "GET", Command.PROCESSING);
        createContext(server, "/macros", "GET", Command.BUILT_IN);
        createContext(server, "/userDefinedMacros", "GET", Command.USER_DEFINED);
        createContext(server, "/execute", "POST", Command.EXECUTE);
        createContext(server, "/run", "POST", Command.RUN);
        createContext(server, "/step", "POST", Command.STEP);
        createContext(server, "/stepInto", "POST", Command.STEP_INTO);
        createContext(server, "/quit", "POST", Command.QUIT);
        server.createContext("/client", e -> {
                if (client == null || client.length() == 0) {
                    response(e, HTTP_OK, MIME_PLAIN, e.getRemoteAddress().getHostString());
                } else {
                    response(e, HTTP_NOT_FOUND, MIME_PLAIN, "404");
                }
            }
        );
        server.createContext("/", e ->

            response(e, HTTP_NOT_FOUND, MIME_PLAIN, "404"));
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @Override
    public void close() {
        server.stop(1);
    }

    private void createContext(HttpServer server, String mapping, String method, Command command) {
        server.createContext(secret + mapping, (e) -> {
            if (!Objects.equals(e.getHttpContext().getPath(), e.getRequestURI().toString()) &&
                !Objects.equals(e.getHttpContext().getPath() + "/", e.getRequestURI().toString())) {
                response(e, HTTP_NOT_FOUND, MIME_PLAIN, "");
                return;
            }
            if (client != null && client.length() > 0 && !Objects.equals(e.getRemoteAddress().getHostString(), client)) {
                response(e, HTTP_UNAUTHORIZED, MIME_PLAIN, "");
                return;
            }
            if (!Objects.equals(method, e.getRequestMethod())) {
                response(e, HTTP_BAD_METHOD, MIME_PLAIN, "");
                return;
            }
            final Task t;
            if ("POST".equals(e.getRequestMethod())) {
                t = new Task(command, new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            } else {
                t = new Task(command);
            }
            request.add(t);
            try {
                t.waitForDone();
            } catch (InterruptedException interruptedException) {
                response(e, HTTP_UNAVAILABLE, MIME_PLAIN, "");
                return;
            }
            response(e, HTTP_OK, t.contentType, t.getMessage());
            t.acknowledge();
        });
    }


    private static void response(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        if (body == null || body.length() == 0) {
            body = "" + status;
            contentType = MIME_PLAIN;
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
        if (status != 200) {
            exchange.getResponseHeaders().add("Location", "https://http.cat/" + status);
        }
        exchange.sendResponseHeaders(status, body.length());
        try (final var out = exchange.getResponseBody()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

}
