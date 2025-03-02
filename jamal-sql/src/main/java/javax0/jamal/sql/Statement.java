package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.sql.Connection;
import java.sql.SQLException;

@Macro.Name("sql:statement")
public
class Statement implements Macro, Scanner.WholeInput, OptionsControlled {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var connectionName = SqlTools.getConnectionName(scanner);
        final var statementName = SqlTools.getStatementName(scanner);
        scanner.done();
        try {
            final var connectionMacro = processor.getRegister().getUserDefined(connectionName.get());
            BadSyntax.when(connectionMacro.isEmpty(), "Connection with the name '%s' is not defined", connectionName.get());
            BadSyntax.when(!(connectionMacro.get() instanceof ObjectHolder), "Macro '%s' is not a connection,it does not hold object.", connectionName.get());
            final var connection = ((ObjectHolder<?>) connectionMacro.get()).getObject();
            BadSyntax.when(!(connection instanceof Connection), "Macro '%s' is not a connection. It is a '%s'", connectionName.get(), connection.getClass().getName());
            final var statement = ((Connection) connection).createStatement();
            final var statementHolder = new Connect.SqlStatementHolder(statement, statementName.get(), processor);
            processor.define(statementHolder);
        } catch (SQLException e) {
            throw new BadSyntax("Cannot connect to the database using the connection string '" + in.toString() + "'", e);
        }
        return "";
    }

}
