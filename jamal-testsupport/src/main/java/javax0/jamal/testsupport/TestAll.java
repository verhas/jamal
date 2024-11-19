package javax0.jamal.testsupport;

import javax0.jamal.api.*;
import javax0.jamal.engine.Processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax0.jamal.tools.Input.makeInput;

/**
 * Test support methods that read from resources, execute Jamal and compare the result with other resource content.
 */
public class TestAll {
    private final Class<?> testClass;
    private String open = "{", close = "}";
    private String extension = null;
    private String message = "";
    private String expected, actual;

    private TestAll(final Class<?> testClass) {
        this.testClass = testClass;
    }

    /**
     * Create a new instance that will work for the given test class. The test class is needed to know which directory
     * the test resources are.
     *
     * @param testClass the class that is using this utility.
     * @return a new TestAll instance initialized to be used from the given class
     */
    public static TestAll in(final Class<?> testClass) {
        return new TestAll(testClass);
    }

    /**
     * Set the macro opening and closing string as in the macro sep. The defaults are "{" and "}".
     *
     * @param open  the new opening string
     * @param close the new closing string
     * @return {@code this}
     */
    public TestAll sep(final String open, final String close) {
        this.open = open;
        this.close = close;
        return this;
    }

    /**
     * Set the extension filter for selecting input or output files.
     *
     * @param extension is the ending of the files that we want to use in the test
     * @return {@code this}
     */
    public TestAll filesWithExtension(final String extension) {
        this.extension = extension;
        return this;
    }

    /**
     * Tests that the files are processed by Jamal just running OK and they all result the string that is in their
     * expected file pair.
     * <p>
     * The typical use is
     *
     * <pre>{@code
     *         final TestAll tests = TestAll.in(this.getClass()).filesWithExtension(".expected");
     *         if (!tests.resultAsExpected()) {
     *             Assertions.assertEquals(tests.getExpected(), tests.getActual(), tests.getMessage());
     *         }
     * }</pre>
     * <p>
     * When the test fails on one of the files then the TestAll object will hold a meaningful message and also two
     * different strings that show the difference between what we got and what we expected.
     *
     * @return true if the tests are ok, false otherwise
     * @throws IOException if some of the files cannot be read
     * @throws BadSyntax   if some of the files are bad
     */
    public boolean resultAsExpected() throws IOException, BadSyntax {
        final var expectedFiles = getFileNames();
        for (final var expectedFile : expectedFiles) {
            final var inputFile = expectedFile.substring(0, expectedFile.length() - extension.length());
            final var expected = readExpectedContent(expectedFile);
            final var actual = result(inputFile);
            if (!Objects.equals(actual, expected)) {
                message = "The test input '" + inputFile + "' returned different results.";
                this.expected = expected;
                this.actual = actual;
                return false;
            }
        }
        return true;
    }

