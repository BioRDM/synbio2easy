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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    final ExcelHandler excelHandler;
    final TemplateGenerator templateGenerator;

    final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    // Excel column header strings - worksheet headers must match these to work
    private static final String DISP_ID_HEADER = "display_id";
    private static final String ATTACH_FILE_HEADER = "attachment_filename";
    private static final String DESC_HEADER = "description";
    private static final String NOTES_HEADER = "notes";

    @Autowired
    public SynBioHandler(SynBioClient client) {
        // that should be probably injected in autowired constructor
        //this.jsonParser = JsonParserFactory.getJsonParser();        
        this(client, new ComponentFlattener(),
                new ComponentUtil(), new ComponentAnnotator(),
                new LibraryGenerator(), new ExcelHandler(client),
                new TemplateGenerator(client));
    }

    protected SynBioHandler(SynBioClient client,
            ComponentFlattener flattener, ComponentUtil compUtil,
            ComponentAnnotator annotator, LibraryGenerator generator,
            ExcelHandler excelHandler, TemplateGenerator templateGenerator) {
        this.client = client;
        this.flattener = flattener;
        this.compUtil = compUtil;
        this.annotator = annotator;
        this.generator = generator;
        this.excelHandler = excelHandler;
        this.templateGenerator = templateGenerator;
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

        Outcome outcome = excelHandler.processUpdateExcel(parameters);

        // TODO printout outcome status, for example id of missing meta
        //
        // if (!outcome.missingMeta.isEmpty()) {
        //  print some designs were missing ....
        // }....
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
        } catch (SBOLValidationException | SBOLConversionException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
    
    void handleGenerate(CommandOptions parameters) throws IOException {
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
                // should add handling of what was actually flattened or not
                flattener.flattenDesigns(inDoc, parameters.suffix, outDoc, false);
            } else {
                ComponentDefinition comp = compUtil.extractComponent(parameters.compDefinitionId, inDoc);
                flattener.flattenDesign(comp, comp.getDisplayId()+parameters.suffix,
                                              compUtil.nameOrId(comp)+parameters.suffix, outDoc);
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
        Outcome outcome = templateGenerator.generateTemplate(parameters);

        // TODO printout outcome status, for example id of missing meta
        //
        // if (!outcome.missingMeta.isEmpty()) {
        //  print some designs were missing ....
        // }....
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
                    client.getOverwriteParam(parameters, true));
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
        int overwriteMerge = client.getOverwriteParam(parameters, false);

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
}
