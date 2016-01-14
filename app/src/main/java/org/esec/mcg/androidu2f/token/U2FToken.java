package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;

/**
 * Created by yz on 2016/1/14.
 */
public interface U2FToken {
    RegisterResponse register(RegisterRequest registerRequest);
//    AuthenticateResponse authenticate(AuthenticateRequest authenticateRequest);
}
