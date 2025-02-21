package javax0.jamal.py;

import javax0.jamal.api.EnvironmentVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code PythonFinder} class is responsible for locating a Python interpreter
 * on the system. It checks environment variables, common installation paths, and
 * the system's PATH variable to find the most recent Python version available.
 * <p>
 * The class caches the detected interpreter at the JVM level to avoid redundant lookups.
 * It first checks the {@code JAMAL_PYTHON_INTERPRETER} environment variable, then searches
 * in standard installation directories depending on the operating system, and finally
 * falls back to scanning the system's PATH.
 * <p>
 * If {@code checkConfigured} is set to {@code true}, the found interpreter is verified
 * to ensure it is functional before returning it.
 */

public class PythonFinder {

    // cache the result on the JVM level, assuming that there is no need to find the Python interpreter
    // again and again in the same process
    static final AtomicReference<Optional<String>> interpreter = new AtomicReference<>(null);

    final boolean checkConfigured;

    // snipline ENV_JAMAL_PYTHON_INTERPRETER filter="(.*)"
    static final String ENV_JAMAL_PYTHON_INTERPRETER = "JAMAL_PYTHON_INTERPRETER";

    public PythonFinder(boolean checkConfigured) {
        this.checkConfigured = checkConfigured;
    }

    public Optional<String> findPythonInterpreter() {

        if (PythonFinder.interpreter.get() != null) {
            return PythonFinder.interpreter.get();
        }

        final var optPython = EnvironmentVariables.getenv(ENV_JAMAL_PYTHON_INTERPRETER);
        if (optPython.isPresent()) {
            final var pythonInterpreter = optPython.get();
            if (checkConfigured) {
                if (getPythonVersion(pythonInterpreter) == null) {
                    return Optional.empty();
                }
            }
            return optPython;
        }

        String os = System.getProperty("os.name").toLowerCase();

        List<String> commonPaths;
        if (os.contains("win")) {
            commonPaths = List.of(
                    // snippet windows_locations_list
                    "C:\\Python310\\python.exe",
                    "C:\\Python39\\python.exe",
                    "C:\\Python38\\python.exe",
                    "C:\\Python37\\python.exe",
                    "C:\\Python36\\python.exe",
                    "C:\\Python35\\python.exe",
                    "C:\\Python34\\python.exe",
                    "C:\\Python33\\python.exe",
                    "C:\\Python32\\python.exe",
                    "C:\\Python31\\python.exe",
                    "C:\\Python30\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python310\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python39\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python38\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python37\\python.exe"
                    //  end snippet
            );
        } else if (os.contains("mac")) {
            commonPaths = List.of(
                    // snippet mac_locations_list
                    "/usr/local/bin/python3",
                    "/usr/bin/python3",
                    "/opt/homebrew/bin/python3",
                    "/usr/local/bin/python",
                    "/usr/bin/python",
                    "/opt/homebrew/bin/python"
                    //  end snippet
            );
        } else { // Assume Linux
            commonPaths = List.of(
                    // snippet linux_locations_list
                    "/usr/bin/python3",
                    "/usr/local/bin/python3",
                    "/bin/python3",
                    "/usr/bin/python",
                    "/usr/local/bin/python",
                    "/bin/python"
                    //  end snippet
            );
        }

        // Collect all valid interpreters with their versions
        TreeMap<String, String> validInterpreters = new TreeMap<>(Collections.reverseOrder());

        // Check common paths first
        for (String path : commonPaths) {
            if (Files.exists(Paths.get(path))) {
                String version = getPythonVersion(path);
                if (version != null) {
                    validInterpreters.putIfAbsent(version, path);
                }
            }
        }

        // Fall back to checking system PATH
        String[] candidates = {
                "python3",
                "python",
                "py"
        };
        for (String candidate : candidates) {
            String version = getPythonVersion(candidate);
            if (version != null) {
                validInterpreters.putIfAbsent(version, candidate);
            }
        }

        // Return the newest Python interpreter (the highest version)
        final var interpreter = validInterpreters.isEmpty() ? null : validInterpreters.firstEntry().getValue();
        PythonFinder.interpreter.compareAndSet(null, Optional.ofNullable(interpreter));
        return PythonFinder.interpreter.get();
    }

    private String getPythonVersion(String pythonExecutable) {
        try {
            Process process = new ProcessBuilder(pythonExecutable, "--version")
                    .redirectErrorStream(true)
                    .start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            process.waitFor();
            if (output != null) {
                Matcher matcher = Pattern.compile("Python (\\d+)\\.(\\d+)\\.(\\d+)").matcher(output);
                if (matcher.find()) {
                    return String.format("%03d.%03d.%03d",
                            Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3))
                    );
                }
            }
        } catch (IOException | InterruptedException ignored) {
        }
        return null;
    }
}

