package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.sql.SQLException;

public class Loop implements Macro, Scanner, OptionsControlled {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var resultName = SqlTools.getResultSetName(scanner);
        scanner.done();
        final var resultMacro = processor.getRegister().getUserDefined(resultName.get());
        BadSyntax.when(resultMacro.isEmpty() || !(resultMacro.get() instanceof Select.ResultSetHolder), "Result set is not defined or not a result set");
        final var resultSet = ((Select.ResultSetHolder) resultMacro.get()).getObject();
        final var output = new StringBuilder();
        try {
            while (resultSet.next()) {
                output.append(processor.process(in.toString()));
            }
        } catch (SQLException e) {
            throw new BadSyntax("Error while looping through the result set", e);
        }

        return output.toString();
    }

    @Override
    public String getId() {
        return "sql:loop";
    }
}
