package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;
import javax0.jamal.xls.utils.WorkSheetUtils;
import javax0.jamal.xls.utils.WorkbookUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.util.Arrays;
import java.util.function.Consumer;

public class Set implements Macro, Scanner {

    public static final String XLS_SHEET = "xls:sheet";
    public static final String XLS_ROW = "xls:row";
    public static final String XLS_COL = "xls:col";

    public enum What {
        value, formula, format, comment, style, width, height
    }

    public enum Style {
        align,
        border, bottomBorder, topBorder, leftBorder, rightBorder,
        borderColor, bottomBorderColor, topBorderColor, leftBorderColor, rightBorderColor,
        fillPattern, fillBackgroundColor, fillForegroundColor,
        dataFormat, hidden, locked, rotation, shrinkToFit, verticalAlignment, wrapText,
        font, zoom

    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var workbook = scanner.str(null, "workbook", "wb").defaultValue(Open.XLS_WORKBOOK);
        final var sheet = scanner.str(XLS_SHEET, "sheet").defaultValue("");
        final var rowDef = scanner.str(XLS_ROW, "row").optional();
        final var colDef = scanner.str(XLS_COL, "col").optional();
        final var cellDef = scanner.str(null, "cell").optional();
        final var what = scanner.enumeration(What.class).defaultValue(What.value);
        final var as = scanner.enumeration(CellType.class).defaultValue(CellType.STRING);
        final var style = scanner.enumeration(Style.class).optional();
        final var author = scanner.str(null, "author").optional();
        scanner.done();

        BadSyntax.when(style.isPresent() && what.isPresent() && what.get(What.class) != What.style,
                "When setting style you cannot set anything else.");
        BadSyntax.when(author.isPresent() && what.get(What.class) != What.comment,
                "When setting author you must specify the comment.");
        BadSyntax.when(what.get(What.class) == What.style && !style.isPresent(),
                "When setting style you must specify the style type.");
        BadSyntax.when(cellDef.isPresent() && (rowDef.isPresent() || colDef.isPresent()),
                "When specifying cell reference you cannot use 'col' or 'row'.");
        BadSyntax.when(!cellDef.isPresent() && !(rowDef.isPresent() && colDef.isPresent()),
                "No cell specified, use either 'cell' or 'row' and 'col' to specify the cell.");

        InputHandler.skipWhiteSpaces(in);
        String ref = "";
        try {
            Cell cell;
            final var wb = WorkbookUtils.get(workbook.get(), processor);
            if (cellDef.isPresent()) {
                var cr = new CellReference(cellDef.get());
                if (cr.getSheetName() == null && !sheet.get().isEmpty()) {
                    cr = new CellReference(sheet.get() + "!" + cellDef.get());
                }
                final var sheetName = cr.getSheetName();
                var s = (sheetName == null ? (wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null) : wb.getSheet(sheetName));
                if (s == null) {
                    if (sheetName == null) {
                        s = wb.createSheet();
                    } else {
                        s = wb.createSheet(sheetName);
                    }
                }
                final var rowNum = cr.getRow();
                var r = s.getRow(rowNum);
                if (r == null) {
                    r = s.createRow(rowNum);
                }
                cell = r.getCell(cr.getCol());
                if (cell == null) {
                    cell = r.createCell(cr.getCol());
                }
                ref = cr.formatAsString();
            } else {
                var s = WorkSheetUtils.get(sheet.get(), wb);
                if (s == null) {
                    s = wb.createSheet(sheet.get());
                }
                final int rowNum = Integer.parseInt(rowDef.get());
                var r = s.getRow(rowNum);
                if (r == null) {
                    r = s.createRow(rowNum);
                }
                final var colNum = Integer.parseInt(colDef.get());
                cell = r.getCell(colNum);
                if (cell == null) {
                    cell = r.createCell(colNum);
                }
                ref = new CellReference(cell).formatAsString();
            }

            final What whatValue = what.isPresent() ? what.get(What.class) : (style.isPresent() ? What.style : What.value);

            setCell(cell, whatValue, as.get(CellType.class), in.toString(), wb, style.get(Style.class), author);
            return "";
        } catch (Exception e) {
            throw BadSyntax.format(e, "Cannot set the XLS cell '%s' %s", workbook.get(), ref);
        }
    }

