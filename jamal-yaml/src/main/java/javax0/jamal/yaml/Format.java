package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.yaml.snakeyaml.DumperOptions;

public class Format implements Macro, InnerScopeDependent, Scanner.WholeInput {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet formatOptions
        final var allowUnicode = scanner.bool(null, "allowUnicode");
        // specify whether to emit non-ASCII printable Unicode characters.
        final var canonical = scanner.bool(null, "canonical");
        // force the emitter to produce a canonical YAML document.
        final var explicitEnd = scanner.bool(null, "explicitEnd");
        // force to add `...` at the end of the Yaml data
        final var explicitStart = scanner.bool(null, "explicitStart");
        // force to ass `---` at the start of the yaml data
        final var prettyFlow = scanner.bool(null, "prettyFlow");
        // instruct the output to follow pretty flow
        final var splitLines = scanner.bool(null, "splitLines");
        // instruct the output to split too long lines
        final var defaultFlowStyle = scanner.str(null, "defaultFlowStyle", "flowStyle").optional();
        // the flow style can be `FLOW`, `BLOCK` or `AUTO`
        final var defaultScalarStyle = scanner.str(null, "defaultScalarStyle", "scalarStyle").optional();
        // the scalar style can be `DOUBLE_QUOTED`, `SINGLE_QUOTED`, `LITERAL`, `FOLDED`, or `PLAIN`,
        final var lineBreak = scanner.str(null, "lineBreak").optional();
        // the output line break can be `WIN`, `MAC`, or `UNIX`
        final var indent = scanner.number(null, "indent").optional();
        // sets the indentation size, should be max 10
        final var indicatorIndent = scanner.number(null, "indicatorIndent").optional();
        // set the number of white-spaces to use for the sequence indicator '-'
        final var width = scanner.number(null, "width").optional();
        // sets the desired width
        // end snippet

        scanner.done();

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
