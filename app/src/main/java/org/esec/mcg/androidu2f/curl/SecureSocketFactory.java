package org.esec.mcg.androidu2f.curl;

import android.os.Build;
import android.util.Log;

import org.esec.mcg.utils.logger.LogUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

/**
 * Created by YangZhou on 2016/4/4.
 */
public class SecureSocketFactory extends SSLSocketFactory {
    private static final String LOG_TAG = "SecureSocketFactory";

    private final SSLContext sslCtx;

    public SecureSocketFactory(KeyStore store)
        throws
        CertificateException,
            NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            UnrecoverableKeyException {
        super(store);

        sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null,
                new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                Exception error = null;

                                if (null == chain || 0 == chain.length) {
                                    error = new CertificateException("Certificate chain is invalid.");
                                } else if (null == authType || 0 == authType.length()) {
                                    error = new CertificateException("Authentication type is invalid.");
                                } else {
                                    LogUtils.d( "Chain inclues " + chain.length + " certificates.");
                                    for (X509Certificate cert : chain) {
                                        Log.i(LOG_TAG, "Server Certificate Details:");
                                        Log.i(LOG_TAG, "---------------------------");
                                        Log.i(LOG_TAG, "IssuerDN: " + cert.getIssuerDN().toString());
                                        Log.i(LOG_TAG, "SubjectDN: " + cert.getSubjectDN().toString());
                                        Log.i(LOG_TAG, "Serial Number: " + cert.getSerialNumber());
                                        Log.i(LOG_TAG, "Version: " + cert.getVersion());
                                        Log.i(LOG_TAG, "Not before: " + cert.getNotBefore().toString());
                                        Log.i(LOG_TAG, "Not after: " + cert.getNotAfter().toString());
                                        Log.i(LOG_TAG, "---------------------------");

                                        // Make sure that it hasn't expired
                                        cert.checkValidity();
                                    }
                                }
                                if (null != error) {
                                    Log.e(LOG_TAG, "Certificate error", error);
                                    throw new CertificateException(error);
                                }
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        }
                },
                null);

        setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {
        injectHostname(socket, host);
        Socket sslSocket = sslCtx.getSocketFactory().createSocket(socket, host, port, autoClose);

        // throw an exception if the hostname does not match the certificate
        getHostnameVerifier().verify(host, (SSLSocket) sslSocket);

        return sslSocket;
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslCtx.getSocketFactory().createSocket();
    }

    /**
     * Pre-ICS Android had a bug resolving HTTPS addresses. This workaround fixes that bug.
     *
     * @param socket The socket to alter
     * @param host   Hostname to connect to
     * @see <a href="https://code.google.com/p/android/issues/detail?id=13117#c14">https://code.google.com/p/android/issues/detail?id=13117#c14</a>
     */
    private void injectHostname(Socket socket, String host) {
        try {
            if (Integer.valueOf(Build.VERSION.SDK) >= 4) {
                Field field = InetAddress.class.getDeclaredField("hostName");
                field.setAccessible(true);
                field.set(socket.getInetAddress(), host);
            }
        } catch (Exception ignored) {
        }
    }
}
