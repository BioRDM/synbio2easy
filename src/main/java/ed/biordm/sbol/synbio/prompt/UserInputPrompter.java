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
        if (args.getNonOptionArgs().isEmpty()) 
            throw new MissingOptionException("Missing command name");
        
        String command = args.getNonOptionArgs().get(0);
        switch(command) {
            case "deposit": return DEPOSIT;
            default: throw new MissingOptionException("Uknown command "+command);
        }
    }

    CommandOptions promptDepositOptions(CommandOptions options) {
        
        console.printf("... depositing desings into SynBioHub%n");

        if (options.user == null) {
            options.user = console.readLine("Please enter SynBioHub username (email address): ");
        } else {
            console.printf("Username: %s", options.user);
        }
        
        if (options.password == null) {
            options.password = console.readLine("Please enter your SynBioHub password: ");
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
    
}
