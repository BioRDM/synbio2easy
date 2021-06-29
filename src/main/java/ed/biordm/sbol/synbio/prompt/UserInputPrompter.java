/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.prompt;

import ed.biordm.sbol.synbio.dom.Command;
import static ed.biordm.sbol.synbio.dom.Command.*;
import ed.biordm.sbol.synbio.dom.CommandOptions;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 *
 * @author tzielins
 */
@Component
public class UserInputPrompter {

    final Console console;

    private static final Pattern Y_N_PATTERN = Pattern.compile("^(Y|y|Yes|yes|N|n|No|no){1}$");
    private static final Pattern Y_PATTERN = Pattern.compile("^(Y|y|Yes|yes){1}$");
    private static final Pattern FILE_EXT_PATTERN = Pattern.compile("\\.([a-zA-Z0-9]{1,6})|\\.(\\*{1})");

    public UserInputPrompter() {
        this(System.console());

    }

    // for testing so that console can be mocked
    protected UserInputPrompter(Console console) {
        this.console = console;
    }

    public CommandOptions getCommandOptions(ApplicationArguments args) throws MissingOptionException {

        Command command = getCommand(args);

        CommandOptions options = new CommandOptions(command);
        setPassedOptions(options, args);

        switch (command) {
            case DEPOSIT: return promptDepositOptions(options);
            case UPDATE: return promptUpdateOptions(options);
            case GENERATE: return promptGenerateOptions(options);
            case CYANO: return promptCyanoOptions(options);
            case CLEAN: return promptCleanOptions(options);
            case FLATTEN: return promptFlattenOptions(options);
            case ANNOTATE: return promptAnnotateOptions(options);
            case TEMPLATE4UPDATE: return promptTemplate4UpdateOptions(options);
            default: throw new IllegalArgumentException("Unsupported command: "+command);
        }
    }

    Command getCommand(ApplicationArguments args) throws MissingOptionException {

        String command;

        if (args.getNonOptionArgs().isEmpty()) {
            command = promptForCommand();
        } else {
            command = args.getNonOptionArgs().get(0);
        }

        switch(Command.valueOf(command.toUpperCase())) {
            case DEPOSIT: return DEPOSIT;
            case UPDATE: return UPDATE;
            case GENERATE: return GENERATE;
            case CYANO: return CYANO;
            case CLEAN: return CLEAN;
            case FLATTEN: return FLATTEN;
            case ANNOTATE: return ANNOTATE;
            case TEMPLATE4UPDATE: return TEMPLATE4UPDATE;
            default: throw new MissingOptionException("Unknown command "+command);
        }
    }

    String promptForCommand() {

        console.printf("%n");
        console.printf("Welcome to the SynBioHub Client!%n");
        console.printf("To exit the application at any time, press <CTRL> + C%n");

        String command = "";

        while(command.isBlank()) {
            console.printf("%n");
            console.printf("What operation do you want to perform?%n");
            StringJoiner joiner = new StringJoiner(" | ", "", "");

            for (Command cmd : Command.values()) {
                joiner.add(String.valueOf(cmd).toLowerCase());
            }
            // Arrays.asList(Command.values()).forEach(joiner::add);

            console.printf("Choose from: %s%n", joiner.toString());
            command = chooseOperation();
        }

        return command;
    }

    String chooseOperation() {
        boolean validCmd = false;
        Command selectedCmd = null;
        String command = "";

        while(validCmd == false) {
            command = console.readLine("Operation: ");
            for (Command cmd : Command.values()) {
                if (cmd.name().equalsIgnoreCase(command)) {
                    validCmd = true;
                    selectedCmd = cmd;
                }
            }
        }

        console.printf("%n");
        console.printf(selectedCmd.getGuidanceText());
        console.printf("%n");

        console.printf("Do you wish to continue?%n");
        String cmdAns = console.readLine("Y | N: ").strip();

        while(!Y_N_PATTERN.matcher(cmdAns).matches()) {
            cmdAns = console.readLine("Y | N: ").strip();
        }

        if (!Y_PATTERN.matcher(cmdAns).matches()) {
            command = "";
            console.printf("%n");
        }

        return command;
    }

