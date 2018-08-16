package javax0.jamal.engine;

public class Input implements javax0.jamal.api.Input {
    private StringBuilder input;
    private String reference;

    public Input() {
    }

    public Input(StringBuilder input, String reference) {
        this.input = input;
        this.reference = reference;
    }

    public Input(StringBuilder input) {
        this.input = input;
    }

    @Override
    public StringBuilder getInput() {
        return input;
    }

    @Override
    public void setInput(StringBuilder input) {
        this.input = input;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }
}
