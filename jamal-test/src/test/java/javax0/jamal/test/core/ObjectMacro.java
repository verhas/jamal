package javax0.jamal.test.core;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.Params;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class ObjectMacro implements Macro {
    public static class BBB implements UserDefinedMacro, ObjectHolder<Object> {
        final String id;
        final Object object;

        public BBB(String id, Object object) {
            this.id = id;
            this.object = object;
        }

        @Override
        public String evaluate(String... parameters) {
            return "";
        }

        @Override
        public int expectedNumberOfArguments() {
            return 0;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Object getObject() {
            return object;
        }
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var type = Params.holder(null, "type").asString();
        Params.using(processor).from(this).keys(type).between("()").parse(in);
        final Identified macro;
        switch (type.get()) {
            case "Array":
                macro = new BBB(in.toString().trim(), new String[]{"a", "b", "c"});
                break;
            case "Stream":
                macro = new BBB(in.toString().trim(), Stream.of("a", "b", "c"));
                break;
            case "Set":
                // test assertion needs the ordering, it is still a set though
                macro = new BBB(in.toString().trim(), new LinkedHashSet<>(List.of("a", "b", "c")));
                break;
            default:
                throw new BadSyntax("invalid format");
        }
        processor.getRegister().define(macro);
        return "";
    }
}
