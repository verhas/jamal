package javax0.jamal.debugger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class TestRequestUriParser {

    @Test
    @DisplayName("Parse http://localhost:8080/aburakan?parameter=value")
    void testParseSimpleCase() throws URISyntaxException {
        final var request = RequestUriParser.parse(new URI("http://localhost:8080/aburakan?parameter=value"));
        Assertions.assertTrue(request.params.containsKey("parameter"));
        Assertions.assertEquals("value", request.params.get("parameter"));
        Assertions.assertEquals("http://localhost:8080/aburakan", request.context);
        Assertions.assertEquals("parameter=value", request.queryString);
    }

    @Test
    @DisplayName("Parse http://localhost:8080/aburakan?parameter&another=")
    void testParseParamNoValue() throws URISyntaxException {
        final var request = RequestUriParser.parse(new URI("http://localhost:8080/aburakan?parameter&another="));
        Assertions.assertTrue(request.params.containsKey("parameter"));
        Assertions.assertEquals("", request.params.get("parameter"));
        Assertions.assertTrue(request.params.containsKey("another"));
        Assertions.assertEquals("", request.params.get("another"));
        Assertions.assertEquals("http://localhost:8080/aburakan", request.context);
        Assertions.assertEquals("parameter&another=", request.queryString);
    }

    @Test
    @DisplayName("Parse http://localhost:8080/aburakan?parameter=value&another=param")
    void testParseMultipleParams() throws URISyntaxException {
        final var request = RequestUriParser.parse(new URI("http://localhost:8080/aburakan?parameter=value&another=param"));
        //
        Assertions.assertTrue(request.params.containsKey("parameter"));
        Assertions.assertEquals("value", request.params.get("parameter"));
        Assertions.assertTrue(request.params.containsKey("another"));
        Assertions.assertEquals("param", request.params.get("another"));
        //
        Assertions.assertEquals("http://localhost:8080/aburakan", request.context);
        Assertions.assertEquals("parameter=value&another=param", request.queryString);
    }

    @Test
    @DisplayName("Parse http://localhost:8080/aburakan?")
    void testParseQMAtEnd() throws URISyntaxException {
        final var request = RequestUriParser.parse(new URI("http://localhost:8080/aburakan?"));
        Assertions.assertFalse(request.params.containsKey("parameter"));
        Assertions.assertEquals("http://localhost:8080/aburakan", request.context);
        Assertions.assertEquals("", request.queryString);
    }

    @Test
    @DisplayName("Parse http://localhost:8080/aburakan")
    void testParseNoQM() throws URISyntaxException {
        final var request = RequestUriParser.parse(new URI("http://localhost:8080/aburakan"));
        Assertions.assertFalse(request.params.containsKey("parameter"));
        Assertions.assertEquals("http://localhost:8080/aburakan", request.context);
        Assertions.assertEquals("", request.queryString);
    }

}
