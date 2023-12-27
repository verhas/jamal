package javax0.jamal.java;

import java.net.URL;

public class Organization extends Xml {

    Organization() {
        super();
    }

    public Organization name(String name) {
        add(path("organization", "name"), name);
        return this;
    }

    public Organization url(URL url) {
        add(path("organization", "url"), url.toString());
        return this;
    }
}
