package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;

import java.security.KeyPair;
import java.security.PrivateKey;
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

    //TODO Implements these interface.
    public LocalU2FToken() {
        attestationCertificate = null;
        certificatePrivateKey = null;
        keyPairGenerator = null;
        keyHandleGenerator = null;
        dataStore = null;
        userPresenceVerifier = null;
        crypto = null;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) throws U2FException{
        byte[] applicationSha256 = registerRequest.getApplicationSha256();
        byte[] challengeSha256 = registerRequest.getChallengeSha256();

        byte userPresent = userPresenceVerifier.verifyUserPresence();
        if ((userPresent & UserPresenceVerifier.USER_PRESENT_FLAG) == 0x00) {
            throw new U2FException("Cannot verify user presence.");
        }

        KeyPair keyPair = keyPairGenerator.generateKeyPair(applicationSha256, challengeSha256);
        byte[] keyHandle = keyHandleGenerator.generateKeyHandle(applicationSha256, keyPair);

        dataStore.storeKeyPair(keyHandle, keyPair);

        byte[] userPublicKey = keyPairGenerator.encodePublicKey(keyPair.getPublic());

        byte[] signedData = RawMessageCodec.encodeRegistrationSignedBytes(applicationSha256, challengeSha256,
                keyHandle, userPublicKey);

        byte[] signature = crypto.sign(signedData, certificatePrivateKey);
        return new RegisterResponse(userPublicKey, keyHandle, attestationCertificate, signature);
    }
}
