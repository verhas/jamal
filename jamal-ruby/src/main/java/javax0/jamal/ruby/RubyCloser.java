package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import org.jruby.RubyString;

public class RubyCloser implements Macro, InnerScopeDependent {
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
            shell.property("$result", RubyString.newString(shell.shell.getProvider().getRuntime(), result.getSB()));
            try {
                final var sb = shell.evaluate(closerScript, null);
                if (sb == null) {
                    throw new BadSyntax("Ruby closer script '" + shell.getId() + "' returned null");
                }
                result.getSB().setLength(0);
                result.getSB().append(sb);
            } catch (Exception e) {
                throw new BadSyntax("There was an exception '"
                    + e.getMessage()
                    + "' executing the ruby closer script in the shell '" + shell.getId() + "'.", e);
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

    @Override
    public String getId() {
        return "ruby:closer";
    }
}
