package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This macro removes all the lines that start with {@code //} from the input.
 * <p>
 * This demonstrates how a macro can implement {@link #fetch(Processor, Input)} that alters the input before the
 * macro is evaluated.
 */
@Macro.Name("useComments")
public
class UseComments implements Macro, Macro.Escape {
    @Override
    public String evaluate(Input in, Processor processor) {
        return "";
    }

    @Override
    public String prefetch(Processor processor, Input input) throws BadSyntaxAt {
        return Macro.super.fetch(processor, input) + processor.getRegister().close();
    }

    @Override
    public String fetch(Processor processor, Input input) throws BadSyntaxAt {
        final var lines = input.toString().split("\n");
        int j = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("//")) {
                continue;
            }
            lines[j++] = lines[i];
        }
        input.replace(Arrays.stream(lines).limit(j).collect(Collectors.joining("\n")));
        return Macro.super.fetch(processor, input);
    }

}
