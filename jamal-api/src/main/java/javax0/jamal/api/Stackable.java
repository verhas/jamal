package javax0.jamal.api;

/**
 * The macros that need the {@code push()} and {@code pop()} call-backs when the macro level is increasing / decreasing
 * should implement this interface.
 * <p>
 * Macro level gets deeper when macros are evaluated in a macro enclosure or when a macro itself calls the macro
 * register {@code push()} method. For example the macro include calls {@link MacroRegister#push(Marker)} before
 * starting to process the content of a file and calls {@link MacroRegister#pop(Marker)} when it finished.
 * <p>
 * Macro level gets back to the previous state when the {@link MacroRegister#pop(Marker)} method is invoked. This
 * happens when the macro {@code import} finished processing a file or when the macro evaluation finished the macro
 * evaluation inside a macro.
 * <p>
 * When a macro starts with the {@code #} character then the content of the macro is processed resolving macro
 * references before processing the macro itself. This process is executed one level deeper than the current level. The
 * {@link Processor#process(Input)} calls  {@link MacroRegister#push(Marker)} before starting to process the content of
 * the macro body and calls {@link MacroRegister#pop(Marker)} when it has finished.
 */
public interface Stackable extends Macro {
    void push();

    void pop();
}
