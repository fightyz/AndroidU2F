package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yz on 2016/1/18.
 */
public class CryptoImpl implements Crypto {
    @Override
    public byte[] computeSha256(String message) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(message.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
