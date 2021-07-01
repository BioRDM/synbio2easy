/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import ed.biordm.sbol.toolkit.meta.ExcelMetaReader;
import ed.biordm.sbol.toolkit.transform.Outcome;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sbolstandard.core2.ComponentDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jhay
 */
public class TemplateGenerator {
    final SynBioClient client;
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final String[] COL_HEADERS;

    public TemplateGenerator(SynBioClient client) {
        // that should be probably injected in autowired constructor
        //this.jsonParser = JsonParserFactory.getJsonParser();        
        this.client = client;
        //this.SBOL_DISP_ID_TYPE = client.encodeURL("<http://sbols.org/v2#displayId>");
        //this.SBOL_OBJ_TYPE = "ComponentDefinition";
        this.COL_HEADERS = new String[]{ ExcelMetaReader.DISP_ID_HEADER,
            "uploaded_name", "original_name",
            ExcelMetaReader.VERSION_HEADER, "uri",
            ExcelMetaReader.ATTACH_FILE_HEADER,
            ExcelMetaReader.DESC_HEADER, ExcelMetaReader.NOTES_HEADER };
    }

    public Outcome generateTemplate(CommandOptions parameters) {
        List<String[]> dataLines = new ArrayList();

        Path outputFile = Paths.get(parameters.outputFile);
        Path inputFile = Paths.get(parameters.inputFile);
        Outcome outcome = new Outcome();

        try {
            // do output to CSV file of uploaded designs
            dataLines.addAll(getUploadedDesignProperties(parameters, inputFile, outcome));
        } catch (FileNotFoundException | UnsupportedEncodingException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            //this.writeLogToCsv(outputFile, dataLines);
            this.writeLogToExcel(outputFile, dataLines);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return outcome;
    }

    protected void writeLogToCsv(Path csvOutputFile, List<String[]> dataLines) throws IOException {
        // add header row
        List<String[]> dataLinesCpy = new ArrayList(dataLines);
        dataLinesCpy.add(0, COL_HEADERS);

        try (PrintWriter pw = new PrintWriter(csvOutputFile.toFile())) {
            dataLinesCpy.stream().map(this::convertToCSV).forEach(pw::println);
        }
    }

    protected List<String[]> getUploadedDesignProperties(CommandOptions parameters,
            Path sbolFile, Outcome outcome)
            throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException {
        Set<ComponentDefinition> cmpDefs = client.getComponentDefinitions(sbolFile);
        List<Map<String,Object>> designMaps = listCollectionDesigns(parameters);
        List<String[]> dataLines = new ArrayList<>();

        for(ComponentDefinition cmpDef: cmpDefs) {
            String name = cmpDef.getName();
            String displayId = cmpDef.getDisplayId();
            String version = cmpDef.getVersion();

            // match the ComponentDefinition properties from the SBOL file with those
            // in the uploaded collection
            for(Map<String,Object> designMap: designMaps) {
                String upldName = (String)designMap.get("name");
                String upldDisplayId = (String)designMap.get("displayId");
                String upldVersion = (String)designMap.get("version");

                if(upldDisplayId.equals(displayId) && upldVersion.equals(version)) {
                    String cleanUpName = escapeSpecialCharacters(upldName);
                    String cleanOrigName = escapeSpecialCharacters(name);
                    String cleanDisplayId = escapeSpecialCharacters(upldDisplayId);
                    String cleanVersion = escapeSpecialCharacters(upldVersion);
                    String cleanUri = escapeSpecialCharacters((String)designMap.get("uri"));

                    dataLines.add(new String[]
                        { cleanDisplayId, cleanUpName, cleanOrigName, cleanVersion, cleanUri });

                    outcome.successful.add(displayId);
                }
            }

            if(!outcome.successful.contains(displayId)) {
                outcome.missingId.add(displayId);
            }
        }

        return dataLines;
    }

    protected String convertToCSV(String[] data) {
        return Stream.of(data)
          .map(this::escapeSpecialCharacters)
          .collect(Collectors.joining(","));
    }

    protected String escapeSpecialCharacters(String data) {
        if(data == null) {
            return "";
        }

        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    protected List<Map<String,Object>> listCollectionDesigns(CommandOptions parameters) throws UnsupportedEncodingException, URISyntaxException {
        String url = client.hubFromUrl(parameters.url);

        // ensure collection URL specifies version
        String verCollUrl = client.verifyCollectionUrlVersion(parameters);

        final String collUrl = client.encodeURL("<"+verCollUrl+">");

        String objType = "http://sbols.org/v2#ComponentDefinition";
        objType = "ComponentDefinition";

        // retrieve existing design and description
        String requestParams = "/objectType="+objType+"&collection="+collUrl+"&/?offset=0&limit=10000";

        // String metadata = client.searchMetadata(url, requestParams, parameters.sessionToken);

        //Object design = client.getSubmissionByDisplayId();
        // List<Object> designList = jsonParser.parseList(metadata);
        List<Object> designList = client.searchMetadata(client.hubFromUrl(verCollUrl), requestParams, parameters.sessionToken);
        List<Map<String,Object>> designMaps = new ArrayList();

        if(designList == null || designList.isEmpty()) {
            System.out.printf("Collection %s is empty!%n", verCollUrl);
        }

        for(Object cmpDef: designList) {
            if (cmpDef instanceof Map) {
                Map<String,Object> cmpDefMap = (Map<String,Object>) cmpDef;
                String uri = (String)cmpDefMap.get("uri");
                String displayId = (String)cmpDefMap.get("displayId");
                String version = (String)cmpDefMap.get("version");
                String name = (String)cmpDefMap.get("name");

                designMaps.add(cmpDefMap);
            }
        }

        return designMaps;
    }

    protected void writeLogToExcel(Path excelOutputFile, List<String[]> dataLines) throws IOException {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        //CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Designs");

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for(int i = 0; i < COL_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(COL_HEADERS[i]);
        }

        // Create Other rows and cells with employees data
        int rowNum = 1;
        for(String[] dataLine: dataLines) {
            Row row = sheet.createRow(rowNum++);
            int cellIdx = 0;

            for(String cellVal: dataLine) {
                row.createCell(cellIdx++)
                    .setCellValue(cellVal);
            }
        }

        // Resize all columns to fit the content size
        for(int i = 0; i < COL_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        try(FileOutputStream fileOut = new FileOutputStream(excelOutputFile.toFile().getAbsoluteFile())) {
            workbook.write(fileOut);
            fileOut.close();
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            // Closing the workbook
            workbook.close();
        }
    }
}
