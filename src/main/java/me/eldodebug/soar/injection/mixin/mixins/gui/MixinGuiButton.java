package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.injection.mixin.access.IOptionSliderAccessor;
import me.eldodebug.soar.injection.mixin.access.ISoundButtonAccessor;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.text.MCText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;

import java.awt.*;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

    @Shadow public boolean visible;
    @Shadow public int xPosition, yPosition, width, height;
    @Shadow protected boolean hovered;
    @Shadow public boolean enabled;
    @Shadow public String displayString;
    @Shadow @Final protected static ResourceLocation buttonTextures;
    @Shadow protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    @Unique private final SimpleAnimation hoverAnim = new SimpleAnimation();
    @Unique private float fillPixels = 0f;
    @Unique private long  lastNs     = 0L;
    /**
     * @author Soar
     **/
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

        final boolean isSlider = (Object)this instanceof IOptionSliderAccessor;
        final boolean isSound = (Object)this instanceof ISoundButtonAccessor;
        final boolean isValueControl = isSlider || isSound;

        float value01 = -1f;
        if ((Object)this instanceof IOptionSliderAccessor) {
            value01 = MathHelper.clamp_float(((IOptionSliderAccessor)this).soar$getSliderValue(), 0f, 1f);
        } else if ((Object)this instanceof ISoundButtonAccessor) {
            value01 = MathHelper.clamp_float(((ISoundButtonAccessor)this).soar$getSoundProgress(), 0f, 1f);
        }

        hoverAnim.setAnimation(hovered ? 1f : 0f, 12f);
        final float hover01 = hoverAnim.getValue();

        float target;
        if (isValueControl) {
            target = value01 > 0f ? value01 * width : 0f;
        } else {
            target = hover01 * width;
        }
        if (target < 1f) target = 0f;

        long now = System.nanoTime();
        float dt = (lastNs == 0L) ? 1f : Math.min(3f, Math.max(0f, (now - lastNs) / 1_000_000_000f * 60f));
        lastNs = now;
        float follow = Math.min(1f, dt * 0.35f);
        fillPixels += (target - fillPixels) * follow;

        final int fillInt = Math.max(0, Math.min((int)Math.floor(fillPixels + 1e-4f), width));
        final boolean drawFill = enabled && fillInt >= 2;

        final Color bgColor = new Color(230, 230, 230, 120);
        Color c1, c2;
        try {
            AccentColor ac = Soar.getInstance().getColorManager().getCurrentColor();
            c1 = ac.getColor1();
            c2 = ac.getColor2();
        } catch (Throwable t) {
            c1 = new Color(0,120,255);
            c2 = new Color(0,180,255);
        }
        final int outlineAlpha = (int)((isValueControl ? (hover01 * 140) : (hover01 * 255)));

        final int fx = xPosition, fy = yPosition, fw = width, fh = height;
        final float r = 4.5f;
        final float fontSize = 9.5f;
        final float cx = fx + fw / 2f;
        final float ty = fy + (fh - fontSize) / 2f + 1f;

        final Color textCol = enabled
                ? (isValueControl ? new Color(255,255,255,220) : new Color(255 - (int)(hover01 * 200), 255 - (int)(hover01 * 200), 255 - (int)(hover01 * 200))) : new Color(255,255,255,180);

        NanoVGManager nvg = null;
        try { nvg = Soar.getInstance().getNanoVGManager(); } catch (Throwable ignored) {}

        if (nvg != null) {
            final NanoVGManager nv = nvg;
            final Color fc1 = c1, fc2 = c2, fbg = bgColor;
            final int drawW0 = fillInt;
            final String label = this.displayString;

            nv.setupAndDraw(() -> {
                nv.drawRoundedRect(fx, fy, fw, fh, r, fbg);

                if (drawFill) {
                    float inset = 1f;
                    float fillW = drawW0 - inset * 2f;
                    if (fillW >= 1f) {
                        float rFill = Math.min(r - 1f, Math.max(0f, Math.min(fillW, fh - inset * 2f) * 0.5f));
                        nv.drawRoundedRect(fx + inset, fy + inset, fillW, fh - inset * 2f, rFill, Color.LIGHT_GRAY);
                    }
                }

                if (outlineAlpha > 0) {
                    nv.drawGradientOutlineRoundedRect(
                            fx, fy, fw, fh, r, 1.5f,
                            new Color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), outlineAlpha),
                            new Color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), outlineAlpha)
                    );
                }

                float tw = MCText.width(nv, label, fontSize);
                float tx = cx - tw / 2f;
                MCText.draw(nv, label, tx, ty, fontSize, textCol, false);
            });
        } else {
            drawRect(fx, fy, fx + fw, fy + fh, bgColor.getRGB());
            if (drawFill) drawRect(fx + 1, fy + 1, fx + fillInt - 1, fy + fh - 1, Color.LIGHT_GRAY.getRGB());
            drawCenteredString(mc.fontRendererObj, displayString,
                    fx + fw / 2, fy + (fh - 8) / 2, enabled ? 0xFFFFFF : 0xA0A0A0);
        }

        mc.getTextureManager().bindTexture(buttonTextures);
        mouseDragged(mc, mouseX, mouseY);
    }
}
