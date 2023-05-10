package javax0.jamal.snippet.tools;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Different tools to ease the handling of methods.
 * The class has a builder to set up the method and the options.
 * That way you can say, for example:
 *
 * <pre>{@code
 *    MethodTool.with(method).asInterface().asPublic().signature();
 * }</pre>
 * <p>
 * to get the signature of the method as a string.
 * The generated string can be decorated with a function that is passed to the builder via the method {@code
 * decorateNameWith}. Calling the `asInterface()` will modify the string generation so that it fits the signature of
 * a method in an interface (no need for {@code public} modifier).
 * <p>
 * This method can be used to generate code.
 */
public class MethodTool {

    final protected AtomicInteger argCounter = new AtomicInteger(0);
    protected Method method;
    private String type = null;
    private boolean isInterface = false;
    private boolean isPublic = false;
    private Function<String, String> decorator;

    public static MethodTool with(Method method) {
        var it = new MethodTool();
        it.method = method;
        return it;
    }

    /**
     * specify a different type than the original type of the method. This is handy when the generated method returns
     * something different. For example, you want to generate a conversion method for each existing method.
     *
     * @param type the new type as a string and as it will be used in the generated method signature
     * @return {@code this}
     */
    public MethodTool withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Decorate the name of the method. This is handy when you want to generate a method that is similar to the one you
     * want to extend, and the name eventually cannot be the same.
     *
     * @param decorator the decorator that modifies the original name
     * @return {@code this}
     */
    public MethodTool decorateNameWith(Function<String, String> decorator) {
        this.decorator = decorator;
        return this;
    }

    /**
     * The generated signature will be used in an interface.
     *
     * @return {@code this}
     */
    public MethodTool asInterface() {
        this.isInterface = true;
        return this;
    }

    public MethodTool asInterface(boolean isInterface) {
        this.isInterface = isInterface;
        return this;
    }

    /**
     * The generated signature will declare the method as public, even if the original method is not public.
     *
     * @return {@code this}
     */
    public MethodTool asPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    public MethodTool asPublic() {
        this.isPublic = true;
        return this;
    }

    static String methodSignature(Method method) {
        return with(method).signature();
    }

    /**
     * Get the signature of the method.
     *
     * @return the method signature as it is
     */
    public String signature() {
        return signature(false);
    }

    /**
     * Get the full declaration signature of the method. This is the line that starts the declaration of the method in a
     * class or in an interface.
     * <p>
     * The argument names in the signature will be {@code arg1}, {@code arg2}, etc.
     * This is assumed when generating the code that calls the method calling the method {@link #call()}.
     *
     * @param concrete the declaration is concrete even if the original method is abstract
     * @return the signature of the method
     */
    public String signature(boolean concrete) {
        final var argumentList = argumentList();
        final String exceptionList = exceptionList();
        final String modifiers = modifiers(concrete);
        return modifiers +
                (type == null ? ReflectionTools.typeAsString(method) : type) +
                " " +
                decoratedName(method) +
                "(" + argumentList + ")" +
                (exceptionList.length() == 0 ? "" : " throws " + exceptionList);
    }

    /**
     * Get the modifiers of the method as a string.
     *
     * @param concrete do not include the abstract modifier, even if the method is abstract.
     * @return the modifiers as a string
     */
    public String modifiers(final boolean concrete) {
        if (isPublic) {
            if (concrete) {
                return (isInterface ? "" : "public " + ReflectionTools.modifiersStringNoAccessConcrete(method));
            } else {
                return (isInterface ? "" : "public " + ReflectionTools.modifiersStringNoAccess(method));
            }
        } else {
            if (concrete) {
                return (isInterface ? "" : (ReflectionTools.modifiersStringConcrete(method)));
            } else {
                return (isInterface ? "" : (ReflectionTools.modifiersString(method)));
            }
        }
    }

    public String exceptionList() {
        final var exceptionList = Arrays.stream(method.getGenericExceptionTypes())
                .map(ReflectionTools::getGenericTypeName)
                .collect(Collectors.joining(","));
        return exceptionList;
    }

    public String argumentList() {
        final var types = method.getGenericParameterTypes();
        final var sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (i == types.length - 1 && method.isVarArgs()) {
                sb.append(getVarArg(types[i]));
            } else {
                sb.append(getArg(types[i]));
            }
        }
        var argumentList = sb.toString();
        return argumentList;
    }

    /**
     * Get a string that calls the original method. The arguments are named {@code arg1}, {@code arg2}, etc.
     *
     * @return the call to the original method. Name decoration is NOT applied.
     */
    public String call() {
        var argList = Arrays.stream(method.getGenericParameterTypes())
                .map(this::getArgCall)
                .collect(Collectors.joining(","));

        return method.getName() + "(" + argList + ")";
    }


    /**
     * Get a string that calls the method. The arguments are named {@code arg1}, {@code arg2}, etc.
     *
     * @return the call to the original method. Name decoration IS applied.
     */
    public String callDecorated() {
        var argList = Arrays.stream(method.getGenericParameterTypes())
                .map(this::getArgCall)
                .collect(Collectors.joining(","));

        return decoratedName(method) + "(" + argList + ")";
    }

    public String getArgCall(Type t) {
        return "arg" + argCounter.addAndGet(1);
    }

    public String getVarArg(Type t) {
        final var normType = ReflectionTools.getGenericTypeName(t);
        final String actualType = normType.substring(0, normType.length() - 2) + "... ";
        return actualType + " arg" + argCounter.addAndGet(1);
    }

    public String getArg(Type t) {
        final var normType = ReflectionTools.getGenericTypeName(t);
        return normType + " arg" + argCounter.addAndGet(1);
    }

    /**
     * Decorate the method name if {@code decorator} is not {@code null}.
     *
     * @param method of which the name is retrieved
     * @return the decorated name
     */
    private String decoratedName(Method method) {
        if (decorator == null) {
            return method.getName();
        } else {
            return decorator.apply(method.getName());
        }
    }
}
