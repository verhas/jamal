package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An actual extension of the {@link AbstractScriptEngine} that can be used to execute Jamal as a script engine.
 * <p>
 * This class is not used inside Jamal, it is provided as a public API.
 */
public class JamalScriptEngine extends AbstractScriptEngine {

    final ScriptEngineFactory factory;

    public JamalScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    private static void pushVariables(final Processor processor,
                                      final Bindings bindings) {
        bindings.replaceAll((n, v) -> processor.getRegister()
                .getUserDefined(n)
                .filter(udm -> udm instanceof UserDefinedMacro)
                .map(udm -> (UserDefinedMacro) udm)
                .map(UserDefinedMacro::getContent)
                .orElse(null));
    }

    final static String[] NO_PARAMS = new String[0];

    private static void pullVariables(final javax0.jamal.engine.Processor processor,
                                      final Bindings bindings) throws BadSyntax {
        for (final String name : bindings.keySet()) {
            final var value = bindings.get(name);
            if (value != null) {
                final var m = processor.newUserDefinedMacro(name, Objects.toString(value), NO_PARAMS);
                processor.getRegister().define(m);
            }
        }
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try (final var processor = new javax0.jamal.engine.Processor()) {
            final var open = Objects.toString(Objects.requireNonNullElse(context.getAttribute("open"), "{"));
            final var close = Objects.toString(Objects.requireNonNullElse(context.getAttribute("close"), "}"));
            processor.separators(open, close);
            pullVariables(processor, context.getBindings(ScriptContext.ENGINE_SCOPE));
            pullVariables(processor, context.getBindings(ScriptContext.GLOBAL_SCOPE));
            final var result = processor.process(Input.makeInput(script));
            context.getWriter().write(result);
            pushVariables(processor, context.getBindings(ScriptContext.GLOBAL_SCOPE));
            pushVariables(processor, context.getBindings(ScriptContext.ENGINE_SCOPE));
            return result;
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(new BufferedReader(reader).lines().collect(Collectors.joining("\n")), context);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
