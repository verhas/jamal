package javax0.jamal.io;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

public class Exec implements Macro {

    final static Consumer<String> DEV_NULL = (String line) -> {
    };

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        // snippet exec_options
        final var osOnly = Params.holder(null, "osOnly", "os").asPattern();
        // {%@define osOnly=defines a pattern for the operating system's name.
        // The execution will only start if the operating system's name matches the pattern.
        // The pattern is a regular expression.
        // The pattern is matched against the operating system's name using the Java pattern matching `find()` method.
        // It means that it is enough to provide a pattern that matches part of the OS name.
        // For example `windows` will match `windows 10` and `windows 7` but not `Linux`.
        // If the pattern is not provided, the execution will start on all operating systems.%}{%osOnly%}
        final var input = Params.holder(null, "input").asString();
        // defines the file name to be used as standard input for the new process.
        // If it is not provided then the content of the macro will be used as input.
        // When an `input` is defined the content of the macro will be ignored.
        final var output = Params.holder(null, "output").asString();
        // defines the file name to be used as standard output for the new process.
        // If it is not provided then the output will appear as the result of the macro.
        // When an `output` is defined the result of the macro will be empty string.
        final var error = Params.holder(null, "error").asString();
        // defines the file name to be used as standard error for the new process.
        // If it is not provided then the standard error will be used.
        final var command = Params.holder(null, "command").asString();
        // The name of the command to be executed.
        // This is not the name of the shell script or any executable.
        // For security reason every executable should be configured via a system property, environment variable or in the `~/.jamal.setup.properties` file.
        // The command itself is the string value of the configuration property.
        final var arguments = Params.holder(null, "argument", "arguments").asList(String.class);
        // The arguments to be passed to the command.
        // This is a multivalued parameter.
        final var environment = Params.holder(null, "environment", "env").asString();
        // This option can specify the environment variables to be passed to the command.
        // This option usually is a multi-line string, thus the use of the `"""` delimiter is recommended.
        // Each line of the configuration parameter can be
        // * empty, in which case the line is ignored
        // * a comment starting with the `#` character, in which case the line is ignored
        // * a `key=value` pair, in which case the key is the name of the environment variable and the value is the value of the variable.
        final var envReset = Params.holder(null, "envReset", "reset").asBoolean();
        // This option can be used to reset the environment variables before the command is executed.
        // Without these option the command will inherit the environment variables of the Jamal process and the defined environment variables are added to the current list.
        final var cwd = Params.holder(null, "directory", "cwd", "curdir", "cd").asString();
        // Set the current working directory for the command.
        // If this option is not provided then the current working directory of the Jamal process will be used.
        final var async = Params.holder(null, "async", "asynch", "asynchronous").asString();
        // Using this option Jamal will not wait for the command to finish before continuing with the next macro.
        // In this case the output cannot be used as the result of the macro.
        // If this option is used the output of the macro will be empty string.
        // The value of this option has to be a macro name, which will be defined and will hold the reference to the process.
        // This macro can later be used to wait for the process to finish.
        // Although technically the name is a user defined macro, you cannot use it as a conventional user defined macro.
        // It does not have any "value" and whenever the code would evaluate the macro it will result an error.
        // Similarly, the name MUST NOT be defined as a user defined macro at the time the `exec` macro is evaluated.
        // The exec macro handles the name as the core built-in macro `define` when a `!` is used after the macro name.
        // If there is a user defined macro of the same name on the same level, an error will occur.
        final var wait = Params.holder(null, "wait", "waitMax", "timeOut").asInt();
        // {%@define wait=This option can be used to specify the maximum amount of time in milliseconds to wait for the process to finish.
        // If the process does not finish in the specified time a BadSyntax exception will be thrown.%}{%wait%}
        // This option cannot be used together with the `async` option.
        final var destroy = Params.holder(null, "destroy", "kill").asBoolean();
        // {%@define destroy=This option can be used to destroy the process if it has not finished within the specified time.
        // This option can only be used together with the wait option.%}{%destroy%}
        final var force = Params.holder(null, "force", "forced").asBoolean();
        // {%@define force=This option instructs the macro to destroy the process forcibly.
        // This option can only be used together with the destroy option.%}{%force%}
        final var optional = Params.holder(null, "optional").asBoolean();
        // This option tells the macro to skip the execution of the command is not configured.
        // If the macro uses the option `asynch` the process id will still be defined without a process.
        // Any `io:waitFor` macro waiting for this process should also use the `optional` option.
        // end snippet
        Scan.using(processor).from(this).firstLine().keys(osOnly, input, output, error, command, arguments,
                environment, envReset, cwd, async, wait, destroy, force, optional).parse(in);

        BadSyntax.when(wait.isPresent() && async.isPresent(), "The `wait` and `async` options cannot be used together.");
        BadSyntax.when(force.is() && !destroy.is(), "The `force` option can only be used together with the `destroy` option.");
        BadSyntax.when(destroy.is() && !wait.isPresent(), "The `destroy` option can only be used together with the `wait` option.");

