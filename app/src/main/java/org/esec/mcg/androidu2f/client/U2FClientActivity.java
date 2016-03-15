package org.esec.mcg.androidu2f.client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.esec.mcg.androidu2f.Constants;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.client.model.U2FClient;
import org.esec.mcg.androidu2f.client.model.U2FClientImpl;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.codec.ResponseCodec;
import org.esec.mcg.androidu2f.msg.ErrorCode;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.androidu2f.msg.U2FResponseType;
import org.esec.mcg.androidu2f.token.U2FToken;
import org.esec.mcg.androidu2f.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2f.token.msg.RegistrationRequest;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class U2FClientActivity extends AppCompatActivity {

    private String request;
    private String requestType;
    private String U2FOperationType;
    private JSONArray signRequests;
    private int signRequestIndex;

    private U2FClient u2fClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signRequestIndex = 0;
    }

    @Override
    protected void onResume() {
        LogUtils.d("onResume");
        super.onResume();
        Bundle extras = getIntent().getExtras();
        request = extras.getString("Request");
        U2FOperationType = extras.getString("U2FIntentType");

        try {
            requestType = (new JSONObject(request)).getString("type");
            u2fClient = new U2FClientImpl(this.getPackageManager().getPackageInfo(this.getPackageName(), this.getPackageManager().GET_SIGNATURES));
        } catch (JSONException e) {
            JSONObject error = ResponseCodec.encodeError(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.toString());
            String u2fResponseType;
            if (U2FOperationType.equals(U2FIntentType.U2F_OPERATION_REG)) {
                u2fResponseType = U2FResponseType.u2f_register_response.name();
            } else {
                u2fResponseType = U2FResponseType.u2f_sign_response.name();
            }
            Intent i = ResponseCodec.encodeResponse(u2fResponseType, error);

            setResult(RESULT_CANCELED, i);
            finish();
            return;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO: 2016/3/10 Handle exception
            e.printStackTrace();
            return;
        }

        // Register, type = u2f_register_request
        if (requestType.equals(U2FRequestType.u2f_register_request.name())) {
            try {
                RegistrationRequest registrationRequest = u2fClient.register(request);
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_token+json");
                Bundle data = new Bundle();
                data.putByteArray("RawMessage", RawMessageCodec.encodeRegistrationRequest(registrationRequest));
                data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_REG.name());
                i.putExtras(data);
                startActivityForResult(i, Constants.REG_ACTIVITY_RES_1); // Start token activity.
            } catch (U2FException e) {
                JSONObject error = ResponseCodec.encodeError(ErrorCode.BAD_REQUEST, e.getMessage());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                setResult(RESULT_CANCELED, i);
                finish();
                return;
            }
        } else if (requestType.equals(U2FRequestType.u2f_sign_request.name())) { // Sign, type = u2f_sign_request
            try {
                if (signRequestIndex == 0) {
                    signRequests = new JSONObject(request).getJSONArray("signRequests");
                }
                if (signRequestIndex == signRequests.length()) {
                    LogUtils.d("out of band");
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.DEVICE_INELIGIBLE, ErrorCode.DEVICE_INELIGIBLE.toString());
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                    return;
                }

                JSONObject signRequest = signRequests.getJSONObject(signRequestIndex);
                LogUtils.d("Index = " + signRequestIndex);
                LogUtils.d("signRequest = " + signRequest.toString());
                signRequestIndex++;
                AuthenticationRequest authenticationRequest = u2fClient.sign(signRequest.toString());
                Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
                i.addCategory("android.intent.category.DEFAULT");
                i.setType("application/fido.u2f_token+json");
                Bundle data = new Bundle();
                data.putByteArray("RawMessage", RawMessageCodec.encodeAuthenticationRequest(authenticationRequest));
                data.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_SIGN.name());
                i.putExtras(data);
                startActivityForResult(i, Constants.SIGN_ACTIVITY_RES_2); // Start token activity
            } catch (U2FException | JSONException e) {
                e.printStackTrace();
                JSONObject error = ResponseCodec.encodeError(ErrorCode.OTHER_ERROR, ErrorCode.OTHER_ERROR.toString().concat(" Wrong in Token."));
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), error);
                setResult(RESULT_CANCELED, i);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REG_ACTIVITY_RES_1) { // register
            if (resultCode == RESULT_OK) { // success
                JSONObject registerResponse = ResponseCodec.encodeRegisterResponse(data.getByteArrayExtra("RawMessage"), U2FClient.getClientData());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), registerResponse);
                setResult(RESULT_OK, i);
                finish();
            } else if (resultCode == RESULT_CANCELED) { // fail
                JSONObject error = ResponseCodec.encodeError(ErrorCode.OTHER_ERROR, ErrorCode.OTHER_ERROR.toString().concat(" Wrong in Token."));
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                setResult(RESULT_CANCELED, i);
                finish();
            }

        } else if (requestCode == Constants.SIGN_ACTIVITY_RES_2) {
            LogUtils.d("onActivityResult");
            // If previous sign request failed, then do the next one.
            if (resultCode == RESULT_CANCELED) {
//                try {
//                    if (signRequests.length() == signRequestIndex) {
//                        LogUtils.d("out of band");
//                        JSONObject error = ResponseCodec.encodeError(ErrorCode.DEVICE_INELIGIBLE, ErrorCode.DEVICE_INELIGIBLE.toString());
//                        Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), error);
//                        setResult(RESULT_CANCELED, i);
//                        finish();
//                        return;
//                    }
//                    JSONObject signRequest = signRequests.getJSONObject(signRequestIndex);
//
//                    LogUtils.d(signRequest.toString());
//                    LogUtils.d("index = " + signRequestIndex);
//                    signRequestIndex++;
//                    AuthenticationRequest authenticationRequest = u2fClient.sign(signRequest.toString());
//                    Intent i = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
//                    i.addCategory("android.intent.category.DEFAULT");
//                    i.setType("application/fido.u2f_token+json");
//                    Bundle bundleData = new Bundle();
//                    bundleData.putByteArray("RawMessage", RawMessageCodec.encodeAuthenticationRequest(authenticationRequest));
//                    bundleData.putString("U2FIntentType", U2FIntentType.U2F_OPERATION_SIGN.name());
//                    i.putExtras(bundleData);
//                    startActivityForResult(i, Constants.SIGN_ACTIVITY_RES_2); // Start token activity
//                } catch (JSONException | U2FException e) {
//                    e.printStackTrace();
//                    JSONObject error = ResponseCodec.encodeError(ErrorCode.OTHER_ERROR, ErrorCode.OTHER_ERROR.toString().concat(" Wrong in Token."));
//                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), error);
//                    setResult(RESULT_CANCELED, i);
//                    finish();
//                }
            } else if (resultCode == RESULT_OK) {
                LogUtils.d("=============");
                JSONObject signResponse = ResponseCodec.encodeSignResponse(u2fClient.getKeyHandle(), data.getByteArrayExtra("RawMessage"), u2fClient.getClientData());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), signResponse);
                setResult(RESULT_OK, i);
                finish();
            }
        }
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
