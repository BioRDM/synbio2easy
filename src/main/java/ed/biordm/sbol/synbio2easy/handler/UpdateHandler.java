/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.handler;

import ed.biordm.sbol.sbol2easy.meta.ExcelMetaReader;
import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import static ed.biordm.sbol.sbol2easy.meta.ExcelMetaReader.*;
import ed.biordm.sbol.sbol2easy.meta.MetaFormat;
import ed.biordm.sbol.sbol2easy.meta.MetaHelper;
import static ed.biordm.sbol.sbol2easy.meta.MetaHelper.setTemplateVariable;
import ed.biordm.sbol.sbol2easy.meta.MetaRecord;
import ed.biordm.sbol.sbol2easy.transform.ComponentAnnotator;
import ed.biordm.sbol.sbol2easy.transform.Outcome;
import ed.biordm.sbol.synbio2easy.client.SynBioClient.SynBioClientException;
import static ed.biordm.sbol.synbio2easy.client.SynBioClient.encodeURL;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sbolstandard.core2.ComponentDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author tzielins
 */
@Service
public class UpdateHandler {
    final SynBioClient client;
    final Logger logger = LoggerFactory.getLogger(this.getClass());


    final int MAX_NUM_COLUMNS;
    
    final MetaHelper metaHelper = new MetaHelper();
    final ComponentAnnotator annotator = new ComponentAnnotator();
    

    @Autowired
    public UpdateHandler(SynBioClient client) {
        // that should be probably injected in autowired constructor
        //this.jsonParser = JsonParserFactory.getJsonParser();        
        this.client = client;
        this.MAX_NUM_COLUMNS = 15;
    }

    public Outcome updateRecords(String collUrl, Path metaFile, boolean overwriteDesc, String session) throws IOException {
        
        ExcelMetaReader metaReader = new ExcelMetaReader();
        
        MetaFormat metaFormat = metaReader.readMetaFormat(metaFile);
        validateMetaFormat(metaFormat);
        
        List<MetaRecord> metaData = metaReader.readMeta(metaFile, metaFormat);
        metaData = metaHelper.calculateIdFromKey(metaData);
    
        Outcome status = new Outcome();
        
        
        return updateRecords(collUrl, metaData, metaFormat, overwriteDesc, status, session);
        
    }
    
    Outcome updateRecords(String collUrl, List<MetaRecord> metaData, MetaFormat metaFormat, boolean overwriteDesc, Outcome status, String session) {

        collUrl = versionedCollection(collUrl, session);
        
        for (MetaRecord meta : metaData) {
            String displayId = meta.displayId.get();
            
            Optional<String> designUri = findDesignUri(collUrl, displayId, session);
            
            if (designUri.isEmpty()) {
                status.missingId.add(displayId);
                continue;
            }
            
            Optional<ComponentDefinition> definition = findCompDefintion(designUri.get(),session);
            if (definition.isEmpty()) {
                status.missingId.add(displayId);
                continue;
            }
            
            boolean success = updateRecord(definition.get(), designUri.get(), meta, overwriteDesc, session);
            if (success) {
                status.successful.add(displayId);
            } else {
                status.failed.add(displayId);
            }

        }

        return status;    
    }    
    
    void validateMetaFormat(MetaFormat metaFormat) {
        if (metaFormat.displayId.isEmpty())
            throw new IllegalArgumentException("DisplayId must be present in the meta description table");
    } 
    
    String versionedCollection(String collUrl, String session) {
        return client.verifyCollectionUrlVersion(collUrl, session);
    }
    
    Optional<String> findDesignUri(String collUrl, String displayId, String session) {
        
        return client.findDesignUriInCollection(session, collUrl, displayId);
    }
    
    Optional<ComponentDefinition> findCompDefintion(String designUri, String session) {
        
        try {
            ComponentDefinition def = client.getComponentDefition(session, designUri);
            return Optional.ofNullable(def);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return Optional.empty();
        }
        
    }    

    boolean updateRecord(ComponentDefinition component, String componentUri, MetaRecord meta, boolean overwrite, String session)  {
        
        String displayId = component.getDisplayId();
        String key = meta.key.orElse("");
        
        String name = component.getName() != null ? component.getName() : "";
        if (meta.name.isPresent()) {
            String template = meta.name.get();
            template = setTemplateVariable("displayId", displayId, template);
            template = setTemplateVariable("key", key, template);
            name = template;
        }
        
        try {
            if (meta.attachment.isPresent()) {
                String attachment = annotator.parseTemplate(meta.attachment.get(), displayId, key, name);
                attachFileToDesign(component, componentUri, attachment, overwrite, session);
            }

            if (meta.description.isPresent()) {
                annotator.addDescription(component, meta.description, overwrite, displayId, key, name);
                String desc = annotator.getDescription(component);
                updateDesignDescription(componentUri, desc, session);
            }
            
            if (meta.notes.isPresent()) {
                annotator.addNotes(component, meta.notes, overwrite, displayId, key, name);
                String note = annotator.getNotes(component);
                updateDesignNotes(componentUri, note, session);
            }

            
        } catch (IllegalArgumentException | SynBioClientException e) {
            logger.error("Could not update "+displayId+"; "+e.getMessage(),e);
            return false;
        }
        
        return true;
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

            // use MAX_NUM_COLUMNS so the reader knows when to stop reading columns
            List<String> colHeaders = featuresReader.readWorksheetHeader(sheet, MAX_NUM_COLUMNS, formEval);
            Map<String, List<String>> rows = featuresReader.readWorksheetRows(sheet, 1, MAX_NUM_COLUMNS, formEval);

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
                    throw new IllegalStateException(e);
                }
            });
        }

        outputDesigns(updatedDesigns);
        return outcome;
    }


    protected void processUpdateRow(CommandOptions parameters, String cwd, 
            String collUrl, String url, String displayId, List<String> colHeaders,
            List<String> colVals, Map<String, String> updatedDesigns, Outcome outcome) throws SynBioClient.SynBioClientException {
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

        String requestParams = ""; //= "/objectType="+SBOL_OBJ_TYPE+"&collection="+collUrl+
        //    "&"+SBOL_DISP_ID_TYPE+"='"+displayId+"'&/?offset=0&limit=10";

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
            String designUri, String attachFilename) throws SynBioClient.SynBioClientException {
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
    
    void attachFileToDesign(ComponentDefinition component, String componentUri, String attachmentPath, boolean overwrite, String session) throws SynBioClient.SynBioClientException {
        if (attachmentPath.isBlank()) return;
        
        Path file = Paths.get(attachmentPath);
        if (!Files.isRegularFile(file))
            throw new IllegalArgumentException("Cant read file: "+file);
        
        client.attachFile(session, componentUri, file, overwrite);
    }    

    protected void updateDesignDescription(CommandOptions parameters, String cwd,
            String designUri, String description) {
        if (description != null && !description.isEmpty()) {
            client.appendToDescription(parameters.sessionToken, designUri+"/", description);
        }
    }
    
    void updateDesignDescription(String componentUri, String desc, String session) {
        if (desc == null) return;
        client.setMutableDescription(session, componentUri, desc);
    }    

    
    protected void updateDesignNotes(CommandOptions parameters, String cwd,
            String designUri, String notes) {
        if (notes != null && !notes.isEmpty()) {
            client.appendToNotes(parameters.sessionToken, designUri+"/", notes);
        }
    }
    

    void updateDesignNotes(String componentUri, String note, String session) {
        
        if (note == null) return;
        
        client.setNotes(session, componentUri, note);
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
