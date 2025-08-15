package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.mods.impl.ClickEffectMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = GuiScreen.class, priority = 2000)
public abstract class MixinGuiScreen extends Gui {

    @Shadow protected Minecraft mc;

    private ResourceLocation SOAR_BG = Soar.getInstance().getProfileManager().getBackgroundManager().getBackgroundLocation(Soar.getInstance().getProfileManager().getBackgroundManager().getCurrentBackground());

    @Shadow protected abstract void keyTyped(char typedChar, int keyCode);

    private boolean shouldReplace() {
        return !((Object) this instanceof GuiIngameMenu);
    }

    private void drawSoarBg(int tint) {
        final ScaledResolution sr = new ScaledResolution(mc);
        final int w = sr.getScaledWidth();
        final int h = sr.getScaledHeight();

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        mc.getTextureManager().bindTexture(SOAR_BG);
        GlStateManager.color(1f, 1f, 1f, 1f);

        final Tessellator t = Tessellator.getInstance();
        final WorldRenderer wr = t.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0,  h, 0).tex(0, 1).endVertex();
        wr.pos(w,  h, 0).tex(1, 1).endVertex();
        wr.pos(w,  0, 0).tex(1, 0).endVertex();
        wr.pos(0,  0, 0).tex(0, 0).endVertex();
        t.draw();

        final int a = MathHelper.clamp_int(tint, 0, 255);
        if (a > 0) {
            final int col = a << 24;
            drawGradientRect(0, 0, w, h / 2, col, 0);
            drawGradientRect(0, h / 2, w, h, 0, col);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    @Inject(method = "drawBackground(I)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void soar$onDrawBackgroundWithTint(int tint, CallbackInfo ci) {
        if (!shouldReplace()) return;
        drawSoarBg(tint);
        ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void postDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (ClickEffectMod.getInstance().isToggled()) {
            ClickEffectMod.getInstance().drawClickEffects();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    public void preMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (ClickEffectMod.getInstance().isToggled()) {
            ClickEffectMod.getInstance().addClickEffect(mouseX, mouseY);
        }
    }

    /**
     * @author EldoDebug
     */
    @Overwrite
    public void handleKeyboardInput() throws IOException {
        char c = Keyboard.getEventCharacter();

        if ((Keyboard.getEventKey() == 0 && c >= ' ') || Keyboard.getEventKeyState()) {
            this.keyTyped(c, Keyboard.getEventKey());
        }

        mc.dispatchKeypresses();
    }
}