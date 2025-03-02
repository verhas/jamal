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
        final var delimiter = calculateDelimiter(closeCode);
        out.println(delimiter + "\n" + closeCode + "\n" + delimiter + "\n");
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

        writeProcessInput(code, delimiter);
        return readProcessOutput();
    }

    private void writeProcessInput(String code, String delimiter) throws IOException {
        final var os = process.getOutputStream();
        os.write(("\n" + delimiter + "\n").getBytes(StandardCharsets.UTF_8));
        os.write(code.getBytes(StandardCharsets.UTF_8));
        os.write(("\n" + delimiter + "\n").getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    private String readProcessOutput() throws IOException {
        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        final var responseDelimiter = getFirstNonEmptyLine(reader);

        final var response = new StringBuilder();
        boolean first = true;
        String line;
        while ((line = reader.readLine()) != null && !line.contentEquals(responseDelimiter)) {
            if (!first) {
                response.append("\n");
            }
            first = false;
            response.append(line);
        }
        return response.toString();
    }

    private String getFirstNonEmptyLine(BufferedReader reader) throws IOException {
        String line;
        //noinspection StatementWithEmptyBody
        while ((line = reader.readLine()).isEmpty()) ;
        return line;
    }

    /**
     * Calculates a delimiter string guaranteed to be distinct from any line in the given input code.
     * <p>
     * The method splits the input string into lines and iteratively constructs a
     * delimiter string.
     * <p>
     * The construction ensures uniqueness by following these rules:
     * <ul>
     *     <li>If the current delimiter matches an existing line, a '.' character is appended to
     *     the delimiter, making it different.</li>
     *     <li>If a longer line exists, the delimiter is extended by appending either 'A' or 'B'
     *     depending on the character at its current length position. This guarantees further
     *     divergence from any existing line.</li>
     * </ul>
     * Since the delimiter is extended whenever it matches a line or encounters a longer
     * line, it ensures that the final delimiter does not appear as any of the lines in the input.
     *
     * @param code the input string containing multiple lines
     * @return a delimiter string that is distinct from any line in the input
     */
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
            final var pythonFile = Files.createTempFile("Processor", "py");
            extractControlProgram(pythonFile);
            final var pythonInterpreter = findPythonInterpreter();
            final var pb = getProcessBuilder(pythonInterpreter, pythonFile);
            setWorkingDirectory(pb);
            return pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setWorkingDirectory(ProcessBuilder pb) {
        if (workingDirectory != null && !workingDirectory.isBlank()) {
            pb.directory(new File(workingDirectory));
        }
    }

    private ProcessBuilder getProcessBuilder(String pythonInterpreter, Path pythonFile) {
        final var pb = new ProcessBuilder(pythonInterpreter, pythonFile.toFile().getAbsolutePath());
        setVenv(pb);
        return pb;
    }

    private void setVenv(ProcessBuilder pb) {
        if (venvPath != null) {
            final var env = pb.environment();
            env.put("VIRTUAL_ENV", venvPath.toFile().getAbsolutePath());
            String binDir = System.getProperty("os.name").toLowerCase().contains("win") ? "Scripts" : "bin";
            env.put("PATH", venvPath.toFile().getAbsolutePath() + File.separator + binDir + File.pathSeparator + env.get("PATH"));
        }
    }

    /**
     * Extracts the control program by copying the embedded Python source file {@code Processor.py}
     * to the specified location.
     *
     * <p>The method retrieves the {@code Processor.py} resource from the classpath and writes it 
     * to the given {@code pythonFile} path. If the resource cannot be found, a 
     * {@link RuntimeException} is thrown.</p>
     *
     * <p>If the file already exists at the target location, it will be replaced.</p>
     *
     * @param pythonFile the path where the Python processor source code should be extracted
     * @throws IOException if an I/O error occurs while copying the resource
     * @throws RuntimeException if the {@code Processor.py} resource cannot be found in the classpath
     */
    private void extractControlProgram(Path pythonFile) throws IOException {
        try (var resourceStream = PythonInterpreter.class.getClassLoader().getResourceAsStream("Processor.py")) {
            if (resourceStream == null) {
                throw new RuntimeException("Could not find Python processor source code");
            }
            Files.copy(resourceStream, pythonFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Finds the Python interpreter to be used.
     *
     * <p>If a virtual environment exists in the specified working directory, the method checks for the Python
     * executable inside the virtual environment. On Windows, it looks for {@code Scripts/python.exe}, while on
     * Linux/macOS, it looks for {@code bin/python}.</p>
     *
     * <p>If the virtual environment contains a valid Python interpreter, its absolute path is returned.
     * If the virtual environment exists but does not contain a valid Python interpreter, a {@link RuntimeException} is thrown.</p>
     *
     * <p>If no virtual environment is found, the method attempts to locate a system-wide Python interpreter using {@link PythonFinder}.
     * If no interpreter is found, a {@link RuntimeException} is thrown.</p>
     *
     * @return the absolute path of the found Python interpreter
     * @throws RuntimeException if no valid Python interpreter is found either in the virtual environment or system-wide
     */
    private String findPythonInterpreter() {
        if (workingDirectory != null && !workingDirectory.isBlank() && venv != null && (venvPath = Paths.get(workingDirectory, venv)).toFile().exists()) {
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
