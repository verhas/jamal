package javax0.jamal.java;

public enum LicenseType {
    MIT("MIT License",
            "https://opensource.org/licenses/MIT"),
    GPL(
            "GNU General Public License version 3",
            "https://www.gnu.org/licenses/gpl-3.0.en.html"),
    Apache("Apache License, Version 2.0",
            "https://www.apache.org/licenses/LICENSE-2.0"),
    BSD("BSD 3-Clause \"New\" or \"Revised\" License",
            "https://opensource.org/licenses/BSD-3-Clause"),
    Eclipse("Eclipse Public License 1.0",
            "https://www.eclipse.org/legal/epl-v10.html");
    final String name;
    final String url;

    LicenseType(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
