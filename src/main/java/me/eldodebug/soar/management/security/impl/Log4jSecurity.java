package me.eldodebug.soar.management.security.impl;

import java.util.regex.Pattern;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.event.impl.EventSendChat;
import me.eldodebug.soar.management.security.SecurityFeature;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.IChatComponent;

public class Log4jSecurity extends SecurityFeature {

	private Pattern pattern = Pattern.compile(".*\\$\\{[^}]*\\}.*");
	
	@EventTarget
	public void onReceivePacket(EventReceivePacket event) {
		
		 if (event.getPacket() instanceof S29PacketSoundEffect) {
			 
			 S29PacketSoundEffect sound = (S29PacketSoundEffect) event.getPacket();
			 String name = sound.getSoundName();
	            
			 if(pattern.matcher(name).matches()) {
				 event.setCancelled(true);
			 }
		 }
		 
		 if (event.getPacket() instanceof S02PacketChat) {
			 S02PacketChat chat = ((S02PacketChat) event.getPacket());
			 IChatComponent component = chat.getChatComponent();
			 
			 if(pattern.matcher(component.getUnformattedText()).matches() || pattern.matcher(component.getFormattedText()).matches()) {
				 event.setCancelled(true);
			 }
		 }
	}
	
	@EventTarget
	public void onChat(EventSendChat event) {
		if(pattern.matcher(event.getMessage()).matches()) {
			event.setCancelled(true);
		}
	}
}
