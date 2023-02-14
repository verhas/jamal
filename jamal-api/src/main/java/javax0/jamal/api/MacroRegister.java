package javax0.jamal.api;

import java.util.Optional;
import java.util.Set;

/**
 * General macro registry that can be used to register built-in (Java implemented) and user defined macros. API and
 * implementation also supports hierarchical macro definitions, in the sense that the registry manages the lifetime of
 * the macros so that they are available and are optionally shadowing other macros of the same name while the context
 * they were defined in exists.
 */
public interface MacroRegister extends Delimiters, Debuggable<Debuggable.MacroRegister> {

    /**
     * Get a macro based on the id of the macro.
     *
     * @param id the identifier (name) of the macro
     * @return the macro. {@code Optional.empty()} if the macro can not be found.
     */
    Optional<Macro> getMacro(String id);

    /**
     * Get a built-in macro from the currently active local (writable) scope. That way a macro can define another macro
     * if it is not defined yet in the current scope or use the existing one in case the macro is already defined in the
     * current scope.
     * <p>
     * The default implementation always returns empty.
     *
     * @param id the identifier of the macro
     * @return the macro. It will return empty in case there is a macro defined with the given name, but not in
     * the currently writable level (higher level or the one level below).
     */
    default Optional<Macro> getMacroLocal(String id) {
        return Optional.empty();
    }

    /**
     * Get a user defined macro from the currently active local (writable) scope. Used by the macro if to test that a
     * macro is local.
     * <p>
     * The default implementation always returns empty.
     *
     * @param id the identifier of the macro
     * @return the macro. It will return empty in case there is a macro defined with the given name, but not in
     * the currently writable level (higher level or the one level below).
     */
    default Optional<Identified> getUdMacroLocal(String id) {
        return Optional.empty();
    }

    /**
     * Get a user defined macro based on the id of the macro.
     *
     * @param id  the identifier (name) of the macro
     * @param <T> some type that implements {@link Identified}
     * @return the user defined macro. {@code Optional.empty()} if the macro can not be found.
     */
    <T extends Identified> Optional<T> getUserDefined(String id);

    /**
     * Get a user defined macro (or anything {@link Identified}) based on the id of the macro or a default macro.
     *
     * @param id  the identifier (name) of the macro
     * @param def the identifier (name) of the macro used in the case macro for {@code id} is not defined
     * @param <T> some type that implements {@link Identified}. Note that anything implementing {@link Identified}
     *            can be stored in the macro registry.
     * @return the user defined macro in an optional. Optional.empty() if the macro can not be found.
     */
    default <T extends Identified> Optional<T> getUserDefined(String id, String def) {
        return (Optional<T>) getUserDefined(id).or(() -> getUserDefined(def));
    }

    /**
     * Define a user defined macro on the global level.
     *
     * @param macro to store in the definition structure on the top level.
     */
    void global(Identified macro);

    /**
     * Define a user defined macro on the global level, but instead of the identifier returned by the
     * {@link Identified#getId()} use the alias provided as argument.
     * <p>
     * The default implementation throws exception.
     *
     * @param macro the user defined macro (or anything {@link Identified}) to be stored on the global level
     * @param alias the alias to use as the id of the macro
     */
    default void global(final Identified macro, final String alias) {
        throw new UnsupportedOperationException("This method is not supported by this implementation.");
    }

    /**
     * Define a macro on the global level.
     * <p>
     * Note that this is an overloaded method and since the {@link Macro} interface extends the {@link Identified}
     * interface this overloading heavily relies on the Java overloading mechanism and resolution. The implementation
     * of this method does extra checks before storing the macro. For example, the actual implementation in Jamal checks
     * that the macro class is stateless.
     *
     * @param macro to store in the definition structure on the top level.
     */
    void global(final Macro macro);

    /**
     * Define a macro on the global level. It is the same as the {@link #global(Macro)} method, but instead of using the
     * identifier returned by the method {@link Identified#getId()} is uses the {@code alias} as a name for the macro.
     * <p>
     * Also see the notes about overloading and extra checks at {@link #global(Macro)}.
     *
     * @param macro to store in the definition structure on the top level.
     * @param alias alias name to be used for the macro instead of the one provided by the macro itself via {@link
     *              Macro#getId()}
     */
    void global(final Macro macro, final String alias);

    /**
     * Given a misspelled macro name suggest the closest possible currently defined and in scope user defined or
     * build-it macro. This method is used to give suggestions in the error messages when the name of a macro is
     * misspelled.
     *
     * @param spelling the misspelled macro name
     * @return the closest possible suggestions or an empty set if no suggestion can be found. The default implementation
     * returns an empty set.
     */
    default Set<String> suggest(final String spelling) {
        return Set.of();
    }

    /**
     * Define a user defined macro in the current evaluation level.
     *
     * @param macro to store in the definition structure.
     */
    void define(final Identified macro);


    /**
     * Define a macro with the given alias. The method does the same as {@link #define(Identified)} but uses the
     * {@code alias} instead of the identifier returned by the method {@link Identified#getId()}.
     *
     * @param macro the {@link Identified} object to store
     * @param alias the identifier to use in the registry instead of the one the object claims
     */
    default void define(final Identified macro, final String alias) {
        throw new UnsupportedOperationException("This method is not supported by this implementation.");
    }

    /**
     * Define a macro on the current evaluation level.
     *
     * @param macro to store in the definition structure
     */
    void define(final Macro macro);

    /**
     * Define a macro on the current evaluation level.
     *
     * @param macro to store in the definition structure
     * @param alias alias name to be used for the macro instead of the one provided by the macro itself via {@link
     *              Macro#getId()}
     */
    void define(final Macro macro, final String alias);

