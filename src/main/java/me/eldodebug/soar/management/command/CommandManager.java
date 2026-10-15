package me.eldodebug.soar.management.command;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.command.impl.*;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventSendChat;

import java.util.ArrayList;

public class CommandManager {

	private static final String IRC_PREFIX = ".irc";
	private static final String ONLINE_PREFIX = ".online";
	private Command ircCommand = new IRCCommand();
	private Command onlineCommand = new OnlineCommand();
	private ArrayList<Command> commands = new ArrayList<Command>();
	
	public CommandManager() {

		commands.add(new ClaimCommand());
		commands.add(new CopyCommand());
		commands.add(new ScreenshotCommand());
		commands.add(new TranslateCommand());
		
		Soar.getInstance().getEventManager().register(this);
	}
	
	@EventTarget
	public void onSendChat(EventSendChat event) {
		if (isOnlineCommand(event.getMessage())) {
			event.setCancelled(true);
			onlineCommand.onCommand(event.getMessage().substring(ONLINE_PREFIX.length()).trim());
			return;
		}
		
		if(isIrcCommand(event.getMessage())) {
			event.setCancelled(true);
			ircCommand.onCommand(event.getMessage().substring(IRC_PREFIX.length()).trim());
			return;
		}

		if(event.getMessage().startsWith(".soarcmd")) {
			
			event.setCancelled(true);
			
			String input = event.getMessage().substring(".soarcmd".length()).trim();
			String[] args = input.split("\\s+", 2);
			for(Command c : commands) {
				if(args[0].equals(c.getPrefix())) {
					c.onCommand(args.length > 1 ? args[1] : "");
					break;
				}
			}
		}
	}

	public static boolean isClientCommand(String message) {
		return isIrcCommand(message) || isOnlineCommand(message) || (message != null && message.startsWith(".soarcmd"));
	}

	private static boolean isOnlineCommand(String message) {
		return message != null && message.startsWith(ONLINE_PREFIX)
				&& (message.length() == ONLINE_PREFIX.length()
						|| Character.isWhitespace(message.charAt(ONLINE_PREFIX.length())));
	}

	private static boolean isIrcCommand(String message) {
		return message != null && message.startsWith(IRC_PREFIX)
				&& (message.length() == IRC_PREFIX.length()
						|| Character.isWhitespace(message.charAt(IRC_PREFIX.length())));
	}

	public ArrayList<Command> getCommands() {
		return commands;
	}
}
