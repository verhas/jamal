package javax0.jamal.groovy;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.Params;

public class GroovyShell implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var id = MacroReader.macro(processor).readValue(Shell.GROOVY_SHELL_NAMING_MACRO).orElse(Shell.DEFAULT_GROOVY_SHELL_NAME);
        final var inputDefinedScriptName = InputHandler.fetch2EOL(in).trim();
        final var scriptName = inputDefinedScriptName.length() == 0 ? id + ".groovy" : inputDefinedScriptName;
        final var shell = Shell.getShell(processor);
        try {
            return "" + shell.evaluate(in.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the groovy script '" + scriptName + "'.", e);
        }
    }

    @Override
    public String getId() {
        return "groovy:shell";
    }
}
