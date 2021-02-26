/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author jhay
 */
@SpringBootTest
public class SynBioHandlerIntTest {
    
    @TempDir
    Path tmpDir;        

    @Autowired
    SynBioHandler handler;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    String synBioCollUrl;
    Path sbolFilePath;

    public SynBioHandlerIntTest() {
    }
    
    @BeforeEach
    public void setUp() throws URISyntaxException {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";

        synBioCollUrl = synBioUrl+"user/Johnny/johnny_collection_29_01_21/johnny_collection_29_01_21_collection/1.0";
        sbolFilePath = Paths.get("D://temp//sbol//cyano_sl1099.xml");
        
        synBioUser = "j.hay@epcc.ed.ac.uk";
        synBioPassword = "admin";
    }

    @Test
    public void testDepositMultipleCollections() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = true;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositMultipleCollections(parameters);
    }
}
