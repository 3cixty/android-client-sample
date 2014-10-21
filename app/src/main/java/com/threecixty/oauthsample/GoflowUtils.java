/*
 * Copyright 2014 EIT ICT Labs project 3cixty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.threecixty.oauthsample;

import android.content.Context;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class is to retrieve a GoFlow account from 3Cixty platform for sake of simplicity.
 * In reality, you should persist the GoFlow account and read it locally for performance.
 */
public class GoflowUtils {


    /**
     * Gets a GoFlow account.
     * <br />
     * This method needs to be called in a asynchronous task.
     * @param _3cixtyAccessToken
     * @return
     */
    public static GoflowAccount getGoflowAccount(String _3cixtyAccessToken) {
        try {
            if (_3cixtyAccessToken == null) return null;

            URL url = new URL("https://api.3cixty.com/v2/createOrRetrieveGoFlowUser");
            trustAllHosts();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("access_token", _3cixtyAccessToken);

            InputStream input = null;
            try {
                input = con.getInputStream();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                input = con.getErrorStream();
            }

            StringBuffer buffer = new StringBuffer();
            byte[] b = new byte[1024];
            int readBytes = 0;

            while ((readBytes = input.read(b)) >= 0) {
                buffer.append(new String(b, 0, readBytes));
            }
            input.close();

            JSONObject jsonObj = new JSONObject(buffer.toString());

            return new GoflowAccount(jsonObj.getString("username"), jsonObj.getString("password"), jsonObj.getString("appid"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void trustAllHosts()
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }
        } };

        // Install the all-trusting trust manager
        try
        {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private GoflowUtils() {
    }

    public static class GoflowAccount {
        private String username;
        private String password;
        private String appid;

        public GoflowAccount(String username, String password, String appid) {
            this.username = username;
            this.password = password;
            this.appid = appid;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getAppid() {
            return appid;
        }
    }
}