    CommandOptions promptDepositOptions(CommandOptions options) {

        console.printf("%n");
        console.printf("... depositing designs into SynBioHub%n");
        console.printf("%n");

        if (options.dir == null) {
            console.printf("Please enter the directory path to upload%n");
            options.dir = console.readLine("Directory path [<ENTER> for current directory]: ");

            if (!validateDirPath(options.dir)) {
                if(options.dir.isEmpty()) {
                    options.dir = System.getProperty("user.dir");
                } else {
                    boolean isInput = true;    // must exist because it's input directory
                    Path inputPath = null;
                    try {
                        inputPath = validateInputPath(options.dir, isInput);
                        options.dir = inputPath.toFile().getAbsolutePath();
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid directory path argument: "+options.dir);
                    }

                    if (inputPath == null || !inputPath.toFile().exists()) {
                        throw new IllegalArgumentException("Invalid directory path argument: "+options.dir);
                    }
                }
            }
        } else {
            console.printf("Directory: %s", options.dir);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.fileExtFilter == null) {
            console.printf("Which type of file extensions do you wish to upload?%n");
            options.fileExtFilter = console.readLine("File extension [<ENTER> for any (.*)]: ");

            if (!validateString(FILE_EXT_PATTERN, options.fileExtFilter)) {
                if(options.fileExtFilter.isEmpty()) {
                    options.fileExtFilter = ".*";
                } else {
                    throw new IllegalArgumentException("Invalid file extension filter argument: "+options.fileExtFilter);
                }
            }
        } else {
            console.printf("File extension filter: %s", options.fileExtFilter);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isMultipleCollectionsDef == false && options.multipleCollections == false) {
            // check for sub-folders
            boolean isSubFolders = isSubFolders(options.dir);

            if(isSubFolders) {
                console.printf("Do you wish to create multiple collections?%n");
                String multipleAns = console.readLine("Y | N: ").strip();
                while(!Y_N_PATTERN.matcher(multipleAns).matches()) {
                    multipleAns = console.readLine("Y | N: ").strip();
                }

                if (Y_PATTERN.matcher(multipleAns).matches()) {
                    // set this automatically since we must create new collections for multiple collections
                    options.crateNew = true;
                    options.multipleCollections = true;
                    console.printf("Each sub-folder in the selected directory will be uploaded to SynBioHub as a separate collection%n");
                } else {
                    console.printf("Only the files in the top level directory (no sub-directories) will be submitted to SynBioHub%n");
                }
            }
        } else {
            console.printf("Each sub-folder in the selected directory will be uploaded to SynBioHub as a separate collection%n");
        }

        if (options.isCreateNewDef == false && options.crateNew == false) {
            console.printf("%n");
            console.printf("Do you wish to create a new collection?%n");
            String createNewAns = console.readLine("Y | N: ").strip();
            while(!Y_N_PATTERN.matcher(createNewAns).matches()) {
                createNewAns = console.readLine("Y | N: ").strip();
            }

            if (Y_PATTERN.matcher(createNewAns).matches()) {
                options.crateNew = true;
            } else {
                options.crateNew = false;
            }
        } else {
            console.printf("Creating new collection(s)");
            console.printf("%n");
        }

        console.printf("%n");

        if (options.crateNew == true) {
            if (options.multipleCollections == false) {
                if (options.collectionName == null) {
                    console.printf("Please enter a name for the new collection%n");
                    options.collectionName = console.readLine("Name: ");
                } else {
                    console.printf("New collection name: %s", options.collectionName);
                    console.printf("%n");
                }
            } else {
                if (options.collectionName == null) {
                    console.printf("Please enter a prefix for the new collections%n");
                    options.collectionName = console.readLine("Prefix [<ENTER> for no prefix]: ");
                } else {
                    console.printf("New collection prefix: %s", options.collectionName);
                    console.printf("%n");
                }  
            }

            console.printf("%n");

            if (options.url == null) {
                console.printf("Please enter the URL of the SynBioHub server%n");
                options.url = console.readLine("URL [<ENTER> for https://synbiohub.org]: ");

                if (options.url.isBlank()) {
                    options.url = "https://synbiohub.org";
                }
                options.url = sanitizeUrl(options.url);
            } else {
                console.printf("SynBioHub URL: %s", options.url);
                console.printf("%n");
            }
        } else {
            if (options.url == null) {
                console.printf("Please enter the URL for the existing collection%n");
                options.url = console.readLine("URL: ");
            } else {
                console.printf("Collection URL: %s", options.url);
                console.printf("%n");
            }
        }

        if (options.version == null) {
            if (options.crateNew == true) {
                console.printf("%n");
                console.printf("Please enter the version number%n");
                options.version = console.readLine("Version [<ENTER> for 1.0]: ");

                if (options.version == null || options.version.trim().isEmpty()) {
                    options.version = "1.0";
                }
            }
        } else {
            console.printf("%n");
            console.printf("Version: %s", options.version);
            console.printf("%n");
        }

        if (options.isOverwriteDef == false && options.overwrite == false) {
            if (options.crateNew == true) {
                options.overwrite = false;
            } else {
                console.printf("%n");

                console.printf("Do you wish to overwrite designs if they exist?%n");
                String overwriteAns = console.readLine("Y | N: ").strip();

                while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                    overwriteAns = console.readLine("Y | N: ").strip();
                }

                if (Y_PATTERN.matcher(overwriteAns).matches()) {
                    options.overwrite = true;
                } else {
                    options.overwrite = false;
                }
            }
        } else {
            console.printf("%n");
            console.printf("Overwrite: %s", options.overwrite);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.user == null) {
            console.printf("Please enter your SynBioHub username%n");
            options.user = console.readLine("Username (email): ");
        } else {
            console.printf("Username: %s", options.user);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.password == null) {
            console.printf("Please enter your SynBioHub password%n");
            options.password = new String(console.readPassword("Password: "));
        } else {
            console.printf("Password: *****");
            console.printf("%n");
        }

        console.printf("%n");

