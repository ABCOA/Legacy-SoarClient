package me.abcoc.soar.utils.netwok;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.eldodebug.soar.logger.SoarLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MinecraftUUIDFetcher {
    public static String getUUID(String id) {
        String urlStr = "https://api.mojang.com/users/profiles/minecraft/" + id;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = bf.readLine()) != null) {
                    response.append(inputLine);
                }
                bf.close();
                JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
                return jsonObject.get("id").getAsString();
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                SoarLogger.error("Failed to fetch UUID for " + id + " because the player does not exist.");
            } else {
                SoarLogger.error("Failed to fetch UUID for " + id + " because of an unknown error.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkNameUUID(String id, String UUID) {
        String urlStr = "https://api.mojang.com/users/profiles/minecraft/" + id;
        boolean result = false;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = bf.readLine()) != null) {
                    response.append(inputLine);
                }
                bf.close();
                JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
                result = jsonObject.get("id").getAsString().equals(UUID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
