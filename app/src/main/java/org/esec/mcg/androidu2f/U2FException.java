package org.esec.mcg.androidu2f;

/**
 * Created by yz on 2016/1/14.
 */
@SuppressWarnings("serial")
public class U2FException extends Exception {
    public U2FException(String message) {
        super(message);
    }

    public U2FException(String message, Throwable cause) {
        super(message, cause);
    }


}
