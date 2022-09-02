package org.imperial.activemilespro.nfc_tag;

@SuppressWarnings("serial")
/**
 * Should be thrown when static Lock bits are set
 * @author NXP67729
 *
 */
public class StaticLockBitsException extends Exception

{
    // Parameterless Constructor
    public StaticLockBitsException() {
    }

    // Constructor that accepts a message
    public StaticLockBitsException(String message) {
        super(message);
    }

}
