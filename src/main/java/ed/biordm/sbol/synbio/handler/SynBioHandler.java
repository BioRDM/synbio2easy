/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

    @Autowired
    public SynBioHandler(SynBioClient client) {
       this.client = client;
    }

    public void handle(CommandOptions command) throws URISyntaxException {
        switch (command.command) {
            case DEPOSIT: handleDeposit(command); break;
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

    String login(CommandOptions parameters) throws URISyntaxException {
        String url = client.hubFromUrl(parameters.url);

        return client.login(url,parameters.user, parameters.password);
    }


    void depositMultipleCollections(CommandOptions orgParameters) {

        String prefix = orgParameters.collectionName;

        orgParameters.collectionName = prefix+"_"+orgParameters.dir;

        // check whether collection exists first?
        String rootCollUrl = createNewCollection(orgParameters);

        List<Path> subCollections = subfolders(orgParameters.dir);

        for (Path col: subCollections) {
            String suffix = col.getFileName().toString();
            String name = prefix+"_"+suffix;

            CommandOptions params = orgParameters.clone();
            params.collectionName = name;
            params.dir = col.toString();
            params.multipleCollections = false;
            params.crateNew = false;

            String collUrl = createNewCollection(params);
            params.url = collUrl;

            depositSingleCollection(params);

            logger.info("Adding child {} to root URL: {}", collUrl, rootCollUrl);
            addSubCollection(params, rootCollUrl, collUrl);
        }
    }

    void depositSingleCollection(CommandOptions parameters) {
        String collectionUrl = parameters.url;
        if (parameters.crateNew) {
            // woudl be cool if new collection could be created with api or by empty depoist
            // the deposit code would be cleaner;
            collectionUrl = createNewCollection(parameters);
        }

        List<Path> files = getFiles(parameters);

        for (Path file: files) {

            // some other params as needed by the API
            client.deposit(parameters.sessionToken, collectionUrl, file);
        }
    }

    String createNewCollection(CommandOptions parameters) {
        // provide the id, version, name, description, citations
        String name = parameters.collectionName;
        String desc = "Default description for " + name;

        String id = sanitizeName(name);
        //int version = 1;
        int version = Integer.parseInt(parameters.version);
        String citations = "";

        boolean isOverwrite = parameters.overwrite;
        int overwriteMerge = 0; // Assume we are always overwriting

        if(isOverwrite) {
            if(parameters.crateNew) {
                overwriteMerge = 1;
            } else {
                overwriteMerge = 3;
            }
        } else {
            if(parameters.crateNew) {
                overwriteMerge = 0;
            } else {
                overwriteMerge = 2;
            }
        }

        logger.info("URL in parameters: {}", parameters.url);

        String newUrl = client.createCollection(parameters.sessionToken, parameters.url+"submit",
                parameters.user, id, version, name, desc, citations, overwriteMerge);

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

    List<Path> getFiles(CommandOptions parameters) {
        String dirPath = parameters.dir;
        String fileExtFilter = "xml";
        File directory = new File(dirPath);

        List<Path> fileNamesList = new ArrayList<>();

        if (directory.isFile()) {
            fileNamesList.add(Paths.get(dirPath));
        } else {
            Predicate<String> fileExtCondition = (String filename) -> {
                if (filename.toLowerCase().endsWith(".".concat(fileExtFilter))) {
                    return true;
                }
                return false;
            };

            // Reading the folder and getting Stream.
            try (Stream<Path> list = Files.list(Paths.get(dirPath))) {

                // Filtering the paths by a regular file and adding into a list.
                fileNamesList = list.filter(Files::isRegularFile)
                        .map(x -> x.toString()).filter(fileExtCondition)
                        .map(x -> Paths.get(x))
                        .collect(Collectors.toList());

                // printing the file nams
                //fileNamesList.forEach(System.out::println);
            } catch (IOException e) {
                logger.error("Error locating files for upload", e);
            }
        }

        return fileNamesList;
    }

    protected String sanitizeName(String name) {
        String cleanName = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "_");
        return cleanName;
    }

    void readExcel(CommandOptions parameters, String filename) throws URISyntaxException,IOException {
        FeaturesReader featuresReader = new FeaturesReader();
        String url = client.hubFromUrl(parameters.url);

        final String collUrl = URLEncoder.encode("<"+parameters.url+">", StandardCharsets.UTF_8.name());
        //collUrl = URLEncoder.encode(collUrl, StandardCharsets.UTF_8.name());

        final String objType = "ComponentDefinition";

        final String dispIdType = URLEncoder.encode("<http://sbols.org/v2#displayId>", StandardCharsets.UTF_8.name());
        //dispIdType = URLEncoder.encode(dispIdType, StandardCharsets.UTF_8.name());

        File file = new File(filename);

        JsonParser springParser = JsonParserFactory.getJsonParser();

        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, List<String>> rows = featuresReader.readWorksheetRows(sheet, 0);

            rows.forEach((key, value) -> {
                List<String> colVals = (List<String>) value;

                final String displayId = key;
                final String attachFilename = colVals.get(1);
                final String description = colVals.get(2);
                final String notes = colVals.get(3);

                String requestParams = "/objectType="+objType+"&collection="+collUrl+
                    "&"+dispIdType+"='"+displayId+"'&/?offset=0&limit=10";

                String metadata = client.searchMetadata(url, requestParams, parameters.sessionToken);
                //Object design = client.getSubmissionByDisplayId();
                List<Object> designList = springParser.parseList(metadata);

                Object design = designList.get(0);
                String designUri = null;
                
                if(design instanceof Map) {
                    Map<String,Object> map = (Map<String,Object>) design;
                    System.out.println("Items found: " + map.size());
                    designUri = (String)map.get("uri");

                    client.attachFile(parameters.sessionToken, designUri, attachFilename);
                    
                    client.updateDesignDescription(parameters.sessionToken, designUri, description);
                    
                    client.updateDesignNotes(parameters.sessionToken, designUri, notes);
                }
            });


        }
    }
}
