package me.eldodebug.soar.utils;

import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

	public static boolean hasProperty(JsonObject jsonObject, String key) {
		return getProperty(jsonObject, key) != null;
	}
	
	public static String getStringProperty(JsonObject jsonObject, String key, String defaultValue) {
		
		JsonElement value = getProperty(jsonObject, key);
		
		if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
			return defaultValue;
		}
		
		return value.getAsJsonPrimitive().getAsString();
	}
	
	public static float getFloatProperty(JsonObject jsonObject, String key, float defaultValue) {
		return getNumberProperty(jsonObject, key, defaultValue).floatValue();
	}
	
	public static double getDoubleProperty(JsonObject jsonObject, String key, double defaultValue) {
		return getNumberProperty(jsonObject, key, defaultValue).doubleValue();
	}
	
	public static long getLongProperty(JsonObject jsonObject, String key, long defaultValue) {
		return getNumberProperty(jsonObject, key, defaultValue).longValue();
	}
	
	public static int getIntProperty(JsonObject jsonObject, String key, int defaultValue) {
		return getNumberProperty(jsonObject, key, defaultValue).intValue();
	}
	
	public static Number getNumberProperty(JsonObject jsonObject, String key, Number defaultValue) {
		
		JsonElement value = getProperty(jsonObject, key);
		
		if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
			return defaultValue;
		}
		
		return value.getAsJsonPrimitive().getAsNumber();
	}
	
	public static boolean getBooleanProperty(JsonObject jsonObject, String key, boolean defaultValue) {
		
		JsonElement value = getProperty(jsonObject, key);
		
		if(value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
			return defaultValue;
		}
		
		return value.getAsJsonPrimitive().getAsBoolean();
	}
	
	public static JsonArray getArrayProperty(JsonObject jsonObject, String key) {
		
		JsonElement result = getProperty(jsonObject, key);
		
		if(result == null || !result.isJsonArray()) {
			return new JsonArray();
		}
		
		return result.getAsJsonArray();
	}
	
	public static JsonObject getObjectProperty(JsonObject jsonObject, String key) {
		
		JsonElement result = getProperty(jsonObject, key);
		
		if(result == null || !result.isJsonObject()) {
			return null;
		}
		
		return result.getAsJsonObject();
	}
	
	public static JsonElement getProperty(JsonObject jsonObject, String key) {
		
		if(key == null) {
			throw new IllegalArgumentException("Property key cannot be null");
		}else if(key.isEmpty()) {
			return jsonObject;
		}
		
        String[] tokens = tokenizeKey(key);
        JsonObject parent = jsonObject;
        
        for (int i = 0; i < tokens.length; i++) {

            JsonElement child = parent.get(tokens[i].replace("\\.", ","));
            
            if (i + 1 == tokens.length) {
                return child;
            }

            if (child instanceof JsonObject) {
                parent = child.getAsJsonObject();
                continue;
            }
            break;
        }

        return null;
	}
	
    private static String[] tokenizeKey(String key) {
        return Pattern.compile("(?<!\\\\)\\,").split(key);
    }
}
