package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;

public class ScanInterfaces {
    public interface AfterUsing {
        AfterFrom from(Identified macro);
    }

    public interface AfterFrom {
        AfterStartWith startWith(char start);

        DelimitersDefined endWith(char terminal);

        DelimitersDefined between(String seps);

        DelimitersDefined tillEnd();

        DelimitersDefined firstLine();
    }

    public interface AfterStartWith {
        DelimitersDefined endWith(char terminal);

    }

    public interface DelimitersDefined {
        AfterKeys keys(Params.Param<?>... holders);
    }

    public interface AfterKeys {
        void parse(Input input) throws BadSyntax;

    }
}
