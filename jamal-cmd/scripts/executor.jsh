import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

    File LOCAL_CACHE = new File(".jamal/cache/.jar");

    void execute() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String sep = System.getProperty("path.separator");
        String cp = String.join(sep,classPath);
        List<String> arguments = new ArrayList<>();
        arguments.addAll(List.of("java", "-cp", cp, "javax0.jamal.cmd.JamalMain"));
        arguments.addAll(commandLineOptions.entrySet().stream().map(e -> "" + e.getKey() + "=" + e.getValue()).collect( Collectors.toSet()));
        System.out.println("EXECUTING");
        for( String a : arguments){
            System.out.println(a);
        }
        builder.command(arguments.toArray(String[]::new))
            .directory(new File("."));
        Process process = builder.start();
        process.getInputStream().transferTo(System.out);
        int exitCode = process.waitFor();
    }