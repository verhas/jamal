package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import org.yaml.snakeyaml.DumperOptions;

import static javax0.jamal.tools.Params.holder;

public class Format implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        // snippet formatOptions
        final var allowUnicode = holder(null, "allowUnicode").asBoolean();
        final var canonical = holder(null, "canonical").asBoolean();
        final var explicitEnd = holder(null, "explicitEnd").asBoolean();
        final var explicitStart = holder(null, "explicitStart").asBoolean();
        final var prettyFlow = holder(null, "prettyFlow").asBoolean();
        final var splitLines = holder(null, "splitLines").asBoolean();

        final var defaultFlowStyle = holder(null, "defaultFlowStyle", "flowStyle").asString().orElseNull();
        final var defaultScalarStyle = holder(null, "defaultScalarStyle", "scalarStyle").asString().orElseNull();
        final var lineBreak = holder(null, "lineBreak").asString().orElseNull();

        final var indent = holder(null, "indent").asInt().orElseNull();
        final var indicatorIndent = holder(null, "indicatorIndent").asInt().orElseNull();
        final var width = holder(null, "width").asInt().orElseNull();
        // end snippet

        Params.using(processor).keys(
            allowUnicode, canonical, explicitEnd, explicitStart, prettyFlow,
            splitLines, defaultFlowStyle, defaultScalarStyle, lineBreak,
            indent, indicatorIndent, width
        ).tillEnd().parse(in);

        final var options = YamlDumperOptions.defineExport(processor).getObject();
        options.setAllowUnicode(allowUnicode.is());
        options.setCanonical(canonical.is());
        options.setExplicitEnd(explicitEnd.is());
        options.setExplicitStart(explicitStart.is());
        options.setPrettyFlow(prettyFlow.is());
        options.setSplitLines(splitLines.is());

        if (defaultFlowStyle.get() != null) {
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.valueOf(defaultFlowStyle.get()));
        }
        if (defaultScalarStyle.get() != null) {
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.valueOf(defaultScalarStyle.get()));
        }
        if (lineBreak.get() != null) {
            options.setLineBreak(DumperOptions.LineBreak.valueOf(lineBreak.get()));
        }

        if (indent.isPresent()) {
            options.setIndent(indent.get());
        }
        if (indicatorIndent.isPresent()) {
            options.setIndicatorIndent(indicatorIndent.get());
        }
        if (width.isPresent()) {
            options.setWidth(width.get());
        }
        return "";
    }

    @Override
    public String getId() {
        return "yaml:format";
    }
}
