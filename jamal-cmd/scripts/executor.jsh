import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

    File LOCAL_CACHE = new File(".jamal/cache/.jar");
    LOCAL_CACHE.mkdirs();

    void execute() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String sep = System.getProperty("path.separator");
        String cp = Arrays.stream(cacheRootDirecotry.listFiles((d, n) -> n.endsWith(".jar")))
            .map(File::getAbsolutePath)
            .collect(Collectors.joining(sep));
        if (LOCAL_CACHE.exists()) {
            String localCp = Arrays.stream(LOCAL_CACHE.listFiles((d, n) -> n.endsWith(".jar")))
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(sep));
            if (localCp.length() > 0) {
                cp = cp + sep + localCp;
            }
        }
        List<String> arguments = new ArrayList<>();
        arguments.addAll(List.of("java", "-cp", cp, "javax0.jamal.cmd.JamalMain"));
        commandLineOptions.entrySet().stream().map(e -> "" + e.getKey() + "=" + e.getValue());
        builder.command(arguments.toArray(String[]::new))
            .directory(new File("."));
        Process process = builder.start();
        process.getInputStream().transferTo(System.out);
        int exitCode = process.waitFor();
    }