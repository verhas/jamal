package javax0.jamal.api;

public interface Closer {

    interface ProcessorAware {
        void set(Processor processor);
    }

    interface OutputAware {
        void set(Input output);
    }
}
