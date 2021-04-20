package javax0.jamal.doclet;

import com.sun.source.doctree.DocTree;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@jamal This is {@define a=processed}{a} as Jamal macros.
 * <p>
 * When we run this processing the current working directory is {#code {@io:cwd}} }
 */
public class JamalTaglet implements Taglet {

    private static final String TAGLET_NAME = "jamal";
    private static final int TEXT_START = TAGLET_NAME.length() + "{@ ".length();

    /**
     * Create the taglet. The taglet uses a single processor instance. When the processor instance is created the
     * ServiceLoader is supposed to load all the macro files, which are provided by different modules. It seems, from
     * experience that it does not.
     * <p>
     * As a patch
     */
    public JamalTaglet() {
    }

    private Processor getProcessor(){
        if( processor == null ){
            this.processor = new javax0.jamal.engine.Processor(open, close);
        }
        return processor;
    }

    @Override
    public Set<Location> getAllowedLocations() {
        return Set.of(Location.OVERVIEW, Location.MODULE, Location.PACKAGE,
            Location.TYPE, Location.CONSTRUCTOR, Location.METHOD, Location.FIELD);
    }

    /**
     *
     * This is before the first Jamal tag.
     *
     * @jamal here we have another Jamal macro part
     *
     * @jamal Jamal can be used as am online as well as a block tag {#code {@io:cwd}}
     *
     * The current working directory is {#code {@io:cwd}}
     * @return {@code true} ... always
     *
     * This is after the return tag
     *
     */
    @Override
    public boolean isInlineTag() {
        return true;
    }

    public boolean isBlockTag() {
        return true;
    }

    @Override
    public String getName() {
        return TAGLET_NAME;
    }

    private Processor processor;
    private Reporter reporter;
    private String sourceRoot;
    private String open;
    private String close;

    private void warning(String message) {
        if (reporter != null) {
            reporter.print(Diagnostic.Kind.ERROR, message);
        }
    }

    private void note(String message) {
        if (reporter != null) {
            reporter.print(Diagnostic.Kind.NOTE, message);
        }
    }

    @Override
    public void init(DocletEnvironment env, Doclet doclet) {
        Taglet.super.init(env, doclet);
        if (doclet instanceof JamalDoclet) {
            this.reporter = ((JamalDoclet) doclet).reporter;
            this.sourceRoot = ((JamalDoclet) doclet).sourceRoot;
        } else {
            try {
                acquireDocletField(doclet, "reporter");
                acquireDocletField(doclet, "sourceRoot");
                acquireDocletField(doclet, "open");
                acquireDocletField(doclet, "close");

                final var sourceRoot = doclet.getClass().getDeclaredField("sourceRoot");
                sourceRoot.setAccessible(true);
                this.sourceRoot = (String) sourceRoot.get(doclet);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                this.reporter = null;
            }
        }
        if (sourceRoot == null) {
            warning("Option '" + JamalDoclet.SOURCE_ROOT_OPTION + "' is not defined. Macros using files will not work.");
        } else {
            note("Option '" + JamalDoclet.SOURCE_ROOT_OPTION + "' is '" + sourceRoot + "'.");
        }
    }

    private void acquireDocletField(Doclet doclet, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        final var source = doclet.getClass().getDeclaredField(fieldName);
        source.setAccessible(true);
        final var target = this.getClass().getDeclaredField(fieldName);
        target.setAccessible(true);
        target.set(this, source.get(doclet));
        note("'"+fieldName+"' is " + source.get(doclet));
    }

    private Map<String, String> memoize = new HashMap<>();

    @Override
    public String toString(List<? extends DocTree> tags, Element element) {
        while (element.getKind() != ElementKind.CLASS &&
            element.getEnclosingElement().getKind() == ElementKind.CLASS) {
            element = element.getEnclosingElement();
        }
        final var sourceFile = sourceRoot + "/" + (element.toString().replaceAll("\\.", "/")) + ".java";
        if (tags.size() == 0) {
            return null;
        }
        final var sb = new StringBuilder();
        for (final var tag : tags) {
            final var text = tag.toString();
            if (text.startsWith("{@")) {
                sb.append(text, TEXT_START, text.length() - 1);
            } else {
                sb.append(text, TEXT_START - 1, text.length());
            }
        }
        return memoizedProcess(sb.toString(), sourceFile);
    }

    private boolean reportRelativeLineNumbersFirstOnlyOnce = true;

    private String memoizedProcess(String sb, String sourceFile) {
        if (memoize.containsKey(sb)) {
            return memoize.get(sb);
        }
        String result;
        try {
            result = getProcessor().process(new Input(sb, new Position(sourceFile)));
        } catch (BadSyntax badSyntax) {
            warning(badSyntax.getMessage());
            if (reportRelativeLineNumbersFirstOnlyOnce) {
                warning("Line numbers in error messages are relative to the start of the @jamal tag");
                reportRelativeLineNumbersFirstOnlyOnce = false;
            }
            result = sb;
        }
        memoize.put(sb, result);
        return result;
    }
}