    /**
     * Export a built-in or user defined macro {@code id}.
     * If there are both types of macros then export both of them.
     * It is not a good practice to have a built-in macro and a user defined macro having the same name.
     * Versions prior 1.11.0 did not support exporting built-in macros.
     * <p>
     * Moves the definition of the macro {@code id} one level higher in the macro definition list. This way the macro is
     * exported into the scope of the environment that is surrounding the macro definition.
     *
     * @param id the name/identifier of the user defined macro that is to be exported.
     * @throws BadSyntax when there is some error exporting
     */
    void export(String id) throws BadSyntax;

    /**
     * Export all the user defined macros defined by the array {@code ids}
     *
     * @param ids the array of the identifiers of the user defined macros to export. Also allow leading and trailing
     *            spaces.
     * @throws BadSyntax when there is some error exporting
     */
    default void export(String... ids) throws BadSyntax {
        for (final var id : ids) {
            export(id.trim());
        }
    }

    /**
     * Start a new macro evaluation level.
     * <p>
     * Macro evaluations can have side effect. For example the macro {@code define} is used to define a macro and it
     * returns an empty string. The side effect is that it defined a new user defined macro. To let different levels
     * define macros without worrying about overwriting a macro on higher level the macro evaluation is done in levels.
     * <p>
     * For example there is a file {@code INC.txt} to be included into the main file. {@code INC.txt} needs a macro and
     * the developer creating this file names this macro {@code COUNTER}. Without the evaluation levels we would have to
     * pay attention not to use the macro {@code COUNTER} in our own file, because the file we include uses it. This is,
     * however, the implementation detail of the included file and none of the business of the using file. This is a way
     * of encapsulation. When the included file processing starts the macro level evaluation is increased and any macro
     * definition that happens on that level will remain on that level. They will temporarily hide the macros of the
     * same name in higher levels and they go out of scope as soon as the level goes one step up, when the method {@link
     * #pop(Marker)} is called.
     * <p>
     * Technically when the {@code push()} is called then the macro register creates a new level in the list of the
     * macros and in the list of the user defined macros. These elements are dropped by the method {@link #pop(Marker)}.
     * Also,
     * it saves the macro opening and closing string. These are also restored by {@link #pop(Marker)}. The method {@code
     * push()} also adds a new layer to the definition of the macro opening and closing string definitions so no layer
     * can "pop" back to a macro opening and closing string definition that way defined in a higher layer, but the same
     * time the layers do not need to pop back all macro opening and closing string definitions.
     * <p>
     * Last, but not least the method {@code push()} calls the {@link Stackable#push()} method of all macros that are
     * also {@link Stackable}. Note that this method of a stackable macro is called even if currently the macro is
     * shadowed by a lower layer macro of the same name.
     * <p>
     * Similarly to {@code push()} the method {@code pop()} calls the {@link Stackable#pop()} method of all macros that
     * are also {@link Stackable}. Note that this method of a stackable macro is called even if currently the macro is
     * shadowed by a lower layer macro of the same name. The {@link Stackable#pop()} method is not invoked for those
     * macros that are currently wiped off by the {@link #pop(Marker)}.
     *
     * @param check is used to ensure that the same code is performing the push as the pop. When {@link #pop(Marker)} is
     *              invoked it checks that the object passed as argument is the same as the object corresponding to the
     *              last {@code push(Marker)}.
     * @throws BadSyntax when the marker is already on the stack
     */
    void push(Marker check) throws BadSyntax;

    /**
     * Pop the last level of the macro stack. This level has to be marked with the marker {@code check}.
     * <p>
     * For a more detailed explanation of macro stack, see the documentation of the method {@link #push(Marker)}.
     *
     * @param check see {@link #push(Marker)}
     * @throws BadSyntax when the pop cannot be performed at the specific situation because there was no corresponding
     *                   push. The marker is not the same as the one passed to the last {@code push(Marker)} or there is
     *                   only one level in the stack, the global one, which cannot be popped.
     */
    void pop(Marker check) throws BadSyntax;

    /**
     * See the documentation of the method {@link #push(Marker)}
     * <p>
     * This method does all the checks that {@link #pop(Marker)} but does not perform a pop. It is used by the
     * processor. The processor's {@link Processor#process(Input) process()} method calls itself recursively when a new
     * block is opened. After the processing finished it checks that all pushed blocks in the recursive call were
     * popped.
     *
     * @param check see {@link #push(Marker)}
     * @throws BadSyntax when the pop cannot be performed at the specific situation because there was no corresponding
     *                   push
     */
    void test(Marker check) throws BadSyntax;

    /**
     * @return the current {@link Marker} object.
     * <p>
     * See the documentation of the method {@link #push(Marker)}
     */
    Marker test();

    /**
     * See the documentation of the method {@link #push(Marker)}
     * <p>
     * Closes most inner scope of the macros for update. The macros are still in scope, but any new macro defined will
     * be defined one level higher.
     *
     * @param check see {@link #push(Marker)}
     * @throws BadSyntax when the pop cannot be performed at the specific situation because there was no corresponding
     *                   push
     */
    void lock(Marker check) throws BadSyntax;

    /**
     * Closes most inner scope of the macros for update. The macros are still in scope, but any new macro defined will
     * be defined one level higher. This version of {@link #lock(Marker)} does not check that the last scope was opened
     * using a specific marker. This method can be used in built-in macro classes when the macro needs options and
     * therefore may need the values of the user defined macros which are inside the macro (so they are essentially
     * {@link InnerScopeDependent}, but may also define user defined macros, like {@code Eval}.
     *
     * @throws BadSyntax when there is an error
     */
    void lock() throws BadSyntax;
}
