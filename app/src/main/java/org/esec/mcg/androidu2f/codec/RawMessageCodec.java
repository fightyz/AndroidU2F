package org.esec.mcg.androidu2f.codec;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by yz on 2016/1/18.
 */
public class RawMessageCodec {

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
}
