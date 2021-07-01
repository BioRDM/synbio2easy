/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy;

import ed.biordm.sbol.synbio2easy.SynBioRunner;
import ed.biordm.sbol.synbio2easy.client.SynBioClient;
import ed.biordm.sbol.synbio2easy.dom.Command;
import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import ed.biordm.sbol.synbio2easy.handler.SynBioHandler;
import ed.biordm.sbol.synbio2easy.prompt.UserInputPrompter;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author tzielins
 */
@SpringBootTest
public class SynBioRunnerIntTest {

    //@Autowired
    UserInputPrompter prompter;

    // @Autowired
    SynBioHandler handler;

    //@Autowired
    SynBioRunner runner;

    @Autowired
    SynBioClient client;

    @TempDir
    Path tmpDir;

    String synBioUrl;
    String synBioUser;
    String synBioPassword;

    String synBioCollUrl;
    Path sbolFilePath;

    BufferedReader mockConsole;

    public SynBioRunnerIntTest() {
    }

    @BeforeEach
    public void setUp() {
         /*prompter = mock(UserInputPrompter.class);
         handler = mock(SynBioHandler.class);
         runner = new SynBioRunner(prompter, handler);*/

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // mockConsole = mock(br);
        // prompter = new UserInputPrompter(mockConsole);

        prompter = new UserInputPrompter();
        handler = new SynBioHandler(client);
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

    @Test
    public void runDepositCommand() throws Exception {
        String[] args = {"deposit", "--user"};
        DefaultApplicationArguments arg = new DefaultApplicationArguments(args);

        CommandOptions command = new CommandOptions(Command.DEPOSIT);
        //when(prompter.getCommandOptions(arg)).thenReturn(command);

        runner.run(arg);

        verify(prompter).getCommandOptions(arg);
        verify(handler).handle(command);

    }
}
