package org.esec.mcg.utils;

import android.util.Log;

import org.esec.mcg.utils.logger.LogUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by yz on 2016/1/11.
 */
public class HTTP {
    public static String post(URL url, Map<String,String> params) throws IOException {
        HttpURLConnection connection = null;

        try {
            StringBuilder urlParametersBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlParametersBuilder.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            urlParametersBuilder.deleteCharAt(0);
            String urlParameters = urlParametersBuilder.toString();
            LogUtils.d(urlParameters);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
