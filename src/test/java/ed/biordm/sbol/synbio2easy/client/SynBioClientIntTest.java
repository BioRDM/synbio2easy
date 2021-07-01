/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.client;

import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import ed.biordm.sbol.synbio2easy.client.CommonAnnotations;
import ed.biordm.sbol.synbio2easy.dom.Command;
import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

        int overwrite = 3;

        client.deposit(token, synBioCollUrl, sbolFilePath, overwrite);
    }

    @Test
    public void multiCollectionDeposit() {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        // Create the child collection and upload a file initially
        String subCollName = "Johnny Child Collection";
        String subCollId = "johnny_child_collection";

        String submitUrl = synBioUrl + "submit";

        String subCollUrl = client.createCollection(token, submitUrl,
                subCollId, "1", subCollName, subCollName, "", 3);

        int overwrite = 3;

        client.deposit(token, subCollUrl, sbolFilePath, overwrite);
        // thos one will fail without overwriting
        client.deposit(token, subCollUrl, sbolFilePath, overwrite);

        subCollName+="2";
        subCollId+="2";
        subCollUrl = client.createCollection(token, submitUrl,
                subCollId, "1", subCollName, subCollName, "", 3);        

        // second collection submit
        client.deposit(token, subCollUrl, sbolFilePath, overwrite);
        
        // not needed
        // Create the parent collection and upload a file
        //String parentCollName = "Johnny Parent Collection";
        //String parentCollId = "johnny_parent_collection";

        //String parentCollUrl = client.createCollection(token, submitUrl, "Johnny",
        //        parentCollId, "1", parentCollName, parentCollName, "", 1);

        //client.deposit(token, parentCollUrl, sbolFilePath);

        // now add the sub collection to the parent collection
        //client.addChildCollection(token, parentCollUrl, subCollUrl+"/");

        // now submit a new object to the sub-collection
        
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

        String subCollUrl = client.createCollection(token, submitUrl, 
                subCollId, "1", subCollName, subCollName, "", 1);

        int overwrite = 1;

        client.deposit(token, subCollUrl, sbolFilePath, overwrite);

        // Create the parent collection and upload a file
        String parentCollName = "Johnny Parent Collection";
        String parentCollId = "johnny_parent_collection";

        String parentCollUrl = client.createCollection(token, submitUrl, 
                parentCollId, "1", parentCollName, parentCollName, "", 1);

        client.deposit(token, parentCollUrl, sbolFilePath, overwrite);

        // now add the sub collection to the parent collection
        client.addChildCollection(token, parentCollUrl, subCollUrl);

        // now submit a new object to the sub-collection
        client.deposit(token, subCollUrl, sbolFilePath, overwrite);
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

        String collUrl = "<http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1>";
        collUrl = URLEncoder.encode(collUrl, StandardCharsets.UTF_8.name());

        String objType = "http://sbols.org/v2#ComponentDefinition";
        objType = "ComponentDefinition";
        //objType = URLEncoder.encode(objType, StandardCharsets.UTF_8.name());

        // this is where we can specify the display ID of the component
        // definition, in the <search string> after the KVP params
        String searchStr = "cyano_codA_Km";
        //searchStr = URLEncoder.encode(searchStr, StandardCharsets.UTF_8.name());

        String requestParams = "/objectType="+objType+"&collection=<"+collUrl+">&"+searchStr+"/?offset=0&limit=10";

        String dispIdType = "<http://sbols.org/v2#displayId>";
        dispIdType = URLEncoder.encode(dispIdType, StandardCharsets.UTF_8.name());

        requestParams = "/objectType="+objType+"&collection="+collUrl+"&"+dispIdType+"='"+searchStr+"'&/?offset=0&limit=10";

        // String metadata = client.searchMetadata(synBioUrl, requestParams, token);
        List<Object> metadata = client.searchMetadata(synBioUrl, requestParams, token);
        System.out.println(metadata);
    }

    @Test
    public void testAttachFile() {
        String attachFilename = "../handler/NC_001499.gbk";
        File file = new File(getClass().getResource(attachFilename).getFile());
        attachFilename = file.getAbsolutePath();

        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";

        int overwrite = 0;

        client.attachFile(token, designUri, attachFilename, overwrite);
    }

    @Test
    public void testUpdateDesignDescription() throws UnsupportedEncodingException {
        String newDesc = "This is a new description, please concatenate";

        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String collUrl = "<http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1>";
        collUrl = URLEncoder.encode(collUrl, StandardCharsets.UTF_8.name());

        String objType = "http://sbols.org/v2#ComponentDefinition";
        objType = "ComponentDefinition";
        
        String dispIdType = "<http://sbols.org/v2#displayId>";
        dispIdType = URLEncoder.encode(dispIdType, StandardCharsets.UTF_8.name());
        String searchStr = "cyano_codA_Km";

        // retrieve existing design and description
        String requestParams = "/objectType="+objType+"&collection="+collUrl+"&"+dispIdType+"='"+searchStr+"'&/?offset=0&limit=10";

        // String metadata = client.searchMetadata(synBioUrl, requestParams, token);
        List<Object> metadata = client.searchMetadata(synBioUrl, requestParams, token);
        System.out.println(metadata);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";
        client.updateDesignText(token, designUri, newDesc, "updateMutableDescription");
    }
    
    @Test
    public void testUpdateDesignNotes() throws UnsupportedEncodingException {
        String newNote = "This is a new note, please concatenate";

        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String collUrl = "<http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1>";
        collUrl = URLEncoder.encode(collUrl, StandardCharsets.UTF_8.name());

        String objType = "http://sbols.org/v2#ComponentDefinition";
        objType = "ComponentDefinition";
        
        String dispIdType = "<http://sbols.org/v2#displayId>";
        dispIdType = URLEncoder.encode(dispIdType, StandardCharsets.UTF_8.name());
        String searchStr = "cyano_codA_Km";

        // retrieve existing design and description
        String requestParams = "/objectType="+objType+"&collection="+collUrl+"&"+dispIdType+"='"+searchStr+"'&/?offset=0&limit=10";

        // String metadata = client.searchMetadata(synBioUrl, requestParams, token);
        List<Object> metadata = client.searchMetadata(synBioUrl, requestParams, token);
        System.out.println(metadata);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";
        client.updateDesignText(token, designUri, newNote, "updateMutableNotes");
    }

    @Test
    public void testGetDesign() throws Exception {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";
        String designXml = client.getDesign(token, designUri);
        assertTrue(designXml.contains("<sbol:persistentIdentity rdf:resource=\"http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/backbone\"/>"));

        designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km";
        designXml = client.getDesign(token, designUri);
        assertTrue(designXml.contains("<sbol:persistentIdentity rdf:resource=\"http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km\"/>"));

        // SBOL method - doesn't work cos the notes/description are sbh namespace
        /*InputStream is = new ByteArrayInputStream(designXml.getBytes(StandardCharsets.UTF_8));
        SBOLDocument doc = SBOLReader.read(is);

        Set<ComponentDefinition> cmpDefs = doc.getComponentDefinitions();

        for(ComponentDefinition cmpDef: cmpDefs) {
            System.out.println(cmpDef.getDescription());
        }*/
    }

    @Test
    public void testAppendToDescription() throws Exception {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        assertNotNull(token);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";

        // get the SBOL document
        String designXml = client.getDesign(token, designUri);
        // System.out.println(designXml);

        // get the existing description and notes from the document
        String curDesc = client.getDesignAnnotation(designXml, designUri, CommonAnnotations.SBH_DESCRIPTION);
        System.out.println("curdesc: "+curDesc);
        String curNotes = client.getDesignAnnotation(designXml, designUri, CommonAnnotations.SBH_NOTES);

        String descToAppend = "Here's a new line in the description";
        String notesToAppend = "Here's a new line in the notes";

        client.appendToDescription(token, designUri, descToAppend);
        client.appendToNotes(token, designUri, notesToAppend);

        // verify the update was successful
        String newDesignXml = client.getDesign(token, designUri);
        // System.out.println(newDesignXml);
        String newDesc = client.getDesignAnnotation(newDesignXml, designUri, CommonAnnotations.SBH_DESCRIPTION);
        String newNotes = client.getDesignAnnotation(newDesignXml, designUri, CommonAnnotations.SBH_NOTES);

        assertEquals(curDesc + "\n" + descToAppend, newDesc);
        assertEquals(curNotes + "\n" + notesToAppend, newNotes);
    }

    @Test
    public void testGetDesignAnnotation() throws Exception {
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        assertNotNull(token);

        String designUri = "http://localhost:7777/user/Johnny/johnny_child_collection/cyano_codA_Km/1.0.0/";

        // get the SBOL document
        String designXml = client.getDesign(token, designUri);

        String description = client.getDesignAnnotation(designXml, designUri, CommonAnnotations.SBH_DESCRIPTION);
        
        assertTrue(description.contains("Here's a new line in the description"));
    }

    @Test
    public void testVerifyCollectionUrlVersion() throws URISyntaxException, UnsupportedEncodingException {
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        String collPidUrl = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection";
        parameters.url = collPidUrl;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        String verCollUrl = client.verifyCollectionUrlVersion(parameters);

        assertEquals(collPidUrl.concat("/1.1-alpha"), verCollUrl);
    }
}
