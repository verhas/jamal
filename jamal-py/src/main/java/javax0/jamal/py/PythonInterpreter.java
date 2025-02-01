package javax0.jamal.py;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class PythonInterpreter implements AutoCloseable {
    final Process process;

    public PythonInterpreter() {
        this.process = launch();
    }

    @Override
    public void close() throws IOException {
        final var out = new PrintStream(process.getOutputStream());
        out.println(".\nsys.exit(0)\n.\n");
        out.flush();
        boolean exited = false;
        try {
            exited = process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
        }
        if (!exited) {
            process.destroy();
        }
    }

    public String execute(String code) throws IOException {
        final var delimiter = calculateDelimiter(code);

        final var os = process.getOutputStream();
        os.write(("\n" + delimiter + "\n").getBytes(StandardCharsets.UTF_8));
        os.write(code.getBytes(StandardCharsets.UTF_8));
        os.write(("\n" + delimiter + "\n").getBytes(StandardCharsets.UTF_8));
        os.flush();
        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        //noinspection StatementWithEmptyBody
        while ((line = reader.readLine()).isEmpty()) ;
        final var responseDelimiter = line;

        final var response = new StringBuilder();
        boolean first = true;
        while ((line = reader.readLine()) != null && !line.contentEquals(responseDelimiter)) {
            if (!first) {
                response.append("\n");
            }
            first = false;
            response.append(line);
        }
        return response.toString();
    }

    private String calculateDelimiter(String code) {
        final var lines = code.split("\n", -1);
        final var delimiter = new StringBuilder();
        for (var line : lines) {
            if (line.contentEquals(delimiter)) {
                delimiter.append('.');
            }
            if (line.length() > delimiter.length()) {
                delimiter.append(line.charAt(delimiter.length()) == 'A' ? 'B' : 'A');
            }
        }
        return delimiter.toString();
    }

    public Process launch() {
        try {
            // Move Python code from a resource file
            Path pythonFile = Files.createTempFile("Processor","py");
            try (var resourceStream = PythonInterpreter.class.getClassLoader().getResourceAsStream("Processor.py")) {
                if (resourceStream == null) {
                    throw new RuntimeException("Could not find Python processor source code");
                }
                Files.copy(resourceStream, pythonFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Find a Python interpreter
            String pythonInterpreter = new PythonFinder(false).findPythonInterpreter();
            if (pythonInterpreter == null) {
                throw new RuntimeException("Error: No Python interpreter found.");
            }

            // Start the Python script
            ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, pythonFile.toFile().getAbsolutePath());
            pb.directory(pythonFile.toFile().getParentFile());

            return pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
