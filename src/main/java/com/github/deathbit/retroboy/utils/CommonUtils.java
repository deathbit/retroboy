package com.github.deathbit.retroboy.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

public class CommonUtils {

    private static final String border = "+" + "-".repeat(200) + "+";
    private static final int barLength = 100;

    public static void printTask(String mainTaskName, List<String> subTaskNames) {
        System.out.println();
        System.out.println(border);
        System.out.println("| " + mainTaskName);
        System.out.println(border);

        if (subTaskNames != null && !subTaskNames.isEmpty()) {
            for (int i = 0; i < subTaskNames.size(); i++) {
                String index = i + 1 >= 10 ? String.valueOf(i + 1) : " " + (i + 1);
                String line = index + ". " + subTaskNames.get(i);
                System.out.println("| " + line);
            }
        }

        System.out.println(border);
    }

    public static void printTaskDone(String taskName) {
        System.out.println(border);
        System.out.println("| " + taskName + " 完成");
        System.out.println(border);
    }

    public static void configureTrustAllSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing - trust all clients
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Do nothing - trust all servers
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}
