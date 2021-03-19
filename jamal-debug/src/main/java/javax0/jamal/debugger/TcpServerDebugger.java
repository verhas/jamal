package javax0.jamal.debugger;

import javax0.jamal.api.Debugger;
import javax0.jamal.tools.ConnectionStringParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerDebugger extends TcpDebugger implements AutoCloseable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    private int port;

    public TcpServerDebugger() {
    }

    public void init(Debugger.Stub stub) throws Exception {
        super.init(stub);
        serverSocket = new ServerSocket(port);
        connect();
    }

    public void connect() throws IOException {
        clientSocket = serverSocket.accept();
        out = clientSocket.getOutputStream();
        in = clientSocket.getInputStream();
    }

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public int affinity(String s) {
        if (s.startsWith("s:")) {
            final var connection = new ConnectionStringParser(s);
            try {
                port = Integer.parseInt(connection.getParameters()[0]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("The debugger connection string '" + s + "' is malformed.", nfe);
            }
            return 1000;
        }
        return -1;
    }
}
