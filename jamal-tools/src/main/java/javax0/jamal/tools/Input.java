package javax0.jamal.tools;

import javax0.jamal.api.LineReference;

/**
 * A simple implementation of the {@link javax0.jamal.api.Input} interface utilizing two fields.
 */
public class Input implements javax0.jamal.api.Input {
    private StringBuilder input;
    private String reference;
    private int lineNumber;

    public Input() {
        input = new StringBuilder();
        reference = null;
        lineNumber = 1;
    }

    public Input(StringBuilder input, String reference) {
        this.input = input;
        this.reference = reference;
        lineNumber = 1;
    }

    public Input(String input, String reference) {
        this.input = new StringBuilder(input);
        this.reference = reference;
        lineNumber = 1;
    }

    public Input(String input) {
        this.input = new StringBuilder(input);
        reference = null;
    }

    @Override
    public int getLine() {
        return lineNumber;
    }

    @Override
    public void stepLine() {
        lineNumber++;
    }

    @Override
    public StringBuilder getSB() {
        return input;
    }

    @Override
    public LineReference getLineReference() {
        return new LineReference(getReference(), getLine());
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return (input == null ? "null" : input.toString());
    }

}
