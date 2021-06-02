/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.cyanosource.plasmid.PlasmidsGenerator;
import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author tzielins
 */
@Service
public class SynBioHandler {

    final SynBioClient client;

    final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    // Excel column header strings - worksheet headers must match these to work
    private static final String DISP_ID_HEADER = "display_id";
    private static final String ATTACH_FILE_HEADER = "attachment_filename";
    private static final String DESC_HEADER = "description";
    private static final String NOTES_HEADER = "notes";

    private static final String SBOL_OBJ_TYPE = "ComponentDefinition";
    private static String SBOL_DISP_ID_TYPE;

    private static final JsonParser JSON_PARSER = JsonParserFactory.getJsonParser();

    private static final Pattern COLL_URL_VERSION_PATTERN = Pattern.compile(".*/[0-9]+.*");

    @Autowired
    public SynBioHandler(SynBioClient client) {
        this.client = client;

        try {
            SBOL_DISP_ID_TYPE = URLEncoder.encode("<http://sbols.org/v2#displayId>", StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to initialise SynBioHandler properly", e);
        }
    }

    public void handle(CommandOptions command) throws URISyntaxException, IOException {
        switch (command.command) {
            case DEPOSIT: handleDeposit(command); break;
            case UPDATE: handleUpdate(command); break;
            case GENERATE: handleGenerate(command); break;
            default: throw new IllegalArgumentException("Unsuported command: "+command.command);
        }
    }

    void handleDeposit(CommandOptions parameters) throws URISyntaxException {
        if (parameters.sessionToken == null) {
            parameters.sessionToken = login(parameters);
        }

        if (parameters.multipleCollections) {
            depositMultipleCollections(parameters);
        } else {
            depositSingleCollection(parameters);
        }
    }

    void handleUpdate(CommandOptions parameters) throws URISyntaxException, IOException {
        if (parameters.sessionToken == null) {
            parameters.sessionToken = login(parameters);
        }

        readExcel(parameters);
    }

    void handleGenerate(CommandOptions parameters) throws URISyntaxException, IOException {
        if (parameters.sessionToken == null) {
            parameters.sessionToken = login(parameters);
        }

        PlasmidsGenerator generator = new PlasmidsGenerator();
        String name = parameters.filenamePrefix;
        String version = parameters.version;
        Path templateFile = Paths.get(parameters.templateFile);
        Path flankFile = Paths.get(parameters.flankFile);
        Path outDir = Paths.get(parameters.outputDir);

        try {
            generator.generateFromFiles(name, version, templateFile, flankFile, outDir);
        } catch (SBOLValidationException | SBOLConversionException | ed.biordm.sbol.toolkit.transform.SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    String login(CommandOptions parameters) throws URISyntaxException {
        String url = client.hubFromUrl(parameters.url);

        return client.login(url,parameters.user, parameters.password);
    }


    void depositMultipleCollections(CommandOptions orgParameters) {

        String prefix = orgParameters.collectionName;

        // orgParameters.collectionName = prefix+"_"+orgParameters.dir;
        // we dont deal with subcollections here we leave it for UI
        // String rootCollUrl = createNewCollection(orgParameters);

        List<Path> subCollections = subfolders(orgParameters.dir);

        // no need to upload the parent directory, only the children
        // Path parentDirPath = Paths.get(orgParameters.dir);
        // processUploadDir(orgParameters, parentDirPath, prefix);
        for (Path col: subCollections) {
            processUploadDir(orgParameters, col, prefix);

            // we dont deal with subcollections here we leave it for UI
            //logger.info("Adding child {} to root URL: {}", collUrl, rootCollUrl);
            //addSubCollection(params, rootCollUrl, collUrl);
        }
    }

    void processUploadDir(CommandOptions origParameters, Path dirPath, String prefix) {

        String suffix = dirPath.getFileName().toString();
        String name = prefix+"_"+suffix;

        CommandOptions params = origParameters.clone();
        params.collectionName = name;
        params.dir = dirPath.toString();
        params.multipleCollections = false;
        params.crateNew = true;

        depositSingleCollection(params);
    }

    void depositSingleCollection(CommandOptions parameters) {
        String collectionUrl = parameters.url;
        if (parameters.crateNew) {
            collectionUrl = createNewCollection(parameters);
        }

        List<Path> files = getFiles(parameters);

        for (Path file: files) {

            // some other params as needed by the API
            // for example overwrite is needed here not only for the creation
            client.deposit(parameters.sessionToken, collectionUrl, file,
                    getOverwriteParam(parameters, true));
        }
    }

    String createNewCollection(CommandOptions parameters) {
        // provide the id, version, name, description, citations
        String name = parameters.collectionName;
        String desc = "Default description for " + name;

        String id = sanitizeName(name);
        //int version = 1;
        //int version = Integer.parseInt(parameters.version);
        String citations = "";

        boolean isOverwrite = parameters.overwrite;
        int overwriteMerge = getOverwriteParam(parameters, false);

        logger.info("URL in parameters: {}", parameters.url);

        String newUrl = client.createCollection(parameters.sessionToken, parameters.url+"submit",
                id, parameters.version, name, desc, citations, overwriteMerge);

        return newUrl;
    }

    void addSubCollection(CommandOptions parameters, String rootCollUrl, String childCollUrl) {
        client.addChildCollection(parameters.sessionToken, rootCollUrl, childCollUrl);
    }

    List<Path> subfolders(String dir) {

        try (Stream<Path> files = Files.list(Paths.get(dir))) {
            return  files.filter( f -> Files.isDirectory(f))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read directories of "+dir, e);
        }
    }

    Predicate<Path> extensionFilter(String ext) {

        if (ext.equals("*") || ext.equals(".*")) {
             return (Path p) -> true;
        }
        if (!ext.startsWith("."))
            ext = "."+ext;

        final String end = ext;
        return (Path p) -> p.getFileName().toString().endsWith(end);
    }

    List<Path> getFiles(CommandOptions parameters) {
        String dirPath = parameters.dir;

        // Reading the folder and getting Stream.
        try (Stream<Path> list = Files.list(Paths.get(dirPath))) {
             // Filtering the paths by a regular file and adding into a list.
            return list.filter(Files::isRegularFile)
                    .filter(extensionFilter(parameters.fileExtFilter))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error locating files for upload", e);
            throw new IllegalStateException("Error locating files for upload", e);
        }
    }

    protected String sanitizeName(String name) {
        String cleanName = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "_");
        return cleanName;
    }

    void readExcel(CommandOptions parameters) throws URISyntaxException, IOException {
        FeaturesReader featuresReader = new FeaturesReader();
        String url = client.hubFromUrl(parameters.url);

        // ensure collection URL specifies version
        String verCollUrl = verifyCollectionUrlVersion(parameters);

        final String collUrl = URLEncoder.encode("<"+verCollUrl+">", StandardCharsets.UTF_8.name());

        String filename = parameters.xslFile;
        File file = new File(filename);
        String cwd = file.getParent();
        Map<String, String> updatedDesigns = new LinkedHashMap();

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
                        processRow(parameters, cwd, collUrl, url, displayId,
                            colHeaders, colVals, updatedDesigns);
                    }
                } catch(Exception e) {
                    // abort the run and print out all the successful rows up to this point
                    outputDesigns(updatedDesigns);
                    throw(e);
                }
            });
        }

        outputDesigns(updatedDesigns);
    }

    String getDesignXml(CommandOptions parameters, String designUri) throws URISyntaxException {
        String token = login(parameters);

        // String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";
        String designXml = client.getDesign(token, designUri);

        return designXml;
    }

    protected void processRow(CommandOptions parameters, String cwd, 
            String collUrl, String url, String displayId, List<String> colHeaders,
            List<String> colVals, Map<String, String> updatedDesigns) {
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

        String metadata = client.searchMetadata(url, requestParams, parameters.sessionToken);
        //Object design = client.getSubmissionByDisplayId();
        List<Object> designList = JSON_PARSER.parseList(metadata);

        if(designList == null || designList.isEmpty()) {
            try {
                String decCollUrl = URLDecoder.decode(collUrl, StandardCharsets.UTF_8.name());
                String userMessage = "No design found with displayId {} in collection {}";
                logger.info(userMessage, displayId, decCollUrl);

                // replace with UI logger
                System.out.printf("No design found with displayId %s in collection %s%n%n", displayId, decCollUrl);
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
                        attachFilename, getOverwriteParam(parameters, true));
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

    protected String verifyCollectionUrlVersion(CommandOptions parameters)
            throws UnsupportedEncodingException, URISyntaxException {
        String verCollUrl = parameters.url;

        boolean hasVersion = COLL_URL_VERSION_PATTERN.matcher(verCollUrl).matches();

        if(hasVersion == false) {
            // retrieve the latest version as the default if none provided in URL
            String requestParams = "/persistentIdentity=" +
                    URLEncoder.encode("<"+verCollUrl+">", StandardCharsets.UTF_8.name()) +
                    "&";

            String metadata = client.searchMetadata(client.hubFromUrl(verCollUrl), requestParams, parameters.sessionToken);
            List<Object> collList = JSON_PARSER.parseList(metadata);

            if(collList == null || collList.isEmpty()) {
                String userMessage = "No collection found with persistent ID {}";
                logger.info(userMessage, verCollUrl);

                // replace with UI logger
                System.out.printf("No collection found with persistent ID %s%n%n", verCollUrl);
                return null;
            }

            //Object collUrl = collList.get(0);
            List<String> versions = new ArrayList();
            ComparableVersion maxCmpVersion = new ComparableVersion("0");

            // Find the latest version and return that URL
            for(Object collObj: collList) {
                if (collObj instanceof Map) {
                    Map collObjMap = (Map) collObj;
                    if(collObjMap.containsKey("version") && collObjMap.containsKey("uri")) {
                        String curVersion = (String)collObjMap.get("version");
                        ComparableVersion curCmpVersion = new ComparableVersion(curVersion);

                        if(curCmpVersion.compareTo(maxCmpVersion) > 0) {
                            maxCmpVersion = curCmpVersion;
                            verCollUrl = (String)collObjMap.get("uri");
                        }
                    }
                }
            }
        }

        return verCollUrl;
    }

    protected int getOverwriteParam(CommandOptions parameters, boolean isFile) {
        boolean isOverwrite = parameters.overwrite;
        int overwriteMerge = 0; // Assume we prevent overwriting if exists

        if(isOverwrite) {
            // never want to accidentally overwrite files if they already exist
            // when the user chose to create a new collection
            overwriteMerge = 3;
            /*if(parameters.crateNew) {
                overwriteMerge = 1;
            } else {
                overwriteMerge = 3;
            }*/
        } else {
            if(parameters.crateNew) {
                if(isFile == true) {
                    overwriteMerge = 2;
                } else {
                    overwriteMerge = 0;
                }
            } else {
                overwriteMerge = 2;
            }
        }

        return overwriteMerge;
    }
}
