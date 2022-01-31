package javax0.jamal;

import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JamalOutputStream extends FilterOutputStream {
    final Processor processor;
    final Position pos;

    /**
     * Creates an output stream that will filter the written bytes using Jamal.
     * The processor wil be created with the "default" macro opening and closing strings.
     *
     * @param out the underlying output stream to be assigned to
     *            the field {@code this.out} for later use, or
     *            {@code null} if this instance is to be
     *            created without an underlying stream.
     */
    public JamalOutputStream(final OutputStream out) {
        this(out, new javax0.jamal.engine.Processor());
    }

    /**
     * Same as {@link #JamalOutputStream(OutputStream out)} but with a {@link Processor}
     *
     * @param out       see {@link #JamalOutputStream(OutputStream out)}
     * @param processor the processor to be used for the filtering
     */
    public JamalOutputStream(final OutputStream out, Processor processor) {
        this(out, processor, new Position(null, 1));
    }

    /**
     * Same as {@link #JamalOutputStream(OutputStream out,Processor processor)} but with a {@link Position}
     *
     * @param out       see {@link #JamalOutputStream(OutputStream out,Processor processor)}
     * @param processor see {@link #JamalOutputStream(OutputStream out,Processor processor)}
     * @param pos       the position where the process starts. Usually a file name and the line number 1, like
     *                  {@code new Position("file.jamal", 1)}
     */
    public JamalOutputStream(final OutputStream out, Processor processor, Position pos) {
        super(out);
        this.processor = processor;
        this.pos = pos;
    }

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void write(int b) {
        sb.append((char) b);
    }

    @Override
    public void close() throws IOException {
        final var input = Input.makeInput(sb, pos);
        try {
            super.out.write(processor.process(input).getBytes(StandardCharsets.UTF_8));
        } catch (final Exception e) {
            throw new IOException(e);
        }
        super.close();
    }
}
