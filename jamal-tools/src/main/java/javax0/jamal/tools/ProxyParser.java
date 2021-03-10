package javax0.jamal.tools;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;

public class ProxyParser {

    public static Proxy parse(String fullProxyString) {
        if (fullProxyString == null) {
            return Proxy.NO_PROXY;
        }
        Proxy.Type type;
        final String hostAndPort;
        if (fullProxyString.startsWith("https://" )) {
            type = Proxy.Type.HTTP;
            hostAndPort = fullProxyString.replaceAll("https://", "" );
        } else if (fullProxyString.startsWith("socks:" )) {
            type = Proxy.Type.SOCKS;
            hostAndPort = fullProxyString.replaceAll("socks:", "" );
        } else {
            throw new IllegalArgumentException("Proxy definition '"
                + fullProxyString + "' must start with http://, https:// or socks:" );
        }
        final var portStart = hostAndPort.indexOf(':') + 1;
        final int port;
        final String host;
        if (portStart == 0) {
            port = 8080;
            host = hostAndPort;
        } else {
            try {
                port = Integer.parseInt(hostAndPort.substring(portStart));
                if (port < 0 || port > 65535) {
                    throw new IllegalArgumentException(
                        "proxy port specified in the proxy definition has to be positive and <= 65535" );
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("proxy port specified in proxy definition has to be a number.", nfe);
            }
            host = hostAndPort.substring(0, portStart - 1);
        }
        final InetSocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Proxy definition specifies a proxy that does not work.", e);
        }
        return new Proxy(type, address);
    }
}
