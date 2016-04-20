package org.esec.mcg.androidu2f.curl;

import android.content.Context;
import android.content.res.Resources;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.msg.U2FRequestType;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * Created by YangZhou on 2016/4/4.
 */
public class HttpServiceClient {
    // Constants related to JSON keys for StrongAuth's webservices
    public static final String JSON_KEY_DID_LABEL = "did";
    public static final String JSON_KEY_ERROR_LABEL = "Error";
    public static final String JSON_KEY_MESSAGE_LABEL = "Message";
    public static final String JSON_KEY_PROTOCOL_LABEL = "protocol";
    public static final String JSON_KEY_SVCUSERNAME_LABEL = "svcusername";
    public static final String JSON_KEY_SVCPASSWORD_LABEL = "svcpassword";
    public static final String JSON_KEY_USERNAME_LABEL = "username";

    // Constants related to FIDO server's webservices
    public static final int SKFE_AUTHENTICATE_WEBSERVICE = 1;
    public static final int SKFE_AUTHORIZE_WEBSERVICE = 2;
    public static final int SKFE_PREAUTHENTICATE_WEBSERVICE = 3;
    public static final int SKFE_PREAUTHORIZE_WEBSERVICE = 4;
    public static final int SKFE_PREREGISTER_WEBSERVICE = 5;
    public static final int SKFE_REGISTER_WEBSERVICE = 6;

    // Constants related to parameter labels
    public static final String PARAMETER_FORMPARAMS_LABEL = "formParams=";
    public static final String PARAMETER_PAYLOAD_LABEL = "payload=";
    public static final String PARAMETER_SVCINFO_LABEL = "svcinfo=";

    // Constants related to the Attestation Key
    public static final String ATTESTATION_CONNECTION_TYPE = "TLS";
    public static final String ATTESTATION_KEYSTORE_FILE = "/res/raw/attestation.bks";
    public static final String ATTESTATION_KEYSTORE_PASSWORD = "changeit";
    public static final String ATTESTATION_KEYSTORE_PRIVATEKEY_ALIAS = "mykey";
    public static final String ATTESTATION_KEYSTORE_TYPE = "BKS";

    public static U2FRequestType op;

    private Context mContext;
    private String URL;
    private Header[] headers;
    private HttpEntity entity;
    private String contentType;
    private ResponseHandlerInterface responseHandler;

    // Miscellaneous constants
    public static final int HTTP_SUCCESS = 200;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private final List<RequestHandle> requestHandles = new LinkedList<RequestHandle>();

    public HttpServiceClient(Context context) {
        this.mContext = context;
    }

    public void callFidoWebService(int webServiceType, Resources res, String username, JSONObject response)
            throws U2FException {
        String payload;

        InputStream is = null;
        try {
            String svcinfo = new JSONObject()
                    .put(JSON_KEY_DID_LABEL, "1")
                    .put(JSON_KEY_SVCUSERNAME_LABEL, "svcfidouser")
                    .put(JSON_KEY_SVCPASSWORD_LABEL, "Abcd1234!")
                    .put(JSON_KEY_PROTOCOL_LABEL, "U2F_V2")
                    .toString();
            LogUtils.d(PARAMETER_SVCINFO_LABEL.concat(svcinfo));

            // Build metadata JSON for register webservice
            JSONObject createMetadata = new JSONObject()
                    .put("version", "1.0")
                    .put("create_location", "Cupertino");

            // Build metadata JSON for authenticate and authorize webservices
            JSONObject lastUsedMetadata = new JSONObject()
                    .put("version", "1.0")
                    .put("last_used_location", "Cupertino");

            // Determine which type of webservice we are calling and setup payload
            String fidoserviceurl;
            switch (webServiceType) {
                case SKFE_PREREGISTER_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/preregister");
                    payload = new JSONObject().put(JSON_KEY_USERNAME_LABEL, username).toString();
                    op = U2FRequestType.u2f_register_request;
                    break;
                case SKFE_REGISTER_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/register");
                    payload = new JSONObject().put("response", response).put("metadata", createMetadata).toString();
                    op = U2FRequestType.u2f_register_response;
                    break;
                case SKFE_PREAUTHENTICATE_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/preauthenticate");
                    payload = new JSONObject().put(JSON_KEY_USERNAME_LABEL, username).toString();
                    op = U2FRequestType.u2f_sign_request;
                    break;
                case SKFE_AUTHENTICATE_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/authenticate");
                    payload = new JSONObject().put("response", response).put("metadata", lastUsedMetadata).toString();
                    op = U2FRequestType.u2f_sign_response;
                    break;
                default:
                    throw new RuntimeException("No this webservice call");
            }
            LogUtils.d(PARAMETER_PAYLOAD_LABEL.concat(payload));

            // Bundle svcinfo and payload into a single string for transmission
            String formParams = PARAMETER_SVCINFO_LABEL
                    .concat(svcinfo)
                    .concat("&payload=")
                    .concat(payload);
            LogUtils.d(PARAMETER_FORMPARAMS_LABEL.concat(formParams));

            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            is = res.openRawResource(R.raw.cacerts);
            store.load(is, null);
            asyncHttpClient.setSSLSocketFactory(new SecureSocketFactory(store));
            URL = fidoserviceurl;
            headers = null;
            entity = new StringEntity(formParams);
            contentType = "application/x-www-form-urlencoded";


            addRequestHandle(asyncHttpClient.post(mContext, URL, headers, entity, contentType, responseHandler));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } finally {
            AsyncHttpClient.silentCloseInputStream(is);
        }
    }

    public void addRequestHandle(RequestHandle handle) {
        if (null != handle) {
            requestHandles.add(handle);
        }
    }

    public List<RequestHandle> getRequestHandles() {
        return requestHandles;
    }

    public void setResponseHandler(ResponseHandlerInterface responseHandlerInterface) {
        this.responseHandler = responseHandlerInterface;
    }

    public static final String debugHeaders(Header[] headers) {
        if (headers != null) {
            StringBuilder builder = new StringBuilder();
            for (Header h : headers) {
                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                builder.append(_h);
                builder.append("\n");
            }
            return builder.toString();
        }
        return null;
    }

    public static final String dubugStatusCode(int statusCode) {
        return String.format(Locale.US, "Return Status Code: %d", statusCode);
    }


}
