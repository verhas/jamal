package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.PlaceHolders;
import javax0.jamal.tools.Trie;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Java {

    public static class ClassMacro implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = Params.<String>holder("classFormat", "format").orElse("$simpleName");
            Params.using(processor).from(this).between("()").keys(format).parse(in);
            InputHandler.skipWhiteSpaces(in);
            final var className = in.toString().trim();
            try {
                final var klass = Class.forName(className);
                return PlaceHolders.with(
                    // snippet classFormats
                    "$simpleName", klass.getSimpleName(),
                    "$name", klass.getName(),
                    "$canonicalName", klass.getCanonicalName(),
                    "$packageName", klass.getPackageName(),
                    "$typeName", klass.getTypeName()
                    // end snippet
                ).format(format.get());
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

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = Params.<String>holder("fieldFormat", "format").orElse("$name");
            Params.using(processor).from(this).between("()").keys(format).parse(in);
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
            final Trie.ThrowingStringSupplier valueCalculator;
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                valueCalculator = () -> "" + field.get(null);
            } else {
                valueCalculator = () -> {
                    throw new BadSyntax("Field '" + fieldRef + "' value is not available as it is not 'final' or not 'static'");
                };
            }
            try {
                return PlaceHolders.with(
                    // OTFDC -> of the field's defining class
                    // OTF -> of the field
                    // snippet fieldFormats
                    "$name", field.getName(), // name OTF
                    "$classSimpleName", klass.getSimpleName(), // simple name OTFDC
                    "$className", klass.getName(), // name of the OTFDC
                    "$classCanonicalName", klass.getCanonicalName(),// canonical name OTFDC
                    "$classTypeName", klass.getTypeName(), // type name OTFDC
                    "$packageName", klass.getPackageName(), // package where the method is
                    "$typeClass", field.getType().getName(), // type OTF
                    "$modifiers", Modifier.toString(field.getModifiers()) // modifiers list of the method
                ).and(
                    "$value", valueCalculator // value OTF in case the field is both `static` and `final`
                    // end snippet
                ).format(format.get());
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

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var pos = in.getPosition();
            final var format = Params.<String>holder("methodFormat", "format").orElse("$name");
            Params.using(processor).from(this).between("()").keys(format).parse(in);
            final var parts = split(in,this);
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
                return PlaceHolders.with(
                    // OTMDC -> of the method's defining class
                    // OTM -> of the method
                    // snippet methodFormats
                    "$classSimpleName", klass.getSimpleName(), // simple name OTMDC
                    "$className", klass.getName(), // name of the OTMDC
                    "$classCanonicalName", klass.getCanonicalName(),// canonical name OTMDC
                    "$classTypeName", klass.getTypeName(), // type name OTMDC
                    "$packageName", klass.getPackageName(), // package where the method is
                    "$name", method.getName(), // name OTM
                    "$typeClass", method.getReturnType().getName(), // return type OTM
                    "$exceptions", Arrays.stream(method.getExceptionTypes()).map(Class::getName).collect(Collectors.joining(",")), // comma separated values of the exception types the method throws
                    "$parameterTypes", Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")),// comma separated parameter types
                    "$parameterCount", "" + method.getParameterCount(), // number of the parameters in decimal format
                    "$modifiers", Modifier.toString(method.getModifiers() // modifiers list of the method
                        // end snippet
                    )
                ).format(format.get());
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
            if (parts.length < 2) {
                throw new BadSyntaxAt("Macro '" + macro.getId() + "' needs exactly two arguments and got " + parts.length + " from '" + in.toString() + "'", in.getPosition());
            }
            className = parts[0];
            fieldName = parts[1];
        }
        return new String[]{className, fieldName};
    }
}
