package javax0.jamal.yaml;

import javax0.jamal.api.Ref;
import javax0.jamal.api.Xml;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Set;

public class YamlFactory {

    private static Set<Class> taggedClasses = Set.of(Ref.class, Xml.ATTR.class, Xml.TAG.class, Xml.CDATA.class, Xml.CDATATEXT.class, Xml.TEXT.class);

    public static Yaml newYaml() {
        Constructor constructor = new Constructor(new LoaderOptions());
        Representer representer = new Representer(new DumperOptions());
        for( final var clazz : taggedClasses ) {
            constructor.addTypeDescription(new TypeDescription(clazz, "!"+clazz.getSimpleName().toLowerCase()));
            representer.addClassTag(clazz, new Tag("!"+clazz.getSimpleName().toLowerCase()));
        }
        return new Yaml(constructor, representer);
    }

}
