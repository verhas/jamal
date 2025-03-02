package javax0.jamal.groovy;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.util.Objects;

@Macro.Name("groovy:closer")
public class GroovyCloser implements Macro, InnerScopeDependent, Scanner {
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
            final var resultSB = new StringBuilder(result);
            shell.property("result", resultSB);
            try {
                final var sb = shell.evaluate(closerScript, null);
                result.replace(Objects.requireNonNullElse(sb, resultSB));
            } catch (Exception e) {
                throw new BadSyntax("There was an exception '"
                    + e.getMessage()
                    + "' executing the groovy closer script in the shell '" + shell.getId() + "'.", e);
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
