package org.esec.mcg.androidu2f.codec;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Created by yz on 2016/1/18.
 */
public class RawMessageCodec {

    public static final byte REGISTRATION_RESERVED_BYTE_VALUE = 0x05;
    public static final byte REGISTRATION_SIGNED_RESERVED_BYTE_VALUE = 0x00;

    public static byte[] encodeRegisterRequest(RegisterRequest registerRequest) {
        byte[] appIdSha256 = registerRequest.getApplicationSha256();
        byte[] challengeSha256 = registerRequest.getChallengeSha256();
        byte[] result = new byte[appIdSha256.length + challengeSha256.length];

        ByteBuffer.wrap(result)
                .put(challengeSha256)
                .put(appIdSha256);

        return result;
    }

    public static RegisterRequest decodeRegisterRequest(byte[] data) throws U2FException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
        byte[] appIdSha256 = new byte[32];
        byte[] challengeSha256 = new byte[32];
        try {
            inputStream.readFully(challengeSha256);
            inputStream.readFully(appIdSha256);

            if (inputStream.available() != 0) {
                throw new U2FException("Message ends with unexpected data");
            }

            return new RegisterRequest(appIdSha256, challengeSha256);
        } catch (IOException e) {
            throw new U2FException("Error when parsing raw RegistrationResponse", e);
        }
    }

    public static byte[] encodeRegistrationSignedBytes(byte[] applicationSha256,
                                                       byte[] challengeSha256,
                                                       byte[] keyHandle,
                                                       byte[] userPublicKey) {
        byte[] signedData = new byte[1 + applicationSha256.length + challengeSha256.length
                + keyHandle.length + userPublicKey.length];
        ByteBuffer.wrap(signedData)
                .put(REGISTRATION_SIGNED_RESERVED_BYTE_VALUE)
                .put(applicationSha256)
                .put(challengeSha256)
                .put(keyHandle)
                .put(userPublicKey);
        return signedData;
    }

    public static byte[] encodeRegisterResponse(RegisterResponse registerResponse) throws U2FException {
        byte[] userPublicKey = registerResponse.getUserPublicKey();
        byte[] keyHandle = registerResponse.getKeyHandle();
        X509Certificate attestationCertificate = registerResponse.getAttestationCertificate();
        byte[] signature = registerResponse.getSignature();

        byte[] attestationCertificateBytes;
        try {
            attestationCertificateBytes = attestationCertificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new U2FException("Error when encoding attestation certificate.", e);
        }

        if (keyHandle.length > 255) {
            throw new U2FException("keyHandle length cannot be longer than 255 bytes!");
        }

        byte[] result = new byte[1 + userPublicKey.length + 1 + keyHandle.length
                + attestationCertificateBytes.length + signature.length];
        ByteBuffer.wrap(result)
                .put(REGISTRATION_RESERVED_BYTE_VALUE)
                .put(userPublicKey)
                .put((byte) keyHandle.length)
                .put(keyHandle)
                .put(attestationCertificateBytes)
                .put(signature);
        return result;
    }
}
