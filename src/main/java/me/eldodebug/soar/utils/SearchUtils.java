package me.eldodebug.soar.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class SearchUtils {
	
	public static boolean isSimillar(String s1, String s2) {
		return isSimillar(s1, s2, 1);
	}
	
	public static boolean isSimillar(String s1, String s2, int searchDistance) {
		
        s1 = s1.toLowerCase(Locale.ENGLISH);
        s2 = s2.toLowerCase(Locale.ENGLISH);
        
        if (s1.length() <= searchDistance) {
            return s1.contains(s2);
        }
        
        boolean similar = false;
        
        for (String a : StringUtils.split(s1)) {
            similar = a.contains(s2) || StringUtils.getLevenshteinDistance(a, s2) <= searchDistance;
            if (similar) {
            	break;
            }
        }
        
        return similar || s1.contains(s2) || StringUtils.getLevenshteinDistance(s1, s2) <= searchDistance;
	}
}
