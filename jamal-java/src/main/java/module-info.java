import javax0.jamal.api.Macro;
import javax0.jamal.java.JavaSourceMacro;
import javax0.jamal.java.LoadMavenJar;

module jamal.java {
    exports javax0.jamal.java;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires mavenDownloader;
    requires SourceBuddy;
    provides Macro with LoadMavenJar, JavaSourceMacro;
}