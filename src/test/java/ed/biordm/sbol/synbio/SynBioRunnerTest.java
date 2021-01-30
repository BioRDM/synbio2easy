/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio;

import ed.biordm.sbol.synbio.dom.Command;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import ed.biordm.sbol.synbio.handler.SynBioHandler;
import ed.biordm.sbol.synbio.prompt.UserInputPrompter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.boot.DefaultApplicationArguments;

/**
 *
 * @author tzielins
 */
public class SynBioRunnerTest {
    
    UserInputPrompter prompter;
    SynBioHandler handler; 
    SynBioRunner runner;
    
    public SynBioRunnerTest() {
    }
    
    @BeforeEach
    public void setUp() {
         prompter = mock(UserInputPrompter.class);
         handler = mock(SynBioHandler.class);
         runner = new SynBioRunner(prompter, handler);
    }

    @Test
    public void runPassesCommandsToHandler() throws Exception {
        
        DefaultApplicationArguments arg = new DefaultApplicationArguments();
        
        CommandOptions command = new CommandOptions(Command.DEPOSIT);
        when(prompter.getCommandOptions(arg)).thenReturn(command);
        
        runner.run(arg);
        
        verify(prompter).getCommandOptions(arg);
        verify(handler).handle(command);
        
    }
    
}
