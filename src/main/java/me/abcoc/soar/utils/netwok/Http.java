package me.abcoc.soar.utils.netwok;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Http class for JDK 1.8
 */
public class Http {

    public static Gson gson() {
        return new GsonBuilder().disableHtmlEscaping().create();
    }

    public static String buildParam(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            try {
                stringBuilder
                        .append(stringStringEntry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(stringStringEntry.getValue(), "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return stringBuilder.toString();
    }

    public static String buildUrl(String url, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder(url);
        if (!map.isEmpty()) {
            stringBuilder.append("?");
            stringBuilder.append(buildParam(map));
        }
        return stringBuilder.toString();
    }

    public static String send(String url) throws IOException, URISyntaxException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("GET request not worked");
        }
    }

    public static String postURL(String url, Map<String, String> data) throws IOException, URISyntaxException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(buildParam(data).getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("POST request not worked");
        }
    }

    public static String postJSON(String url, Map<String, Object> data) throws IOException, URISyntaxException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(gson().toJson(data).getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new RuntimeException("POST request not worked");
        }
    }

    public static String get(String urlString, Map<String, Object> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue().toString());
            }
        }

        int responseCode = connection.getResponseCode();
        System.out.println("GET Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            System.out.println("GET request not worked");
        }
        return null;
    }
}