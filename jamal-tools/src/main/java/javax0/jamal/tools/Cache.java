package javax0.jamal.tools;

import javax0.jamal.api.EnvironmentVariables;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * A cache implementation that can store the strings downloaded from certain https addresses in files.
 * The cache elements never expire by default.
 * The assumption is that the resources downloaded from a web page are versioned, and the url contains the version number.
 * <p>
 * URLs containing the literal {@code SNAPSHOT} are not cached.
 * In all other cases, we assume resources NEVER change.
 * <p>
 * The structure of the files and directories were influenced by the structure of the Maven local repository.
 * <p>
 * The cached files are stored under the directory {@code ~/.jamal/cache/}.
 * In this directory there are two subdirectories {@code https/} and {@code properties/}.
 * The directory {@code https/} contains the cached files.
 * The directory {@code properties/} contain properties files.
 * The properties contain certain information about the file in the cache.
 * <p>
 * You can set certain entries to expire adding a {@code ttl} value to the properties file.
 * <p>
 * When a ttl value is badly formatter, not a number, it is ignored.
 * The reason to ignore such an error instead of throwing an exception is that the cache is not a critical part of the system.
 * Processing should go on even when the handcrafted time-to-live values are erroneous.
 *
 */
public class Cache {

    /**
     * A cache entry.
     * It contains the content File and the properties File and the Properties object.
     * The properties are loaded when the content is requested by the caller.
     * There is no method to query the properties.
     * <p>
     * The properties are inherited from the containing directories.
     * The close to the actual entry overwrites the farther elements.
     * Properties contain time values, e.g., a cache element was written last time and also time to live value.
     */
    public static class Entry {

        /**
         * The entry file.
         */
        private final File file;
        /**
         * The properties file of the entry.
         */
        private final File propertiesFile;
        /**
         * Contains all the properties from the properties file of the entry.
         * They are lazy loaded when the content is requested.
         */
        private final Properties properties;
        /**
         * Contains all the properties from the properties file of the entry and also the properties from the
         * {@code .properties} files in the directories up to the cache root directory.
         */
        private final Properties effectiveProperties;

        /**
         * {@code true} if the properties file is a flat file, {@code false} if the properties are to be inherited from
         * the directories up to the cache root directory.
         */
        private final boolean flatProperties;
        /**
         * {@code true} if the properties are loaded.
         * Used to load the properties only once and only when they are needed.
         */
        private boolean propertiesLoaded = false;

        private Entry(File file, File propertiesFile) {
            this(file, propertiesFile, false);
        }

        /**
         * Create a new entry for the cache.
         * <p>
         * NOTE: Flat is implemented, not tested and not used.
         *
         * @param file           the file that contains the content
         * @param propertiesFile the file that contains the properties
         * @param flat           {@code true} if the properties file is a flat file, {@code false} if the properties
         *                       are to be inherited from the directories up to the cache root directory.
         *                       <p>
         */
        public Entry(File file, File propertiesFile, boolean flat) {
            this.file = file;
            this.propertiesFile = propertiesFile;
            this.properties = new Properties();
            this.effectiveProperties = new Properties();
            this.flatProperties = flat;
        }

        /**
         * Check if the entry is a cache miss.
         * <p>
         * It is a miss if
         *
         * <ul>
         *     <li>the file does not exist
         *     <li>the file exists but expired
         * </ul>
         *
         * @return {@code true} if the file is not in the cache.
         */
        public boolean isMiss() {
            if (!file.exists()) {
                return true;
            }
            final var ttl = getProperty("ttl");
            if (ttl == null) {
                return false;
            }
            final var write = getProperty("write");
            return expiration(ttl, write) < System.currentTimeMillis();
        }

        private long expiration(final String ttl, final String write) {
            final var ttlMillis = parseTtl(ttl);
            long writeMillis = 0L;
            try {
                writeMillis = Long.parseLong(write);
            } catch (NumberFormatException e) {
                //
            }
            return writeMillis + ttlMillis;
        }

        /**
         * Parse the ttl string and return the number of seconds that the ttl means.
         * <p>
         * A time to live string can contain numbers with units.
         * The units can be
         * <ul>
         *     <li>{@code Y} meaning years</li>
         *     <li>{@code M} meaning months, 31 days exactly</li>
         *     <li>{@code w} meaning weeks</li>
         *     <li>{@code d} meaning days</li>
         *     <li>{@code h} meaning hours</li>
         *     <li>{@code m} meaning minutes</li>
         *     <li>{@code s} meaning seconds</li>
         * </ul>
         * The units MUST be used in this order.
         * The number {@code 2Y3M5s} is correct, and it means 2 years, 3 months and 5 seconds.
         * On the other hand {@code 2Y5s3M} is not correct, and it will result zero TTL value.
         *
         * @param ttl the time to live string
         * @return the number of seconds the ttl string means
         */
        public static long parseTtl(final String ttl) {
            final var sb = new StringBuilder(ttl);
            long seconds = 0L;
            try {
                seconds += chopSeconds(sb, "y", 365 * 24 * 60 * 60);
                seconds += chopSeconds(sb, "M", 31 * 24 * 60 * 60);
                seconds += chopSeconds(sb, "w", 7 * 24 * 60 * 60);
                seconds += chopSeconds(sb, "d", 24 * 60 * 60);
                seconds += chopSeconds(sb, "h", 60 * 60);
                seconds += chopSeconds(sb, "m", 60);
                seconds += chopSeconds(sb, "s", 1);
                final var value = sb.toString().trim();
                if (!value.isEmpty()) {
                    seconds += Long.parseLong(value);
                }
            } catch (NumberFormatException e) {
                return 0L;
            }
            return seconds;
        }

