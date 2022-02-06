package javax0.jamal.tools;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class TestCache {

    private static File getCacheRoot() {
        return new File(TestCache.class.getClassLoader().getResource("test.cache.structure").getFile());
    }

    private static File savedCacheRoot;

    @BeforeAll
    static void setCacheRoot() throws Exception {
        final var crd = Cache.class.getDeclaredField("CACHE_ROOT_DIRECTORY");
        crd.setAccessible(true);
        savedCacheRoot = (File) crd.get(null);
        crd.set(null, getCacheRoot());
    }

    @AfterAll
    static void restoreCacheRoot() throws Exception {
        final var crd = Cache.class.getDeclaredField("CACHE_ROOT_DIRECTORY");
        crd.setAccessible(true);
        crd.set(null, savedCacheRoot);
    }


    @Test
    void test() throws Exception {
        var sut = Cache.getEntry(new URL("https://raw.githubusercontent.com/central7/pom/main/plugins/compiler.jim"));
        var ttl = sut.getProperty("ttl");
        Assertions.assertEquals("0", ttl);
        var evil = sut.getProperty("evil");
        Assertions.assertEquals("666", evil);
        sut = Cache.getEntry(new URL("https://raw.githubusercontent.com/central7/pom/main/plugins/javadoc.jim"));
        ttl = sut.getProperty("ttl");
        Assertions.assertEquals("1", ttl);
        sut = Cache.getEntry(new URL("https://raw.githubusercontent.com/central7/pom/2/pom.jim"));
        ttl = sut.getProperty("ttl");
        Assertions.assertEquals("2", ttl);
    }
}
