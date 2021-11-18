package javax0.jamal.tools;

import javax0.jamal.api.EnvironmentVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class CachedHttpInput {
    private static final int CONNECT_TIMEOUT;
    private static final int READ_TIMEOUT;

    static {
        final var connTimeout = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_CONNECT_TIMEOUT_ENV).orElse("5000");
        CONNECT_TIMEOUT = Integer.parseInt(connTimeout);
    }

    static {
        final var readTimeout = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_READ_TIMEOUT_ENV).orElse("5000");
        READ_TIMEOUT = Integer.parseInt(readTimeout);
    }


    /**
     * Get a file from the cache or from the URL in case the file is not in the cache.
     *
     * @param urlString where the content is
     * @return the content of the file
     * @throws IOException if the file cannot be downloaded. If there file is in the cache but cannot be read then it
     *                     will try to download from the original source and throw and exception only if that also
     *                     fails. A wrong cache configuration will lead slower execution and repeated download but does
     *                     not stop operation.
     */
    public static StringBuilder getInput(String urlString) throws IOException {
        final var url = new URL(urlString);
        final var entry = Cache.getEntry(url);
        final StringBuilder content;
        if (entry.isMiss() || (content = entry.getContent()) == null) {
            return entry.save(readBufferedReader(getBufferedReader(url)));
        } else {
            return content;
        }
    }


    /**
     * Read from the reader and return the lines
     *
     * @param in from where the lines come
     * @return the concatenated lines. There will be a newline after the last line wven if the reader reads something
     * that does not have a terminating new line.
     * @throws IOException when the reader cannot be read.
     */
    static StringBuilder readBufferedReader(BufferedReader in) throws IOException {
        final var content = new StringBuilder();
        try (final BufferedReader ignored = in) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append('\n');
            }
        }
        return content;
    }

    /**
     * Get a buffered reader from a URL.
     *
     * @param url that the reader will read.
     * @return the reader
     * @throws IOException if the response is not OK
     */
    private static BufferedReader getBufferedReader(URL url) throws IOException {
        final var con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setInstanceFollowRedirects(true);
        final int status = con.getResponseCode();
        if (status != 200) {
            throw new IOException("GET url '" + url + "' returned " + status);
        }
        return new HttpBufferedReader(
            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), con);
    }

    /**
     * A special reader that disconnects the underlying connection when the reader is closed.
     */
    private static class HttpBufferedReader extends BufferedReader {
        private final HttpURLConnection con;

        public HttpBufferedReader(Reader in, HttpURLConnection con) {
            super(in);
            this.con = con;
        }

        @Override
        public void close() throws IOException {
            super.close();
            con.disconnect();
        }
    }


}
