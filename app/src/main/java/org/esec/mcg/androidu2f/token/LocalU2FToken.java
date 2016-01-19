package org.esec.mcg.androidu2f.token;

import android.content.Context;
import android.util.Log;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.token.impl.KeyHandleGeneratorWithKeyStore;
import org.esec.mcg.androidu2f.token.impl.SCSecp256r1;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by yz on 2016/1/14.
 */
public class LocalU2FToken implements U2FToken {

    private final X509Certificate attestationCertificate;
    private final PrivateKey certificatePrivateKey;
    private final KeyPairGenerator keyPairGenerator;
    private final KeyHandleGenerator keyHandleGenerator;
    private final DataStore dataStore;
    private final UserPresenceVerifier userPresenceVerifier;
    private final Crypto crypto;

    private Context context;

    //TODO Implements these interface.
    public LocalU2FToken(Context context) {
        attestationCertificate = null;
        certificatePrivateKey = null;
        keyPairGenerator = new SCSecp256r1();

        keyHandleGenerator = new KeyHandleGeneratorWithKeyStore();;
        dataStore = null;
        userPresenceVerifier = null;
        crypto = null;
        this.context = context;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) throws U2FException{
        byte[] applicationSha256 = registerRequest.getApplicationSha256();
        byte[] challengeSha256 = registerRequest.getChallengeSha256();

//        byte userPresent = userPresenceVerifier.verifyUserPresence();
//        if ((userPresent & UserPresenceVerifier.USER_PRESENT_FLAG) == 0x00) {
//            throw new U2FException("Cannot verify user presence.");
//        }

//        KeyPair keyPair = keyPairGenerator.generateKeyPair(applicationSha256, challengeSha256);
//        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, keyPair);
//        LogUtils.d(keyHandle);

//        dataStore.storeKeyPair(keyHandle, keyPair);

        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, challengeSha256);
//        KeyPair keyPair = keyPairGenerator.generateKeyPair(applicationSha256, challengeSha256);
        KeyStore keyStore = null;
        PublicKey publicKey = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            publicKey = keyStore.getCertificate(new String(keyHandle)).getPublicKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        byte[] userPublicKey = keyPairGenerator.encodePublicKey(publicKey);
        byte[] userPublicKey = publicKey.getEncoded(); // this is x.509 encoded, so has 91 bytes.
        LogUtils.d(ByteUtil.ByteArrayToHexString(userPublicKey));
        LogUtils.d(userPublicKey.length);

        byte[] signedData = RawMessageCodec.encodeRegistrationSignedBytes(applicationSha256, challengeSha256,
                keyHandle, userPublicKey);

        byte[] signature = crypto.sign(signedData, certificatePrivateKey);
        return new RegisterResponse(userPublicKey, keyHandle, attestationCertificate, signature);
    }
}
