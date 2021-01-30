/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.prompt;
import ed.biordm.sbol.synbio.dom.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
    
}
