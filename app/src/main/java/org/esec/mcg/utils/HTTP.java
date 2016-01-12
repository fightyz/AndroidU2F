package org.esec.mcg.utils;

import android.util.Log;

import org.esec.mcg.utils.logger.LogUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
    public static String post(URL url, Map<String,String> params) {
        HttpURLConnection connection = null;

        try {
            StringBuilder urlParametersBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlParametersBuilder.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            urlParametersBuilder.deleteCharAt(0);
            String urlParameters = urlParametersBuilder.toString();
            LogUtils.d(urlParameters);

            LogUtils.d("open");
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
            LogUtils.d("opened");

            //Get Response
            InputStream is = connection.getInputStream();
            LogUtils.d("getInputStream");
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }  catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(connection != null) {
                LogUtils.d("post finally");
                connection.disconnect();
            }
        }
        return null;
    }

    public static String get(URL url) {
        HttpURLConnection connection = null;

        try {
            LogUtils.d("open");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            LogUtils.d("opened");
            int responseCode = connection.getResponseCode();
            LogUtils.d("getResponseCode "+responseCode);
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                String state = getStringFromInputString(is);
                return state;
            } else {
                LogUtils.d("GET failed, " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(connection != null) {
                LogUtils.d("get finally");
                connection.disconnect();
            }
        }

        return null;
    }

    private static String getStringFromInputString(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 模板代码 必须熟练
        byte[] buffer = new byte[1024];
        int len = -1;
        // 一定要写len=is.read(buffer)
        // 如果while((is.read(buffer))!=-1)则无法将数据写入buffer中
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
        os.close();
        return state;
    }
}
