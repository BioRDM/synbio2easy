/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author tzielins
 */
@SpringBootTest
public class SynBioClientIntTest {
    
    @Autowired
    SynBioClient client;

    @TempDir
    Path tmpDir;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    String synBioCollUrl;
    Path sbolFilePath;
    
    public SynBioClientIntTest() {
    }
    
    @BeforeEach
    public void setUp() {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";

        synBioCollUrl = synBioUrl+"user/Johnny/johnny_collection_29_01_21/johnny_collection_29_01_21_collection/1.0";
        sbolFilePath = Paths.get("D://temp//sbol//cyano_sl1099.xml");
        
        synBioUser = "j.hay@epcc.ed.ac.uk";
        synBioPassword = "admin";
    }

    @Test
    public void handlesFailedLogins() {
        
        String url = "https://synbiohub.org/";
        String user = "wrong@user.mail";
        String password = "wrong";
        
        try {
            String token = client.login(url, user, password);
            
        } catch (IllegalStateException e) {
            // test if the anwers is user friendly
        }
        
        url = "http://wrong.address.url";
        
        try {
            String token = client.login(url, user, password);
            
        } catch (IllegalStateException e) {
            // test if the anwers is user friendly
        }
    }
    
    @Test
    public void logsIn() {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);
    }

    @Test
    public void deposit() {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        client.deposit(token, synBioCollUrl, sbolFilePath);
    }

    @Test
    public void addSubCollectionDeposit() {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        // Create the child collection and upload a file initially
        String subCollName = "Johnny Child Collection";
        String subCollId = "johnny_child_collection";

        String submitUrl = synBioUrl + "submit";

        String subCollUrl = client.createCollection(token, submitUrl, "Johnny", 
                subCollId, 1, subCollName, subCollName, "", 1);

        client.deposit(token, subCollUrl, sbolFilePath);

        // Create the parent collection and upload a file
        String parentCollName = "Johnny Parent Collection";
        String parentCollId = "johnny_parent_collection";

        String parentCollUrl = client.createCollection(token, submitUrl, "Johnny", 
                parentCollId, 1, parentCollName, parentCollName, "", 1);

        client.deposit(token, parentCollUrl, sbolFilePath);

        // now add the sub collection to the parent collection
        client.addChildCollection(token, parentCollUrl, subCollUrl);

        // now submit a new object to the sub-collection
        client.deposit(token, subCollUrl, sbolFilePath);
    }

    @Test
    public void searchSubmissions() {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String subsData = client.searchSubmissions(synBioUrl, token);
        System.out.println(subsData);
    }

    @Test
    public void searchMetadata() throws Exception {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String collUrl = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";
        // collUrl = "http://localhost:7777/user/Johnny/johnny_parent_collection";

        String requestParams = "objectType=<http://sbols.org/v2#ComponentDefinition>&collection=<"+collUrl+">&";
        requestParams = "objectType=<http://sbols.org/v2#ComponentDefinition>&";
        requestParams = "/"+URLEncoder.encode(requestParams, StandardCharsets.UTF_8.name());
        // requestParams = "/objectType=All&collection="+collUrl;
        String metadata = client.searchMetadata(synBioUrl, requestParams, token);
        System.out.println(metadata);
    }
}
