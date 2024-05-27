package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.util.CellReference;

@Name("xls:move")
public class Move implements Macro, Scanner {

    public enum Direction {
        up, down, left, right,
        north, south, west, east,
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var cellDef = new ParopsCell(scanner);
        final var directionDef = scanner.enumeration(Direction.class).defaultValue(Direction.right);
        final var amount = scanner.number(null, "amount", "n", "N").defaultValue(1);
        scanner.done();

        InputHandler.skipWhiteSpaces(in);
        final var wb = cellDef.getWorkbook(processor);
        final var cell = cellDef.getCell(in.toString(), this, wb);
        final var sheet = cell.getSheet().getSheetName();
        var row = cell.getRowIndex();
        var col = cell.getColumnIndex();
        final var n = amount.get();
        switch (directionDef.get(Direction.class)) {
            case up:
            case north:
                row -= n;
                break;
            case down:
            case south:
                row += n;
                break;
            case left:
            case west:
                col -= n;
                break;
            case right:
            case east:
                col += n;
                break;
        }
        BadSyntax.when(row < 0 || col < 0, "The cell is out of the sheet");
        return new CellReference(sheet, row, col, false, false).formatAsString();
    }
}
