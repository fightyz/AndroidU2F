package org.esec.mcg.androidu2fsimulator.token;

import java.security.KeyPair;
import java.security.PrivateKey;

/**
 * Created by yz on 2016/1/18.
 */
public interface KeyHandleGenerator {
    byte[] generateKeyHandle(byte[] applicationSha256, KeyPair keyPair);
    byte[] generateKeyHandle(byte[] applicationSha256, byte[] challengeSha256) throws U2FTokenException;
    PrivateKey getUserPrivateKey(byte[] keyHandle) throws U2FTokenException;
    boolean checkKeyHandle(byte[] keyHandle) throws U2FTokenException;
}
