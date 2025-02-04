package javax0.jamal.py;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class PythonInterpreter implements AutoCloseable {
    private final Process process;
    private final String workingDirectory;
    private final String venv;

    private Path venvPath;

    private String closeCode = "sys.exit(0)";
    public void setCloseCode(String closeCode) {
        this.closeCode = closeCode;
    }


    public PythonInterpreter(String workingDirectory, String venv) {
        this.workingDirectory = workingDirectory;
        this.venv = venv;
        this.process = launch();

    }

    @Override
    public void close() throws IOException {
        final var out = new PrintStream(process.getOutputStream());
        out.println(".\n" + closeCode + "\n.\n");
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
            Path pythonFile = Files.createTempFile("Processor", "py");
            extractControlProgram(pythonFile);

            // Find Python interpreter
            String pythonInterpreter = findPythonInterpreter();

            // Start the Python script
            ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, pythonFile.toFile().getAbsolutePath());
            if (venvPath != null) {

                final var env = pb.environment();

                // Set VIRTUAL_ENV
                env.put("VIRTUAL_ENV", venvPath.toFile().getAbsolutePath());

                // Prepend the venv's bin directory to PATH (Linux/macOS)
                env.put("PATH", venvPath.toFile().getAbsolutePath() + "/bin:" + env.get("PATH"));
            }

            // Set a working directory if specified
            if (workingDirectory != null && !workingDirectory.isBlank()) {
                pb.directory(new File(workingDirectory));
            }

            return pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void extractControlProgram(Path pythonFile) throws IOException {
        try (var resourceStream = PythonInterpreter.class.getClassLoader().getResourceAsStream("Processor.py")) {
            if (resourceStream == null) {
                throw new RuntimeException("Could not find Python processor source code");
            }
            Files.copy(resourceStream, pythonFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String findPythonInterpreter() {
        if (workingDirectory != null && !workingDirectory.isBlank() && (venvPath = Paths.get(workingDirectory, venv)).toFile().exists()) {
            final var isWin = System.getProperty("os.name").toLowerCase().contains("win");
            Path pythonBin = venvPath.resolve(isWin ? "Scripts/python.exe" : "bin/python"); // Linux/macOS

            if (Files.exists(pythonBin)) {
                return pythonBin.toAbsolutePath().toString();
            } else {
                throw new RuntimeException("Python executable not found in virtual environment: " + pythonBin);
            }
        }

        return new PythonFinder(false)
                .findPythonInterpreter()
                .orElseThrow(() -> new RuntimeException("Error: No Python interpreter found."));
    }
}
