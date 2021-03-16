package javax0.jamal.engine.debugger;

public interface Debugger extends AutoCloseable{
    void setInput(int level, CharSequence input);

    void setAfter(int level, CharSequence input, CharSequence output);

    void setStart(CharSequence macro);

    void close();
}
