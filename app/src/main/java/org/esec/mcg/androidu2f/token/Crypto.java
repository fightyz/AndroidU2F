package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.U2FException;

import java.security.PrivateKey;

/**
 * Created by yz on 2016/1/18.
 */
public interface Crypto {
    byte[] sign(byte[] signedData, PrivateKey certificatePrivateKey) throws U2FException;
}
