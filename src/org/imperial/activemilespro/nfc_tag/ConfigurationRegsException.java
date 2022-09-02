package org.imperial.activemilespro.nfc_tag;

@SuppressWarnings("serial")
/**
 * Should be thrown when configuration registers cannot be updated
 * @author NXP67729
 *
 */
public class ConfigurationRegsException extends Exception

{
    // Parameterless Constructor
    public ConfigurationRegsException() {
    }

    // Constructor that accepts a message
    public ConfigurationRegsException(String message) {
        super(message);
    }

}
