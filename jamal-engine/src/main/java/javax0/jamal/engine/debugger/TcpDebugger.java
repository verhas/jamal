package javax0.jamal.engine.debugger;

import javax0.jamal.api.Debuggable;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class TcpDebugger implements Debugger {

    private enum RunState {
        RUN,
        STEP_IN,
        STEP
    }

    private enum StopState {
        BEFORE,
        AFTER
    }

    public abstract InputStream getIn();

    public abstract OutputStream getOut();

    public abstract Processor getProcessor();

    public abstract void connect() throws IOException;

    private int currentLevel;
    private int stepLevel;
    private RunState state = RunState.STEP_IN;
    private StopState stop;
    String input = "";
    String inputAfter = "";
    String output = "";
    String macros = "";

    /**
     * Read an number from the input stream.
     * <p>
     * The number will be represented in the format {@code nDDDDD} format, where {@code n} is a single decimal number
     * character and {@code DDDDD} is a decimal number of {@code n} characters. For example {@code 3123} will mean the
     * number 123.
     *
     * @param in the stream from which we read the number
     * @return the number read or -1 if there was an error
     */
    private int numberIn(InputStream in) throws IOException {
        byte[] len = new byte[1];
        if( in.read(len) != 1 ){
            return -1;
        }
        int length = len[0] - '0';
        if (length < 1 || length > 9) {
            return -1;
        }
        byte[] n = new byte[length];
        if( in.read(n) != n.length ){
            return -1;
        }
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = 10 * result;
            int d = n[i] - '0';
            if (d < 0 || d > 9) {
                return -1;
            }
            result += d;
        }
        return result;
    }

    private void sendBuffer(String s) throws IOException {
        final var out = getOut();
        numberOut(s.length());
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void sendMessage(String message) throws IOException {
        final var out = getOut();
        out.write("13MSG".getBytes(StandardCharsets.UTF_8));
        sendBuffer(message);
    }

    private void numberOut(int number) throws IOException {
        final var out = getOut();
        final String num = "" + number;
        final int len = num.length();
        if (len > 9) {
            throw new IllegalArgumentException("The level is larger than 10^10. This is total WTF, probably internal error.");
        }
        out.write(("" + len).getBytes(StandardCharsets.UTF_8));
        out.write(num.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private void handle() {
        if (state != RunState.STEP_IN && (state != RunState.STEP || currentLevel > stepLevel)) {
            return;
        }

        try {
            final var in = getIn();
            final var out = getOut();
            boolean prompt = true;
            while (true) {
                if (prompt) {
                    sendMessage("\n" + currentLevel + ":" + (stop == StopState.AFTER ? "A" : "B") + "> ");
                }
                prompt = true;
                out.flush();
                byte[] command = new byte[1];
                int commandLength = in.read(command);
                if (commandLength == 1) {
                    final List<Debuggable.Scope> scopes;
                    switch (command[0]) {
                        case 'q': // exit the debugger and abort the execution of the processor
                            throw new IllegalArgumentException("Debugger was aborted.");
                        case 'r': // run the processor to the end
                            state = RunState.RUN;
                            return;
                        case 's': // step over the macro, do not step into
                            state = RunState.STEP;
                            stepLevel = currentLevel;
                            return;
                        case 'S': // step into the macro
                            state = RunState.STEP_IN;
                            return;
                        case 'i': // send the input of the required level to the client
                            sendBuffer(input);
                            break;
                        case 'I': // send the input after of the required level to the client
                            sendBuffer(inputAfter);
                            break;
                        case 'o': // send the output of the required level to the client
                            sendBuffer(output);
                            break;
                        case 'm': // send the macro text of the required level to the client
                            sendBuffer(macros);
                            break;
                        case 'l': // send the current level to the client
                            numberOut(currentLevel);
                            break;
                        case 'x': // execute a macro in the current processor at the current level
                            int len = numberIn(in);
                            if (len == -1) {
                                sendMessage("Invalid length for the string to be executed\n");
                            } else {
                                byte[] buffer = new byte[len];
                                if( in.read(buffer) != len ){
                                    sendMessage("Error reading expression");
                                    break;
                                }
                                final RunState save = state;
                                state = RunState.RUN;
                                getProcessor().process(Input.makeInput(new String(buffer, StandardCharsets.UTF_8)));
                                state = save;
                            }
                            break;
                        case 'b': // list built in macros
                            scopes = ((Debuggable.MacroRegister) getProcessor().getRegister()).getScopes();
                            numberOut(scopes.size());
                            int i = 0;
                            for (final var scope : scopes) {
                                numberOut(i);
                                final var macros = scope.getMacros();
                                numberOut(macros.size());
                                for (final var macro : macros.values()) {
                                    sendBuffer(macro.getId());
                                }
                                i++;
                            }
                            break;
                        case 'u': // list user defined macros
                            scopes = ((Debuggable.MacroRegister) getProcessor().getRegister()).getScopes();
                            numberOut(scopes.size());
                            int j = 0;
                            for (final var scope : scopes) {
                                numberOut(j);
                                final var macros = scope.getUdMacros();
                                numberOut(macros.size());
                                for (final var macro : macros.values()) {
                                    sendBuffer(macro.getId());
                                    if (macro instanceof Debuggable.UserDefinedMacro) {
                                        final var ud = (Debuggable.UserDefinedMacro) macro;
                                        sendBuffer(ud.getOpenStr());
                                        numberOut(ud.getParameters().length);
                                        for (final var parameter : ud.getParameters()) {
                                            sendBuffer(parameter);
                                        }
                                        sendBuffer(ud.getCloseStr());
                                    }
                                }
                                j++;
                            }
                            break;
                        case 'h': // send help text
                            sendMessage("q QUIT | s STEP | S STEP IN | l LEVEL | h HELP |\n" +
                                "i INPUT BEFORE | I INPUT AFTER | o OUTPUT | m START TEXT |\n" +
                                "x EXECUTE ");
                            break;
                        default:
                            prompt = false;
                            if (!Character.isWhitespace(command[0])) {
                                sendMessage("Invalid character '" + ((char) command[0]) + "'\n");
                            }
                            break;
                    }
                }
            }
        } catch (SocketException se) {
            try {
                connect();
            } catch (IOException e) {
                throw new IllegalArgumentException("Debugger cannot reconnect broken socket");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("There was an exception in the debugger.", e);
        }
    }

    @Override
    public void setStart(CharSequence macro) {
        macros = macro.toString();
        stop = StopState.BEFORE;
        handle();
    }

    @Override
    public void setInput(int level, CharSequence input) {
        currentLevel = level;
        this.input = input.toString();
    }

    @Override
    public void setAfter(int level, CharSequence input, CharSequence output) {
        currentLevel = level;
        inputAfter = input.toString();
        this.output = output.toString();
        stop = StopState.AFTER;
        handle();
    }
}
