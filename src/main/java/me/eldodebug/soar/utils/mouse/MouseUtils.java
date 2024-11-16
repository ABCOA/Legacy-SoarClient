package me.eldodebug.soar.utils.mouse;

public class MouseUtils {
	
    public static boolean isInside(int mouseX, int mouseY, double x, double y, double width, double height) {
        return (mouseX > x && mouseX < (x + width)) && (mouseY > y && mouseY < (y + height));
    }
}
