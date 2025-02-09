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
		commands.add(new IRCCommand());
		commands.add(new OnlineUsers());
		commands.add(new ScreenshotCommand());
		commands.add(new TranslateCommand());
		
		Soar.getInstance().getEventManager().register(this);
	}
	
	@EventTarget
	public void onSendChat(EventSendChat event) {
		
		if(event.getMessage().startsWith(".soarcmd")) {
			
			event.setCancelled(true);
			
			String[] args = event.getMessage().split(" ");
			
			if(args.length > 1) {
				for(Command c : commands) {
					if(args[1].equals(c.getPrefix())) {
						c.onCommand(event.getMessage().replace(".soarcmd ", "").replace(args[1] + " ", ""));
					}
				}
			}
		}
	}

	public ArrayList<Command> getCommands() {
		return commands;
	}
}
