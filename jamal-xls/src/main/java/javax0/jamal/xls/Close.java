package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.io.Closeable;

public class Close implements Macro, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var nameSomethingOpen = in.toString().trim();
        final var optionalSomethingOpen = processor.getRegister().getUserDefined(nameSomethingOpen);
        BadSyntax.when(optionalSomethingOpen.isEmpty(), "The macro '%s' does not exist", in.toString().trim());
        final var somethingOpen = optionalSomethingOpen.get();
        try {
            if (somethingOpen instanceof Closeable) {
                ((Closeable) somethingOpen).close();
            } else if (somethingOpen instanceof AutoCloseable) {
                ((AutoCloseable) somethingOpen).close();
            } else BadSyntax.format("The macro '%s' is not closeable", in.toString().trim());
            return "";
        } catch (Exception e) {
            throw new BadSyntax("Exception while closing '"+nameSomethingOpen+"'", e);
        }
    }

    @Override
    public String getId() {
        return "xls:close";
    }
}
