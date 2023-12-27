package javax0.jamal.java;

public class License extends Xml {
    private static final String PATH = "//project/licenses";
    private static final String LICENSE = PATH + "/license[last()]";

    License() {
        super("license");
    }

    public License name(String name) {
        add(path("license", "name"), name);
        return this;
    }

    public License url(String url) {
        add(path("license", "url"), url);
        return this;
    }

    public License distribution(String distribution) {
        add(path("license", "distribution"), distribution);
        return this;
    }

    public License distribution(DistributionType distribution) {
        return distribution(distribution.toString());
    }

    public License repo() {
        add(path("license", "distribution"), "repo");
        return this;
    }

    public License manual() {
        add(path("license", "distribution"), "manual");
        return this;
    }

    public License comments(String comments) {
        add(path("license", "comments"), comments);
        return this;
    }


    public static License MIT() {
        return new License().name("MIT License").url("https://opensource.org/licenses/MIT");
    }

    public static License GPL() {
        return new License().name("GNU General Public License version 3").url("https://www.gnu.org/licenses/gpl-3.0.en.html");
    }

    public static License Apache() {
        return new License().name("Apache License, Version 2.0").url("https://www.apache.org/licenses/LICENSE-2.0");
    }


    public static License BSD() {
        return new License().name("BSD 3-Clause \"New\" or \"Revised\" License").url("https://opensource.org/licenses/BSD-3-Clause");
    }

    public static License Eclipse() {
        return new License().name("Eclipse Public License 1.0").url("https://www.eclipse.org/legal/epl-v10.html");
    }
}
