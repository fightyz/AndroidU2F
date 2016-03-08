package org.esec.mcg.androidu2f.token;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2f.token.msg.AuthenticationResponse;
import org.esec.mcg.androidu2f.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2f.token.msg.RegistrationResponse;

/**
 * Created by yz on 2016/1/18.
 */
public class SwipeCardU2FToken implements U2FToken {
    @Override
    public RegistrationResponse register(RegistrationRequest registrationRequest) throws U2FException {
        return null;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws U2FException {
        return null;
    }
}
