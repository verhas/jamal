package javax0.jamal.snippet;

import com.javax0.sourcebuddy.Compiler;
import com.javax0.sourcebuddy.Fluent;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.snippet.tools.MethodTool;
import javax0.jamal.snippet.tools.ReflectionTools;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.refi.selector.Selector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CompileJavaMacros {

    /**
     * The name of the macro that will hold the loaded classes through the {@link
     * com.javax0.sourcebuddy.Compiler.Loaded} object.
     */
    // snipline LOADED_CLASSES filter="(.*)"
    public static final String LOADED_CLASSES = "loadedClasses";
    private static final String TRUE = "true";

    public static class Holder {
        public final Compiler.Loaded loaded;

        final Map<Method, String> methods = new HashMap<>();
        final Map<Field, String> fields = new HashMap<>();

        private Holder(final Compiler.Loaded loaded) {
            this.loaded = loaded;
        }
    }

    private static Optional<Holder> getHolder(final Processor processor, final String id) throws BadSyntax {
        final var result = processor.getRegister()
                .getUserDefined(id)
                .filter(p -> p instanceof ObjectHolder)
                .map(p -> (ObjectHolder<?>) p)
                .map(ObjectHolder::getObject)
                .filter(o -> o instanceof Holder)
                .map(o -> (Holder) o);
        if (result.isEmpty()) {
            throw new BadSyntax("Macro " + id + " is not defined or not a class store. Were Java classes specified with 'java:sources'?");
        } else {
            return result;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Compiler.Loaded getLoaded(final Processor processor, final String id) throws BadSyntax {
        return getHolder(processor, id)
                .map(h -> h.loaded)
                .get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Map<Method, String> getMethods(final Processor processor, final String id) throws BadSyntax {
        return getHolder(processor, id)
                .map(h -> h.methods)
                .get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Map<Field, String> getFields(final Processor processor, final String id) throws BadSyntax {
        return getHolder(processor, id)
                .map(h -> h.fields)
                .get();
    }

    private static Params.Param<String> separatorParam(final Scanner.ScannerObject scanner) {
        return scanner.str("sep", "separator").defaultValue(",");
    }

    private static Params.Param<String> storeNameParam(final Scanner.ScannerObject scanner) {
        return scanner.str("store").defaultValue(LOADED_CLASSES);
    }

    private static Params.Param<String> selectorParam(final Scanner.ScannerObject scanner) {
        return scanner.str("selector", "only", "filter").defaultValue(TRUE);
    }

    private static Params.Param<String> classParam(final Scanner.ScannerObject scanner) {
        return scanner.str("class").defaultValue(TRUE);
    }

    private static Params.Param<String> constructorParam(final Scanner.ScannerObject scanner) {
        return scanner.str("constructor").defaultValue(TRUE);
    }

    public static class Compile implements Macro, Scanner.WholeInput {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in,processor);
            final var storeName = storeNameParam(scanner);
            final var source  = scanner.str("source", "src", "sources").defaultValue(null);
            final var classes = scanner.str("class", "classes").defaultValue(null);
            final var options = scanner.str("options", "compilerOptions").defaultValue("");
            scanner.done();
            try {
                final var sourceLocations = source.get() == null ? null : source.get().split(",");
                final var classLocations = classes.get() == null ? null : classes.get().split(",");
                BadSyntax.when((sourceLocations == null || sourceLocations.length == 0) &&
                                (classLocations == null || classLocations.length == 0),
                        "There is no location defined to compile");

                final Fluent.AddSource compilerObject = Compiler.java().options(options.get().trim().split("\\s+"));
                if (sourceLocations != null && sourceLocations.length > 0 && compilerObject.canCompile()) {
                    tryToCompileAndLoadFromSources(in, processor, storeName, sourceLocations, classLocations, compilerObject);
                    return "";
                }
                tryToLoadFromCompiledCode(in, processor, storeName, classLocations, () -> new BadSyntax("There is no location defined from which we could compile or load the Java code."));
            } catch (Exception e) {
                throw new BadSyntax("There was an exception while compiling Java sources.", e);
            }
            return "";
        }

        /**
         * Try to load the classes from the compiled code.
         *
         * @param in                the input, used to get the reference for the BadSyntax exception if there is an error
         * @param processor         the processor used to locate the files relative to the input reference
         * @param storeName         the name of the macro where to store the result
         * @param classLocations    the locations where the compiled classes are located
         * @param exceptionSupplier the exception to throw if there is no location defined from which we could load the Java code
         * @throws Exception if the files cannot be compiled or loaded, or some other error occurs
         */
        private static void tryToLoadFromCompiledCode(Input in, Processor processor,
                                                      Params.Param<String> storeName,
                                                      String[] classLocations,
                                                      Supplier<Exception> exceptionSupplier) throws Exception {
            if (classLocations != null && classLocations.length > 0) {
                try {
                    loadFromCompiledCode(in, processor, storeName, classLocations);
                } catch (Exception e) {
                    final var ex = exceptionSupplier.get();
                    ex.addSuppressed(e);
                    throw ex;
                }
            } else {
                throw exceptionSupplier.get();
            }
        }

        /**
         * Try to compile the source code and load it from the compilation result.
         * If there is an error during the compilation, then try to load the classes from the class locations.
         *
         * @param in              the input, used to get the reference for the BadSyntax exception if there is an error
         * @param processor       the processor used to locate the files relative to the input reference
         * @param storeName       the name of the macro where to store the result
         * @param sourceLocations the locations where the source code is located
         * @param classLocations  the locations where the compiled classes are located
         * @param compilerObject  the compiler object that can compile the source code
         * @throws Exception if the files cannot be compiled or loaded, or some other error occurs
         */
        private static void tryToCompileAndLoadFromSources(Input in,
                                                           Processor processor,
                                                           Params.Param<String> storeName,
                                                           String[] sourceLocations,
                                                           String[] classLocations,
                                                           Fluent.AddSource compilerObject) throws Exception {
            try {
                compileAndLoadFromSources(in, processor, storeName, sourceLocations, compilerObject);
            } catch (Exception e) {
                tryToLoadFromCompiledCode(in, processor, storeName, classLocations, () -> e);
            }
        }

        /**
         * Load the classes from the compiled code.
         *
         * @param in             the input, used to get the reference for the BadSyntax exception if there is an error
         * @param processor      the processor used to locate the files relative to the input reference
         * @param storeName      the name of the macro where to store the result
         * @param classLocations the locations where the compiled classes are located
         * @throws IOException            if the files cannot be loaded
         * @throws ClassNotFoundException if some classes cannot be loaded
         * @throws BadSyntax              if there is a NoClassDefFoundError, since this is an error, but in this case it should
         *                                be handled more like an exception, it is converted to a BadSyntax exception
         */
        private static void loadFromCompiledCode(Input in,
                                                 Processor processor,
                                                 Params.Param<String> storeName,
                                                 String[] classLocations) throws IOException, ClassNotFoundException, BadSyntax {
            final var compilerObject = Compiler.java();
            final var dir0 = FileTools.absolute(in.getReference(), classLocations[0]);
            final var compiler = compilerObject.byteCode(Paths.get(dir0));
            for (int i = 1; i < classLocations.length; i++) {
                final var dir = FileTools.absolute(in.getReference(), classLocations[i]);
                compiler.byteCode(Paths.get(dir));
            }
            try {
                final var loaded = new Holder(compiler.load(Compiler.LoaderOption.SLOPPY));
                processor.getRegister().global(new IdentifiedObjectHolder<>(loaded, storeName.get()));
            } catch (NoClassDefFoundError e) {
                throw new BadSyntax(String.format("There was an exception while loading Java classes at '%s'", in), e);
            }
        }

        /**
         * Compile the source code and load it from the compilation result.
         *
         * @param in              the input, used to get the reference for the BadSyntax exception if there is an error
         * @param processor       the processor used to locate the files relative to the input reference
         * @param storeName       the name of the macro where to store the result
         * @param sourceLocations the locations where the source code is located
         * @param compilerObject  the compiler object that can compile the source code
         * @throws IOException            if the files cannot be loaded
         * @throws ClassNotFoundException if some classes cannot be loaded
         * @throws BadSyntax              if there is a NoClassDefFoundError, since this is an error, but in this case it should
         *                                be handled more like an exception, it is converted to a BadSyntax exception
         */
        private static void compileAndLoadFromSources(Input in,
                                                      Processor processor,
                                                      Params.Param<String> storeName,
                                                      String[] sourceLocations,
                                                      Fluent.AddSource compilerObject) throws IOException, ClassNotFoundException, BadSyntax {
            final var dir0 = FileTools.absolute(in.getReference(), sourceLocations[0]);
            final var compiler = compilerObject.from(Paths.get(dir0));
            for (int i = 1; i < sourceLocations.length; i++) {
                final var dir = FileTools.absolute(in.getReference(), sourceLocations[i]);
                compiler.from(Paths.get(dir));
            }
            final Holder loaded;
            try {
                loaded = new Holder(compiler.compile().load());
            } catch (Compiler.CompileException e) {
                throw new BadSyntax(String.format("There was a compiler exception + %s", e.getMessage()), e);
            }
            processor.getRegister().global(new IdentifiedObjectHolder<>(loaded, storeName.get()));
        }

        @Override
        public String getId() {
            return "java:sources";
        }
    }

    /**
     * List all the classes that were loaded. Since the fully qualified names are unique (unlike the names of methods or
     * fields), there is no need to convert the classes to named objets and to hold them in object holders to retrieve
     * them later by the names. Just return the names comma separated.
     */
    public static class ListClasses implements Macro, Scanner.WholeInput {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in,processor);
            final var storeName = storeNameParam(scanner);
            final var selectorParam = selectorParam(scanner);
            final var sep = separatorParam(scanner);
            scanner.done();
            return getLoaded(processor, storeName.get()).stream()
                    .filter(Selector.compile(selectorParam.get())::match)
                    .map(Class::getName)
                    .collect(Collectors.joining(sep.get()));
        }

        @Override
        public String getId() {
            return "java:classes";
        }
    }

    private static class MethodHolder extends IdentifiedObjectHolder<Method> implements UserDefinedMacro {
        private MethodHolder(final Processor processor, final Method object, final String id) {
            super(object, id);
            processor.getRegister().global(this);
        }


        @Override
        public String evaluate(final String... parameters) throws BadSyntax {
            if (parameters.length == 0) {
                return getObject().getName();
            }
            switch (parameters[0]) {
                case "signature":
                    boolean abstractOnly = true;
                    boolean interfaceOnly = false;
                    boolean publicOnly = false;
                    for (int i = 1; i < parameters.length; i++) {
                        switch (parameters[i]) {
                            case "concrete":
                                abstractOnly = true;
                                break;
                            case "abstract":
                                abstractOnly = false;
                                break;
                            case "interface":
                                interfaceOnly = true;
                                break;
                            case "public":
                                publicOnly = true;
                                break;
                            case "":// just ignore empty arguments
                                break;
                            default:
                                final var suggestions = Params.suggest(parameters[i], Set.of("concrete", "abstract", "interface", "public"));
                                throw new BadSyntax(String.format("The parameter '%s' is not valid for a method signature. Did you mean %s?",
                                        parameters[i],
                                        suggestions
                                                .stream()
                                                .map(s -> "'" + s + "'")
                                                .collect(Collectors.joining(", "))));
                        }
                    }
                    return MethodTool.with(getObject())
                            .asInterface(interfaceOnly)
                            .asPublic(publicOnly).
                            signature(abstractOnly);
                case "class":
                    assertNoMoreParameters(parameters);
                    return getObject().getDeclaringClass().getName();
                case "name":
                    assertNoMoreParameters(parameters);
                    return getObject().getName();
                case "modifiers":
                    assertNoMoreParameters(parameters);
                    return MethodTool.with(getObject()).modifiers(false);
                case "modifiers-abstract":
                    assertNoMoreParameters(parameters);
                    return MethodTool.with(getObject()).modifiers(true);
                case "exceptions":
                    assertNoMoreParameters(parameters);
                    return MethodTool.with(getObject()).exceptionList();
                case "args":
                    assertNoMoreParameters(parameters);
                    return MethodTool.with(getObject()).argumentList();
                case "call":
                    assertNoMoreParameters(parameters);
                    return MethodTool.with(getObject()).call();
                case "callWith":
                    return getObject().getName() + "(" + Arrays.stream(parameters).skip(1).collect(Collectors.joining(",")) + ")";
                case "type":
                    return getObject().getReturnType().getName();
            }
            return null;
        }

        @Override
        public int expectedNumberOfArguments() {
            return -1;
        }
    }

    private static class FieldHolder extends IdentifiedObjectHolder<Field> implements UserDefinedMacro {
        private FieldHolder(final Processor processor, final Field object, final String id) {
            super(object, id);
            processor.getRegister().global(this);
        }

        @Override
        public String evaluate(final String... parameters) throws BadSyntax {
            if (parameters.length == 0) {
                return getObject().getName();
            }
            switch (parameters[0]) {
                case "class":
                    assertNoMoreParameters(parameters);
                    return getObject().getDeclaringClass().getName();
                case "name":
                    assertNoMoreParameters(parameters);
                    return getObject().getName();
                case "modifiers":
                    assertNoMoreParameters(parameters);
                    return ReflectionTools.modifiersString(getObject());
                case "modifiers-abstract":
                    assertNoMoreParameters(parameters);
                    return ReflectionTools.modifiersStringConcrete(getObject());
                case "type":
                    assertNoMoreParameters(parameters);
                    return ReflectionTools.typeAsString(getObject());
            }
            return null;
        }

        @Override
        public int expectedNumberOfArguments() {
            return -1;
        }
    }

    private static void assertNoMoreParameters(final String[] parameters) throws BadSyntax {
        BadSyntax.when(parameters.length > 1, "'%s' does not take any additional parameter, and you have %s", parameters[0],
                Arrays.stream(parameters).skip(1).collect(Collectors.joining(",")));
    }

    public static class ListMethods implements Macro, Scanner.WholeInput {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in,processor);
            final var storeName = storeNameParam(scanner);
            final var classParam = classParam(scanner);
            final var methodParam = selectorParam(scanner);
            final var sep = separatorParam(scanner);
            scanner.done();
            final var methods = getMethods(processor, storeName.get());
            final var klassFilter = Selector.compile(classParam.get());
            final var methodFilter = Selector.compile(methodParam.get());
            return getLoaded(processor, storeName.get()).stream().filter(klassFilter::match).flatMap(klass ->
                            Arrays.stream(ReflectionTools.getAllMethodsSorted(klass)))
                    .filter(methodFilter::match)
                    .map(method -> {
                        if (methods.containsKey(method)) {
                            return methods.get(method);
                        } else {
                            final var macroName = randomName(method.getName());
                            new MethodHolder(processor, method, macroName);
                            methods.put(method, macroName);
                            return macroName;
                        }
                    }).collect(Collectors.joining(sep.get()));
        }

        @Override
        public String getId() {
            return "java:methods";
        }
    }

    public static class ListFields implements Macro, Scanner.WholeInput {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in,processor);
            final var storeName = storeNameParam(scanner);
            final var classParam = classParam(scanner);
            final var fieldParam = selectorParam(scanner);
            final var sep = separatorParam(scanner);
            scanner.done();
            final var fields = getFields(processor, storeName.get());
            final var klassFilter = Selector.compile(classParam.get());
            final var fieldFilter = Selector.compile(fieldParam.get());
            return getLoaded(processor, storeName.get()).stream().filter(klassFilter::match).flatMap(klass ->
                            Arrays.stream(ReflectionTools.getAllFieldsSorted(klass)))
                    .filter(fieldFilter::match)
                    .map(field -> {
                        if (fields.containsKey(field)) {
                            return fields.get(field);
                        } else {
                            final var macroName = randomName(field.getName());
                            new FieldHolder(processor, field, macroName);
                            fields.put(field, macroName);
                            return macroName;
                        }
                    }).collect(Collectors.joining(sep.get()));
        }

        @Override
        public String getId() {
            return "java:fields";
        }
    }

    private static String randomName(final String hint) {
        return hint + "_" + UUID.randomUUID().toString().replace("-", "");
    }


}
