import javax0.jamal.api.ResourceReader;

import javax0.jamal.maven.input.MavenInput;


module jamal.maven.input {
    requires jamal.api;
    requires jamal.tools;
    requires mavenDownloader;
    exports javax0.jamal.maven.input;
    uses ResourceReader;
    provides ResourceReader with MavenInput;
}