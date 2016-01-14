package org.esec.mcg.androidu2f.codec;

import com.google.gson.JsonObject;

/**
 * Created by yz on 2016/1/14.
 */
public class ClientDataCodec {
    // Constants for ClientData.typ
    public static final String REQUEST_TYPE_REGISTER = "navigator.id.finishEnrollment";
    public static final String REQUEST_TYPE_AUTHENTICATE = "navigator.id.getAssertion";

    // Constants for building ClientData.challenge
    public static final String JSON_PROPERTY_REQUEST_TYPE = "typ";
    public static final String JSON_PROPERTY_SERVER_CHALLENGE_BASE64 = "challenge";
    public static final String JSON_PROPERTY_SERVER_ORIGIN = "origin";

    public static String encodeClientData(String requestType, String serverChallengeBase64, String facetID) {
        JsonObject clientData = new JsonObject();
        clientData.addProperty(JSON_PROPERTY_REQUEST_TYPE, requestType);
        clientData.addProperty(JSON_PROPERTY_SERVER_CHALLENGE_BASE64, serverChallengeBase64);
        clientData.addProperty(JSON_PROPERTY_SERVER_ORIGIN, facetID);
        return clientData.toString();
    }
}
