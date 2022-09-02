package org.imperial.activemilespro.nfc_tag;

@SuppressWarnings("serial")
/**
 * Should be thrown when Dynamic Lock bits are set
 * @author NXP67729
 *
 */
public class DynamicLockBitsException extends Exception

{
    // Parameterless Constructor
    public DynamicLockBitsException() {
    }

    // Constructor that accepts a message
    public DynamicLockBitsException(String message) {
        super(message);
    }

}
