package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;

/**
 * Created by yz on 2016/1/14.
 */
public class LocalU2FToken implements U2FToken {
    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        return null;
    }
}
