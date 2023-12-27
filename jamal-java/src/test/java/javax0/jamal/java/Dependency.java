package javax0.jamal.java;

public class Dependency extends Xml {

    public Dependency() {
        super("dependency");
    }

    private Dependency(CharSequence coordinate) {
        this();
        final var coords = ((String) coordinate).split(":");
        if (coords.length > 0 && !coords[0].isEmpty()) {
            groupId(coords[0]);
        }
        if (coords.length > 1 && !coords[1].isEmpty()) {
            artifactId(coords[1]);
        }
        if (coords.length > 2 && !coords[2].isEmpty()) {
            version(coords[2]);
        }
        if (coords.length > 3 && !coords[3].isEmpty()) {
            scope(coords[3]);
        }
        if (coords.length > 4 && !coords[4].isEmpty()) {
            classifier(coords[4]);
        }
        if (coords.length > 5 && !coords[5].isEmpty()) {
            type(coords[5]);
        }
    }

    public static Dependency dependency(CharSequence coordinates) {
        return new Dependency(coordinates);
    }

    public static Dependency dependency() {
        return new Dependency();
    }

    public Dependency groupId(CharSequence groupId) {
        add(path("dependency", "groupId"), groupId);
        return this;
    }

    public Dependency artifactId(CharSequence artifactId) {
        add(path("dependency", "artifactId"), artifactId);
        return this;
    }

    public Dependency version(CharSequence version) {
        add(path("dependency", "version"), version);
        return this;
    }

    public Dependency scope(CharSequence scope) {
        add(path("dependency", "scope"), scope);
        return this;
    }

    public Dependency TEST() {
        return scope("test");
    }

    public Dependency RUNTIME() {
        return scope("runtime");
    }

    public Dependency COMPILE() {
        return scope("compile");
    }

    public Dependency PROVIDED() {
        return scope("provided");
    }

    public Dependency SYSTEM() {
        return scope("system");
    }

    public Dependency IMPORT() {
        return scope("import");
    }


    public Dependency classifier(CharSequence classifier) {
        add(path("dependency", "classifier"), classifier);
        return this;
    }

    public Dependency type(CharSequence type) {
        add(path("dependency", "type"), type);
        return this;
    }

    public Dependency JAR() {
        return type("jar");
    }

    public Dependency POM(CharSequence type) {
        return type("pom");
    }

    public Dependency WAR(CharSequence type) {
        return type("war");
    }

    public Dependency EAR(CharSequence type) {
        return type("ear");
    }

    public Dependency ZIP(CharSequence type) {
        return type("zip");
    }

    public Dependency systemPath(CharSequence systemPath) {
        add(path("dependency", "systemPath"), systemPath);
        return this;
    }

    public Dependency optional() {
        add(path("dependency", "optional"), "true");
        return this;
    }

    public Dependency exclusions(CharSequence... exclusions) {
        for (var exclusion : exclusions) {
            if (!(exclusion instanceof Exclusion)) {
                exclusion = Exclusion.exclusion(exclusion);
            }
            add(path("dependency", "exclusions"), exclusion);
        }
        return this;
    }


}
