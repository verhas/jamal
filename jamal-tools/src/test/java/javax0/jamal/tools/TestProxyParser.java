package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class TestProxyParser {

    @Test
    void testHttpHost() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("http://127.0.0.1:8080" ));
    }

    @Test
    void testHttpsHost() throws Exception {
        final var sut = ProxyParser.parse("https://127.0.0.1:8080" );
        Assertions.assertEquals(new InetSocketAddress(InetAddress.getByName("127.0.0.1" ), 8080), sut.address());
        Assertions.assertEquals(Proxy.Type.HTTP, sut.type());
    }

    @Test
    void testSocksHost() throws Exception {
        final var sut = ProxyParser.parse("socks:127.0.0.1:8080" );
        Assertions.assertEquals(new InetSocketAddress(InetAddress.getByName("127.0.0.1" ), 8080), sut.address());
        Assertions.assertEquals(Proxy.Type.SOCKS, sut.type());
    }

    @Test
    void testDefaultPort() throws Exception {
        final var sut = ProxyParser.parse("https://127.0.0.1" );
        Assertions.assertEquals(new InetSocketAddress(InetAddress.getByName("127.0.0.1" ), 8080), sut.address());
        Assertions.assertEquals(Proxy.Type.HTTP, sut.type());
    }

    @Test
    void testNoProxy() throws Exception {
        final var sut = ProxyParser.parse(null);
        Assertions.assertEquals(Proxy.NO_PROXY, sut);
    }

    @Test
    void testBadHost() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("socks:127.0.0.:8080" ));
    }

    @Test
    void testBadPort1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("socks:127.0.0.1:not a number" ));
    }

    @Test
    void testBadPortNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("socks:127.0.0.1:-1" ));
    }

    @Test
    void testBadPortTooBig() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("socks:127.0.0.1:65536" ));
    }

    @Test
    void testBadNoPort() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("socks:127.0.0.1:" ));
    }

    @Test
    void testBadProtocol() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("sockets:127.0.0.1:8080" ));
    }

    @Test
    void testNoPortNoProtocol() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ProxyParser.parse("127.0.0.1" ));
    }
}
