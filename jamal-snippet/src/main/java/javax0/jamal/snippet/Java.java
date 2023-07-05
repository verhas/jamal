package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.IndexedPlaceHolders;
import javax0.jamal.tools.IndexedPlaceHolders.ThrowingStringSupplier;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
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
            final var klass = classForName(className, processor, getId(), in.getPosition());
            try {
                return Trie.formatter.format(format.get(),
                        klass.getSimpleName(),
                        klass.getName(),
                        klass.getCanonicalName(),
                        klass.getPackageName(),
                        klass.getTypeName()
                );
            } catch (Exception e) {
                throw new BadSyntaxAt(String.format("The class '%s' cannot be formatted in the macro '%s'.", className, getId()), in.getPosition(), e);
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
                    "$modifiers",// modifier list of the method
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
            klass = classForName(className, processor, getId(), in.getPosition());
            final Field field;
            try {
                field = klass.getDeclaredField(fieldName);
                field.setAccessible(true);
            } catch (Exception e) {
                throw new BadSyntaxAt("The field '" + fieldRef + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition(), e);
            }
            final ThrowingStringSupplier valueCalculator;
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                valueCalculator = () -> Objects.toString(field.get(null));
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
                    "$modifiers"// modifier list of the method
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
            klass = classForName(className, processor, getId(), in.getPosition());

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
                        Objects.toString(method.getParameterCount()),
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

    private static Optional<CompileJavaMacros.Holder> getHolder(final Processor processor, final String id) throws BadSyntax {
        final var result = processor.getRegister()
                .getUserDefined(id)
                .filter(p -> p instanceof ObjectHolder)
                .map(p -> (ObjectHolder<?>) p)
                .map(ObjectHolder::getObject)
                .filter(o -> o instanceof CompileJavaMacros.Holder)
                .map(o -> (CompileJavaMacros.Holder) o);
        if (result.isEmpty()) {
            throw new BadSyntax("Macro " + id + " is not defined or not a class store. Were Java classes specified with 'java:sources'?");
        } else {
            return result;
        }
    }

    private static Class<?> classForName(final String className, final Processor processor, final String macroId, final Position pos) throws BadSyntax {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            final var loaded = getHolder(processor, CompileJavaMacros.LOADED_CLASSES);
            try {
                return loaded.get().loaded.get(className);
            } catch (ClassNotFoundException ex) {
                throw new BadSyntaxAt("The class '" + className + "' cannot be found on the classpath in the macro '" + macroId + "'.", pos);
            }
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
            BadSyntaxAt.when(parts.length < 2, "Macro '" + macro.getId() + "' needs exactly two arguments and got " + parts.length + " from '" + in + "'", in.getPosition());
            className = parts[0];
            fieldName = parts[1];
        }
        return new String[]{className, fieldName};
    }
}
