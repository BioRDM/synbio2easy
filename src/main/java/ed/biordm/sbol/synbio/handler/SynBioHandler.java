/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.cyanosource.plasmid.PlasmidsGenerator;
import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import ed.biordm.sbol.toolkit.transform.ComponentAnnotator;
import ed.biordm.sbol.toolkit.transform.ComponentFlattener;
import ed.biordm.sbol.toolkit.transform.ComponentUtil;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.emptyDocument;
import ed.biordm.sbol.toolkit.transform.SynBioTamer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.saveValidSbol;
import ed.biordm.sbol.toolkit.transform.LibraryGenerator;
import ed.biordm.sbol.toolkit.transform.Outcome;

/**
 *
 * @author tzielins
 */
@Service
public class SynBioHandler {

    final SynBioClient client;
    final ComponentFlattener flattener;
    final ComponentUtil compUtil;
    final ComponentAnnotator annotator;
    final LibraryGenerator generator;
    
    final String SBOL_DISP_ID_TYPE;
    final JsonParser jsonParser;

    final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    // Excel column header strings - worksheet headers must match these to work
    private static final String DISP_ID_HEADER = "display_id";
    private static final String ATTACH_FILE_HEADER = "attachment_filename";
    private static final String DESC_HEADER = "description";
    private static final String NOTES_HEADER = "notes";

    private static final String SBOL_OBJ_TYPE = "ComponentDefinition";


    private static final Pattern COLL_URL_VERSION_PATTERN = Pattern.compile(".*/[0-9]+.*");

    @Autowired
    public SynBioHandler(SynBioClient client) {
        // that should be probably injected in autowired constructor
        //this.jsonParser = JsonParserFactory.getJsonParser();        
        this(client, JsonParserFactory.getJsonParser(), new ComponentFlattener(), 
                new ComponentUtil(), new ComponentAnnotator(), new LibraryGenerator());
    }

    protected SynBioHandler(SynBioClient client, JsonParser jsonParser, 
            ComponentFlattener flattener, ComponentUtil compUtil,ComponentAnnotator annotator,
            LibraryGenerator generator) {
        this.client = client;
        this.flattener = flattener;
        this.compUtil = compUtil;
        this.annotator = annotator;
        this.generator = generator;
        this.jsonParser = jsonParser;
        this.SBOL_DISP_ID_TYPE = encodeURL("<http://sbols.org/v2#displayId>");
    }
    
