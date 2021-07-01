/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy;

import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import ed.biordm.sbol.synbio2easy.handler.SynBioHandler;
import ed.biordm.sbol.synbio2easy.prompt.MissingOptionException;
import ed.biordm.sbol.synbio2easy.prompt.UserInputPrompter;
import java.io.IOException;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author tzielins
 */
@Component
@Profile("!test")
public class SynBioRunner implements ApplicationRunner{

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    final UserInputPrompter prompter;
    final SynBioHandler handler;

    @Autowired
    public SynBioRunner(UserInputPrompter prompter, SynBioHandler handler) {
        this.prompter = prompter;
        this.handler = handler;
    }

    @Override
    public void run(ApplicationArguments args) {

        try {
            CommandOptions command = prompter.getCommandOptions(args);             
            handler.handle(command);

        } catch (MissingOptionException | URISyntaxException | IOException e) {
            System.out.println(e.getMessage());
            System.out.println(prompter.getUsageTxt());
        } 
    }

}
