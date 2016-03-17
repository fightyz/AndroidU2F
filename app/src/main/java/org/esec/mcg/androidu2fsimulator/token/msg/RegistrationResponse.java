package org.esec.mcg.androidu2fsimulator.token.msg;

import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Created by yz on 2016/1/14.
 */
public class RegistrationResponse {
    private final byte[] userPublicKey;
    private final byte[] keyHandle;
    private final X509Certificate attestationCertificate;
    private final byte[] signature;

    public RegistrationResponse(byte[] userPublicKey, byte[] keyHandle,
                                X509Certificate attestationCertificate, byte[] signature) {
        super();
        this.userPublicKey = userPublicKey;
        this.keyHandle = keyHandle;
        this.attestationCertificate = attestationCertificate;
        this.signature = signature;
    }

    /**
     * This is the (uncompressed) x,y-representation of a curve point on the P-256
     * NIST elliptic curve.
     */
    public byte[] getUserPublicKey() {
        return userPublicKey;
    }

    /**
     * This a handle that allows the U2F token to identify the generated key pair.
     * U2F tokens MAY wrap the generated private key and the application id it was
     * generated for, and output that as the key handle.
     */
    public byte[] getKeyHandle() {
        return keyHandle;
    }

    /**
     * This is a X.509 certificate.
     */
    public X509Certificate getAttestationCertificate() {
        return attestationCertificate;
    }

    /** This is a ECDSA signature (on P-256) */
    public byte[] getSignature() {
        return signature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + attestationCertificate.hashCode();
        result = prime * result + Arrays.hashCode(keyHandle);
        result = prime * result + Arrays.hashCode(signature);
        result = prime * result + Arrays.hashCode(userPublicKey);
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
        RegistrationResponse other = (RegistrationResponse) obj;
        if (!attestationCertificate.equals(other.attestationCertificate))
            return false;
        if (!Arrays.equals(keyHandle, other.keyHandle))
            return false;
        if (!Arrays.equals(signature, other.signature))
            return false;
        if (!Arrays.equals(userPublicKey, other.userPublicKey))
            return false;
        return true;
    }
}
