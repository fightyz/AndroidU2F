package org.esec.mcg.androidu2f.client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.esec.mcg.androidu2f.Constants;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.client.card.JavaCardTokenActivity;
import org.esec.mcg.androidu2f.client.model.U2FClient;
import org.esec.mcg.androidu2f.client.model.U2FClientImpl;
import org.esec.mcg.androidu2f.client.msg.U2FTokenIntentType;
import org.esec.mcg.androidu2f.codec.RawMessageCodec;
import org.esec.mcg.androidu2f.codec.ResponseCodec;
import org.esec.mcg.androidu2f.msg.ErrorCode;
import org.esec.mcg.androidu2f.client.msg.RegistrationRequest;
import org.esec.mcg.androidu2f.msg.U2FIntentType;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.androidu2f.msg.U2FResponseType;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.User;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class U2FClientActivity extends AppCompatActivity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private String request;
    private String requestType;
    private String U2FOperationType;
    private JSONArray signRequests;

    private U2FClient u2fClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            LogUtils.d("555");
        }
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
            // "type" field is wrong, can't be resolved.
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
            throw new RuntimeException(e);
        }

        // Register, type = u2f_register_request
        if (requestType.equals(U2FRequestType.u2f_register_request.name())) {
            try {
                JSONObject requestJson = new JSONObject(request);
                if (requestJson.has("signRequests")) {
                    signRequests = requestJson.getJSONArray("signRequests");
                    if (signRequests.length() != 0) {
                        AuthenticationRequest[] authenticationRequestsBatch = u2fClient.signBatch(signRequests, false);
                        RegistrationRequest registrationRequest = u2fClient.register(request);
                        Intent i = genTokenIntent(U2FTokenIntentType.U2F_OPERATION_REG,
                                RawMessageCodec.encodeRegistrationRequest(registrationRequest), authenticationRequestsBatch);
                        startActivityForResult(i, Constants.REG_ACTIVITY_RES_1);
                    } else {
                        RegistrationRequest registrationRequest = u2fClient.register(request);
                        Intent i = genTokenIntent(U2FTokenIntentType.U2F_OPERATION_REG,
                                RawMessageCodec.encodeRegistrationRequest(registrationRequest), null);
                        startActivityForResult(i, Constants.REG_ACTIVITY_RES_1);
                    }
                } else {
                    RegistrationRequest registrationRequest = u2fClient.register(request);
                    Intent i = genTokenIntent(U2FTokenIntentType.U2F_OPERATION_REG,
                            RawMessageCodec.encodeRegistrationRequest(registrationRequest), null);
                    startActivityForResult(i, Constants.REG_ACTIVITY_RES_1);
                }

            } catch (U2FException | JSONException e) {
                LogUtils.d("------------------");
                JSONObject error = ResponseCodec.encodeError(ErrorCode.BAD_REQUEST, e.getMessage());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                setResult(RESULT_CANCELED, i);
                finish();
                return;
            }
        } else if (requestType.equals(U2FRequestType.u2f_sign_request.name())) { // Sign, type = u2f_sign_request
            try {
                JSONObject requestJson = new JSONObject(request);
                if (requestJson.has("signRequests")) {
                    signRequests = requestJson.getJSONArray("signRequests");
                    AuthenticationRequest[] authenticationRequestsBatch = u2fClient.signBatch(signRequests, true);
                    Intent i = genTokenIntent(U2FTokenIntentType.U2F_OPERATION_SIGN_BATCH,
                            null, authenticationRequestsBatch);
                    startActivityForResult(i, Constants.SIGN_ACTIVITY_RES_2);
                }
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
            LogUtils.d("REG_ACTIVITY_RES_1");
            if (resultCode == RESULT_OK) { // success
                if (data.getIntExtra("SW", 0) == Constants.SW_TEST_OF_USER_PRESENCE_REQUIRED) { // token had already been registered
                    Toast.makeText(this, "Token had already been registered.", Toast.LENGTH_LONG).show();
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.DEVICE_INELIGIBLE , ErrorCode.DEVICE_INELIGIBLE .toString().concat("Token had already been registered."));
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                } else {
                    JSONObject registerResponse = ResponseCodec.encodeRegisterResponse(data.getByteArrayExtra("RawMessage"), U2FClient.getClientData());
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), registerResponse);
                    setResult(RESULT_OK, i);
                    finish();
                }

            } else if (resultCode == RESULT_CANCELED) { // fail
                if (data.getIntExtra("SW", 0) == Constants.SW_TEST_OF_USER_PRESENCE_REQUIRED) {
                    Toast.makeText(this, "Please show me your token.", Toast.LENGTH_LONG).show();
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.OTHER_ERROR, ErrorCode.OTHER_ERROR.toString().concat("Please show me your token."));
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                } else {
                    throw new RuntimeException("should not happend!!!");
                }

            }

        } else if (requestCode == Constants.REG_ACTIVITY_RES_3) { // Reg: sign's check only
            LogUtils.d("sign's check only");
            if (resultCode == RESULT_OK) {
                JSONObject registerResponse = ResponseCodec.encodeRegisterResponse(data.getByteArrayExtra("RawMessage"), U2FClient.getClientData());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), registerResponse);
                setResult(RESULT_OK, i);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                if (data.getIntExtra("SW", 0) == Constants.SW_TEST_OF_USER_PRESENCE_REQUIRED) { // success, key handle had been registered
                    Toast.makeText(this, "show me your card", Toast.LENGTH_LONG).show();
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.DEVICE_INELIGIBLE, ErrorCode.DEVICE_INELIGIBLE.toString());
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                } else if (data.getIntExtra("SW", 0) == Constants.SW_INVALID_KEY_HANDLE) { // fail, bad key handle
                    Toast.makeText(this, "Bad Key Handle. Do register", Toast.LENGTH_LONG).show();
                } else {
                    throw new RuntimeException("shouldn't happen!!!");
                }
            }
        }

        else if (requestCode == Constants.SIGN_ACTIVITY_RES_2) { // sign
            LogUtils.d("onActivityResult");
            // If previous sign request failed, then do the next one.
            if (resultCode == RESULT_CANCELED) {
                if (data.getIntExtra("SW", 0) == Constants.SW_TEST_OF_USER_PRESENCE_REQUIRED) {
                    Toast.makeText(this, "Please show me your token.", Toast.LENGTH_LONG).show();
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.OTHER_ERROR, ErrorCode.OTHER_ERROR.toString().concat(" Wrong in Token."));
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_register_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                } else if (data.getIntExtra("SW", 0) == Constants.SW_INVALID_KEY_HANDLE) {
                    Toast.makeText(this, "Bad Key Handle..", Toast.LENGTH_LONG).show();
                    JSONObject error = ResponseCodec.encodeError(ErrorCode.DEVICE_INELIGIBLE, ErrorCode.DEVICE_INELIGIBLE.toString());
                    Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), error);
                    setResult(RESULT_CANCELED, i);
                    finish();
                } else {
                    throw new RuntimeException("shouldnt happendddd.");
                }
            } else if (resultCode == RESULT_OK) {
                JSONObject signResponse = ResponseCodec.encodeSignResponse(u2fClient.getKeyHandle(), data.getByteArrayExtra("RawMessage"), u2fClient.getClientData());
                Intent i = ResponseCodec.encodeResponse(U2FResponseType.u2f_sign_response.name(), signResponse);
                setResult(RESULT_OK, i);
                finish();
            }
        } else if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
