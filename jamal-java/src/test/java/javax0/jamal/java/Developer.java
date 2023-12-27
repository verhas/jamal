package javax0.jamal.java;

import java.net.URL;

public class Developer extends Xml {
    public Developer() {
        super();
    }

    public Developer roles(String... roles) {
        for (final var role : roles) {
            add("roles", Xml.tagValue("role", role));
        }
        return this;
    }

    public Developer id(String id) {
        add(path("id"), id);
        return this;
    }

    public Developer name(String name) {
        add("name", name);
        return this;
    }

    public Developer timezone(String timezone) {
        add("timezone", timezone);
        return this;
    }

    public Developer timezone(int tz) {
        final String timezone;
        if( tz > 0 ) {
            timezone = "GMT+" + tz;
        } else if( tz < 0 ) {
            timezone = "GMT" + tz;
        } else {
            timezone = "GMT";
        }
        add("timezone", timezone);
        return this;
    }

    public Developer organization(String organization) {
        add("organization", organization);
        return this;
    }

    public Developer email(String email) {
        add("email", email);
        return this;
    }

    public Developer url(URL url) {
        add("url", url.toString());
        return this;
    }

    public Developer organizationUrl(URL organizationUrl) {
        add("organizationUrl", organizationUrl.toString());
        return this;
    }

    public Developer property(String name, String value) {
        add("properties", tagValue(name, value));
        return this;
    }
}
