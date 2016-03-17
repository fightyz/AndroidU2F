package org.esec.mcg.androidu2fsimulator.token;

import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Created by yz on 2016/1/18.
 */
public interface KeyPairGenerator {
    KeyPair generateKeyPair(byte[] applicationSha256, byte[] challengeSha256);
    byte[] encodePublicKey(PublicKey publicKey);
}
