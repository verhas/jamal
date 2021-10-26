package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;

public class Unescape implements Macro {

    static final String UNESCAPE_OPTION = "799f665ceaa5aab34161793aefb4be17";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        if (in.toString().trim().length() == 0) {
            processor.deferredClose(new UnescapeCloser());
            return "";
        } else {
            OptionsStore.getInstance(processor).addOptions(UNESCAPE_OPTION);
            final String result = processor.process(in);
            OptionsStore.getInstance(processor).addOptions("~" + UNESCAPE_OPTION);
            return result;
        }
    }

    static class UnescapeCloser implements Closer.OutputAware, Closer.ProcessorAware, AutoCloseable {
        private Processor processor;
        private Input output;
        private boolean iAmAlreadyClosing = false;

        @Override
        public boolean equals(Object o) {
            return UnescapeCloser.class == o.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public void set(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void set(Input output) {
            if (!iAmAlreadyClosing) {
                this.output = output;
            }
        }


        @Override
        public void close() throws Exception {
            if (iAmAlreadyClosing) {
                return;
            }
            iAmAlreadyClosing = true;
            OptionsStore.getInstance(processor).addOptions(UNESCAPE_OPTION);
            final String result = processor.process(output);
            OptionsStore.getInstance(processor).addOptions("~" + UNESCAPE_OPTION);
            output.getSB().setLength(0);
            output.append(result);
        }
    }
}
