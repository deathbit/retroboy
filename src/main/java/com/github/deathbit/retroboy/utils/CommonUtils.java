package com.github.deathbit.retroboy.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.Map;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    private static final String HORIZONTAL_LINE = "+" + "-".repeat(200) + "+";
    private static final int barLength = 100;
    private static final Map<String, Long> TASK_START_TIMES = new ConcurrentHashMap<>();

    public static void printTask(String mainTaskName) {
        TASK_START_TIMES.put(mainTaskName, System.nanoTime());
        System.out.println();
        System.out.println(HORIZONTAL_LINE);
        System.out.println("| " + mainTaskName);
        System.out.println(HORIZONTAL_LINE);
    }

    public static void printTaskDone(String taskName) {
        System.out.println(HORIZONTAL_LINE);
        System.out.println("| " + taskName + " 完成 - 耗时: " + elapsedTime(taskName));
        System.out.println(HORIZONTAL_LINE);
    }

    private static String elapsedTime(String taskName) {
        Long startTime = TASK_START_TIMES.remove(taskName);
        if (startTime == null) {
            return "0分0秒";
        }

        long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
        return "%d分%d秒".formatted(elapsedSeconds / 60, elapsedSeconds % 60);
    }

    public static void printAsciiArt() {
        System.out.println(HORIZONTAL_LINE);
        System.out.print("""
                ▗▄▄▖ ▗▄▄▄▖▗▄▄▄▖▗▄▄▖  ▗▄▖ ▗▄▄▖  ▗▄▖▗▖  ▗▖
                ▐▌ ▐▌▐▌     █  ▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▐▌ ▐▌▝▚▞▘\s
                ▐▛▀▚▖▐▛▀▀▘  █  ▐▛▀▚▖▐▌ ▐▌▐▛▀▚▖▐▌ ▐▌ ▐▌ \s
                ▐▌ ▐▌▐▙▄▄▖  █  ▐▌ ▐▌▝▚▄▞▘▐▙▄▞▘▝▚▄▞▘ ▐▌  by deathbit\s
                """);
        System.out.println(HORIZONTAL_LINE);
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
