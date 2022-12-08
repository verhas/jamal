package javax0.jamal.asciidoc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

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
