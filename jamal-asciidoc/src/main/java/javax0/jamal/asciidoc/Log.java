package javax0.jamal.asciidoc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * A simple logger that writes to a file. It is used to log different events of the asciidoctor preprocessor into a file.
 * With that you can see when the preprocessor is called and what it does:
 *
 * <ul>
 * <li>when does it use already calculated values and
 * <li>when it uses the last cached value.
 * </ul>
 */
class Log {
    final String fileName;
    final boolean on;
    final int instance;

    Log(final String fileName, final boolean on, final int instance) {
        this.fileName = fileName;
        this.on = on;
        this.instance = instance;
    }

    void info(final String message) {
        final var when = LocalDateTime.now();
        if (on) {
            try {
                Files.writeString(Paths.get(fileName + ".log"),
                        String.format("%s [%d:%d:%s:%08X] %s\n",
                                when,
                                instance,
                                Thread.currentThread().getId(),
                                Thread.currentThread().getName(),
                                this.hashCode(),
                                message),
                        StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace(); // there is not much we can do here
            }
        }
    }
}
