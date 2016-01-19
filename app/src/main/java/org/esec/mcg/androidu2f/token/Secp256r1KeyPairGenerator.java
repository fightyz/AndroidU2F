package org.esec.mcg.androidu2f.token;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

/**
 * Created by yz on 2016/1/19.
 */
public class Secp256r1KeyPairGenerator implements KeyPairGenerator {

    private ECParameterSpec ecParameterSpec;

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    Secp256r1KeyPairGenerator() {
        ecParameterSpec = ECNamedCurveTable.getParameterSpec("SECP256r1");
    }

    @Override
    public KeyPair generateKeyPair(byte[] applicationSha256, byte[] challengeSha256) {
        try {
            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance("ECDSA", "SC");
            keyPairGenerator.initialize(ecParameterSpec, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] encodePublicKey(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
