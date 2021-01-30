/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
    

    String synBioUrl;
    String synBioUser;
    String synBioPassword;
    
    public SynBioClientIntTest() {
    }
    
    @BeforeEach
    public void setUp() {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";       
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
    
}
