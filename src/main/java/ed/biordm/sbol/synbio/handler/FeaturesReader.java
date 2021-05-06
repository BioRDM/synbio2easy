/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author tzielins
 */
public class FeaturesReader {

    /**
     * Reads features to be used by transformations from excel file. First
     * column contains object id, second is the feature value. It can ignore
     * header rows and read from particular sheet
     *
     * @param file
     * @param skipRows
     * @param sheetNr
     * @return map with id, value pairs from the read rows
     */
    public Map<String, String> readSimpleFeatures(Path file, int skipRows,
            int sheetNr, int numCols) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(file.toFile(), null, true)) {

            Map<String, String> features = new HashMap<>();

            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            Sheet sheet = workbook.getSheetAt(sheetNr);

            Map<String, List<String>> rows = readWorksheetRows(sheet, skipRows,
                    numCols, formEval);

            rows.forEach((key, value) -> {
                List<String> colVals = (List<String>) value;
                String featureVal = new String();
                if (colVals.size() > 0) {
                    featureVal = colVals.get(0);
                }

                features.put(key, featureVal);

            });
            return features;
        } catch (IllegalArgumentException | NotOLE2FileException e) {
            throw new IOException("Not valid excel: " + e.getMessage(), e);
        }

    }

    /**
     * Reads multiple features represented as multiple values in consecutive
     * columns. First column contains object id, the following the values. It
     * can ignore header rows and read from particular sheet
     *
     * @param file
     * @param skipRows
     * @param sheetNr
     * @return map with id and the list of read features values
     */
    public Map<String, List<String>> readMultiFeatures(Path file, int skipRows,
            int sheetNr, int numCols) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(file.toFile(), null, true)) {
            Map<String, List<String>> features = new HashMap<>();

            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            Sheet sheet = workbook.getSheetAt(sheetNr);

            features = readWorksheetRows(sheet, skipRows, numCols, formEval);
            return features;
        } catch (IllegalArgumentException | NotOLE2FileException e) {
            throw new IOException("Not valid excel: " + e.getMessage(), e);
        }

    }

    /**
     * Iterates through all rows in the provided worksheet and
     *
     * @param worksheet
     * @param skipRows: the number of rows to skip, e.g. '1' for skipping header
     * @return map with id and the list of read column values
     */
    protected Map<String, List<String>> readWorksheetRows(Sheet worksheet,
            int skipRows, int numCols, FormulaEvaluator formEval) {
        Map<String, List<String>> rows = new HashMap<>();

        // https://knpcode.com/java-programs/read-excel-file-java-using-apache-poi/
        Iterator<Row> rowItr = worksheet.iterator();
        while (rowItr.hasNext()) {
            Row row = rowItr.next();
            // skip header (First row)
            if (row.getRowNum() < skipRows) {
                continue;
            }
            List<String> colVals = new ArrayList<>();

            for (int cn = 0; cn < numCols; cn++) {
                Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                int index = cell.getColumnIndex();

                String cellValue = getStringValueFromCell(cell, formEval);

                if (index == 0) {
                    rows.put(cellValue, colVals);
                    colVals.add(cellValue);
                } else {
                    colVals.add(cellValue);
                }
            }
        }

        return rows;
    }

    public List<String> readWorksheetHeader(Sheet worksheet, int numCols, FormulaEvaluator formEval) {
        Iterator<Row> rowItr = worksheet.iterator();
        List<String> colNames = new ArrayList();

        while (rowItr.hasNext()) {
            Row row = rowItr.next();

            if (row.getRowNum() > 0) {
                break;
            }

            for (int cn = 0; cn < numCols; cn++) {
                Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                int index = cell.getColumnIndex();

                String cellValue = getStringValueFromCell(cell, formEval);

                if(!cellValue.isBlank()) {
                    colNames.add(cellValue);
                }
            }
        }

        return colNames;
    }

    // Utility method to get String value of cell based on cell type
    private String getStringValueFromCell(Cell cell, FormulaEvaluator formEval) {
        String stringCellVal = new String();
        Object cellValue = getValueFromCell(cell, formEval);

        if (cellValue instanceof Number) {
            stringCellVal = String.valueOf(cellValue);
        } else if (cellValue instanceof String) {
            stringCellVal = (String) cellValue;
        } else if (cellValue instanceof Boolean) {
            stringCellVal = String.valueOf(cellValue);
        } else if (cellValue instanceof Date) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            stringCellVal = df.format(cellValue);
        }

        return stringCellVal;
    }

    // Utility method to get cell value based on cell type
    private Object getValueFromCell(Cell cell, FormulaEvaluator formEval) {

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case FORMULA:
                return formEval.evaluate(cell).getStringValue();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /*
    * Are we expected to be able to parse both old Excel 2003 and newer Excel
    * 2007 formats? If so, check out "How to Read both Excel 2003 and 2007 format"
    * https://www.codejava.net/coding/how-to-read-excel-files-in-java-using-apache-poi
     */
}
