package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationRequest;
import org.json.JSONObject;

/**
 * Created by yz on 2016/1/14.
 */
public abstract class U2FClient {
    protected static String clientData;
    protected String keyHandle;
    abstract public RegistrationRequest register(String u2fProtocolMessage) throws U2FException;
    abstract public AuthenticationRequest sign(JSONObject signRequest, boolean isSign) throws U2FException;

    /**
     * If request have "signRequests", then it will return AuthenticationRequest[]. Otherwise it will return null.
     * @param request
     * @param sign
     * @return
     * @throws U2FException
     */
    abstract public AuthenticationRequest[] signBatch(JSONObject request, boolean sign) throws U2FException;

    public static String getClientData() { return clientData; }
    abstract public String getClientDataForIndex(int index);
    abstract public String getKeyHandle(int index);

    //    void authenticate(String u2fProtoclMessage);
}
