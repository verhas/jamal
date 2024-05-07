package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Scanner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Select implements Macro, Scanner, OptionsControlled {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var resultName = SqlTools.getResultSetName(scanner);
        final var statementName = SqlTools.getStatementName(scanner);
        scanner.done();
        final var statementMacro = processor.getRegister().getUserDefined(statementName.get());
        BadSyntax.when(statementMacro.isEmpty(), "Statement is not prepared to execute select");
        BadSyntax.when(!(statementMacro.get() instanceof ObjectHolder), "Statement macro '%s' is not a statement,it does not hold object.", statementName.get());
        final var statement = ((ObjectHolder<?>) statementMacro.get()).getObject();
        BadSyntax.when(!(statement instanceof Statement), "Statement macro '%s' is not a statement. It is an '%s'.", statementName.get(), statement.getClass().getName());
        final var sql = in.toString().trim();
        SqlTools.assertSqlSelectSafe(sql);
        try {
            final var resultSet = ((Statement) statement).executeQuery("SELECT " + sql);
            final var resultSetHolder = new ResultSetHolder(resultSet, resultName.get(), processor);
            processor.define(resultSetHolder);
        } catch (SQLException e) {
            throw new BadSyntax("Cannot execute 'select " + sql + "'", e);
        }
        return "";
    }

    @Override
    public String getId() {
        return "sql:select";
    }

    static class ResultSetHolder extends IdentifiedObjectHolder<ResultSet> implements UserDefinedMacro, AutoCloseable {

        ResultSetHolder(final java.sql.ResultSet resultSet, final String name, final Processor processor) {
            super(resultSet, name);
            processor.deferredClose(this);
        }

        @Override
        public void close() throws Exception {
            getObject().close();
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            BadSyntax.when(parameters.length < 1, "The column name or number is missing");
            var params = parameters[0].trim().split("\\s");
            var columnName = params[0];
            final String type = (parameters.length > 2 && "as".equals(parameters[1]))
                    ? parameters[2]
                    : (parameters.length > 1 ? parameters[1] : "string");

            final var resultSet = getObject();
            try {
                if (columnName.equals("next")) {
                    BadSyntax.when( params.length > 1, "Result set 'next' must ot have any parameter");
                    resultSet.next();
                    return "";
                }
                if (columnName.equals("close")) {
                    BadSyntax.when( params.length > 1, "Result set 'close' must ot have any parameter");
                    resultSet.close();
                    return "";
                }
                int columnNumber = 0;
                if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                } else if (columnName.matches("\\d+")) {
                    columnNumber = Integer.parseInt(columnName);
                    columnName = "";
                }
                switch (type) {
                    case "string":
                        return columnName.isEmpty() ? resultSet.getString(columnNumber) : resultSet.getString(columnName);
                    case "int":
                        return columnName.isEmpty() ? Integer.toString(resultSet.getInt(columnNumber)) : Integer.toString(resultSet.getInt(columnName));
                    case "long":
                        return columnName.isEmpty() ? Long.toString(resultSet.getLong(columnNumber)) : Long.toString(resultSet.getLong(columnName));
                    case "double":
                        return columnName.isEmpty() ? Double.toString(resultSet.getDouble(columnNumber)) : Double.toString(resultSet.getDouble(columnName));
                    case "float":
                        return columnName.isEmpty() ? Float.toString(resultSet.getFloat(columnNumber)) : Float.toString(resultSet.getFloat(columnName));
                    case "boolean":
                        return columnName.isEmpty() ? Boolean.toString(resultSet.getBoolean(columnNumber)) : Boolean.toString(resultSet.getBoolean(columnName));
                    case "date":
                        return columnName.isEmpty() ? resultSet.getDate(columnNumber).toString() : resultSet.getDate(columnName).toString();
                    case "time":
                        return columnName.isEmpty() ? resultSet.getTime(columnNumber).toString() : resultSet.getTime(columnName).toString();
                    case "timestamp":
                        return columnName.isEmpty() ? resultSet.getTimestamp(columnNumber).toString() : resultSet.getTimestamp(columnName).toString();
                    default:
                        throw BadSyntax.format( "Unknown type '%s'", type);
                }
            } catch (Exception e) {
                throw BadSyntax.format( "Cannot read column '%s' as '%s'", columnName, type);
            }
        }

        @Override
        public boolean isVerbatim() {
            return true;
        }

        @Override
        public int expectedNumberOfArguments() {
            return 1;
        }
    }
}
