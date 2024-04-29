package javax0.jamal.sql;

import javax0.jamal.api.*;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Scanner;

import java.sql.ResultSet;
import java.sql.Statement;

public class Select implements Macro, Scanner, OptionsControlled {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var resultName = scanner.str(null, "rs", "resultSet", "result").defaultValue("sql$result");
        final var statementName = scanner.str(null, "statement").defaultValue("sql$statement");
        scanner.done();
        final var statementMacro = processor.getRegister().getUserDefined(statementName.get());
        BadSyntax.when(statementMacro.isEmpty(), "Statement is not prepared to execute select");
        BadSyntax.when(!(statementMacro.get() instanceof ObjectHolder), "Statement macro '%s' is not a statement,it does not hold object.", statementName.get());
        final var statement = ((ObjectHolder<?>) statementMacro.get()).getObject();
        BadSyntax.when(!(statement instanceof Statement), "Statement macro '%s' is not a statement. It is an '%s'.", statementName.get(), statement.getClass().getName());
        final var sql = in.toString().trim();
        try {
            //TODO make it safe
            final var resultSet = ((Statement) statement).executeQuery("SELECT " + sql);
            final var resultSetHolder = new ResultSetHolder(resultSet, resultName.get());
            processor.define(resultSetHolder);
        } catch (Exception e) {
            throw new BadSyntax("Cannot execute 'select " + sql + "'", e);
        }
        return "";
    }

    @Override
    public String getId() {
        return "sql:select";
    }

    static class ResultSetHolder extends IdentifiedObjectHolder<ResultSet> implements UserDefinedMacro, AutoCloseable {

        ResultSetHolder(java.sql.ResultSet resultSet, String name) {
            super(resultSet, name);
        }

        @Override
        public void close() throws Exception {
            getObject().close();
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            BadSyntax.when(parameters.length < 1, "The column name or number is missing");
            var columnName = parameters[0].trim();
            final String type = parameters.length > 1 ? parameters[1].trim() : "string";
            final var resultSet = getObject();
            try {
                if (columnName.equals("next")) {
                    resultSet.next();
                    return "";
                }
                if (columnName.equals("close")) {
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
                        BadSyntax.when(true, "Unknown type '%s'", type);
                }
            } catch (Exception e) {
                BadSyntax.when(true, "Cannot read column '%s' as '%s'", columnName, type);
            }
            return "";
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
