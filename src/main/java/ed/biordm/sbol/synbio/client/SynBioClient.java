/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.apache.tika.Tika;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

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

        doDeposit(url, sessionToken, collectionUrl, file);
    }

    public void doDeposit(String url, String sessionToken, String collectionUrl, Path file) {
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

    public String createCollection(String sessionToken, String url, 
            String id, String version, String name, String description,
            String citations, int overwriteMerge) {

        String newUrl = null;
        logger.debug(sessionToken);
        logger.debug(url);
        logger.debug(id);
        logger.debug(version);
        logger.debug(name);
        logger.debug(description);
        logger.debug(citations);
        logger.debug(String.valueOf(overwriteMerge));

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
            logger.debug("Response from create collection request: {}", resBody);

            if (responseEntity.getStatusCode().is2xxSuccessful() ||
                    responseEntity.getStatusCode().is3xxRedirection()) {
                // build the collection URL
                newUrl = this.hubFromUrl(url);

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

    public void addChildCollection(String sessionToken, String parentCollectionUrl,
            String childCollectionUrl) {

        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("collections", parentCollectionUrl);

        final HttpEntity<MultiValueMap<String, Object>> reqEntity = new HttpEntity<>(body, headers);

        RestTemplate template = restBuilder.build();

        logger.debug("Parent coll URL: {}", parentCollectionUrl);
        logger.debug("Child coll URL: {}", childCollectionUrl);
        String url = childCollectionUrl+"addToCollection";

        logger.debug("POSTing to URL: {}", url);
        /*try {
            url = hubFromUrl(parentCollectionUrl) + "addToCollection";
        } catch (URISyntaxException e) {
            throw reportError("Could not derive base SynBioHub server URL", e);
        }*/

        try {
            String response = template.postForObject(url, reqEntity, String.class);
            logger.debug("Response from add child collection request: "+response);
        } catch (RuntimeException e) {
            throw reportError("Could not add child collection", e);
        }
    }

    public String searchSubmissions(String hubUrl, String sessionToken) {
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));

        // build the request
        HttpEntity request = new HttpEntity(headers);

        RestTemplate template = restBuilder.build();

        String url = hubUrl + "manage";
        logger.debug("GETting from URL: {}", url);
        String responseData = null;

        try {
            ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, request, String.class, 1);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Request Successful.");
                responseData = response.getBody();
            } else {
                logger.warn("Request Failed: {}", response.getStatusCode());
            }

            return responseData;
        } catch (RuntimeException e) {
            throw reportError("Could search submissions", e);
        }
    }

    public String searchMetadata(String hubUrl, String requestParams, String sessionToken) {
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        //headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));

        // build the request
        HttpEntity request = new HttpEntity(headers);

        // Prevent the template doing any conversion
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        RestTemplate template = new RestTemplate();
        template.setUriTemplateHandler(defaultUriBuilderFactory);

        String url = hubUrl + "search";
        url = url + requestParams;
        logger.debug("GETting from URL: {}", url);
        String responseData = null;

        try {
            ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Request Successful.");
                responseData = response.getBody();
            } else {
                logger.warn("Request Failed: {}", response.getStatusCode());
            }

            return responseData;
        } catch (RuntimeException e) {
            throw reportError("Could not search metadata", e);
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
        File fromPath = file.toFile();
        Resource fileResource = new FileSystemResource(fromPath);

        HttpHeaders parts = new HttpHeaders();

        Tika tika = new Tika();
        String mimeType = null;

        try {
            mimeType = tika.detect(file.toFile());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.debug("Mime type for file {} is {}", file.toFile().getAbsolutePath(), mimeType);

        if(mimeType == null) {
            mimeType = "application/octet-stream";
        }

        parts.setContentType(MediaType.valueOf(mimeType));
        logger.debug("This resolves to {}", MediaType.valueOf(mimeType));

        final HttpEntity<Resource> partsEntity = new HttpEntity<>(fileResource, parts);

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("overwrite_merge", OVERWRITE_MERGE);
        requestMap.add("rootCollections", collectionUrl);
        requestMap.add("file", partsEntity);

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

    public void attachFile(String sessionToken, String designUri, String attachFilename) {
        String url = designUri + "attach";

        Path file = Paths.get(attachFilename);

        doDeposit(url, sessionToken, designUri, file);
    }

    public void appendToDescription(String sessionToken, String designUri, String description) {
        String designXml = getDesign(sessionToken, designUri);
        String currentDesc = getDesignAnnotation(designXml, designUri, CommonAnnotations.SBH_DESCRIPTION);

        if(currentDesc == null) {
            currentDesc = "";
        }

        String newDesc = currentDesc.concat("\n"+description);

        updateDesignText(sessionToken, designUri, newDesc, "updateMutableDescription");
    }

    public void appendToNotes(String sessionToken, String designUri, String notes) {
        String designXml = getDesign(sessionToken, designUri);
        String currentNotes = getDesignAnnotation(designXml, designUri, CommonAnnotations.SBH_NOTES);

        if(currentNotes == null) {
            currentNotes = "";
        }

        String newNotes = currentNotes.concat("\n"+notes);

        updateDesignText(sessionToken, designUri, newNotes, "updateMutableNotes");
    }

    public void updateDesignText(String sessionToken, String designUri, String text, String endpoint) {
        String url;
        try {
            url = hubFromUrl(designUri) + endpoint;
        } catch (URISyntaxException e) {
            throw reportError("Could not derive base SynBioHub server URL", e);
        }

        // Need to remove any trailing slash as it will return a 401 otherwise
        if(designUri.endsWith("/")) {
            designUri = removeLastMatchingChar(designUri, "/");
        }
        doEditPost(sessionToken, url, designUri, text);
    }

    public String getDesign(String sessionToken, String designUri) {
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));

        // build the request
        HttpEntity request = new HttpEntity(headers);

        RestTemplate template = restBuilder.build();

        String responseData = null;

        try {
            ResponseEntity<String> response = template.exchange(designUri, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.debug("Request Successful.");
                responseData = response.getBody();
            } else {
                logger.warn("Request Failed: {}", response.getStatusCode());
            }

            return responseData;
        } catch (RuntimeException e) {
            throw reportError("Could search submissions", e);
        }
    }

    protected void doEditPost(String sessionToken, String url, String designUri, String data) {
        HttpHeaders headers = authenticatedHeaders(sessionToken);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));

        MultiValueMap<String, Object> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("uri", designUri);
        requestMap.add("value", data);

        final HttpEntity<MultiValueMap<String, Object>> reqEntity = new HttpEntity<>(requestMap, headers);

        RestTemplate template = restBuilder.build();

        logger.debug("POSTting to URL: {}", url);
        String responseData = null;
        
        try {
            String response = template.postForObject(url, reqEntity, String.class);
            logger.debug("Response from edit design request: "+response);
        } catch (RuntimeException e) {
            throw reportError("Could not edit design", e);
        }
    }

    protected String removeLastMatchingChar(String s, String lastChar) {
        return Optional.ofNullable(s)
            .filter(str -> str.length() != 0)
            .filter(str -> str.endsWith(lastChar))
            .map(str -> str.substring(0, str.length() - 1))
            .orElse(s);
    }

    protected String getDesignAnnotation(String designXml, String designUri, QName qname) {
        String dataValue = null;

        InputStream is = new ByteArrayInputStream(designXml.getBytes(StandardCharsets.UTF_8));

        SBOLDocument doc;
        try {
            doc = SBOLReader.read(is);
            doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
            ComponentDefinition cmpDef = doc.getComponentDefinition(new URI(removeLastMatchingChar(designUri, "/")));
            Annotation anno = cmpDef.getAnnotation(qname);

            if(anno != null) {
                dataValue = anno.getStringValue();
            }
        } catch (SBOLValidationException | IOException | SBOLConversionException e) {
            logger.error("Unable to read SBOL document", e);
        } catch (URISyntaxException e) {
            logger.error("Unable to set SBOL document prefix", e);
        }

        return dataValue;
    }
}
