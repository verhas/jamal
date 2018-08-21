package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;
import javax0.jamal.tools.ScriptingTools;

import static javax0.jamal.tools.ScriptingTools.*;

public class UserDefinedMacro implements javax0.jamal.api.UserDefinedMacro {
    final private String id;
    final private String[] parameters;
    final private String content;
    private boolean isScript;
    private String scriptType = null;

    public UserDefinedMacro(String id, String content, String... parameters) throws BadSyntax {
        this.isScript = false;
        this.id = id;
        this.content = content;
        this.parameters = parameters;
        ensureSafety();
    }


    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
        isScript = true;
    }

    public UserDefinedMacro newUserDefinedMacro(String id, String content, String... parameters) throws BadSyntax {
        return new UserDefinedMacro(id, content, parameters);
    }

    public String getId() {
        return id;
    }

    private void ensureSafety() throws BadSyntax {
        for (int i = 0; i < parameters.length; i++) {
            for (int j = 0; j < parameters.length; j++) {
                if (i != j) {
                    if (parameters[i].contains(parameters[j])) {
                        throw new BadSyntax("User defined macro parameter name should not be a substring of another parameter.\n" +
                            "\"" + parameters[i] + "\" contains \"" + parameters[j] + "\"");
                    }
                }
            }
        }
    }

    public String evaluate(String... actualValues) throws BadSyntax {
        if (actualValues.length != parameters.length) {
            throw new BadSyntax("Macro '" + id + "' needs " + parameters.length + " arguments and got " + actualValues.length);
        }
        if (isScript) {
            var engine = getEngine(scriptType);
            for (int i = 0; i < parameters.length; i++) {
                populate(engine, parameters[i], actualValues[i]);
            }
            try {
                return resultToString(ScriptingTools.evaluate(engine,content));
            } catch (Exception e) {
                throw new BadSyntax("Script '" + id + "' threw exception", e);
            }
        } else {
            var root = new TextSegment(null, content);
            for (int i = 0; i < actualValues.length; i++) {
                for (Segment segment = root; segment != null; segment = segment.next()) {
                    segment.split(parameters[i], actualValues[i]);
                }
            }
            final var output = new StringBuilder(segmentsLengthSum(root));
            for (Segment segment = root; segment != null; segment = segment.next()) {
                output.append(segment.content());
            }
            return output.toString();
        }
    }

    private int segmentsLengthSum(TextSegment root) {
        int size = 0;
        for (Segment segment = root; segment != null; segment = segment.next()) {
            size += segment.content().length();
        }
        return size;
    }
}
