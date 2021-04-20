package javax0.jamal.doclet;

import jdk.javadoc.doclet.Doclet;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class JamalOption implements Doclet.Option {

    private final int argumentCount;
    private final String description;
    private final Kind kind;
    private final List<String> names;
    private final Consumer<String> valueConsumer;

    protected JamalOption(Consumer<String> valueConsumer, String description, Kind kind, String... names) {
        this.argumentCount = 1;
        this.description = description;
        this.kind = kind;
        this.names = Arrays.asList(names);
        this.valueConsumer = valueConsumer;
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getParameters() {
        return "";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        valueConsumer.accept(arguments.get(0));
        return true;
    }
}
