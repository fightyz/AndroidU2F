package org.esec.mcg.androidu2fsimulator.token;

import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationResponse;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationResponse;

/**
 * Created by yz on 2016/1/14.
 */
public interface U2FToken {
    RegistrationResponse register(RegistrationRequest registrationRequest) throws U2FTokenException;
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws U2FTokenException;
}
