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
 *
 */
public class JamalTaglet implements Taglet {

    private static final String TAGLET_NAME = "jamal";
    private static final int TEXT_START = TAGLET_NAME.length() + "{@ ".length();

    /**
     * Create the taglet. The taglet uses a single processor instance. When the processor instance is created the
     * ServiceLoader is supposed to load all the macro files, which are provided by different modules. It seems, from
     * experience that it does not.
     */
    public JamalTaglet() {
    }

    private Link link;

    /**
     * Initialize the processor in case it was not initialized before. Also initialize the field {@link #link}, which
     * points to the instance of the {@code link} macro used to mimic the behavior of the {@code link} JavaDoc tag.
     */
    private void initProcessor() {
        if (processor == null) {
            processor = new javax0.jamal.engine.Processor(open, close);
            final var l = processor.getRegister().getMacro("link");
            if (l.isPresent() && l.get() instanceof Link) {
                link = (Link) l.get();
            }
        }
    }

    @Override
    public Set<Location> getAllowedLocations() {
        return Set.of(Location.OVERVIEW, Location.MODULE, Location.PACKAGE,
            Location.TYPE, Location.CONSTRUCTOR, Location.METHOD, Location.FIELD);
    }

    /**
     * @return {@code true} because the tag {@code jamal} can be used as an inline tag.
     */
    @Override
    public boolean isInlineTag() {
        return true;
    }

    /**
     * @return {@code true} because the tag {@code jamal} can be used as a block tag. It is not recommended, but it can.
     */
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
    private String open = "{";
    private String close = "}";

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

    /**
     * Copy the fields {@code reporter}, {@code sourceRoot}, {@code open} and {@code close} from the doclet to this
     * taglet. These fieds hold configuration options.
     *
     * @param env    the environment
     * @param doclet the executing doclet that uses this taglet
     */
    @Override
    public void init(DocletEnvironment env, Doclet doclet) {
        Taglet.super.init(env, doclet);
        /**
         * If future version of JavaDoc uses the same class loader to load the doclet and the taglet then this code
         * may work. As it is today using Java 16 only reflection works.
         */
        if (doclet instanceof JamalDoclet) {
            this.reporter = ((JamalDoclet) doclet).reporter;
            this.sourceRoot = ((JamalDoclet) doclet).sourceRoot;
            this.open = ((JamalDoclet) doclet).open;
            this.close = ((JamalDoclet) doclet).close;
        } else {
            try {
                acquireDocletField(doclet, "reporter");
                acquireDocletField(doclet, "sourceRoot");
                acquireDocletField(doclet, "open");
                acquireDocletField(doclet, "close");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                warning("Cannot get the configuration values from the doclet to thetaglet.");
                reporter = null;
                sourceRoot = null;
                open = "{";
                close = "}";
            }
        }
        if (sourceRoot == null) {
            warning("Option '" + JamalDoclet.SOURCE_ROOT_OPTION + "' is not defined. Macros using files will not work.");
        } else {
            note("Option '" + JamalDoclet.SOURCE_ROOT_OPTION + "' is '" + sourceRoot + "'.");
        }
    }

    /**
     * Copy the value of the field named {@code fieldName} from the {@code doclet} to the field of the same name in this
     * object.
     *
     * @param doclet    where the field is defined, has the same time and the same name as the field in this object
     * @param fieldName the name of the field to read reflectively and also the same name in this object to set the
     *                  value read from the other field, reflectively
     * @throws NoSuchFieldException   if there is no such field
     * @throws IllegalAccessException if you cannot read or set the field
     */
    private void acquireDocletField(Doclet doclet, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        final var source = doclet.getClass().getDeclaredField(fieldName);
        source.setAccessible(true);
        final var target = this.getClass().getDeclaredField(fieldName);
        target.setAccessible(true);
        target.set(this, source.get(doclet));
        note("'" + fieldName + "' is " + source.get(doclet));
    }

    private Map<String, String> memoize = new HashMap<>();

    /**
     * Convert the tags processing using Jamal. First the elements of the {@code tags} are copied together and then they
     * are processed using Jamal. Because JavaDoc assumes that processing a string is cheap and calls this method many
     * time for each and every taglet occurrence this method is memoized.
     * <p>
     * If there is an error in the Jamal processing it will report an error to JavaDoc and will include the source
     * unmodified (Jamal source) to the output.
     *
     * @param tags
     * @param element
     * @return
     */
    @Override
    public String toString(List<? extends DocTree> tags, Element element) {
        while (element.getKind() != ElementKind.CLASS &&
            element.getEnclosingElement().getKind() == ElementKind.CLASS) {
            element = element.getEnclosingElement();
        }
        final var sourceFile = sourceRoot + "/" + (element.toString().replaceAll("\\.", "/")) + ".java";
        initProcessor();
        link.currentClass = element.toString();
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
            initProcessor();
            result = processor.process(new Input(sb, new Position(sourceFile)));
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
