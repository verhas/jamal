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
            final var klass = Compiler.java().from("module-info.java","module myModule {\n" +
                    "    uses javax0.jamal.api.Macro;\n" +
                    "    requires jamal.api;" +
                    "    exports javax0.jamal.java.testmacros;\n" +
                    "}").from(in.toString()).compile().load();
            if (klass instanceof Macro) {
                final var macro = (Macro) klass;
                processor.getRegister().define(macro);
                return "";
            } else {
                throw new BadSyntax("The class does not implement the Macro interface");
            }
        } catch (ClassNotFoundException e) {
            throw new BadSyntax("There is a problem with the class name in the source code", e);
        } catch (Compiler.CompileException e) {
            throw new BadSyntax("The Java code is erroneous", e);
        }
    }

    @Override
    public String getId() {
        return "java:source";
    }
}
