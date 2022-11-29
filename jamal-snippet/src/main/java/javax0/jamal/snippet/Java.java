package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.IndexedPlaceHolders;
import javax0.jamal.tools.IndexedPlaceHolders.ThrowingStringSupplier;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import static javax0.jamal.tools.IndexedPlaceHolders.value;

public class Java {

    public static class ClassMacro implements Macro, InnerScopeDependent {
        private static class Trie {
            static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                    // snippet classFormats
                    "$simpleName",
                    "$name",
                    "$canonicalName",
                    "$packageName",
                    "$typeName"
                    // end snippet
            );
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = Params.<String>holder("classFormat", "format").orElse("$simpleName");
            Scan.using(processor).from(this).between("()").keys(format).parse(in);
            InputHandler.skipWhiteSpaces(in);
            final var className = in.toString().trim();
            try {
                final var klass = Class.forName(className);
                return Trie.formatter.format(format.get(),
                        klass.getSimpleName(),
                        klass.getName(),
                        klass.getCanonicalName(),
                        klass.getPackageName(),
                        klass.getTypeName()
                );
            } catch (Exception e) {
                throw new BadSyntaxAt("The class '" + className + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition(), e);
            }
        }

        @Override
        public String getId() {
            return "java:class";
        }
    }

    public static class FieldMacro implements Macro, InnerScopeDependent {
        private static class Trie {
            static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                    // OTFDC -> of the field's defining class
                    // OTF -> of the field
                    // snippet fieldFormats
                    "$name",// name OTF
                    "$classSimpleName",// simple name OTFDC
                    "$className",// name of the OTFDC
                    "$classCanonicalName",// canonical name OTFDC
                    "$classTypeName",// type name OTFDC
                    "$packageName",// package where the method is
                    "$typeClass",// type OTF
                    "$modifiers",// modifiers list of the method
                    "$value"// value OTF in case the field is both `static` and `final`
                    // end snippet
            );
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = Params.<String>holder("fieldFormat", "format").orElse("$name");
            Scan.using(processor).from(this).between("()").keys(format).parse(in);
            InputHandler.skipWhiteSpaces(in);
            final var fieldRef = in.toString().trim();
            final var parts = split(in, this);
            final var className = parts[0];
            final var fieldName = parts[1];
            final Class<?> klass;
            try {
                klass = Class.forName(className);
            } catch (Exception e) {
                throw new BadSyntaxAt("The class '" + className + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition(), e);
            }
            final Field field;
            try {
                field = klass.getDeclaredField(fieldName);
                field.setAccessible(true);
            } catch (Exception e) {
                throw new BadSyntaxAt("The field '" + fieldRef + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition(), e);
            }
            final ThrowingStringSupplier valueCalculator;
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                valueCalculator = () -> "" + field.get(null);
            } else {
                valueCalculator = () -> {
                    throw new BadSyntax("Field '" + fieldRef + "' value is not available as it is not 'final' or not 'static'");
                };
            }
            try {
                return Trie.formatter.format(format.get(),
                        value(field.getName()),
                        value(klass.getSimpleName()),
                        value(klass.getName()),
                        value(klass.getCanonicalName()),
                        value(klass.getTypeName()),
                        value(klass.getPackageName()),
                        value(field.getType().getName()),
                        value(Modifier.toString(field.getModifiers())),
                        value(valueCalculator)
                );
            } catch (Exception e) {
                throw new BadSyntax("Cannot evaluate field formatting for '" + fieldRef + "' using '" + format.get() + "'", e);
            }
        }

        @Override
        public String getId() {
            return "java:field";
        }
    }

    public static class MethodMacro implements Macro, InnerScopeDependent {
        private static class Trie {
            static final IndexedPlaceHolders formatter = IndexedPlaceHolders.with(
                    // OTMDC -> of the method's defining class
                    // OTM -> of the method
                    // snippet methodFormats
                    "$classSimpleName",// simple name OTMDC
                    "$className",// name of the OTMDC
                    "$classCanonicalName",// canonical name OTMDC
                    "$classTypeName",// type name OTMDC
                    "$packageName",// package where the method is
                    "$name",// name OTM
                    "$typeClass",// return type OTM
                    "$exceptions",// comma separated values of the exception types the method throws
                    "$parameterTypes",// comma separated parameter types
                    "$parameterCount",// number of the parameters in decimal format
                    "$modifiers"// modifiers list of the method
                    // end snippet
            );
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var pos = in.getPosition();
            final var format = Params.<String>holder("methodFormat", "format").orElse("$name");
            Scan.using(processor).from(this).between("()").keys(format).parse(in);
            final var parts = split(in, this);
            final var className = parts[0];
            final var methodName = parts[1];
            final Class<?> klass;
            try {
                klass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BadSyntaxAt("The class '" + className + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition());
            }

            final var method = Arrays.stream(klass.getDeclaredMethods()).filter(m -> m.getName().equals(methodName)).findAny().orElseThrow(
                    () -> new BadSyntaxAt("The method '" + methodName + "' cannot be found in the class '" + className + "' in the macro '" + getId() + "'.", in.getPosition())
            );
            try {
                return Trie.formatter.format(format.get(),
                        klass.getSimpleName(),
                        klass.getName(),
                        klass.getCanonicalName(),
                        klass.getTypeName(),
                        klass.getPackageName(),
                        method.getName(),
                        method.getReturnType().getName(),
                        Arrays.stream(method.getExceptionTypes()).map(Class::getName).collect(Collectors.joining(",")),
                        Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")),
                        "" + method.getParameterCount(),
                        Modifier.toString(method.getModifiers()
                        )
                );
            } catch (Exception e) {//can't really happen as we only have string values for the placeholders
                throw new BadSyntaxAt("There is an exception formatting the method '" + methodName + "' ", pos, e);
            }
        }

        @Override
        public String getId() {
            return "java:method";
        }
    }


    private static String[] split(Input in, Macro macro) throws BadSyntaxAt {
        final var trimmed = in.toString().trim();
        final int fieldStart, classEnd;
        final String className, fieldName;
        if (trimmed.length() > 0 && Character.isAlphabetic(trimmed.charAt(0))) {
            int j = trimmed.indexOf('#');
            if (j != -1) {
                fieldStart = j + 1;
                classEnd = j;
            } else {
                j = trimmed.indexOf("::");
                if (j != -1) {
                    fieldStart = j + 2;
                    classEnd = j;
                } else {
                    throw new BadSyntaxAt("Macro '" + macro.getId() + "' needs a class and a method/field name separated by '#' or '::'", in.getPosition());
                }
            }
            className = trimmed.substring(0, classEnd);
            fieldName = trimmed.substring(fieldStart);
        } else {
            final var parts = InputHandler.getParts(in, 2);
            BadSyntaxAt.when(parts.length < 2, () -> "Macro '" + macro.getId() + "' needs exactly two arguments and got " + parts.length + " from '" + in + "'",in.getPosition());
            className = parts[0];
            fieldName = parts[1];
        }
        return new String[]{className, fieldName};
    }
}
