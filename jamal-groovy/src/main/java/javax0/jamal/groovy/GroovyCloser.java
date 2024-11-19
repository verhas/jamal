package javax0.jamal.groovy;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

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
            shell.property("result", result.getSB());
            try {
                final var sb = shell.evaluate(closerScript, null);
                if (sb != null && sb != result.getSB()) {// NOT EQUALS, does it return the same object or not
                    result.getSB().setLength(0);
                    result.getSB().append(sb);
                }
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

    @Override
    public String getId() {
        return "groovy:closer";
    }
}
