package javax0.jamal.java;

public class Parent extends Xml {
    public Parent() {
        super();
    }

    public Parent(final String coordinates) {
        super();
        coordinates(coordinates);
    }

    public Parent groupId(String groupId) {
        add("groupId", groupId);
        return this;
    }

    public Parent artifactId(String artifactId) {
        add("artifactId", artifactId);
        return this;
    }

    public Parent version(String version) {
        add("version", version);
        return this;
    }

    public Parent relativePath(String relativePath) {
        add("relativePath", relativePath);
        return this;
    }

    public Parent coordinates(String coords) {
        final var s = coords.split(":");
        if (s.length > 0 && !s[0].isEmpty()) {
            groupId(s[0]);
        }
        if (s.length > 1 && !s[1].isEmpty()) {
            artifactId(s[1]);
        }
        if (s.length > 2 && !s[2].isEmpty()) {
            version(s[2]);
        }
        return this;
    }
}
