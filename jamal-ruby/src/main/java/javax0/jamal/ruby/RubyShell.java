package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.ruby.Shell;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;

public class RubyShell implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var id = MacroReader.macro(processor).readValue(Shell.RUBY_SHELL_NAMING_MACRO).orElse(Shell.DEFAULT_RUBY_SHELL_NAME);
        final var inputDefinedScriptName = InputHandler.fetch2EOL(in).trim();
        final var scriptName = inputDefinedScriptName.length() == 0 ? id + ".rb" : inputDefinedScriptName;
        final var shell = Shell.getShell(processor);
        try {
            return "" + shell.evaluate(in.toString(), scriptName);
        } catch (Exception e) {
            throw new BadSyntax("There was an exception '" + e.getMessage() + "' executing the ruby script '" + scriptName + "'.", e);
        }
    }

    @Override
    public String getId() {
        return "ruby:shell";
    }
}
