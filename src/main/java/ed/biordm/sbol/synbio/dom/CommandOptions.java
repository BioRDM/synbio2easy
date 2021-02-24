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

    public Command command;
    public String sessionToken;
    public String url;
    public String user;
    public String password;
    public boolean multipleCollections;
    public boolean crateNew;
    public String collectionName;
    public String dir;
    public String fileExtFilter;
    public String version;
    public boolean overwrite;

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
