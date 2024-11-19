package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.Scanner;

public class Defer implements Macro, InnerScopeDependent, OptionsControlled.Core, Scanner.Core {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var inputName = scanner.str("$input", "input", "inputName").defaultValue("$input");
        final var outputName = scanner.str("$output", "output", "outputName").defaultValue("$output");
        scanner.done();
        processor.deferredClose(new DeferredCloser(in, inputName.get(), outputName.get()));
        return "";
    }

    private static class DeferredCloser implements AutoCloseable, Closer.ProcessorAware, Closer.OutputAware {
        private Processor processor;
        private Input output;
        private final Input input;

        @Override
        public String toString() {
            return "Defer[" + inputName + "->" + outputName + "]=" + input;
        }

        /**
         * Creates a new closer and stores a copy of the input. It does not store the original input since that is not
         * immutable, and it may not be the same when the deferred input is evaluated.
         *
         * @param input      of the deferred macro. Note that this is not the result of the processing. The input of the
         *                   processing, a.k.a. the result of the Jamal execution is available in the macro {@code $input}
         *                   macro.
         * @param inputName the name of the macro containing the input to the deferred macro.
         * @param outputName the name of the macro where the result of the deferred macro will be stored.
         */
        private DeferredCloser(Input input, String inputName, String outputName) {
            // store a copy of the input of the macro
            this.input = javax0.jamal.tools.Input.makeInput(input.toString(), input.getPosition());
            this.inputName = inputName;
            this.outputName = outputName;
        }

        @Override
        public void set(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }

        private final String inputName;// = "$input";
        private final String outputName;// = "$output";

        @Override
        public void close() throws Exception {
            final String out = output.toString();
            processor.defineGlobal(processor.newUserDefinedMacro(inputName, out, true));
            processor.defineGlobal(new Identified.Undefined(outputName));
            processor.process(input);
            if (!processor.errors().isEmpty()) {
                processor.throwUp();
            }
            final var reader = MacroReader.macro(processor);
            final String result = reader.readValue(outputName).orElse(out);
            final StringBuilder sb = output.getSB();
            sb.setLength(0);
            sb.append(result);
        }
    }
}
/*template jm_defer
{template |defer|defer [$O$] $C|defer evaluation of content after the document was processed|
  {variable |O|"$input $output"}
  {variable |C|"..."}
}
 */