//            if ()
            LogUtils.d("666");
        } else {
            LogUtils.d("can not happedndd!!");
        }
    }

    public void swipeProceed(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private static void startRegWithCheck() {

    }

    private static Intent genTokenIntent(U2FTokenIntentType intentType,
                                         byte[] rawMessage,
                                         AuthenticationRequest[] authenticationRequests) {
        Intent i = new Intent();
        i.setAction("org.fidoalliance.intent.FIDO_OPERATION");
        i.addCategory("android.intent.category.DEFAULT");
        i.setType("application/fido.u2f_token+json");

        Bundle data = new Bundle();

        switch (intentType) {
            case U2F_OPERATION_REG:
                data.putByteArray("RawMessage", rawMessage);
                data.putParcelableArray("signBatch", authenticationRequests);
                i.putExtra(U2FTokenIntentType.U2F_OPERATION_REG.name(), data);
                break;
            case U2F_OPERATION_SIGN:
                data.putByteArray("RawMessage", rawMessage);
                i.putExtra(U2FTokenIntentType.U2F_OPERATION_SIGN.name(), data);
                break;
            case U2F_OPERATION_SIGN_BATCH:
                data.putParcelableArray("signBatch", authenticationRequests);
                i.putExtra(U2FTokenIntentType.U2F_OPERATION_SIGN_BATCH.name(), data);
                break;
        }
        return i;
    }
}
