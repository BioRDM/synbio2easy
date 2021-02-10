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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
            case DEPOSIT: handleDeposit(command);
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
        
        List<Path> subCollections = subfolders(orgParameters.dir);
        
        for (Path col: subCollections) {
            String suffix = col.getFileName().toString();
            String name = prefix+"_"+suffix;
            
            CommandOptions params = orgParameters.clone();
            params.collectionName = name;
            params.dir = col.toString();
            params.multipleCollections = false;
            
            depositSingleCollection(params);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
