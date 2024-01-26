package javax0.jamal.java;

public class Extension extends Xml {

    public Extension() {
        super("dependency");
    }

    private Extension(CharSequence coordinate) {
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
    }

    public static Extension extension(CharSequence coordinates) {
        return new Extension(coordinates);
    }

    public static Extension extension() {
        return new Extension();
    }

    public Extension groupId(CharSequence groupId) {
        add(path("extension", "groupId"), groupId);
        return this;
    }

    public Extension artifactId(CharSequence artifactId) {
        add(path("extension", "artifactId"), artifactId);
        return this;
    }

    public Extension version(CharSequence version) {
        add(path("extension", "version"), version);
        return this;
    }
}
