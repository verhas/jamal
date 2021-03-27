package javax0.jamal.test.tools.params;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;
import org.junit.jupiter.api.Assertions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParamsTestSupport {

    private final Processor processor;
    private final Map<String, Params.Param<?>> params = new LinkedHashMap<>();

    private ParamsTestSupport(Processor processor) {
        this.processor = processor;
    }

    private String keyDefinition;

    public static ParamsTestSupport keys(String s) {
        final var it = new ParamsTestSupport(new javax0.jamal.engine.Processor("{", "}"));
        it.keyDefinition = s;
        final String[] declarations = s.split(",", -1);
        for (final var declaration : declarations) {
            final String[] part = declaration.split(":", -1);
            final var name = part[0].split("\\|", -1);
            final var type = part[1];
            final String defaultValue;
            if (part.length > 2) {
                defaultValue = part[2];
            } else {
                defaultValue = null;
            }
            final var holder = Params.holder(name);
            switch (type) {
                case "I":
                    if (defaultValue != null)
                        holder.orElseInt(Integer.parseInt(defaultValue));
                    holder.asInt();
                    break;
                case "S":
                    if (defaultValue != null)
                        holder.orElse(defaultValue);
                    holder.asString();
                    break;
                case "B":
                    holder.asBoolean();
                    break;
                case "L":
                    holder.asList();
                    break;
                default:
                    throw new IllegalArgumentException("Type cannot be " + type);
            }
            it.params.put(name[0], holder);
        }
        return it;
    }

    private String processed = "";

    public ParamsTestSupport process(String s) throws BadSyntax {
        processor.process(Input.makeInput(s));
        processed = s + "\n";
        return this;
    }

    private Character terminal = '\n';
    private Character start = null;

    public ParamsTestSupport between(String seps) {
        Objects.nonNull(seps);
        this.start = seps.charAt(0);
        this.terminal = seps.charAt(1);
        return this;
    }

    public ParamsTestSupport startWith(char start) {
        this.start = start;
        return this;
    }

    public ParamsTestSupport endWith(char terminal) {
        this.terminal = terminal;
        return this;
    }

    private String result;
    private Exception exception = null;

    public ParamsTestSupport input(String s) {
        try {
            final var sut = Params.using(processor).from(() -> "test environment");
            if (start != null) sut.startWith(start);
            if (terminal != null) sut.endWith(terminal);
            sut.keys(params.values().toArray(Params.Param[]::new)).parse(Input.makeInput(s));
            final var sb = new StringBuilder();
            sb.append(keyDefinition);
            sb.append("\ninput:\n").append(processed).append(s).append("\nresult:");
            for (final var e : params.entrySet()) {
                sb.append("\n");
                sb.append(e.getKey()).append('=');
                final var value = e.getValue();
                if (value.get() instanceof String) {
                    sb.append('"').append(
                        ((String) value.get())
                            .replaceAll("\"", "\\\\\"")
                            .replaceAll("\n", "\\\\n"))
                        .append('"');
                    continue;
                }
                if (value.get() instanceof Integer) {
                    sb.append(value.get());
                    continue;
                }
                if (value.get() instanceof Boolean) {
                    sb.append("(boolean)").append(value.get());
                    continue;
                }
                if (value.get() instanceof List) {
                    sb.append('[').append(String.join(",", (List<String>) value.get())).append(']');
                    continue;
                }
                sb.append('(')
                    .append(value.get().getClass().getSimpleName())
                    .append(')').append(value.get().toString());
            }
            this.result = sb.toString();
        } catch (BadSyntax bs) {
            result = null;
            exception = bs;
        }
        return this;
    }

    public void throwsUp(String... s) {
        Assertions.assertNotNull(exception);
    }

    public void results(String s) throws Exception {
        if (exception != null) {
            throw exception;
        }
        Assertions.assertEquals(s, result);
    }

}
