package org.esec.mcg.androidu2f.token.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.esec.mcg.androidu2f.token.AttestationCertificate;
import org.esec.mcg.androidu2f.token.Crypto;
import org.esec.mcg.androidu2f.token.DataStore;
import org.esec.mcg.androidu2f.token.KeyHandleGenerator;
import org.esec.mcg.androidu2f.token.KeyPairGenerator;
import org.esec.mcg.androidu2f.token.U2FToken;
import org.esec.mcg.androidu2f.token.U2FTokenActivity;
import org.esec.mcg.androidu2f.token.U2FTokenException;
import org.esec.mcg.androidu2f.token.UserPresenceVerifier;
import org.esec.mcg.androidu2f.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2f.token.msg.AuthenticationResponse;
import org.esec.mcg.androidu2f.token.msg.RawMessageCodec;
import org.esec.mcg.androidu2f.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2f.token.msg.RegistrationResponse;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;

import java.io.IOException;
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

    public LocalU2FToken(Context context) {
        attestationCertificate = (X509Certificate) AttestationCertificate.getAttestationCertificate();
        certificatePrivateKey = AttestationCertificate.getAttestationPrivateKey();
        keyPairGenerator = new SCSecp256r1();

        keyHandleGenerator = new KeyHandleGeneratorWithKeyStore();
        dataStore = null;
        userPresenceVerifier = null;
        crypto = new CryptoECDSA();
        this.context = context;
    }

    @Override
    public RegistrationResponse register(RegistrationRequest registrationRequest) throws U2FTokenException {
        byte[] applicationSha256 = registrationRequest.getApplicationSha256();
        byte[] challengeSha256 = registrationRequest.getChallengeSha256();

        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, challengeSha256);
        LogUtils.d(ByteUtil.ByteArrayToHexString(keyHandle));

        byte[] userPublicKey;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(android.util.Base64.encodeToString(keyHandle, Base64.NO_WRAP | Base64.URL_SAFE)).getPublicKey();
            byte[] userPublicKeyX509 = publicKey.getEncoded(); // this is x.509 encoded, so has 91 bytes.
            SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(userPublicKeyX509));
            userPublicKey = subjectPublicKeyInfo.getPublicKeyData().getBytes();
            LogUtils.d(keyStore.getCertificate(android.util.Base64.encodeToString(keyHandle, Base64.NO_WRAP | Base64.URL_SAFE)));
            LogUtils.d(ByteUtil.ByteArrayToHexString(userPublicKeyX509));
            LogUtils.d(ByteUtil.ByteArrayToHexString(userPublicKey));
        } catch (KeyStoreException e) {
            throw new U2FTokenException("Local token register error.", e);
        } catch (CertificateException e) {
            throw new U2FTokenException("Local token register error.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new U2FTokenException("Local token register error.", e);
        } catch (IOException e) {
            throw new U2FTokenException("Local token register error.", e);
        }

        byte[] signedData = RawMessageCodec.encodeRegistrationSignedBytes(applicationSha256, challengeSha256,
                keyHandle, userPublicKey);

        if (certificatePrivateKey == null) {
            LogUtils.e("attestation certificate private key is null");
        }
        byte[] signature = crypto.sign(signedData, certificatePrivateKey);
        LogUtils.d(ByteUtil.ByteArrayToHexString(signature));
        return new RegistrationResponse(userPublicKey, keyHandle, attestationCertificate, signature);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws U2FTokenException {
        byte[] applicationSha256 = authenticationRequest.getApplicationSha256();
        byte[] challengeSha256 = authenticationRequest.getChallengeSha256();
        byte[] keyHandle = authenticationRequest.getKeyHandle();
        byte control = authenticationRequest.getControl();

        if (control == AuthenticationRequest.USER_PRESENCE_SIGN) {
            // TODO: 2016/3/8 counter should be stored safely
            SharedPreferences sharedPreferences = context.getSharedPreferences("org.esec.mcg.android.fido.PREFERENCE_FILE_KEY"
                    .concat(".").concat(Base64.encodeToString(keyHandle, Base64.NO_WRAP | Base64.URL_SAFE)), Context.MODE_PRIVATE);
            int counter = sharedPreferences.getInt("Counter", 1);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("Counter", counter + 1);
            editor.commit();

            PrivateKey privateKey = keyHandleGenerator.getUserPrivateKey(keyHandle);
            byte[] signedData = RawMessageCodec.encodeAuthenticationSignedBytes(applicationSha256, (byte)0x01, counter, challengeSha256);

            byte[] signature = crypto.sign(signedData, privateKey);
            return new AuthenticationResponse((byte)0x01, counter, signature);
        } else if (control == AuthenticationRequest.CHECK_ONLY) {
            boolean keyHandlePresence = keyHandleGenerator.checkKeyHandle(keyHandle);
            if (keyHandlePresence) {
                throw new U2FTokenException(U2FTokenActivity.TEST_OF_PRESENCE_REQUIRED);
            } else {
                throw new U2FTokenException(U2FTokenActivity.INVALID_KEY_HANDLE);
            }
        } else {
            throw new U2FTokenException("unsupported control byte");
        }

    }
}
