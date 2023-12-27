package javax0.jamal.java;

public class Exclusion extends Xml {

    public Exclusion() {
        super("exclusion");
    }

    public Exclusion groupId(CharSequence groupId) {
        add(path("exclusion", "groupId"), groupId);
        return this;
    }

    public Exclusion artifactId(CharSequence artifactId) {
        add(path("exclusion", "artifactId"), artifactId);
        return this;
    }

    public static Exclusion exclusion() {
        return new Exclusion();
    }
    public static Exclusion exclusion(String groupId, String artifactId) {
        return new Exclusion().artifactId(artifactId).groupId(groupId);
    }
    public static Exclusion exclusion(CharSequence coordinates) {
        final var coords = coordinates.toString().split(":");
        final var exclusion = new Exclusion();
        String groupId = null;
        String artifactId = null;
        if (coords.length > 0 && !coords[0].isEmpty()) {
            exclusion.groupId(coords[0]);
        }
        if (coords.length > 1 && !coords[1].isEmpty()) {
            exclusion.artifactId(coords[1]);
        }
        return exclusion;
    }

}
