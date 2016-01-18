package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.androidu2f.token.msg.RegisterResponse;

/**
 * Created by yz on 2016/1/18.
 */
public class SwipeCardU2FToken implements U2FToken {
    @Override
    public RegisterResponse register(RegisterRequest registerRequest) throws U2FException {
        return null;
    }
}