    protected static String encodeURL(String url) {
        try {
            return URLEncoder.encode("<http://sbols.org/v2#displayId>", StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }        
    }
    
    public void handle(CommandOptions command) throws URISyntaxException, IOException {
        switch (command.command) {
            case DEPOSIT: handleDeposit(command); break;
            case UPDATE: handleUpdate(command); break;
            case GENERATE: handleGenerate(command); break;
            case CYANO: handleCyano(command); break;
            case CLEAN: handleClean(command); break;
            case FLATTEN: handleFlatten(command); break;
            case ANNOTATE: handleAnnotate(command); break;
            case TEMPLATE4UPDATE: handleTemplate4Update(command); break;
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

        processUpdateExcel(parameters);
    }

    void handleCyano(CommandOptions parameters) throws URISyntaxException, IOException {
        PlasmidsGenerator generator = new PlasmidsGenerator();
        String name = parameters.filenamePrefix;
        String version = parameters.version;
        Path templateFile = Paths.get(parameters.templateFile);
        Path flankFile = Paths.get(parameters.metaFile);
        Path outDir = Paths.get(parameters.outputDir);

        try {
            generator.generateFromFiles(name, version, templateFile, flankFile, outDir);
        } catch (SBOLValidationException | SBOLConversionException | ed.biordm.sbol.toolkit.transform.SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
    
    void handleGenerate(CommandOptions parameters) throws URISyntaxException, IOException {
        String name = parameters.filenamePrefix;
        String defVersion = parameters.version;
        Path templateFile = Paths.get(parameters.templateFile);
        Path metaFile = Paths.get(parameters.metaFile);
        Path outDir = Paths.get(parameters.outputDir);
        boolean stopOnMissing = parameters.stopOnMissingMeta;
        
        int batchSize = generator.DEF_BATCH;
        generator.DEBUG = true;
        Outcome outcome = generator.generateFromFiles(name, defVersion, templateFile, metaFile, outDir, stopOnMissing, batchSize);
        
            // TODO printout outcome status, for example id of missing meta
            // 
            // if (!outcome.missingMeta.isEmpty()) {
            //  print some designs were missing ....    
            // }....
            
            
        /*try {
            
            generator.generateFromFiles(name, version, templateFile, flankFile, outDir);
        } catch (SBOLValidationException | SBOLConversionException | ed.biordm.sbol.toolkit.transform.SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }*/
    }

    void handleClean(CommandOptions parameters) throws URISyntaxException, IOException {
        SynBioTamer  tamer = new SynBioTamer();
        Path inputFile = Paths.get(parameters.inputFile);
        Path outputFile = Paths.get(parameters.outputFile);
        String namespace = parameters.namespace;
        boolean removeColls = parameters.removeColls;

        SBOLDocument orig;

        try 
        {
            orig = SBOLReader.read(inputFile.toFile());
            SBOLDocument output = tamer.tameForSynBio(orig, namespace, removeColls);
            //output.write(outputFile.toFile());
            saveValidSbol(output,outputFile);
        } catch (SBOLValidationException | SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    void handleFlatten(CommandOptions parameters) throws IOException {
        
        Path inputFile = Paths.get(parameters.inputFile);
        Path outFile = Paths.get(parameters.outputFile);

        SBOLDocument outDoc = emptyDocument();
        if (parameters.namespace != null && !parameters.namespace.isBlank()) {
            outDoc.setDefaultURIprefix(parameters.namespace);
        }

        try {
        
            SBOLDocument inDoc = SBOLReader.read(inputFile.toFile());


            if (parameters.allRoots) {
                flattener.flattenDesigns(inDoc, parameters.suffix, outDoc);
            } else {
                ComponentDefinition comp = compUtil.extractComponent(parameters.compDefinitionId, inDoc);
                flattener.flattenDesign(comp, compUtil.nameOrId(comp)+parameters.suffix, outDoc);
            }

            saveValidSbol(outDoc, outFile);

        } catch (SBOLValidationException | SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }

    void handleAnnotate(CommandOptions parameters) throws IOException, URISyntaxException {
        Path inputFile = Paths.get(parameters.inputFile);
        Path outFile = Paths.get(parameters.outputFile);
        Path metaFile = Paths.get(parameters.metaFile);
        
        
        try {
        
            SBOLDocument doc = SBOLReader.read(inputFile.toFile());

            Outcome outcome = annotator.annotate(doc, metaFile, parameters.overwrite, parameters.stopOnMissingId, parameters.stopOnMissingMeta);
            
            saveValidSbol(doc, outFile);
            
            // TODO printout outcome status, for example missing id and missing meta
            // 
            // if (!outcome.missingId.isEmpty()) {
            //  print some designs were missing ....    
            // }....

        } catch (SBOLValidationException | SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }        
    }

    void handleTemplate4Update(CommandOptions parameters) throws URISyntaxException {
        if (parameters.sessionToken == null) {
            parameters.sessionToken = login(parameters);
        }

        // String csvLogFilename = new SimpleDateFormat("'deposit_log_'yyyy-MM-dd-HH-mm-ss'.csv'").format(new Date());
        // Path csvOutputFile = Paths.get(System.getProperty("user.dir")).resolve(csvLogFilename);
        List<String[]> dataLines = new ArrayList();
        Path outputFile = Paths.get(parameters.outputFile);
        Path inputFile = Paths.get(parameters.inputFile);

        try {
            // do output to CSV file of uploaded designs
            dataLines.addAll(getUploadedDesignProperties(parameters, inputFile));
        } catch (FileNotFoundException | UnsupportedEncodingException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            this.writeLogToCsv(outputFile, dataLines);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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

    void processUpdateExcel(CommandOptions parameters) throws URISyntaxException, IOException {
        FeaturesReader featuresReader = new FeaturesReader();
        String url = client.hubFromUrl(parameters.url);

        // ensure collection URL specifies version
        String verCollUrl = verifyCollectionUrlVersion(parameters);

        final String collUrl = URLEncoder.encode("<"+verCollUrl+">", StandardCharsets.UTF_8.name());

        String filename = parameters.metaFile;
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
                        processUpdateRow(parameters, cwd, collUrl, url, displayId,
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


    protected void processUpdateRow(CommandOptions parameters, String cwd, 
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
        List<Object> designList = jsonParser.parseList(metadata);

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

    protected List<String[]> getUploadedDesignProperties(CommandOptions parameters, Path sbolFile)
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
                }
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

    public void writeLogToCsv(Path csvOutputFile, List<String[]> dataLines) throws IOException {
        // add header row
        String[] header = new String[]{"display_id", "uploaded_name", "original_name", "version", "uri"};
        dataLines.add(0, header);

        try (PrintWriter pw = new PrintWriter(csvOutputFile.toFile())) {
            dataLines.stream().map(this::convertToCSV).forEach(pw::println);
        }
    }

    protected List<Map<String,Object>> listCollectionDesigns(CommandOptions parameters) throws UnsupportedEncodingException, URISyntaxException {
        String url = client.hubFromUrl(parameters.url);

        // ensure collection URL specifies version
        String verCollUrl = verifyCollectionUrlVersion(parameters);

        final String collUrl = encodeURL("<"+verCollUrl+">");

        String objType = "http://sbols.org/v2#ComponentDefinition";
        objType = "ComponentDefinition";

        // retrieve existing design and description
        String requestParams = "/objectType="+objType+"&collection="+collUrl+"&/?offset=0&limit=10";

        String metadata = client.searchMetadata(url, requestParams, parameters.sessionToken);

        //Object design = client.getSubmissionByDisplayId();
        List<Object> designList = jsonParser.parseList(metadata);
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
            List<Object> collList = jsonParser.parseList(metadata);

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
