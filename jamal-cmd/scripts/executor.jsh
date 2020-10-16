import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

    void execute() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String sep = System.getProperty("path.separator");
        String cp = Arrays.stream(
            CACHE_ROOT_DIRECTORY.listFiles((d, n) -> n.endsWith(".jar"))).map(File::getAbsolutePath).collect(Collectors.joining(sep));
        System.out.println("Class path is " + cp);
        builder.command("java", "-cp", cp, "javax0.jamal.cmd.JamalMain")
            .directory(new File("."));
        Process process = builder.start();
        process.getInputStream().transferTo(System.out);
        int exitCode = process.waitFor();
    }