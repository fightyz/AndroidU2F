package org.esec.mcg.androidu2f.token;

/**
 * Created by yz on 2016/1/18.
 */
public interface UserPresenceVerifier {
    public static final byte USER_PRESENT_FLAG = 0x01;

    byte verifyUserPresence();
}
