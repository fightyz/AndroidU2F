package org.esec.mcg.androidu2f.token;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;

import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

/**
 * Created by yz on 2016/1/19.
 */
public class AESKeyWrapKeyHandleGenerator implements KeyHandleGenerator {

    private final Cipher mCipher;
    private final KeyPair mPair;

    public AESKeyWrapKeyHandleGenerator(Context context, String alias)
            throws GeneralSecurityException, IOException {
        mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(alias)) {
            generateKeyPair(context, alias);
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                alias, null);
        mPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    private static void generateKeyPair(Context context, String alias)
            throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);

//        Android 6.0
//        final KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(context)
//                .setAlias(alias)
//                .
        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        final java.security.KeyPairGenerator gen = java.security.KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        gen.initialize(spec);
        gen.generateKeyPair();
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
}
