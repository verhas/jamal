package javax0.jamal.doclet;

import com.sun.source.doctree.DocTree;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@jamal This is {%@define a=processed%}{%a%} as Jamal macros. {@io:print hahóóóóó I am io module from a taglet}
 * <p>
 * }
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
        this.processor = new javax0.jamal.engine.Processor("{%", "%}");
    }

    @Override
    public Set<Location> getAllowedLocations() {
        return Set.of(Location.OVERVIEW, Location.MODULE, Location.PACKAGE,
            Location.TYPE, Location.CONSTRUCTOR, Location.METHOD, Location.FIELD);
    }

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

    private final Processor processor;

    @Override
    public void init(DocletEnvironment env, Doclet doclet) {
        Taglet.super.init(env, doclet);
    }

    @Override
    public String toString(List<? extends DocTree> tags, Element element) {
        if (tags.size() == 0) {
            return null;
        }
        final var sb = new StringBuilder();
        for (final var tag : tags) {
            final var text = tag.toString();
            sb.append(text, TEXT_START, text.length() - 1);
        }
        try {
            return processor.process(new Input(sb.toString()));
        } catch (BadSyntax badSyntax) {
            throw new IllegalArgumentException("There was an error processing the Jamal tag '" + sb + "'", badSyntax);
        }
    }

    /**
     * Register this Taglet.
     *
     * @param tags the map to register this tag to.
     */
    public static void register(Map tags) {
        JamalTaglet tag = new JamalTaglet();
        Taglet t = (Taglet) tags.get(tag.getName());
        if (t != null) {
            tags.remove(tag.getName());
        }
        tags.put(tag.getName(), tag);
    }
}
