package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.Processor;
import org.yaml.snakeyaml.DumperOptions;

public class YamlDumperOptions implements Identified, ObjectHolder<DumperOptions> {
    public static final String DUMPER_OPTIONS_MACRO_ID = "`yaml_dumper_options";

    private final DumperOptions options = new DumperOptions();

    @Override
    public String getId() {
        return DUMPER_OPTIONS_MACRO_ID;
    }

    /**
     * This method is here on case some scripting wants to manipulate the options directly it is available through this
     * method.
     *
     * @return the DumperOptions object
     */
    @Override
    public DumperOptions getObject() {
        return options;
    }

    public static YamlDumperOptions get(Processor processor) {
        return (YamlDumperOptions)processor.getRegister().getUserDefined(DUMPER_OPTIONS_MACRO_ID).orElse(null);
    }

    public static YamlDumperOptions defineExport(Processor processor) throws BadSyntax {
        final var result = new YamlDumperOptions();
        processor.getRegister().define(result);
        processor.getRegister().export(result.getId());
        return result;
    }
}
