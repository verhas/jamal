package javax0.jamal.java;
public class Build extends Xml {


    public void sourceDirectory(String sourceDirectory) {
        add("sourceDirectory", sourceDirectory);
    }

    public void scriptSourceDirectory(String scriptSourceDirectory) {
        add("scriptSourceDirectory", scriptSourceDirectory);
    }

    public void testSourceDirectory(String testSourceDirectory) {
        add("testSourceDirectory", testSourceDirectory);
    }

    public void outputDirectory(String outputDirectory) {
        add("outputDirectory", outputDirectory);
    }

    public void testOutputDirectory(String testOutputDirectory) {
        add("testOutputDirectory", testOutputDirectory);
    }

    public void extensions (String extensions ) {
        add("extensions ", extensions );
    }

    public Build extensions(CharSequence... extensions) {
        for (var extension : extensions) {
            if (!(extension instanceof Extension)) {
                extension = Extension.extension(extension);
            }
            add("extensions", extension);
        }
        return this;
    }
}

