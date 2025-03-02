package javax0.jamal.java;

import com.javax0.sourcebuddy.Compiler;
import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This macro will convert the operation to a Java DSL interpretation.
 * The rest of the input after the macro is treated as Java code inside an {@code evaluate()} method returning a string.
 */
@Macro.Name("java:dsl")
public
class Jdsl implements Macro, Scanner.WholeInput {


    private static class State implements AutoCloseable, Closer.OutputAware {
        private String source;
        private String result;
        private Input output;

        @Override
        public void close() throws Exception {
            output.replace(result);
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }
    }

    @Override
    public String fetch(Processor processor, Input input) throws BadSyntaxAt {
        final var original = Macro.super.fetch(processor, input);

        processor.state(this, State::new).source = input.toString();
        input.reset();
        return original;
    }

    public String prefetch(Processor processor, Input input) throws BadSyntaxAt {
        return Macro.super.fetch(processor, input) + processor.getRegister().close();
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var className = scanner.str("class").defaultValue("Jdsl" + System.nanoTime());
        final var packageName = scanner.str("package").defaultValue("javax0.jamal.java");
        final var extendsClass = scanner.str("extends").optional();
        final var implementsInterfaces = scanner.str("implements").optional();
        final var methodName = scanner.str("method").defaultValue("evaluate");
        final var imports = scanner.str("import", "imports").optional();
        final var precode = scanner.str("precode").optional();
        final var postcode = scanner.str("postcode").optional();
        final var requires = scanner.str("requires").optional();
        scanner.done();

        final var source = new StringBuilder();
        source.append("package ").append(packageName.get()).append(";\n");
        source.append(join(imports, q -> q.map(s -> "import " + s + ";\n")));
        source.append("public class ").append(className.get()).append(" ");
        if (extendsClass.isPresent()) {
            source.append("extends ").append(extendsClass.get()).append(" ");
        }
        if (implementsInterfaces.isPresent()) {
            source.append("implements ").append(implementsInterfaces.get()).append(" ");
        }
        source.append("{\n");
        source.append("    public static String ").append(methodName.get()).append("() throws Exception {\n");
        if (precode.isPresent()) {
            source.append(precode.get());
        }
        final var state = processor.state(this, State::new);
        source.append(state.source);
        if (postcode.isPresent()) {
            source.append(postcode.get());
        }
        source.append("    }\n");
        source.append("}\n");
        try {
            final var compiler = Compiler.java();
            compiler.options("-cp", System.getProperty("java.class.path"));
            split(requires, compiler::modules);
            final var klass = compiler.from(source.toString()).compile("").load().get();
            state.result = (String) klass.getDeclaredMethod(methodName.get()).invoke(null);
            processor.deferredClose(state);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception compiling, executing Java code.", e);
        }
        return "";
    }

    private static String join(StringParameter parameter, Function<Stream<String>, Stream<String>> translate) throws BadSyntax {
        if (parameter.isPresent()) {
            return translate.apply(Arrays.stream(parameter.get().split("[,\n]"))
                    .filter(s -> !s.isBlank())
                    .map(String::trim)).reduce("", String::concat);
        } else {
            return "";
        }
    }

    private static void split(StringParameter parameter, Consumer<String> consume) throws BadSyntax {
        if (parameter.isPresent()) {
            Arrays.stream(parameter.get().split("[,\n]"))
                    .filter(s -> !s.isBlank())
                    .map(String::trim).forEach(consume);
        }
    }

}
