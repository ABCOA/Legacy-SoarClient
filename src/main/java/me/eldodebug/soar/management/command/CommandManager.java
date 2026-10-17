package me.eldodebug.soar.management.command;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.command.impl.*;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventSendChat;

import java.util.ArrayList;

public class CommandManager {

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
		return (message != null && message.startsWith(".soarcmd"));
	}



	public ArrayList<Command> getCommands() {
		return commands;
	}
}