        /**
         * Find the unit in the string and take the number before it.
         * Calculate the number of seconds that number means with the given unit.
         * <p>
         * Delete the number and the unit from the string.
         *
         * @param sb      string builder that contains the number and the unit
         * @param unit    the unit to look for
         * @param seconds the number of seconds that the unit means
         * @return the number of seconds that the number and the unit means
         */
        private static long chopSeconds(StringBuilder sb, String unit, long seconds) throws NumberFormatException {
            final var index = sb.indexOf(unit);
            if (index == -1) {
                return 0;
            }
            final var value = sb.substring(0, index).trim();
            sb.delete(0, index + 1);
            return Long.parseLong(value) * seconds;
        }

        /**
         * @return the content of the cached file or {@code null} if the file is not in the cache or cannot be read.
         */
        public StringBuilder getContent() {
            try {
                assertPropertiesAreLoaded();
                if (file.exists()) {
                    properties.put("read", "" + System.currentTimeMillis());
                    properties.put("read_formatted", now());
                    properties.put("count", "" + (Integer.parseInt(Optional.ofNullable((String) properties.get("count")).orElse("0")) + 1));
                    saveProperties();
                    return CachedHttpInput.readBufferedReader(getBufferedReader(file));
                } else {
                    return null;
                }
            } catch (IOException ioex) {
                return null;
            }
        }

        public byte[] getBinaryContent() {
            try {
                assertPropertiesAreLoaded();
                if (file.exists()) {
                    properties.put("read", "" + System.currentTimeMillis());
                    properties.put("read_formatted", now());
                    properties.put("count", "" + (Integer.parseInt(Optional.ofNullable((String) properties.get("count")).orElse("0")) + 1));
                    saveProperties();
                    final var buffer = new ByteArrayOutputStream();
                    try (final var is = new FileInputStream(file)) {
                        is.transferTo(buffer);
                        buffer.close();
                        return buffer.toByteArray();
                    }
                } else {
                    return null;
                }
            } catch (IOException ioex) {
                return null;
            }
        }

        /**
         * Get the property value from the properties file of the entry.
         * If the properties were not read then they will be read and loaded.
         * The loading is done only once and only when the properties are needed.
         * The loading reads the properties file and also the {@code .properties} files in the directories up to the
         * cache root directory, unless the enty is flat.
         *
         * @param key the key of the property
         * @return the value of the property or {@code null} if the property is not found or the properties cannot be
         * read.
         */
        public String getProperty(String key) {
            try {
                assertPropertiesAreLoaded();
                return effectiveProperties.getProperty(key);
            } catch (IOException ignored) {
                return null;
            }
        }

        /**
         * Load the properties from the properties file. The properties are loaded only once and only when they are
         * needed.
         */
        private void assertPropertiesAreLoaded() throws IOException {
            if (!propertiesLoaded) {
                if (propertiesFile.exists()) {
                    properties.load(new FileInputStream(propertiesFile));
                }
                propertiesLoaded = true;
                if (!flatProperties) {
                    effectiveProperties.putAll(collectEffectiveProperties(propertiesFile.getParentFile()));
                }
                effectiveProperties.putAll(properties);
            }
        }

        /**
         * Collect the properties from the {@code .properties} files in the directories up to the cache root directory.
         *
         * @param directory the directory from where to start collecting the properties
         * @return the properties collected from the {@code .properties} files in the directories up to the cache root
         */
        private Properties collectEffectiveProperties(final File directory) {
            final var properties = new Properties();
            if (!directory.getParentFile().equals(CACHE_ROOT_DIRECTORY)) {
                properties.putAll(collectEffectiveProperties(directory.getParentFile()));
            }
            final var dotPropertiesFile = new File(directory, ".properties");
            if (dotPropertiesFile.exists()) {
                try {
                    final var localProperties = new Properties();
                    localProperties.load(new FileInputStream(dotPropertiesFile));
                    properties.putAll(localProperties);
                } catch (IOException e) {
                    //
                }
            }
            return properties;
        }

        /**
         * @return the current date and time formatted. Used to record the time in the properties file human readable.
         */
        private static String now() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
            return format.format(new Date());
        }

