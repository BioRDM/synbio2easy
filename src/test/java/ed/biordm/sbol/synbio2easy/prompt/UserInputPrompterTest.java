/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio2easy.prompt;
import ed.biordm.sbol.synbio2easy.dom.Command;
import ed.biordm.sbol.synbio2easy.dom.CommandOptions;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.ApplicationArguments;
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

        String expOutput = "D:\\temp\\sbol\\sl0199_annotated.xml";

        assertEquals(expOutput, output);

        Path inputFilePath = Paths.get(inputFilename);
        Path parentDir = inputFilePath.getParent();
        String outputFilePath = parentDir.resolve("sl0199_annotated.xml").toFile().getAbsolutePath();

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

    @Test
    public void testDepositCreateNewSingleCollCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("deposit");
        argsList.add("--username=j.hay@epcc.ed.ac.uk");
        argsList.add("--password=pass");
        argsList.add("--dir=examples");
        argsList.add("--file-extension=.xml");
        argsList.add("--multi=N");
        argsList.add("--create-new=Y");
        argsList.add("--name=test collection");
        argsList.add("--url=https://synbiohub.org/");
        argsList.add("--version=1.0");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.DEPOSIT, params.command);
        assertEquals("j.hay@epcc.ed.ac.uk", params.user);
        assertEquals("pass", params.password);
        assertTrue(params.dir.endsWith("examples"));
        assertEquals(".xml", params.fileExtFilter);
        assertEquals(Boolean.FALSE, params.multipleCollections);
        assertEquals(Boolean.TRUE, params.isMultipleCollectionsDef);
        assertEquals(Boolean.TRUE, params.crateNew);
        assertEquals(Boolean.TRUE, params.isCreateNewDef);
        assertEquals("test collection", params.collectionName);
        assertEquals("https://synbiohub.org/", params.url);
        assertEquals("1.0", params.version);
    }

    @Test
    public void testDepositCreateNewMultiCollCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("deposit");
        argsList.add("--username=j.hay@epcc.ed.ac.uk");
        argsList.add("--password=pass");
        argsList.add("--dir=examples");
        argsList.add("--file-extension=.xml");
        argsList.add("--multi=Y");
        argsList.add("--create-new=Y");
        argsList.add("--name=test prefix");
        argsList.add("--url=https://synbiohub.org/");
        argsList.add("--version=1.0");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.DEPOSIT, params.command);
        assertEquals("j.hay@epcc.ed.ac.uk", params.user);
        assertEquals("pass", params.password);
        assertTrue(params.dir.endsWith("examples"));
        assertEquals(".xml", params.fileExtFilter);
        assertEquals(Boolean.TRUE, params.multipleCollections);
        assertEquals(Boolean.TRUE, params.isMultipleCollectionsDef);
        assertEquals(Boolean.TRUE, params.crateNew);
        assertEquals(Boolean.TRUE, params.isCreateNewDef);
        assertEquals("test prefix", params.collectionName);
        assertEquals("https://synbiohub.org/", params.url);
        assertEquals("1.0", params.version);
    }

    @Test
    public void testGenerateCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("generate");
        argsList.add("--output-dir=examples/library");
        argsList.add("--template-file=examples/template.xml");
        argsList.add("--meta-file=examples/library_def.xlsx");
        argsList.add("--filename-prefix=library");
        argsList.add("--overwrite=Y");
        argsList.add("--stop-missing-metadata=N");
        argsList.add("--version=1.0");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.GENERATE, params.command);
        assertTrue(params.outputDir.endsWith("library"));
        assertTrue(params.templateFile.endsWith("examples/template.xml"));
        assertTrue(params.metaFile.endsWith("examples/library_def.xlsx"));
        assertEquals("library", params.filenamePrefix);
        assertEquals(Boolean.TRUE, params.overwrite);
        assertEquals(Boolean.TRUE, params.isOverwriteDef);
        assertEquals(Boolean.FALSE, params.stopOnMissingMeta);
        assertEquals(Boolean.TRUE, params.isStopOnMissingMetaDef);
        assertEquals("1.0", params.version);
    }

    @Test
    public void testFlattenCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("flatten");
        argsList.add("--input-file=examples/library/library.1.xml");
        argsList.add("--output-file=examples/library/library_flattened.1.xml");
        argsList.add("--all-roots=Y");
        argsList.add("--component-id=sll0188");
        argsList.add("--suffix=_flat");
        argsList.add("--overwrite=Y");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.FLATTEN, params.command);
        assertTrue(params.inputFile.endsWith("examples/library/library.1.xml"));
        assertTrue(params.outputFile.endsWith("examples/library/library_flattened.1.xml"));
        assertEquals(Boolean.TRUE, params.allRoots);
        assertEquals(Boolean.TRUE, params.isAllRootsDef);
        assertEquals(Boolean.TRUE, params.overwrite);
        assertEquals(Boolean.TRUE, params.isOverwriteDef);
        assertEquals("_flat", params.suffix);
        assertEquals("sll0188", params.compDefinitionId);
    }

    @Test
    public void testAnnotateCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("annotate");
        argsList.add("--input-file=examples/library/library_flattened.1.xml");
        argsList.add("--meta-file=examples/flat_annotation.xlsx");
        argsList.add("--output-file=examples/library/library_flattened_annotated.1.xml");
        argsList.add("--stop-missing-metadata=N");
        argsList.add("--stop-missing-id=N");
        argsList.add("--overwrite=N");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.ANNOTATE, params.command);
        assertTrue(params.inputFile.endsWith("examples/library/library_flattened.1.xml"));
        assertTrue(params.outputFile.endsWith("examples/library/library_flattened_annotated.1.xml"));
        assertTrue(params.metaFile.endsWith("examples/flat_annotation.xlsx"));
        assertEquals(Boolean.FALSE, params.stopOnMissingId);
        assertEquals(Boolean.TRUE, params.isStopOnMissingIdDef);
        assertEquals(Boolean.FALSE, params.stopOnMissingMeta);
        assertEquals(Boolean.TRUE, params.isStopOnMissingMetaDef);
        assertEquals(Boolean.FALSE, params.overwrite);
        assertEquals(Boolean.TRUE, params.isOverwriteDef);
    }

    @Test
    public void testCleanCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("clean");
        argsList.add("--input-file=examples/library/library_downloaded.1.xml");
        argsList.add("--output-file=examples/library/library_downloaded_cleaned.1.xml");
        argsList.add("--namespace=http://biordm.sbs.ed.ac.uk");
        argsList.add("--remove-collections=Y");
        // argsList.add("--overwrite=N");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.CLEAN, params.command);
        assertTrue(params.inputFile.endsWith("examples/library/library_downloaded.1.xml"));
        assertTrue(params.outputFile.endsWith("examples/library/library_downloaded_cleaned.1.xml"));
        assertEquals("http://biordm.sbs.ed.ac.uk", params.namespace);
        assertEquals(Boolean.TRUE, params.removeColls);
        assertEquals(Boolean.TRUE, params.isRemoveCollsDef);
    }

    @Test
    public void testTemplate4UpdateCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("synbio2table");
        argsList.add("--output-file=examples/library/template_4_update.xlsx");
        argsList.add("--url=http://synbiohub.org/user/johnny/test_13_07_21/test_13_07_21_collection/1.0");
        argsList.add("--username=j.hay@epcc.ed.ac.uk");
        argsList.add("--password=pass");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.SYNBIO2TABLE, params.command);
        assertTrue(params.outputFile.endsWith("examples/library/template_4_update.xlsx"));
        assertEquals("http://synbiohub.org/user/johnny/test_13_07_21/test_13_07_21_collection/1.0", params.url);
        assertEquals("j.hay@epcc.ed.ac.uk", params.user);
        assertEquals("pass", params.password);
    }

    @Test
    public void testUpdateCommandPassedArgs() throws MissingOptionException {
        List<String> argsList = new ArrayList();
        argsList.add("update");
        argsList.add("--meta-file=examples/library/template_4_update.xlsx");
        argsList.add("--url=http://synbiohub.org/user/johnny/test_13_07_21/test_13_07_21_collection/1.0");
        argsList.add("--username=j.hay@epcc.ed.ac.uk");
        argsList.add("--password=pass");

        String[] args = argsList.toArray(new String[argsList.size()]);
        ApplicationArguments appArgs = new DefaultApplicationArguments(args);

        CommandOptions params = null;

        params = instance.getCommandOptions(appArgs);

        assertEquals(Command.UPDATE, params.command);
        assertTrue(params.metaFile.endsWith("examples/library/template_4_update.xlsx"));
        assertEquals("http://synbiohub.org/user/johnny/test_13_07_21/test_13_07_21_collection/1.0", params.url);
        assertEquals("j.hay@epcc.ed.ac.uk", params.user);
        assertEquals("pass", params.password);
    }
}
