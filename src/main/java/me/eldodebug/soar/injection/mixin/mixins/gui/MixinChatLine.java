package me.eldodebug.soar.injection.mixin.mixins.gui;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.IMixinChatLine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;

@Mixin(ChatLine.class)
public class MixinChatLine implements IMixinChatLine {
	
    private NetworkPlayerInfo playerInfo;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int i, IChatComponent iChatComponent, int j, CallbackInfo ci) {
    	
        chatLines.add(new WeakReference<>((ChatLine) (Object) this));
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) return;
        Map<String, NetworkPlayerInfo> nicknameCache = new HashMap<>();
        
        try {
            for (String word : iChatComponent.getFormattedText().split("(§.)|\\W")) {
            	
                if (word.isEmpty()) {
                	continue;
                }
                
                playerInfo = netHandler.getPlayerInfo(word);
                
                if (playerInfo == null) {
                    playerInfo = getPlayerFromNickname(word, netHandler, nicknameCache);
                }
                
                if (playerInfo != null) {
                	break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static NetworkPlayerInfo getPlayerFromNickname(String word, NetHandlerPlayClient connection, Map<String, NetworkPlayerInfo> nicknameCache) {
    	
        if (nicknameCache.isEmpty()) {
            for (NetworkPlayerInfo p : connection.getPlayerInfoMap()) {
            	
                IChatComponent displayName = p.getDisplayName();
                
                if (displayName != null) {
                    String nickname = displayName.getUnformattedTextForChat();
                    
                    if (word.equals(nickname)) {
                        nicknameCache.clear();
                        return p;
                    }
                    
                    nicknameCache.put(nickname, p);
                }
            }
        } else {
            return nicknameCache.get(word);
        }

        return null;
    }

    @Override
    public NetworkPlayerInfo getPlayerInfo() {
        return playerInfo;
    }
}
