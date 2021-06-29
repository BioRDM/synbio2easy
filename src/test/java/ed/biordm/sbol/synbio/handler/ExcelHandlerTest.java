/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jhay
 */
public class ExcelHandlerTest {

    @TempDir
    Path tmpDir;        

    @Autowired
    ExcelHandler handler;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    public ExcelHandlerTest() {
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
    }

    @Test
    public void testReadExcel() throws URISyntaxException, IOException {
        // File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        File file = new File(getClass().getResource("update_designs_tz.xlsx").getFile());
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.client.login(parameters.url, synBioUser, synBioPassword);
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

        String token = handler.client.login(parameters.url, synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;
        String filename = file.getAbsolutePath();
        parameters.metaFile = filename;

        handler.processUpdateExcel(parameters);
    }
}
