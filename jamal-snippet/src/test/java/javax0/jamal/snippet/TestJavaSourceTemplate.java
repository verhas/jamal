package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Collectors;

public class TestJavaSourceTemplate {


    private static final String testFile = "target/TestJavaSourceTemplates/Test.java";
    private static final Path path = Paths.get(testFile);

    private void createTestFile(final String content) throws IOException {
        if (Files.exists(path.getParent())) {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(path.getParent())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(File::exists)
                    .forEach(File::delete);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void assertResult(final String fileContent) throws IOException {
        final var result = Files.readString(path);
        if (!fileContent.equals(result)) {
            final var sb = new StringBuilder();
            sb.append("Expected: ").append(fileContent.chars().mapToObj(c -> String.format("%02X", c)).collect(Collectors.joining(" ")));
            sb.append("\n");
            sb.append("Actual: ").append(result.chars().mapToObj(c -> String.format("%02X", c)).collect(Collectors.joining(" ")));
            sb.append("\n");
            Assertions.fail(sb.toString());
        }
    }

    @Test
    @DisplayName("The header remains, and the header text is appended after the template text")
    void testSimplestTemplate() throws Exception {
        createTestFile("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"id1\" template=\"testTemplate\">\n" +
                "// this text will remain and will also be put after this line without the leading // characters\n" +
                "This text will be deleted and replaced with the 'generated' code\n" +
                "//</editor-fold>\n" +
                "");
        TestThat.theInput(
                "{@java:template debug path=\"target/TestJavaSourceTemplates\" template=\"testTemplate\"\n" +
                        "this will be inserted as new value from the template\n" +
                        "}\n" +
                        "").results("//target/TestJavaSourceTemplates/Test.java\n" +
                "this will be inserted as new value from the template\n" +
                " this text will remain and will also be put after this line without the leading // characters\n" +
                "\n");
        assertResult("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"id1\" template=\"testTemplate\">\n" +
                "// this text will remain and will also be put after this line without the leading // characters\n" +
                "this will be inserted as new value from the template\n" +
                " this text will remain and will also be put after this line without the leading // characters\n" +
                "\n" +
                "//</editor-fold>\n");
    }

    @Test
    @DisplayName("A simple setter/getter template")
    void testSimpleTemplate() throws Exception {
        createTestFile("public class Main {\n" +
                "//<editor-fold id=\"id1\" template=\"setters\">\n" +
                "// {%setter /int/jakab%}\n" +
                "// {%setter /String/hakan%}\n" +
                "This text will be deleted and replaced with the 'generated' code\n" +
                "//</editor-fold>\n" +
                "}");
        TestThat.theInput("" +
                "{%@java:template debug path=\"target/TestJavaSourceTemplates\" template=\"setters\"\n" +
                "{%@define setter($type,$id)=\nprivate $type $id;\n" +
                "public $type get_$id(){\n" +
                "  return $id;\n" +
                "}\n" +
                "%}\n" +
                "%}\n" +
                "").usingTheSeparators("{%", "%}").results("//target/TestJavaSourceTemplates/Test.java\n" +
                "\n" +
                " \n" +
                "private int jakab;\n" +
                "public int get_jakab(){\n" +
                "  return jakab;\n" +
                "}\n" +
                "\n" +
                " \n" +
                "private String hakan;\n" +
                "public String get_hakan(){\n" +
                "  return hakan;\n" +
                "}\n" +
                "\n" +
                "\n");
        assertResult("public class Main {\n" +
                "//<editor-fold id=\"id1\" template=\"setters\">\n" +
                "// {%setter /int/jakab%}\n" +
                "// {%setter /String/hakan%}\n" +
                "\n" +
                " \n" +
                "private int jakab;\n" +
                "public int get_jakab(){\n" +
                "  return jakab;\n" +
                "}\n" +
                "\n" +
                " \n" +
                "private String hakan;\n" +
                "public String get_hakan(){\n" +
                "  return hakan;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "//</editor-fold>\n" +
                "}");
    }

    @Test
    @DisplayName("Editor folds can also be generated")
    void testNestedEditorFoldTemplate() throws Exception {
        createTestFile("package javax0.jamal.templated;\n" +
                "\n" +
                "public class Templated1 {\n" +
                "\n" +
                "    //<editor-fold template=\"attributes\" id=\"Templated1\">\n" +
                "    // //<editor-fold desc=\"generated fields\">\n" +
                "    // {%field :String:firstName%}\n" +
                "    // {%field :String:lastName%}\n" +
                "    // {%field :int:age%}\n" +
                "    // {%field :String:address%}\n" +
                "    // {%field :String:city%}\n" +
                "    // {%field :String:state%}\n" +
                "    // {%field :String:zipCode%}\n" +
                "    // {%field :String:phoneNumber%}\n" +
                "    // {%field :String:email%}\n" +
                "    // {%field :String:github%}\n" +
                "    // {%field :String:linkedin%}\n" +
                "    // {%field :String:website%}\n" +
                "    // {%field :String:objective%}\n" +
                "    // {%field :String:education%}\n" +
                "    // {%field :String:experience%}\n" +
                "    // {%field :String:skills%}\n" +
                "    // {%field :String:projects%}\n" +
                "    // {%field :String:interests%}\n" +
                "    // {%field :String:references%}\n" +
                "    // //</editor-fold>\n" +
                "\n" +
                "    //</editor-fold>\n" +
                "}\n");
        TestThat.theInput(
                "{%@java:template debug path=\"target/TestJavaSourceTemplates\" template=\"attributes\"\n" +
                        "{%@define field($type,$name)=\n" +
                        "  private $type $name;\n" +
                        "  public $type get{%@case:cap $name%}(){\n" +
                        "    return $name;\n" +
                        "  }\n" +
                        "  public void set{%@case:cap $name%}($type $name){\n" +
                        "    this.$name=$name;\n" +
                        "  }%}\n" +
                        "%}").usingTheSeparators("{%", "%}").results("//target/TestJavaSourceTemplates/Test.java\n" +
                "\n" +
                " //<editor-fold desc=\"generated fields\">\n" +
                " \n" +
                "  private String firstName;\n" +
                "  public String getFirstName(){\n" +
                "    return firstName;\n" +
                "  }\n" +
                "  public void setFirstName(String firstName){\n" +
                "    this.firstName=firstName;\n" +
                "  }\n" +
                " \n" +
                "  private String lastName;\n" +
                "  public String getLastName(){\n" +
                "    return lastName;\n" +
                "  }\n" +
                "  public void setLastName(String lastName){\n" +
                "    this.lastName=lastName;\n" +
                "  }\n" +
                " \n" +
                "  private int age;\n" +
                "  public int getAge(){\n" +
                "    return age;\n" +
                "  }\n" +
                "  public void setAge(int age){\n" +
                "    this.age=age;\n" +
                "  }\n" +
                " \n" +
                "  private String address;\n" +
                "  public String getAddress(){\n" +
                "    return address;\n" +
                "  }\n" +
                "  public void setAddress(String address){\n" +
                "    this.address=address;\n" +
                "  }\n" +
                " \n" +
                "  private String city;\n" +
                "  public String getCity(){\n" +
                "    return city;\n" +
                "  }\n" +
                "  public void setCity(String city){\n" +
                "    this.city=city;\n" +
                "  }\n" +
                " \n" +
                "  private String state;\n" +
                "  public String getState(){\n" +
                "    return state;\n" +
                "  }\n" +
                "  public void setState(String state){\n" +
                "    this.state=state;\n" +
                "  }\n" +
                " \n" +
                "  private String zipCode;\n" +
                "  public String getZipCode(){\n" +
                "    return zipCode;\n" +
                "  }\n" +
                "  public void setZipCode(String zipCode){\n" +
                "    this.zipCode=zipCode;\n" +
                "  }\n" +
                " \n" +
                "  private String phoneNumber;\n" +
                "  public String getPhoneNumber(){\n" +
                "    return phoneNumber;\n" +
                "  }\n" +
                "  public void setPhoneNumber(String phoneNumber){\n" +
                "    this.phoneNumber=phoneNumber;\n" +
                "  }\n" +
                " \n" +
                "  private String email;\n" +
                "  public String getEmail(){\n" +
                "    return email;\n" +
                "  }\n" +
                "  public void setEmail(String email){\n" +
                "    this.email=email;\n" +
                "  }\n" +
                " \n" +
                "  private String github;\n" +
                "  public String getGithub(){\n" +
                "    return github;\n" +
                "  }\n" +
                "  public void setGithub(String github){\n" +
                "    this.github=github;\n" +
                "  }\n" +
                " \n" +
                "  private String linkedin;\n" +
                "  public String getLinkedin(){\n" +
                "    return linkedin;\n" +
                "  }\n" +
                "  public void setLinkedin(String linkedin){\n" +
                "    this.linkedin=linkedin;\n" +
                "  }\n" +
                " \n" +
                "  private String website;\n" +
                "  public String getWebsite(){\n" +
                "    return website;\n" +
                "  }\n" +
                "  public void setWebsite(String website){\n" +
                "    this.website=website;\n" +
                "  }\n" +
                " \n" +
                "  private String objective;\n" +
                "  public String getObjective(){\n" +
                "    return objective;\n" +
                "  }\n" +
                "  public void setObjective(String objective){\n" +
                "    this.objective=objective;\n" +
                "  }\n" +
                " \n" +
                "  private String education;\n" +
                "  public String getEducation(){\n" +
                "    return education;\n" +
                "  }\n" +
                "  public void setEducation(String education){\n" +
                "    this.education=education;\n" +
                "  }\n" +
                " \n" +
                "  private String experience;\n" +
                "  public String getExperience(){\n" +
                "    return experience;\n" +
                "  }\n" +
                "  public void setExperience(String experience){\n" +
                "    this.experience=experience;\n" +
                "  }\n" +
                " \n" +
                "  private String skills;\n" +
                "  public String getSkills(){\n" +
                "    return skills;\n" +
                "  }\n" +
                "  public void setSkills(String skills){\n" +
                "    this.skills=skills;\n" +
                "  }\n" +
                " \n" +
                "  private String projects;\n" +
                "  public String getProjects(){\n" +
                "    return projects;\n" +
                "  }\n" +
                "  public void setProjects(String projects){\n" +
                "    this.projects=projects;\n" +
                "  }\n" +
                " \n" +
                "  private String interests;\n" +
                "  public String getInterests(){\n" +
                "    return interests;\n" +
                "  }\n" +
                "  public void setInterests(String interests){\n" +
                "    this.interests=interests;\n" +
                "  }\n" +
                " \n" +
                "  private String references;\n" +
                "  public String getReferences(){\n" +
                "    return references;\n" +
                "  }\n" +
                "  public void setReferences(String references){\n" +
                "    this.references=references;\n" +
                "  }\n" +
                " //</editor-fold>\n");
        assertResult("package javax0.jamal.templated;\n" +
                "\n" +
                "public class Templated1 {\n" +
                "\n" +
                "    //<editor-fold template=\"attributes\" id=\"Templated1\">\n" +
                "    // //<editor-fold desc=\"generated fields\">\n" +
                "    // {%field :String:firstName%}\n" +
                "    // {%field :String:lastName%}\n" +
                "    // {%field :int:age%}\n" +
                "    // {%field :String:address%}\n" +
                "    // {%field :String:city%}\n" +
                "    // {%field :String:state%}\n" +
                "    // {%field :String:zipCode%}\n" +
                "    // {%field :String:phoneNumber%}\n" +
                "    // {%field :String:email%}\n" +
                "    // {%field :String:github%}\n" +
                "    // {%field :String:linkedin%}\n" +
                "    // {%field :String:website%}\n" +
                "    // {%field :String:objective%}\n" +
                "    // {%field :String:education%}\n" +
                "    // {%field :String:experience%}\n" +
                "    // {%field :String:skills%}\n" +
                "    // {%field :String:projects%}\n" +
                "    // {%field :String:interests%}\n" +
                "    // {%field :String:references%}\n" +
                "    // //</editor-fold>\n" +
                "\n" +
                " //<editor-fold desc=\"generated fields\">\n" +
                " \n" +
                "  private String firstName;\n" +
                "  public String getFirstName(){\n" +
                "    return firstName;\n" +
                "  }\n" +
                "  public void setFirstName(String firstName){\n" +
                "    this.firstName=firstName;\n" +
                "  }\n" +
                " \n" +
                "  private String lastName;\n" +
                "  public String getLastName(){\n" +
                "    return lastName;\n" +
                "  }\n" +
                "  public void setLastName(String lastName){\n" +
                "    this.lastName=lastName;\n" +
                "  }\n" +
                " \n" +
                "  private int age;\n" +
                "  public int getAge(){\n" +
                "    return age;\n" +
                "  }\n" +
                "  public void setAge(int age){\n" +
                "    this.age=age;\n" +
                "  }\n" +
                " \n" +
                "  private String address;\n" +
                "  public String getAddress(){\n" +
                "    return address;\n" +
                "  }\n" +
                "  public void setAddress(String address){\n" +
                "    this.address=address;\n" +
                "  }\n" +
                " \n" +
                "  private String city;\n" +
                "  public String getCity(){\n" +
                "    return city;\n" +
                "  }\n" +
                "  public void setCity(String city){\n" +
                "    this.city=city;\n" +
                "  }\n" +
                " \n" +
                "  private String state;\n" +
                "  public String getState(){\n" +
                "    return state;\n" +
                "  }\n" +
                "  public void setState(String state){\n" +
                "    this.state=state;\n" +
                "  }\n" +
                " \n" +
                "  private String zipCode;\n" +
                "  public String getZipCode(){\n" +
                "    return zipCode;\n" +
                "  }\n" +
                "  public void setZipCode(String zipCode){\n" +
                "    this.zipCode=zipCode;\n" +
                "  }\n" +
                " \n" +
                "  private String phoneNumber;\n" +
                "  public String getPhoneNumber(){\n" +
                "    return phoneNumber;\n" +
                "  }\n" +
                "  public void setPhoneNumber(String phoneNumber){\n" +
                "    this.phoneNumber=phoneNumber;\n" +
                "  }\n" +
                " \n" +
                "  private String email;\n" +
                "  public String getEmail(){\n" +
                "    return email;\n" +
                "  }\n" +
                "  public void setEmail(String email){\n" +
                "    this.email=email;\n" +
                "  }\n" +
                " \n" +
                "  private String github;\n" +
                "  public String getGithub(){\n" +
                "    return github;\n" +
                "  }\n" +
                "  public void setGithub(String github){\n" +
                "    this.github=github;\n" +
                "  }\n" +
                " \n" +
                "  private String linkedin;\n" +
                "  public String getLinkedin(){\n" +
                "    return linkedin;\n" +
                "  }\n" +
                "  public void setLinkedin(String linkedin){\n" +
                "    this.linkedin=linkedin;\n" +
                "  }\n" +
                " \n" +
                "  private String website;\n" +
                "  public String getWebsite(){\n" +
                "    return website;\n" +
                "  }\n" +
                "  public void setWebsite(String website){\n" +
                "    this.website=website;\n" +
                "  }\n" +
                " \n" +
                "  private String objective;\n" +
                "  public String getObjective(){\n" +
                "    return objective;\n" +
                "  }\n" +
                "  public void setObjective(String objective){\n" +
                "    this.objective=objective;\n" +
                "  }\n" +
                " \n" +
                "  private String education;\n" +
                "  public String getEducation(){\n" +
                "    return education;\n" +
                "  }\n" +
                "  public void setEducation(String education){\n" +
                "    this.education=education;\n" +
                "  }\n" +
                " \n" +
                "  private String experience;\n" +
                "  public String getExperience(){\n" +
                "    return experience;\n" +
                "  }\n" +
                "  public void setExperience(String experience){\n" +
                "    this.experience=experience;\n" +
                "  }\n" +
                " \n" +
                "  private String skills;\n" +
                "  public String getSkills(){\n" +
                "    return skills;\n" +
                "  }\n" +
                "  public void setSkills(String skills){\n" +
                "    this.skills=skills;\n" +
                "  }\n" +
                " \n" +
                "  private String projects;\n" +
                "  public String getProjects(){\n" +
                "    return projects;\n" +
                "  }\n" +
                "  public void setProjects(String projects){\n" +
                "    this.projects=projects;\n" +
                "  }\n" +
                " \n" +
                "  private String interests;\n" +
                "  public String getInterests(){\n" +
                "    return interests;\n" +
                "  }\n" +
                "  public void setInterests(String interests){\n" +
                "    this.interests=interests;\n" +
                "  }\n" +
                " \n" +
                "  private String references;\n" +
                "  public String getReferences(){\n" +
                "    return references;\n" +
                "  }\n" +
                "  public void setReferences(String references){\n" +
                "    this.references=references;\n" +
                "  }\n" +
                " //</editor-fold>\n" +
                "\n" +
                "    //</editor-fold>\n" +
                "}\n");
    }

}
