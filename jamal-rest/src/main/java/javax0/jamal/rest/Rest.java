package javax0.jamal.rest;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.EnumerationParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

// snipline Rest
@Macro.Name({"rest", "http:get", "http:post", "http:put", "http:delete", "http:head", "http:options", "http:trace"})
public class Rest implements Macro, Scanner {

    public enum Method {
        // snippet Method
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE
        // end snippet
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var reference = in.getReference();
        final var me = processor.getId();
        final var isGetLike = me.equals("http:get") || me.equals("http:delete") || me.equals("http:head") || me.equals("http:options") || me.equals("http:trace");
        final var scanner = newScanner(in, processor);
        final var urlOpt = isGetLike ? scanner.str("url") : scanner.str("url").optional();
        final var methodOpt = scanner.enumeration(Method.class).defaultValue(Method.GET);
        final var headers = scanner.list("header");
        final var contentType = scanner.str("ContentType");
        final var to = scanner.str("to").optional();
        final var cache = scanner.str("cache", "file", "cacheFile").optional();
        final var ttl = scanner.str("ttl", "time", "timeToLive", "cacheTimeOut").optional();
        scanner.done();

        BadSyntax.when(ttl.isPresent() && !cache.isPresent(), "You cannot specify ttl without specifying cache");

        final String url;
        if (isGetLike && !urlOpt.isPresent()) {
            url = in.toString().trim();
        } else {
            url = urlOpt.get();
        }

        final Method method = calculateMethod(me, methodOpt);
        try {
            final var con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method.name());
            for (final var h : headers.get()) {
                final var parts = h.split(":");
                BadSyntax.when(parts.length != 2, "Header '" + h + "' is not in the format 'name:value'");
                BadSyntax.when(parts[0].trim().isEmpty(), "Header name is empty");
                BadSyntax.when("content-type".equalsIgnoreCase(parts[0].trim()), "ContentType should be set as a separate option");
                con.setRequestProperty(parts[0].trim(), parts[1].trim());
            }
            switch (method) {
                case GET:
                case HEAD:
                case OPTIONS:
                case TRACE:
                case DELETE:
                    BadSyntax.when(contentType.isPresent(), "Content-Type is not allowed for method " + method);
                    break;
                case POST:
                case PUT:
                    con.setDoOutput(true);
                    con.getOutputStream().write(in.toString().getBytes(StandardCharsets.UTF_8));
                    break;
                default:
                    throw new BadSyntax("Method '" + method.name() + "' is not supported");
            }
            final int status = con.getResponseCode();
            final InputStream responseStream = isOK(status) ? con.getInputStream() : con.getErrorStream();
            final var reader = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n"));
            Result result = new Result(status, retval);


            if (to.isPresent()) {
                final var restResult = new RestResult(to.get());
                restResult.status = result.status;
                restResult.response = result.retval;
                processor.define(restResult);
                return "";
            }
            BadSyntax.when(!isOK(result.status), "Request failed with status " + result.status + " and response '" + result.retval + "'");
            return result.retval;
        } catch (final Exception e) {
            throw new BadSyntax(method.name() + " url '" + url + "' failed", e);
        }
    }

    private static class Result {
        public final int status;
        public final String retval;

        public Result(int status, String retval) {
            this.status = status;
            this.retval = retval;
        }
    }

    private Optional<String> getCachedResult(Path cacheFile, String s) throws BadSyntax {
        try {
            final var lines = Files.readAllLines(cacheFile,StandardCharsets.UTF_8);

        return null;
        } catch (IOException e) {
            throw new BadSyntax("Exception reading the cache file "+cacheFile.toString(),e);
        }
    }

    private static boolean isOK(int status) {
        return status >= 200 && status < 300;
    }

    private static Method calculateMethod(String me, EnumerationParameter methodOpt) throws BadSyntax {
        final Method method;
        if (me.startsWith("http:")) {
            BadSyntax.when(methodOpt.isPresent(), "You cannot specity method for the macro %s", me);
            method = Method.valueOf(me.substring(5).toUpperCase());
        } else {
            method = methodOpt.get(Method.class);
        }
        return method;
    }

    public static class RestResult implements Identified, Evaluable {
        final String name;

        int status;
        String response;

        public RestResult(String name) {
            this.name = name;
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            if (parameters.length == 0) {
                BadSyntax.when(!isOK(status), "The response is an error response");
                return response;
            }
            BadSyntax.when(parameters.length != 1, "The result macro '" + name + "' can have only one parameter");
            switch (parameters[0]) {
                case "status":
                    return Integer.toString(status);
                case "response":
                    return response;
                default:
                    throw new BadSyntax("Unknown parameter '" + parameters[0] + "' for the result '" + name + "'");
            }
        }

        @Override
        public int expectedNumberOfArguments() {
            return 1;
        }

        @Override
        public String getId() {
            return name;
        }
    }
}



