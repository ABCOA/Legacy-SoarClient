package me.eldodebug.soar.management.security.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.security.SecurityFeature;
import net.minecraft.network.play.server.S48PacketResourcePackSend;

public class ResourcePackSecurity extends SecurityFeature {

	@EventTarget
	public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof S48PacketResourcePackSend) {
            S48PacketResourcePackSend pack = ((S48PacketResourcePackSend) event.getPacket());

            String url = pack.getURL();
            String hash = pack.getHash();

            if (url.toLowerCase().startsWith("level://")) {
                if(check(url, hash)) {
                	event.setCancelled(true);
                }
            }
        }
	}
	
    private boolean check(String url, String hash) {
        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme();
            boolean isLevelProtocol = "level".equals(scheme);

            if (!("http".equals(scheme) || "https".equals(scheme) || isLevelProtocol)) {
                throw new URISyntaxException(url, "Wrong protocol");
            }

            url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.toString());

            if (isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                throw new URISyntaxException(url, "Invalid levelstorage resource pack path");
            }

            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
