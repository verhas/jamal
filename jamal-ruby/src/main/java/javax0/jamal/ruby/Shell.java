package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.io.StringReader;
import java.io.StringWriter;

public class Shell implements Identified {
    private final String id;
    final ScriptingContainer shell;

    public void property(String key, Object value) {
        shell.put(key, value);
    }

    public Object property(String key) {
        return shell.get(key);
    }

    public Shell(String id) {
        this.id = id;
        this.shell = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        //noinspection NullableProblems
        shell.setError(new StringWriter() {
            public void write(char[] cbuf, int off, int len) {
            }
        });
    }

    public Object evaluate(String script, String fileName) {
        if (fileName != null && fileName.length() > 0) {
            return shell.runScriptlet(new StringReader(script), fileName);
        } else {
            return shell.runScriptlet(new StringReader(script), id + ".rb");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    // snippet defaultShellName
    public static final String DEFAULT_RUBY_SHELL_NAME = ":ruby_shell";
    // end snippet
    // snippet shellNamingMacro
    public static final String RUBY_SHELL_NAMING_MACRO = "rubyShell";

    // end snippet
    public static Shell getShell(final Input in, final Processor processor) throws BadSyntax {
        final var id = Params.<String>holder(Shell.RUBY_SHELL_NAMING_MACRO, "shell").orElse(Shell.DEFAULT_RUBY_SHELL_NAME);
        Params.using(processor).keys(id).between("()").parse(in);
        return getShell(processor, id.get());
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
            return shell;
        } else {
            return (Shell) opt.get();
        }
    }
}
