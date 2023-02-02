package javax0.jamal.asciidoc;

import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;

public class StringColletingLogger implements Processor.Logger {
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void log(final System.Logger.Level level, final Position pos, final String format, final String... params) {
        sb.append(level.toString()).append(": ").append(String.format(format, (Object[]) params)).append("\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
