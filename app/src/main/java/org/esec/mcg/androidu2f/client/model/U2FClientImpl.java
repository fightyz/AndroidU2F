package org.esec.mcg.androidu2f.client.model;

import android.content.pm.PackageInfo;
import android.util.Base64;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2fsimulator.token.msg.AuthenticationRequest;
import org.esec.mcg.androidu2fsimulator.token.msg.RegistrationRequest;
import org.esec.mcg.androidu2f.codec.ClientDataCodec;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * Created by yz on 2016/1/14.
 */
public class U2FClientImpl extends U2FClient {

    public static final byte CHECK_ONLY = 0x07;
    public static final byte USER_PRESENCE_SIGN = 0x03;

    private static final String U2F_V2 = "U2F_V2";

//    private U2FToken u2fToken;

    private String version;
    private String appId;
    private String serverChallengeBase64;
    private String facetID;

    private JSONArray signBatch;

    /**
     * Caller of the U2FClientActivity
     */
    private PackageInfo packageInfo;
    private Crypto crypto;

    /**
     * Implementaion of U2FClient.
     * @param packageInfo Caller's packageInfo.
     */
    public U2FClientImpl(PackageInfo packageInfo) {
//        this.u2fToken = u2fToken;
        this.packageInfo = packageInfo;
        crypto = new CryptoImpl();
    }

    /**
     * Decode U2F request message and generate raw message.
     * @param u2fProtocolMessage
     * @return
     * @throws U2FException
     */
    @Override
    public RegistrationRequest register(String u2fProtocolMessage) throws U2FException {
        try {
            JSONObject reg = new JSONObject(u2fProtocolMessage);

            version = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("version");
            appId = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("appId");
            serverChallengeBase64 = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("challenge");

            // Check the u2f version
            if (!version.equals(U2F_V2)) {
                throw new U2FException(String.format("Unsupported protocol version: %s", version));
            }

            // TODO: 2016/3/21 The facetID should be the orign, and it should be in appid's json.
            facetID = getFacetID(packageInfo);
            verifyAppId(appId);

            // TODO: 2016/3/11 Actually the "faceID" should be the origin. Change it.
            clientData = ClientDataCodec.encodeClientData(ClientDataCodec.REQUEST_TYPE_REGISTER, serverChallengeBase64, facetID, null);
            LogUtils.d("client data: " + clientData);

            //TODO Actually, application parameter should be "SHA-256 hash of the application identity of the application requesting the registration"
            byte[] appIdSha256 = crypto.computeSha256(appId);
            byte[] clientDataSha256 = crypto.computeSha256(clientData);

            LogUtils.d(ByteUtil.ByteArrayToHexString(appIdSha256));
            LogUtils.d(ByteUtil.ByteArrayToHexString(clientDataSha256));

            return new RegistrationRequest(appIdSha256, clientDataSha256);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new U2FException("Register request JSON format is wrong.", e);
        } catch (CertificateException e) {
            throw new U2FException("Can not get the caller's apk signature(facet ID).", e);
        } catch (NoSuchAlgorithmException e) {
            throw new U2FException("Can not get the caller's apk signature(facet ID).", e);
        }
    }

    /**
     * Decode U2F request message and generate raw message.
     * @param signRequest
     * @return
     * @throws U2FException
     */
    public AuthenticationRequest sign(JSONObject signRequest, boolean isSign) throws U2FException {
        try {
            version = signRequest.getString("version");
            appId = signRequest.getString("appId");
            serverChallengeBase64 = signRequest.getString("challenge");
            keyHandle = signRequest.getString("keyHandle");

            // Check the u2f version
            if (!version.equals(U2F_V2)) {
                throw new U2FException(String.format("Unsupported protocol version: %s", version));
            }

            facetID = getFacetID(packageInfo);
            verifyAppId(appId);

            clientData = ClientDataCodec.encodeClientData(ClientDataCodec.REQUEST_TYPE_AUTHENTICATE, serverChallengeBase64, facetID, null);
            //TODO Actually, application parameter should be "SHA-256 hash of the application identity of the application requesting the registration"
            byte[] appIdSha256 = crypto.computeSha256(appId);
            byte[] clientDataSha256 = crypto.computeSha256(clientData);

            byte control;
            if (isSign) {
                control = USER_PRESENCE_SIGN;
            } else {
                control = CHECK_ONLY;
            }

            byte[] rawKeyHandle = android.util.Base64.decode(keyHandle, Base64.URL_SAFE);
            return new AuthenticationRequest(control, clientDataSha256, appIdSha256, rawKeyHandle);
        } catch (JSONException e) {
            throw new U2FException("Rgister request JSON format is wrong.", e);
        } catch (CertificateException e) {
            throw new U2FException("Can not get the caller's apk signature(facet ID).", e);
        } catch (NoSuchAlgorithmException e) {
            throw new U2FException("Can not get the caller's apk signature(facet ID).", e);
        } catch (IllegalArgumentException e) {
            throw new U2FException("Bad Base64 encoding of key Handle.", e);
        }
    }

    @Override
    public AuthenticationRequest[] signBatch(JSONObject signRequests, boolean sign) throws U2FException {
        try {
            if (signRequests.has("signRequests")) {
                signBatch = signRequests.getJSONArray("signRequests");
            }
            if (signBatch.length() == 0) {
                return null;
            }
            AuthenticationRequest[] authenticationRequestsBatch = new AuthenticationRequest[signBatch.length()];
            for (int i = 0; i < signBatch.length(); i++) {
                authenticationRequestsBatch[i] = sign(signBatch.getJSONObject(i), sign);
                LogUtils.d("authenticationRequestsBatch: " + authenticationRequestsBatch[i]);
            }
            return authenticationRequestsBatch;
        } catch (JSONException e) {
            throw new U2FException("Rgister request JSON format is wrong.", e);
        }

    }

    @Override
    public String getClientDataForIndex(int index) {
        try {
            return ClientDataCodec.encodeClientData(ClientDataCodec.REQUEST_TYPE_AUTHENTICATE,
                    signBatch.getJSONObject(index).getString("challenge"), facetID, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getKeyHandle(int index) {
        try {
            return signBatch.getJSONObject(index).getString("keyHandle");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO Implement this function
    /**
     * @param appId
     * @return
     */
    private boolean verifyAppId(String appId) {
//        LogUtils.d(appId);
        return true;
    }

    private String getFacetID(PackageInfo paramPackageInfo)
            throws CertificateException, NoSuchAlgorithmException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paramPackageInfo.signatures[0].toByteArray());
        Certificate certificate = CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        String facetID = "android:apk-key-hash:" + Base64.encodeToString(messageDigest.digest(certificate.getEncoded()), Base64.DEFAULT | Base64.NO_WRAP);
//        LogUtils.d("android:apk-key-hash:" + Base64.encodeToString(messageDigest.digest(certificate.getEncoded()), Base64.DEFAULT | Base64.NO_WRAP));
        facetID = "https://demo.strongauth.com";
        return facetID;
    }
}
