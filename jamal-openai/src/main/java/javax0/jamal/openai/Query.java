package javax0.jamal.openai;

import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
    private static final String DEFAULT_CACHE_SUB = "openai/";
    private final static File CACHE_ROOT_DIRECTORY = new File(
            EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_HTTPS_CACHE_ENV)
                    .or(() -> Optional.of(DEFAULT_CACHE_ROOT)).map(FileTools::adjustedFileName).get());

    private final String cacheSeed;

    public Query(final String cacheSeed) {
        this.cacheSeed = cacheSeed;
    }

    private File cacheFile = null;

    private String hashCode(final String url, final String content) {
        return subdirs(HexDumper.encode(SHA256.digest(cacheSeed + url + content)));
    }

    private static String subdirs(final String s) {
        return s.replaceAll("([0-9a-fA-F]{8})(?!$)", "$1-").replaceFirst("-", "/");
    }

    private String getCachedResponse(final String url, final String params) throws IOException {
        final var cacheFile = getCachefile(url, params);
        if (cacheFile.exists()) {
            return Files.readString(cacheFile.toPath(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private File getCachefile(final String url, final String params) {
        if (cacheFile == null) {
            final var cacheDir = new File(CACHE_ROOT_DIRECTORY, DEFAULT_CACHE_SUB + hashCode(url, params));
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            cacheFile = new File(cacheDir, "response.json");
        }
        return cacheFile;
    }

    String post(final String url, String params) throws IOException {
        final var cachedResponse = getCachedResponse(url, params);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        final var con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            final var org = ConfigurationReader.getOrganization();
            org.ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));
            final int status = con.getResponseCode();
            final var reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n"));
            if (status != 200) {
                throw new IOException("POST url '" + url + "' returned " + status + "\n" + retval);
            }
            final var cachefile = getCachefile(url, params);
            Files.writeString(cachefile.toPath(), retval, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return retval;
        } finally {
            con.disconnect();
        }
    }

    String get(final String url) throws IOException {
        final var cachedResponse = getCachedResponse(url, "");
        if (cachedResponse != null) {
            return cachedResponse;
        }
        final var con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            final var org = ConfigurationReader.getOrganization();
            org.ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            final int status = con.getResponseCode();
            if (status != 200) {
                throw new IOException("GET url '" + url + "' returned " + status);
            }
            final var reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n")).trim();
            final var cachefile = getCachefile(url, "");
            Files.writeString(cachefile.toPath(), retval, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return retval;
        } finally {
            con.disconnect();
        }
    }
}


