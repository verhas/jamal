package javax0.jamal.asciidoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Execute Jamal as an external process as it is configured in the environment, the system variables or in the Jamal
 * configuration.
 */
public class JamalExecutor {

    public static synchronized List<String> execute(String inputFileName, List<String> lines) {
        final var outputFileName = inputFileName.substring(0, inputFileName.length() - ".jam".length());
        final var input = new File(inputFileName+".tmp");
        try {
            try (final var writer = new BufferedWriter(new FileWriter(input))) {
                writer.write(String.join("\n", lines));
            }
            final var commandArray = Configuration.INSTANCE.externalCommand
                    .replaceAll("\\$1", input.getAbsolutePath())
                    .replaceAll("\\$2", outputFileName)
                    .split("\\s+");
            ProcessBuilder pb = new ProcessBuilder(commandArray);
            final var process = pb.start();
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            return List.of(ExceptionDumper.dump(e,inputFileName).toString().split("\n"));
        }finally {
            input.delete();
        }
        try (final var reader = new BufferedReader(new FileReader(outputFileName, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            return List.of(ExceptionDumper.dump(e,inputFileName).toString().split("\n"));
        }
    }
}
