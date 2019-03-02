package javax0.jamal.api;

/**
 * A line reference contains the name of a file and a line number. This serves as a parameter when an error happens.
 * The exception {@link BadSyntaxAt} gets an object of this type as a parameter and later it is used to compose the
 * message of the exception when the exception is logged.
 */
public class LineReference {
    public final String fileName;
    public final int lineNumber;

    public LineReference(String fileName, int lineNumber) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
}
