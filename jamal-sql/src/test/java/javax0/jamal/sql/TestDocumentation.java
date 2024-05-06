package javax0.jamal.sql;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

public class TestDocumentation {


    @Test
    void convertDocumentation() throws Exception {
        DocumentConverter.convert(DocumentConverter.getRoot() + "/jamal-sql/README.adoc.jam");
    }

    @Test
    void createSampleDb() throws Exception {
        final var root = DocumentConverter.getRoot();
        String url = "jdbc:h2:" + root + "/jamal-sql/demodb";

        try (final var connection = DriverManager.getConnection(url)) {
            try (final var statement = connection.createStatement()) {
                try {
                    statement.execute("DROP TABLE JamalDocumentation");
                } catch (Exception ignore) {
                }
                statement.execute("CREATE TABLE JamalDocumentation (" +
                        "id INT PRIMARY KEY, some_text VARCHAR(255))");

                for (int i = 1; i < 10; i++) {
                    statement.execute("INSERT INTO JamalDocumentation VALUES (" + i + ", 'text" + i + "')");
                }
            }
        }
        TestThat.theInput("" +
                        "{@option failfast}" +
                        "{@sql:connect " + url + "}{@sql:select (rs=rs) * from JamalDocumentation}{@sql:loop (rs=rs) {rs 1} {rs 2}\n}")
                .atPosition("DocumentationTest.jamal", 1, 0)
                .results("" +
                        " 1 text1\n" +
                        " 2 text2\n" +
                        " 3 text3\n" +
                        " 4 text4\n" +
                        " 5 text5\n" +
                        " 6 text6\n" +
                        " 7 text7\n" +
                        " 8 text8\n" +
                        " 9 text9\n"
                );
    }

    @Test
    void testSampleDb() throws Exception {
        final var root = DocumentConverter.getRoot();
        String url = "jdbc:h2:" + root + "/jamal-sql/demodb";


        TestThat.theInput("" +
                        "{@option failfast}" +
                        "{@sql:connect " + url + "}{@sql:select (rs=rs) * from JamalDocumentation}{@sql:loop (rs=rs) {rs 1} {rs 2}\n}")
                .atPosition("DocumentationTest.jamal", 1, 0)
                .results("" +
                        " 1 text1\n" +
                        " 2 text2\n" +
                        " 3 text3\n" +
                        " 4 text4\n" +
                        " 5 text5\n" +
                        " 6 text6\n" +
                        " 7 text7\n" +
                        " 8 text8\n" +
                        " 9 text9\n"
                );
    }

}
