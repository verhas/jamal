package javax0.jamal.openai;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Collectors;

public class Query {

    private static final String DEFAULT_CACHE_ROOT = "~/.jamal/cache/";
    private static final String DEFAULT_CACHE_SUB = ".openai/";
    private final static File CACHE_ROOT_DIRECTORY = new File(
            EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_HTTPS_CACHE_ENV)
                    .or(() -> Optional.of(DEFAULT_CACHE_ROOT)).map(FileTools::adjustedFileName).get());

    private final Options opt;

    public Query(final Options opt) {
        this.opt = opt;
    }

    private File cacheFile = null;

    private String hashCode(final String content) {
        return createSubDirNames(HexDumper.encode(SHA256.digest(opt.cacheSeed + opt.url + normalizeJSON(content))));
    }

    private String normalizeJSON(final String content) {
        return new JSONObject(content).toString();
    }

    private String createSubDirNames(final String s) {
        final var h = s.replaceAll("([0-9a-fA-F]{8})(?!$)", "$1-");
        return opt.local ? h : h.replaceFirst("-", "/");
    }

    private String getCachedResponse(final String params) throws IOException {
        getCachefile(params);
        if (cacheFile.exists()) {
            return Files.readString(cacheFile.toPath(), StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * Memoized cache file reader. If the field cacheFile is already set then it is used.
     *
     * @param params
     */
    private void getCachefile(final String params) {
        if (cacheFile == null) {
            final var cacheDir = new File((opt.local ? opt.top.getParentFile() : CACHE_ROOT_DIRECTORY), DEFAULT_CACHE_SUB + hashCode(params));
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cacheFile = new File(cacheDir, "response.json");
        }
    }

    private enum METHOD {
        GET, POST
    }

    private String doHttp(METHOD method, String params) throws IOException, BadSyntax {
        final var cachedResponse = getCachedResponse(params);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        if (opt.sealed) {
            throw new BadSyntax("The operation was sealed, but does not have cached response.");
        }
        final var con = (HttpURLConnection) new URL(opt.url).openConnection();
        try {
            con.setRequestMethod(method.name());
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            ConfigurationReader.getOrganization().ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            if (method == METHOD.POST) {
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                con.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));
            }
            final int status = con.getResponseCode();
            final InputStream responseStream = (status >= 200 && status <300) ? con.getInputStream() : con.getErrorStream();
            final var reader = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n"));
            if (status != 200) {
                throw new BadSyntax(method.name()+ " url '" + opt.url + "' returned " + status + "\n" + retval);
            }
            getCachefile(params);
            Files.writeString(cacheFile.toPath(), retval, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return retval;
        } finally {
            con.disconnect();
        }
    }

    String post(String params) throws IOException, BadSyntax {
        return doHttp(METHOD.POST, params);
    }

    String get() throws IOException, BadSyntax {
        return doHttp(METHOD.GET, "");
    }
}


