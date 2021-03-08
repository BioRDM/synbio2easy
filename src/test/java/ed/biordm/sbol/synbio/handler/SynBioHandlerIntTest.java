/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

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
    public void testDepositSingleCollection() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoOverwrite() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = false;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        try {
            handler.depositSingleCollection(parameters);
            // this should fail because the collection already exists but overwrite is 0
            assertTrue(false);
        } catch(Exception e) {
            /*assertEquals(HttpClientErrorException.class, e.getClass());
            assertEquals("Submission id and version do not exist", e.getMessage());*/
            System.out.println("Single deposit failed");
        }
    }

    @Test
    public void testDepositSingleCollectionNoOverwriteNewVersion() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = false;
        parameters.version = "2";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoCreateNew() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";
        parameters.overwrite = false;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoCreateNewDoOverwrite() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
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

    @Test
    public void testDepositMultipleCollectionsNoCreateNew() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
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

    @Test
    public void testReadExcel() throws URISyntaxException, IOException {
        File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;
        String filename = file.getAbsolutePath();

        handler.readExcel(parameters, filename);
    }
}
