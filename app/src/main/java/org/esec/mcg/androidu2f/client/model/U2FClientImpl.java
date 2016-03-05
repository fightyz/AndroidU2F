package org.esec.mcg.androidu2f.client.model;

import android.content.pm.PackageInfo;
import android.util.Base64;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.client.U2FClientActivity;
import org.esec.mcg.androidu2f.codec.ClientDataCodec;
import org.esec.mcg.androidu2f.token.U2FToken;
import org.esec.mcg.androidu2f.token.msg.RegisterRequest;
import org.esec.mcg.utils.ByteUtil;
import org.esec.mcg.utils.logger.LogUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Created by yz on 2016/1/14.
 */
public class U2FClientImpl extends U2FClient {

    private static final String U2F_V2 = "U2F_V2";

//    private U2FToken u2fToken;

    private String version;
    private String appId;
    private String serverChallengeBase64;
    private String facetID;
//    private String clientData;

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
    public RegisterRequest register(String u2fProtocolMessage) throws U2FException {
//        LogUtils.d(u2fProtocolMessage);
        try {
            JSONObject reg = new JSONObject(u2fProtocolMessage);
            version = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("version");
            appId = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("appId");
            serverChallengeBase64 = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("challenge");

            // Check the u2f version
            if (!version.equals(U2F_V2)) {
                throw new U2FException(String.format("Unsupported protocol version: %s", version));
            }

            facetID = getFacetID(packageInfo);
            verifyAppId(appId);

            clientData = ClientDataCodec.encodeClientData(ClientDataCodec.REQUEST_TYPE_REGISTER, serverChallengeBase64, facetID, null);
            LogUtils.d("client data: " + clientData);

            //TODO Actually, application parameter should be "SHA-256 hash of the application identity of the application requesting the registration"
            byte[] appIdSha256 = crypto.computeSha256(appId);
            byte[] clientDataSha256 = crypto.computeSha256(clientData);

            LogUtils.d(ByteUtil.ByteArrayToHexString(appIdSha256));
            LogUtils.d(ByteUtil.ByteArrayToHexString(clientDataSha256));

            return new RegisterRequest(appIdSha256, clientDataSha256);
        } catch (JSONException e) {
            throw new U2FException("Rgister request JSON format is wrong.", e);
        }
    }

    //TODO Implement this function
    /**
     * @param appId
     * @return
     */
    private boolean verifyAppId(String appId) {
        return true;
    }

    private String getFacetID(PackageInfo paramPackageInfo) throws U2FException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paramPackageInfo.signatures[0].toByteArray());
            Certificate certificate = CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            String facetID = "android:apk-key-hash:" + Base64.encodeToString(messageDigest.digest(certificate.getEncoded()), Base64.DEFAULT);
            LogUtils.d("android:apk-key-hash:" + Base64.encodeToString(messageDigest.digest(certificate.getEncoded()), Base64.DEFAULT));
            return facetID;
        } catch (Exception e) {
            throw new U2FException("Can't get caller's facetID.", e);
        }
//        return null;
    }

    public String getClientData() {
        return clientData;
    }
}
