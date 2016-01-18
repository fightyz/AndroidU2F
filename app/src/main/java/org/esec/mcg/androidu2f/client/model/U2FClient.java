package org.esec.mcg.androidu2f.client.model;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;

/**
 * Created by yz on 2016/1/14.
 */
public interface U2FClient {
    RegisterRequest register(String u2fProtocolMessage) throws U2FException;
//    void authenticate(String u2fProtoclMessage);
}
