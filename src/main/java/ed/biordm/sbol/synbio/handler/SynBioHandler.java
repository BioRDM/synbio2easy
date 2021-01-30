/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author tzielins
 */
@Service
public class SynBioHandler {
    
    final SynBioClient client;
    
    @Autowired
    public SynBioHandler(SynBioClient client) {
       this.client = client; 
    }

    public void handle(CommandOptions command) {
        
        switch (command.command) {
            case DEPOSIT: handleDeposit(command);
            default: throw new IllegalArgumentException("Unsuported command: "+command.command);
        }
    }

    void handleDeposit(CommandOptions parameters) {
        
        if (parameters.sessionToken == null) {
            parameters.sessionToken = login(parameters);
        }
        
        if (parameters.multipleCollections) {
            depositMultipleCollections(parameters);            
        } else {
            depositSingleCollection(parameters);
        }
    }
    
    String login(CommandOptions parameters) {
        
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    List<Path> getFiles(CommandOptions parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    
}
