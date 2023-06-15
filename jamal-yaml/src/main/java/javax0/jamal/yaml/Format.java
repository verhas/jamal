package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import org.yaml.snakeyaml.DumperOptions;

import static javax0.jamal.tools.Params.holder;

public class Format implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        // snippet formatOptions
        final var allowUnicode = holder(null, "allowUnicode").asBoolean();
        // specify whether to emit non-ASCII printable Unicode characters.
        final var canonical = holder(null, "canonical").asBoolean();
        // force the emitter to produce a canonical YAML document.
        final var explicitEnd = holder(null, "explicitEnd").asBoolean();
        // force to add `...` at the end of the Yaml data
        final var explicitStart = holder(null, "explicitStart").asBoolean();
        // force to ass `---` at the start of the yaml data
        final var prettyFlow = holder(null, "prettyFlow").asBoolean();
        // instruct the output to follow pretty flow
        final var splitLines = holder(null, "splitLines").asBoolean();
        // instruct the output to split too long lines
        final var defaultFlowStyle = holder(null, "defaultFlowStyle", "flowStyle").asString().orElseNull();
        // the flow style can be `FLOW`, `BLOCK` or `AUTO`
        final var defaultScalarStyle = holder(null, "defaultScalarStyle", "scalarStyle").asString().orElseNull();
        // the scalar style can be `DOUBLE_QUOTED`, `SINGLE_QUOTED`, `LITERAL`, `FOLDED`, or `PLAIN`,
        final var lineBreak = holder(null, "lineBreak").asString().orElseNull();
        // the output line break can be `WIN`, `MAC`, or `UNIX`
        final var indent = holder(null, "indent").asInt().orElseNull();
        // sets the indentation size, should be max 10
        final var indicatorIndent = holder(null, "indicatorIndent").asInt().orElseNull();
        final var width = holder(null, "width").asInt().orElseNull();
        // sets the desired width
        // end snippet

        Scan.using(processor).from(this).tillEnd().keys(
            allowUnicode, canonical, explicitEnd, explicitStart, prettyFlow,
            splitLines, defaultFlowStyle, defaultScalarStyle, lineBreak,
            indent, indicatorIndent, width
        ).parse(in);

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
