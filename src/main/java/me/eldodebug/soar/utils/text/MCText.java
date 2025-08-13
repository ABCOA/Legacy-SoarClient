package me.eldodebug.soar.utils.text;

import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class MCText {

    private static final char SEC = '\u00A7';

    private static final Map<Character, Color> MC_COLOR = new HashMap<Character, Color>() {{
        put('0', new Color(0x000000));
        put('1', new Color(0x0000AA));
        put('2', new Color(0x00AA00));
        put('3', new Color(0x00AAAA));
        put('4', new Color(0xAA0000));
        put('5', new Color(0xAA00AA));
        put('6', new Color(0xFFAA00));
        put('7', new Color(0xAAAAAA));
        put('8', new Color(0x555555));
        put('9', new Color(0x5555FF));
        put('a', new Color(0x55FF55));
        put('b', new Color(0x55FFFF));
        put('c', new Color(0xFF5555));
        put('d', new Color(0xFF55FF));
        put('e', new Color(0xFFFF55));
        put('f', new Color(0xFFFFFF));
    }};

    private static final Random OBFUSCATE_RNG = new Random();

    private MCText() {}

    public static float draw(NanoVGManager nvg, String text, float x, float y,
                             float size, Color defaultColor, boolean shadow) {
        if (text == null || text.isEmpty()) return 0f;

        Color color = defaultColor != null ? defaultColor : Color.WHITE;
        boolean bold = false, italic = false, underline = false, strike = false, obf = false;

        float penX = x;
        int i = 0;

        while (i < text.length()) {
            char ch = text.charAt(i);

            if (ch == SEC && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                Color mapped = MC_COLOR.get(code);
                if (mapped != null) {
                    color = mapped;
                    bold = italic = underline = strike = obf = false;
                    i += 2;
                    continue;
                }
                switch (code) {
                    case 'l': bold = true;         i += 2; continue; // 粗体
                    case 'n': underline = true;    i += 2; continue; // 下划线
                    case 'm': strike = true;       i += 2; continue; // 删除线
                    case 'o': italic = true;       i += 2; continue; // 斜体
                    case 'k': obf = true;          i += 2; continue; // 乱码
                    case 'r':
                        color = defaultColor != null ? defaultColor : Color.WHITE;
                        bold = italic = underline = strike = obf = false;
                        i += 2; continue;
                    default:
                        i++;
                        continue;
                }
            }

            int start = i;
            while (i < text.length()) {
                char c2 = text.charAt(i);
                if (c2 == SEC) break;
                i++;
            }
            String run = text.substring(start, i);
            if (run.isEmpty()) continue;

            if (obf) run = obfuscate(run);

            me.eldodebug.soar.management.nanovg.font.Font font =
                    bold ? Fonts.DEMIBOLD : Fonts.REGULAR;
            if (italic && !bold) font = Fonts.MEDIUM;

            if (shadow) {
                nvg.drawText(run, penX + 1f, y + 1f, new Color(0,0,0,140), size, font);
            }
            nvg.drawText(run, penX, y, color, size, font);

            float w = nvg.getTextWidth(run, size, font);

            if (underline || strike) {
                float thickness = Math.max(1f, size * 0.08f);
                if (underline) {
                    float uy = y + size * 0.85f;
                    nvg.drawRect(penX, uy, w, thickness, color);
                }
                if (strike) {
                    float sy2 = y + size * 0.45f;
                    nvg.drawRect(penX, sy2, w, thickness, color);
                }
            }

            penX += w;
        }

        return penX - x;
    }

    public static float width(NanoVGManager nvg, String text, float size) {
        Color color = Color.WHITE;
        boolean bold = false, italic = false, obf = false;

        float wTotal = 0f;
        int i = 0;

        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == SEC && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (MC_COLOR.containsKey(code)) {
                    color = MC_COLOR.get(code);
                    bold = italic = obf = false;
                    i += 2; continue;
                }
                switch (code) {
                    case 'l': bold = true;   i += 2; continue;
                    case 'o': italic = true; i += 2; continue;
                    case 'k': obf = true;    i += 2; continue;
                    case 'r': bold = italic = obf = false; i += 2; continue;
                    default: i++; continue;
                }
            }
            int start = i;
            while (i < text.length() && text.charAt(i) != SEC) i++;
            String run = text.substring(start, i);
            if (obf) run = obfuscate(run);

            me.eldodebug.soar.management.nanovg.font.Font font =
                    bold ? Fonts.DEMIBOLD : Fonts.REGULAR;
            if (italic && !bold) font = Fonts.MEDIUM;

            wTotal += nvg.getTextWidth(run, size, font);
        }
        return wTotal;
    }

    private static String obfuscate(String s) {
        char[] cs = s.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            if (Character.isWhitespace(cs[i])) continue;
            cs[i] = (char) (33 + OBFUSCATE_RNG.nextInt(94));
        }
        return new String(cs);
    }
}
