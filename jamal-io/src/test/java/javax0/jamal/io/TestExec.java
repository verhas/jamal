package javax0.jamal.io;

import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Testing external process execution is system-dependent and makes not much sense to do uint testing.
 * These tests are integration tests that are executed only occasionally.
 */
@Disabled("Integration tests, runs on MacOS only. Run it from IDE interactively, not part of the every day build. Not portable and slow.")
public class TestExec {

    @BeforeAll
    static void beforeAll() throws Exception {
        SentinelSmith.forge("exec");
    }

    @Test
    @DisplayName("Executing 'java -version' will print out the Java version to the console.")
    void test() throws Exception {
        // tag::java_echo_version[]
        System.setProperty("exec", "java");
        // end::java_echo_version[]
        TestThat.theInput("" +
                        // tag::java_echo_version[]
                        "{@io:exec command=EXEC argument=\"-version\"}"
                // end::java_echo_version[]
        ).results("");
    }

    @Test
    @DisplayName("Executing 'echo hello' print out 'hello' to file")
    void testOutputToFile() throws Exception {
        SentinelSmith.forge("exec");
        // tag::pwd[]
        System.setProperty("exec", "pwd");
        // end::pwd[]
        Files.deleteIfExists(Paths.get("target/hallo.txt"));
        Assertions.assertTrue(
                TestThat.theInput("" +
                                        // tag::pwd[]
                                        "{@io:exec command=EXEC cwd=target output=\"target/hallo.txt\"}" +
                                        "{@include [verbatim] target/hallo.txt}"
                                // end::pwd[]
                        )
                        .ignoreLineEnding()
                        .atPosition("./in.jam", 1, 1)
                        .results().endsWith("/target\n"));
    }

    @Test
    @DisplayName("Executing 'cat' print out 'macro text' as the macro result")
    void testOutputAndInput() throws Exception {
        // tag::cat[]
        System.setProperty("exec", "cat");
        // end::cat[]
        Files.deleteIfExists(Paths.get("target/catoutput.txt"));
        TestThat.theInput("" +
                                // tag::cat[]
                                "{@io:exec command=EXEC output=\"target/catoutput.txt\"\n" +
                                "hello, this is the text for the file}"
                        // end::cat[]
                ).atPosition("./in.jam", 1, 1)
                .results("");
        Assertions.assertEquals("hello, this is the text for the file", Files.readString(Paths.get("target/catoutput.txt")));
    }

    @Test
    @DisplayName("Executing 'echo hello' print out 'hello' as the macro result")
    void testOutput() throws Exception {
        // tag::echo[]
        System.setProperty("exec", "echo");
        // end::echo[]
        TestThat.theInput("" +
                                // tag::echo[]
                                "{@io:exec command=EXEC argument=\"hello\"}"
                        // end::echo[]
                ).atPosition("./in.jam", 1, 1)
                .results("hello");
    }

    @Test
    @DisplayName("Executing 'sleep 1; echo hello' print out 'hello' as the macro result only when synchronous")
    void testOutputAsync() throws Exception {
        // tag::sleep[]
        System.setProperty("exec", "sh");
        // end::sleep[]
        Files.write(Paths.get("target/async.sh"), "sleep 1\necho hello".getBytes(StandardCharsets.UTF_8));
        TestThat.theInput("" +
                        "{@io:exec command=EXEC argument=target/async.sh}"
                ).atPosition("./in.jam", 1, 1)
                .results("hello");
        final var start = System.currentTimeMillis();
        TestThat.theInput("" +
                                // tag::sleep[]
                                "{@io:exec asynch=PROC001 command=EXEC argument=target/async.sh}"
                        // end::sleep[]
                ).atPosition("./in.jam", 1, 1)
                .results("");
        final var runTime = System.currentTimeMillis() - start;
        // This is not deterministic as it MAY happen in some circumstances that the test execution is slower than 1 second.
        Assertions.assertTrue(runTime < 1000);
    }

    @Test
    @DisplayName("Executing 'sleep 1; echo hello' print out to file as the macro result only when synchronous")
    void testFileOutputAsync() throws Exception {
        System.setProperty("exec", "sh");
        Files.write(Paths.get("target/async.sh"), "sleep 1\necho hello".getBytes(StandardCharsets.UTF_8));
        TestThat.theInput("" +
                        "{@io:exec asynch=PROC001 command=EXEC argument=target/async.sh output=target/async_echo_output.txt}"
                ).atPosition("./in.jam", 1, 1)
                .results("");
        Thread.sleep(2000);
        Assertions.assertTrue(Files.exists(Paths.get("target/async_echo_output.txt")));
    }