    private static void setCell(final Cell cell,
                                final What what,
                                final CellType as,
                                final String in,
                                final Workbook wb,
                                final Style style,
                                final StringParameter author
    ) throws BadSyntax {
        switch (what) {
            case value:
                setCellValue(cell, in, as);
                break;
            case width:
                try {
                    final var width = Double.parseDouble(in);
                    final var col = cell.getColumnIndex();
                    final var sheet = cell.getSheet();
                    sheet.setColumnWidth(col, (int) (width * 256));
                } catch (NumberFormatException e) {
                    throw new BadSyntax("Cannot convert the value to a number setting column width: " + in, e);
                }
                break;
            case height:
                try {
                    final var height = Double.parseDouble(in);
                    final var row = cell.getRow();
                    row.setHeightInPoints((float) height);
                } catch (NumberFormatException e) {
                    throw new BadSyntax("Cannot convert the value to a number setting row height: " + in, e);
                }
                break;
            case formula:
                try {
                    cell.setCellFormula(in);
                } catch (Exception e) {
                    throw new BadSyntax("Cannot set the formula: " + in, e);
                }
                break;
            case format:
                setCellStyle(wb, cell, cs -> cs.setDataFormat(wb.createDataFormat().getFormat(in)));
                break;
            case comment: {
                var comment = cell.getCellComment();
                final var factory = wb.getCreationHelper();
                if (comment == null) {
                    comment = createComment(cell, factory);
                }
                if (author.isPresent()) {
                    final var str = factory.createRichTextString(author.get() + ":\n" + in);
                    comment.setString(str);
                    comment.setAuthor(author.get());
                } else {
                    final var str = factory.createRichTextString(in);
                    comment.setString(str);
                }
            }
            break;
            case style:
                switch (style) {
                    case align:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setAlignment(HorizontalAlignment.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown alignment: " + in, e);
                        }
                        break;
                    case border:
                        try {
                            final var borderStyle = BorderStyle.valueOf(in);
                            setCellStyle(wb, cell, cs -> {
                                cs.setBorderBottom(borderStyle);
                                cs.setBorderTop(borderStyle);
                                cs.setBorderLeft(borderStyle);
                                cs.setBorderRight(borderStyle);
                            });
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case rightBorder:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setBorderRight(BorderStyle.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case leftBorder:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setBorderLeft(BorderStyle.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case topBorder:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setBorderTop(BorderStyle.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case bottomBorder:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setBorderBottom(BorderStyle.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case borderColor:
                        try {
                            final var borderColor = IndexedColors.valueOf(in).getIndex();
                            setCellStyle(wb, cell, cs -> {
                                cs.setBottomBorderColor(borderColor);
                                cs.setTopBorderColor(borderColor);
                                cs.setLeftBorderColor(borderColor);
                                cs.setRightBorderColor(borderColor);
                            });
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case bottomBorderColor:
                        try {
                            cell.getCellStyle().setBottomBorderColor(IndexedColors.valueOf(in).getIndex());
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case topBorderColor:
                        try {
                            cell.getCellStyle().setTopBorderColor(IndexedColors.valueOf(in).getIndex());
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                    case leftBorderColor:
                        try {
                            cell.getCellStyle().setLeftBorderColor(IndexedColors.valueOf(in).getIndex());
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case rightBorderColor:
                        try {
                            cell.getCellStyle().setRightBorderColor(IndexedColors.valueOf(in).getIndex());
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown border style: " + in, e);
                        }
                        break;
                    case fillPattern:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setFillPattern(FillPatternType.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown fill pattern: " + in, e);
                        }
                        break;
                    case fillForegroundColor:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setFillForegroundColor(IndexedColors.valueOf(in).getIndex()));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown fill pattern: " + in, e);
                        }
                        break;
                    case fillBackgroundColor:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setFillBackgroundColor(IndexedColors.valueOf(in).getIndex()));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown fill pattern: " + in, e);
                        }
                        break;
                    case zoom:
                        try {
                            final var zoom = Integer.parseInt(in);
                            cell.getSheet().setZoom(zoom);
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a number setting zoom: " + in, e);
                        }
                        break;
                    case font:
                        final var font = wb.createFont();
                        final var parts = in.split("\\s*,\\s*|\\s*;\\s*");
                        BadSyntax.when(parts.length < 1, "Not enough parameters for font");
                        font.setFontName(parts[0].trim());
                        for (int i = 1; i < parts.length; i++) {
                            final var part = parts[i].trim();
                            if (part.matches("\\d+(:?pt|px)?")) {
                                final String size;
                                if (part.endsWith("pt") || part.endsWith("px")) {
                                    size = part.substring(0, part.length() - 2);
                                } else {
                                    size = part;
                                }
                                font.setFontHeightInPoints(Short.parseShort(size));
                            } else if (part.equalsIgnoreCase("bold")) {
                                font.setBold(true);
                            } else if (part.equalsIgnoreCase("italic")) {
                                font.setItalic(true);
                            } else {
                                BadSyntax.when(Arrays.stream(IndexedColors.values())
                                                .filter(c -> c.name().equalsIgnoreCase(part))
                                                .findAny().map(
                                                        c -> {
                                                            font.setColor(c.getIndex());
                                                            return false;
                                                        }).orElse(true),
                                        "Unknown color: " + part);
                            }
                        }
                        setCellStyle(wb, cell, cs -> cs.setFont(font));
                        break;
                    case dataFormat:
                        setCellStyle(wb, cell, cs ->
                                cs.setDataFormat(wb.createDataFormat().getFormat(in)));
                        break;
                    case hidden:
                        try {
                            setCellStyle(wb, cell, cs -> cs.setHidden(parseBoolean(in)));
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a boolean: " + in, e);
                        }
                        break;
                    case locked:
                        try {
                            setCellStyle(wb, cell, cs -> cs.setLocked(parseBoolean(in)));
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a boolean: " + in, e);
                        }
                        break;
                    case rotation:
                        try {
                            setCellStyle(wb, cell, cs -> cs.setRotation(Short.parseShort(in)));
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a short: " + in, e);
                        }
                        break;
                    case shrinkToFit:
                        try {
                            setCellStyle(wb, cell, cs -> cs.setShrinkToFit(parseBoolean(in)));
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a boolean: " + in, e);
                        }
                        break;
                    case verticalAlignment:
                        try {
                            setCellStyle(wb, cell, cs ->
                                    cs.setVerticalAlignment(VerticalAlignment.valueOf(in)));
                        } catch (IllegalArgumentException e) {
                            throw new BadSyntax("Unknown vertical alignment: " + in, e);
                        }
                        break;
                    case wrapText:
                        try {
                            setCellStyle(wb, cell, cs -> cs.setWrapText(parseBoolean(in)));
                        } catch (NumberFormatException e) {
                            throw new BadSyntax("Cannot convert the value to a boolean: " + in, e);
                        }
                        break;
                }
                break;
        }
    }

    private static boolean parseBoolean(String in) {
        return in.isBlank() || Boolean.parseBoolean(in);
    }

    private static void setCellStyle(Workbook wb, Cell cell, Consumer<CellStyle> styleSetter) {
        final var cellStyle = wb.createCellStyle();
        cellStyle.cloneStyleFrom(cell.getCellStyle());
        styleSetter.accept(cellStyle);
        cell.setCellStyle(cellStyle);
    }

    private static void setCellValue(final Cell cell, final String in, final CellType as) throws BadSyntax {
        switch (as) {
            case STRING:
                cell.setCellValue(in);
                break;
            case NUMERIC:
                try {
                    cell.setCellValue(Double.parseDouble(in));
                } catch (NumberFormatException e) {
                    throw new BadSyntax("Cannot convert the value to a number: " + in, e);
                }
                break;
            case BOOLEAN:
                try {
                    cell.setCellValue(parseBoolean(in));
                } catch (NumberFormatException e) {
                    throw new BadSyntax("Cannot convert the value to a boolean: " + in, e);
                }
                break;
            case FORMULA:
                try {
                    cell.setCellFormula(in);
                } catch (Exception e) {
                    throw new BadSyntax("Cannot set the formula: " + in, e);
                }
                break;
            case BLANK:
                BadSyntax.when(!in.isBlank(), "The value for a blank cell should be empty.");
                try {
                    cell.setBlank();
                } catch (Exception e) {
                    throw new BadSyntax("Cannot set the cell to blank: " + in, e);
                }
                break;
            case ERROR:
                cell.setCellErrorValue(FormulaError.forString(in).getCode());
                break;
        }
    }

    private static Comment createComment(Cell cell, CreationHelper factory) {
        final Sheet sheet = cell.getSheet();
        final var anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRow().getRowNum());
        anchor.setRow2(cell.getRow().getRowNum() + 3);
        final var drawing = sheet.createDrawingPatriarch();
        return drawing.createCellComment(anchor);
    }

    @Override
    public String getId() {
        return "xls:set";
    }
}
