package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.Scanner;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect implements Macro, Scanner, OptionsControlled {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var connectionName = SqlTools.getConnectionName(scanner);
        final var statementName = SqlTools.getStatementName(scanner);
        final var driverClass = scanner.str(null, "driver").optional();
        scanner.done();
        final var connectionString = in.toString().trim();
        try {
            if (driverClass.isPresent()) {
                Class.forName(driverClass.get().trim());
            } else {
                loadClassFromConnectionStringUsingJdbcMacro(processor, connectionString);
            }
            final var connection = DriverManager.getConnection(connectionString);
            final var connectionHolder = new SqlConnectionHolder(connection, connectionName.get());
            processor.define(connectionHolder);
            final var statement = connection.createStatement();
            final var statementHolder = new SqlStatementHolder(statement, statementName.get());
            processor.define(statementHolder);
        } catch (SQLException e) {
            throw new BadSyntax("Cannot connect to the database using the connection string '" + in.toString() + "'", e);
        } catch (ClassNotFoundException e) {
            throw new BadSyntax("Cannot load the SQL driver class'" + in.toString() + "'", e);
        }
        return "";
    }

    /**
     * Load the driver class using the connection string. The connection string is expected to be in the format
     * {@code jdbc:driver:...}
     * <p>
     * The class for the given driver can be defined in a macro.
     * The name of the macro is {@code jdbc:driver} where the driver is the name of the driver in the connection string.
     * The value of the macro is the name of the class that is the driver.
     * <p>
     * The most common driver class names are defined in the {@code sql.jim} import file.
     *
     * @param processor        the processor to use to read the macro
     * @param connectionString the connection string
     * @throws BadSyntax              if the macro cannot be read, typically it needs argument, it either can be undefined or be without arguments
     * @throws ClassNotFoundException if the class cannot be loaded. Note that the class has to be on the classpath.
     */
    private static void loadClassFromConnectionStringUsingJdbcMacro(Processor processor, String connectionString) throws BadSyntax, ClassNotFoundException {
        final var parts = connectionString.split(":");
        if (parts.length > 1) {
            final var driver = MacroReader.macro(processor).readValue("jdbc:" + parts[1]);
            if (driver.isPresent()) {
                Class.forName(driver.get().trim());
            }
        }
    }

    @Override
    public String getId() {
        return "sql:connect";
    }

    static class SqlConnectionHolder extends IdentifiedObjectHolder<java.sql.Connection> implements AutoCloseable {

        SqlConnectionHolder(java.sql.Connection connection, String name) {
            super(connection, name);
        }


        @Override
        public void close() throws Exception {
            getObject().close();
        }
    }

    static class SqlStatementHolder extends IdentifiedObjectHolder<java.sql.Statement> implements AutoCloseable {

        SqlStatementHolder(java.sql.Statement statement, String name) {
            super(statement, name);
        }


        @Override
        public void close() throws Exception {
            getObject().close();
        }
    }

}
