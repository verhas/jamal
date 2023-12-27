package javax0.jamal.java.testmacros;

import javax0.jamal.java.Pom;

import java.net.URL;

import static javax0.jamal.java.Dependency.dependency;
import static javax0.jamal.java.DistributionType.repo;
import static javax0.jamal.java.Exclusion.exclusion;
import static javax0.jamal.java.License.MIT;
import static javax0.jamal.java.TextTags.filter;
import static javax0.jamal.java.TextTags.finalName;
import static javax0.jamal.java.Xml.path;

public class MyPom extends Pom {

    public MyPom() throws Exception {
        super(".");
    }

    public static void main(String[] args) throws Exception {
        new MyPom().build();
    }

    public String build() throws Exception {

//{@import res:pom.jim}{@java:dsl}

        coordinates("myGroup:myArtifact:1.0.0");
        parent("myGroup:myParent:1.0.0").relativePath("../myParent");
        modules(dirsWith("pom.java"));
        packaging("pom");
        description("This is a test project");
        dependencies(
                dependency("org.junit.jupiter:junit-jupiter-api:5.9.0:test")
                        .exclusions("grp:arti", exclusion("grp:arti")),
                "org.junit.jupiter:junit-jupiter-api:5.9.1",
                dependency().groupId("org.junit.jupiter").artifactId("junit-jupiter-api").version("5.7.0").JAR().TEST(),
                dependency("org.junit.jupiter:junit-jupiter-api::test").version("5.7.0").JAR()
        );
        dependencies("gid:aid:ver:scope:classifier:type");
        organization("javax0").url(new URL("http://javax0.com"));
        to(path("dependencies"), under("dependency"),
                add("groupId", "tag-groupId",
                        "artifactId", "tag-artifactId",
                        "version", "tag-version",
                        "scope", "tag-scope",
                        "classifier", "tag-classifier",
                        "type", "tag-type"));

        url(new URL("http://example.com"));
        license(MIT().distribution(repo));
        developer("PV").name("Peter Verhas")
                .email("peter@verhas.com")
                .organization("N/A")
                .roles("developer")
                .timezone(+1);
build(finalName("myPom"),filters(filter("")));
        return format();
    }

}
