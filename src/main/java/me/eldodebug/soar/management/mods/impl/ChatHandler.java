package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatHandler {
    public static void handleChat(S02PacketChat chatPacket) {
        Minecraft mc = Minecraft.getMinecraft();
        IChatComponent chatComponent = chatPacket.getChatComponent();
        String chatMessage = chatComponent.getUnformattedText();
        String cleanMessage = chatMessage.replaceAll("§[0-9a-fk-or]", "");

        if (chatComponent.getSiblings().stream().anyMatch(sibling -> sibling.getUnformattedText().contains("[☭]") || sibling.getUnformattedText().contains("[✎]"))) {
            return;
        }

        if (cleanMessage.replaceAll(" ", "").isEmpty() || chatPacket.getType() == 2) {
            return;
        }

        if (ChatTranslateMod.getInstance().isToggled() && ChatCopyMod.getInstance().isToggled()) {
            IChatComponent translateComponent = new ChatComponentText(" [" + String.valueOf('\u270E') + "]")
                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd translate " + chatMessage))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_TRANSLATE.getText()))));

            IChatComponent copyComponent = new ChatComponentText(" [" + String.valueOf('\u262D') + "]")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd copy " + cleanMessage))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_COPY.getText()))));
            chatComponent.appendSibling(translateComponent).appendSibling(copyComponent);
            mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        } else if (ChatTranslateMod.getInstance().isToggled() && !ChatCopyMod.getInstance().isToggled()) {
            IChatComponent translateComponent = new ChatComponentText(" [" + String.valueOf('\u270E') + "]")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd translate " + chatMessage))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_TRANSLATE.getText()))));
            chatComponent.appendSibling(translateComponent);
            mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        } else {
            IChatComponent copyComponent = new ChatComponentText(" [" + String.valueOf('\u262D') + "]")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd copy " + chatMessage))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_COPY.getText()))));
            chatComponent.appendSibling(copyComponent);
            mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        }
    }
}
