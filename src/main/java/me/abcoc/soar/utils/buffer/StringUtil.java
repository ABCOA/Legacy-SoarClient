package me.abcoc.soar.utils.buffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static String ExtractString(String input, String start, String end) throws IllegalArgumentException {
        String regex = start + "(.*?)" + end;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("No match found");
        }
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    public static String cookieToString(Collection<String> cookies) {
        Iterator<String> it = cookies.iterator();
        if (! it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        // sb.append('[');
        for (;;) {
            String e = it.next();
            sb.append(e);
            if (! it.hasNext())
                return sb.toString();
            sb.append(';').append(' ');
        }
    }
}
