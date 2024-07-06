package javax0.jamal.rest;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.EnumerationParameter;

import java.io.BufferedReader;
import java.io.IOException;
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
        // snipline GetLike
        final var isGetLike = me.equals("http:get") || me.equals("http:delete") || me.equals("http:head") || me.equals("http:options") || me.equals("http:trace");
        final var scanner = newScanner(in, processor);
        // snippet Rest_Parops
        final var urlOpt = isGetLike ? scanner.str("url").optional(): scanner.str("url");
        // specifies the url.
        // This parameter is mandatory unless the macro is used as a GET like macro.
        // In that case, the URL can be the content of the macro.
        final var methodOpt = scanner.enumeration(Method.class).defaultValue(Method.GET);
        final var headers = scanner.list("header");
        // can add one or more headers.
        // The headers are specified as a list of strings in the format 'name: value'.
        // The header name must not be empty or `Content-Type`.
        // To specify the content type you should use the `ContentType` parameter.
        final var contentType = scanner.str("ContentType").optional();
        // can specify the content type of the content sent to the server.
        // It must not be specified when the method is `GET`, `HEAD`, `OPTIONS`, `TRACE`, or `DELETE`.
        final var to = scanner.str("to").optional();
        // can specify the name of a user defined macro that will store the result of the query.
        // After the query is executed, the macro can be used to retrieve the content of the response or the status.
        // When this parameter is specified, the return value of the macro is an empty string.
        // In other cases, the return value is the content of the response.
        final var cache = scanner.str("cache", "file", "cacheFile").optional();
        // Can specify a cache file where the result of the query is stored.
        // If the cache file exists and is not expired, then the content of the cache file is returned.
        // If the cache file exists but reading it causes exception, then an error will happen.
        final var ttl = scanner.number("ttl", "time", "timeToLive", "cacheTimeOut").defaultValue(-1);
        // can specify the time to live of the cache file in seconds.
        // The default value is `-1` that means the cache never expires.
        // end snippet
        scanner.done();

        BadSyntax.when(ttl.isPresent() && !cache.isPresent(), "You cannot specify ttl without specifying cache");
        final Method method = calculateMethod(me, methodOpt);
        BadSyntax.when(isGetLike && contentType.isPresent(), "Content-Type is not allowed for method " + method);

        final String url;
        if (isGetLike && !urlOpt.isPresent()) {
            url = in.toString().trim();
        } else {
            url = urlOpt.get();
        }
        try {
            final Path cacheFile = cache.isPresent() ? Paths.get(FileTools.absolute(reference, cache.get())) : null;
            if (cacheFile != null) {
                final var cachedResult = getCachedResult(cacheFile, ttl.get());
                if (cachedResult.isPresent()) {
                    if (to.isPresent()) {
                        defineMacro(processor, to.get(), cachedResult.get().status, cachedResult.get().content);
                        return "";
                    }
                    return cachedResult.get().content;
                }
            }

            final var con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method.name());
            for (final var h : headers.get()) {
                final var parts = h.split(":", 2);
                BadSyntax.when(parts.length != 2, "Header '" + h + "' is not in the format 'name:value'");
                BadSyntax.when(parts[0].trim().isEmpty(), "Header name is empty");
                BadSyntax.when("content-type".equalsIgnoreCase(parts[0].trim()), "ContentType should be set as a separate option.");
                BadSyntax.when("content-length".equalsIgnoreCase(parts[0].trim()), "Content length is calculated and must not be specified.");
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
            final var responseStream = isOK(status) ? con.getInputStream() : con.getErrorStream();
            final var reader = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            final var content = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

            if (cacheFile != null) {
                Files.writeString(cacheFile, System.currentTimeMillis() + "\n" + status + "\n" + content, StandardCharsets.UTF_8);
            }

            if (to.isPresent()) {
                defineMacro(processor, to.get(), status, content);
                return "";
            }
            BadSyntax.when(!isOK(status), "Request failed with status " + status + " and response '" + content + "'");
            return content;

        } catch (final Exception e) {
            throw new BadSyntax(method.name() + " url '" + url + "' failed", e);
        }
    }

    private static void defineMacro(Processor processor, String to, int status, String content) {
        final var restResult = new RestResult(to, status, content);
        processor.define(restResult);
    }


    private Optional<RestResult> getCachedResult(Path cacheFile, int ttl) throws BadSyntax {
        try {
            if (!Files.exists(cacheFile)) return Optional.empty();
            final var lines = Files.readAllLines(cacheFile, StandardCharsets.UTF_8);
            if (lines.size() < 3) return Optional.empty();
            final var time = Long.parseLong(lines.get(0));
            if (ttl >= 0 && System.currentTimeMillis() - time > ttl * 1000L) return Optional.empty();
            final var status = Integer.parseInt(lines.get(1));
            final var content = String.join("\n", lines.subList(2, lines.size()));
            return Optional.of(new RestResult(null, status, content));
        } catch (IOException | NumberFormatException e) {
            throw new BadSyntax("Exception reading the cache file " + cacheFile, e);
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

    public static class RestResult implements EvaluableVerbatim {
        final String name;

        final int status;
        final String content;

        public RestResult(String name, int status, String content) {
            this.status = status;
            this.name = name;
            this.content = content;
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            if (parameters.length == 0) {
                BadSyntax.when(!isOK(status), "The response is an error response");
                return content;
            }
            BadSyntax.when(parameters.length != 1, "The result macro '" + name + "' can have only one parameter");
            switch (parameters[0]) {
                case "status":
                    return Integer.toString(status);
                case "response":
                    return content;
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



