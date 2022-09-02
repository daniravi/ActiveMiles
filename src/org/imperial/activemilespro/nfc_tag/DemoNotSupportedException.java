package org.imperial.activemilespro.nfc_tag;

@SuppressWarnings("serial")
/**
 * Should be thrown when Dynamic Lock bits are set
 * @author NXP67729
 *
 */
public class DemoNotSupportedException extends Exception

{
    // Parameterless Constructor
    public DemoNotSupportedException() {
    }

    // Constructor that accepts a message
    public DemoNotSupportedException(String message) {
        super(message);
    }

}
