package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.PlaceHolders;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Java {

    public static class ClassMacro implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var reader = MacroReader.macro(processor);
            final var format = reader.readValue("classFormat").orElse("$simpleName");
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
                ).format(format);
            } catch (Exception e) {
                throw new BadSyntaxAt("The class '" + className + "' cannot be found on the classpath in the macro '" + getId() + "'.", in.getPosition());
            }
        }

        @Override
        public String getId() {
            return "java:class";
        }
    }

    public static class MethodMacro implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var pos = in.getPosition();
            final var reader = MacroReader.macro(processor);
            final var format = reader.readValue("methodFormat").orElse("$name");
            final var trimmed = in.toString().trim();
            final int methodStart, classEnd;
            final String className, methodName;
            if (trimmed.length() > 0 && Character.isAlphabetic(trimmed.charAt(0))) {
                int j = trimmed.indexOf('#');
                if (j != -1) {
                    methodStart = j + 1;
                    classEnd = j;
                } else {
                    j = trimmed.indexOf("::");
                    if (j != -1) {
                        methodStart = j + 2;
                        classEnd = j;
                    } else {
                        throw new BadSyntaxAt("Macro '" + getId() + "' needs a class and a method name separated by '#' or '::'", in.getPosition());
                    }
                }
                className = trimmed.substring(0, classEnd);
                methodName = trimmed.substring(methodStart);
            } else {
                final var parts = InputHandler.getParts(in, 2);
                if (parts.length < 2) {
                    throw new BadSyntaxAt("Macro '" + getId() + "' needs exactly two arguments and got " + parts.length + " from '" + in.toString() + "'", in.getPosition());
                }
                className = parts[0];
                methodName = parts[1];
            }
            final Class klass;
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
                    "$classTypeName", klass.getTypeName(), // type name OTMC
                    "$packageName", klass.getPackageName(), // package where the method is
                    "$name", method.getName(), // name OTM
                    "$typeClass", method.getReturnType().getName(), // return type OTM
                    "$exceptions", Arrays.stream(method.getExceptionTypes()).map(Class::getName).collect(Collectors.joining(",")), // comma separated values of the exception types the method throws
                    "$parameterTypes", Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(",")),// comma separated parameter types
                    "$parameterCount", "" + method.getParameterCount(), // number of the parameters in decimal format
                    "$modifiers", Modifier.toString(method.getModifiers() // modifiers list of the method
                        // end snippet
                    )
                ).format(format);
            } catch (Exception e) {
                throw new BadSyntaxAt("There is an exception formatting the method '" + methodName + "' ", pos, e);
            }
        }

        @Override
        public String getId() {
            return "java:method";
        }
    }
}
