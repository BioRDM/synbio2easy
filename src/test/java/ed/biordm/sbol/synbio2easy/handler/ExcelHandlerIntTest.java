/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.handler;

import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import ed.biordm.sbol.synbio2easy.dom.Command;
import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.assertj.core.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 *
 * @author jhay
 */
@SpringBootTest
public class ExcelHandlerIntTest {
    @Autowired
    Environment environment;

    @TempDir
    Path tmpDir;        

    ExcelHandler handler;

    @Autowired
    SynBioClient client;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    public ExcelHandlerIntTest() {
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

        handler = new ExcelHandler(client);

        Assumptions.assumeTrue(Arrays.asList(this.environment.getActiveProfiles()).contains("integration"));
    }

    @Test
    public void testReadExcel() throws URISyntaxException, IOException {
        // File file = new File(getClass().getResource("update_designs_test.xlsx").getFile());
        File file = new File(getClass().getResource("update_designs_tz.xlsx").getFile());
        CommandOptions parameters = new CommandOptions(Command.UPDATE);

        parameters.url = "http://localhost:7777/user/Johnny/johnny_child_collection/johnny_child_collection_collection/1";
        parameters.user = synBioUser;
        parameters.password = synBioPassword;

        String token = handler.client.login(handler.client.hubFromUrl(parameters.url), synBioUser, synBioPassword);
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

        String token = handler.client.login(handler.client.hubFromUrl(parameters.url), synBioUser, synBioPassword);
        System.out.println(token);
        assertNotNull(token);

        parameters.sessionToken = token;
        String filename = file.getAbsolutePath();
        parameters.metaFile = filename;

        handler.processUpdateExcel(parameters);
    }
}
