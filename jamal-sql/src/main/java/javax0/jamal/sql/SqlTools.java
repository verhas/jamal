package javax0.jamal.sql;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;

public class SqlTools {
    static StringParameter getResultSetName(Scanner.ScannerObject scanner) {
        return scanner.str("resultSetName", "resultSet", "rs", "result").defaultValue("sql$result");
    }

    static StringParameter getStatementName(Scanner.ScannerObject scanner) {
        return scanner.str(null, "stmt", "statement").defaultValue("sql$statement");
    }

    static StringParameter getConnectionName(Scanner.ScannerObject scanner) {
        return scanner.str(null, "con", "connection").defaultValue("sql$connection");
    }

    /**
     * Check if the SQL query is a SELECT query, and it is safe to execute.
     *
     * @param query the SQL query to check without the 'SELECT' keyword at the beginning
     * @throws BadSyntax if the query is not safe to execute
     */
    static void assertSqlSelectSafe(final String query) throws BadSyntax {
        final CCJSqlParser parser;
        try {
            parser = new CCJSqlParser("SELECT " + query);
            final var statements = parser.Statements();
            if (statements.size() != 1)
                throw new IllegalArgumentException("SQL select query '" + query + "' seems to be dangerous.");
        } catch (ParseException e) {
            throw new BadSyntax("SQL select query '" + query + "' is not safe and erroneous.", e);
        }
    }

}
