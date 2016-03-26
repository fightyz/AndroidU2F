package org.esec.mcg.androidu2f.codec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

import org.esec.mcg.androidu2f.msg.ErrorCode;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.jcajce.provider.symmetric.ARC4;

/**
 * Created by yz on 2016/3/10.
 */
public class ResponseCodec {
    public static Intent encodeResponse(String type, JSONObject responseData) {
        Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        Bundle data = new Bundle();
        JSONObject response = new JSONObject();
        try {
            response.put("type", type);
            response.put("responseData", responseData);
        } catch (JSONException e) {
            new RuntimeException(e);
        }
        data.putString("Response", response.toString());
        i.putExtras(data);
        return i;
    }

    public static JSONObject encodeError(ErrorCode errorCode, String errorMessage) {
        JSONObject error = new JSONObject();
        try {
            error.put("errorCode", errorCode.ordinal());
            error.put("errorMessage", errorMessage);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return error;
    }

    public static JSONObject encodeRegisterResponse(byte[] registrationResponse, String clientData) {
        String registrationDataBase64 = android.util.Base64.encodeToString(registrationResponse, Base64.NO_WRAP | Base64.URL_SAFE);
        String clientDataBase64 = android.util.Base64.encodeToString(clientData.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
        JSONObject registerResponse = new JSONObject();
        try {
            registerResponse.put("registrationData", registrationDataBase64);
            registerResponse.put("clientData", clientDataBase64);
            return registerResponse;
        } catch (JSONException e) {
            // TODO: 2016/3/10 Handle exception
            throw new RuntimeException(e);
        }
    }

    public static JSONObject encodeSignResponse(String keyHandle, byte[] authenticationResponse, String clientData) {
        String signatureDataBase64 = Base64.encodeToString(authenticationResponse, Base64.URL_SAFE);
        String clientDataBase64 = Base64.encodeToString(clientData.getBytes(), Base64.URL_SAFE);
        JSONObject signResponse = new JSONObject();
        try {
            signResponse.put("keyHandle", keyHandle);
            signResponse.put("signatureData", signatureDataBase64);
            signResponse.put("clientData", clientDataBase64);
            return signResponse;
        } catch (JSONException e) {
            // TODO: 2016/3/14 Handle exception
            throw new RuntimeException(e);
        }

    }
}
