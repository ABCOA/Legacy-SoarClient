package me.eldodebug.soar.injection.mixin.mixins.gui;

import me.eldodebug.soar.Soar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {

    @Shadow protected Minecraft mc;

    @Shadow protected boolean field_178041_q;
    @Shadow protected int mouseX;
    @Shadow protected int mouseY;

    @Shadow protected int left;
    @Shadow protected int right;
    @Shadow protected int top;
    @Shadow protected int bottom;
    @Shadow protected int width;
    @Shadow protected int height;
    @Shadow protected int slotHeight;

    @Shadow protected boolean hasListHeader;

    @Shadow protected float amountScrolled;

    @Shadow protected abstract void drawBackground();
    @Shadow protected abstract int getScrollBarX();
    @Shadow protected abstract void bindAmountScrolled();
    @Shadow protected abstract int getListWidth();
    @Shadow protected abstract int getSize();
    @Shadow protected abstract void drawListHeader(int x, int y, Tessellator t);
    @Shadow protected abstract void drawSlot(int index, int x, int y, int height, int mouseX, int mouseY);
    @Shadow protected abstract boolean isSelected(int index);
    @Shadow protected abstract int getContentHeight();
    @Shadow protected abstract int func_148135_f();
    @Shadow protected abstract void func_148142_b(int mouseX, int mouseY);

    private ResourceLocation SOAR_BG = Soar.getInstance().getProfileManager().getBackgroundManager().getBackgroundLocation(Soar.getInstance().getProfileManager().getBackgroundManager().getCurrentBackground());

    private static final int EDGE_GRADIENT_PX = 4;
    private static final int SCROLLBAR_WIDTH  = 6;
    private static final int SCROLL_KNOB_MIN  = 32;
    private static final float RADIUS_ITEM    = 6f;
    private static final float RADIUS_SCROLL  = 4f;
    private static final float HL_HPAD          = 5.0f;
    private static final float HL_VPAD_TOP      = 0f;
    private static final float HL_VPAD_BOTTOM   = 0f;
    private static final float HL_BIAS_UP       = 2.0f;

    private static final int[] COL_ITEM_SELECTED = {255, 255, 255, 80};
    private static final int[] COL_ITEM_HOVERED  = {255, 255, 255, 40};
    private static final int[] COL_SCROLL_TRACK  = {  0,   0,   0, 120};
    private static final int[] COL_SCROLL_KNOB   = {180, 180, 180, 220};
    private static final int[] COL_SCROLL_EDGE   = {220, 220, 220, 255};

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void soar$drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!this.field_178041_q) return;

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        this.drawBackground();

        this.bindAmountScrolled();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        final Tessellator tess = Tessellator.getInstance();
        final WorldRenderer wr  = tess.getWorldRenderer();

        final ScaledResolution sr = new ScaledResolution(mc);
        final int w = sr.getScaledWidth();
        final int h = sr.getScaledHeight();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(SOAR_BG);

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, h, 0).tex(0, 1).endVertex();
        wr.pos(w, h, 0).tex(1, 1).endVertex();
        wr.pos(w, 0, 0).tex(1, 0).endVertex();
        wr.pos(0, 0, 0).tex(0, 0).endVertex();
        tess.draw();

        final int contentX = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
        final int contentY = this.top + 4 - (int) this.amountScrolled;

        if (this.hasListHeader) {
            this.drawListHeader(contentX, contentY, tess);
        }

        drawListWithRoundedSelection(contentX, contentY, mouseX, mouseY, partialTicks);

        GlStateManager.disableDepth();
        drawOutsideShades();
        drawEdgeGradients();

        drawRoundedScrollbar();

        this.func_148142_b(mouseX, mouseY);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);

        ci.cancel();
    }

    private void drawListWithRoundedSelection(int contentX, int contentY, int mouseX, int mouseY, float partialTicks) {
        final int size = this.getSize();
        if (size <= 0) return;

        final int slotW = this.getListWidth();
        final int slotH = this.slotHeight;

        pushListScissor();
        try {
            int hoveredIndex = -1;
            if (mouseY >= this.top && mouseY <= this.bottom) {
                int relY = mouseY - this.top - 4 + (int) this.amountScrolled;
                if (relY >= 0) hoveredIndex = relY / slotH;
            }

            int baseY = contentY;
            for (int i = 0; i < size; i++) {
                int y = baseY + i * slotH;

                if (y + slotH < this.top || y > this.bottom) continue;

                boolean selected = isSelected(i);
                boolean hovered  = (i == hoveredIndex);

                if (selected || hovered) {
                    int[] col = selected ? COL_ITEM_SELECTED : COL_ITEM_HOVERED;
                    float x0 = contentX - HL_HPAD;
                    float x1 = contentX + slotW + HL_HPAD;

                    float y0 = y + HL_VPAD_TOP - HL_BIAS_UP;
                    float y1 = y + slotH - HL_VPAD_BOTTOM - HL_BIAS_UP;

                    y0 = Math.max(y0, this.top + 0.5f);
                    y1 = Math.min(y1, this.bottom - 0.5f);

                    if (y1 > y0) {
                        GlStateManager.disableTexture2D();
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
                        drawRoundedRect(x0, y0, x1, y1, RADIUS_ITEM, col[0], col[1], col[2], col[3]);
                    }
                }

                GlStateManager.enableTexture2D();
                this.drawSlot(i, contentX, y, slotH, this.mouseX, this.mouseY);
            }
        } finally {
            popScissor();
        }
    }

    private void drawEdgeGradients() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr  = tess.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(this.left,  this.top + EDGE_GRADIENT_PX, 0).color(0, 0, 0,   0).endVertex();
        wr.pos(this.right, this.top + EDGE_GRADIENT_PX, 0).color(0, 0, 0,   0).endVertex();
        wr.pos(this.right, this.top,                   0).color(0, 0, 0, 255).endVertex();
        wr.pos(this.left,  this.top,                   0).color(0, 0, 0, 255).endVertex();
        tess.draw();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(this.left,  this.bottom,                   0).color(0, 0, 0, 255).endVertex();
        wr.pos(this.right, this.bottom,                   0).color(0, 0, 0, 255).endVertex();
        wr.pos(this.right, this.bottom - EDGE_GRADIENT_PX,0).color(0, 0, 0,   0).endVertex();
        wr.pos(this.left,  this.bottom - EDGE_GRADIENT_PX,0).color(0, 0, 0,   0).endVertex();
        tess.draw();

    }

    private static final int OUTSIDE_SHADE_ALPHA = 90;

    private void drawOutsideShades() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr  = tess.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(this.left,  this.top, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.right, this.top, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.right, 0,        0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.left,  0,        0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        tess.draw();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(this.left,  this.height, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.right, this.height, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.right, this.bottom, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        wr.pos(this.left,  this.bottom, 0).color(0, 0, 0, OUTSIDE_SHADE_ALPHA).endVertex();
        tess.draw();
    }

    private void drawRoundedScrollbar() {
        final int maxScroll = this.func_148135_f();
        if (maxScroll <= 0) return;

        final int trackLeft  = this.getScrollBarX() + 8;
        final int trackRight = trackLeft + SCROLLBAR_WIDTH;

        final int viewport = this.bottom - this.top;
        final int contentH = this.getContentHeight();

        int knobH = viewport * viewport / contentH;
        knobH = MathHelper.clamp_int(knobH, SCROLL_KNOB_MIN, viewport - 8);

        int knobY = (int) this.amountScrolled * (viewport - knobH) / maxScroll + this.top;
        if (knobY < this.top) knobY = this.top;

        float x0 = trackLeft;
        float x1 = trackRight;
        float y0 = this.top;
        float y1 = this.bottom;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        drawRoundedRect(x0, y0, x1, y1, RADIUS_SCROLL, COL_SCROLL_TRACK[0], COL_SCROLL_TRACK[1], COL_SCROLL_TRACK[2], COL_SCROLL_TRACK[3]);

        float ky0 = knobY;
        float ky1 = knobY + knobH;
        drawRoundedRect(x0 + 0.5f, ky0, x1 - 0.5f, ky1, RADIUS_SCROLL, COL_SCROLL_KNOB[0], COL_SCROLL_KNOB[1], COL_SCROLL_KNOB[2], COL_SCROLL_KNOB[3]);
    }

    private void drawRoundedRect(float x0, float y0, float x1, float y1, float radius,
                                 int r, int g, int b, int a) {
        if (x0 > x1) { float t = x0; x0 = x1; x1 = t; }
        if (y0 > y1) { float t = y0; y0 = y1; y1 = t; }

        float w = x1 - x0;
        float h = y1 - y0;
        radius = Math.max(0f, Math.min(radius, Math.min(w, h) * 0.5f));

        boolean wasCull  = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean wasTex2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean wasBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        int segments = Math.max(8, (int)(radius * 1.3f));

        fillRect(x0 + radius, y0, x1 - radius, y1, r, g, b, a);
        fillRect(x0, y0 + radius, x0 + radius, y1 - radius, r, g, b, a);
        fillRect(x1 - radius, y0 + radius, x1, y1 - radius, r, g, b, a);

        drawCorner(x0 + radius, y0 + radius, radius, 180f, 270f, segments, r, g, b, a);
        drawCorner(x1 - radius, y0 + radius, radius, 270f, 360f, segments, r, g, b, a);
        drawCorner(x1 - radius, y1 - radius, radius,   0f,  90f, segments, r, g, b, a);
        drawCorner(x0 + radius, y1 - radius, radius,  90f, 180f, segments, r, g, b, a);

        if (wasCull)  GlStateManager.enableCull();     else GlStateManager.disableCull();
        if (wasTex2D) GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D();
        if (wasBlend) GlStateManager.enableBlend();    else GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    private void fillRect(float x0, float y0, float x1, float y1,
                          int r, int g, int b, int a) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr  = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x0, y1, 0).color(r, g, b, a).endVertex();
        wr.pos(x1, y1, 0).color(r, g, b, a).endVertex();
        wr.pos(x1, y0, 0).color(r, g, b, a).endVertex();
        wr.pos(x0, y0, 0).color(r, g, b, a).endVertex();
        tess.draw();
    }

    private void drawCorner(float cx, float cy, float radius,
                            float angStartDeg, float angEndDeg, int segments,
                            int r, int g, int b, int a) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr  = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(r, g, b, a).endVertex();

        float step = (angEndDeg - angStartDeg) / segments;
        for (int i = 0; i <= segments; i++) {
            float ang = (float)Math.toRadians(angStartDeg + step * i);
            float x = (float)(cx + Math.cos(ang) * radius);
            float y = (float)(cy + Math.sin(ang) * radius);
            wr.pos(x, y, 0).color(r, g, b, a).endVertex();
        }
        tess.draw();
    }

    private void pushListScissor() {
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();

        int scX = this.left * scale;
        int scY = mc.displayHeight - this.bottom * scale;
        int scW = Math.max(0, (this.right - this.left) * scale);
        int scH = Math.max(0, (this.bottom - this.top) * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scX, scY, scW, scH);
    }

    private void popScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
