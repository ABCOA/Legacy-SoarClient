package me.eldodebug.soar.hooks;

import me.eldodebug.soar.injection.interfaces.IMixinChatLine;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;

public class GuiNewChatHook {

	private static Minecraft mc = Minecraft.getMinecraft();

    public static boolean hasOnlineIcon(ChatLine line) {
        if (!(line instanceof IMixinChatLine)) return false;
        OnlineChatMetadata.Sender sender = ((IMixinChatLine)line).getOnlineSender();
        me.eldodebug.soar.management.online.PresenceManager online = me.eldodebug.soar.management.online.PresenceManager.getInstance();
        return sender != null && !sender.isSuppressed() && sender.isFirstLine() && sender.getUuid() != null
                && online != null && online.isOnline(sender.getUuid());
    }

    public static int prefixWidth(ChatLine line) {
        if (!(line instanceof IMixinChatLine)) return 0;
        me.eldodebug.soar.management.mods.impl.ChatMod mod = me.eldodebug.soar.management.mods.impl.ChatMod.getInstance();
        int head = mod.isToggled() && mod.getHeadSetting().isToggled()
                && ((IMixinChatLine)line).getPlayerInfo() != null ? 10 : 0;
        return head + (hasOnlineIcon(line) ? 11 : 0);
    }
	
	public static int drawStringWithHead(ChatLine line, String text, float x, float y, int color) {
		return drawDecoratedString(line, text, x, y, color, true);
	}

	public static int drawDecoratedString(ChatLine line, String text, float x, float y, int color, boolean showHead) {
		
		float actualX = x;
		if(!(line instanceof IMixinChatLine)) return mc.fontRendererObj.drawStringWithShadow(text, actualX, y, color);
		IMixinChatLine hook = (IMixinChatLine) line;
		
		NetworkPlayerInfo networkPlayerInfo = showHead ? hook.getPlayerInfo() : null;
		
		actualX += networkPlayerInfo != null ? 10f : 0;
		
		if(networkPlayerInfo != null) {
			
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            
            mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
            
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, ColorUtils.getAlphaByInt(color));
            
            RenderUtils.drawScaledCustomSizeModalRect(x, y - 0.5, 8.0f, 8.0f, 8, 8, 8, 8, 64.0f, 64.0f);
            RenderUtils.drawScaledCustomSizeModalRect(x, y - 0.5, 40.0f, 8.0f, 8, 8, 8, 8, 64.0f, 64.0f);
            
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if(hasOnlineIcon(line)) {
			int iconColor = (color & 0xFF000000) | 0x00FFD24A;
			me.eldodebug.soar.utils.render.IconFontRenderer.drawIcon(me.eldodebug.soar.management.nanovg.font.Icon.SOAR, actualX, y, 8, iconColor);
			actualX += 11;
		}
		
		return mc.fontRendererObj.drawStringWithShadow(text, actualX, y, color);
	}
}
