package org.esec.mcg.androidu2f.client.msg;

/**
 * Created by yz on 2016/4/15.
 */
public class RegisterRequest {
    private final String version;
    private final String challenge;
    private final String appId;

    public RegisterRequest(String version, String challenge, String appId) {
        this.version = version;
        this.challenge = challenge;
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getVersion() {
        return version;
    }
}
