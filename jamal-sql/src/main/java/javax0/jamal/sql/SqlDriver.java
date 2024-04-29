package javax0.jamal.sql;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class SqlDriver implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        try {
            skipWhiteSpaces(in);
            Class.forName(in.toString().trim());
        } catch (ClassNotFoundException e) {
            throw new BadSyntax("Cannot load the SQL driver class'" + in.toString() + "'", e);
        }
        return "";
    }

    @Override
    public String getId() {
        return "sql:driver";
    }
}
