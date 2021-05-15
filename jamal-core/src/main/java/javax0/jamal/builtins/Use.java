package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * This macro can be used to define a Java implemented macro class, which is not exported by the module system.
 * <p>
 * The macro code can contain {@code use global com.my.class as name} or {@code use com.my.class as name} to use the
 * class {@code com.my.class} as a macro implementation. The class has to implement the {@link Macro} interface. In case
 * it is defined as {@code global} then it will get into the global level, otherwise to the local level.
 * <p>
 * If the class name does not contain any dot character then it is assument that this is not a class name, rather an
 * already loaded macro name. In that case the {@code as alias} is not optional.
 */
public class Use implements Macro {
    // The syntax is:    [global] com.package.name.MacroClass [as Alias]
    // $1 will be "global" or ""
    // $2 will be the fully qualified name of the class
    // $3 will be the alias or null if no alias
    private static final Pattern pattern = Pattern.compile("((?:global\\s+)?)([\\w:.]+)(?:\\s+as\\s+(\\w+))?");

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var macroImports = input.toString().split(",");
        for (final var macroImport : macroImports) {
            final var stripped = macroImport
                .trim()
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ");
            if (stripped.length() > 0) {
                var matcher = pattern.matcher(stripped);
                if (!matcher.matches()) {
                    throw new BadSyntax("use macro has bad syntax '" + stripped + "'");
                }
                final var isGlobal = matcher.group(1).length() > 0;
                final var klassName = matcher.group(2);
                final var alias = matcher.group(3);
                final Macro macro;
                final var register = processor.getRegister();
                if (klassName.contains(".")) {
                    macro = forName(klassName, klassName);
                } else {
                    macro = register.getMacro(klassName)
                        .orElseThrow(() -> new BadSyntax("There is no built-in macro with the name '" + klassName + "'"));
                    if (alias == null || alias.length() == 0) {
                        throw new BadSyntax(format(
                            "You cannot define an alias for the macro '%s' without actually providing an alias after the 'as'"
                            , klassName));
                    }
                }
                if (isGlobal) {
                    if (alias != null && alias.length() > 0) {
                        register.global(macro, alias);
                    } else {
                        register.global(macro);
                    }
                } else {
                    if (alias != null && alias.length() > 0) {
                        register.define(macro, alias);
                    } else {
                        register.define(macro);
                    }
                }
            }
        }
        return "";
    }

    /**
     * Get a new instance of the built-in macro. The implementation loads the class first. If the class is an static
     * inner class then it recognizes that and loads the inner class.
     *
     * @param klassName    the name of the class with dots as separator. Using {@code $} in case of inner classes is
     *                     also possible, but it is not the intended use. In case of an inner class the algorithm tries
     *                     to load the class as a top level class and in case it fails it replaces the last dot with a
     *                     {@code $} sign and tries to load again and again until all dots are replaces.
     * @param originalName is the original class name before the search process started to replace the {@code .} to
     *                     {@code $}. Used to create the exception.
     * @return the instance of the macro class
     * @throws BadSyntax if the class cannot be found or the instance cannot be created
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
