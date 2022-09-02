package org.imperial.activemilespro.nfc_tag;

@SuppressWarnings("serial")
/**
 * Should be thrown when the CC was altered
 * @author NXP67729
 *
 */
public class CC_differException extends Exception

{
    // Parameterless Constructor
    public CC_differException() {
    }

    // Constructor that accepts a message
    public CC_differException(String message) {
        super(message);
    }

}
