/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.dom;

import java.util.Objects;

/**
 *
 * @author tzielins
 */
public class CommandOptions implements Cloneable {

    // General options
    public Command command;
    public String sessionToken;
    public String url;
    public String user;
    public String password;

    // Deposit command options
    public boolean multipleCollections;
    public boolean isMultipleCollectionsDef;
    public String collectionName;
    public boolean crateNew;
    public boolean isCreateNewDef;
    public String dir;
    public String fileExtFilter;
    public String version;
    public boolean overwrite;
    public boolean isOverwriteDef;

    // Attachment command options
    public String xslFile;

    // Generate command options
    public String templateFile;
    public String flankFile;
    public String outputDir;
    public String filenamePrefix;

    // Clean command options
    public String inputFile;
    public String outputFile;
    public String namespace;
    public boolean removeColls;
    public boolean isRemoveCollsDef;

    public CommandOptions(Command command) {
        Objects.requireNonNull(command);
        this.command = command;
    }

    @Override
    public CommandOptions clone() {
        try  {
            return (CommandOptions)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
