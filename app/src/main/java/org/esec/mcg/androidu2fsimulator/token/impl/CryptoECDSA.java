package org.esec.mcg.androidu2fsimulator.token.impl;

import org.esec.mcg.androidu2fsimulator.token.Crypto;
import org.esec.mcg.androidu2fsimulator.token.U2FTokenException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Created by yz on 2016/1/21.
 */
public class CryptoECDSA implements Crypto {

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    @Override
    public byte[] sign(byte[] signedData, PrivateKey certificatePrivateKey) throws U2FTokenException {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(certificatePrivateKey);
            signature.update(signedData);
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new U2FTokenException("Error when signing", e);
        } catch (SignatureException e) {
            throw new U2FTokenException("Error when signing", e);
        } catch (InvalidKeyException e) {
            throw new U2FTokenException("Error when signing", e);
        }
    }
}
