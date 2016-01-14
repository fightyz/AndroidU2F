package org.esec.mcg.androidu2f.client.model;

import android.content.pm.PackageInfo;
import android.util.Base64;

import org.esec.mcg.androidu2f.U2FException;
import org.esec.mcg.androidu2f.client.U2FClientActivity;
import org.esec.mcg.androidu2f.codec.ClientDataCodec;
import org.esec.mcg.androidu2f.token.U2FToken;
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
public class U2FClientImpl implements U2FClient {

    private static final String U2F_V2 = "U2F_V2";

    private U2FToken u2fToken;

    private String version;
    private String appId;
    private String serverChallengeBase64;
    private String facetID;
    private PackageInfo packageInfo;

    public U2FClientImpl(U2FToken u2fToken, PackageInfo packageInfo) {
        this.u2fToken = u2fToken;
        this.packageInfo = packageInfo;
    }

    @Override
    public void register(String u2fProtocolMessage) throws U2FException {
        LogUtils.d(u2fProtocolMessage);
        try {
            JSONObject reg = new JSONObject(u2fProtocolMessage);
            version = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("version");
            appId = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("appId");
            serverChallengeBase64 = ((JSONObject)reg.getJSONArray("registerRequests").get(0)).getString("challenge");

            if (!version.equals(U2F_V2)) {
                throw new U2FException(String.format("Unsupported protocol version: %s", version));
            }

            verifyAppId(appId);

            facetID = getFacetID(packageInfo);
            String clientData = ClientDataCodec.encodeClientData(ClientDataCodec.REQUEST_TYPE_REGISTER, serverChallengeBase64, facetID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void verifyAppId(String appId) {

    }

    private String getFacetID(PackageInfo paramPackageInfo) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paramPackageInfo.signatures[0].toByteArray());
            Certificate certificate = CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            String facetID = "android:apk-key-hash:" + Base64.encodeToString(messageDigest.digest(certificate.getEncoded()), Base64.DEFAULT);
            return facetID;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
