package org.esec.mcg.androidu2f.client.op;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by yz on 2016/1/14.
 * Client's register operation.
 */
public class U2FServerRequest {
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private String type;


    /**
     * Get the
     * @param url
     * @return
     */
    public String getU2fMsgRegRequest(URL url) {
        String msg = "{\"u2fProtocolMessage\":\"";
        try {
            String serverResponse = getRegRequest(url);
            LogUtils.d("server register request: " + serverResponse);
            JSONObject reg = new JSONObject(serverResponse);
            String appId = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("appId");
            return reg.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * Get request from U2F server.
     * @param url
     * @return
     */
    public String getRequest(URL url) {
        return "{\"type\": \"u2f_register_request\", \"signRequest\": [], \"registerRequests\": [{\"challenge\": \"mLkHCmQZGbZEXefhWByeKo5zTFldYLIZFRGeHdvTFBc=\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8000\"}]}";
    }

    private String getRegRequest(URL url) {
        //TODO get register request through web
//        return "{\"authenticateRequests\": [], \n" +
//                "\"registerRequests\": [{\"challenge\": \"9s80ruHc6q9shJM5WLfOmz-ejb_Rm8dmWCnOvgZ2ovw\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8081\"}]}";

//        return HTTP.get(url);
        return "{\"signrequest\": [], \"registerRequests\": [{\"challenge\": \"mLkHCmQZGbZEXefhWByeKo5zTFldYLIZFRGeHdvTFBc=\", \"version\": \"U2F_V2\", \"appId\": \"http://localhost:8000\"}]}";
    }

//    public genRawMessage
}
