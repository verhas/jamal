package javax0.jamal.engine;

import javax0.jamal.api.Processor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestScriptEngine {

    @Test
    @DisplayName("The scripting system finds the interpreter")
    public void findsTheInterpreterAmongAllTheInterpreters() {
        final var manager = new ScriptEngineManager();
        final List<ScriptEngineFactory> factories = manager.getEngineFactories();
        boolean wasFound = false;
        for (final ScriptEngineFactory factory : factories) {
            if (Objects.equals(factory.getEngineName(), "jamal")) {
                wasFound = true;
                assertEquals("jamal", factory.getLanguageName());
                assertEquals(Processor.jamalVersionString(), factory.getLanguageVersion());
                assertEquals(1, factory.getNames().size());
                assertEquals(1, factory.getMimeTypes()
                        .size());
                assertEquals(1, factory.getExtensions()
                        .size());
            }
        }
        assertTrue(wasFound);
    }

    @Test
    @DisplayName("Run a simple text without any context a.k.a. predefined user variables")
    void runWithoutAnyContext() throws ScriptException {
        final var manager = new ScriptEngineManager();
        final var se = manager.getEngineByExtension("jam");
        assertNotNull(se);
        assertTrue(se instanceof JamalScriptEngine);
        final var res = se.eval("first script");
        assertEquals("first script", res);
    }

    @Test
    @DisplayName("Run a simple text with context a.k.a. predefined user variables")
    void runWithContext() throws ScriptException {
        final var manager = new ScriptEngineManager();
        final var se = manager.getEngineByExtension("jam");
        final var context = se.getContext();
        final var bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("B", 13);
        // null values will be undefined macros, but they will be propagated back
        bindings.put("A", null);
        final var writer = new StringWriter();
        context.setWriter(writer);
        se.eval("{#define A ={B}}hiha", context);
        final var z = bindings.get("A");
        assertEquals("13", z);
        assertEquals("hiha", writer.toString());
    }

    @Test
    @DisplayName("An undefined output variable will be null")
    void undefineOutput() throws ScriptException {
        final var manager = new ScriptEngineManager();
        final var se = manager.getEngineByExtension("jam");
        final var context = se.getContext();
        final var bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("A", "value");
        final var writer = new StringWriter();
        context.setWriter(writer);
        se.eval("{@undefine A}hiha", context);
        final var a = bindings.get("A");
        assertNull(a);
        assertEquals("hiha", writer.toString());
        final var k = bindings.get("K");
        assertNull(k);
    }

    @Test
    @DisplayName("'open' and 'close' attributes are the macro opening and closing strings")
    void runWithNonDefaultOpenAndClose() throws ScriptException {
        final var manager = new ScriptEngineManager();
        final var se = manager.getEngineByExtension("jam");
        final var context = se.getContext();
        context.setAttribute("open", "<<<", ScriptContext.ENGINE_SCOPE);
        context.setAttribute("close", ">>>", ScriptContext.ENGINE_SCOPE);
        final var bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("A", "value");
        final var writer = new StringWriter();
        context.setWriter(writer);
        se.eval("<<<@define A=muhaha>>>hiha", context);
        final var a = bindings.get("A");
        assertEquals("muhaha", a);
        assertEquals("hiha", writer.toString());
        final var k = bindings.get("K");
        assertNull(k);
    }
}
