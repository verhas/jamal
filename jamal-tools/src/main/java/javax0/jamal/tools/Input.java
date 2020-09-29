package javax0.jamal.tools;

import javax0.jamal.api.Position;

/**
 * A simple implementation of the {@link javax0.jamal.api.Input} interface utilizing four fields.
 */
public class Input implements javax0.jamal.api.Input {
    private final StringBuilder input;
    private final String file;
    private int line;
    private int column;

    public static Input makeInput() {
        return new Input();
    }

    public Input() {
        this("");
    }

    public static Input makeInput(String string) {
        return new Input(string);
    }

    public Input(String string) {
        this(string, null);
    }

    public static Input makeInput(StringBuilder input, Position ref) {
        return new Input(input, ref);
    }

    public static Input makeInput(String input, Position ref) {
        return new Input(input, ref);
    }

    public Input(StringBuilder input, Position ref) {
        this.input = input;
        this.file = ref == null ? null : ref.file;
        line = ref == null ? 1 : ref.line;
        column = ref == null ? 1 : ref.column;
    }

    public Input(String input, Position ref) {
        this.input = new StringBuilder(input);
        this.file = ref == null ? null : ref.file;
        line = ref == null ? 1 : ref.line;
        column = ref == null ? 1 : ref.column;
    }


    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void stepLine() {
        line++;
        column = 1;
    }

    @Override
    public void stepColumn() {
        column++;
    }


    @Override
    public StringBuilder getSB() {
        return input;
    }

    @Override
    public Position getPosition() {
        return new Position(getReference(), getLine(), getColumn());
    }

    @Override
    public String getReference() {
        return file;
    }

    @Override
    public String toString() {
        return (input == null ? "null" : input.toString());
    }

}
