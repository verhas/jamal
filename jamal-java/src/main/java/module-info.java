import javax0.jamal.api.Macro;
import javax0.jamal.java.Download;

module jamal.java {
    exports javax0.jamal.java;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires mavenDownloader;
    provides Macro with Download;
}