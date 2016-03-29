package org.esec.mcg.androidu2f.curl;

import android.content.res.Resources;
import android.util.Log;

import org.esec.mcg.androidu2f.R;
import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by yz on 2016/3/11.
 */
public class FidoWebService {
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

    // Miscellaneous constants
    public static final int HTTP_SUCCESS = 200;

    public static String callFidoWebService(int webServiceType, Resources res, String username, JSONObject response)
    throws U2FException {
        String payload;

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
                    break;
                case SKFE_REGISTER_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/register");
                    payload = new JSONObject().put("response", response).put("metadata", createMetadata).toString();
                    break;
                case SKFE_PREAUTHENTICATE_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/preauthenticate");
                    payload = new JSONObject().put(JSON_KEY_USERNAME_LABEL, username).toString();
                    break;
                case SKFE_AUTHENTICATE_WEBSERVICE:
                    fidoserviceurl = "https://demo.strongauth.com".concat("/skfe/rest/authenticate");
                    payload = new JSONObject().put("response", response).put("metadata", lastUsedMetadata).toString();
                    break;
                default:
                    return "Invalid webservice called..".concat(": ") + webServiceType;
            }
            LogUtils.d(PARAMETER_PAYLOAD_LABEL.concat(payload));

            // Bundle svcinfo and payload into a single string for transmission
            String formParams = PARAMETER_SVCINFO_LABEL
                    .concat(svcinfo)
                    .concat("&payload=")
                    .concat(payload);
            LogUtils.d(PARAMETER_FORMPARAMS_LABEL.concat(formParams));

            // Setup connection-object to FIDO server
            HttpsURLConnection conn = getConnection(fidoserviceurl, res);
            if (conn == null) {
                return "Failed to connect to SSL server..";
            }

            // Connect and post the message and check response
            conn.setFixedLengthStreamingMode(formParams.getBytes().length);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(formParams.getBytes());
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            if (responseCode != HTTP_SUCCESS) {
                throw new RuntimeException("Failed: HTTP error code:" + responseCode);
            }

            /**
             * Read FIDO server's response.  Can't initialize response to null value
             * because  the Android version of Java seems to convert it to a string
             * ("null"), which shows up in the JSON string and causes parsing issues
             */
            String output;
            String webServerResponse = "";
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                webServerResponse = webServerResponse + output;
            }
            return webServerResponse;
        } catch (IOException
                | CertificateException
                | KeyStoreException
                | NoSuchAlgorithmException
                | KeyManagementException
                | JSONException e) {
            throw new U2FException("Network error.", e);
        }
    }

    private static HttpsURLConnection getConnection (String urlparam, Resources resources)
            throws IOException,
            CertificateException,
            KeyStoreException,
            NoSuchAlgorithmException,
            KeyManagementException {
        // Setup TrustManagerFactory because of self-signed cert
        KeyStore keystore = KeyStore.getInstance(ATTESTATION_KEYSTORE_TYPE);
        keystore.load(resources.openRawResource(R.raw.cacerts), ATTESTATION_KEYSTORE_PASSWORD.toCharArray());
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keystore);
        SSLContext context = SSLContext.getInstance(ATTESTATION_CONNECTION_TYPE);
        context.init(null, tmf.getTrustManagers(), null);

        // Setup connection parameters
        URL url = new URL(urlparam);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(context.getSocketFactory());
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return conn;
    }
}