        if (thisOsIsNotOk(osOnly)) {
            return "";
        }

        ProcessBuilder pb = new ProcessBuilder();
        if (setCommand(command, arguments, pb, optional)) {
            if (async.isPresent()) {
                defineProcessNameHolderMacro(processor, async.get(), null);
            }
            return "";
        }

        redirectIfPresent(input, in, pb::redirectInput);
        redirectIfPresent(output, in, pb::redirectOutput);
        redirectIfPresent(error, in, pb::redirectError);
        if (!error.isPresent()) {
            pb.redirectError(INHERIT);
        }

        setCWDIfPresent(in, cwd, pb);
        setEnvironment(environment, envReset, pb);

        final Process process = startProcess(pb);

        feedInputFromMacro(in, input, process);

        String result;
        if (output.isPresent()) {
            result = "";
            if (async.isPresent()) {
                defineProcessNameHolderMacro(processor, async.get(), process);
            } else {
                waitForTheProcessToFinish(process, wait, destroy, force);
            }
        } else {
            if (async.isPresent()) {
                result = "";
                asyncOutputCollector(process, DEV_NULL);
                defineProcessNameHolderMacro(processor, async.get(), process);
            } else {
                try (final var sw = new StringWriter()) {
                    waitForTheProcessToFinish(process, wait, destroy, force, sw);
                    result = sw.toString();
                } catch (IOException | InterruptedException e) {
                    throw new BadSyntax(String.format("Failed to read the output of the process %s", e.getMessage()), e);
                }
            }
        }
        return result;
    }

    private void defineProcessNameHolderMacro(final Processor processor, final String id, final Process process) throws BadSyntax {
        final var existing = processor.getRegister().getUserDefined(id);
        BadSyntax.when(existing.isPresent(),  "The name `%s` is already used as a user defined macro .", id);
        processor.define(new ProcessHolder(id, process));
    }

    private static boolean thisOsIsNotOk(final Params.Param<Pattern> osOnly) throws BadSyntax {
        if (osOnly.isPresent()) {
            return !osOnly.get().matcher(System.getProperty("os.name")).find();
        }
        return false;
    }

    private static void waitForTheProcessToFinish(final Process process,
                                                  final Params.Param<Integer> wait,
                                                  final Params.Param<Boolean> destroy,
                                                  final Params.Param<Boolean> force) throws BadSyntax {
        try {
            if (wait.isPresent()) {
                process.waitFor(wait.get(), TimeUnit.MILLISECONDS);
                if (process.isAlive()) {
                    destroyTheRunawayProcess(process, destroy, force);
                    throwTimeout(process.pid(), wait.get());
                }
            } else {
                process.waitFor();
            }
        } catch (InterruptedException e) {
            throw new BadSyntax("The process was interrupted.");
        }
    }

    private static void waitForTheProcessToFinish(final Process process,
                                                  final Params.Param<Integer> wait,
                                                  final Params.Param<Boolean> destroy,
                                                  final Params.Param<Boolean> force,
                                                  final StringWriter sw) throws BadSyntax, InterruptedException {
        final var outputCollector = asyncOutputCollector(process, sw::write);
        if (wait.isPresent()) {
            process.waitFor(wait.get(), TimeUnit.MILLISECONDS);
            if (process.isAlive()) {
                outputCollector.cancel(true);
                destroyTheRunawayProcess(process, destroy, force);
                throwTimeout(process.pid(), wait.get());
            }
        } else {
            process.waitFor();
        }
        try {
            outputCollector.get();
        } catch (ExecutionException e) {
            throw new BadSyntax(String.format("Failed to read the output of the process %s", e.getMessage()), e);
        }
    }

    private static void throwTimeout(final long pid, final int timeOut) throws BadSyntax {
        throw new BadSyntax(
                String.format("The process (pid=%d) did not finish in the specified time, %d milliseconds.", pid, timeOut));
    }

    private static Future<?> asyncOutputCollector(final Process process, final Consumer<String> consumer) {
        final var handler = new StreamHandler(process.getInputStream(), consumer);
        return Executors.newSingleThreadExecutor().submit(handler);
    }

    private static void destroyTheRunawayProcess(final Process process,
                                                 final Params.Param<Boolean> destroy,
                                                 final Params.Param<Boolean> force) throws BadSyntax {
        if (destroy.is()) {
            if (force.is()) {
                process.destroyForcibly();
            } else {
                process.destroy();
            }
        }
    }

    private static void feedInputFromMacro(final Input in, final Params.Param<String> input, final Process process) throws BadSyntax {
        if (!input.isPresent()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(in.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new BadSyntax(String.format("Failed to write to process: %s", e.getMessage()), e);
            }
        }
    }

    private static Process startProcess(final ProcessBuilder pb) throws BadSyntax {
        try {
            return pb.start();
        } catch (IOException e) {
            throw new BadSyntax(String.format("Failed to start process: %s", e.getMessage()), e);
        }
    }

    private static void setEnvironment(final Params.Param<String> environment, final Params.Param<Boolean> envReset, final ProcessBuilder pb) throws BadSyntax {
        if (environment.isPresent()) {
            final var env = pb.environment();
            if (envReset.is()) {
                env.clear();
            }
            for (var line : environment.get().split("\n")) {
                if (line.isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                final var parts = line.split("=", 2);
                BadSyntax.when(parts.length != 2,  "The environment variable '%s' is not defined correctly.", line);
                env.put(parts[0], parts[1]);
            }
        }
    }

    private static void setCWDIfPresent(final Input in, final Params.Param<String> cwd, final ProcessBuilder pb) throws BadSyntax {
        if (cwd.isPresent()) {
            pb.directory(getFile(cwd.get(), in));
        }
    }

    /**
     * Sets the command in the process builder.
     *
     * @param command   the parameter with the symbolic name of the command as it is configured in the environment.
     * @param arguments the arguments to the command.
     * @param pb        the process builder to set the command in.
     * @param optional  option to ignore error in case the command is not configured in the environment.
     * @return {@code true} if the command is optional, and was not configured.
     * @throws BadSyntax if the command is not configured in the environment and is not optional or the parameter is missing.
     */
    private static boolean setCommand(final Params.Param<String> command, final Params.Param<List<String>> arguments, final ProcessBuilder pb, final Params.Param<Boolean> optional) throws BadSyntax {
        BadSyntax.when(!command.isPresent(), "'command' for the macro 'exec' is mandatory.");
        final var executable = EnvironmentVariables.getenv(command.get());
        if (executable.isEmpty()) {
            if (optional.is()) {
                return true;
            }
            throw new BadSyntax(String.format("The command '%s' is not defined in the environment.", command.get()));
        }
        final var cmd = new ArrayList<String>();
        cmd.add(executable.get());
        cmd.addAll(arguments.get());
        pb.command(cmd);
        return false;
    }

    private static void redirectIfPresent(Params.Param<String> stream, Input in, Consumer<File> store) throws BadSyntax {
        if (stream.isPresent()) {
            store.accept(getFile(stream.get(), in));
        }
    }

    private static File getFile(final String name, final Input in) throws BadSyntax {
        final var absolute = FileTools.absolute(in.getReference(), name);
        BadSyntax.when(absolute.startsWith("https:") || absolute.startsWith("res:"), String.format("The file '%s' cannot be used as input, output or error", name));
        return new File(absolute);
    }

    @Override
    public String getId() {
        return "io:exec";
    }

    public static class WaitFor implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            // snippet waitFor_options
            final var osOnly = Params.holder(null, "osOnly", "os").asPattern();
            // {%osOnly%}
            final var async = Params.holder(null, "async", "asynch", "asynchronous", "id", "name").asString();
            // This option should refer to the name, which was specified in the macro `io:exec`.
            // The macro will wait for the process that was started with this name to finish.
            // Note that this option has two extra aliases, that do not exist in the macro `exec`.
            // These are `id` and `name`.
            final var wait = Params.holder(null, "wait", "waitMax", "timeOut").asInt();
            // {%wait%}
            // If this option is not present the macro will wait for the process to finish without time limit.
            final var destroy = Params.holder(null, "destroy", "kill").asBoolean();
            // {%destroy%}
            final var force = Params.holder(null, "force", "forced").asBoolean();
            // {%force%}
            final var optional = Params.holder(null, "optional").asBoolean();
            // Use this option if the process was started with the `optional` option.
            // Using this option will not try to wait for a process, which was not started at the first place.
            // end snippet
            Scan.using(processor).from(this).tillEnd().keys(osOnly, async, wait, destroy, force, optional).parse(in);
            final var idMacro = processor.getRegister().getUserDefined(async.get());
            BadSyntax.when(idMacro.isEmpty(),  "Process id '%s' is not defined.", async.get());
            BadSyntax.when(!(idMacro.get() instanceof ProcessHolder),  "Process id '%s' is not a process name.", async.get());
            final var process = ((ProcessHolder) idMacro.get()).getObject();
            if (process == null || optional.is()) {
                return "";
            }
            waitForTheProcessToFinish(process, wait, destroy, force);
            return "";
        }

        @Override
        public String getId() {
            return "io:waitFor";
        }
    }

    private static class ProcessHolder implements UserDefinedMacro, ObjectHolder<Process> {
        private final String id;
        private final Process process;

        private ProcessHolder(final String id, final Process process) {
            this.id = id;
            this.process = process;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Process getObject() {
            return process;
        }

        @Override
        public String evaluate(final String... parameters) throws BadSyntax {
            throw new BadSyntax(String.format("'%s' is a process reference and must not be used as a user defined macro.", id));
        }

        @Override
        public int expectedNumberOfArguments() {
            return -1;
        }
    }

    private static class StreamHandler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamHandler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (final var br = new BufferedReader(new InputStreamReader(inputStream))) {
                br.lines().forEach(consumer);
            } catch (IOException e) {
                // ignore, it comes from close() as in Closeable
            }
        }
    }
}
