/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author tzielins
 */
@Service
public class SynBioClient {

    final RestTemplateBuilder restBuilder;
    
    public SynBioClient(RestTemplateBuilder restBuilder) {
        this.restBuilder = restBuilder;
    }

    public String hubFromUrl(String url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
    
    public String login(String hubUrl, String user, String password) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", user);
        body.put("password", password);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        
        String url = hubUrl+"login";
        
        RestTemplate template = restBuilder.build();
        
        ResponseEntity<String> response = template.postForEntity(url, request, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            // what can we get from the SynBioHub reponse ....
            // log details, prepare a message
            //System.out.println(response.getStatusCode());
            //System.out.println(response.getBody());
            String reason = "unknown";
            throw new IllegalStateException("Cound not login: "+reason);
        }
        
        return response.getBody();
    }
    
    // some other params as for API
    public void deposit(String sessionToken, String collectionUrl, Path file) {
        
        String url = hubFromUrl(collectionUrl) + "submit";
        
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        //other params as needed by api
        MultiValueMap<String, Object> body = makeDepositBody(collectionUrl, file);
        
        RestTemplate template = restBuilder.build();
        
        ResponseEntity<String> response = template.postForEntity(url, body, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            // what can we get from the SynBioHub reponse ....
            // log details, prepare a message
            //System.out.println(response.getStatusCode());
            //System.out.println(response.getBody());
            String reason = "fix me";
            throw new IllegalStateException("Cound not deposit: "+reason);
        }        
        
    }

    HttpHeaders authenticatedHeaders(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("Empty session token");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-authorization", sessionToken);
        return headers;
    }

    MultiValueMap<String, Object> makeDepositBody(String collectionUrl, Path file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
