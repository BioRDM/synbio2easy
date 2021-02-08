/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author tzielins
 */
@Service
public class SynBioClient {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final RestTemplateBuilder restBuilder;
    
    public SynBioClient(RestTemplateBuilder restBuilder) {
        this.restBuilder = restBuilder;
    }

    public String hubFromUrl(String url) throws URISyntaxException {
        URI collectionUri = new URI(url);
        String scheme = collectionUri.getScheme();
        String domain = collectionUri.getHost();
        int port = collectionUri.getPort();
        String collServerUrl = scheme.concat("://").concat(domain);

        if (port > 0) {
            collServerUrl = collServerUrl.concat(":").concat(String.valueOf(port));
        }

        collServerUrl = collServerUrl.concat("/");
        return collServerUrl;
    }    
    
    public String login(String hubUrl, String user, String password) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", user);
        body.add("password", password);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        
        String url = hubUrl+"login";
        
        RestTemplate template = restBuilder.build();
        
        try {
            String response = template.postForObject(url, request, String.class);
            return response;
        } catch (RuntimeException e) {
            throw reportError("Could not login", e);
        }
        
    }
    
    // some other params as for API
    public void deposit(String sessionToken, String collectionUrl, Path file) {
        
        String url;
        try {
            url = hubFromUrl(collectionUrl) + "submit";
        } catch (URISyntaxException e) {
            throw reportError("Could not derive base SynBioHub server URL", e);
        }
        
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        //other params as needed by api
        MultiValueMap<String, Object> body = makeDepositBody(collectionUrl, file);
        
        RestTemplate template = restBuilder.build();
        
        try {
            String response = template.postForObject(url, body, String.class);
        } catch (RuntimeException e) {
            throw reportError("Could not deposit", e);
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


    IllegalStateException reportError(String operation, Exception e) {
        
        logger.error(operation, e);
        
        String msg = operation+": "+e.getMessage();
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException he = (HttpClientErrorException)e;
            msg = operation+": "+he.getStatusText(); 
        }
        return new IllegalStateException(msg);
    }    

    
}
