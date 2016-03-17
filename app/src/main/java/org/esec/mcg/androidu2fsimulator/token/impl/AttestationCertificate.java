package org.esec.mcg.androidu2fsimulator.token.impl;

import org.esec.mcg.androidu2fsimulator.token.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by yz on 2016/1/22.
 */
class AttestationCertificate {
    private static final String ATTESTATION_KEYSTORE_PASSWORD = "123456";
//    private static final String ATTESTATION_KEYSTORE_FILE = "/res/raw/attestation.bks";
    private static final String ATTESTATION_KEYSTORE_FILE = "/res/raw/attestationcert.bks";
    private static final String ATTESTATION_KEYSTORE_PRIVATEKEY_ALIAS = "mykey";

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private static final byte[] attestationCertificateByteArray = StringUtil.HexStringToByteArray("308201123081B90204075BCD15300A06082A8648CE3D04030230133111300F06035504031308553246546F6B656E3020170D3135303230313131303030305A180F32303530303230313131303030305A30133111300F06035504031308553246546F6B656E3059301306072A8648CE3D020106082A8648CE3D030107034200046D35E326E148859E2663E077331A6B4C9986712F4E7E35DABECC34FF9BB603B54CEC8C322DD36A112C41440DB80074BDE2A10F99746DBBAD77F2CD2949D5B76E300A06082A8648CE3D040302034800304502207733188ACF10F2BFD9B885A262E581FCDC08926C2A1F75554C48DC9B83AB1803022100F2A8A861B4E1954144314B303D7A405CC7CE6F847BC83BE42DD55E5FBDF0C0BD");
    private static final byte[] attestationPrivateKeyByteArray = StringUtil.HexStringToByteArray("3082024B0201003081EC06072A8648CE3D02013081E0020101302C06072A8648CE3D0101022100FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF30440420FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC04205AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B0441046B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C2964FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5022100FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC6325510201010482015530820151020101042054B03CEA3594D23A4EBA0F88227A1588E84EC711571FABD0B7EB15FC5469ABE3A081E33081E0020101302C06072A8648CE3D0101022100FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF30440420FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC04205AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B0441046B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C2964FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5022100FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551020101A144034200046D35E326E148859E2663E077331A6B4C9986712F4E7E35DABECC34FF9BB603B54CEC8C322DD36A112C41440DB80074BDE2A10F99746DBBAD77F2CD2949D5B76E");

    static Certificate getAttestationCertificate() {
//        Certificate certificate = null;
//        CertificateFactory certificateFactory = null;
//        try {
//            certificateFactory = CertificateFactory.getInstance("X.509");
//            InputStream inputStream = new ByteArrayInputStream(attestationCertificateByteArray);
//            certificate = certificateFactory.generateCertificate(inputStream);
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//
//        return certificate;
        KeyStore attks = null;
        try {
            attks = KeyStore.getInstance("BKS");
            char[] kspassword = ATTESTATION_KEYSTORE_PASSWORD.toCharArray();
            attks.load(AttestationCertificate.class.getResourceAsStream(ATTESTATION_KEYSTORE_FILE), kspassword);
            return attks.getCertificate(ATTESTATION_KEYSTORE_PRIVATEKEY_ALIAS);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static PrivateKey getAttestationPrivateKey() {
//        PrivateKey privateKey = null;
//        KeyFactory keyFactory = null;
//        try {
//            keyFactory = KeyFactory.getInstance("ECDSA", "SC");
//            PKCS8EncodedKeySpec encodedPrivateKey = new PKCS8EncodedKeySpec(attestationPrivateKeyByteArray);
//            privateKey = keyFactory.generatePrivate(encodedPrivateKey);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//
//        return privateKey;
        KeyStore attks = null;
        try {
            attks = KeyStore.getInstance("BKS");
            char[] kspassword = ATTESTATION_KEYSTORE_PASSWORD.toCharArray();
            attks.load(AttestationCertificate.class.getResourceAsStream(ATTESTATION_KEYSTORE_FILE), kspassword);
            return (PrivateKey)attks.getKey(ATTESTATION_KEYSTORE_PRIVATEKEY_ALIAS, kspassword);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