    @Test
    @DisplayName("Executing 'sleep 1000; and waiting only 1000 milliseconds throws error, destroy the process")
    void testTimeOutDestroy() throws Exception {
        // tag::timeout[]
        System.setProperty("exec", "sleep");
        // end::timeout[]
        TestThat.theInput("" +
                        // tag::timeout[]
                        "{@io:exec command=EXEC argument=1000 wait=1000 destroy}"
                // end::timeout[]
        ).throwsBadSyntax("The process \\(pid=\\d+\\) did not finish in the specified time, 1000 milliseconds.");
    }

    @Test
    @DisplayName("Executing 'sleep 1000; and waiting only 1000 milliseconds throws error with output file, destroy the process")
    void testTimeOutDestroyWithOutput() throws Exception {
        System.setProperty("exec", "sleep");
        TestThat.theInput("" +
                "{@io:exec command=EXEC argument=1000 wait=1000 destroy output=target/dummy.txt}"
        ).throwsBadSyntax("The process \\(pid=\\d+\\) did not finish in the specified time, 1000 milliseconds.");
    }

    @Test
    @DisplayName("Executing 'sleep 1000; and waiting only 1000 milliseconds throws error, destroy the process forcibly")
    void testTimeOutDestroyForcibly() throws Exception {
        System.setProperty("exec", "sleep");
        TestThat.theInput("" +
                "{@io:exec command=EXEC argument=1000 wait=1000 destroy force}"
        ).throwsBadSyntax("The process \\(pid=\\d+\\) did not finish in the specified time, 1000 milliseconds.");
    }

    @Test
    @DisplayName("Setting environment variables")
    void testEnvironmentSet() throws Exception {
        // tag::env[]
        System.setProperty("exec", "printenv");
        // end::env[]
        TestThat.theInput("" +
                        // tag::env[]
                        "{@io:exec command=EXEC argument=AAA env=\"AAA=BABA\\n\\n #  oooh my\\n\"}"
                // end::env[]
        ).results("BABA");
    }

    @Test
    @DisplayName("Resetting environment variables")
    void testEnvironmentReset() throws Exception {
        // tag::env_reset[]
        System.setProperty("exec", "printenv");
        // end::env_reset[]
        TestThat.theInput("" +
                        // tag::env_reset[]
                        "{@io:exec command=EXEC argument=JAVA_HOME envReset env=\"AAA=BABA\"}"
                // end::env_reset[]
        ).results("");
    }

    @Test
    @DisplayName("Executing 'sleep 1; async, and waiting only 1000 milliseconds throws error")
    void testWaitFor() throws Exception {
        // tag::wait_for[]
        System.setProperty("exec", "sleep");
        // end::wait_for[]
        TestThat.theInput("" +
                        // tag::wait_for[]
                        "{@io:exec command=EXEC argument=1 asynch=PRG001}{@io:waitFor id=PRG001}"
                // end::wait_for[]
        ).results("");
    }

    @Test
    @DisplayName("invalid command is not a problem when this is not the OS")
    void testInvalidOutput() throws Exception {
        TestThat.theInput("" +
                "{@io:exec command=abrakadabra os=abrakadabra}"
        ).results("");
    }

    @Test
    @DisplayName("Command specified but not defined does not throw exception when optional")
    void testUndefinedCommandOptional() throws Exception {
        TestThat.theInput("" +
                        // tag::undefined_command_optional[]
                        "{@io:exec command=abrakadabra optional}"
                //end::undefined_command_optional[]
        ).results("");
    }

    @Test
    @DisplayName("Optional command still defines the process id as a macro")
    void testUndefinedCommandOptionalDefines() throws Exception {
        TestThat.theInput("" +
                        // tag::undefined_command_optional_defines[]
                        // using PRG001 as a macro will throw an exception, but not undefined macro
                        "{@io:exec command=abrakadabra optional async=PRG001}{PRG001}"
                //end::undefined_command_optional_defines[]
        ).throwsBadSyntax("'PRG001' is a process reference and must not be used as a user defined macro\\.");
    }

