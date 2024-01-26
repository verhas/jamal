package javax0.jamal.java;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static javax0.jamal.java.Dependency.dependency;

import static javax0.jamal.java.TextTags.filter;
import static javax0.jamal.java.TextTags.module;
import static javax0.jamal.java.Xml.path;
import static javax0.jamal.java.Xml.tagValue;

public class Pom {

    final protected Xml project;

    private final String CWD;

    public Pom(final String cwd) {
        CWD = cwd;
        project = new Xml();
        project.add("modelVersion", "4.0.0");
    }

    //<editor-fold id="typed_tags">

    public Parent parent() {
       var parent = project.get(Parent.class, "parent");
       if (parent != null) {
            return parent;
       }
       parent = new Parent();
       project.add("parent", parent);
       return parent;
    }

    public Organization organization() {
       var organization = project.get(Organization.class, "organization");
       if (organization != null) {
            return organization;
       }
       organization = new Organization();
       project.add("organization", organization);
       return organization;
    }

    public Prerequisites prerequisites() {
       var prerequisites = project.get(Prerequisites.class, "prerequisites");
       if (prerequisites != null) {
            return prerequisites;
       }
       prerequisites = new Prerequisites();
       project.add("prerequisites", prerequisites);
       return prerequisites;
    }

    public IssueManagement issueManagement() {
       var issueManagement = project.get(IssueManagement.class, "issueManagement");
       if (issueManagement != null) {
            return issueManagement;
       }
       issueManagement = new IssueManagement();
       project.add("issueManagement", issueManagement);
       return issueManagement;
    }

    public CiManagement ciManagement() {
       var ciManagement = project.get(CiManagement.class, "ciManagement");
       if (ciManagement != null) {
            return ciManagement;
       }
       ciManagement = new CiManagement();
       project.add("ciManagement", ciManagement);
       return ciManagement;
    }

    public DistributionManagement distributionManagement() {
       var distributionManagement = project.get(DistributionManagement.class, "distributionManagement");
       if (distributionManagement != null) {
            return distributionManagement;
       }
       distributionManagement = new DistributionManagement();
       project.add("distributionManagement", distributionManagement);
       return distributionManagement;
    }

    public DependencyManagement dependencyManagement() {
       var dependencyManagement = project.get(DependencyManagement.class, "dependencyManagement");
       if (dependencyManagement != null) {
            return dependencyManagement;
       }
       dependencyManagement = new DependencyManagement();
       project.add("dependencyManagement", dependencyManagement);
       return dependencyManagement;
    }

    public Build build() {
       var build = project.get(Build.class, "build");
       if (build != null) {
            return build;
       }
       build = new Build();
       project.add("build", build);
       return build;
    }

    public Reporting reporting() {
       var reporting = project.get(Reporting.class, "reporting");
       if (reporting != null) {
            return reporting;
       }
       reporting = new Reporting();
       project.add("reporting", reporting);
       return reporting;
    }


    //</editor-fold>
    //<editor-fold id="text_tags">

    public void modelVersion(String modelVersion) {
      project.add("modelVersion", modelVersion);
    }

    public void groupId(String groupId) {
      project.add("groupId", groupId);
    }

    public void artifactId(String artifactId) {
      project.add("artifactId", artifactId);
    }

    public void version(String version) {
      project.add("version", version);
    }

    public void packaging(String packaging) {
      project.add("packaging", packaging);
    }

    public void name(String name) {
      project.add("name", name);
    }

    public void description(String description) {
      project.add("description", description);
    }

    public void url(String url) {
      project.add("url", url);
    }

    public void inceptionYear(String inceptionYear) {
      project.add("inceptionYear", inceptionYear);
    }

    //</editor-fold>

    public Pom url(URL url) {
        project.add("url", url.toString());
        return this;
    }


    public Pom coordinates(String coords) {
        final var s = coords.split(":");
        if (s.length > 0 && !s[0].isEmpty()) {
            groupId(s[0]);
        }
        if (s.length > 1 && !s[1].isEmpty()) {
            artifactId(s[1]);
        }
        return this;
    }

