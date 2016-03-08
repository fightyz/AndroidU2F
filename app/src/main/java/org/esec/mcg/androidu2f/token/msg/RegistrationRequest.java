package org.esec.mcg.androidu2f.token.msg;

import java.util.Arrays;

/**
 * Byte array of challengeSha256 and applicationSha256
 * Created by yz on 2016/1/14.
 */
public class RegistrationRequest {
    private final byte[] challengeSha256;
    private final byte[] applicationSha256;

    public RegistrationRequest(byte[] applicationSha256, byte[] challengeSha256) {
        this.challengeSha256 = challengeSha256;
        this.applicationSha256 = applicationSha256;
    }

    /**
     * The challenge parameter is the SHA-256 hash of the Client Data, a
     * stringified JSON datastructure that the FIDO Client prepares. Among other
     * things, the Client Data contains the challenge from the relying party
     * (hence the name of the parameter). See below for a detailed explanation of
     * Client Data.
     */
    public byte[] getChallengeSha256() {
        return challengeSha256;
    }

    /**
     * The application parameter is the SHA-256 hash of the application identity
     * of the application requesting the registration
     */
    public byte[] getApplicationSha256() {
        return applicationSha256;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(applicationSha256);
        result = prime * result + Arrays.hashCode(challengeSha256);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegistrationRequest other = (RegistrationRequest) obj;
        if (!Arrays.equals(applicationSha256, other.applicationSha256))
            return false;
        if (!Arrays.equals(challengeSha256, other.challengeSha256))
            return false;
        return true;
    }
}
