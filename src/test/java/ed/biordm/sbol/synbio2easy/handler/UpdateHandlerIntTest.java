/*
 * The MIT License
 *
 * Copyright 2021 tzielins.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ed.biordm.sbol.synbio2easy.handler;

import ed.biordm.sbol.sbol2easy.meta.MetaRecord;
import ed.biordm.sbol.sbol2easy.transform.ComponentUtil;
import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.sbolstandard.core2.ComponentDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 *
 * @author tzielins
 */
@SpringBootTest
public class UpdateHandlerIntTest {
    
    @Autowired
    Environment environment;
    
    @Autowired
    SynBioClient client;

    @Autowired
    UpdateHandler handler;   
    
    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    String synBioCollUrl;
    String compId;

    public UpdateHandlerIntTest() {
    }
    
    @BeforeEach
    public void setUp() {
        synBioUrl = "http://localhost:7777/";
        synBioUser = "test@test.ed";
        synBioPassword = "test";

        synBioCollUrl = synBioUrl+"user/test/Testupload/Testupload_collection/1";
        compId = "cs0004_sll0558";
        
        


        Assumptions.assumeTrue(Arrays.asList(this.environment.getActiveProfiles()).contains("integration"));
    }

    @Test
    public void testSetup() {
        assertNotNull(handler);
    }
    
    String testURIFromId(String displayId, String token) {
        
        Optional<String> uri = client.findDesignUriInCollection(token, synBioCollUrl, displayId);
        assertTrue(uri.isPresent());
        
        return uri.get();
        
    }

    @Test
    public void updatesRecord() throws Exception {
        
        String displayId = compId;
        
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        assertNotNull(token);

        String componentUri = testURIFromId(displayId, token);
        
        ComponentDefinition def = client.getComponentDefition(token, componentUri);
        assertNotNull(def);
        
        ComponentUtil util = new ComponentUtil();
        
        String oldDesc = util.getDescription(def);
        String oldNotes = util.getNotes(def);
        if (oldNotes == null) oldNotes = "";
                
        boolean overwrite = false;
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of(displayId);
        String key = LocalDateTime.now().toString();
        meta.key = Optional.of(key);
        
        meta.description = Optional.of("{key}");
        meta.notes = Optional.of("N {displayId}");
        
        boolean res  = handler.updateRecord(def, componentUri, meta, overwrite, token);
        assertTrue(res);
        
        def = client.getComponentDefition(token, componentUri);
        String newDesc = util.getDescription(def);
        String newNotes = util.getNotes(def);
        
        //System.out.println(newDesc);
        //System.out.println(newNotes);
        assertEquals(oldDesc+key, newDesc);
        assertEquals(oldNotes+"N "+displayId, newNotes);
        
        meta.notes = Optional.of(oldNotes != null ? oldNotes : "");
        meta.description = Optional.of(oldDesc);
        overwrite = true;
        handler.updateRecord(def, componentUri, meta, overwrite, token);
        
        def = client.getComponentDefition(token, componentUri);
        newDesc = util.getDescription(def);
        newNotes = util.getNotes(def);
        if (newNotes == null) newNotes = "";
        //System.out.println(newDesc);
        //System.out.println(newNotes);
        assertEquals(oldDesc, newDesc);
        assertEquals(oldNotes, newNotes);
        
    }


    @Test
    public void attachesFile() throws Exception {
        
        
        String displayId = compId;

        Path testFile = Paths.get(this.getClass().getResource(displayId+"_a.gbk").toURI());
        assertTrue(Files.isRegularFile(testFile));
        
        Path file = testFile.getParent().resolve("{displayId}_a.gbk");
        
        String token = client.login(synBioUrl, synBioUser, synBioPassword);
        assertNotNull(token);

        String componentUri = testURIFromId(displayId, token);
        
        ComponentDefinition def = client.getComponentDefition(token, componentUri);
        assertNotNull(def);
             
        boolean overwrite = false;
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of(displayId);
        meta.attachment = Optional.of(file.toString());
                
        boolean res = handler.updateRecord(def, componentUri, meta, overwrite, token);
        assertTrue(res);
        
        meta.attachment = Optional.of("missing");
        res = handler.updateRecord(def, componentUri, meta, overwrite, token);
        assertFalse(res);
        
        
    }
    
}
