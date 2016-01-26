package org.esec.mcg.androidu2f.client.curl;

import android.os.AsyncTask;
import android.preference.PreferenceActivity;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.esec.mcg.utils.logger.LogUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HostnameVerifier;

/**
 * Created by yangzhou on 1/24/16.
 */
public class Curl {

    public static void getInSeperateThread(String url) {
        GetAsyncTask asyncTask = new GetAsyncTask();
        asyncTask.execute(url);
    }

    public static void postInSeperateThread(String url, String header, String data) {
        PostAsyncTask asyncTask = new PostAsyncTask();
        asyncTask.execute(url, header, data);
    }

    public static String toStr(HttpResponse response) {
        String result = "";
        try {
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + '\n');
            }
            in.close();
            result = str.toString();
        } catch (Exception ex) {
            result = "Error";
        }
        return result;
    }

    public static String get(String url) {
        return get(url, null);
    }

    public static String get(String url, String[] header) {
        String ret = "";
        try {
            HttpClient httpClient = getClient(url);
            HttpGet request = new HttpGet(url);

            try {
                if (header != null) {
                    for (String h : header) {
                        String[] split = h.split(":");
                        request.addHeader(split[0], split[1]);
                    }
                }
                HttpResponse response = httpClient.execute(request);
                ret = Curl.toStr(response);
                Header[] headers = response.getAllHeaders();
            } catch (Exception ex) {
                ex.printStackTrace();
                ret = "{'error_code':'connect_fail','url':'" + url + "'}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = "{'error_code':'connect_fail','e':'" + e + "'}";
        }

        return ret;
    }

    public static String post(String url, String header, String data) {
        return post(url, header.split(" "), data);
    }

    public static String post(String url, String[] header, String data) {
        String ret = "";
        try {
            HttpClient httpClient = getClient(url);

            HttpPost request = new HttpPost(url);
            if (header != null) {
                for (String h : header) {
                    String[] split  = h.split(":");
                    request.addHeader(split[0], split[1]);
                }
            }

            request.setEntity(new StringEntity(data));
            try {
                HttpResponse response = httpClient.execute(request);
                ret = Curl.toStr(response);
                Header[] headers = response.getAllHeaders();
            } catch (Exception ex) {
                ex.printStackTrace();
                ret = "{'error_code':'connect_fail','url':'" + url + "'}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = "{'error_code':'connect_fail','e':'" + e + "'}";
        }
        return ret;
    }

    private static HttpClient getClient(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        if (url.toLowerCase().startsWith("https")) {
            httpClient = createHttpsClient();
        }
        return httpClient;
    }

    private static HttpClient createHttpsClient() {
        HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 443));
        HttpClient client = new DefaultHttpClient();
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
        return httpClient;
    }
}

class GetAsyncTask extends AsyncTask<String, Integer, String> {

    private String result = null;
    private boolean done = false;
    public boolean isDone() { return done; }
    public String getResult() { return result; }

    @Override
    protected String doInBackground(String... params) {
        result = Curl.get(params[0]);
        done = true;
        return result;
    }

    protected void onPostExecute(String result) {
        this.result = result;
        done = true;
    }
}

class PostAsyncTask extends AsyncTask<String, Integer, String> {
    private String result = null;
    private boolean done = false;
    public boolean isDone() { return done; }
    public String getResult() { return result; }

    @Override
    protected String doInBackground(String... params) {
        result = Curl.post(params[0], params[1], params[2]); // (url, header, data)
        done = true;
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        this.result = result;
        done = true;
        LogUtils.d("response: " + result);
    }
}
