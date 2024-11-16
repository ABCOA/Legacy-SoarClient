package me.eldodebug.soar.utils;

import javax.swing.JOptionPane;

public class DialogUtils {
	
	public static void info(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void warn(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void error(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void error(Exception e) {
		error("Error occurred", e.toString());
	}
}
