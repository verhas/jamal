package javax0.jamal.snippet;

import javax0.jamal.api.Position;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestCacheMacro {

    private static final String INPUT = "./target/wups.jam";
    private static final Position POS = new Position(INPUT);

    @Test
    @DisplayName("Test that the cache macro works testing the existence of the non existing cache file")
    void testFileDoesNotExist() throws Exception {
        TestThat.theInput("{@cache dir=.cache key=abraka extension=png exists}").results("false");
    }

    @Test
    @DisplayName("Test that the cache macro works testing the existence of the existing cache file")
    void testFileExists() throws Exception {
        final var f = new File("target/.cache/abrake.png");
        new File(f.getParent()).mkdirs();
        f.createNewFile();
        TestThat.theInput("{@cache dir=.cache key=abrake extension=png exists}").atPosition(POS).results("true");
        f.delete();
    }

    @Test
    @DisplayName("Returns the name of the file if it exists")
    void testFileExistsContent() throws Exception {
        final var pathname = "target/.cache/abraki.png";
        final var f = new File(pathname);
        new File(f.getParent()).mkdirs();
        f.createNewFile();
        TestThat.theInput("{@cache dir=.cache key=abraki extension=png}").atPosition(POS).results(pathname);
        f.delete();
    }

    @Test
    @DisplayName("Test that the cache macro throws when there is no file specified and no cache when retrieving the content")
    void testFileDoesNotExistsThrows() throws Exception {
        TestThat.theInput("{@cache dir=.cache key=abraka extension=png}")
                .throwsBadSyntax("The file name must be specified when the cache is empty.");
    }

    @Test
    @DisplayName("Returns the name of the file after downloaded")
    void testFileDownloads() throws Exception {
        final var pathname = "target/.cache/abraki.png";
        final var f = new File(pathname);
        new File(f.getParent()).mkdirs();
        f.createNewFile();
        new File(pathname.replaceAll("abraki","abraka")).delete();
        TestThat.theInput(String.format("{@cache file=%s dir=.cache key=abraka extension=png}",pathname))
                .atPosition(POS).results(pathname.replaceAll("abraki","abraka"));
        f.delete();
        new File(pathname.replaceAll("abraki","abraka")).delete();
    }

    @Test
    @DisplayName("Returns the name of the file after downloaded without key")
    void testFileDownloadsWoKey() throws Exception {
        final var pathname = "target/.cache/abraki.png";
        final var f = new File(pathname);
        new File(f.getParent()).mkdirs();
        f.createNewFile();
        new File(pathname.replaceAll("abraki","abraka")).delete();
        String cacheFileName = "target/.cache/a8472142a0582f38e56fe786e7b439d51b10781324642b16d4ae8b5e274b206c.png";
        new File(cacheFileName).delete();
        TestThat.theInput(String.format("{@cache file=%s dir=.cache extension=png}",pathname))
                .atPosition(POS)
                .results(cacheFileName);
        f.delete();
        new File(cacheFileName).delete();
    }

}
