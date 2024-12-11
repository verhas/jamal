package com.javax0.jamal.maven;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;

public class JamalLogger {

    public static void log(final System.Logger.Level level, final Position pos, final String format, final String... params) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("jamal");
        final var msg = String.format(format, (Object[]) params) + (pos == null ? "" : " at " + pos.posFormat());
        switch (level) {
            case DEBUG:
                log.debug(msg);
                break;
            case INFO:
            case TRACE:
                log.info(msg);
                break;
            case WARNING:
                log.warn(msg);
                break;
            case ERROR:
                log.error(msg);
                break;
            default:
                log.info(msg);
                break;
        }
    }
}