    @Nested
    class TestFailures {
        @Test
        @DisplayName("Executing 'sleep 10; and waiting only 1000 milliseconds throws error")
        void testTimeOut() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC argument=10 wait=1000}"
            ).throwsBadSyntax("The process \\(pid=\\d+\\) did not finish in the specified time, 1000 milliseconds.");
        }

        @Test
        @DisplayName("Executing 'sleep 10; async, and waiting only 1000 milliseconds throws error")
        void testTimeOutWithWait() throws Exception {
            // tag::wait_for_timeout[]
            System.setProperty("exec", "sleep");
            // end::wait_for_timeout[]
            TestThat.theInput("" +
                            // tag::wait_for_timeout[]
                            "{@io:exec command=EXEC argument=10 asynch=PRG001}{@io:waitFor id=PRG001 timeOut=1000 destroy}"
                    // end::wait_for_timeout[]
            ).throwsBadSyntax("The process \\(pid=\\d+\\) did not finish in the specified time, 1000 milliseconds.");
        }

        @Test
        @DisplayName("Waiting for undefined process throws error")
        void testWaitForUndefined() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:waitFor id=PRG001 timeOut=1000}"
            ).throwsBadSyntax("Process id 'PRG001' is not defined\\.");
        }

        @Test
        @DisplayName("Waiting for illdefined process throws error")
        void testWaitForIlldefined() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@define PRG001=}{@io:waitFor id=PRG001 timeOut=1000}"
            ).throwsBadSyntax("Process id 'PRG001' is not a process name\\.");
        }

        @Test
        @DisplayName("Cannot use wait and asynch together")
        void testWaitSync() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC argument=1000 wait=1000 asynch=PROC001}"
            ).throwsBadSyntax("In the macro 'io:exec' you cannot use 'wait', 'asynch' together\\.");
        }

        @Test
        @DisplayName("Force without destroy")
        void testForceNoDestroy() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC argument=1000 wait=1000 force}"
            ).throwsBadSyntax("The `force` option can only be used together with the `destroy` option\\.");
        }

        @Test
        @DisplayName("Force without destroy")
        void testDestroyWoWait() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC argument=1000 destroy}"
            ).throwsBadSyntax("The `destroy` option can only be used together with the `wait` option\\.");
        }

        @Test
        @DisplayName("Bad command fails to start and throws exception")
        void testFailToStart() throws Exception {
            System.setProperty("exec", "abrakadabra");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC}"
            ).throwsBadSyntax("Cannot run program \"abrakadabra\": error=2, No such file or directory");
        }

        @Test
        @DisplayName("Bad environment variable setting")
        void testBadEnvironmentSetting() throws Exception {
            System.setProperty("exec", "does not matter");
            TestThat.theInput("" +
                    "{@io:exec command=EXEC env=\"\"\"\n" +
                    "bad environment setting\n" +
                    "\"\"\"}"
            ).throwsBadSyntax("The environment variable 'bad environment setting' is not defined correctly.");
        }

        @Test
        @DisplayName("Command not specified throws exception")
        void testNoCommand() throws Exception {
            TestThat.theInput("" +
                    "{@io:exec}"
            ).throwsBadSyntax("The mandatory parameters 'command' are missing for the macro 'io:exec'");
        }

        @Test
        @DisplayName("Command specified but not defined throws exception")
        void testUndefinedCommand() throws Exception {
            TestThat.theInput("" +
                    "{@io:exec command=abrakadabra}"
            ).throwsBadSyntax("The command 'abrakadabra' is not defined in the environment\\.");
        }

        @Test
        @DisplayName("Invalid output")
        void testInvalidOutput() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=exec output=res:oma.txt}"
            ).throwsBadSyntax("The file 'res:oma\\.txt' cannot be used as input, output or error");
        }

        @Test
        @DisplayName("Invalid output")
        void testErroneousUseOfProcessIdMacro() throws Exception {
            System.setProperty("exec", "sleep");
            TestThat.theInput("" +
                    "{@io:exec command=exec argument=1 asynch=PROC001}{PROC001}"
            ).throwsBadSyntax("'PROC001' is a process reference and must not be used as a user defined macro\\.");
        }
    }
}
