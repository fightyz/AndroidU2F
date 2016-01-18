package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yz on 2016/1/18.
 */
public class CryptoImpl implements Crypto {
    @Override
    public byte[] computeSha256(String message) throws U2FException {
        try {
            return MessageDigest.getInstance("SHA-256").digest(message.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new U2FException("Cannot compute SHA-256", e);
        }
    }
}
