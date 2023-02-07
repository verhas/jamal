package javax0.jamal.java;

import com.javax0.sourcebuddy.Compiler;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.lang.invoke.MethodHandles;

public class JavaSourceMacro implements Macro {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        try {
            final var className = Compiler.getBinaryNameFromSource(in.toString());
            final var macro = Compiler.java()
                    .from("module-info", "module myModule {\n" +
                            "    uses javax0.jamal.api.Macro;\n" +
                            "    requires jamal.api;" +
                            "    exports javax0.jamal.java.testmacros;\n" +
                            "}")
                    .from(in.toString())
                    .compile()
                    .load(Compiler.LoaderOption.REVERSE)//even if there is a class with the same name in the classpath
                    .newInstance(className, Macro.class);
            processor.getRegister().define(macro);
            return "";
        } catch (Exception e) {
            throw new BadSyntax("There is a problem with the class in the source code", e);
        }
    }

    @Override
    public String getId() {
        return "java:source";
    }
}
