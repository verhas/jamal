package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.ScannerTools;

public class Pos implements Macro, Scanner {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        var pos = in.getPosition();
        final var scanner = newScanner(in, processor);
        // snippet pos_options
        final var top = scanner.bool(null, "top");
        // will instruct the macro to use the location no of the top level.
        // It is the same as the current file if there were no imports or includes.
        // This option cannot be used together with `parent`, `all` or `up`
        final var parent = scanner.bool(null, "parent");
        // will use the location of the `include` or `import` macro that was used to include or import the current file.
        // This option cannot be used together with `top`, `all` or `up`
        final var all = scanner.bool(null, "all");
        // list all the locations in the hierarchy from the current to the top level.
        // The locations will be separated by a comma `,` or by the string specified in the option `sep`.
        // This option cannot be used together with `top`, `parent` or `up`
        final var format = scanner.str(null, "format").defaultValue("%f:%l:%c");
        // specifies the format of the location.
        // The format can be any string, and the formatting escape sequences `%f`, `%l` and `%l` are placeholders for the name of the file, line and column.
        // The default is `%f:%l:%c`.
        // This is also changed when the `.file`, `.line` or `.column` ending is used in the macro.
        // These cannot be used together with the `format` macro.
        // They are the short forms for `format=%f`, `format=%l`, and `format=%c`.
        // The format is also used with the option `all`.
        final var up = scanner.number(null, "up").defaultValue(0);
        // specifies the number of steps up in the hierarchy.
        // `up=0` is the default.
        // `up=1` is the same as `parent`.
        // This option cannot be used together with `top`, `parent` or `all`
        final var sep = scanner.str(null, "sep").defaultValue(",");
        // specifies the string used to concatenate the locations when the option `all` is used.
        // The default value is a comma `,`.
        // This option must be used together with the option `all`.
        // end snippet
        scanner.done();
        final var what = in.toString().trim();
        final String formatString;
        if (!what.isEmpty() && format.isPresent()) {
            throw new BadSyntax("Cannot use 'format' for 'pos' when there is .file, .line, or .column");
        }
        switch (what) {
            case ".file":
                formatString = "%f";
                break;
            case ".line":
                formatString = "%l";
                break;
            case ".column":
                formatString = "%c";
                break;
            default:
                formatString = format.get();
                break;
        }

        ScannerTools.badSyntax(this).whenParameters(top, parent, all, up).multipleArePresent();

        if (sep.isPresent() && !all.is()) {
            processor.logger().log(System.Logger.Level.WARNING, pos, "There is no reason to specify separator unless the parameter 'all' is also used for the macro 'pos'");
        }

        if (all.is()) {
            final var sb = new StringBuilder();
            for (var p = pos; p != null; p = p.parent) {
                final var thisPos = formatString
                        .replace("%f", String.valueOf(p.file))
                        .replace("%l", String.valueOf(p.line))
                        .replace("%c", String.valueOf(p.column));
                if (sb.length() > 0) {
                    sb.append(sep.get());
                }
                sb.append(thisPos);
            }
            return sb.toString();
        }

        if (up.isPresent()) {
            for (int counter = up.get(); counter > 0; counter--) {
                pos = pos.parent;
                if (pos == null) {
                    throw new BadSyntax(String.format("The value %d for up in macro pos is too large, there are not so many levels of hierarchy.", up.get()));
                }
            }
        }

        if (parent.is()) {
            pos = pos.parent;
        }

        if (top.is()) {
            if (pos != null) {
                while (pos.parent != null) {
                    pos = pos.parent;
                }
            }
        }
        if (pos == null) {
            return "";
        }
        return formatString
                .replace("%f", String.valueOf(pos.file))
                .replace("%l", String.valueOf(pos.line))
                .replace("%c", String.valueOf(pos.column));
    }
}
