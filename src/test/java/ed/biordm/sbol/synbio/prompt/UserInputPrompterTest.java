/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.prompt;
import ed.biordm.sbol.synbio.dom.Command;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.DefaultApplicationArguments;

/**
 *
 * @author tzielins
 */
public class UserInputPrompterTest {

    UserInputPrompter instance;

    public UserInputPrompterTest() {
    }

    @BeforeEach
    public void setUp() {
        // mocking of Console imposible as it is final
        //console = mock(Console.class);
        //instance = new UserInputPrompter(console);
        instance = new UserInputPrompter();
    }

    /*@Test
    public void promptForCommandPrompts() {

        when(console.readLine(anyString(), any())).thenReturn("deposit");

        String command = instance.promptForCommand();
        assertEquals("deposit", command);

        verify(console, times(2)).printf(anyString(), any());
    }*/

    @Test
    public void getCommandGetsCommandFromOptionsIfProvided() throws Exception {

        String[] args = {"deposit", "--user"};

        DefaultApplicationArguments arg = new DefaultApplicationArguments(args);

        Command cmd = instance.getCommand(arg);
        assertEquals(Command.DEPOSIT, cmd);
    }

    @Test
    public void testRenameFileWithSuffix() {
        String inputFilename = "D:\\temp\\sbol\\sl0199.xml";
        String suffix = "_annotated";
        String output = instance.renameFileWithSuffix(inputFilename, suffix);

        String expOutput = "sl0199_annotated.xml";

        assertEquals(expOutput, output);

        Path inputFilePath = Paths.get(inputFilename);
        Path parentDir = inputFilePath.getParent();
        String outputFilePath = parentDir.resolve(output).toFile().getAbsolutePath();

        String expFilePath = "D:\\temp\\sbol\\sl0199_annotated.xml";

        assertEquals(expFilePath, outputFilePath);
    }

    @Test
    public void testRemoveFileExtension() {
        String inputFilename = "D:\\temp\\sbol\\sl0199.xml";
        String output = instance.removeFileExtension(inputFilename, false);

        String expOutput = "D:\\temp\\sbol\\sl0199";

        assertEquals(expOutput, output);

        Path inputFilePath = Paths.get(inputFilename);
        Path parentDir = inputFilePath.getParent();
        String outputFilePath = parentDir.resolve(output.concat(".xlsx")).toFile().getAbsolutePath();

        String expFilePath = "D:\\temp\\sbol\\sl0199.xlsx";

        assertEquals(expFilePath, outputFilePath);
    }
}
