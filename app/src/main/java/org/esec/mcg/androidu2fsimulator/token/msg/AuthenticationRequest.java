package org.esec.mcg.androidu2fsimulator.token.msg;

import android.os.Parcel;
import android.os.Parcelable;

import org.esec.mcg.utils.ByteUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by yz on 2016/3/7.
 */
public class AuthenticationRequest implements Parcelable {
    public static final byte CHECK_ONLY = 0x07;
    public static final byte USER_PRESENCE_SIGN = 0x03;

    private byte control;
    private byte[] challengeSha256;
    private byte[] applicationSha256;
    private byte[] keyHandle;

    public AuthenticationRequest(byte control, byte[] challengeSha256, byte[] applicationSha256,
                                 byte[] keyHandle) {
        this.control = control;
        this.challengeSha256 = challengeSha256;
        this.applicationSha256 = applicationSha256;
        this.keyHandle = keyHandle;
    }

    @Override
    public String toString() {
        return "AuthenticationRequest{" +
                ", control=" + control +
                "applicationSha256=" + ByteUtil.ByteArrayToHexString(applicationSha256) +
                ", challengeSha256=" + ByteUtil.ByteArrayToHexString(challengeSha256) +
                ", keyHandle=" + ByteUtil.ByteArrayToHexString(keyHandle) +
                '}';
    }

    private AuthenticationRequest(Parcel source) {
        control = source.readByte();

        challengeSha256 = new byte[source.readInt()];
        source.readByteArray(challengeSha256);

        applicationSha256 = new byte[source.readInt()];
        source.readByteArray(applicationSha256);
//
        keyHandle = new byte[source.readInt()];
        source.readByteArray(keyHandle);
    }

    /** The FIDO Client will set the control byte to one of the following values:
     * 0x07 ("check-only")
     * 0x03 ("enforce-user-presence-and-sign")
     */
    public byte getControl() {
        return control;
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

    /** The key handle obtained during registration. */
    public byte[] getKeyHandle() {
        return keyHandle;
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(control, challengeSha256, applicationSha256, keyHandle);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        AuthenticationRequest other = (AuthenticationRequest) obj;
//        return Objects.equals(control, other.control)
//                && Arrays.equals(challengeSha256, other.challengeSha256)
//                && Arrays.equals(applicationSha256, other.applicationSha256)
//                && Arrays.equals(keyHandle, other.keyHandle);
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(control);

        dest.writeInt(challengeSha256.length);
        dest.writeByteArray(challengeSha256);

        dest.writeInt(applicationSha256.length);
        dest.writeByteArray(applicationSha256);
//
        dest.writeInt(keyHandle.length);
        dest.writeByteArray(keyHandle);
    }

    public static final Creator<AuthenticationRequest> CREATOR = new Creator<AuthenticationRequest>() {

        @Override
        public AuthenticationRequest createFromParcel(Parcel source) {
            return new AuthenticationRequest(source);
        }

        @Override
        public AuthenticationRequest[] newArray(int size) {
            return new AuthenticationRequest[size];
        }

    };
}
