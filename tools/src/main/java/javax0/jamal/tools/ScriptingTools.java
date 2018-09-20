package javax0.jamal.tools;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Utility class containing static methods handling scripting. Scripting is handled through the JSR223 defined interface
 * and no interpreter specific API is used, thus the scripts can be written in any scripting implementation that is
 * on the classpath runtime.
 */
public class ScriptingTools {


    /**
     * Populate the scripting engine with the key/value pair. This means that the global variable that has the name
     * specified in the parameter {@code key} will have the value specified in the parameter {@code value}.
     * <p>
     * If the string given in the parameter {@code value} can be interpreted as an integer then the population converts
     * the value to {@code Integer}. If the string can be interpreted as a floating point number then it will
     * be converted to a {@code Double}. If the value if {@code "true"} (any case, even mixed case) then the value will
     * be {@code Boolean} {@code true}. Likewise if the value is {@code "false"} then it is converted to {@code false}.
     * <p>
     * In any other cases the string is stored and assigned to the global variable.
     * @param engine that stores the global variables for later execution
     * @param key the name of the global variable
     * @param value the value to be assigned to the global variable
     */
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

    /**
     * Return the string representation of the object. If the object is a {@code Double} then the trailing {@code .0}
     * is chopped off. This is to help the macro evaluation and use in case the scripting implementation returns a
     * {@code Double} even for integer values.
     *
     * @param obj to convert to string
     * @return the converted string
     */
    public static String resultToString(Object obj) {
        if( obj instanceof Double){
            return obj.toString().replaceAll(".0$","");
        }
        return obj.toString();
    }

    /**
     * Get the scripting engine based on the name of the script type e.g.: {@code JavaScript}.
     *
     * @param scriptType the name of the scripting language
     * @return the engine for the specified script type
     */
    public static ScriptEngine getEngine(String scriptType){
        return new ScriptEngineManager().getEngineByName(scriptType);
    }

    /**
     * Evaluate the content using the scripting engine.
     *
     * @param engine to be used to execute the evaluation
     * @param content the program code to be evaluated
     * @return the result of the evaluation
     * @throws ScriptException when there is an error executing the script
     */
    public static Object evaluate(ScriptEngine engine, String content) throws ScriptException {
        return engine.eval(content);
    }

}
