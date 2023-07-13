package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;

@Deprecated(since = "2.4.0")
public class Scan {

    public static ScanInterfaces.AfterUsing using(final Processor processor) {
        return new Builder(Params.using(processor));
    }

    private static class Builder implements ScanInterfaces.AfterUsing, ScanInterfaces.AfterFrom, ScanInterfaces.AfterStartWith, ScanInterfaces.DelimitersDefined, ScanInterfaces.AfterKeys {
        private final Params params;

        private Builder(final Params params) {
            this.params = params;
        }

        public Builder from(Identified macro) {
            params.from(macro);
            return this;
        }

        public Builder startWith(char start) {
            params.startWith(start);
            return this;
        }

        public Builder endWith(char terminal) {
            params.endWith(terminal);
            return this;
        }

        public Builder between(String seps) {
            params.between(seps);
            return this;
        }

        public Builder tillEnd() {
            params.tillEnd();
            return this;
        }

        public Builder firstLine() {
            return this;
        }

        public ScanInterfaces.AfterKeys keys(Params.ExtraParams extraParams, Params.Param<?>... holders) {
            params.keys(extraParams, holders);
            return this;
        }

        public ScanInterfaces.AfterKeys keys(Params.Param<?>... holders) {
            params.keys(holders);
            return this;
        }

        public void parse(Input input) throws BadSyntax {
            params.parse(input);
        }

    }


}