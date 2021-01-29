/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio;

import ed.biordm.sbol.synbio.dom.CommandOptions;
import ed.biordm.sbol.synbio.prompt.MissingOptionException;
import ed.biordm.sbol.synbio.prompt.UserInputPrompter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 *
 * @author tzielins
 */
@Component
public class SynBioRunner implements ApplicationRunner{

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    final UserInputPrompter prompter;
    
    
    @Autowired
    public SynBioRunner(UserInputPrompter prompter) {
        this.prompter = prompter;
    }
    
    
    @Override
    public void run(ApplicationArguments args) {
        
        
        try {
            CommandOptions command = prompter.getCommandOptions(args); 
            throw new IllegalArgumentException("Logging");
            
        } catch (MissingOptionException e) {
            
            System.out.println(e.getMessage());
            System.out.println(prompter.getUsageTxt());
        } 
    }
    
}
