import javax0.jamal.api.Macro;

module jamal.maven.load {
    exports javax0.jamal.maven.load;
    requires jamal.api;
    requires jamal.tools;
    requires jamal.engine;
    requires mavenDownloader;
    provides Macro with LoadMavenJar;
}