package javax0.jamal.api;

import java.util.Optional;

public interface MacroRegister extends Delimiters {

    /**
     * Get a macro based on the id of the macro.
     *
     * @param id the identifier (name) of the macro
     * @return the macro in an optional. Optional.empty() if the macro can not be found.
     */
    Optional<Macro> geMacro(String id);

    /**
     * Get a user defined macro based on the id of the macro.
     *
     * @param id the identifier (name) of the macro
     * @return the user defined macro in an optional. Optional.empty() if the macro can not be found.
     */
    Optional<UserDefinedMacro> getUserMacro(String id);

    /**
     * Define a user defined macro on the global level.
     * @param macro to store in the definition structure on the top level.
     */
    void global(UserDefinedMacro macro);

    /**
     * Define a macro on the global level.
     * @param macro to store in the definition structure on the top level.
     */
    void global(Macro macro);

    /**
     * Define a user defined macro in the current evaluation level.
     *
     * @param macro to store in the definition structure.
     */
    void define(UserDefinedMacro macro);

    /**
     * Define a macro on the current evaluation level.
     *
     * @param macro to store in the definition structure
     */
    void define(Macro macro);

    /**
     * Export the user defined macro {@code id}.
     * <p>
     * Moves the definition of the macro {@code id} one level higher in the macro definition list. This way the macro
     * is exported into the scope of the environment that is surrounding the macro definition.
     *
     * @param id the name/identifier of the user defined macro that is to be exported.
     * @throws BadSyntax
     */
    void export(String id) throws BadSyntax;

    /**
     * Start a new macro evaluation level.
     * <p>
     * Macro evaluations can have side effect. For example the macro {@code define} is used to define a macro and
     * it returns an empty string. The side effect is that it defined a new user defined macro. To let different levels
     * define macros without worrying about overwriting a macro on higher level the macro evaluation is done in
     * levels.
     * <p>
     * For example there is a file {@code INC.txt} to be included into the main file. {@code INC.txt} needs a macro
     * and the developer creating this file names this macro {@code COUNTER}. Without the evaluation levels
     * we would have to pay attention not to use the macro {@code COUNTER} in our own file, because the file we
     * include uses it. This is, however, the implementation detail of the included file and none of the business
     * of the using file. This is a way of encapsulation. When the included file processing starts the macro level
     * evaluation is increased and any macro definition that happens on that level will remain on that level.
     * They will temporarily hide the macros of the same name in higher levels and they go out of scope as soon as the
     * level goes one step up, when the method {@link #pop()} is called.
     * <p>
     * Technically when the {@code push()} is called then the macro register creates a new level in the list of
     * the macros and in the list of the user defined macros. These elements are dropped by the method {@code pop()}.
     * Also it saves the macro opening and closing string. These are also restored by {@code pop()}. The method
     * {@code push()} also adds a new layer to the definition of the macro opening and closing string definitions so
     * no layer can "pop" back to a macro opening and closing string definition that way defined in a higher layer,
     * but the same time the layers do not need to pop back all macro opening and closing string definitions.
     * <p>
     * Last, but not least the method {@code push()} calls the {@link Stackable#push()} method of all macros that are
     * also {@link Stackable}. Note that this method of a stackable macro is called even if currently the macro is
     * shadowed by a lower layer macro of the same name.
     * <p>
     * Similarly to {@code push()} the method {@code pop()} calls the {@link Stackable#pop()} method
     * of all macros that are
     * also {@link Stackable}. Note that this method of a stackable macro is called even if currently the macro is
     * shadowed by a lower layer macro of the same name. The {@link Stackable#pop()} method is not invoked for those
     * macros that are currently wiped off by the {@link #pop()}.
     */
    void push();

    /**
     * See the documentation of the method {@link #push()}
     */
    void pop();
}
