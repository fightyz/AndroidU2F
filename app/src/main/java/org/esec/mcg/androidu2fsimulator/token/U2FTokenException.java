package org.esec.mcg.androidu2fsimulator.token;

/**
 * Created by yz on 2016/3/16.
 */
public class U2FTokenException extends Exception {
    public U2FTokenException(String message) { super(message); }
    public U2FTokenException(String message, Throwable cause) { super(message, cause); }
}
