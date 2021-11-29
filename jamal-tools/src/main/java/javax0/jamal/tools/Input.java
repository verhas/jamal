package javax0.jamal.tools;

import javax0.jamal.api.Position;

/**
 * A simple implementation of the {@link javax0.jamal.api.Input} interface utilizing four fields.
 */
public class Input implements javax0.jamal.api.Input {
    private final StringBuilder input;
    private final Position pos;


    /**
     * Create an empty input, which may also serve as an output where the characters are collected.
     */
    public Input() {
        this("");
    }

    /**
     * Create an empty input with the specified parent.
     *
     * @param parent the parent of the new input
     */
    public Input(Position parent) {
        this("", new Position(null, 1, 1, parent));
    }

    /**
     * Create an input from the string builder with the given position.
     * The string builder is directly used to create the input and will be consumed as the input is consumed.
     *
     * @param input the character of the input initially
     * @param pos   the position of the input initially
     */
    public Input(StringBuilder input, Position pos) {
        this.input = input;
        this.pos = new Position(pos);
    }

    /**
     * Creates a new input from the characters of the string with the given position.
     *
     * @param input the character of the input
     * @param pos   the position of the input initially
     */
    public Input(String input, Position pos) {
        this(new StringBuilder(input), pos);
    }


    /**
     * Create new input from the characters of te string with null position.
     *
     * @param string the characters of the new input
     */
    public Input(String string) {
        this(string, null);
    }

    /**
     * Create an empty input, which may also serve as an output where the characters are collected.
     *
     * @return the new input
     */
    public static Input makeInput() {
        return new Input();
    }

    /**
     * Create an empty input with the specified parent.
     *
     * @param parent the parent of the new input
     * @return the new input
     */
    public static Input makeInput(Position parent) {
        return new Input(parent);
    }

    /**
     * Create a new input from the characters of the string with null position information.
     *
     * @param string the characters of the new input
     * @return the new input
     */
    public static Input makeInput(String string) {
        return new Input(string);
    }


    /**
     * Create an input from the string builder with the given position.
     * The string builder is directly used to create the input and will be consumed as the input is consumed.
     *
     * @param input the character of the input initially
     * @param pos   the position of the input initially
     * @return the new input
     */
    public static Input makeInput(StringBuilder input, Position pos) {
        return new Input(input, pos);
    }

    /**
     * Creates a new input from the characters of the string with the given position.
     *
     * @param input the character of the input
     * @param pos   the position of the input initially
     * @return the new input
     */
    public static Input makeInput(String input, Position pos) {
        return new Input(input, pos);
    }

    /**
     * Make a new input from the characters of the input given as an argument. The new input inherits the position
     * of the argument and clones it (any change in the created input will not alter the position of the original
     * input).
     *
     * @param input the input from which the characters and the position are used
     * @return the new input.
     */
    public static Input makeInput(javax0.jamal.api.Input input) {
        return new Input(input.toString(), input.getPosition());
    }


    @Override
    public int getLine() {
        return pos.line;
    }

    @Override
    public int getColumn() {
        return pos.column;
    }

    @Override
    public void stepLine() {
        pos.line++;
        pos.column = 1;
    }

    @Override
    public void stepColumn() {
        pos.column++;
    }


    @Override
    public StringBuilder getSB() {
        return input;
    }

    /**
     * Return a clone of the position of the input.
     *
     * @return the position copy.
     */
    @Override
    public Position getPosition() {
        return pos.clone();
    }

    /**
     * Get the file name of the position of the input. This is called {@code getReference()} because this file name is
     * the reference file name to calculate the absolute path of a file specified as relative file name. The relative
     * file names are always relative to the input in which they are specified.
     *
     * @return the file name of the position of the input
     */
    @Override
    public String getReference() {
        return pos.file;
    }

    @Override
    public String toString() {
        return (input == null ? "null" : input.toString());
    }

}
