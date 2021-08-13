/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.handler;

import ed.biordm.sbol.sbol2easy.meta.ExcelMetaReader;
import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import ed.biordm.sbol.sbol2easy.meta.MetaFormat;
import ed.biordm.sbol.sbol2easy.meta.MetaHelper;
import static ed.biordm.sbol.sbol2easy.meta.MetaHelper.setTemplateVariable;
import ed.biordm.sbol.sbol2easy.meta.MetaRecord;
import ed.biordm.sbol.sbol2easy.transform.ComponentAnnotator;
import ed.biordm.sbol.sbol2easy.transform.Outcome;
import ed.biordm.sbol.synbio2easy.client.SynBioClient.SynBioClientException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.sbolstandard.core2.ComponentDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author tzielins
 */
@Service
public class UpdateHandler {
    final SynBioClient client;
    final Logger logger = LoggerFactory.getLogger(this.getClass());


    
    final MetaHelper metaHelper = new MetaHelper();
    final ComponentAnnotator annotator = new ComponentAnnotator();
    

    @Autowired
    public UpdateHandler(SynBioClient client) {
        this.client = client;
    }

    public Outcome updateRecords(String collUrl, Path metaFile, boolean overwriteDesc, String session) throws IOException {
        
        ExcelMetaReader metaReader = new ExcelMetaReader();
        
        MetaFormat metaFormat = metaReader.readMetaFormat(metaFile);
        validateMetaFormat(metaFormat);
        
        List<MetaRecord> metaData = metaReader.readMeta(metaFile, metaFormat);
        metaData = metaHelper.calculateIdFromKey(metaData);
    
        Outcome status = new Outcome();
        
        
        return updateRecords(collUrl, metaData, metaFormat, overwriteDesc, status, session);
        
    }
    
    Outcome updateRecords(String collUrl, List<MetaRecord> metaData, MetaFormat metaFormat, boolean overwriteDesc, Outcome status, String session) {

        collUrl = versionedCollection(collUrl, session);
        
        for (MetaRecord meta : metaData) {
            String displayId = meta.displayId.get();
            
            Optional<String> designUri = findDesignUri(collUrl, displayId, session);
            
            if (designUri.isEmpty()) {
                status.missingId.add(displayId);
                continue;
            }
            
            Optional<ComponentDefinition> definition = findCompDefintion(designUri.get(),session);
            if (definition.isEmpty()) {
                status.missingId.add(displayId);
                continue;
            }
            
            boolean success = updateRecord(definition.get(), designUri.get(), meta, overwriteDesc, session);
            if (success) {
                status.successful.add(displayId);
            } else {
                status.failed.add(displayId);
            }

        }

        return status;    
    }    
    
    void validateMetaFormat(MetaFormat metaFormat) {
        if (metaFormat.displayId.isEmpty())
            throw new IllegalArgumentException("DisplayId must be present in the meta description table");
    } 
    
    String versionedCollection(String collUrl, String session) {
        return client.verifyCollectionUrlVersion(collUrl, session);
    }
    
    Optional<String> findDesignUri(String collUrl, String displayId, String session) {
        
        return client.findDesignUriInCollection(session, collUrl, displayId);
    }
    
    Optional<ComponentDefinition> findCompDefintion(String designUri, String session) {
        
        try {
            ComponentDefinition def = client.getComponentDefition(session, designUri);
            return Optional.ofNullable(def);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return Optional.empty();
        }
        
    }    

    boolean updateRecord(ComponentDefinition component, String componentUri, MetaRecord meta, boolean overwrite, String session)  {
        
        String displayId = component.getDisplayId();
        String key = meta.key.orElse("");
        
        String name = component.getName() != null ? component.getName() : "";
        if (meta.name.isPresent()) {
            String template = meta.name.get();
            template = setTemplateVariable("displayId", displayId, template);
            template = setTemplateVariable("key", key, template);
            name = template;
        }
        
        try {
            if (meta.attachment.isPresent()) {
                String attachment = annotator.parseTemplate(meta.attachment.get(), displayId, key, name);
                attachFileToDesign(component, componentUri, attachment, overwrite, session);
            }

            if (meta.description.isPresent()) {
                annotator.addDescription(component, meta.description, overwrite, displayId, key, name);
                String desc = annotator.getDescription(component);
                updateDesignDescription(componentUri, desc, session);
            }
            
            if (meta.notes.isPresent()) {
                annotator.addNotes(component, meta.notes, overwrite, displayId, key, name);
                String note = annotator.getNotes(component);
                updateDesignNotes(componentUri, note, session);
            }

            
        } catch (IllegalArgumentException | SynBioClientException e) {
            logger.error("Could not update "+displayId+"; "+e.getMessage(),e);
            return false;
        }
        
        return true;
    }    
    
    
    
    
    void attachFileToDesign(ComponentDefinition component, String componentUri, String attachmentPath, boolean overwrite, String session) throws SynBioClient.SynBioClientException {
        if (attachmentPath.isBlank()) return;
        
        Path file = Paths.get(attachmentPath);
        if (!Files.isRegularFile(file))
            throw new IllegalArgumentException("Cant read file: "+file);
        
        client.attachFile(session, componentUri, file, overwrite);
    }    

    
    void updateDesignDescription(String componentUri, String desc, String session) {
        if (desc == null) return;
        client.setMutableDescription(session, componentUri, desc);
    }    

    
    

    void updateDesignNotes(String componentUri, String note, String session) {
        
        if (note == null) return;        
        client.setNotes(session, componentUri, note);
    }
    














}
