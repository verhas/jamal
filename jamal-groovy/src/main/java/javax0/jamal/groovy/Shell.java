package javax0.jamal.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;

public class Shell implements Identified {
    private final String id;
    final Binding binding = new Binding();
    final GroovyShell shell = new GroovyShell(binding);

    public void property(String key, Object value) {
        binding.setProperty(key, value);
    }

    public Object property(String key) {
        return binding.getProperty(key);
    }

    public void variable(String key, Object value) {
        binding.setVariable(key, value);
    }

    public Object variable(String key) {
        return binding.getVariable(key);
    }

    public Shell(String id) {
        this.id = id;
    }

    public Object evaluate(String script, String fileName) {
        if (fileName != null && fileName.length() > 0) {
            return shell.evaluate(script, fileName);
        } else {
            return shell.evaluate(script, id + ".groovy");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    // snippet defaultShellName
    public static final String DEFAULT_GROOVY_SHELL_NAME = ":groovyShell";
    // end snippet
    // snippet shellNamingMacro
    public static final String GROOVY_SHELL_NAMING_MACRO = "groovyShell";
    // end snippet
    public static Shell getShell(final Processor processor) throws BadSyntax {
        final var id = MacroReader.macro(processor).readValue(GROOVY_SHELL_NAMING_MACRO).orElse(DEFAULT_GROOVY_SHELL_NAME);
        return getShell(processor, id);
    }

    public static Shell getShell(final Processor processor, final String id) throws BadSyntax {
        final var opt = processor.getRegister().getUserDefined(id)
            .filter(s -> s instanceof Shell);
        final Shell shell;
        if (opt.isEmpty()) {
            final var gid = InputHandler.convertGlobal(id);
            shell = new Shell(gid);
            if (InputHandler.isGlobalMacro(id)) {
                processor.defineGlobal(shell);
            } else {
                processor.define(shell);
                processor.getRegister().export(shell.getId());
            }

        } else {
            shell = (Shell) opt.get();
        }
        return shell;
    }
}
