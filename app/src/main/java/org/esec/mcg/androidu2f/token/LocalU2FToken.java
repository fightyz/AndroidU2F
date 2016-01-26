package org.esec.mcg.androidu2f.token;

import android.content.Context;
import android.util.Log;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.token.impl.CryptoECDSA;
import org.esec.mcg.androidu2f.token.impl.KeyHandleGeneratorWithKeyStore;
import org.esec.mcg.androidu2f.token.impl.SCSecp256r1;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

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
        attestationCertificate = (X509Certificate)AttestationCertificate.getAttestationCertificate();
        certificatePrivateKey = AttestationCertificate.getAttestationPrivateKey();
        keyPairGenerator = new SCSecp256r1();

        keyHandleGenerator = new KeyHandleGeneratorWithKeyStore();;
        dataStore = null;
        userPresenceVerifier = null;
        crypto = new CryptoECDSA();
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
//        startActivity()

//        KeyPair keyPair = keyPairGenerator.generateKeyPair(applicationSha256, challengeSha256);
//        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, keyPair);
//        LogUtils.d(keyHandle);

//        dataStore.storeKeyPair(keyHandle, keyPair);

        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, challengeSha256);
        LogUtils.d(ByteUtil.ByteArrayToHexString(keyHandle));

        byte[] userPublicKey;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(new String(keyHandle)).getPublicKey();
            byte[] userPublicKeyX509 = publicKey.getEncoded(); // this is x.509 encoded, so has 91 bytes.
            SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(userPublicKeyX509));
            userPublicKey = subjectPublicKeyInfo.getPublicKeyData().getBytes();
            LogUtils.d(keyStore.getCertificate(new String(keyHandle)));
            LogUtils.d(ByteUtil.ByteArrayToHexString(userPublicKeyX509));
            LogUtils.d(ByteUtil.ByteArrayToHexString(userPublicKey));
        } catch (KeyStoreException e) {
            throw new U2FException("Local token register error.", e);
        } catch (CertificateException e) {
            throw new U2FException("Local token register error.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new U2FException("Local token register error.", e);
        } catch (IOException e) {
            throw new U2FException("Local token register error.", e);
        }

        byte[] signedData = RawMessageCodec.encodeRegistrationSignedBytes(applicationSha256, challengeSha256,
                keyHandle, userPublicKey);

        if (certificatePrivateKey == null) {
            LogUtils.e("attestation certificate private key is null");
        }
        byte[] signature = crypto.sign(signedData, certificatePrivateKey);
        LogUtils.d(ByteUtil.ByteArrayToHexString(signature));
        return new RegisterResponse(userPublicKey, keyHandle, attestationCertificate, signature);
    }
}
