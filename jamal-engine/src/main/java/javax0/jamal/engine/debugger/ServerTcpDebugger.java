package javax0.jamal.engine.debugger;

import javax0.jamal.engine.Processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTcpDebugger extends TcpDebugger implements AutoCloseable {

    final private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    private final Processor processor;


    ServerTcpDebugger(Processor processor, int port) throws IOException {
        this.processor = processor;
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
        }catch (IOException ignored){}
    }

    public Processor getProcessor() {
        return processor;
    }
}
