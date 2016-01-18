package org.esec.mcg.androidu2f.token;

import java.security.KeyPair;

/**
 * Created by yz on 2016/1/18.
 */
public interface KeyHandleGenerator {
    byte[] generateKeyHandle(byte[] applicationSha256, KeyPair keyPair);
}
