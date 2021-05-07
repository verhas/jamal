package javax0.jamal.doclet;

import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class JamalDoclet extends StandardDoclet {
    Reporter reporter;
    public static final String SOURCE_ROOT_OPTION = "--source-root";
    String sourceRoot;
    public static final String OPEN_OPTION = "--macro-open";
    String open = "{";
    public static final String CLOSE_OPTION = "--macro-close";
    String close = "}";

    public JamalDoclet() {
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        super.init(locale, reporter);
        this.reporter = reporter;
    }

    @Override
    public Set<Option> getSupportedOptions() {
        final var optionSet = new HashSet<Option>(super.getSupportedOptions());
        optionSet.add(new JamalOption((x) -> sourceRoot = x, "Java source root path", Option.Kind.STANDARD, SOURCE_ROOT_OPTION));
        optionSet.add(new JamalOption((x) -> open = x, "opening string, default is {%", Option.Kind.STANDARD, OPEN_OPTION));
        optionSet.add(new JamalOption((x) -> close = x, "closing string, default is %}", Option.Kind.STANDARD, CLOSE_OPTION));
        return optionSet;
    }
}
