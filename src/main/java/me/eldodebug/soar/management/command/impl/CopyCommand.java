package me.eldodebug.soar.management.command.impl;

import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.Multithreading;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyCommand extends Command {

    public CopyCommand() {
        super("copy");
    }

    @Override
    public void onCommand(String message) {

        String text = message;

        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Multithreading.runAsync(()-> {
            try {
                clipboard.setContents(stringSelection, null);
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[Copy] " + EnumChatFormatting.WHITE + TranslateText.AFTER_COPY.getText()));
            } catch (Exception e) {
                SoarLogger.error("Failed to copy", e);
            }
        });
    }
}
