/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import ed.biordm.sbol.synbio.client.SynBioClient;
import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.*;

/**
 *
 * @author tzielins
 */
public class SynBioHandlerTest {
    
    @TempDir
    Path tmpDir;        
    
    SynBioClient client;
    SynBioHandler handler;
    
    String url = "http://synbio.org/toconvert";
    String user = "user";
    String pass = "pass";

    public SynBioHandlerTest() {
    }
    
    @BeforeEach
    public void setUp() throws URISyntaxException {
        client = mock(SynBioClient.class);
        
        when(client.hubFromUrl(anyString())).thenReturn("http://synbio.org/");
        
        handler = new SynBioHandler(client);
    }

    @Test
    public void loginCallsLoginWithExtractedHubUrl() throws URISyntaxException {        
        CommandOptions params = new CommandOptions(Command.DEPOSIT);
        params.url = url;
        params.user = user;
        params.password = pass;
        
        when(client.login(eq("http://synbio.org/"), eq(user), eq(pass))).thenReturn("123");
        
        String session = handler.login(params);
        assertEquals("123",session);
        
    }
    
    @Test
    public void subfoldersGivesChildren() throws Exception {
        assertNotNull(tmpDir);
        
        Files.createDirectory(tmpDir.resolve("dir1"));
        Files.createDirectory(tmpDir.resolve("dir2"));
        Files.createFile(tmpDir.resolve("file.xml"));
        
        List<Path> dirs = handler.subfolders(tmpDir.toString());
        assertEquals(2, dirs.size());
    }

    @Test
    public void testGetFiles() throws Exception {
        assertNotNull(tmpDir);
        
        Files.createDirectory(tmpDir.resolve("dir1"));
        Files.createDirectory(tmpDir.resolve("dir2"));

        String newFileName = "test_file.xml";
        Files.createFile(tmpDir.resolve(newFileName));
        
        CommandOptions params = new CommandOptions(Command.DEPOSIT);
        params.url = url;
        params.user = user;
        params.password = pass;
        params.dir = tmpDir.toString();

        List<Path> files = handler.getFiles(params);
        assertEquals(1, files.size());
        assertEquals(newFileName, files.get(files.size()-1).getFileName().toString());

        newFileName = "test_file2.xml";
        Files.createFile(tmpDir.resolve(newFileName));

        files = handler.getFiles(params);
        assertEquals(2, files.size());
        assertEquals(newFileName, files.get(files.size()-1).getFileName().toString());

        String badFileName = "test_file2.xml2";
        Files.createFile(tmpDir.resolve(badFileName));

        files = handler.getFiles(params);
        assertEquals(2, files.size());
        assertEquals(newFileName, files.get(files.size()-1).getFileName().toString());
    }

    @Test
    public void testReadExcel() {

    }
}
