package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Scanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Macro.Name("sql:connect")
public
class Connect implements Macro, Scanner, OptionsControlled {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var connectionName = SqlTools.getConnectionName(scanner);
        final var statementName = SqlTools.getStatementName(scanner);
        scanner.done();
        final var url = in.toString().trim();
        try {
            closeOldConnection(processor, connectionName.get());
            final var connection = DriverManager.getConnection(url);
            final var connectionHolder = new SqlConnectionHolder(connection, connectionName.get(), processor);
            processor.define(connectionHolder);
            final var statement = connection.createStatement();
            final var statementHolder = new SqlStatementHolder(statement, statementName.get(), processor);
            processor.define(statementHolder);
        } catch (SQLException e) {
            throw new BadSyntax("Cannot connect to the database using the connection string '" + url + "'", e);
        }
        return "";
    }

    private static void closeOldConnection(Processor processor, String s) throws BadSyntax {
        final var connectionMacro = processor.getRegister().getUdMacroLocal(s);
        if (connectionMacro.isPresent() && connectionMacro.get() instanceof ObjectHolder) {
            final var connection = ((ObjectHolder<?>) connectionMacro.get()).getObject();
            if (connection instanceof Connection) {
                try {
                    ((Connection) connection).close();
                } catch (SQLException e) {
                    throw new BadSyntax("Cannot close the old connection", e);
                }
            }
        }

    }

    static class SqlConnectionHolder extends IdentifiedObjectHolder<Connection> implements AutoCloseable {

        SqlConnectionHolder(final Connection connection, final String name, final Processor processor) {
            super(connection, name);
            processor.deferredClose(this);
        }


        @Override
        public void close() throws Exception {
            getObject().close();
        }
    }

    static class SqlStatementHolder extends IdentifiedObjectHolder<Statement> implements AutoCloseable {

        SqlStatementHolder(final Statement statement, final String name, final Processor processor) {
            super(statement, name);
            processor.deferredClose(this);
        }


        @Override
        public void close() throws Exception {
            getObject().close();
        }
    }

}
