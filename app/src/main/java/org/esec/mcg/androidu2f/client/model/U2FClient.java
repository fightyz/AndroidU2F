package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.RegistrationRequest;

/**
 * Created by yz on 2016/1/14.
 */
public abstract class U2FClient {
    protected String clientData;
    abstract public RegistrationRequest register(String u2fProtocolMessage) throws U2FException;

    public String getClientData() {
        return clientData;
    }

    //    void authenticate(String u2fProtoclMessage);
}
