package javax0.jamal.maven.load;

import javax0.jamal.api.Processor;

import java.util.Properties;

class JamalVersion {

    static String get() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        return version.getProperty("version");
    }
}
