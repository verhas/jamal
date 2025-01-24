package javax0.jamal.engine;

import javax0.jamal.api.Processor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of the {@link ScriptEngineFactory} that can be used to register Jamal as a scripting engine.
 * The implementation follows the JSR-223 specification.
 */
public class JamalEngineFactory implements ScriptEngineFactory {
    @Override
    public String getEngineName() {
        return "jamal";
    }

    @Override
    public String getEngineVersion() {
        return Processor.jamalVersionString();
    }

    @Override
    public List<String> getExtensions() {
        return List.of("jam");
    }

    @Override
    public List<String> getMimeTypes() {
        return List.of("application/x-jamal");
    }

    @Override
    public List<String> getNames() {
        return List.of("jamal");
    }

    @Override
    public String getLanguageName() {
        return "jamal";
    }

    @Override
    public String getLanguageVersion() {
        return Processor.jamalVersionString();
    }

    @Override
    public Object getParameter(final String key) {
        switch (key) {
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.NAME:
                return getNames();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            case "THREADING":
                return "STATELESS";
        }
        return null;
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String m, final String... args) {
        return String.format("{%s %s}", m, Arrays.stream(args).map(s -> "|" + s).collect(Collectors.joining("")));
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return String.format("{@log %s}", toDisplay);
    }

    @Override
    public String getProgram(String... statements) {
        return String.join("",statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new JamalScriptEngine(this);
    }
}
