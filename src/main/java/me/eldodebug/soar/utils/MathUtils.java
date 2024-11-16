package me.eldodebug.soar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

    public static boolean isOdd(int number) {
        return number % 2 != 0;
    }
    
    public static float clamp(float value) {
        return (double) value < 0.0D ? 0.0F : ((double) value > 1.0D ? 1.0F : value);
    }
    
    public static float clamp(float number, float min, float max) {
        return number < min ? min : Math.min(number, max);
    }
    
    public static Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }
    
    public static boolean isInRange(float value, float min, float max) {
    	return value > min && value < max;
    }
    
    public static float sin(double value) {
    	return (float) Math.sin(value);
    }
    
    public static float cos(double value) {
    	return (float) Math.cos(value);
    }
    
    public static float lerp(float f, float g, float h) {
        return g + f * (h - g);
    }

    public static double lerp(double d, double e, double f) {
        return e + d * (f - e);
    }
    
    public static float fastInvSqrt(float f) {
        float g = 0.5F * f;
        int i = Float.floatToIntBits(f);
        i = 1597463007 - (i >> 1);
        f = Float.intBitsToFloat(i);
        f *= 1.5F - g * f * f;
        return f;
    }
    
    public static float fastInvCubeRoot(float f) {
        int i = Float.floatToIntBits(f);
        i = 1419967116 - i / 3;
        float g = Float.intBitsToFloat(i);
        g = 0.6666667F * g + 1.0F / 3.0F * g * g * f;
        g = 0.6666667F * g + 1.0F / 3.0F * g * g * f;
        return g;
    }
    
    public static double roundToPlace(double value, int places) {
    	
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static double abs(double value) {
        return value >= 0.0F ? value : -value;
    }
}
