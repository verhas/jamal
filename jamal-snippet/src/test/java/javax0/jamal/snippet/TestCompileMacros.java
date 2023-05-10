package javax0.jamal.snippet;

import javax0.jamal.api.Position;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

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
}
