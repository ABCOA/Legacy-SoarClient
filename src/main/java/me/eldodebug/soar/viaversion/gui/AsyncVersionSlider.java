package me.eldodebug.soar.viaversion.gui;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import me.eldodebug.soar.viaversion.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.util.Collections;
import java.util.List;

public class AsyncVersionSlider extends GuiButton {
	
    private float dragValue = (float) (ViaLoadingBase.getProtocols().size() - ViaLoadingBase.getInstance().getTargetVersion().getIndex()) / ViaLoadingBase.getProtocols().size();

    private final List<ProtocolVersion> values;
    private float sliderValue;
    public boolean dragging;

    public AsyncVersionSlider(int buttonId, int x, int y , int widthIn, int heightIn) {
        super(buttonId, x, y, Math.max(widthIn, 110), heightIn, "");
        this.values = ViaLoadingBase.getProtocols();
        Collections.reverse(values);
        this.sliderValue = dragValue;
        this.displayString = values.get((int) (this.sliderValue * (values.size() - 1))).getName();
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    	
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                this.dragValue = sliderValue;
                this.displayString = values.get((int) (this.sliderValue * (values.size() - 1))).getName();
                ViaLoadingBase.getInstance().reload(values.get((int) (this.sliderValue * (values.size() - 1))));
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            this.dragValue = sliderValue;
            this.displayString = values.get((int) (this.sliderValue * (values.size() - 1))).getName();
            ViaLoadingBase.getInstance().reload(values.get((int) (this.sliderValue * (values.size() - 1))));
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    public void setVersion(int protocol) {
        this.dragValue = (float) (ViaLoadingBase.getProtocols().size() - ViaLoadingBase.fromProtocolId(protocol).getIndex()) / ViaLoadingBase.getProtocols().size();
        this.sliderValue = this.dragValue;
        this.displayString = values.get((int) (this.sliderValue * (values.size() - 1))).getName();
    }
}
