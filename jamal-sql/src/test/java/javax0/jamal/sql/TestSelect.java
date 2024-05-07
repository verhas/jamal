package javax0.jamal.sql;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.DriverManager;

public class TestSelect {

    @Test
    void testSqlSelect() throws Exception {
        final var root = DocumentConverter.getRoot();
        String url = "jdbc:h2:" + root + "/target/testdb";
        Class.forName("org.h2.Driver");

        try (final var connection = DriverManager.getConnection(url)) {
            try (final var statement = connection.createStatement()) {
                String createTableSQL = "DROP TABLE IF EXISTS Employees; CREATE TABLE Employees (" +
                        "id INT PRIMARY KEY," +
                        "name VARCHAR(255)," +
                        "position VARCHAR(255))";
                statement.execute(createTableSQL);

                String insertSQL1 = "INSERT INTO Employees VALUES (1, 'Alice', 'Manager')";
                String insertSQL2 = "INSERT INTO Employees VALUES (2, 'Bob', 'Developer')";
                statement.execute(insertSQL1);
                statement.execute(insertSQL2);
            }
        }
        TestThat.theInput("" +
                        "{@option failfast}" +
                        "{@sql:connect " + url + "}{@sql:select (rs=rs) * from Employees}{rs next}{rs 1} {rs 2} {rs 3}\n" +
                        "{rs next}{rs 1} {rs 2} {rs 3}\n").atPosition("SelectTest.jamal", 1, 0)
                .results("1 Alice Manager\n" +
                        "2 Bob Developer\n"
                );
    }

    @Test
    void testMaliciousCode() throws Exception {
        final var root = DocumentConverter.getRoot();
        String url = "jdbc:h2:" + root + "/target/testdb";

        TestThat.theInput("{@option failfast}\" +\n" +
                "{@sql:connect " + url + "}" +
                "{@sql:select * from Employees;select * Bbb }").throwsUp(IllegalArgumentException.class);
    }

    @Test
    void testVerbatimness() throws Exception {
        final var root = DocumentConverter.getRoot();
        String url = "jdbc:h2:" + root + "/target/testdb";
        TestThat.theInput("{@option failfast}" +
                "{@sql:connect " + url + "}" +
                "{@sql:select (rs=rs) '{@ident hello}' AS result}" +
                "{rs next}" +
                "{#eval {rs 1}}").results("hello");
    }

}
