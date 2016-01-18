package org.esec.mcg.androidu2f.token;

import java.security.KeyPair;

/**
 * Created by yz on 2016/1/18.
 */
public interface DataStore {
    void storeKeyPair(byte[] keyHandle, KeyPair keyPair);

    KeyPair getKeyPair(byte[] keyHandle);

    int incrementCounter();
}
