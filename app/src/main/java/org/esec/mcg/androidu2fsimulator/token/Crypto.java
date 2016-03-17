package org.esec.mcg.androidu2fsimulator.token;

import java.security.PrivateKey;

/**
 * Created by yz on 2016/1/18.
 */
public interface Crypto {
    byte[] sign(byte[] signedData, PrivateKey certificatePrivateKey) throws U2FTokenException;
}
