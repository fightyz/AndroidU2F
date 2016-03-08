package org.esec.mcg.androidu2f.token.impl;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.KeyHandleGenerator;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Created by yz on 2016/1/19.
 */
public class KeyHandleGeneratorWithKeyStore implements KeyHandleGenerator {

    private Cipher mCipher;
    private KeyPair mPair;

//    public KeyHandleGeneratorWithKeyStore(Context context, String alias)
//            throws GeneralSecurityException, IOException {
//        mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//
//        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);
//
//        if (!keyStore.containsAlias(alias)) {
//            generateKeyPair(context, alias);
//        }
//
//        // Even if we just generated the key, always read it back to ensure we
//        // can read it successfully.
//        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
//                alias, null);
//        mPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
//    }

    private static void generateKeyPair(Context context, String alias)
            throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);

//        Android 6.0
//        final KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(context)
//                .setAlias(alias)
//                .
//        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
//                .setAlias(alias)
//                .setSubject(new X500Principal("CN=" + alias))
//                .setSerialNumber(BigInteger.ONE)
//                .setStartDate(start.getTime())
//                .setEndDate(end.getTime())
//                .build();
//
//        final java.security.KeyPairGenerator gen = java.security.KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
//        gen.initialize(spec);
//        gen.generateKeyPair();

//        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance(
//                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
//        kpg.initialize(new KeyGenParameterSpec.Builder(
//                alias,
//                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
//                .setDigests(KeyProperties.DIGEST_SHA256,
//                        KeyProperties.DIGEST_SHA512)
//                .build());
//
//        KeyPair kp = kpg.generateKeyPair();

//        java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance()
    }

    /**
     * Wrap a {@link SecretKey} using the public key assigned to this wrapper.
     * Use {@link #unwrap(byte[])} to later recover the original
     * {@link SecretKey}.
     *
     * @return a wrapped version of the given {@link SecretKey} that can be
     *         safely stored on untrusted storage.
     */
    public byte[] wrap(PrivateKey key) throws GeneralSecurityException {
        mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
        return mCipher.wrap(key);
    }
    /**
     * Unwrap a {@link SecretKey} using the private key assigned to this
     * wrapper.
     *
     * @param blob a wrapped {@link SecretKey} as previously returned by
     *            {@link #wrap(PrivateKey)}.
     */
    public PrivateKey unwrap(byte[] blob) throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());
        return (PrivateKey) mCipher.unwrap(blob, "AES", Cipher.PRIVATE_KEY);
    }

    @Override
    public byte[] generateKeyHandle(byte[] applicationSha256, KeyPair keyPair) {
        PrivateKey key = keyPair.getPrivate();

        byte[] result = null;
        try {
            LogUtils.d(ByteUtil.ByteArrayToHexString(key.getEncoded()));
            result = wrap(key);
            LogUtils.d(ByteUtil.ByteArrayToHexString(result));
            unwrap(result);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public byte[] generateKeyHandle(byte[] applicationSha256, byte[] challengeSha256) throws U2FException{
        byte[] keyHandle = new byte[applicationSha256.length + challengeSha256.length];
        ByteBuffer.wrap(keyHandle).put(applicationSha256).put(challengeSha256);
//        String keyHandleString = new String(keyHandle);
        String keyHandleString = android.util.Base64.encodeToString(keyHandle, Base64.NO_WRAP);
        LogUtils.d("keyHandleString");
        LogUtils.d(keyHandleString);
        LogUtils.d(ByteUtil.ByteArrayToHexString(keyHandle));

        try {
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(keyHandleString)) {
                //TODO throw exception
//                throw new U2FException("Key handle already existed.");
                keyStore.deleteEntry(keyHandleString);
            }

            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            keyHandleString,
                            KeyProperties.PURPOSE_SIGN)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            .setDigests(KeyProperties.DIGEST_SHA256)
//                    .setUserAuthenticationRequired(true)
//                    .setUserAuthenticationValidityDurationSeconds(5 * 60)
                            .build()
            );

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

//            LogUtils.d(ByteUtil.ByteArrayToHexString(keyPair.getPublic().getEncoded()));
//            LogUtils.d(ByteUtil.ByteArrayToHexString(keyPair.getPrivate().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return keyHandle;
    }

    @Override
    public PrivateKey getUserPrivateKey(byte[] keyHandle) throws U2FException {
        String keyHandleString = android.util.Base64.encodeToString(keyHandle, Base64.NO_WRAP);
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(keyHandleString)) {
                return (PrivateKey)keyStore.getKey(keyHandleString, null);
            } else {
                throw new U2FException("Can not find user key");
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
