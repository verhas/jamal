package javax0.jamal.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

class Cache {
    static class Entry {
        private final File file;
        private final File propertiesFile;
        private final Properties properties;

        private Entry(File file, File propertiesFile) {
            this.file = file;
            this.propertiesFile = propertiesFile;
            this.properties = new Properties();
        }

        boolean isMiss() {
            return !file.exists();
        }

        StringBuilder getContent() throws IOException {
            if (propertiesFile.exists()) {
                properties.load(new FileInputStream(propertiesFile));
            }
            if (file.exists()) {
                properties.put("lastRead", "" + System.currentTimeMillis());
                properties.put("count", "" + (Integer.parseInt(Optional.ofNullable((String) properties.get("count")).orElse("0")) + 1));
                saveProperties();
                return CachedHttpInput.readBufferedReader(getBufferedReader(file));
            } else {
                return null;
            }
        }

        StringBuilder save(StringBuilder content) throws IOException {
            if (cacheExists()) {
                properties.put("lastWrite", "" + System.currentTimeMillis());
                saveProperties();
                file.getParentFile().mkdirs();
                try (final var fos = new FileOutputStream(file)) {
                    fos.write(content.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            return content;
        }

        private void saveProperties() throws IOException {
            propertiesFile.getParentFile().mkdirs();
            properties.store(new FileOutputStream(propertiesFile),
                " cache parameters of the entry " + file.getAbsolutePath());
        }
    }

    private static final String JAMAL_HTTPS_CACHE = "JAMAL_HTTPS_CACHE";
    private static final String DEFAULT_CACHE_ROOT = "~/.jamal/cache/";
    private static final String SNAPSHOT = "SNAPSHOT";

    static final Entry NO_CACHE = new Entry(NonexistentFile.INSTANCE, NonexistentFile.INSTANCE);

    private static final File CACHE_ROOT_DIRECTORY;

    static {
        final var envCacheRoot = System.getenv(JAMAL_HTTPS_CACHE);
        final var userHome = System.getProperty("user.home");

        final String cacheRoot;
        if (envCacheRoot != null) {
            cacheRoot = envCacheRoot;
        } else {
            cacheRoot = DEFAULT_CACHE_ROOT;
        }
        CACHE_ROOT_DIRECTORY = new File(cacheRoot.charAt(0) == '~' ? userHome + cacheRoot.substring(1) : cacheRoot);
    }


    static Entry getEntry(URL url) {
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


    private static BufferedReader getBufferedReader(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }


    static boolean cacheExists() {
        return CACHE_ROOT_DIRECTORY.exists() && CACHE_ROOT_DIRECTORY.isDirectory();
    }

    private static class NonexistentFile extends File {
        private static final File INSTANCE = new NonexistentFile("");

        public NonexistentFile(String pathname) {
            super(pathname);
        }

        @Override
        public boolean exists() {
            return false;
        }
    }
}
