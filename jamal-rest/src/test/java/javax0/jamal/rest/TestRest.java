package javax0.jamal.rest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestRest {

    private HttpServer server;
    private int port;
    private String mockResponse = "Mock Response";
    private int mockResoonseCode = 200;

    @BeforeEach
    public void setUp() {

        port = 8001;
        while (true) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                break;
            } catch (IOException e) {
                port++;
            }
        }
        server.createContext("/test", (HttpExchange exchange) -> {
            final var rm = exchange.getRequestMethod();
            final var response = rm + " " + mockResponse;
            exchange.sendResponseHeaders(mockResoonseCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private void setMockResponse(String response) {
        this.mockResponse = response;
    }


    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testREST() throws Exception {
        setMockResponse("This is the rest response");
        for (final var s : List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "TRACE")) {
            TestThat.theInput("{@rest (" + s + " url=http://localhost:" + port + "/test)}").results(s + " " + mockResponse);
        }
    }

    @Test
    void testCachedResult() throws Exception {
        final var testCache = Paths.get("test.cache.txt");
        if (Files.exists(testCache)) Files.delete(testCache);
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}" +
                "{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}"
        ).results("GET " + mockResponse + "GET " + mockResponse);
        if (Files.exists(testCache)) Files.delete(testCache);
    }

    @Test
    void testCachedResultTimeout() throws Exception {
        final var testCache = Paths.get("test.cache.txt");
        if (Files.exists(testCache)) Files.delete(testCache);
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}" +
                "{@http:get (ttl=0 cache=test.cache.txt url=http://localhost:" + port + "/test)}"
        ).results("GET " + mockResponse + "GET " + mockResponse);
        if (Files.exists(testCache)) Files.delete(testCache);
    }

    @Test
    void testCachedResultNoTimeout() throws Exception {
        final var testCache = Paths.get("test.cache.txt");
        if (Files.exists(testCache)) Files.delete(testCache);
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}" +
                "{@http:get (ttl=32000 cache=test.cache.txt url=http://localhost:" + port + "/test)}"
        ).results("GET " + mockResponse + "GET " + mockResponse);
        if (Files.exists(testCache)) Files.delete(testCache);
    }

    @Test
    void testCachedFileTooShort() throws Exception {
        final var testCache = Paths.get("test.cache.txt");
        if (Files.exists(testCache)) Files.delete(testCache);
        Files.writeString(testCache, "123\n", java.nio.charset.StandardCharsets.UTF_8);
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}"
        ).results("GET " + mockResponse);
        if (Files.exists(testCache)) Files.delete(testCache);
    }

    @Test
    void testCachedResultStored() throws Exception {
        final var testCache = Paths.get("test.cache.txt");
        if (Files.exists(testCache)) Files.delete(testCache);
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (to=testi cache=test.cache.txt url=http://localhost:" + port + "/test)}" +
                "{testi}" +
                "{@http:get (to=testii cache=test.cache.txt url=http://localhost:" + port + "/test)}" +
                "{testii}"
        ).results("GET " + mockResponse + "GET " + mockResponse);
        if (Files.exists(testCache)) Files.delete(testCache);
    }

    @Test
    void testGet() throws Exception {
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test)}").results("GET " + mockResponse);
    }

    @Test
    void testAddHeaders() throws Exception {
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test" +
                " header=\"X-set: baba\")}").results("GET " + mockResponse);
    }

    @Test
    void testGetNoUrl() throws Exception {
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get http://localhost:" + port + "/test}").results("GET " + mockResponse);
    }

    @Test
    void testPost() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:post (url=http://localhost:" + port + "/test)}").results("POST " + mockResponse);
    }

    @Test
    void testPut() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:put (url=http://localhost:" + port + "/test)}").results("PUT " + mockResponse);
    }

    @Test
    void testDelete() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:delete (url=http://localhost:" + port + "/test)}").results("DELETE " + mockResponse);
    }

    @Test
    void testOptions() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:options (url=http://localhost:" + port + "/test)}").results("OPTIONS " + mockResponse);
    }

    @Test
    void testHEAD() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:head (url=http://localhost:" + port + "/test)}").results("");
    }

    @Test
    void testRestHEAD() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@rest (HEAD url=http://localhost:" + port + "/test)}").results("");
    }

    @Test
    void testTrace() throws Exception {
        setMockResponse("This is the post response");
        TestThat.theInput("{@http:trace (url=http://localhost:" + port + "/test)}").results("TRACE " + mockResponse);
    }

    @Test
    void testGetSaved() throws Exception {
        setMockResponse("This is the get response");
        TestThat.theInput("{@http:get (to=$response url=http://localhost:" + port + "/test)}" +
                "{$response} " +
                "{$response status} " +
                "{$response response}").results("GET " + mockResponse + " 200 GET " + mockResponse);
    }

    @Test
    void testSavedIsVerbatim() throws Exception {
        setMockResponse("This{is} the get response");
        TestThat.theInput("{@http:get (to=$response url=http://localhost:" + port + "/test)}" +
                "{$response} " +
                "{$response status} " +
                "{$response response}").results("GET " + mockResponse + " 200 GET " + mockResponse);
    }

    @Nested
    class FailCases {
        @Test
        void testHeadersContentLength() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test" +
                    " header=\"Content-Length: baba\")}").throwsBadSyntax("Content length is calculated and must not be specified\\.");
        }

        @Test
        void testHeadersContentType() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test" +
                    " header=\"Content-Type: baba\")}").throwsBadSyntax("ContentType should be set as a separate option\\.");
        }

        @Test
        void testHeadersEmptyName() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test" +
                    " header=\": baba\")}").throwsBadSyntax("Header name is empty");
        }

        @Test
        void testHeaderWrong() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test" +
                    " header=\"baba\")}").throwsBadSyntax("Header 'baba' is not in the format 'name:value'");
        }

        @Test
        void testGetSavedUsedWrong1() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (to=$response url=http://localhost:" + port + "/test)}" +
                    "{$response responsa}").throwsBadSyntax("Unknown parameter 'responsa' for the result '\\$response'");
        }

        @Test
        void testTtlButNoCache() throws Exception {
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (ttl=0 url=http://localhost:" + port + "/test)}")
                    .throwsBadSyntax("You cannot specify ttl without specifying cache");
        }

        @Test
        void testGet404() throws Exception {
            setMockResponse("This is the get response");
            mockResoonseCode = 404;
            TestThat.theInput("{@http:get (url=http://localhost:" + port + "/test)}")
                    .throwsBadSyntax("Request failed with status 404.*");
        }

        @Test
        void testCachedFileInvalidTextReadFails() throws Exception {
            final var testCache = Paths.get("test.cache.txt");
            if (Files.exists(testCache)) Files.delete(testCache);
            byte[] invalidBytes = new byte[]{(byte) 0xC3, (byte) 0x28};
            Files.write(testCache, invalidBytes);
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}"
            ).throwsBadSyntax("Exception reading the cache file test\\.cache\\.txt");
            if (Files.exists(testCache)) Files.delete(testCache);
        }

        @Test
        void testCachedFileInvalidStatus() throws Exception {
            final var testCache = Paths.get("test.cache.txt");
            if (Files.exists(testCache)) Files.delete(testCache);
            Files.writeString(testCache, System.currentTimeMillis() + "\ninvalid number\nsomething content");
            setMockResponse("This is the get response");
            TestThat.theInput("{@http:get (cache=test.cache.txt url=http://localhost:" + port + "/test)}"
            ).throwsBadSyntax("Exception reading the cache file test\\.cache\\.txt");
            if (Files.exists(testCache)) Files.delete(testCache);
        }
    }

}
