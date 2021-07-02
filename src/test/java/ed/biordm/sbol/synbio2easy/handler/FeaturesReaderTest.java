/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.handler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jhay
 */
public class FeaturesReaderTest {

    FeaturesReader featuresReader = new FeaturesReader();

    /**
     * Test of readSimpleFeatures method, of class FeaturesReader.
     */
    @Test
    public void testReadSimpleFeatures() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());

        String expKey = "sll0199_left";
        String expValue = "NC_001499.gbk";

        // Test worksheet 1 (Left flank)
        Map<String, String> result = featuresReader.readSimpleFeatures(file.toPath(), 0, 0, 15);
        assertEquals(expKey, result.get(expKey));

        expKey = "sll0199_right";
        expValue = "NC_035470.gbk";

        result = featuresReader.readSimpleFeatures(file.toPath(), 1, 0, 15);
        assertEquals(expKey, result.get(expKey));
    }

    /**
     * Test of readMultiFeatures method, of class FeaturesReader.
     */
    @Test
    public void testReadMultiFeatures() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());

        String expKey = "sll0199_left";
        List<String> expValues = Arrays.asList("sll0199_left","NC_001499.gbk","this is the left flank of cyano_codA_Km plasmid","The attached genbank file is from ftp://ftp.ncbi.nlm.nih.gov/genomes/Viruses/abelson_murine_leukemia_virus_uid14654/NC_001499.gbk");

        // Test worksheet 1 (Left flank)
        Map<String, List<String>> result = featuresReader.readMultiFeatures(file.toPath(), 0, 0, 4);
        List<String> resultVals = result.get(expKey);

        for(String expValue: expValues) {
            List<String> matches = resultVals.stream().filter(str -> str.trim().contains(expValue))
                .collect(Collectors.toList());
            
            assertTrue(matches.size() > 0);
        }


        expKey = "sll0199_right";
        expValues = Arrays.asList("sll0199_right","NC_035470.gbk","this is the right flank of cyano_codA_Km plasmid","The attached genbank file is from ftp://ftp.ncbi.nlm.nih.gov/genomes/Viruses/abisko_virus_uid399942/NC_035470.gbk");

        result = featuresReader.readMultiFeatures(file.toPath(), 1, 0, 15);
        resultVals = result.get(expKey);
        // assertEquals(expValue, result.get(expKey));
        for(String expValue: expValues) {
            List<String> matches = resultVals.stream().filter(str -> str.trim().contains(expValue))
                .collect(Collectors.toList());
            
            assertTrue(matches.size() > 0);
        }
    }

    @Test
    public void readMultiFeaturesReadsListOfValuesPreservingBlanks() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());

        // Test worksheet 1 (Right flank)
        Map<String, List<String>> result = featuresReader.readMultiFeatures(file.toPath(), 0, 0, 4);
        String expKey = "0001_slr0611_right";
        List<String> expValues = List.of("0001_slr0611_right", "", "this is a row with an empty attachment file", "this is a row with an empty attachment file");

        List<String> resultVals = result.get(expKey);
        // assertEquals(expValue, result.get(expKey));
        for(String expValue: expValues) {
            List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                .collect(Collectors.toList());
            
            assertTrue(matches.size() > 0);
        }

        expKey = "0003_slr0613_left";
        expValues = List.of("0003_slr0613_left", "NC_014139.gbk", "", "This is a row with an empty description");

        resultVals = result.get(expKey);
        // assertEquals(expValue, result.get(expKey));
        for(String expValue: expValues) {
            List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                .collect(Collectors.toList());
            
            assertTrue(matches.size() > 0);
        }
    }

    /**
     * Test of readWorksheetRows method, of class FeaturesReader.
     */
    @Test
    public void testReadWorksheetRows() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, List<String>> rows = featuresReader.readWorksheetRows(sheet, 0, 4, formEval);

            String expKey = "sll0199_left";
            List<String> expValues = Arrays.asList("sll0199_left", "NC_001499.gbk","this is the left flank of cyano_codA_Km plasmid","The attached genbank file is from ftp://ftp.ncbi.nlm.nih.gov/genomes/Viruses/abelson_murine_leukemia_virus_uid14654/NC_001499.gbk");

            List<String> resultVals = rows.get(expKey);
            // assertEquals(expValue, result.get(expKey));
            for(String expValue: expValues) {
                List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                    .collect(Collectors.toList());

                assertTrue(matches.size() > 0);
            }

            expKey = "sll0199_right";
            expValues = Arrays.asList("NC_035470.gbk","this is the right flank of cyano_codA_Km plasmid","The attached genbank file is from ftp://ftp.ncbi.nlm.nih.gov/genomes/Viruses/abisko_virus_uid399942/NC_035470.gbk");

            rows = featuresReader.readWorksheetRows(sheet, 1, 4, formEval);
            resultVals = rows.get(expKey);
            // assertEquals(expValue, result.get(expKey));
            for(String expValue: expValues) {
                List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                    .collect(Collectors.toList());

                assertTrue(matches.size() > 0);
            }

            expKey = "0001_slr0611_right";
            expValues = Arrays.asList("","this is a row with an empty attachment file","this is a row with an empty attachment file");

            rows = featuresReader.readWorksheetRows(sheet, 2, 4, formEval);
            resultVals = rows.get(expKey);
            // assertEquals(expValue, result.get(expKey));
            for(String expValue: expValues) {
                List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                    .collect(Collectors.toList());

                assertTrue(matches.size() > 0);
            }

            expKey = "0003_slr0613_left";
            expValues = Arrays.asList("NC_014139.gbk","","This is a row with an empty description");

            rows = featuresReader.readWorksheetRows(sheet, 3, 4, formEval);
            resultVals = rows.get(expKey);
            // assertEquals(expValue, result.get(expKey));
            for(String expValue: expValues) {
                List<String> matches = resultVals.stream().filter(str -> str.trim().endsWith(expValue))
                    .collect(Collectors.toList());

                assertTrue(matches.size() > 0);
            }
        }
    }

    /**
     * Test of getStringValueFromCell method, of class FeaturesReader.
     */
    @Test
    public void testGetStringValueFromCell() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Use reflection to access the private method
            Method privateMethod = FeaturesReader.class.getDeclaredMethod("getStringValueFromCell", Cell.class, FormulaEvaluator.class);
            privateMethod.setAccessible(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            // Test row 684
            Row row = sheet.getRow(2);

            String expKey = "sll0199_right";
            String expValue = "NC_035470.gbk";

            String resKey = (String) privateMethod.invoke(featuresReader, row.getCell(0), formEval);
            String resValue = (String) privateMethod.invoke(featuresReader, row.getCell(1), formEval);

            assertEquals(resKey, expKey);
            assertTrue(resValue.endsWith(expValue));
        }
    }

    /**
     * Test of getValueFromCell method, of class FeaturesReader.
     */
    @Test
    public void testGetValueFromCell() throws Exception {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Use reflection to access the private method
            Method privateMethod = FeaturesReader.class.getDeclaredMethod("getValueFromCell", Cell.class, FormulaEvaluator.class);
            privateMethod.setAccessible(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            String expKey = "sll0199_left";
            String expValue = "NC_001499.gbk";

            // Test row 684
            Row row = sheet.getRow(1);

            Object resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0), formEval);
            Object resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(1), formEval);

            assertTrue(((String)resKey).endsWith(expKey));
            assertTrue(((String)resValue).endsWith(expValue));

            // Test row 4
            row = sheet.getRow(4);

            expKey = "0003_slr0613_left";
            expValue = null;

            resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0), formEval);
            resValue = null;

            // This particular row has no code value column
            try {
                resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(2), formEval);
            } catch (Exception e) {

            }

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);
        }
    }
}
