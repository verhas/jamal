package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.util.CellReference;

@Name("xls:range")
public class Range implements Macro, Scanner.WholeInput {

    public enum Direction {
        horizontal, vertical
    }

    public enum Level {
        single, multi
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet range_parops
        final var range = new ParopsRange(scanner, processor);
        // {%@snip rangedef_parops%}
        final var direction = scanner.enumeration(Direction.class).defaultValue(Direction.horizontal);
        // * You can specify either `horizontal` or `vertical` direction.
        // The default is `horizontal`.
        // The direction specifies how the range is traversed.
        // If the direction is `horizontal`, then the range is traversed row by row, like `A1,A2,A3,B1,B2,B3,C1,C2,C3`.
        // If the direction is `vertical`, then the range is traversed column by column, like `A1,B1,C1,A2,B2,C2,A3,B3,C3`.
        final var reverse = scanner.bool(null, "reverse");
        // * If the `reverse` parameter is `true`, then the range is traversed in reverse order.
        final var separatorDef = scanner.str("$forsep", "separator", "sep").defaultValue(",");
        // * The `$forsep` (aliased as `separator` or `sep`) parameter specifies the string inserted between the cell references.
        // The default is `,`.
        final var subSeparatorDef = scanner.str("$forsubsep", "subseparator", "subsep").defaultValue("|");
        // * The `$forsubsep` (aliased as `subseparator` or `subsep`) parameter specifies the string inserted between the cell references in a multi-level range.
        // The default is `|`.
        // Note that the default separator and subseparator are exactly the values that the core macro `for` uses by default.
        final var level = scanner.enumeration(Level.class).defaultValue(Level.single);
        // * The `single` or `multi` parameter specifies how the range is provided.
        // The default is `single`.
        // In this case, each cell is an individual element.
        // If `multi` is specified, then each row (if the direction is `horizontal`) or each column (if the direction is `vertical`) is an element.
        // These are separated by the subseparator.
        // That way they can be used in a `for` loop with multiple loop variables.
        // end snippet
        scanner.done();

        range.init(true);
        final var sheet = range.sheet().getSheetName();
        final var list = new StringBuilder();

        class Appender {
            final String sep;
            boolean z = false;

            void reset() {
                z = false;
            }

            void append() {
                if (z) {
                    list.append(sep);
                }
                z = true;
            }

            private Appender(String sep) {
                this.sep = sep;
            }
        }
        final var separator = new Appender(separatorDef.get());
        final var subSeparator = new Appender(level.get(Level.class) == Level.single ? separatorDef.get() : subSeparatorDef.get());
        if (reverse.is()) {
            switch (direction.get(Direction.class)) {
                case horizontal:
                    for (int j = range.bottom(); j >= range.top(); j--) {
                        separator.append();
                        subSeparator.reset();
                        for (int i = range.right(); i >= range.left(); i--) {
                            subSeparator.append();
                            list.append(new CellReference(sheet, j, i, false, false).formatAsString());
                        }
                    }
                    break;
                case vertical:
                    for (int i = range.right(); i >= range.left(); i--) {
                        separator.append();
                        subSeparator.reset();
                        for (int j = range.bottom(); j >= range.top(); j--) {
                            subSeparator.append();
                            list.append(new CellReference(sheet, j, i, false, false).formatAsString());
                        }
                    }
                    break;
            }
        } else {
            switch (direction.get(Direction.class)) {
                case horizontal:
                    for (int j = range.top(); j <= range.bottom(); j++) {
                        separator.append();
                        subSeparator.reset();
                        for (int i = range.left(); i <= range.right(); i++) {
                            subSeparator.append();
                            list.append(new CellReference(sheet, j, i, false, false).formatAsString());
                        }
                    }
                    break;
                case vertical:
                    for (int i = range.left(); i <= range.right(); i++) {
                        separator.append();
                        subSeparator.reset();
                        for (int j = range.top(); j <= range.bottom(); j++) {
                            subSeparator.append();
                            list.append(new CellReference(sheet, j, i, false, false).formatAsString());
                        }
                    }
                    break;
            }
        }
        return list.toString();
    }
}
