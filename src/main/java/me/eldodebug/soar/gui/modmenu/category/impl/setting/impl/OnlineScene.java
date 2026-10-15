package me.eldodebug.soar.gui.modmenu.category.impl.setting.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.category.impl.SettingCategory;
import me.eldodebug.soar.gui.modmenu.category.impl.setting.SettingScene;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.online.PresenceManager;
import me.eldodebug.soar.management.online.PresenceManager.AccountLinkPreview;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OnlineScene extends SettingScene {
    private final Scroll contentScroll = new Scroll();
    private final CompTextBox codeBox = new CompTextBox();
    private AccountLinkPreview displayedPreview;
    private float actionY, drawnOffset;
    private boolean fieldVisible, inputShown, actionsShown;

    public OnlineScene(SettingCategory parent) {
        super(parent, TranslateText.ONLINE, TranslateText.ONLINE_DESCRIPTION, Icon.USERS);
        codeBox.setMaxStringLength(32);
    }
    @Override public void initGui() {
        codeBox.setText(""); codeBox.setFocused(false);
        contentScroll.resetAll(); displayedPreview = null;
        fieldVisible = inputShown = actionsShown = false;
    }
    @Override public void onClosed() {
        initGui();
        PresenceManager manager = PresenceManager.getInstance();
        if (manager != null && (manager.isAccountLinkBusy() || manager.getAccountLinkPreview() != null)) manager.cancelAccountLink();
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Soar soar = Soar.getInstance(); NanoVGManager nvg = soar.getNanoVGManager();
        ColorPalette palette = soar.getColorManager().getPalette();
        PresenceManager manager = soar.getOnlineManager();
        AccountLinkPreview preview = manager.getAccountLinkPreview();
        boolean busy = manager.isAccountLinkBusy();
        float textWidth = getWidth() - 20;
        List<String> privacy = lines(nvg, TranslateText.ONLINE_PRIVACY_DESCRIPTION.getText(), getWidth() - 12);
        float bindingY = getY() + 110 + privacy.size() * 15 + 12;
        List<String> hint = lines(nvg, preview == null ? TranslateText.ONLINE_LINK_HINT.getText()
                : TranslateText.ONLINE_LINK_REVIEW.getText(), textWidth);
        float detailY = bindingY + 30 + hint.size() * 15;
        List<String> target = new ArrayList<String>();
        if (preview != null) {
            target.addAll(lines(nvg, format(TranslateText.ONLINE_LINK_TARGET, preview.getAccountHint()), textWidth));
            target.addAll(lines(nvg, format(TranslateText.ONLINE_LINK_ACCOUNTID, preview.getAccountIdHint()), textWidth));
            target.addAll(lines(nvg, format(TranslateText.ONLINE_LINK_PLAYER, preview.getMinecraftName()), textWidth));
            target.addAll(lines(nvg, format(TranslateText.ONLINE_LINK_REMAINING,
                    Long.toString(Math.max(0, (preview.getRemainingMillis() + 999) / 1000))), textWidth));
        }
        inputShown = manager.canLinkAccount() && preview == null && !busy;
        actionsShown = manager.canLinkAccount();
        codeBox.setX(getX() + 10); codeBox.setY(detailY + 8);
        codeBox.setWidth(textWidth); codeBox.setHeight(22);
        codeBox.setDefaultText(TranslateText.ONLINE_LINK_CODE.getText());
        actionY = detailY + (preview == null ? 38 : target.size() * 15 + 8);
        float statusY = actionsShown ? actionY + 36 : detailY + 8;
        List<String> status = preview != null && !busy && manager.getAccountLinkStatus().equals(TranslateText.ONLINE_LINK_REVIEW.getText())
                ? new ArrayList<String>() : lines(nvg, manager.getAccountLinkStatus(), textWidth);
        float bottom = statusY + status.size() * 15 + 12;
        float maximum = Math.max(0, bottom - getY() - getHeight());
        contentScroll.setMaxScroll(maximum);
        if (preview != null && preview != displayedPreview) {
            contentScroll.setScrollPosition(-Math.min(maximum, bindingY - getY()));
            codeBox.setText(""); codeBox.setFocused(false);
        } else if (contentScroll.getValue() < -maximum) {
            contentScroll.setScrollPosition(-maximum);
        }
        if (MouseCursor.isInteractive() && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) contentScroll.onScroll();
        contentScroll.onAnimation(); drawnOffset = contentScroll.getValue();
        displayedPreview = preview;
        boolean interactive = MouseCursor.isInteractive();
        boolean inside = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());
        int localY = Math.round(mouseY - drawnOffset);
        fieldVisible = inputShown && codeBox.getY() + drawnOffset >= getY()
                && codeBox.getY() + drawnOffset + codeBox.getHeight() <= getY() + getHeight();
        if (!fieldVisible) codeBox.setFocused(false);
        nvg.save();
        nvg.intersectScissor(getX(), getY(), getWidth(), getHeight());
        nvg.translate(0, drawnOffset);
        MouseCursor.setInteractive(interactive && inside);
        nvg.drawRoundedRect(getX(), getY(), getWidth(), 42, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(manager.getStatusText(), getX() + 10, getY() + 15, palette.getFontColor(ColorType.DARK), 12, Fonts.MEDIUM);
        MouseCursor.pointer(mouseX, localY, getX(), getY() + 55, getWidth(), 42);
        nvg.drawRoundedRect(getX(), getY() + 55, getWidth(), 42, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.ONLINE_HIDE_USERNAME.getText(), getX() + 10, getY() + 70,
                palette.getFontColor(ColorType.DARK), 12, Fonts.MEDIUM);
        if (manager.isUsernameHidden()) nvg.drawText(Icon.CHECK, getX() + getWidth() - 28, getY() + 68,
                soar.getColorManager().getCurrentColor().getInterpolateColor(), 16, Fonts.ICON);
        drawLines(nvg, privacy, getX() + 4, getY() + 110, palette.getFontColor(ColorType.NORMAL));
        nvg.drawRoundedRect(getX(), bindingY, getWidth(), bottom - bindingY, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.ONLINE_LINK_TITLE.getText(), getX() + 10, bindingY + 10,
                palette.getFontColor(ColorType.DARK), 12, Fonts.MEDIUM);
        drawLines(nvg, hint, getX() + 10, bindingY + 30, palette.getFontColor(ColorType.NORMAL));
        drawLines(nvg, target, getX() + 10, detailY + 8, palette.getFontColor(ColorType.DARK));
        if (fieldVisible) codeBox.draw(mouseX, localY, partialTicks);
        if (actionsShown) {
            float buttonWidth = (textWidth - 8) / 2;
            drawButton(nvg, palette, mouseX, localY, getX() + 10, actionY, buttonWidth,
                    preview == null ? TranslateText.ONLINE_LINK_PREVIEW : TranslateText.ONLINE_LINK_CONFIRM, !busy);
            drawButton(nvg, palette, mouseX, localY, getX() + 18 + buttonWidth, actionY, buttonWidth, TranslateText.CANCEL, !busy || preview == null);
        }
        drawLines(nvg, status, getX() + 10, statusY, palette.getFontColor(ColorType.NORMAL));
        nvg.restore(); MouseCursor.setInteractive(interactive);
        if (maximum > 0) {
            float thumb = Math.max(16, getHeight() * getHeight() / (getHeight() + maximum));
            float y = getY() + Math.min(1, -drawnOffset / maximum) * (getHeight() - thumb);
            nvg.drawRoundedRect(getX() + getWidth() - 3, y, 2, thumb, 1, palette.getFontColor(ColorType.NORMAL, 100));
        }
    }
    private void drawButton(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY,
            float x, float y, float width, TranslateText label, boolean enabled) {
        nvg.drawRoundedRect(x, y, width, 26, 5, palette.getBackgroundColor(ColorType.NORMAL));
        float size = 10;
        while (size > 7 && nvg.getTextWidth(label.getText(), size, Fonts.MEDIUM) > width - 8) size -= 0.5F;
        nvg.drawText(label.getText(), x + 6, y + 8, palette.getFontColor(enabled ? ColorType.DARK : ColorType.NORMAL), size, Fonts.MEDIUM);
        if (enabled) MouseCursor.pointer(mouseX, mouseY, x, y, width, 26);
    }
    private static String format(TranslateText label, String value) { return String.format(Locale.ROOT, label.getText(), value); }
    private static List<String> lines(NanoVGManager nvg, String text, float width) {
        List<String> result = new ArrayList<String>(); StringBuilder line = new StringBuilder();
        for (int offset = 0; offset < text.length();) {
            int codepoint = text.codePointAt(offset); offset += Character.charCount(codepoint);
            String character = new String(Character.toChars(codepoint));
            if (line.length() > 0 && nvg.getTextWidth(line.toString() + character, 10, Fonts.REGULAR) > width) {
                result.add(line.toString()); line.setLength(0);
            }
            line.append(character);
        }
        if (line.length() > 0) result.add(line.toString());
        return result;
    }
    private static void drawLines(NanoVGManager nvg, List<String> lines, float x, float y, Color color) {
        for (int index = 0; index < lines.size(); index++) nvg.drawText(lines.get(index), x, y + index * 15, color, 10, Fonts.REGULAR);
    }
    @Override public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || !MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            codeBox.setFocused(false); return;
        }
        PresenceManager manager = Soar.getInstance().getOnlineManager();
        int localY = Math.round(mouseY - drawnOffset);
        if (MouseUtils.isInside(mouseX, localY, getX(), getY() + 55, getWidth(), 42)) {
            manager.setUsernameHidden(!manager.isUsernameHidden()); codeBox.setFocused(false); return;
        }
        if (fieldVisible && inputShown && manager.canLinkAccount() && !manager.isAccountLinkBusy()
                && manager.getAccountLinkPreview() == null) codeBox.mouseClicked(mouseX, localY, mouseButton);
        else codeBox.setFocused(false);
        if (!actionsShown || !manager.canLinkAccount()) return;
        float buttonWidth = (getWidth() - 28) / 2;
        if (!manager.isAccountLinkBusy() && MouseUtils.isInside(mouseX, localY, getX() + 10, actionY, buttonWidth, 26)) {
            if (displayedPreview != null && displayedPreview == manager.getAccountLinkPreview()) manager.confirmAccountLink();
            else if (inputShown && displayedPreview == null && manager.getAccountLinkPreview() == null) submitCode();
        } else if ((!manager.isAccountLinkBusy() || displayedPreview == null)
                && MouseUtils.isInside(mouseX, localY, getX() + 18 + buttonWidth, actionY, buttonWidth, 26)) {
            manager.cancelAccountLink(); codeBox.setText(""); codeBox.setFocused(false);
        }
    }
    private void submitCode() {
        PresenceManager.getInstance().previewAccountLink(codeBox.getText());
        codeBox.setText(""); codeBox.setFocused(false);
    }
    @Override public void keyTyped(char typedChar, int keyCode) {
        PresenceManager manager = PresenceManager.getInstance();
        if (!fieldVisible || !inputShown || !manager.canLinkAccount() || manager.isAccountLinkBusy() || manager.getAccountLinkPreview() != null) return;
        if (keyCode == Keyboard.KEY_TAB) { codeBox.setFocused(!codeBox.isFocused()); return; }
        if (codeBox.isFocused() && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) { submitCode(); return; }
        codeBox.keyTyped(typedChar, keyCode);
    }
}
