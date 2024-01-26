//{@import res:pom.jim}{@java:dsl}
dependencyManagement().dependency("myGroup:myArtifact:1.0.0");
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
dependencyManagement(
        dependency("org.junit.jupiter:junit-jupiter-api:5.9.0:test")
                        .exclusions("grp:arti", exclusion("grp:arti")),
        "org.junit.jupiter:junit-jupiter-api:5.9.1",
dependency().groupId("org.junit.jupiter").artifactId("junit-jupiter-api").version("5.7.0").JAR().TEST(),
dependency("org.junit.jupiter:junit-jupiter-api::test").version("5.7.0").JAR());
resource(targetPath("pom.xml"), filtering(true),directory("abraka dabra"));
dependencies("gid:aid:ver:scope:classifier:type");
organization("javax0").url(new URL("http://javax0.com"));
to(path("dependencies"), under("dependency"),
        "groupId", "tag-groupId",
        "artifactId", "tag-artifactId",
        "version", "tag-version",
        "scope", "tag-scope",
        "classifier", "tag-classifier",
        "type", "tag-type");

url(new URL("http://example.com"));
license(MIT().distribution(repo));
developer("PV").name("Peter Verhas")
               .email("peter@verhas.com")
               .organization("N/A")
               .roles("developer")
               .timezone(1);
properties("key.zapp", "value");

filters("filter1", filter("filter2"));
build(defaultGoal("clean"), finalName("alma-ata"));

build().extensions(Extension.extension().groupId("extension group").artifactId("extension artifact").version("extension version"));