package javax0.jamal.snippet;

import javax0.jamal.api.DotEnvLoder;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class TestShellVar {

    @Test
    @DisplayName("simple shell variable replacement")
    void testSimpleReplacement() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@shell:var $A}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter")
    void testSimpleReplacementWithParameters() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@shell:var (variables=\"A=ops\")$A}"
        ).results("ops");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter name")
    void testSimpleReplacementWithParametersInName() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@define B=A}" +
                "{@shell:var ${$B}}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter value")
    void testSimpleReplacementWithParametersInValue() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@define B=$A}" +
                "{@shell:var $B}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter value as well as in the name")
    void testSimpleReplacementWithParametersInValueAndInName() throws Exception {
        TestThat.theInput("" +
                "{@define this=that or this}" +
                "{@define A=$this is the replacement of the shell variable}" +
                "{@define B=$A}" +
                "{@shell:var ${B}}"
        ).results("that or this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter value as well as in the name")
    void testSimpleReplacementWithSysVar() throws Exception {
        System.setProperty("this", "that or this");
        TestThat.theInput("" +
                "{@define A=$this is the replacement of the shell variable}" +
                "{@define B=$A}" +
                "{@shell:var ${B}}"
        ).results("that or this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("When the last character is the dollar sign then it is not a variable")
    void testLastCharacterIs$() throws Exception {
        TestThat.theInput("" +
                "{@shell:var abrakadabra$}"
        ).results("abrakadabra$");
    }

    @Test
    @DisplayName("When the $ is escaped using \\ character then it is not a variable")
    void testEscCharacterIs$() throws Exception {
        TestThat.theInput("" +
                "{@shell:var abrakadabra\\$a}"
        ).results("abrakadabra$a");
    }

    @Test
    @DisplayName("When the \\$ is escaped using \\ character then it is not not escaped")
    void testEscEscCharacterIs$() throws Exception {
        TestThat.theInput("" +
                "{@define a=quux}" +
                "{@shell:var abrakadabra\\\\$a}"
        ).results("abrakadabra\\quux");
    }

    @Test
    @DisplayName("When the \\\\$ is escaped using \\ character then it is not not escaped")
    void testEscXXEscCharacterIs$() throws Exception {
        TestThat.theInput("" +
                "{@shell:var abrakadabra\\\\\\$a}"
        ).results("abrakadabra\\$a");
    }

    @Test
    @DisplayName("When the string ends with ${ then it is not a variable")
    void testLastChaarcterIs$Brace() throws Exception {
        TestThat.theInput("{@sep []}" +
                "[@shell:var abrakadabra${]"
        ).throwsBadSyntax("Missing '}' in the shell variable substitution");
    }

    @Test
    @DisplayName("Test shell vars from .env file")
    void testDotEnvRead() throws Exception {
        Path currentPath = Paths.get("").toAbsolutePath();
        Path envPath = currentPath.resolve(".env");
        byte[] oldEnv = null;
        try {
            oldEnv = Optional.of(envPath).filter(Files::exists)
                    .map(path -> {
                        try {
                            return Files.readAllBytes(path);
                        } catch (Exception e) {
                            return null;
                        }
                    }).orElse(null);

            Files.write(envPath, "nuwanda=Loretta\n".getBytes());
            DotEnvLoder.load();

            TestThat.theInput("" +
                    "{@shell:var From now on, call me $nuwanda}"
            ).results("From now on, call me Loretta");
        } finally {
            // delete the test environment file
            Files.delete(envPath);
            // write back the old .env file
            if (oldEnv != null) {
                Files.write(envPath, oldEnv);
            }
        }
    }

    @Test
    @DisplayName("too deep recursion")
    void testTooDeepRecursion() throws Exception {
        TestThat.theInput("" +
                "{@define recursion=$recursion}" +
                "{@shell:var $recursion}"
        ).throwsBadSyntax("Too deep recursion in shell variable substitution");
    }
}
