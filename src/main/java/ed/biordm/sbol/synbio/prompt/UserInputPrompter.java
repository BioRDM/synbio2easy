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
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 *
 * @author tzielins
 */
@Component
public class UserInputPrompter {

    final Console console;

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

        final String depositCommand = Command.DEPOSIT.toString();

        switch(Command.valueOf(command)) {
            case DEPOSIT: return DEPOSIT;
            default: throw new MissingOptionException("Uknown command "+command);
        }
    }

    String promptForCommand() {

        console.printf("What operation do you want to perform?%n");
        StringJoiner joiner = new StringJoiner("|", "", "");

        for (Command cmd : Command.values()) {
            joiner.add(String.valueOf(cmd));
        }
        // Arrays.asList(Command.values()).forEach(joiner::add);

        console.printf("Choose from: %s%n", joiner.toString());
        String command = console.readLine("Operation: ");

        return command;
    }

    CommandOptions promptDepositOptions(CommandOptions options) {

        console.printf("... depositing desings into SynBioHub%n");

        if (options.user == null) {
            options.user = console.readLine("Please enter SynBioHub username (email address): ");
        } else {
            console.printf("Username: %s", options.user);
        }

        if (options.password == null) {
            options.password = new String(console.readPassword("Please enter your SynBioHub password: "));
        } else {
            console.printf("Password: *****");
        }

        if (options.crateNew == true) {
            if (options.collectionName == null) {
                options.collectionName = new String(console.readLine("Please enter a name for the new collection: "));
            } else {
                console.printf("New collection name: %s", options.collectionName);
            }
            if (options.url == null) {
                options.url = new String(console.readLine("Please enter the URL of the SynBioHub server: "));
            } else {
                console.printf("SynBioHub URL: %s", options.url);
            }
        } else {
            if (options.url == null) {
                options.url = new String(console.readLine("Please enter the URL for the existing collection: "));
            } else {
                console.printf("Collection URL: %s", options.url);
            }
        }

        if (options.dir == null) {
            options.dir = new String(console.readLine("Please enter the directory path to upload: "));
        } else {
            console.printf("Directory: %s", options.dir);
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

}
