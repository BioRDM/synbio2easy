/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author jhay
 */
@SpringBootTest
public class SynBioHandlerIntTest {
    
    @TempDir
    Path tmpDir;        

    @Autowired
    SynBioHandler handler;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    String synBioCollUrl;
    Path sbolFilePath;

    public SynBioHandlerIntTest() {
    }
    
    @BeforeEach
    public void setUp() throws URISyntaxException {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.com";
        synBioPassword = "testpass";

        synBioCollUrl = synBioUrl+"user/Johnny/johnny_collection_29_01_21/johnny_collection_29_01_21_collection/1.0";
        sbolFilePath = Paths.get("D://temp//sbol//cyano_sl1099.xml");
        
        synBioUser = "j.hay@epcc.ed.ac.uk";
        synBioPassword = "admin";
    }
    
    @Test
    public void setupWorks() {
        assertNotNull(handler);
        assertNotNull(handler.jsonParser);
    }

    @Test
    public void testDepositSingleCollection() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoOverwrite() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = false;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        try {
            handler.depositSingleCollection(parameters);
            // this should fail because the collection already exists but overwrite is 0
            assertTrue(false);
        } catch(Exception e) {
            /*assertEquals(HttpClientErrorException.class, e.getClass());
            assertEquals("Submission id and version do not exist", e.getMessage());*/
            System.out.println("Single deposit failed");
        }
    }

    @Test
    public void testDepositSingleCollectionNoOverwriteNewVersion() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = false;
        parameters.version = "2";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoCreateNew() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";
        parameters.overwrite = false;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositSingleCollectionNoCreateNewDoOverwrite() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = "http://localhost:7777/user/Johnny/johnny_parent_collection/johnny_parent_collection_collection/1";
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = false;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositSingleCollection(parameters);
    }

    @Test
    public void testDepositMultipleCollections() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = true;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = true;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositMultipleCollections(parameters);
    }

    @Test
    public void testDepositMultipleCollectionsNoCreateNew() throws URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.DEPOSIT);

        parameters.collectionName = "johnny";
        parameters.crateNew = false;
        parameters.dir = "D:\\temp\\sbol\\";
        parameters.url = synBioUrl;
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.fileExtFilter = ".xml";
        parameters.multipleCollections = true;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        handler.depositMultipleCollections(parameters);
    }

    @Test
    public void testReadExcel() throws URISyntaxException, IOException {
        // File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        File file = new File(getClass().getResource("update_designs_tz.xlsx").getFile());
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;
        String filename = file.getAbsolutePath();
        parameters.metaFile = filename;

        handler.processUpdateExcel(parameters);
    }

    @Test
    public void testReadExcelUnsorted() throws URISyntaxException, IOException {
        File file = new File(getClass().getResource("update_designs_test_unsorted.xlsx").getFile());
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;
        String filename = file.getAbsolutePath();
        parameters.metaFile = filename;

        handler.processUpdateExcel(parameters);
    }

    @Test
    public void testVerifyCollectionUrlVersion() throws URISyntaxException, UnsupportedEncodingException {
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        String collPidUrl = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection";
        parameters.url = collPidUrl;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        String verCollUrl = handler.verifyCollectionUrlVersion(parameters);

        assertEquals(collPidUrl.concat("/1.1-alpha"), verCollUrl);
    }

    @Test
    public void testUploadDifferentVersions() throws URISyntaxException, UnsupportedEncodingException {
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        String collPidUrl = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection";
        parameters.url = collPidUrl;
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        String verCollUrl = handler.verifyCollectionUrlVersion(parameters);

        assertEquals(collPidUrl.concat("/1"), verCollUrl);
        
        File file = new File(getClass().getResource("cyano_sl1099.xml").getFile());
        parameters.crateNew = false;
        parameters.fileExtFilter = ".xml";
        parameters.dir = file.getParent();
        parameters.overwrite = true;
        parameters.version = "1";
        parameters.url = verCollUrl;

        // upload version 2.0.0
        replaceStringInFile(file, "1.0.0", "2.0.0");
        handler.depositSingleCollection(parameters);
        
        // upload version 1.1.0
        replaceStringInFile(file, "2.0.0", "1.1.0");
        handler.depositSingleCollection(parameters);
        
        // upload version 1.0.0
        replaceStringInFile(file, "1.1.0", "1.0.0");
        handler.depositSingleCollection(parameters);
    }

    private void replaceStringInFile(File file, String strToReplace, String replaceStr) {
        String originalFilePath = file.getAbsolutePath();
        String originalFileContent = "";

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(originalFilePath));

            String currentReadingLine = reader.readLine();

            while (currentReadingLine != null) {
                originalFileContent += currentReadingLine + System.lineSeparator();
                currentReadingLine = reader.readLine();
            }

            String modifiedFileContent = originalFileContent.replaceAll(strToReplace, replaceStr);

            writer = new BufferedWriter(new FileWriter(originalFilePath));

            writer.write(modifiedFileContent);

        } catch (IOException e) {
            //handle exception
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }

            } catch (IOException e) {
                //handle exception
            }
        }
    }

    @Test
    public void testGenerateLibrary() throws URISyntaxException, IOException {
        CommandOptions parameters = new CommandOptions(Command.GENERATE);
        String outputDir = tmpDir.resolve("designs").toFile().getAbsolutePath();
        new File(outputDir).mkdirs();

        parameters.templateFile = new File(getClass().getResource("cyano_template.xml").getFile()).getAbsolutePath();
        parameters.metaFile = new File(getClass().getResource("flank-list_20200821_fix2_short.xlsx").getFile()).getAbsolutePath();
        parameters.filenamePrefix = "plasmid";
        parameters.outputDir = outputDir;
        parameters.overwrite = true;
        parameters.isOverwriteDef = true;
        parameters.version = "1.0";

        handler.handleGenerate(parameters);
    }

    @Test
    public void testCleanSbolFile() throws URISyntaxException, IOException {
        CommandOptions parameters = new CommandOptions(Command.GENERATE);
        String outputDir = tmpDir.resolve("clean").toFile().getAbsolutePath();
        new File(outputDir).mkdirs();

        parameters.inputFile = new File(getClass().getResource("synbiohub_sbol.xml").getFile()).getAbsolutePath();
        parameters.outputFile = new File(outputDir).toPath().resolve("synbiohub_sbol.xml").toFile().getAbsolutePath();
        parameters.namespace = "DEFAULT_NAMESPACE";
        parameters.removeColls = true;
        parameters.isRemoveCollsDef = true;

        handler.handleClean(parameters);
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

        String token = handler.login(parameters);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;

        Path inputFilePath = Paths.get(new File(getClass().getResource("cyano_template.xml").getFile()).getAbsolutePath());
        List<String[]> dataLines = handler.getUploadedDesignProperties(parameters, inputFilePath);

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

        String token = handler.login(parameters);
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

    @Test
    public void testHandleFlatten() throws IOException {
        CommandOptions parameters = new CommandOptions(Command.GENERATE);
        // Path outputDir = Paths.get("D:\\temp\\sbol\\");
        Path outputDir = tmpDir;
        String outputFile = outputDir.resolve("cyano_template_flatten.xml").toFile().getAbsolutePath();

        parameters.inputFile = new File(getClass().getResource("cyano_template.xml").getFile()).getAbsolutePath();
        parameters.outputFile = outputFile;
        parameters.isAllRootsDef = true;
        parameters.allRoots = true;

        handler.handleFlatten(parameters);
    }

    @Test
    public void testHandleAnnotate() throws IOException, URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.ANNOTATE);
        Path outputDir = Paths.get("D:\\temp\\sbol\\");
        //Path outputDir = tmpDir;
        String outputFile = outputDir.resolve("cyano_template_annotated.xml").toFile().getAbsolutePath();

        parameters.inputFile = new File(getClass().getResource("cyano_sl1099.xml").getFile()).getAbsolutePath();
        parameters.outputFile = outputFile;
        parameters.metaFile = new File(getClass().getResource("update_designs_tz.xlsx").getFile()).getAbsolutePath();
        parameters.isStopOnMissingIdDef = true;
        parameters.stopOnMissingId = false;
        parameters.isStopOnMissingMetaDef = true;
        parameters.stopOnMissingMeta = false;

        /*String nowTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
        parameters.description = "This is Johnny's latest update to the description at "+ nowTime;
        parameters.notes = "This is Johnny's latest update to the notes at "+ nowTime;*/

        handler.handleAnnotate(parameters);
    }

    @Test
    public void testHandleTemplate4Update() throws IOException, URISyntaxException {
        CommandOptions parameters = new CommandOptions(Command.TEMPLATE4UPDATE);
        Path outputDir = Paths.get("D:\\temp\\sbol\\");
        //Path outputDir = tmpDir;
        String outputFile = outputDir.resolve("template4update.csv").toFile().getAbsolutePath();

        parameters.inputFile = new File(getClass().getResource("cyano_sl1099.xml").getFile()).getAbsolutePath();
        parameters.outputFile = outputFile;
        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        /*String nowTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
        parameters.description = "This is Johnny's latest update to the description at "+ nowTime;
        parameters.notes = "This is Johnny's latest update to the notes at "+ nowTime;*/

        handler.handleTemplate4Update(parameters);
    }

}
