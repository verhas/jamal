package javax0.jamal.tools;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptingTools {

    public static void populate(ScriptEngine engine, String key, String value) {
        try {
            var intval = Integer.parseInt(value);
            engine.put(key, intval);
            return;
        } catch (NumberFormatException ignored) {
        }
        try {
            var doubleval = Double.parseDouble(value);
            engine.put(key, doubleval);
            return;
        } catch (NumberFormatException ignored) {
        }
        if ("true".equalsIgnoreCase(value)) {
            engine.put(key, true);
            return;
        }
        if ("false".equalsIgnoreCase(value)) {
            engine.put(key, false);
            return;
        }

        engine.put(key, value);
    }

    public static String resultToString(Object obj) {
        if( obj instanceof Double){
            return obj.toString().replaceAll(".0$","");
        }
        return obj.toString();
    }

    public static ScriptEngine getEngine(String scriptType){
        return new ScriptEngineManager().getEngineByName(scriptType);
    }

    public static Object evaluate(ScriptEngine engine, String content) throws ScriptException {
        return engine.eval(content);
    }

}
