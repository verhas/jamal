package javax0.jamal.tools;

/**
 * A very simple debug log tool to debug issues which only happen in an environment where debugging is not possible,
 * typically in GitHub actions.
 * <p>
 * This log just prints to the standard output.
 */
public class Debug {

    public static void log(String format, Object... parameters) {
        System.out.printf((format) + "%n", parameters);
    }

}
