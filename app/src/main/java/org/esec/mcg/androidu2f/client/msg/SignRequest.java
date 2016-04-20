package org.esec.mcg.androidu2f.client.msg;

/**
 * Created by yz on 2016/4/15.
 */
public class SignRequest {
    private final String version;
    private final String challenge;
    private final String keyHandle;
    private final String appId;

    public SignRequest(String version, String challenge, String keyHandle, String appId) {
        this.version = version;
        this.challenge = challenge;
        this.keyHandle = keyHandle;
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getKeyHandle() {
        return keyHandle;
    }

    public String getVersion() {
        return version;
    }
}
