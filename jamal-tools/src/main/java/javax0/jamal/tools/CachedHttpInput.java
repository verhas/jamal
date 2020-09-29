package javax0.jamal.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CachedHttpInput {
    private static final String JAMAL_CONNECT_TIMEOUT = "JAMAL_CONNECT_TIMEOUT";
    private static final String JAMAL_READ_TIMEOUT = "JAMAL_READ_TIMEOUT";
    private static final int CONNECT_TIMEOUT;
    private static final int READ_TIMEOUT;


    static {
        final var connTimeout = System.getenv(JAMAL_CONNECT_TIMEOUT);
        if (connTimeout != null) {
            CONNECT_TIMEOUT = Integer.parseInt(connTimeout);
        } else {
            CONNECT_TIMEOUT = 5000;
        }
    }

    static {
        final var readTimeout = System.getenv(JAMAL_READ_TIMEOUT);
        if (readTimeout != null) {
            READ_TIMEOUT = Integer.parseInt(readTimeout);
        } else {
            READ_TIMEOUT = 5000;
        }
    }

    public static StringBuilder geInput(String urlString) throws IOException {
        final var url = new URL(urlString);
        final var entry = Cache.getEntry(url);
        if (entry.isMiss()) {
            return entry.save(readBufferedReader(getBufferedReader(url)));
        } else {
            return entry.getContent();
        }
    }


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


    private static BufferedReader getBufferedReader(URL url) throws IOException {
        final var con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setInstanceFollowRedirects(true);
        final int status = con.getResponseCode();
        if (status != 200) {
            throw new IOException("GET url '" + url.toString() + "' returned " + status);
        }
        return new HttpBufferedReader(
            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8), con);
    }

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
