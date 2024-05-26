package javax0.jamal.xls.utils;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class WorkSheetUtils {

    public static Sheet get(final String id, final Workbook workbook) {
        if( id == null || id.isBlank()){
            return workbook.getSheetAt(0);
        }
        if( id.startsWith("\"") && id.endsWith("\"")){
            return workbook.getSheet(id.substring(1, id.length()-1));
        }
        if( id.matches("\\d+")){
            return workbook.getSheetAt(Integer.parseInt(id));
        }
        return workbook.getSheet(id);
    }
}
