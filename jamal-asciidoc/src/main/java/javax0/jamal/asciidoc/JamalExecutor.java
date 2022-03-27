package javax0.jamal.asciidoc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class JamalExecutor {

    public static List<String> execute(String inputFileName) {
        final var outputFileName = inputFileName.substring(0, inputFileName.length() - 4);
        final var commandArray = Configuration.INSTANCE.externalCommand
                .replaceAll("\\$1", inputFileName)
                .replaceAll("\\$2", outputFileName)
                .split("\\s+");
        ProcessBuilder pb = new ProcessBuilder(commandArray);
        try {
            pb.start();
        } catch (IOException e) {
            return List.of(ExceptionDumper.dump(e).toString().split("\n"));
        }
        try (final var reader = new BufferedReader(new FileReader(outputFileName, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            return List.of(ExceptionDumper.dump(e).toString().split("\n"));
        }
    }
}
