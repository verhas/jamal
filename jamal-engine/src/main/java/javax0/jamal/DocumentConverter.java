package javax0.jamal;

import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;

/**
 * Convert one single document.
 * <p>
 * Use this class and method to maintain your documentation in Jamal format. The project documentation can use macros
 * that fetch data automatically from the Java code. For example the value of a {@code static final} can be retrieved
 * using reflection. The snippet macro {@code java:field} does that. It can work only if the application is loaded and
 * on the classpath or module path.
 * <p>
 * The suggested practice is that you invoke {@link DocumentConverter#convert(String)} from your test code. That way the
 * application as well as the tests are on the classpath and are available to be referenced. If you  use the {@code
 * java:*} macros then you will automatically get the actual value of all fields documented and you get an error when
 * you alter the name of a class, method or field and you forget to follow the change in the documentation.
 */
public class DocumentConverter {
    /**
     * Create a JUnit test in your application that looks the following:
     *
     * <pre>
     *     @Test
     *     void generateDoc() throws Exception {
     *         DocumentConverter.convert("./README.adoc.jam");
     *     }
     * </pre>
     * <p>
     * This will convert the {@code README.adoc.jam} file to {@code README.adoc}. The name and the primary extension of
     * the file can be different.
     *
     * @param file the name of the Jamal source documentation file.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file) throws Exception {
        final var in = FileTools.getInput(file);
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        final var output = file.substring(0, file.length() - ".jam".length());
        FileTools.writeFileContent(output, result);
    }
}
