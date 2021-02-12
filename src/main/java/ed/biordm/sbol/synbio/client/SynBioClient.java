/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

    final private static int OVERWRITE_MERGE = 3;
    
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
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        //other params as needed by api
        MultiValueMap<String, Object> body = makeDepositBody(collectionUrl, file);
        final HttpEntity<MultiValueMap<String, Object>> reqEntity = new HttpEntity<>(body, headers);
        
        RestTemplate template = restBuilder.build();
        
        try {
            String response = template.postForObject(url, reqEntity, String.class);
            logger.debug("Response from deposit request: "+response);
        } catch (RuntimeException e) {
            throw reportError("Could not deposit", e);
        }
    }

    public String createCollection(String sessionToken, String url, String user,
            String id, int version, String name, String description,
            String citations, int overwriteMerge) {

        String newUrl = null;

        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));

        //other params as needed by api
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("id", id);
        body.add("version", version);
        body.add("name", name);
        body.add("description", description);
        body.add("citations", citations);
        body.add("overwrite_merge", overwriteMerge);

        final HttpEntity<MultiValueMap<String, Object>> reqEntity = new HttpEntity<>(body, headers);

        RestTemplate template = restBuilder.build();

        try {
            // Use ResponseEntity since it's the best way to check if the response is 200 or not
            // rather than checking if response is spurious String such as "Successfully Uploaded"
            URI uri = URI.create(url);
            final ResponseEntity<String> responseEntity = template.postForEntity(uri, reqEntity, String.class);
            String resBody = responseEntity.getBody();
            logger.debug("Response from deposit request: {}", resBody);
            System.out.println(responseEntity.getStatusCode().value());

            if (responseEntity.getStatusCode().is2xxSuccessful() ||
                    responseEntity.getStatusCode().is3xxRedirection()) {
                // build the collection URL
                newUrl = this.hubFromUrl(url);
                System.out.println(newUrl);
                /*newUrl = newUrl.concat("user/");
                newUrl = newUrl.concat(user+"/");
                newUrl = newUrl.concat(id+"/");
                newUrl = newUrl.concat(id+"_collection/");
                newUrl = newUrl.concat(String.valueOf(version));*/

                // When the request is made to accept only HTML rather than TEXT,
                // the new collection URL is returned in the 'Location' header
                HttpHeaders resHeaders = responseEntity.getHeaders();
                for (Map.Entry<String, List<String>> resHeader : resHeaders.entrySet()) {
                    if (resHeader.getKey().equalsIgnoreCase("Location")) {
                        // strip leading slash since it's already trailing in URL
                        String collPath = resHeader.getValue().get(0);
                        collPath = collPath.replaceFirst("/", "");
                        newUrl = newUrl.concat(collPath);
                    }
                }
            }
        } catch (RuntimeException | URISyntaxException e) {
            throw reportError("Could not create new collection", e);
        }

        logger.info("Newly created collection URL is: {}", newUrl);
        return newUrl;
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
        File fromPath = file.toFile();
        Resource fileResource = new FileSystemResource(fromPath);

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("overwrite_merge", OVERWRITE_MERGE);
        requestMap.add("rootCollections", collectionUrl);
        requestMap.add("file", fileResource);

        return requestMap;
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
