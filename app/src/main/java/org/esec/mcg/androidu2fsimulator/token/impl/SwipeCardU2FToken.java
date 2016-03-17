package org.esec.mcg.androidu2fsimulator.token.impl;

import org.esec.mcg.androidu2fsimulator.token.U2FToken;
import org.esec.mcg.androidu2fsimulator.token.U2FTokenException;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationResponse;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationResponse;

/**
 * Created by yz on 2016/1/18.
 */
public class SwipeCardU2FToken implements U2FToken {
    @Override
    public RegistrationResponse register(RegistrationRequest registrationRequest) throws U2FTokenException {
        return null;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws U2FTokenException {
        return null;
    }
}
