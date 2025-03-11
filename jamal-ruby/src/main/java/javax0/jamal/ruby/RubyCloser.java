package javax0.jamal.ruby;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import org.jruby.RubyString;

@Macro.Name("ruby:closer")
@Macro.Sentinel("ruby")
public class RubyCloser implements Macro, InnerScopeDependent, Scanner {
    private static class Closer implements AutoCloseable, javax0.jamal.api.Closer.OutputAware {
        private Input result;
        private final Shell shell;
        private final String closerScript;

        private Closer(Shell shell, String closerScript) {
            this.shell = shell;
            this.closerScript = closerScript;
        }

        @Override
        public void close() throws Exception {
            shell.property("$result", RubyString.newString(shell.shell.getProvider().getRuntime(), result));
            try {
                final var sb = shell.evaluate(closerScript, null);
                BadSyntax.when(sb == null, "Ruby closer script '%s' returned null", shell.getId());
                result.replace(sb);
            } catch (Exception e) {
                throw new BadSyntax(String.format("There was an exception '%s' executing the ruby closer script in the shell '%s'.",
                        e.getMessage(), shell.getId()), e);
            }
        }

        @Override
        public void set(Input result) {
            this.result = result;
        }
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var shell = Shell.getShell(in, processor, this);
        final var closer = new Closer(shell, in.toString());
        processor.deferredClose(closer);
        return "";
    }

}
