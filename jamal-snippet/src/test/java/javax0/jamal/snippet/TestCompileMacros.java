package javax0.jamal.snippet;

import javax0.jamal.api.Position;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class TestCompileMacros {

    @Test
    void testCompileMacros() throws Exception {
        TestThat.theInput("{@java:sources classes=target/classes}" +
                "{!@for [evalist] $field in ({@java:fields class=\"name ~ /^javax0.jamal.snippet.tools.MethodTool$/\"})=" +
                "  {$field :modifiers} {$field :type} {$field} <-- class {$field :class}\n" +
                "}\n"
        ).atPosition(new Position("./README.adic.jam")).results("  protected final  java.util.concurrent.atomic.AtomicInteger argCounter <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "  private  java.util.function.Function<String,String> decorator <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "  private  boolean isInterface <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "  private  boolean isPublic <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "  protected  java.lang.reflect.Method method <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "  private  String type <-- class javax0.jamal.snippet.tools.MethodTool\n" +
                "\n");
    }

    void testClassReading() throws IOException {
        try (final var files = Files.walk(new File("target/classes").toPath())) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .map(p -> {
                        try {
                            return Files.readAllBytes(p);
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(b -> b != null)
                    .forEach(b -> {
                        final var cr = new ClassReader(b);
                        System.out.printf("Class: %s\n", cr.getClassName());
                        Arrays.stream(cr.getInterfaces()).forEach(i -> System.out.printf("  Interface: %s\n", i));
                        System.out.printf("  Super: %s\n", cr.getSuperName());
                        ClassNode cn = new ClassNode(Opcodes.ASM4);
                        cr.accept(cn, 0);
                        cn.methods.forEach(m -> System.out.printf("    Method: %s\n", m.name));
                        cn.fields.forEach(f -> System.out.printf("    Field: %s\n", f.name));
                        cn.innerClasses.forEach(ic -> System.out.printf("    Inner: %s\n", ic.name));

                    });
        }

    }
}

