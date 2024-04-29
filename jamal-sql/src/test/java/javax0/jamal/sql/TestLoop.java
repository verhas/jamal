package javax0.jamal.sql;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

public class TestLoop {

    @Test
    void testSqlLoop() throws Exception {
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

                for (int i = 1; i < 10; i++) {
                    String insertSQL = "INSERT INTO Employees VALUES (" + i + ", 'Name" + i + "', 'Position" + i + "')";
                    statement.execute(insertSQL);
                }
            }
        }
        TestThat.theInput("" +
                        "{@option failfast}" +
                        "{@import res:sql.jim}" +
                        "{@sql:connect " + url + "}{@sql:select (rs=rs) * from Employees}{@sql:loop (rs=rs) {rs 1} {rs 2} {rs 3}\n}")
                .atPosition("SelectTest.jamal", 1, 0)
                .results(" 1 Name1 Position1\n" +
                        " 2 Name2 Position2\n" +
                        " 3 Name3 Position3\n" +
                        " 4 Name4 Position4\n" +
                        " 5 Name5 Position5\n" +
                        " 6 Name6 Position6\n" +
                        " 7 Name7 Position7\n" +
                        " 8 Name8 Position8\n" +
                        " 9 Name9 Position9\n"
                );
    }

}