        return options;
    }

    CommandOptions promptUpdateOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... updating existing designs in SynBioHub%n");
        console.printf("%n");

        if (options.metaFile == null) {
            console.printf("Please enter the path to the Excel file with designs to update%n");
            options.metaFile = console.readLine("Filename: ");

            if (!validateDirPath(options.metaFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.dir, isInput);
                    options.metaFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }
            }
        } else {
            console.printf("Excel File: %s", options.metaFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.url == null) {
            console.printf("Please enter the URL for the collection to update%n");
            options.url = console.readLine("URL: ");
        } else {
            console.printf("Collection URL: %s", options.url);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.user == null) {
            console.printf("Please enter your SynBioHub username%n");
            options.user = console.readLine("Username (email): ");
        } else {
            console.printf("Username: %s", options.user);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.password == null) {
            console.printf("Please enter your SynBioHub password%n");
            options.password = new String(console.readPassword("Password: "));
        } else {
            console.printf("Password: *****");
        }

        options.crateNew = false;
        options.overwrite = true;

        console.printf("%n");

        return options;
    }

    CommandOptions promptGenerateOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... generating library designs from SBOL template%n");
        console.printf("%n");

        if (options.templateFile == null) {
            console.printf("Please enter the path to the template SBOL file for generating component definition library%n");
            options.templateFile = console.readLine("Filename: ");

            if (!validateDirPath(options.templateFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.templateFile, isInput);
                    options.templateFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid template SBOL file path argument: "+options.templateFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid template SBOL file path argument: "+options.templateFile);
                }
            }
        } else {
            console.printf("Template SBOL File: %s", options.templateFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.metaFile == null) {
            console.printf("Please enter the path to the Excel file with designs descriptions and concrete sequences for child components%n");
            options.metaFile = console.readLine("Filename: ");

            if (!validateDirPath(options.metaFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.metaFile, isInput);
                    options.metaFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }
            }
        } else {
            console.printf("Excel Flank File: %s", options.metaFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.filenamePrefix == null) {
            console.printf("Please enter the filename prefix for the generated SBOL document files%n");
            options.filenamePrefix = console.readLine("Filename Prefix [<ENTER> for 'plasmid']: ");

            if (options.filenamePrefix == null || options.filenamePrefix.trim().isEmpty()) {
                options.filenamePrefix = "plasmid";
            }
        } else {
            console.printf("%n");
            console.printf("Filename Prefix: %s", options.filenamePrefix);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.version == null) {
            console.printf("Please enter the default version number for new designs%n");
            options.version = console.readLine("Version [<ENTER> for 1.0]: ");

            if (options.version == null || options.version.trim().isEmpty()) {
                options.version = "1.0";
            }
        } else {
            console.printf("%n");
            console.printf("Version: %s", options.version);
            console.printf("%n");
        }

        console.printf("%n");
        
        if (options.isStopOnMissingMetaDef == false && options.stopOnMissingMeta == false) {
            console.printf("Do you wish to halt the generation procedure if missing metadata is encountered?%n");
            String stopOnMissingMetaAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

            if(stopOnMissingMetaAns.isEmpty()) {
                stopOnMissingMetaAns = "N";
            }

            while(!Y_N_PATTERN.matcher(stopOnMissingMetaAns).matches()) {
                stopOnMissingMetaAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

                if(stopOnMissingMetaAns.isEmpty()) {
                    stopOnMissingMetaAns = "N";
                }
            }

            if (Y_PATTERN.matcher(stopOnMissingMetaAns).matches()) {
                options.stopOnMissingMeta = true;
            } else {
                options.stopOnMissingMeta = false;
            }
        } else {
            console.printf("%n");
            console.printf("Stop on missing metadata: %s", options.stopOnMissingMeta);
            console.printf("%n");
        }
        
        console.printf("%n");
        
        if (options.outputDir == null) {
            console.printf("Please enter the directory path in which to put the generated SBOL files%n");
            options.outputDir = console.readLine("Directory path [<ENTER> for 'library']: ");

            if(options.outputDir.isEmpty()) {
                options.outputDir = Paths.get(System.getProperty("user.dir")).resolve("library").toFile().getAbsolutePath();
                console.printf("Directory: %s", options.outputDir);
            } else {
                boolean isInput = false;    // doesn't matter that it doesn't exist because it's for output
                Path outputDirPath = validateInputPath(options.outputDir, isInput);
                options.outputDir = outputDirPath.toFile().getAbsolutePath();
            }
        } else {
            console.printf("Directory: %s", options.outputDir);
            console.printf("%n");
        }

        if (new File(options.outputDir).exists()) {
            console.printf("%n");

            if (options.isOverwriteDef == false && options.overwrite == false) {
                console.printf("%n");
                console.printf("You have selected a directory that already exists. If you continue, existing designs may be overwritten by newly generated designs.%n");
                console.printf("Do you wish to continue and overwrite designs if they already exist?%n");
                String overwriteAns = console.readLine("Y | N: ").strip();

                while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                    overwriteAns = console.readLine("Y | N: ").strip();
                }

                if (Y_PATTERN.matcher(overwriteAns).matches()) {
                    options.overwrite = true;
                } else {
                    throw new IllegalArgumentException("Cannot continue with generation as existing designs will be overwritten in "+options.outputDir);
                }
            } else {
                console.printf("%n");
                console.printf("Overwrite: %s", options.overwrite);
                console.printf("%n");
            }
        } else {
            options.overwrite = false;
        }

        console.printf("%n");

        return options;
    }
    
    
    CommandOptions promptCyanoOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... generating plasmid designs in SBOL documents%n");
        console.printf("%n");

        if (options.templateFile == null) {
            console.printf("Please enter the path to the template SBOL file for generating plasmid designs%n");
            options.templateFile = console.readLine("Filename: ");

            if (!validateDirPath(options.templateFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.templateFile, isInput);
                    options.templateFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid template SBOL file path argument: "+options.templateFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid template SBOL file path argument: "+options.templateFile);
                }
            }
        } else {
            console.printf("Template SBOL File: %s", options.templateFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.metaFile == null) {
            console.printf("Please enter the path to the Excel file with flank sequences to generate%n");
            options.metaFile = console.readLine("Filename: ");

            if (!validateDirPath(options.metaFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.metaFile, isInput);
                    options.metaFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }
            }
        } else {
            console.printf("Excel Flank File: %s", options.metaFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.filenamePrefix == null) {
            console.printf("Please enter the filename prefix for the generated SBOL document files%n");
            options.filenamePrefix = console.readLine("Filename Prefix [<ENTER> for 'plasmid']: ");

            if (options.filenamePrefix == null || options.filenamePrefix.trim().isEmpty()) {
                options.filenamePrefix = "plasmid";
            }
        } else {
            console.printf("%n");
            console.printf("Filename Prefix: %s", options.filenamePrefix);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.version == null) {
            console.printf("Please enter the version number%n");
            options.version = console.readLine("Version [<ENTER> for 1.0]: ");

            if (options.version == null || options.version.trim().isEmpty()) {
                options.version = "1.0";
            }
        } else {
            console.printf("%n");
            console.printf("Version: %s", options.version);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.outputDir == null) {
            console.printf("Please enter the directory path in which to put the generated SBOL files%n");
            options.outputDir = console.readLine("Directory path [<ENTER> for 'library']: ");

            if(options.outputDir.isEmpty()) {
                options.outputDir = Paths.get(System.getProperty("user.dir")).resolve("library").toFile().getAbsolutePath();
                console.printf("Directory: %s", options.outputDir);
            } else {
                boolean isInput = false;    // doesn't matter that it doesn't exist because it's for output
                Path outputDirPath = validateInputPath(options.outputDir, isInput);
                options.outputDir = outputDirPath.toFile().getAbsolutePath();
            }
        } else {
            console.printf("Directory: %s", options.outputDir);
            console.printf("%n");
        }

        if (new File(options.outputDir).exists()) {
            console.printf("%n");

            if (options.isOverwriteDef == false && options.overwrite == false) {
                console.printf("%n");
                console.printf("You have selected a directory that already exists. If you continue, existing designs may be overwritten by newly generated designs.%n");
                console.printf("Do you wish to continue and overwrite designs if they already exist?%n");
                String overwriteAns = console.readLine("Y | N: ").strip();

                while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                    overwriteAns = console.readLine("Y | N: ").strip();
                }

                if (Y_PATTERN.matcher(overwriteAns).matches()) {
                    options.overwrite = true;
                } else {
                    throw new IllegalArgumentException("Cannot continue with generation as existing designs will be overwritten in "+options.outputDir);
                }
            } else {
                console.printf("%n");
                console.printf("Overwrite: %s", options.overwrite);
                console.printf("%n");
            }
        } else {
            options.overwrite = false;
        }

        console.printf("%n");

        return options;
    }

    CommandOptions promptCleanOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... cleaning SBOL document in input file%n");
        console.printf("%n");

        if (options.inputFile == null) {
            console.printf("Please enter the path to the input file containing the SBOL document to be cleaned%n");
            options.inputFile = console.readLine("Filename: ");

            if (!validateDirPath(options.inputFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.inputFile, isInput);
                    options.inputFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argument: "+options.inputFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argumen: "+options.inputFile);
                }
            }
        } else {
            console.printf("Input SBOL File: %s", options.inputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.outputFile == null) {
            String suffix = "_clean";

            String defOutputFile = renameFileWithSuffix(options.inputFile, suffix);

            console.printf("Please enter the path to the cleaned output file to generate%n");
            options.outputFile = console.readLine(String.format("Filename [<ENTER> for default '%s' path]: ", defOutputFile)).strip();

            if(options.outputFile.isEmpty()) {
                options.outputFile = defOutputFile;
            }

            if (!validateDirPath(options.outputFile)) {
                boolean isInput = false;    // not an input file
                Path outputPath = null;
                try {
                    outputPath = validateInputPath(options.outputFile, isInput);
                    options.outputFile = outputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                }

                if (outputPath == null) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                } else if(outputPath.toFile().exists()) {
                    console.printf("%n");
                    console.printf("You have selected an output file that already exists. If you continue, the existing SBOL document file will be overwritten.%n");
                    console.printf("Do you wish to continue and overwrite designs if they already exist?%n");
                    String overwriteAns = console.readLine("Y | N: ").strip();

                    while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                        overwriteAns = console.readLine("Y | N: ").strip();
                    }

                    if (!Y_PATTERN.matcher(overwriteAns).matches()) {
                        throw new IllegalArgumentException("Cannot continue with cleaning as existing SBOL document will be overwritten in "+options.outputFile);
                    }
                }
            }
        } else {
            console.printf("Cleaned SBOL Output File: %s", options.outputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.namespace == null) {
            console.printf("Please enter the namespace for components in the cleaned SBOL document file%n");
            options.namespace = console.readLine("Namespace [press <ENTER> for the default 'http://biordm.sbs.ed.ac.uk' namespace if you are not sure]: ");

            if (options.namespace == null || options.namespace.trim().isEmpty()) {
                options.namespace = "http://biordm.sbs.ed.ac.uk";
            }
        } else {
            console.printf("%n");
            console.printf("SBOL Component Namespace: %s", options.namespace);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isRemoveCollsDef == false && options.removeColls == false) {
            console.printf("Do you wish to remove references to any SynBioHub collections in the cleaned SBOL output document?%n");
            String removeCollsAns = console.readLine("Y | N [<ENTER> for 'Y' if you are not sure]: ").strip();

            if(removeCollsAns.isEmpty()) {
                removeCollsAns = "Y";
            }

            while(!Y_N_PATTERN.matcher(removeCollsAns).matches()) {
                removeCollsAns = console.readLine("Y | N [<ENTER> for 'Y' if you are not sure]: ").strip();

                if(removeCollsAns.isEmpty()) {
                    removeCollsAns = "Y";
                }
            }

            if (Y_PATTERN.matcher(removeCollsAns).matches()) {
                options.removeColls = true;
            } else {
                options.removeColls = false;
            }
        } else {
            console.printf("%n");
            console.printf("Remove Collections: %s", options.removeColls);
            console.printf("%n");
        }

        console.printf("%n");

        return options;
    }

    CommandOptions promptFlattenOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... flattening designs in designated SBOL document%n");
        console.printf("%n");

        if (options.inputFile == null) {
            console.printf("Please enter the path to the SBOL file containing the designs to be flattened%n");
            options.inputFile = console.readLine("Filename: ");

            if (!validateDirPath(options.inputFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.inputFile, isInput);
                    options.inputFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argument: "+options.inputFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argumen: "+options.inputFile);
                }
            }
        } else {
            console.printf("Filename: %s", options.inputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.outputFile == null) {
            String inputFile = options.inputFile;
            String suffix = "_flat";

            String defOutputFile = renameFileWithSuffix(options.inputFile, suffix);

            console.printf("Please enter the path to the SBOL file that will be generated to contain the flattened designs%n");
            options.outputFile = console.readLine(String.format("Filename [<ENTER> for default '%s' path]: ", defOutputFile)).strip();

            if(options.outputFile.isEmpty()) {
                options.outputFile = defOutputFile;
            }

            if (!validateDirPath(options.outputFile)) {
                boolean isInput = false;    // not an input file
                Path outputPath = null;
                try {
                    outputPath = validateInputPath(options.outputFile, isInput);
                    options.outputFile = outputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                }

                if (outputPath == null) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                } else if(outputPath.toFile().exists()) {
                    console.printf("%n");
                    console.printf("You have selected an output file that already exists. If you continue, the existing SBOL document file will be overwritten.%n");
                    console.printf("Do you wish to continue and overwrite designs if they already exist?%n");
                    String overwriteAns = console.readLine("Y | N: ").strip();

                    while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                        overwriteAns = console.readLine("Y | N: ").strip();
                    }

                    if (!Y_PATTERN.matcher(overwriteAns).matches()) {
                        throw new IllegalArgumentException("Cannot continue with cleaning as existing SBOL document will be overwritten in "+options.outputFile);
                    }
                }
            }
        } else {
            console.printf("Output Filename is: %s", options.outputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isAllRootsDef == false && options.allRoots == false) {
            console.printf("Do you wish to flatten all root components in the SBOL document (Y) or one particular component (N)?%n");
            String allRootsAns = console.readLine("Y | N [<ENTER> for 'Y' if you are not sure]: ").strip();

            if(allRootsAns.isEmpty()) {
                allRootsAns = "Y";
            }

            while(!Y_N_PATTERN.matcher(allRootsAns).matches()) {
                allRootsAns = console.readLine("Y | N [<ENTER> for 'Y' if you are not sure]: ").strip();

                if(allRootsAns.isEmpty()) {
                    allRootsAns = "Y";
                }
            }

            if (Y_PATTERN.matcher(allRootsAns).matches()) {
                options.allRoots = true;
            } else {
                options.allRoots = false;

                if (options.compDefinitionId == null) {
                    console.printf("Please enter the display ID of the component definition to flatten%n");
                    options.compDefinitionId = console.readLine("Component Definition Display ID: ");
                } else {
                    console.printf("Component Definition Display ID to flatten: %s", options.compDefinitionId);
                    console.printf("%n");
                }
            }
        } else {
            console.printf("Flatten only root components: %s", options.allRoots);
            console.printf("%n");

            if(options.allRoots == false) {
                console.printf("Component Definition Display ID to flatten: %s", options.compDefinitionId);
                console.printf("%n");
            }
        }

        console.printf("%n");

        if (options.suffix == null) {
            console.printf("Please enter a suffix for the flattened components if desired%n");
            options.suffix = console.readLine("Flattened Component Suffix [<ENTER> for blank]: ");

            if (options.suffix == null || options.suffix.trim().isEmpty()) {
                options.suffix = "";
            }
        } else {
            console.printf("%n");
            console.printf("Flattened Component Suffix: %s", options.suffix);
            console.printf("%n");
        }

        options.crateNew = false;
        options.overwrite = true;

        console.printf("%n");

        return options;
    }

    CommandOptions promptAnnotateOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... annotating designs in designated SBOL document%n");
        console.printf("%n");

        if (options.metaFile == null) {
            console.printf("Please enter the path to the Excel file with designs to update%n");
            options.metaFile = console.readLine("Filename: ");

            if (!validateDirPath(options.metaFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.dir, isInput);
                    options.metaFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid Excel file path argument: "+options.metaFile);
                }
            }
        } else {
            console.printf("Excel File: %s", options.metaFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.inputFile == null) {
            console.printf("Please enter the path to the SBOL file containing the designs to be annotated%n");
            options.inputFile = console.readLine("Filename: ");

            if (!validateDirPath(options.inputFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.inputFile, isInput);
                    options.inputFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argument: "+options.inputFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argumen: "+options.inputFile);
                }
            }
        } else {
            console.printf("Filename: %s", options.inputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.outputFile == null) {
            String suffix = "_annotated";

            String defOutputFile = renameFileWithSuffix(options.inputFile, suffix);

            console.printf("Please enter the path to the SBOL file that will be generated to contain the annotated designs%n");
            options.outputFile = console.readLine(String.format("Filename [<ENTER> for default '%s' path]: ", defOutputFile)).strip();

            if(options.outputFile.isEmpty()) {
                options.outputFile = defOutputFile;
            }

            if (!validateDirPath(options.outputFile)) {
                boolean isInput = false;    // not an input file
                Path outputPath = null;
                try {
                    outputPath = validateInputPath(options.outputFile, isInput);
                    options.outputFile = outputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                }

                if (outputPath == null) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                } else if(outputPath.toFile().exists()) {
                    console.printf("%n");
                    console.printf("You have selected an output file that already exists. If you continue, the existing SBOL document file will be overwritten.%n");
                    console.printf("Do you wish to continue and overwrite designs if they already exist?%n");
                    String overwriteAns = console.readLine("Y | N: ").strip();

                    while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                        overwriteAns = console.readLine("Y | N: ").strip();
                    }

                    if (!Y_PATTERN.matcher(overwriteAns).matches()) {
                        throw new IllegalArgumentException("Cannot continue with cleaning as existing SBOL document will be overwritten in "+options.outputFile);
                    }
                }
            }
        } else {
            console.printf("Output Filename is: %s", options.outputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isOverwriteDef == false && options.overwrite == false) {
            console.printf("Do you wish to overwrite existing descriptions and comments if they exist (Y) or append to them (N)?%n");
            String overwriteAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

            if(overwriteAns.isEmpty()) {
                overwriteAns = "N";
            }

            while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                overwriteAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

                if(overwriteAns.isEmpty()) {
                    overwriteAns = "N";
                }
            }

            if (Y_PATTERN.matcher(overwriteAns).matches()) {
                options.overwrite = true;
            } else {
                options.overwrite = false;
            }
        } else {
            console.printf("%n");
            console.printf("Overwrite: %s", options.overwrite);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isStopOnMissingIdDef == false && options.stopOnMissingId == false) {
            console.printf("Do you wish to halt the annotating procedure if a missing component ID is encountered?%n");
            String stopOnMissingIdAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

            if(stopOnMissingIdAns.isEmpty()) {
                stopOnMissingIdAns = "N";
            }

            while(!Y_N_PATTERN.matcher(stopOnMissingIdAns).matches()) {
                stopOnMissingIdAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

                if(stopOnMissingIdAns.isEmpty()) {
                    stopOnMissingIdAns = "N";
                }
            }

            if (Y_PATTERN.matcher(stopOnMissingIdAns).matches()) {
                options.stopOnMissingId = true;
            } else {
                options.stopOnMissingId = false;
            }
        } else {
            console.printf("%n");
            console.printf("Stop on missing ID: %s", options.stopOnMissingId);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.isStopOnMissingMetaDef == false && options.stopOnMissingMeta == false) {
            console.printf("Do you wish to halt the annotating procedure if missing metadata is encountered?%n");
            String stopOnMissingMetaAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

            if(stopOnMissingMetaAns.isEmpty()) {
                stopOnMissingMetaAns = "N";
            }

            while(!Y_N_PATTERN.matcher(stopOnMissingMetaAns).matches()) {
                stopOnMissingMetaAns = console.readLine("Y | N [<ENTER> for 'N' if you are not sure]: ").strip();

                if(stopOnMissingMetaAns.isEmpty()) {
                    stopOnMissingMetaAns = "N";
                }
            }

            if (Y_PATTERN.matcher(stopOnMissingMetaAns).matches()) {
                options.stopOnMissingMeta = true;
            } else {
                options.stopOnMissingMeta = false;
            }
        } else {
            console.printf("%n");
            console.printf("Stop on missing metadata: %s", options.stopOnMissingMeta);
            console.printf("%n");
        }

        console.printf("%n");

        options.crateNew = false;

        return options;
    }

    CommandOptions promptTemplate4UpdateOptions(CommandOptions options) {
        console.printf("%n");
        console.printf("... creating template for updating SynBioHub designs in designated SBOL document%n");
        console.printf("%n");

        if (options.inputFile == null) {
            console.printf("Please enter the path to the SBOL file containing the designs to be updated%n");
            options.inputFile = console.readLine("Filename: ");

            if (!validateDirPath(options.inputFile)) {
                boolean isInput = true;    // must exist because it's input file
                Path inputPath = null;
                try {
                    inputPath = validateInputPath(options.inputFile, isInput);
                    options.inputFile = inputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argument: "+options.inputFile);
                }

                if (inputPath == null || !inputPath.toFile().exists()) {
                    throw new IllegalArgumentException("Invalid SBOL document input file path argumen: "+options.inputFile);
                }
            }
        } else {
            console.printf("Filename: %s", options.inputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.outputFile == null) {
            String noExtFilePath = removeFileExtension(options.inputFile, false);

            String defOutputFile = noExtFilePath.concat(".xlsx");

            console.printf("Please enter the path to the Excel file that will be generated to contain the template for update%n");
            options.outputFile = console.readLine(String.format("Filename [<ENTER> for default '%s' path]: ", defOutputFile)).strip();

            if(options.outputFile.isEmpty()) {
                options.outputFile = defOutputFile;
            }

            if (!validateDirPath(options.outputFile)) {
                boolean isInput = false;    // not an input file
                Path outputPath = null;
                try {
                    outputPath = validateInputPath(options.outputFile, isInput);
                    options.outputFile = outputPath.toFile().getAbsolutePath();
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                }

                if (outputPath == null) {
                    throw new IllegalArgumentException("Invalid output file path argument: "+options.outputFile);
                } else if(outputPath.toFile().exists()) {
                    console.printf("%n");
                    console.printf("You have selected an output file that already exists. If you continue, the existing Excel template file will be overwritten.%n");
                    console.printf("Do you wish to continue and overwrite the template?%n");
                    String overwriteAns = console.readLine("Y | N: ").strip();

                    while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                        overwriteAns = console.readLine("Y | N: ").strip();
                    }

                    if (!Y_PATTERN.matcher(overwriteAns).matches()) {
                        throw new IllegalArgumentException("Cannot continue with cleaning as existing Excel template will be overwritten in "+options.outputFile);
                    }
                }
            }
        } else {
            console.printf("Output Filename is: %s", options.outputFile);
            console.printf("%n");
        }

        console.printf("%n");

        if (options.url == null) {
            console.printf("Please enter the URL for the existing collection%n");
            options.url = console.readLine("URL: ");
        } else {
            console.printf("Collection URL: %s", options.url);
            console.printf("%n");
        }

        console.printf("%n");

        return options;
    }

    public String getUsageTxt() {
        return "Usage:"
                + "\n"
                + "deposit | update | generate | cyano | clean | flatten | annotate | template4update";
    }

    void setPassedOptions(CommandOptions options, ApplicationArguments args) {

        if (args.getOptionNames().contains("username") && !args.getOptionValues("username").isEmpty()) {
            options.user = args.getOptionValues("username").get(0);
        }
        if (args.getOptionNames().contains("u") && !args.getOptionValues("u").isEmpty()) {
            options.user = args.getOptionValues("u").get(0);
        }

        if (args.getOptionNames().contains("url") && !args.getOptionValues("url").isEmpty()) {
            options.url = args.getOptionValues("url").get(0);
        }
        if (args.getOptionNames().contains("l") && !args.getOptionValues("l").isEmpty()) {
            options.url = args.getOptionValues("url").get(0);
        }

        if(options.command == Command.DEPOSIT) {
            if (args.getOptionNames().contains("file-extension") && !args.getOptionValues("file-extension").isEmpty()) {
                options.fileExtFilter = args.getOptionValues("file-extension").get(0);
            }
            if (args.getOptionNames().contains("f") && !args.getOptionValues("f").isEmpty()) {
                options.fileExtFilter = args.getOptionValues("f").get(0);
            }

            if (args.getOptionNames().contains("create-new") && !args.getOptionValues("create-new").isEmpty()) {
                options.crateNew = Boolean.parseBoolean(args.getOptionValues("create-new").get(0));
                options.isCreateNewDef = true;
            }
            if (args.getOptionNames().contains("c") && !args.getOptionValues("c").isEmpty()) {
                options.crateNew = Boolean.parseBoolean(args.getOptionValues("c").get(0));
                options.isCreateNewDef = true;
            }

            if (options.crateNew == true) {
                options.url = sanitizeUrl(options.url);
            }
            setPassedDepositOptions(options, args);
        } else if(options.command == Command.UPDATE) {
            if (args.getOptionNames().contains("meta-file") && !args.getOptionValues("meta-file").isEmpty()) {
                options.metaFile = args.getOptionValues("meta-file").get(0);
            }
            if (args.getOptionNames().contains("e") && !args.getOptionValues("e").isEmpty()) {
                options.metaFile = args.getOptionValues("e").get(0);
            }
        } else if(options.command == Command.GENERATE) {
            setPassedGenerateOptions(options, args);
        }
    }

    void setPassedGenerateOptions(CommandOptions options, ApplicationArguments args) {
        if (args.getOptionNames().contains("template-file") && !args.getOptionValues("template-file").isEmpty()) {
            options.templateFile = args.getOptionValues("template-file").get(0);
        }
        if (args.getOptionNames().contains("t") && !args.getOptionValues("t").isEmpty()) {
            options.templateFile = args.getOptionValues("t").get(0);
        }

        if (args.getOptionNames().contains("flank-file") && !args.getOptionValues("flank-file").isEmpty()) {
            options.metaFile = args.getOptionValues("flank-file").get(0);
        }
        if (args.getOptionNames().contains("f") && !args.getOptionValues("f").isEmpty()) {
            options.metaFile = args.getOptionValues("f").get(0);
        }

        if (args.getOptionNames().contains("filename-prefix") && !args.getOptionValues("filename-prefix").isEmpty()) {
            options.filenamePrefix = args.getOptionValues("filename-prefix").get(0);
        }
        if (args.getOptionNames().contains("p") && !args.getOptionValues("p").isEmpty()) {
            options.filenamePrefix = args.getOptionValues("p").get(0);
        }

        if (args.getOptionNames().contains("version") && !args.getOptionValues("version").isEmpty()) {
            options.version = args.getOptionValues("version").get(0);
        }
        if (args.getOptionNames().contains("v") && !args.getOptionValues("v").isEmpty()) {
            options.version = args.getOptionValues("v").get(0);
        }

        if (args.getOptionNames().contains("output-dir") && !args.getOptionValues("output-dir").isEmpty()) {
            options.outputDir = args.getOptionValues("output-dir").get(0);
        }
        if (args.getOptionNames().contains("o") && !args.getOptionValues("o").isEmpty()) {
            options.outputDir = args.getOptionValues("o").get(0);
        }

        if (args.getOptionNames().contains("overwrite") && !args.getOptionValues("overwrite").isEmpty()) {
            options.overwrite = Boolean.parseBoolean(args.getOptionValues("overwrite").get(0));
            options.isOverwriteDef = true;
        }
        if (args.getOptionNames().contains("o") && !args.getOptionValues("o").isEmpty()) {
            options.overwrite = Boolean.parseBoolean(args.getOptionValues("o").get(0));
            options.isOverwriteDef = true;
        }
    }

    void setPassedDepositOptions(CommandOptions options, ApplicationArguments args) {
        if (args.getOptionNames().contains("name") && !args.getOptionValues("name").isEmpty()) {
            options.collectionName = args.getOptionValues("name").get(0);
        }
        if (args.getOptionNames().contains("n") && !args.getOptionValues("n").isEmpty()) {
            options.collectionName = args.getOptionValues("n").get(0);
        }

        if (args.getOptionNames().contains("version") && !args.getOptionValues("version").isEmpty()) {
            options.version = args.getOptionValues("version").get(0);
        }
        if (args.getOptionNames().contains("v") && !args.getOptionValues("v").isEmpty()) {
            options.version = args.getOptionValues("v").get(0);
        }

        if (args.getOptionNames().contains("multi") && !args.getOptionValues("multi").isEmpty()) {
            options.multipleCollections = Boolean.parseBoolean(args.getOptionValues("multi").get(0));
            options.isMultipleCollectionsDef = true;
        }
        if (args.getOptionNames().contains("m") && !args.getOptionValues("m").isEmpty()) {
            options.multipleCollections = Boolean.parseBoolean(args.getOptionValues("m").get(0));
            options.isMultipleCollectionsDef = true;
        }

        if (args.getOptionNames().contains("dir") && !args.getOptionValues("dir").isEmpty()) {
            options.dir = args.getOptionValues("dir").get(0);
        }
        if (args.getOptionNames().contains("d") && !args.getOptionValues("d").isEmpty()) {
            options.dir = args.getOptionValues("d").get(0);
        }

        if (args.getOptionNames().contains("overwrite") && !args.getOptionValues("overwrite").isEmpty()) {
            options.overwrite = Boolean.parseBoolean(args.getOptionValues("overwrite").get(0));
            options.isOverwriteDef = true;
        }
        if (args.getOptionNames().contains("o") && !args.getOptionValues("o").isEmpty()) {
            options.overwrite = Boolean.parseBoolean(args.getOptionValues("o").get(0));
            options.isOverwriteDef = true;
        }
    }

    void setPassedCleanOptions(CommandOptions options, ApplicationArguments args) {
        if (args.getOptionNames().contains("input-file") && !args.getOptionValues("input-file").isEmpty()) {
            options.inputFile = args.getOptionValues("input-file").get(0);
        }
        if (args.getOptionNames().contains("i") && !args.getOptionValues("i").isEmpty()) {
            options.inputFile = args.getOptionValues("i").get(0);
        }

        if (args.getOptionNames().contains("output-file") && !args.getOptionValues("output-file").isEmpty()) {
            options.outputFile = args.getOptionValues("output-file").get(0);
        }
        if (args.getOptionNames().contains("o") && !args.getOptionValues("o").isEmpty()) {
            options.outputFile = args.getOptionValues("o").get(0);
        }

        if (args.getOptionNames().contains("namespace") && !args.getOptionValues("namespace").isEmpty()) {
            options.namespace = args.getOptionValues("namespace").get(0);
        }
        if (args.getOptionNames().contains("n") && !args.getOptionValues("n").isEmpty()) {
            options.namespace = args.getOptionValues("n").get(0);
        }

        if (args.getOptionNames().contains("remove-collections") && !args.getOptionValues("remove-collections").isEmpty()) {
            options.removeColls = Boolean.parseBoolean(args.getOptionValues("remove-collections").get(0));
            options.isRemoveCollsDef = true;
        }
        if (args.getOptionNames().contains("r") && !args.getOptionValues("r").isEmpty()) {
            options.removeColls = Boolean.parseBoolean(args.getOptionValues("r").get(0));
            options.isRemoveCollsDef = true;
        }
    }

    void setPassedFlattenOptions(CommandOptions options, ApplicationArguments args) {

    }

    void setPassedAnnotateOptions(CommandOptions options, ApplicationArguments args) {

    }

    boolean validateDirPath(String dirPath) {
        Path path = Paths.get(dirPath);

        File dirFile = path.toFile();

        return dirFile.exists();
    }

    Path validateInputPath(String inputPath, boolean isInput) {
        Path path = Paths.get(inputPath);
        Path vInputPath;

        File file = path.toFile();
        boolean exists = file.exists();

        if(exists == false) {
            // check if they provided a relative path
            vInputPath = Paths.get(System.getProperty("user.dir")).resolve(path);
            file = vInputPath.toFile();
            exists = file.exists();

            if(exists == false && isInput == true) {
                throw new IllegalArgumentException("Invalid input file path: "+inputPath);
            }
        } else {
            vInputPath = path;
        }
        return vInputPath;
    }

    boolean validateString(Pattern pattern, String str) {
        return pattern.matcher(str).matches();
    }

    String sanitizeUrl(String url) {
        String sanitizedUrl = url;
        if(!url.endsWith("/")) {
            sanitizedUrl = url.concat("/");
        }

        return sanitizedUrl;
    }

    boolean isSubFolders(String dirPath) {
        Path path = Paths.get(dirPath);

        File dirFile = path.toFile();

        boolean isSubs = false;

        if (dirFile.isDirectory()) {
            DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path file) throws IOException {
                    return (Files.isDirectory(file));
                }
            };

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter)) {
                for (Path subPath : stream) {
                    // Iterate over the paths in the directory and print filenames
                    //System.out.println(subPath.getFileName());
                    isSubs = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isSubs;
    }

    String renameFileWithSuffix(String inputFilename, String suffix) {
        Path inputFilePath = Paths.get(inputFilename);
        Path filename = inputFilePath.getFileName();
        Path parentDir = inputFilePath.getParent();

        String[] origFilenameBits = filename.toString().split("\\.");
        String origFilePrefix = origFilenameBits[0];
        String origFileExts = Arrays.stream(Arrays.copyOfRange(origFilenameBits, 1, origFilenameBits.length)).collect(Collectors.joining(""));

        String newFilePrefix = origFilePrefix.concat(suffix);
        String newFilename = newFilePrefix.concat(".").concat(origFileExts);
        return parentDir.resolve(newFilename).toFile().getAbsolutePath();
    }

    Optional<String> getFilenameExtension(String filename) {
        Optional<String> ext = Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1));

        return ext;
    }

    String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }
}
