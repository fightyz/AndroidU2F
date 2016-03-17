package org.esec.mcg.androidu2fsimulator.token.msg;

import org.esec.mcg.androidu2fsimulator.token.U2FTokenException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Created by yz on 2016/3/16.
 */
public class RawMessageCodec {
    public static final byte REGISTRATION_RESERVED_BYTE_VALUE = 0x05;
    public static final byte REGISTRATION_SIGNED_RESERVED_BYTE_VALUE = 0x00;

    public static RegistrationRequest decodeRegistrationRequest(byte[] data) throws U2FTokenException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
        byte[] appIdSha256 = new byte[32];
        byte[] challengeSha256 = new byte[32];
        try {
            inputStream.readFully(challengeSha256);
            inputStream.readFully(appIdSha256);

            if (inputStream.available() != 0) {
                throw new U2FTokenException("Message ends with unexpected data");
            }

            return new RegistrationRequest(appIdSha256, challengeSha256);
        } catch (IOException e) {
            throw new U2FTokenException("Error when parsing raw RegistrationRequest", e);
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

    public static byte[] encodeAuthenticationSignedBytes(byte[] applicationSha256,
                                                         byte userPresence,
                                                         int counter,
                                                         byte[] challengeSha256) {
        byte[] signedData = new byte[applicationSha256.length + 1 + 4 + challengeSha256.length];
        byte[] rawCounter = new byte[4];
        rawCounter[0] = (byte)((counter >> 24) & 0xff);
        rawCounter[1] = (byte)((counter >> 16) & 0xff);
        rawCounter[2] = (byte)((counter >> 8) & 0xff);
        rawCounter[3] = (byte)((counter) & 0xff);

        ByteBuffer.wrap(signedData)
                .put(applicationSha256)
                .put(userPresence)
                .put(rawCounter)
                .put(challengeSha256);
        return signedData;
    }

    public static byte[] encodeRegistrationResponse(RegistrationResponse registrationResponse) throws U2FTokenException {
        byte[] userPublicKey = registrationResponse.getUserPublicKey();
        byte[] keyHandle = registrationResponse.getKeyHandle();
        X509Certificate attestationCertificate = registrationResponse.getAttestationCertificate();
        byte[] signature = registrationResponse.getSignature();

        byte[] attestationCertificateBytes;
        try {
            attestationCertificateBytes = attestationCertificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new U2FTokenException("Error when encoding attestation certificate.", e);
        }

        if (keyHandle.length > 255) {
            throw new U2FTokenException("keyHandle length cannot be longer than 255 bytes!");
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

    public static AuthenticationRequest decodeAuthenticationRequest(byte[] data) throws U2FTokenException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
        byte control;
        byte[] challengeSha256 = new byte[32];
        byte[] appIdSha256 = new byte[32];
        byte keyHandleLength;
        byte[] keyHandle;
        try {
            control = inputStream.readByte();
            inputStream.readFully(challengeSha256);
            inputStream.readFully(appIdSha256);
            keyHandleLength = inputStream.readByte();
            keyHandle = new byte[keyHandleLength & 0x00ff];
            inputStream.readFully(keyHandle);

            if (inputStream.available() != 0) {
                throw new U2FTokenException("Message ends with unexpected data");
            }
            return new AuthenticationRequest(control, challengeSha256, appIdSha256, keyHandle);
        } catch (IOException e) {
            throw new U2FTokenException("Error when parsing raw AuthenticationRequest", e);
        }
    }

    public static byte[] encodeAuthenticationResponse(AuthenticationResponse authenticationResponse) {
        byte userPresence = authenticationResponse.getUserPresence();
        int counter = authenticationResponse.getCounter();
        byte[] signature = authenticationResponse.getSignature();

        byte[] result = new byte[1 + 4 + signature.length];
        ByteBuffer.wrap(result)
                .put(userPresence)
                .putInt(counter)
                .put(signature);
        return result;
    }
}