        /**
         * See the documentation of {@link #save(String, Map[])}
         *
         * @param content to be saved into the cache file
         * @return the content itself
         */
        StringBuilder save(StringBuilder content) {
            save(content.toString());
            return content;
        }

        /**
         * Save the given content into a cache file. The saving may fail. In that case the failure will be silent and
         * does not throw an exception. This is designed that way not to prevent operation in case of a wrongly
         * configured cache. In that case, the file will be downloaded each time instead of using the cache, but Jamal
         * will still work.
         *
         * @param content to be saved into the cache file
         * @param maps    contains the key value pairs that will be saved into the cache properties file
         */
        @SafeVarargs
        public final void save(String content, Map<String, String>... maps) {
            save(content.getBytes(StandardCharsets.UTF_8), maps);
        }

        public final byte[] save(byte[] content, Map<String, String>... maps) {
            if (cacheExists()) {
                try {
                    assertPropertiesAreLoaded();
                    properties.put("write", "" + System.currentTimeMillis());
                    properties.put("write_formatted", now());
                    for (final var map : maps) {
                        properties.putAll(map);
                    }
                    saveProperties();
                    //noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                    try (final var fos = new FileOutputStream(file)) {
                        fos.write(content);
                    }
                } catch (IOException ignore) {
                }
            }
            return content;
        }

        /**
         * Save the properties. In case there is an error then silently ignore it. For reasoning see {@link
         * #save(StringBuilder)}. Do not call this method. It is called from {@link #save(StringBuilder)}. (It is
         * private after all.)
         */
        private void saveProperties() {
            //noinspection ResultOfMethodCallIgnored
            propertiesFile.getParentFile().mkdirs();
            try {
                properties.store(new FileOutputStream(propertiesFile),
                        " cache parameters of the entry " + file.getAbsolutePath());
            } catch (IOException ignore) {
            }
        }
    }

    private static final String DEFAULT_CACHE_ROOT = "~/.jamal/cache/";
    private static final String SNAPSHOT = "SNAPSHOT";

    static final Entry NO_CACHE = new Entry(NonexistentFile.INSTANCE, NonexistentFile.INSTANCE);

    private /*non-final for test*/ static File CACHE_ROOT_DIRECTORY = new File(
            EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_HTTPS_CACHE_ENV)
                    .or(() -> Optional.of(DEFAULT_CACHE_ROOT)).map(FileTools::adjustedFileName).get());

    /**
     * Get a cache entry for the given URL.
     *
     * @param url is the URL that the entry represents. This is the URL from where the original content was downloaded.
     * @return an entry. If there is no entry configured then it returns a pseudo entry that says that he entry is not
     * found. This same entry is returned in case the url contains the string {@code SNAPSHOT} all upper case.
     */
    public static Entry getEntry(URL url) {
        if (!Cache.cacheExists() || url.toString().contains(SNAPSHOT)) {
            return Cache.NO_CACHE;
        }

        final var fn = convertUrl2FN(url);
        final var propfile = new StringBuilder(fn);
        fn.insert(0, "/https/");
        fn.insert(0, CACHE_ROOT_DIRECTORY.getAbsolutePath());
        propfile.insert(0, "/properties/");
        propfile.insert(0, CACHE_ROOT_DIRECTORY.getAbsolutePath());
        return new Entry(new File(fn.toString()), new File(propfile.toString()));
    }

    /**
     * Convert a URL to a file name. This file name will be used in the cache as a structured directory path and file
     * name.
     *
     * @param url the url to convert to file name
     * @return a directory structure and file name with the {@code /} separator.
     */
    private static StringBuilder convertUrl2FN(URL url) {
        final var fn = new StringBuilder();
        final var host = url.getHost();
        final var path = url.getPath();
        for (final var s : host.split("\\.", -1)) {
            fn.insert(0, s + "/");
        }
        fn.append(path.substring(1));
        return fn;
    }


    /**
     * Create a buffered reader that reads the file using the UTF-8 character set.
     *
     * @param file is going to be read by the returned reader.
     * @return the reader
     * @throws IOException if the file cannot be opened.
     */
    private static BufferedReader getBufferedReader(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    /**
     * @return {@code true} is the cache is properly configured and exists. The cache is used if the cache directory
     * exists. It has to be created manually. The directories under it are recursively created when a file is cached,
     * but the cache directory has to be manually created. That way the cache can be configured not to be used simply
     * deleting the cache directory. (Not simply emptying!)
     */
    static boolean cacheExists() {
        return CACHE_ROOT_DIRECTORY.exists() && CACHE_ROOT_DIRECTORY.isDirectory();
    }

    /**
     * A file object that says the file does not exist. On anything else it returns what is returned by {@code new
     * File("")}. This is a singleton, use the {@code INSTANCE} field.
     */
    private static class NonexistentFile extends File {
        static final File INSTANCE = new NonexistentFile("");

        NonexistentFile(String pathname) {
            super(pathname);
        }

        @Override
        public boolean exists() {
            return false;
        }
    }
}
