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
import java.util.StringJoiner;
import java.util.regex.Pattern;
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
            default: throw new MissingOptionException("Unknown command "+command);
        }
    }

    String promptForCommand() {

        console.printf("What operation do you want to perform?%n");
        StringJoiner joiner = new StringJoiner(" | ", "", "");

        for (Command cmd : Command.values()) {
            joiner.add(String.valueOf(cmd).toLowerCase());
        }
        // Arrays.asList(Command.values()).forEach(joiner::add);

        console.printf("Choose from: %s%n", joiner.toString());
        String command = console.readLine("Operation: ");

        return command;
    }

    CommandOptions promptDepositOptions(CommandOptions options) {

        console.printf("... depositing designs into SynBioHub%n");

        if (options.dir == null) {
            console.printf("Please enter the directory path to upload%n");
            options.dir = console.readLine("Directory path [<ENTER> for current directory]: ");

            if (!validateDirPath(options.dir)) {
                if(options.dir.isEmpty()) {
                    options.dir = System.getProperty("user.dir");
                } else {
                    throw new IllegalArgumentException("Invalid directory path argument: "+options.dir);
                }
            }
        } else {
            console.printf("Directory: %s", options.dir);
        }

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
        }

        if (options.multipleCollections == false) {
            // check for sub-folders
            boolean isSubFolders = isSubFolders(options.dir);

            if(isSubFolders) {
                console.printf("Do you wish to create multiple collections%n");
                String multipleAns = console.readLine("Y | N: ").strip();
                while(!Y_N_PATTERN.matcher(multipleAns).matches()) {
                    multipleAns = console.readLine("Y | N: ").strip();
                }

                if (Y_PATTERN.matcher(multipleAns).matches()) {
                    // set this automatically since we must create new collections for multiple collections
                    options.crateNew = true;
                    console.printf("Each sub-folder in the selected directory will be uploaded to SynBioHub as a separate collection%n");
                } else {
                    console.printf("Only the files in the top level directory (no sub-directories) will be submitted to SynBioHub%n");
                }
            }
        }

        if (options.crateNew == false) {
            console.printf("Do you wish to create a new collection?%n");
            String createNewAns = console.readLine("Y | N: ").strip();
            while(!Y_N_PATTERN.matcher(createNewAns).matches()) {
                createNewAns = console.readLine("Y | N: ").strip();
            }

            if (Y_PATTERN.matcher(createNewAns).matches()) {
                options.crateNew = true;
            } else {

            }
        }

        if (options.crateNew == true) {
            if (options.multipleCollections == false) {
                if (options.collectionName == null) {
                    console.printf("Please enter a name for the new collection%n");
                    options.collectionName = console.readLine("Name: ");
                } else {
                    console.printf("New collection name: %s", options.collectionName);
                }
            } else {
                if (options.collectionName == null) {
                    console.printf("Please enter a prefix for the new collections%n");
                    options.collectionName = console.readLine("Prefix [<ENTER> for no prefix]: ");
                } else {
                    console.printf("New collection prefix: %s", options.collectionName);
                }  
            }

            if (options.url == null) {
                console.printf("Please enter the URL of the SynBioHub server%n");
                options.url = console.readLine("URL [<ENTER> for https://synbiohub.org]: ");
                options.url = sanitizeUrl(options.url);
            } else {
                console.printf("SynBioHub URL: %s", options.url);
            }
        } else {
            if (options.url == null) {
                console.printf("Please enter the URL for the existing collection%n");
                options.url = console.readLine("URL: ");
            } else {
                console.printf("Collection URL: %s", options.url);
            }
        }

        if (options.version == null) {
            if (options.crateNew == true) {
                console.printf("Please enter the version number%n");
                options.version = console.readLine("Version [<ENTER> for 1.0]: ");
            }
        } else {
            console.printf("Version: %s", options.version);
        }

        if (options.overwrite == false) {
            console.printf("Do you wish to overwrite designs if they exist?%n");
            String overwriteAns = console.readLine("Y | N: ").strip();

            while(!Y_N_PATTERN.matcher(overwriteAns).matches()) {
                overwriteAns = console.readLine("Y | N: ").strip();
            }

            if (Y_PATTERN.matcher(overwriteAns).matches()) {
                options.overwrite = true;
            }
        } else {
            console.printf("Overwrite: %s", options.overwrite);
        }

        if (options.user == null) {
            console.printf("Please enter your SynBioHub username%n");
            options.user = console.readLine("Username (email): ");
        } else {
            console.printf("Username: %s", options.user);
        }

        if (options.password == null) {
            console.printf("Please enter your SynBioHub password%n");
            options.password = new String(console.readPassword("Password: "));
        } else {
            console.printf("Password: *****");
        }

        return options;
    }

    CommandOptions promptUpdateOptions(CommandOptions options) {
        console.printf("... updating existing designs in SynBioHub%n");

        if (options.xslFile == null) {
            console.printf("Please enter the path to the Excel file with designs to update%n");
            options.xslFile = console.readLine("Filename: ");

            if (!validateDirPath(options.xslFile)) {
                throw new IllegalArgumentException("Invalid Excel file path argument: "+options.xslFile);
            }
        } else {
            console.printf("Excel File: %s", options.xslFile);
        }

        if (options.url == null) {
            console.printf("Please enter the URL for the collection to update%n");
            options.url = console.readLine("URL: ");
        } else {
            console.printf("Collection URL: %s", options.url);
        }

        if (options.user == null) {
            console.printf("Please enter your SynBioHub username%n");
            options.user = console.readLine("Username (email): ");
        } else {
            console.printf("Username: %s", options.user);
        }

        if (options.password == null) {
            console.printf("Please enter your SynBioHub password%n");
            options.password = new String(console.readPassword("Password: "));
        } else {
            console.printf("Password: *****");
        }

        return options;
    }

    public String getUsageTxt() {
        return "Usage:"
                + "\n"
                + "deposit | sequence";
    }

    void setPassedOptions(CommandOptions options, ApplicationArguments args) {

        if (args.getOptionNames().contains("username") && !args.getOptionValues("username").isEmpty()) {
            options.user = args.getOptionValues("username").get(0);
        }
        if (args.getOptionNames().contains("u") && !args.getOptionValues("u").isEmpty()) {
            options.user = args.getOptionValues("u").get(0);
        }

    }

    boolean validateDirPath(String dirPath) {
        Path path = Paths.get(dirPath);

        File dirFile = path.toFile();

        return dirFile.exists();
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
}
