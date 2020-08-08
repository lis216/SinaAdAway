package me.lee.adaway.sina.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    public static String getPageContent(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.connect();
            StringBuilder builder = new StringBuilder();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    int len;
                    char buf[] = new char[4096];
                    while ((len = br.read(buf)) != -1) {
                        builder.append(buf, 0, len);
                    }
                }
            }
            conn.disconnect();
            return builder.toString();
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }

    }
}
