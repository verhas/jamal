package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;

public class UserDefinedMacro implements javax0.jamal.api.UserDefinedMacro {
    final private String[] parameters;

    public String getId() {
        return id;
    }

    final private String id;
    final private String content;

    public UserDefinedMacro(String id, String content, String... parameters) throws BadSyntax {
        this.id = id;
        this.content = content;
        this.parameters = parameters;
        ensureSafety();
    }

    private void ensureSafety() throws BadSyntax {
        for (int i = 0; i < parameters.length; i++) {
            for (int j = 0; j < parameters.length; j++) {
                if (i != j) {
                    if (parameters[i].contains(parameters[j])) {
                        throw new BadSyntax();
                    }
                }
            }
        }
    }

    public String evaluate(String... actualValues) throws BadSyntax {
        if (actualValues.length != parameters.length) {
            throw new BadSyntax();
        }
        var root = new TextSegment(null, content);
        for (int i = 0; i < actualValues.length; i++) {
            for (Segment segment = root; segment != null; segment = segment.next()) {
                segment.split(parameters[i], actualValues[i]);
            }
        }
        final var output = new StringBuilder(segmentsLengthSum(root));
        for (Segment segment = root; segment != null; segment = segment.next()) {
            output.append(segment.content());
        }
        return output.toString();
    }

    private int segmentsLengthSum(TextSegment root) {
        int size = 0;
        for (Segment segment = root; segment != null; segment = segment.next()) {
            size += segment.content().length();
        }
        return size;
    }
}
