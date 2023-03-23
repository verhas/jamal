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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Class to execute an openAI query synchronously or asynchronously.
 * The query can be a GET or a POST.
 * The parameters of the query are stored in the {@link Options} object.
 * <p>
 * The authorization key is automatically added to the request.
 * It is fetched from the environment variable {@code OPENAI_API_KEY}.
 * If there is an organization configured in teh environment variable {@code OPENAI_ORGANIZATION} it is also added to the request.
 * <p>
 * The query is executed by the {@link #doHttp(String)} method.
 * It should be invoked through the {@link #get()} or {@link #post(String)} methods.
 * <p>
 * The query is executed synchronously if the {@link Options#asynch} is {@code false}.
 * In this case the {@link #get()} or {@link #post(String)} methods return the response.
 * The code does not check whether the response is a valid JSON or not.
 * <p>
 * The response is cached in a file.
 * The file name is the SHA256 hash of the URL and the parameters.
 * For the calculation of the hash the parameters are normalized.
 * The {@code parameters} string has to be a proper JSON object.
 * The calculation also includes the {@link Options#cacheSeed} parameter to trigger a new download every time the seed value changes.
 * <p>
 * The results are saved into a cache file.
 * The cache file name is "response.json" and it is stored in a directory named by the hash code.
 * The directory is stored in the cache root directory.
 * The cache root directory is the value of the environment variable {@code JAMAL_HTTPS_CACHE_ENV} or the directory of the document when the {@link Options#local} is {@code true}.
 * This possibility is used to attach the downloaded cache files to the document, for example adding them to the git repository and creating a "sealed" document.
 * For more information on "sealed" option see the description below.
 * <p>
 * The code also writes a "cache.log" file into the cache directory to log the requests and responses.
 * <p>
 * When executed asynchronously the {@link #get()} or {@link #post(String)} methods return the cached result if it exists.
 * If there is no cached result they start the download as a background task and throw an exception or return an error JSON.
 * The background task is executed and the result gets into the cache file.
 * <p>
 * The asynchronous execution should be used in an environment, where Jamal is executed many times in a single JVM.
 * The example is the IntelliJ AsciiDoc plugin.
 * When using the plugin the rendering of a document may take minutes if there are multiple calls to the OpenAI API.
 * The asynchronous execution allows the user to continue working on the document while the API calls are executed in the background.
 * In the meantime the rendered response will show and error or whatever the macros in the document display in the case of the error JSON.
 * <p>
 * The option {@link Options#fallible} controls whether the asynchronous execution throws an exception or returns an error JSON.
 * This option also makes the methods return the error JSON when the response status is not 2XX.
 * Other errors, like no cache for sealed request (see below), or the inability to connect to the server are not affected by this option.
 * <p>
 * When the {@link Options#sealed} is {@code true} the result is an error if there is no cached response.
 * <p>
 * For examples how to use this class see {@link LowLevelApi}.
 */
public class Query {

    /**
     * The default cache root directory is {@code ~/.jamal/cache/}.
     * The {@code ~/.jamal/cache/} is the default value of the environment variable {@code JAMAL_HTTPS_CACHE_ENV}.
     * This configuration is shared with Jamal's HTTPS module.
     */
    private static final String DEFAULT_CACHE_ROOT = "~/.jamal/cache/";

    /**
     * The default cache subdirectory is {@code .openai/}.
     * Since the cache root directory is shared with the HTTPS module the subdirectory is used to separate the cache
     * files of the two modules.
     */
    private static final String DEFAULT_CACHE_SUB = ".openai/";

    /**
     * The cache root directory is the value of the environment variable {@code JAMAL_HTTPS_CACHE_ENV} or the directory
     * of the document when the {@link Options#local} is {@code true}.
     */
    private final static File CACHE_ROOT_DIRECTORY = new File(
            EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_HTTPS_CACHE_ENV)
                    .or(() -> Optional.of(DEFAULT_CACHE_ROOT)).map(FileTools::adjustedFileName).get());

    /**
     * The file name to store the response in the cache directory.
     */
    public static final String CACHE_FILE_NAME = "response.json";
    /**
     * The file name to store the request and response logs along with the time stamps at the end in the cache directory.
     */
    public static final String CACHE_LOG_FILE_NAME = "cache.log";
    private static final String POST = "POST";
    private static final String GET = "GET";
    public static final String STD_DT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * The options of the query.
     */
    private final Options opt;

    /**
     * This builder is used during the download process to build the log information about the download that gets saved  into the {@link #CACHE_LOG_FILE_NAME} file.
     */
    private final StringBuilder requestResponseLogDump = new StringBuilder();

    /**
     * The time stamps of the query. Note that the {@code timeScheduled} will be zero displayed as the EPOC date when the operation is executed synchronously.
     */
    private long timeScheduled, timeStarted, timeFinished;
    private AtomicInteger asynchCounter = new AtomicInteger(0);
    private String responseBody;
    private String requestBody;


    private File cacheLog;
    private File cacheFile;

    /**
     * The map of the tasks that are running asynchronously.
     */
    private static final Map<String, Query> tasks = new HashMap<>();

    /**
     * Create a new query with the given options.
     *
     * @param opt the options of the query
     */
    public Query(final Options opt) {
        this.opt = opt;
    }

    /**
     * Execute a POST request with the given request body.
     *
     * @param requestBody the request body, must be a properly formatted JSON object
     * @return the response body, or the error JSON if the {@link Options#fallible} is {@code true} and the operation is
     * asynchronous and not cached
     * @throws IOException if the connection fails
     * @throws BadSyntax when some error happens
     */
    String post(String requestBody) throws IOException, BadSyntax {
        this.requestBody = requestBody;
        return doCachedHttp(POST);
    }

    String get() throws IOException, BadSyntax {
        this.requestBody = "";
        return doCachedHttp(GET);
    }

    private String doCachedHttp(String method) throws IOException, BadSyntax {
        final var cachedResponse = getCachedResponse();
        if (cachedResponse != null) {
            return cachedResponse;
        }
        if (opt.sealed) {
            throw new BadSyntax("The operation was sealed, but does not have cached response.");
        }
        if (opt.asynch) {
            final long delta;
            synchronized (tasks) {
                if (!tasks.containsKey(cacheFile.getAbsolutePath())) {
                    final var task = new FutureTask<>(() -> doHttp(method));
                    timeScheduled = System.currentTimeMillis();
                    tasks.put(cacheFile.getAbsolutePath(), this);
                    new Thread(task).start();
                } else {
                    final Query query = tasks.get(cacheFile.getAbsolutePath());
                    timeScheduled = query.timeScheduled;
                    asynchCounter = query.asynchCounter;
                }
                asynchCounter.incrementAndGet();
                delta = System.currentTimeMillis() - timeScheduled;
            }
            if (!opt.fallible) {
                throw new BadSyntax(String.format("The operation is running asynchronously, and does not have cached response for '%s'.", opt.url));
            }
            return asynchProgressJson(delta);
        }
        return doHttp(method);
    }

    private String asynchProgressJson(final long delta) {
        final String fmt = "\"%s\":\"%s\"";
        final String fmtN = "\"%s\":%s";
        return "{" +
                String.format(fmt, "message", "Asynchronous download is running") + "," +
                String.format(fmtN, "counter", asynchCounter.get()) + "," +
                "process : {" +
                String.format(fmtN, "process-id", ProcessHandle.current().pid()) + "," +
                String.format(fmt, "cmd", ProcessHandle.current().info().commandLine().orElse("")) + "," +
                String.format(fmt, "thread", Thread.currentThread().getName()) +
                "}," +
                String.format(fmt, "id", cacheFile.getParentFile().getName()) + "," +
                String.format(fmt, "running-since", new SimpleDateFormat(STD_DT_FORMAT).format(new Date(timeScheduled))) + "," +
                String.format(fmtN, "download-time", delta) +
                "}";
    }

    private String doHttp(String method) throws IOException, BadSyntax {
        timeStarted = System.currentTimeMillis();
        final var con = (HttpURLConnection) new URL(opt.url).openConnection();
        try {
            con.setRequestMethod(method);
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            ConfigurationReader.getOrganization().ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            if (POST.equals(method)) {
                con.setRequestProperty("Content-Type", "application/json");
                appendRequestLogDump(con);
                con.setDoOutput(true);
                con.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            } else {
                appendRequestLogDump(con);
            }

            final int status = con.getResponseCode();
            final InputStream responseStream = (status >= 200 && status < 300) ? con.getInputStream() : con.getErrorStream();
            final var reader = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            responseBody = reader.lines().collect(Collectors.joining("\n"));
            calculateCacheFileObject();
            timeFinished = System.currentTimeMillis();
            writeCacheOutput();
            // without removing this from the map removing the cache file from disk would not ignite new download
            synchronized (tasks) {
                tasks.remove(cacheFile.getAbsolutePath());
            }
            if (!(status >= 200 && status < 300) && !opt.fallible) {
                appendResponseLogDump(con);
                throw new BadSyntax(method + " url '" + opt.url + "' request/response is " + requestResponseLogDump);
            }
            return responseBody;
        } finally {
            con.disconnect();
        }
    }

    /**
     * Get the cache directory name. This is not the full pathy, only the name that is under the local or central configured cache directory.
     * <p>
     * The calculation of the cache directory name is based on the {@link Options#cacheSeed} and the {@link Options#url} and the request body.
     * These are concatenated and then hashed using SHA-256 and finally converted to a hexadecimal string.
     *
     * @return the cache directory name
     */
    private String cacheDirName() {
        return createSubDirNames(HexDumper.encode(SHA256.digest(opt.cacheSeed + opt.url + normalizeJSON(requestBody))));
    }

    /**
     * Normalize the JSON string so that hash calculation does not depend on the formatting of the JSON file.
     *
     * @param content the presumably unformatted JSON string
     * @return the formatted and thus normalized JSON string
     */
    private static String normalizeJSON(final String content) {
        if( content == null || content.isBlank() ) {
            return "";
        }
        return new JSONObject(content).toString();
    }

    /**
     * Convert the hexadecimal hashcode into a directory name or structure.
     * <p>
     * The hash code is a 64 character hexadecimal string.
     * The first step inserts a dash after every 8 characters.
     * If the cache is local then this result is returned and will be used in the {@code .openai} directory under
     * the document directory.
     * <p>
     * If the cache is not local then the cache directory will hold all the responses for many documents processed on the same machine.
     * This may be a lot of file, therefore a deeper directory structure may be desirable.
     * To alleviate this a '/' character is inserted after the 3rd hex character converting the first three characters into a separate directory name.
     * This means that the results will be stored in 4096 directories.
     *
     * @param s the hash code of the request hexadecimal
     * @return the name of the cache directory
     */
    private String createSubDirNames(final String s) {
        final var h = s.replaceAll("([0-9a-fA-F]{8})(?!$)", "$1-");
        return opt.local ? h : h.substring(0, 3) + "/" + h.substring(3);
    }

    /**
     * Read the cached response if it exists.
     *
     * @return the cached response or {@code null} if the cache file does not exist, or it is not readable.
     */
    private String getCachedResponse() {
        calculateCacheFileObject();
        try {
            if (cacheFile.exists()) {
                return Files.readString(cacheFile.toPath(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Create the cache file object and the cache log file object. Take the hash code of the request body and the options.
     * Safe to call multiple times.
     */
    private void calculateCacheFileObject() {
        if (cacheFile == null) {
            File cacheDir = new File((opt.local ? opt.top.getParentFile() : CACHE_ROOT_DIRECTORY), DEFAULT_CACHE_SUB + cacheDirName());
            if (!cacheDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cacheDir.mkdirs();
            }
            cacheFile = new File(cacheDir, CACHE_FILE_NAME);
            cacheLog = new File(cacheDir, CACHE_LOG_FILE_NAME);
        }
    }

    private void writeCacheOutput() throws IOException {
        Files.writeString(cacheFile.toPath(), responseBody, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        final var sdf = new SimpleDateFormat(STD_DT_FORMAT);
        requestResponseLogDump.append("\n")
                .append(String.format("time-scheduled: %s\n", sdf.format(new Date(timeScheduled))))
                .append(String.format("time-started: %s\n", sdf.format(new Date(timeStarted))))
                .append(String.format("time-finished: %s\n", sdf.format(new Date(timeFinished))))
                .append(String.format("download-time: %dms\n", timeFinished - timeStarted))
        ;
        Files.writeString(cacheLog.toPath(), requestResponseLogDump, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void appendRequestLogDump(final HttpURLConnection con) {
        requestResponseLogDump.append("-->\n");
        requestResponseLogDump.append("HTTP/1.1 ").append(con.getRequestMethod()).append(" ").append(con.getURL()).append("\n");
        con.getRequestProperties().forEach((k, v) -> requestResponseLogDump.append(k).append(": ").append(v).append("\n"));
        requestResponseLogDump.append("\n");
        requestResponseLogDump.append(requestBody);
    }

    private void appendResponseLogDump(final HttpURLConnection con) throws IOException {
        requestResponseLogDump.append("<--\n");
        requestResponseLogDump.append("HTTP/1.1 ").append(con.getResponseCode()).append(" ").append(con.getResponseMessage()).append("\n");
        con.getHeaderFields().forEach((k, v) -> requestResponseLogDump.append(k).append(": ").append(v).append("\n"));
        requestResponseLogDump.append("\n");
        requestResponseLogDump.append(responseBody);
    }
}


