/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

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


}
