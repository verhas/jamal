package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Debuggers should implement this interface in order to be loadable via the service loader. Also debugger classes
 * should have a public argument-less constructor.
 */
public interface Debugger extends AutoCloseable, ServiceLoaded {
    String JAMAL_DEBUG_ENV = "JAMAL_DEBUG";
    String JAMAL_DEBUG_SYS = "jamal.debug";

    /**
     * Loads the instances that are available via the service loader.
     *
     * @return the different debugger objects as returned by the service loader
     */
    static List<Debugger> getInstances() {
        return ServiceLoaded.getInstances(Debugger.class);
    }

    /**
     * A {@code Debugger.Stub} provides access to functionality, which is not part of the normal operation of a {@link
     * Processor} but are needed and defined in the {@link Debuggable} interface and in the interfaces that are inside.
     * The debugger gets an instance of the implementation of this stub instead of the {@code Processor}, because the
     * debugger does not need the regular processor functionality. On the other hand the processor object the debugger
     * could access implements methods that return objects which need casting to their {@link Debuggable} counterpart
     * and the stub implementation does that and also calls the subsequent methods that the debugger needs. Essentially
     * the implementation of this interface is a facade towards the functionalities from the processor that the debugger
     * needs.
     */
    interface Stub {

        /**
         * Return the current list of the scopes of the processor. Scopes are internal matter of the processor and are
         * not exposed. However, debugging is something that pokes into the internals when the user needs to see what
         * the current state is. For this reason the interface {@link Debuggable} is defined that gives access to those
         * methods that are needed for the debugger.
         *
         * The debugger must not modify this list.
         *
         * @return the list of scopes
         */
        List<Debuggable.Scope> getScopeList();

        /**
         * Ask the currently debugged processor to evaluate the string at the current state.
         * Note that the evaluation will also trigger the debugger events in a recursive way.
         * If the debugger does not want to stop every time it has to care about this fact
         * maintaining a state signaling when it needs to stop and when it must not.
         *
         * @param in the string to evaluate as macro
         * @return the result of the evaluation. This result is not appended to the output.
         * @throws BadSyntax if the evaluation of the input is erroneous
         */
        String process(String in) throws BadSyntax;
    }

    /**
     * The processor calls this method before the evaluation of a macro. The implementation may store the reference and
     * copy the content of {@code input} and the {@code level} and may, at it's own decision stop the execution and
     * interact with the debugger client.
     * <p>
     * Note that the {@code input} is a character sequence and this it is mutable. The processor does mutate the
     * instance afterwards. So if the debugger wants to save the state of the input it has to make a copy of the
     * content, for example applying {@link CharSequence#toString() toString()}. The reference itself can later be used,
     * to get the later state of the input. For example in the method {@link #setAfter(int, CharSequence)} the {@code
     * input} will already contain the state of the input after the macro evaluation (the macro or the text block from
     * the start is chopped off.)
     *
     * @param level the actual macro nesting level at the time of the method invocation
     * @param input the input
     */
    void setBefore(int level, CharSequence input);

    /**
     * Give the content of the current macro to be evaluated to the debugger before the macro is evaluated. The
     * processor invokes this method on the debugger before it invokes a macro evaluation, or before it appends a text
     * segment to the output.
     * <p>
     * Note that the {@code macro} is a character sequence and this it is mutable. The processor does mutate the
     * instance afterwards. So if the debugger wants to save the state of the input it has to make a copy of the
     * content, for example applying {@link CharSequence#toString() toString()}.
     * <p>
     * It is guaranteed that this method will be invoked after {@link #setBefore(int, CharSequence) setBefore()} while
     * still before the evaluation. That way the debugger can be sure the that the {@code macro} belongs to the same
     * {@code level} as it was passed to {@link #setBefore(int, CharSequence) setBefore()}.
     *
     * @param macro the text of the macro to be evaluated
     */
    void setStart(CharSequence macro);

    /**
     * Give the content of the evaluation result, {@code output} to the debugger. The processor invokes this method
     * after it evaluated a macro or appended a text segment to the output.
     * <p>
     * Note that the {@code output} is a character sequence and this it is mutable. The processor does mutate the
     * instance afterwards. So if the debugger wants to save the state of the input it has to make a copy of the
     * content, for example applying {@link CharSequence#toString() toString()}.
     *
     * @param level  the actual macro nesting level at the time of the method invocation
     * @param output the output
     */
    void setAfter(int level, CharSequence output);

    /**
     * Implement this method to close the debugger. This may include closing communication channels with the client and
     * other possible clean-up. This method is invoked when the processor is closed.
     */
    void close();

    /**
     * The debugger should implement this method to signal its affinity to handle the debugger connection string {@code
     * s}.
     * <p>
     * The affinity value can be -1, 0 or any other positive value.
     * <p>
     * Affinity -1 means that the debugger cannot handle this connection string. The connection string in this case is
     * probably for a different debugger.
     * <p>
     * Affinity 0 means that the debugger is absolutely sure that it can handle this connection string and it is the
     * debugger that has to be used.
     * <p>
     * Values between 0 and {@link Integer#MAX_VALUE Integer.MAX_VALUE} mean a certain affinity expressed numerically.
     * The processor chooses the debugger that has the smallest affinity value.
     * <p>
     * It is an error if there are more than one debuggers that return the smallest affinity value.
     * <p>
     * The module {@code jamal-engine} implements this interface with the {@code javax0.jamal.engine.NullDebugger}. The
     * {@code affinity()} method of the {@code NullDebugger} returns {@code Integer.MAX_VALUE-1}. The algorithm
     * searching for a debugger will find this implementation if no other debugger claims the connection string any
     * better for themselves.
     * <p>
     * The implementation of this method is also a good place to parse the connection string and to extract the
     * parameters from it. To do that the implementations can use the {@code javax0.jamal.tools.ConnectionStringParser}
     * class. An implementation may also decide to store the connection string and parse it only in the {@link
     * #init(Stub) init()} method.
     *
     * @param s the debugger connection string
     * @return the affinity value of the debugger implementation related to this debugger connection string
     */
    int affinity(String s);

    /**
     * Initialize the debugger. This method is called right after the debugger was selected from the list of available
     * debuggers. The implementation should perform all operations that are needed to set up the debugger. This usually
     * includes opening some channel that the debugger client can use to communicate with the debugger.
     *
     * @param stub the debugger stub (see {@link Debugger.Stub} the debugger should use to implement certain functions.
     * @throws Exception if the initialization cannot be performed. For example the configured post to bind on is
     *                   already bound.
     */
    void init(Stub stub) throws Exception;
}
