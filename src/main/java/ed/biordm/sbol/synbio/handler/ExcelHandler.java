/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import static ed.biordm.sbol.toolkit.meta.ExcelMetaReader.ATTACH_FILE_HEADER;
import static ed.biordm.sbol.toolkit.meta.ExcelMetaReader.DESC_HEADER;
import static ed.biordm.sbol.toolkit.meta.ExcelMetaReader.DISP_ID_HEADER;
import static ed.biordm.sbol.toolkit.meta.ExcelMetaReader.NOTES_HEADER;
import ed.biordm.sbol.toolkit.transform.Outcome;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jhay
 */
public class ExcelHandler {
    final SynBioClient client;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    final String SBOL_OBJ_TYPE;
    final String SBOL_DISP_ID_TYPE;

    public ExcelHandler(SynBioClient client) {
        // that should be probably injected in autowired constructor
        //this.jsonParser = JsonParserFactory.getJsonParser();        
        this.client = client;
        this.SBOL_DISP_ID_TYPE = client.encodeURL("<http://sbols.org/v2#displayId>");
        this.SBOL_OBJ_TYPE = "ComponentDefinition";
    }

    public Outcome processUpdateExcel(CommandOptions parameters) throws URISyntaxException, IOException {
        FeaturesReader featuresReader = new FeaturesReader();
        String url = client.hubFromUrl(parameters.url);

        // ensure collection URL specifies version
        String verCollUrl = client.verifyCollectionUrlVersion(parameters);

        final String collUrl = URLEncoder.encode("<"+verCollUrl+">", StandardCharsets.UTF_8.name());

        String filename = parameters.metaFile;
        File file = new File(filename);
        String cwd = file.getParent();
        Map<String, String> updatedDesigns = new LinkedHashMap();
        Outcome outcome = new Outcome();

        System.out.println("");

        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            Sheet sheet = workbook.getSheetAt(0);

            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // assume always 4 column names in header
            List<String> colHeaders = featuresReader.readWorksheetHeader(sheet, 4, formEval);
            Map<String, List<String>> rows = featuresReader.readWorksheetRows(sheet, 1, 4, formEval);

            rows.forEach((key, value) -> {
                List<String> colVals = (List<String>) value;

                try {
                    final String displayId = colVals.get(colHeaders.indexOf(DISP_ID_HEADER));

                    if(!displayId.isBlank()) {
                        processUpdateRow(parameters, cwd, collUrl, url, displayId,
                            colHeaders, colVals, updatedDesigns, outcome);
                    }
                } catch(Exception e) {
                    // abort the run and print out all the successful rows up to this point
                    outputDesigns(updatedDesigns);
                    throw(e);
                }
            });
        }

        outputDesigns(updatedDesigns);
        return outcome;
    }


    protected void processUpdateRow(CommandOptions parameters, String cwd, 
            String collUrl, String url, String displayId, List<String> colHeaders,
            List<String> colVals, Map<String, String> updatedDesigns, Outcome outcome) {
        String attachFilename = null;
        String description = null;
        String notes = null;

        if(colHeaders.contains(ATTACH_FILE_HEADER)) {
            attachFilename = colVals.get(colHeaders.indexOf(ATTACH_FILE_HEADER));
        }

        if(colHeaders.contains(DESC_HEADER)) {
            description = colVals.get(colHeaders.indexOf(DESC_HEADER));
        }

        if(colHeaders.contains(NOTES_HEADER)) {
            notes = colVals.get(colHeaders.indexOf(NOTES_HEADER));
        }

        String requestParams = "/objectType="+SBOL_OBJ_TYPE+"&collection="+collUrl+
            "&"+SBOL_DISP_ID_TYPE+"='"+displayId+"'&/?offset=0&limit=10";

        // String metadata = client.searchMetadata(url, requestParams, parameters.sessionToken);
        //Object design = client.getSubmissionByDisplayId();
        // List<Object> designList = jsonParser.parseList(metadata);
        List<Object> designList = client.searchMetadata(url, requestParams, parameters.sessionToken);

        if(designList == null || designList.isEmpty()) {
            try {
                String decCollUrl = URLDecoder.decode(collUrl, StandardCharsets.UTF_8.name());
                String userMessage = "No design found with displayId {} in collection {}";
                logger.info(userMessage, displayId, decCollUrl);

                // replace with UI logger
                System.out.printf("No design found with displayId %s in collection %s%n%n", displayId, decCollUrl);
                outcome.missingId.add(displayId);
                return;
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }

        Object design = designList.get(0);

        if (design instanceof Map) {
            Map<String,Object> map = (Map<String,Object>) design;
            String designUri = (String)map.get("uri");

            if(attachFilename != null) {
                attachFileToDesign(parameters, cwd, designUri, attachFilename);
            }

            if(description != null) {
                updateDesignDescription(parameters, cwd, designUri, description);
            }

            if(notes != null) {
                updateDesignNotes(parameters, cwd, designUri, notes);
            }

            updatedDesigns.put(displayId, collUrl);
            outcome.successful.add(displayId);
        }
    }

    protected void attachFileToDesign(CommandOptions parameters, String cwd,
            String designUri, String attachFilename) {
        if (attachFilename != null && !attachFilename.isEmpty()) {
            File attachFile = new File(attachFilename);

            if (!attachFile.exists()) {
                // assume this must be a relative file path, so prepend parent dir path
                attachFilename = cwd+System.getProperty("file.separator")+attachFilename;
                attachFile = new File(attachFilename);
            }

            if (attachFile.exists()) {
                client.attachFile(parameters.sessionToken, designUri+"/",
                        attachFilename, client.getOverwriteParam(parameters, true));
            }
        }
    }

    protected void updateDesignDescription(CommandOptions parameters, String cwd,
            String designUri, String description) {
        if (description != null && !description.isEmpty()) {
            client.appendToDescription(parameters.sessionToken, designUri+"/", description);
        }
    }

    protected void updateDesignNotes(CommandOptions parameters, String cwd,
            String designUri, String notes) {
        if (notes != null && !notes.isEmpty()) {
            client.appendToNotes(parameters.sessionToken, designUri+"/", notes);
        }
    }

    protected void outputDesigns(Map<String, String> updatedDesigns) {
        // replace this with UI logger
        System.out.println("");
        System.out.println("Successfully updated the following designs...\n");
        System.out.println("");

        updatedDesigns.forEach((key, value) -> {
            try {
                String decVal = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                System.out.printf("DisplayId: %s in collection %s%n%n", key, decVal);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        });

        System.out.println("");
    }
}
