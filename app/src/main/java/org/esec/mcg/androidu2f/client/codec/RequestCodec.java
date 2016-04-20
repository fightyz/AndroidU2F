package org.esec.mcg.androidu2f.client.codec;

import android.support.annotation.Nullable;

import org.esec.mcg.androidu2f.client.msg.RegisterRequest;
import org.esec.mcg.androidu2f.client.msg.Request;
import org.esec.mcg.androidu2f.client.msg.SignRequest;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yz on 2016/4/15.
 */
public class RequestCodec {
    public static Request encodeRequest(String requestStr) {
        try {
            LogUtils.d(requestStr);
            JSONObject requestJson = new JSONObject(requestStr);

            RegisterRequest[] registerRequests = null;
            SignRequest[] signRequests = null;
            if (requestJson.has("registerRequests")) {
                JSONArray registerRequestsJson = requestJson.getJSONArray("registerRequests");
                registerRequests = encodeRegisterRequest(registerRequestsJson);
            }
            if (requestJson.has("signRequests")) {
                JSONArray signRequestsJson = requestJson.getJSONArray("signRequests");
                signRequests = encodeSignRequest(signRequestsJson);
            }

            int timeoutSeconds = -1;
            int requestId = -1;
            if (requestJson.has("timeoutSeconds")) {
                timeoutSeconds = Integer.valueOf(requestJson.getString("timeoutSeconds"));
            }
            if (requestJson.has("requestId")) {
                requestId = Integer.valueOf(requestJson.getString("requestId"));
            }

            return new Request(requestJson.getString("type"),
                    signRequests,
                    registerRequests,
                    timeoutSeconds,
                    requestId);
        } catch (JSONException e) {
            throw new RuntimeException("Bad request", e);
        }
    }

    @Nullable
    public static RegisterRequest[] encodeRegisterRequest(JSONArray registerRequstsJson) {
        try {
            if (registerRequstsJson == null || registerRequstsJson.length() == 0) {
                return null;
            } else {
                RegisterRequest[] registerRequests = new RegisterRequest[registerRequstsJson.length()];
                for (int i = 0; i < registerRequstsJson.length(); i++) {
                    JSONObject registerRequestJson = registerRequstsJson.getJSONObject(i);
                    registerRequests[i] = new RegisterRequest(registerRequestJson.getString("version"),
                            registerRequestJson.getString("challenge"),
                            registerRequestJson.getString("appId")
                    );
                }
                return registerRequests;
            }
        } catch (JSONException e) {
            throw new RuntimeException("Bad request", e);
        }
    }

    public static SignRequest[] encodeSignRequest(JSONArray signRequestsJson) {
        try {
            if (signRequestsJson == null || signRequestsJson.length() == 0) {
                return null;
            } else {
                SignRequest[] signRequests = new SignRequest[signRequestsJson.length()];
                for (int i = 0; i < signRequestsJson.length(); i++) {
                    JSONObject signRequestJson = signRequestsJson.getJSONObject(i);
                    signRequests[i] = new SignRequest(signRequestJson.getString("version"),
                            signRequestJson.getString("challenge"),
                            signRequestJson.getString("keyHandle"),
                            signRequestJson.getString("appId")
                    );
                }
                return signRequests;
            }
        } catch (JSONException e) {
            throw new RuntimeException("Bad request", e);
        }

    }
}
