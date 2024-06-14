package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * This macro can be used to define a Java implemented macro class, which is not exported by the module system.
 * <p>
 * The macro code can contain {@code use global com.my.class as name} or {@code use com.my.class as name} to use the
 * class {@code com.my.class} as a macro implementation. The class has to implement the {@link Macro} interface. In case
 * it is defined as {@code global} then it will get into the global level, otherwise to the local level.
 * <p>
 * If the class name does not contain any dot character, then it is assumed that this is not a class name, rather an
 * already loaded macro name. In that case the {@code as alias} is not optional.
 * <p>
 * The syntax is
 *
 * <pre>{@code
 *  use [global] com.package.name.MacroClass [as Alias]
 * }</pre>
 * <p>
 * or
 *
 * <pre>{@code
 *  use [global] macroname as Alias
 * }</pre>
 * <p>
 * There can be many such declarations on the macro input separated by commas.
 */
public class Use implements Macro {
    /**
     * The syntax is: {@code [global] com.package.name.MacroClass [as Alias]}
     * <ul>
     * <li>{@code $1} will be "global" or ""
     * <li>{@code $2} will be the fully qualified name of the class, or the name of the macro if there is no . in the name
     * <li>{@code $3} will be the alias or null if no alias
     * </ul>
     */
    private static final Pattern pattern = Pattern.compile("((?:global\\s+)?)([\\w\\d:.]+)(?:\\s+as\\s+([\\w][\\w\\d:]*))?");

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var macroImports = input.toString().split(",");
        for (final var macroImport : macroImports) {
            final var stripped = getTrimmed(macroImport);
            if (!stripped.isEmpty()) {
                evaluateSingleUseDeclaration(processor, stripped);
            }
        }
        return "";
    }

    /**
     * Remove all white space characters from the string and replace them with a single space character.
     * The final string will not have any other white space character than normal space and there will be no sequence of
     * consecutive space characters.
     *
     * @param useDeclaration the string to be trimmed
     * @return the trimmed string
     */
    private static String getTrimmed(String useDeclaration) {
        return useDeclaration
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Evaluate a single use declaration.
     *
     * @param processor the processor
     * @param stripped  the stripped declaration, no new lines in it and only one declaration
     * @throws BadSyntax if the declaration is not correct, or the actual macro does not exist or the class cannot be
     *                   loaded, instantiated or the class is not a macro
     */
    private void evaluateSingleUseDeclaration(Processor processor, String stripped) throws BadSyntax {
        var matcher = pattern.matcher(stripped);
        BadSyntax.when(!matcher.matches(), "use macro has bad syntax '%s'", stripped);
        final var isGlobal = new AtomicBoolean(!matcher.group(1).isEmpty());
        final var klassName = matcher.group(2);
        final var alias = matcher.group(3);
        final var register = processor.getRegister();
        final Macro macro = getMacro(klassName, isGlobal, register, alias);
        registerMacro(macro, register, isGlobal.get(), alias);
    }

    /**
     * Get the macro instance from the register or create a new instance if the macro is given by the name of the class.
     * <p>
     * The method has two side effects. It sets the {@code isGlobal} to {@code true} if the macro is global, and it throws
     * an exception if the macro is provided by the name of the macro and the alias is not provided.
     *
     * @param klassName the name of the class or the name of the macro.
     *                  It is assumed that it is a class name if it contains a dot.
     * @param isGlobal  if the macro is global. If the name of the macro is provided, and it contains a colon {@code :},
     *                  then the macro is global and in that case this variable is also set to be {@code true}.
     * @param register  the register that holds all the macros
     * @param alias     the alias of the macro. It is only used in error checking to throw an exception if the alias is
     *                  null or empty when the macro is provided by the name of the macro. In that case, an alias is mandatory.
     *                  There is no point to registering a macro with a name it is already registered with.
     * @return the macro instance that is either created or found in the register
     * @throws BadSyntax if the macro is specified
     *                   <ul>
     *                   <li>by a class name, but
     *                     <ul>
     *                     <li>the class does not exist, or
     *                     <li>does not have a public no-argument constructor,
     *                     <li>the class is not an instance of {@link Macro}, or
     *                     </ul>
     *                   <li>by the name
     *                     <ul>
     *                     <li>but there is no macro registered by that name, or
     *                     <li>there is no alias specified in the declaration.
     *                     </ul>
     *                   </ul>
     */
    private Macro getMacro(String klassName, AtomicBoolean isGlobal, MacroRegister register, String alias) throws BadSyntax {
        final Macro macro;
        if (klassName.contains(".")) {
            macro = forName(klassName);
        } else {
            if (klassName.contains(":")) {
                isGlobal.set(true);
            }
            macro = register.getMacro(klassName)
                    .orElseThrow(() -> new BadSyntax("There is no built-in macro with the name '" + klassName + "'"));
            BadSyntax.when(alias == null || alias.isEmpty(), () -> String.format("You cannot define an alias for the macro '%s' without actually providing an alias after the 'as'",
                    klassName));
        }
        return macro;
    }

    /**
     * Register the macro in the register.
     *
     * @param macro    the macro to register
     * @param register the register that holds all the macros
     * @param isGlobal if the macro is global
     * @param alias    the alias of the macro. Ignored if {@code null} or empty
     */
    private static void registerMacro(Macro macro, MacroRegister register, boolean isGlobal, String alias) {
        if (isGlobal) {
            if (alias != null && !alias.isEmpty()) {
                register.global(macro, alias);
            } else {
                register.global(macro);
            }
        } else {
            if (alias != null && !alias.isEmpty()) {
                register.define(macro, alias);
            } else {
                register.define(macro);
            }
        }
    }

    /**
     * Get a new instance of the built-in macro.
     *
     * @param klassName the name of the class with dots as separator that implements the {@link Macro} interface
     * @return a new instance of the macro class
     * @throws BadSyntax if the class cannot be found or the instance cannot be created or the class is not a macro
     */
    private Macro forName(final String klassName) throws BadSyntax {
        return forName(klassName, klassName);
    }

    /**
     * Get a new instance of the built-in macro.
     * <p>
     * The implementation tries to load the class first.
     * If the class is a static inner class, this attempt fails.
     * In that case, the method changes the last `.` to `$` and tires again so long as long there is a `.` in the
     * class name.
     * <p>
     * The class can be a top level class or a static inner class.
     * The class has to implement the {@link Macro} interface and should have a public constructor that takes no arguments.
     *
     * @param klassName    the name of the class with dots as separator. Using {@code $} in case of inner classes is
     *                     also possible, but it is not the intended use. In case of an inner class the algorithm tries
     *                     to load the class as a top level class, and in case it fails it replaces the last dot with a
     *                     {@code $} sign and tries to load again and again until all dots are replaced.
     * @param originalName is the original class name before the search process started to replace the {@code .} to
     *                     {@code $}. Used only to create the exception and needed for the recursive calls.
     * @return the instance of the macro class
     * @throws BadSyntax if the class cannot be found or the instance cannot be created or the class is not a macro
     */
    private Macro forName(final String klassName, final String originalName) throws BadSyntax {
        final Macro macro;
        final Class<?> klass;
        try {
            klass = Class.forName(klassName);
        } catch (Exception e) {
            if (!klassName.contains(".")) {
                throw new BadSyntax("Class '" + originalName + "' cannot be used as macro", e);
            }
            var lastDot = klassName.lastIndexOf('.');
            return forName(klassName.substring(0, lastDot) + "$" + klassName.substring(lastDot + 1), originalName);
        }
        try {
            macro = (Macro) klass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BadSyntax("Class '" + klassName + "' cannot be used as macro", e);
        }
        return macro;
    }

}
