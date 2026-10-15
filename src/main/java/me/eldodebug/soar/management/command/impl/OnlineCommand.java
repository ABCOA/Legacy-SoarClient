package me.eldodebug.soar.management.command.impl;

import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.online.PresenceManager;
import java.util.Locale;

public final class OnlineCommand extends Command {
    public OnlineCommand() { super("online"); }
    @Override public void onCommand(String message) {
        PresenceManager manager = PresenceManager.getInstance();
        if (manager == null) return;
        String[] arguments = (message == null ? "" : message.trim()).split("\\s+", 2);
        String command = arguments[0].toLowerCase(Locale.ROOT);
        if (arguments.length == 1 && (command.isEmpty() || "list".equals(command))) manager.printRoster();
        else if (arguments.length == 1 && "status".equals(command)) manager.print(manager.getStatusText());
        else if (arguments.length == 1 && "confirm".equals(command)) manager.confirmAccountLink();
        else if (arguments.length == 1 && "cancel".equals(command)) manager.cancelAccountLink();
        else if ("bind".equals(command)) manager.previewAccountLink(arguments.length == 2 ? arguments[1] : "");
        else if (arguments.length == 1 && "help".equals(command)) {
            manager.print(TranslateText.ONLINE_LINK_HELP.getText());
            manager.print(TranslateText.ONLINE_LINK_COMMANDS.getText());
        } else manager.print(TranslateText.ONLINE_LINK_HELP.getText());
    }
}
