package org.esec.mcg.androidu2f.token.impl;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.Crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Created by yz on 2016/1/21.
 */
public class CryptoECDSA implements Crypto {
    @Override
    public byte[] sign(byte[] signedData, PrivateKey certificatePrivateKey) throws U2FException {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(certificatePrivateKey);
            signature.update(signedData);
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new U2FException("Error when signing", e);
        } catch (SignatureException e) {
            throw new U2FException("Error when signing", e);
        } catch (InvalidKeyException e) {
            throw new U2FException("Error when signing", e);
        }
    }
}