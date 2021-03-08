/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.client;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author tzielins
 */
public class CommonAnnotations {
    
    static final String SEQ_ONTO_PREF = SequenceOntology.NAMESPACE.toString(); //"http://identifiers.org/so/";
        
    public static final QName CREATOR = new QName("http://purl.org/dc/elements/1.1/","creator","dc");
    
    static final String SBH_PREF = "http://wiki.synbiohub.org/wiki/Terms/synbiohub#";
    public static final QName SBH_DESCRIPTION = new QName(SBH_PREF,"mutableDescription","sbh");     
    public static final QName SBH_NOTES = new QName(SBH_PREF,"mutableNotes","sbh");  
    public final static QName SBH_TOPLEVEL = new QName(SBH_PREF,"topLevel", "sbh");
    public final static QName SBH_OWNED = new QName(SBH_PREF,"ownedBy","sbh");    
    
    public static final String GEN_BANK_PREF = "http://sbols.org/genBankConversion#";
    public static final QName GB_GENE = new QName(GEN_BANK_PREF, "gene","gbconv");
    public static final QName GB_PRODUCT = new QName(GEN_BANK_PREF, "product","gbconv");
    public final static QName GB_FEATURE = new QName(GEN_BANK_PREF,"featureType","gbconv");
    
    
    public static final URI SO(String termId) {
        try {
            return new URI(SEQ_ONTO_PREF+termId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(),e);                    
        }
    }
}
