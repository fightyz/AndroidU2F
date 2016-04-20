package org.esec.mcg.androidu2f.client.msg;

/**
 * Created by yz on 2016/4/15.
 */
public class Request {
    private final String type;
    private final SignRequest[] signRequests;
    private final RegisterRequest[] registerRequests;
    private final int timeoutSeconds;
    private final int requestId;

    public Request(String type, SignRequest[] signRequests, RegisterRequest[] registerRequest, int timeoutSeconds, int requestId) {
        this.type = type;
        this.signRequests = signRequests;
        this.registerRequests = registerRequest;
        this.timeoutSeconds = timeoutSeconds;
        this.requestId = requestId;
    }

    public RegisterRequest[] getRegisterRequests() {
        return registerRequests;
    }

    public int getRequestId() {
        return requestId;
    }

    public SignRequest[] getSignRequests() {
        return signRequests;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getType() {
        return type;
    }
}