    public Pom dependencyManagement(CharSequence... dependencies) {
        try {
            for (var dependency : dependencies) {
                if (!(dependency instanceof Dependency)) {
                    dependency = dependency(dependency);
                }
                project.add(path("dependencyManagement", "dependencies"), dependency);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Pom resource(CharSequence... elements) {
        for (var e : elements) {
            if (e.getClass() == String.class) {
                throw new IllegalArgumentException("You cannot have a string argument to a resource tag");
            }
            project.add(path("build", "resources", "resource"), e);
        }
        return this;
    }

    public Pom dependencies(CharSequence... dependencies) {
        for (var dependency : dependencies) {
            if (!(dependency instanceof Dependency)) {
                dependency = dependency(dependency);
            }
            project.add("dependencies", dependency);
        }
        return this;
    }

    public void license(String name, String url) {
        project.add("licenses", new License().name(name).url(url));
    }

    public void license(LicenseType lt) {
        project.add("licenses", new License().name(lt.name).url(lt.url));
    }

    public License license() {
        return new License();
    }

    public void license(License license) {
        licenses(license);
    }

    public void licenses(License... licenses) {
        for (var license : licenses) {
            project.add("licenses", license);
        }
    }

    public Developer developer(String id) {
        final var d = new Developer().id(id);
        project.add("developers", d);
        return d;
    }

    public Organization organization(String name) {
        final var org = new Organization().name(name);
        project.add("organization", org);
        return org;
    }

    public Parent parent(String coords) {
        final var parent = new Parent().coordinates(coords);
        project.add("parent", parent);
        return parent;
    }

    public String[] dirsWith(String file) throws IOException {
        final String normalizedCWD = CWD.startsWith("/") && File.separatorChar == '\\' ? CWD.substring(1) : CWD;
        return Files.list(Path.of(normalizedCWD).getParent()).filter(Files::isDirectory)
                .filter(p -> new File(p.toFile(), file).exists())
                .map(p -> p.getFileName().toString())
                .toArray(String[]::new);
    }

    public void modules(CharSequence... modules) {
        for (var module : modules) {
            if (!(module instanceof TextTags.Module)) {
                module = module(module);
            }
            project.add(path("modules"), module);
        }
    }

    public void properties(String... properties) {
        if (properties.length % 2 != 0) {
            throw new IllegalArgumentException("The number of properties must be even");
        }
        for (int i = 0; i < properties.length; i += 2) {
            project.add("properties", tagValue(properties[i], properties[i + 1]));
        }
    }

    public void build(Xml... tags) {
        for (var tag : tags) {
            if (tag != null) {
                project.add(path("build"), tag);
            }
        }
    }

    /**
     * <pre>
     * {@code
     * filters("filters/filter1.properties")
     * }
     * </pre>
     * <p>
     * Defines {@code *.properties} files that contain a list of properties that apply to resources which accept their settings (covered below).
     * In other words, the {@code "name=value"} pairs defined within the filter files replace {@code ${name}} strings within resources on build.
     * The example above defines the {@code filter1.properties} file under the {@code filters/} directory.
     * Maven's default filter directory is {@code ${project.basedir}/src/main/filters/}.
     * For a more comprehensive look at what filters are and what they can do, take a look at the quick start guide.
     *
     * @param filters the filters to add
     * @return {@code null} so that you can invoke the method as an argument to the method {@code buld()} which will,
     * then ignore the null values.
     */
    public Xml filters(CharSequence... filters) {
        for (var filter : filters) {
            if (!(filter instanceof TextTags.Filter)) {
                filter = filter(filter);
            }
            project.add(path("build", "filters"), filter);
        }
        return null;
    }

    /**
     * Format the XML and return it as a string.
     *
     * @return the formatted XML
     */
    public String format() {
        // create the top level XML containing the <project>...</project>
        final var xml = new Xml();
        xml.add("project", project);
        return format(xml.toString());
    }

    public static class Tag {
        private final String tag;

        public Tag(String tag) {
            this.tag = tag;
        }
    }


    public static Tag under(String tag) {
        return new Tag(tag);
    }

    public static String[] add(String... values) {
        return values;
    }

    /**
     * Add new elements to the path under the {@code tag}. The {@code tag} is the name of the tag that is added to the
     * path.
     *
     * @param path   the path to the elements
     * @param tag    under which the elements are added
     * @param values the key and value pairs of the elements
     */
    public void to(String[] path, Tag tag, String... values) {
        project.add(path, tagValue(tag.tag, null));
        final var newPath = Arrays.copyOf(path, path.length + 1);
        newPath[path.length] = tag.tag;
        to(newPath, values);
    }

    public void to(String[] path, String... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("The number of values must be even");
        }
        for (int i = 0; i < values.length; i += 2) {
            project.add(path, tagValue(values[i], values[i + 1]));
        }
    }


    private String format(String xml) {
        try {
            // Parse the XML into a document object
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));

            // Set up a transformer to convert the document to a string with indentation
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Set indent amount

            // Transform the document to a string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

            return stringWriter.toString();
        } catch (Exception e) {
            return xml;
        }
    }
}