    /**
     * Test that all files that are processed fail. The typical use is:
     *
     * <pre>{@code
     *         final TestAll tests = TestAll.in(this.getClass()).filesWithExtension(".err");
     *         if (!tests.failAsExpected()) {
     *             Assertions.assertEquals(tests.getExpected(), tests.getActual(), tests.getMessage());
     *         }
     * }</pre>
     * <p>
     * The test checks that each file processing ends with a bad syntax exception thrown and that the exception is
     * referring to a specific line and column. The test input should define the user defined macros {@code line} and
     * {@code column} to hold the position of the error. For example:
     *
     * <pre>{@code
     * {@define line=4}{@define column=5}
     * }</pre>
     * <p>
     * to signal that this file should fail in the line 4 at the column 5. The input optionally can define a user
     * defined macro {@code message} that may contain the start of the error message. If there is no {@code message}
     * defined then it is not checked and this is not an error. {@code line} and {@code column} must be defined.
     * <p>
     * When some of the files do not fail or fail at a different location or throw a different error message than
     * defined in the input then the TestAll object will hold a meaningful message and also two different strings that
     * show the difference between what we got and what we expected.
     *
     * @return true if the tests are ok, false otherwise
     * @throws IOException if some of the files cannot be read
     * @throws BadSyntax   if some of the files are bad
     */
    public boolean failAsExpected() throws IOException, BadSyntax {
        final var errFiles = getFileNames();
        for (final var errFile : errFiles) {
            var in = inputFrom(errFile);
            final var sut = new Processor("{", "}");
            try {
                actual = sut.process(in);
                message = "The test file '" + errFile + "' did not throw BadSyntax exception";
                expected = "There is no expected output, we expected an exception that did not occur.";
                return false;
            } catch (BadSyntaxAt thrown) {
                final var line = getInt(sut, "line");
                if (line.isEmpty()) {
                    message = "The test file '" + errFile + "' does not {@define line=NNN} to the expected syntax error line";
                    actual = "" + thrown.getPosition().line;
                    expected = "null";
                    return false;
                }
                if (thrown.getPosition().line != line.get()) {
                    message = "The test file '" + errFile + "' reports error on a wrong line.";
                    actual = "" + thrown.getPosition().line;
                    expected = "" + line.get();
                    return false;
                }
                final var column = getInt(sut, "column");
                if (column.isEmpty()) {
                    message = "The test file '" + errFile + "' does not {@define column=NNN} to the expected syntax error column";
                    actual = "" + thrown.getPosition().column;
                    expected = "null";
                    return false;
                }
                if (thrown.getPosition().column != column.get()) {
                    message = "The test file '" + errFile + "' reports error on a wrong column.";
                    actual = "" + thrown.getPosition().column;
                    expected = "" + column.get();
                    return false;
                }
                final var msg = getString(sut, "message");
                if (msg.isPresent()) {
                    if (!thrown.getMessage().startsWith(msg.get())) {
                        message = "The test file '" + errFile + "' reports error with unexpected message.";
                        actual = thrown.getMessage().substring(0, msg.get().length());
                        expected = msg.get();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Get the file names that have the extension that were specified in the {@link #filesWithExtension(String)}
     * method.
     *
     * @return the array of the file names
     * @throws IOException is the directory cannot be read.
     */
    private String[] getFileNames() throws IOException {
        final var directoryName = fixupPath(testClass.getResource("").getFile());
        final var directory = new File(directoryName);
        final var errFiles = directory.list((File dir, String name) -> name.endsWith(extension));
        if (errFiles == null) {
            throw new IOException("There are no '" + extension
                + "' files in the test resources directory '" + directoryName + "'");
        }
        return errFiles;
    }

    /**
     * Create an input from the file named.
     *
     * @param testFileName the name of the test file from which the input is created.
     * @return the content of the file as input.
     * @throws IOException if the file cannot be opened.
     */
    public Input inputFrom(String testFileName) throws IOException {
        var fileName = Objects.requireNonNull(testClass.getResource(testFileName), "File '" + testFileName + "' does not exist").getFile();
        fileName = fixupPath(fileName);
        try (final var lines = Files.lines(Paths.get(fileName))) {
            var fileContent = lines.collect(Collectors.joining("\n"));
            return makeInput(fileContent, new Position(fileName));
        }
    }

    @FunctionalInterface
    public interface AssertEquals {
        void apply(Object expected, Object actual, String message);
    }

    public static void testExpected(Object testThis, AssertEquals asserter) throws IOException, BadSyntax {
        testExpected(testThis, asserter, ".expected");
    }

    public static void testExpected(Object testThis, AssertEquals asserter, String ext) throws IOException, BadSyntax {
        final TestAll tests = in(testThis.getClass()).filesWithExtension(ext);
        if (!tests.resultAsExpected()) {
            asserter.apply(tests.getExpected(), tests.getActual(), tests.getMessage());
        }
    }

    /**
     * When a test fails then the message is a user friendly text that help explain why the test failed. This method is
     * a getter.
     *
     * @return the message. If there was no error then it is empty string
     */
    public String getMessage() {
        return message;
    }

    /**
     * When a test fails then the field 'expected' will contain the expected result. This is a getter for that. The
     * calling code can execute an {@code assertEquals(getExpected(),getActual())} line to get a developer friendly
     * output that shows the difference between what was expected and what was the actual result.
     *
     * @return the expected result.
     */
    public String getExpected() {
        return expected;
    }

    /**
     * When a test fails then the field 'actual' will contain the actual result. This is a getter for that. The calling
     * code can execute an {@code assertEquals(getExpected(),getActual())} line to get a developer friendly output that
     * shows the difference between what was expected and what was the actual result.
     *
     * @return the actual result.
     */
    public String getActual() {
        return actual.replaceAll("\r", "");
    }

    /**
     * Processes the text file and creates the resulting file in the resources directory so it can be examined.
     *
     * @param testFileName the test file name
     * @return the evaluated result
     * @throws IOException when the file cannot be read
     * @throws BadSyntax   when there is syntax error in the test file
     */
    private String result(String testFileName) throws IOException, BadSyntax {
        var in = inputFrom(testFileName);
        try (final var sut = new Processor("{", "}")) {
            return sut.process(in);
        }
    }

    private String readExpectedContent(String expectedFileName) throws IOException {
        var fileName = Objects.requireNonNull(testClass.getResource(expectedFileName), "File '" + expectedFileName + "' does not exist").getFile();
        fileName = fixupPath(fileName);
        try (final var is = new FileInputStream(fileName)) {
            final var bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8).replaceAll("\r", "");
        }
    }

    /**
     * Fixup the JDK bug JDK-8197918
     *
     * @param fileName the file name that may contain an erroneous leading / on Windows
     * @return the file without the leading / if it contains ':', so it is assumed this is Windows
     */
    private String fixupPath(String fileName) {
        if (fileName.contains(":")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    /**
     * Get an int parameter from the input file. Input that has bad syntax and should fail must define two macros {@code
     * line} and {@code column}. This method will return the value of the user defined macro named by the argument
     * {@code macroName}.
     *
     * @param sut       the processor that was running and failed throwing {@code BadSyntaxAt}
     * @param macroName the name of the user defined macro
     * @return the optional value of the parameter (note that the user defined macro is String, and it is converted to
     * Integer.
     * @throws BadSyntax if the macro evaluation throws up.
     */
    private Optional<Integer> getInt(Processor sut, String macroName) throws BadSyntax {
        return getString(sut, macroName).map(Integer::parseInt);
    }

    /**
     * Get a String parameter from the input file. Input that has bad syntax and should fail may define a macro {@code
     * message}. This method will return the value of the user defined macro named by the argument {@code macroName}.
     *
     * @param sut       the processor that was running and failed throwing {@code BadSyntaxAt}
     * @param macroName the name of the user defined macro
     * @return the optional value of the parameter
     * @throws BadSyntax if the macro evaluation throws up.
     */
    private Optional<String> getString(Processor sut, String macroName) throws BadSyntax {
        final var userDefined = sut.getRegister().getUserDefined(macroName);
        if (userDefined.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(((UserDefinedMacro) userDefined.get()).evaluate());
    }
}
