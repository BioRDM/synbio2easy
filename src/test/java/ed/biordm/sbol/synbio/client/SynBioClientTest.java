/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author tzielins
 */
public class SynBioClientTest {
    
    
    RestTemplate template;    
    MockRestServiceServer server;    
    
    SynBioClient client;
    
    String synBioUrl;
    String synBioUser;
    String synBioPassword;
    
    public SynBioClientTest() {
    }
    
    @BeforeEach
    public void setUp() {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";
        
        template = new RestTemplate();
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(template);
        
        server = MockRestServiceServer.bindTo(template).build();
        
        client = new SynBioClient(builder);
    }

    
    @Test
    public void logsIn() {
        
        String url = synBioUrl+"login";
        
        String token = "bf938ea7-b805-438e-ac26-d1f23592a70";
        
        MultiValueMap<String, String> expected = new LinkedMultiValueMap<>();
        expected.set("email", synBioUser);
        expected.set("password", synBioPassword);      
        
        server.expect(ExpectedCount.once(), 
                MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().formData(expected))     
                .andRespond(MockRestResponseCreators.withSuccess(token, MediaType.APPLICATION_JSON));        
        
        String response = client.login(synBioUrl, synBioUser, synBioPassword);
        assertEquals(token, response);
        
        server.verify();   
    }
    
    @Test
    public void handlesFailedLogins() {
        
        String url = synBioUrl+"login";
        
        server.expect(ExpectedCount.once(), 
                MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withUnauthorizedRequest());

        try {                
            String response = client.login(synBioUrl, synBioUser, synBioPassword);
        } catch (IllegalStateException e) {
            assertEquals("Could not login: Unauthorized", e.getMessage());
        }
        
        server.verify();   
    }  
    
    @Test
    public void getsServerFromURL() throws URISyntaxException {
        String url = "https://synbiohub.org/user/zajawka/trevor_test/trevor_test_collection/1";
        
        assertEquals("https://synbiohub.org/", client.hubFromUrl(url));
    }

    @Test
    public void depositBadUrl() {
        String token = "token";
        int overwrite = 0;

        try {
            client.deposit(token, synBioUrl.replace("/", "\\"),
                    Paths.get("D://temp"), overwrite);
        } catch(IllegalStateException e) {
            assertTrue(e.getMessage().startsWith("Could not derive base SynBioHub server URL: Illegal character in opaque part"));
        }
    }
}
