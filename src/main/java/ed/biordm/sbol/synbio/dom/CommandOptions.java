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
public class CommandOptions {
    
    public Command command;
    public String url;
    public String user;
    public String password;
    
    public CommandOptions(Command command) {
        Objects.requireNonNull(command);
        this.command = command;
    }
}
