package javax0.jamal.tools;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Test the cache.
 * <p>
 * This tests the reading from the cache.
 * To do that, it sets the cache root directory to point to the test resources directory, where there is a manually
 * set-up cache structure.
 * <p>
 * After the test the original cache root directory is restored.
 */
public class TestCache {

    /**
     * Get the cache root directory from the test resources.
     *
     * @return the cache root directory
     */
    private static File getCacheRoot() {
        return new File(
                Objects.requireNonNull(
                                TestCache.class.getClassLoader().getResource("test.cache.structure"))
                        .getFile());
    }

    private static File savedCacheRoot;

    /**
     * Set the cache root directory to the test resources and save the original in the filed {@code savedCacheRoot}.
     *
     * @throws Exception if the reflection fails
     */
    @BeforeAll
    static void setCacheRoot() throws Exception {
        final var crd = Cache.class.getDeclaredField("CACHE_ROOT_DIRECTORY");
        crd.setAccessible(true);
        savedCacheRoot = (File) crd.get(null);
        crd.set(null, getCacheRoot());
    }

    /**
     * Restore the original cache root directory.
     *
     * @throws Exception if the reflection fails
     */
    @AfterAll
    static void restoreCacheRoot() throws Exception {
        final var crd = Cache.class.getDeclaredField("CACHE_ROOT_DIRECTORY");
        crd.setAccessible(true);
        crd.set(null, savedCacheRoot);
    }


    @DisplayName("Test that the properties are read from the cache")
    @Test
    void testHierarchicalPropertyRead() throws Exception {
        var sut = Cache.getEntry(new URI("https://raw.githubusercontent.com/central7/pom/main/plugins/compiler.jim").toURL());
        var ttl = sut.getProperty("ttl");
        Assertions.assertEquals("0", ttl);
        var evil = sut.getProperty("evil");
        Assertions.assertEquals("666", evil);
        sut = Cache.getEntry(new URI("https://raw.githubusercontent.com/central7/pom/main/plugins/javadoc.jim").toURL());
        ttl = sut.getProperty("ttl");
        Assertions.assertEquals("1", ttl);
        sut = Cache.getEntry(new URI("https://raw.githubusercontent.com/central7/pom/2/pom.jim").toURL());
        ttl = sut.getProperty("ttl");
        Assertions.assertEquals("2", ttl);
    }

    @DisplayName("Test that the entry is a miss because it is expired")
    @Test
    void testCacheHitMiss() throws Exception {
        var sut = Cache.getEntry(new URI("https://raw.githubusercontent.com/central7/pom/main/plugins/compiler.jim").toURL());
        Assertions.assertTrue(sut.isMiss());
    }

    @DisplayName("Test that the entry is a miss because file does not exists")
    @Test
    void testCacheHitMissFileDoesNotExists() throws Exception {
        var sut = Cache.getEntry(new URI("https://raw.githubusercontent.com/central7/pom/main/plugins/compiler.jimo").toURL());
        Assertions.assertTrue(sut.isMiss());
    }

    @DisplayName("Test that the entry is a not miss because file exists")
    @Test
    void testCacheHitMissFileExists() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/kht.html").toURL());
        Assertions.assertFalse(sut.isMiss());
    }

    @DisplayName("Test that the entry content can be retrieved")
    @Test
    void testCacheGetContent() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/kht.html").toURL());
        final var result = sut.getContent().toString();
        Assertions.assertEquals("ome content\n", result);
    }


    @DisplayName("Test that the entry content can be retrieved as binary")
    @Test
    void testCacheGetContentBinary() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/kht.html").toURL());
        final var result = sut.getBinaryContent();
        Assertions.assertArrayEquals("ome content".getBytes(StandardCharsets.UTF_8), result);
    }

    @DisplayName("Test that the entry content is null when the entry is a miss")
    @Test
    void testCacheGetNoContent() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/khx.html").toURL());
        final var result = sut.getContent();
        Assertions.assertNull(result);
    }

    @DisplayName("Test saving file binary into the cache")
    @Test
    void testCacheSave() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/khi.html").toURL());
        final var result = sut.getBinaryContent();
        sut.save("khi content".getBytes(StandardCharsets.UTF_8), Map.of("ttl", "1y3m"));
        Assertions.assertFalse(sut.isMiss());
    }

    @DisplayName("Test saving file text cache")
    @Test
    void testCacheSaveText() throws Exception {
        var sut = Cache.getEntry(new URI("https://com/khk.html").toURL());
        final var result = sut.getBinaryContent();
        sut.save(new StringBuilder("khk content"));
        Assertions.assertFalse(sut.isMiss());
    }
}
