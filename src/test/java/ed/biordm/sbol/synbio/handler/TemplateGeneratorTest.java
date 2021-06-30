/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import ed.biordm.sbol.toolkit.transform.Outcome;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author jhay
 */
@SpringBootTest
public class TemplateGeneratorTest {

    @TempDir
    Path tmpDir;        

    TemplateGenerator handler;

    @Autowired
    SynBioClient client;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    public TemplateGeneratorTest() {
    }
    
    @BeforeEach
    public void setUp() throws URISyntaxException {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";

        //synBioCollUrl = synBioUrl+"user/Johnny/johnny_collection_29_01_21/johnny_collection_29_01_21_collection/1.0";
        //sbolFilePath = Paths.get("D://temp//sbol//cyano_sl1099.xml");
        
        synBioUser = "j.hay@epcc.ed.ac.uk";
        synBioPassword = "admin";

        handler = new TemplateGenerator(client);
    }

    @Test
    public void testGetUploadedDesignProperties() throws URISyntaxException, FileNotFoundException, UnsupportedEncodingException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);
        String collPidUrl = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = collPidUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.client.login(handler.client.hubFromUrl(parameters.url), synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        Outcome outcome = new Outcome();
        Path inputFilePath = Paths.get(new File(getClass().getResource("cyano_template.xml").getFile()).getAbsolutePath());
        List<String[]> dataLines = handler.getUploadedDesignProperties(parameters,
                inputFilePath, outcome);

        for(String[] dataLine: dataLines) {
            System.out.println(String.join(",", dataLine));
        }
    }

    @Test
    public void testWriteLogToCsv() throws IOException {
        List<String[]> dataLines = new ArrayList();

        // add header row
        String[] row1 = new String[]{"insert", null, "insert", "1.0", "http://localhost:7777/user/Johnny/insert/1.0"};
        String[] row2 = new String[]{"sl1099_left", "sl1099_left", "sl1099_left", "1.0", "http://localhost:7777/user/Johnny/sl1099_left/1.0"};
        String[] row3 = new String[]{"sl1099_right", null, "sl1099/right", "1.0", "http://localhost:7777/user/Johnny/sl1099_right/1.0"};

        dataLines.add(0, row1);
        dataLines.add(1, row2);
        dataLines.add(2, row3);

        Path outputFile = tmpDir.resolve("log.csv");
        handler.writeLogToCsv(outputFile, dataLines);

        try(BufferedReader bfr = new BufferedReader(new FileReader(outputFile.toFile()))) {
            String line = bfr.readLine();
            assertTrue(line.equals("display_id,uploaded_name,original_name,version,uri"));
            int count = 0;

            while(line != null) {
                count++;
                line = bfr.readLine();

                switch (count) {
                    case 1:
                        assertEquals(String.join(",", row1).replaceAll("null", ""), line);
                        break;
                    case 2:
                        assertEquals(String.join(",", row2).replaceAll("null", ""), line);
                        break;
                    case 3:
                        assertEquals(String.join(",", row3).replaceAll("null", ""), line);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Test
    public void testListCollectionDesigns() throws URISyntaxException, UnsupportedEncodingException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);
        String collPidUrl = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection";

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = collPidUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.client.login(handler.client.hubFromUrl(parameters.url), synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        List<Map<String,Object>> designMaps = handler.listCollectionDesigns(parameters);

        List<String> designNames = new ArrayList(Arrays.asList(new String[]{"sl0199_flatten", "sll0199_left", "insert", "sll0199_right"}));
        List<String> designDisplayIds = new ArrayList(Arrays.asList(new String[]{"sl0199_flatten", "sll0199_left", "insert", "sll0199_right"}));
        List<String> designVersions = new ArrayList(Arrays.asList(new String[]{"2.0.0", "2.0.8", "1.1.0", "2.0.6", "2.0.1"}));

        for(Map<String,Object> designMap: designMaps) {
            String upldName = (String)designMap.get("name");
            String upldDisplayId = (String)designMap.get("displayId");
            String upldVersion = (String)designMap.get("version");

            System.out.println(upldName);
            System.out.println(upldDisplayId);
            System.out.println(upldVersion);

            assertTrue(designNames.contains(upldName));
            assertTrue(designDisplayIds.contains(upldDisplayId));
            assertTrue(designVersions.contains(upldVersion));
        }
    }
}
