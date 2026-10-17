package me.eldodebug.soar.gui.modmenu.category.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.profile.Profile;
import me.eldodebug.soar.management.profile.ProfileIcon;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.management.profile.ProfileType;
import me.eldodebug.soar.ui.comp.impl.field.CompTextBox;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProfileCategory extends Category {

	private enum Modal { NONE, NEW, COPY, DELETE, RESET }

	private enum Style { PRIMARY, CARD, PAGE, DANGER, DANGER_SOFT }

	private static final TranslateText[] TAG_NAMES = {TranslateText.PROFILE_BEDWARS, TranslateText.PROFILE_DUELS, TranslateText.PROFILE_BRIDGE, TranslateText.PROFILE_POTPVP,
			TranslateText.PROFILE_SKYWARS, TranslateText.PROFILE_UHC, TranslateText.PROFILE_PERFORMANCE, TranslateText.PROFILE_HUD};
	private static final float LIST_WIDTH = 150, ROW_HEIGHT = 30, ROW_STRIDE = 34;
	private static final Color GREEN = new Color(76, 175, 80), AMBER = new Color(255, 179, 0), STAR = new Color(255, 193, 7);

	private final CompTextBox search = new CompTextBox(), name = new CompTextBox(), rename = new CompTextBox(),
			server = new CompTextBox(), title = new CompTextBox(), description = new CompTextBox();
	private final Scroll listScroll = new Scroll(), warningScroll = new Scroll();
	private final SimpleAnimation tabAnimation = new SimpleAnimation(), modalAnimation = new SimpleAnimation();
	private final Map<String, SimpleAnimation> animations = new HashMap<String, SimpleAnimation>();
	private final List<Hit> hits = new ArrayList<Hit>();
	private final Set<String> tags = new LinkedHashSet<String>();

	private boolean renaming, serverVisible, blocked;
	private String selectedLocal, selectedCloud, searchSeen = "", requestedQuery = "", localQuery = "";
	private String pendingSelect, serverOwner, publishOwner, switching, renameError, lastClick, tooltip;
	private long refreshAt, searchAt, account = -1, lastClickTime;
	private Modal modal = Modal.NONE;
	private Profile target;
	private float listHeight, modalX, modalY, modalW, modalH;
	private JsonObject countedPreview;
	private int enabledModules = -1, hudModules;

	private NanoVGManager nvg;
	private ColorPalette palette;
	private AccentColor accent;
	private int mx, my;
	private float ticks;

	public ProfileCategory(GuiModMenu parent) {
		super(parent, TranslateText.PROFILE, Icon.EDIT, false);
		search.setMaxStringLength(128);
		name.setMaxStringLength(80);
		rename.setMaxStringLength(80);
		server.setMaxStringLength(255);
		title.setMaxStringLength(80);
		description.setMaxStringLength(2000);
	}

	private ProfileManager manager() {
		return Soar.getInstance().getProfileManager();
	}



	@Override
	public void initGui() {
		closeModal();
		cancelRename();
		hits.clear();
		listScroll.resetAll();
	}

	@Override
	public void initCategory() {
		closeModal();
		listScroll.resetAll();
		refreshAt = 0;
	}

	@Override
	public void onClosed() {
		commitServer();
		cancelRename();
		closeModal();
		blur();
		manager().save();
	}



	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Soar instance = Soar.getInstance();
		nvg = instance.getNanoVGManager();
		palette = instance.getColorManager().getPalette();
		accent = instance.getColorManager().getCurrentColor();
		mx = mouseX;
		my = mouseY;
		ticks = partialTicks;
		hits.clear();
		tooltip = null;
		scroll.setMaxScroll(0);


		boolean modalOpen = modal != Modal.NONE;
		setCanClose(!modalOpen);

		blocked = modalOpen;
		drawHeader();
		drawBody();
		drawFooter();
		blocked = false;

		modalAnimation.setAnimation(modalOpen ? 1 : 0, 20);
		if(modalOpen) {
			drawModal();
		}

		drawTooltip();
	}


    private void drawHeader() {
        float x=getX()+10,y=getY()+9,right=getX()+getWidth()-10;
        nvg.drawText(TranslateText.PROFILE_LOCAL.getText(),x,y+4,textColor(),10,Fonts.MEDIUM);
        iconButton("import",Icon.FOLDER,TranslateText.PROFILE_IMPORT.getText(),right-20,y,Style.PAGE,!manager().isWorking(),false,()->choose(false,null));
        iconButton("new",Icon.PLUS,TranslateText.PROFILE_NEW_PROFILE.getText(),right-44,y,Style.PRIMARY,!manager().isWorking(),false,()->openModal(Modal.NEW,null));
        drawSearch(x+100,y,right-52-x-100,20);
    }

	private void drawSegment(String label, float x, float y, float width, boolean selected) {
		nvg.drawCenteredText(limit(label, 9, Fonts.MEDIUM, width - 12), x + width / 2, y + 5.5F,
				selected ? Color.WHITE : textColor(), 9, Fonts.MEDIUM);
	}

	private void drawSearch(float x, float y, float width, float height) {
		nvg.drawRoundedRect(x, y, width, height, 6, palette.getBackgroundColor(ColorType.NORMAL));
		search.setDefaultText(TranslateText.PROFILE_SEARCH.getText());
		search.setPosition(x, y, width - 18, height);
		search.draw(mx, my, ticks);
		nvg.drawOutlineRoundedRect(x, y, width, height, 6, search.isFocused() ? 1 : 0.7F,
				search.isFocused() ? accent.getColor1() : palette.getBackgroundColor(ColorType.DARK));

		if(search.getText().isEmpty()) {
			nvg.drawCenteredText(Icon.SEARCH, x + width - 10, y + 5.5F, mutedColor(), 9, Fonts.ICON);
		} else {
			boolean hovered = hover(x + width - 18, y, 18, height);
			nvg.drawCenteredText(Icon.X, x + width - 10, y + 5.5F, hovered ? accent.getColor1() : mutedColor(), 9, Fonts.ICON);
			hit(x + width - 18, y, 18, height, () -> {
				search.setText("");
				search.setFocused(false);
			});
		}
	}


    private void drawBody() {
        float x=getX()+10,y=getY()+37,height=getHeight()-63;
        float detailX=x+LIST_WIDTH+8,detailWidth=getWidth()-20-LIST_WIDTH-8;
        serverVisible=false;
        drawLocalList(x,y,LIST_WIDTH,height);
        nvg.drawRoundedRect(detailX,y,detailWidth,height,8,palette.getBackgroundColor(ColorType.DARK));
        drawLocalDetails(detailX,y,detailWidth,height);
    }

	private List<Profile> localProfiles() {
		String query = search.getText().trim().toLowerCase(Locale.ROOT);
		List<Profile> list = new ArrayList<Profile>();

		for(Profile p : manager().getProfiles()) {
			if(p.getName().toLowerCase(Locale.ROOT).contains(query) || p.getServerIp().toLowerCase(Locale.ROOT).contains(query)) {
				list.add(p);
			}
		}

		list.sort(Comparator.comparing((Profile p) -> p.getType() != ProfileType.FAVORITE).thenComparing(Profile::getName, String.CASE_INSENSITIVE_ORDER));
		return list;
	}

	private void drawLocalList(float x, float y, float width, float height) {
		List<Profile> list = localProfiles();

		if(pendingSelect != null) {
			for(int i = 0; i < list.size(); i++) {
				if(list.get(i).getName().equalsIgnoreCase(pendingSelect)) {
					listHeight = height;
					selectLocal(list.get(i));
					reveal(i);
					pendingSelect = null;
					break;
				}
			}
		}

		if(selected() == null && manager().getCurrent() != null) {
			selectedLocal = manager().getCurrent().getStableId();
		}

		beginList(x, y, width, height, list.size());

		if(list.isEmpty()) {
			centeredLines(TranslateText.PROFILE_EMPTY.getText(), x + width / 2, y + 20, width - 16, 9, mutedColor(), 2);
		}

		for(int i = 0; i < list.size(); i++) {
			Profile p = list.get(i);
			float rowY = y + i * ROW_STRIDE + listScroll.getValue();

			if(rowY + ROW_HEIGHT < y || rowY > y + height) {
				continue;
			}

			boolean selected = p.getStableId().equals(selectedLocal), active = p == manager().getCurrent();
			boolean favorite = p.getType() == ProfileType.FAVORITE;
			float textWidth = width - 38 - (favorite ? 14 : 0);

			drawRow("local-" + p.getStableId(), x, rowY, width, selected, y, height);
			nvg.drawRoundedImage(p.getIcon().getIcon(), x + 6, rowY + 5, 20, 20, 5);

			String subtitle = active ? TranslateText.PROFILE_ACTIVE.getText() : p.getServerIp();

			if(subtitle.isEmpty()) {
				nvg.drawText(limit(p.getName(), 9.5F, Fonts.MEDIUM, textWidth), x + 32, rowY + 10.25F, textColor(), 9.5F, Fonts.MEDIUM);
			} else {
				nvg.drawText(limit(p.getName(), 9.5F, Fonts.MEDIUM, textWidth), x + 32, rowY + 5, textColor(), 9.5F, Fonts.MEDIUM);

				if(active) {
					nvg.drawCircle(x + 34.5F, rowY + 21.5F, 2.2F, accent.getColor1());
					nvg.drawText(limit(subtitle, 7.5F, Fonts.REGULAR, textWidth - 7), x + 39.5F, rowY + 17.75F, accent.getColor1(), 7.5F, Fonts.REGULAR);
				} else {
					nvg.drawText(limit(subtitle, 7.5F, Fonts.REGULAR, textWidth), x + 32, rowY + 17.75F, mutedColor(), 7.5F, Fonts.REGULAR);
				}
			}

			if(favorite) {
				nvg.drawText(Icon.STAR_FILL, x + width - 17, rowY + 10, STAR, 10, Fonts.ICON);
			}

			listHit(x, rowY, width, y, height, () -> clickLocal(p));
		}

		endList(x, y, width, height, list.size());
	}

	private void drawLocalDetails(float x, float y, float width, float height) {
		Profile p = selected();

		if(p == null) {
			centeredLines(TranslateText.PROFILE_SELECT.getText(), x + width / 2, y + height / 2 - 5, width - 24, 9.5F, mutedColor(), 2);
			return;
		}

		boolean active = p == manager().getCurrent(), working = manager().isWorking();
		float inX = x + 12, inWidth = width - 24, useWidth = 62, useX = x + width - 12 - useWidth;
		float textX = inX + 42, nameWidth = useX - 8 - textX;

		nvg.drawRoundedImage(p.getIcon().getIcon(), inX, y + 12, 32, 32, 7);

		if(renaming) {
			field(rename, TranslateText.PROFILE_NAME.getText(), textX, y + 11, nameWidth, 18);
			String note = renameError != null ? renameError : TranslateText.PROFILE_RENAME_HINT.getText();
			nvg.drawText(limit(note, 7.5F, Fonts.REGULAR, nameWidth), textX, y + 33, renameError != null ? palette.getMaterialRed() : mutedColor(), 7.5F, Fonts.REGULAR);
		} else {
			String display = limit(p.getName(), 12, Fonts.MEDIUM, nameWidth - (p.isDefault() ? 0 : 15));
			nvg.drawText(display, textX, y + 13, textColor(), 12, Fonts.MEDIUM);

			if(!p.isDefault()) {
				iconLink("rename", Icon.PENCIL, TranslateText.PROFILE_RENAME.getText(), textX + nvg.getTextWidth(display, 12, Fonts.MEDIUM) + 4, y + 14, 10, !working, () -> startRename(p));
			}

			String kind = p.isDefault() ? TranslateText.PROFILE_DEFAULT.getText() : !p.getSourceId().isEmpty() ? TranslateText.PROFILE_FROM_CLOUD.getText() : TranslateText.PROFILE_CUSTOM.getText();
			nvg.drawText(limit(kind, 8, Fonts.REGULAR, nameWidth), textX, y + 31, mutedColor(), 8, Fonts.REGULAR);
		}

		if(active) {
			nvg.drawRoundedRect(useX, y + 17, useWidth, 22, 6, ColorUtils.applyAlpha(accent.getColor1(), 32));
			nvg.drawOutlineRoundedRect(useX, y + 17, useWidth, 22, 6, 0.8F, accent.getColor1());
			drawContent(Icon.CHECK, limit(TranslateText.PROFILE_ACTIVE.getText(), 8.5F, Fonts.MEDIUM, useWidth - 22), useX + useWidth / 2, y + 28, accent.getColor1(), 8.5F);
		} else {
			boolean switchingHere = working && p.getStableId().equals(switching);
			button("use", switchingHere ? TranslateText.PROFILE_SWITCHING.getText() : TranslateText.PROFILE_USE.getText(), null, useX, y + 17, useWidth, 22,
					Style.PRIMARY, !working, () -> use(p));
		}

		label(TranslateText.PROFILE_ICON.getText(), inX, y + 54);
		float size = 22, gap = (inWidth - size * 7) / 6;
		int index = 0;

		for(ProfileIcon icon : ProfileIcon.values()) {
			float iconX = inX + index++ * (size + gap), iconY = y + 65;
			boolean chosen = icon == p.getIcon(), hovered = !working && !chosen && hover(iconX - 2, iconY - 2, size + 4, size + 4);
			SimpleAnimation animation = animation("icon-" + icon.name());
			animation.setAnimation(chosen ? 1 : hovered ? 0.8F : 0.5F, 18);

			nvg.drawRoundedImage(icon.getIcon(), iconX, iconY, size, size, 5, animation.getValue());

			if(chosen) {
				nvg.drawGradientOutlineRoundedRect(iconX - 2, iconY - 2, size + 4, size + 4, 6, 1.4F, accent.getColor1(), accent.getColor2());
			} else if(!working) {
				hit(iconX - 2, iconY - 2, size + 4, size + 4, () -> manager().updateMetadata(p, p.getType(), icon, p.getServerIp()));
			}
		}

		label(TranslateText.PROFILE_SERVER.getText(), inX, y + 97);

		if(!p.getStableId().equals(serverOwner)) {
			serverOwner = p.getStableId();
			server.setText(p.getServerIp());
			server.setFocused(false);
		}

		boolean dirty = !server.getText().trim().equals(p.getServerIp());
		field(server, TranslateText.PROFILE_SERVER_EXAMPLE.getText(), inX, y + 108, inWidth - (dirty ? 26 : 0), 20);
		serverVisible = true;

		if(dirty) {
			iconButton("server-save", Icon.CHECK, TranslateText.PROFILE_SAVE.getText(), inX + inWidth - 20, y + 108, Style.PRIMARY, !working, false, this::commitServer);
		}

		float tileY = y + height - 44, tileWidth = (inWidth - 12) / 4, step = tileWidth + 4;
		boolean favorite = p.getType() == ProfileType.FAVORITE;

		tile("favorite", favorite ? Icon.STAR_FILL : Icon.STAR, favorite ? TranslateText.PROFILE_UNFAVORITE.getText() : TranslateText.PROFILE_FAVORITE.getText(), inX, tileY, tileWidth,
				favorite ? STAR : null, false, !working,
				() -> manager().updateMetadata(p, favorite ? ProfileType.ALL : ProfileType.FAVORITE, p.getIcon(), p.getServerIp()));
		tile("copy", Icon.COPY, TranslateText.PROFILE_DUPLICATE.getText(), inX + step, tileY, tileWidth, null, false, !working, () -> openModal(Modal.COPY, p));
		tile("export", Icon.SAVE, TranslateText.PROFILE_EXPORT.getText(), inX + step * 2, tileY, tileWidth, null, false, !working, () -> choose(true, p));
		tile("delete", p.isDefault() ? Icon.REFRESH : Icon.TRASH, p.isDefault() ? TranslateText.PROFILE_RESET.getText() : TranslateText.PROFILE_DELETE.getText(), inX + step * 3, tileY, tileWidth,
				palette.getMaterialRed(), true, !working, () -> openModal(p.isDefault() ? Modal.RESET : Modal.DELETE, p));
	}


    private void drawFooter() {
        float x=getX()+10,y=getY()+getHeight()-21;
        nvg.drawText(manager().getStatus(),x,y+3,manager().isFailed()?palette.getMaterialRed():mutedColor(),8.5F,Fonts.MEDIUM);
        if(manager().isFailed())hit(x,y,150,14,()->manager().save());
    }

	private void drawModal() {
		float value = modalAnimation.getValue();

		nvg.drawRoundedRectVarying(getX(), getY(), getWidth(), getHeight(), 0, 0, 0, 12, new Color(0, 0, 0, (int) (120 * value)));
		layoutModal(264, modal == Modal.NEW || modal == Modal.COPY ? 132 : 116);

		nvg.save();
		nvg.scale(modalX, modalY, modalW, modalH, 0.94F + 0.06F * value);
		nvg.setAlpha(Math.max(0.01F, value));
		nvg.drawShadow(modalX, modalY, modalW, modalH, 10);
		nvg.drawRoundedRect(modalX, modalY, modalW, modalH, 10, palette.getBackgroundColor(ColorType.DARK));
		drawPrompt(modalX, modalY, modalW, modalH);

		nvg.restore();
	}

	private void layoutModal(float width, float height) {
		modalW = width;
		modalH = height;
		modalX = getX() + (getWidth() - width) / 2;
		modalY = getY() + (getHeight() - height) / 2;
	}

	private void drawPrompt(float x, float y, float width, float height) {
		float inX = x + 16, inWidth = width - 32, buttonY = y + height - 36, buttonWidth = 78;
		boolean naming = modal == Modal.NEW || modal == Modal.COPY;
		String confirm;
		boolean enabled;

		if(naming) {
			nvg.drawText(modal == Modal.NEW ? TranslateText.PROFILE_NEW_PROFILE.getText() : TranslateText.PROFILE_COPY.getText(), inX, y + 15, textColor(), 12, Fonts.MEDIUM);
			String hint = modal == Modal.NEW ? TranslateText.PROFILE_NEW_HINT.getText() : String.format(TranslateText.PROFILE_COPY_HINT.getText(), target.getName());
			nvg.drawText(limit(hint, 8, Fonts.REGULAR, inWidth), inX, y + 33, mutedColor(), 8, Fonts.REGULAR);

			field(name, TranslateText.PROFILE_NAME.getText(), inX, y + 50, inWidth, 20);
			String problem = name.getText().trim().isEmpty() ? null : nameProblem(name.getText());

			if(problem != null) {
				nvg.drawText(limit(problem, 7.5F, Fonts.REGULAR, inWidth), inX, y + 75, palette.getMaterialRed(), 7.5F, Fonts.REGULAR);
			}

			confirm = modal == Modal.NEW ? TranslateText.PROFILE_CREATE.getText() : TranslateText.PROFILE_DUPLICATE.getText();
			enabled = !manager().isWorking() && nameProblem(name.getText()) == null;
		} else {
			String heading, subject, hint, icon;

			if(modal == Modal.RESET) {
				heading = TranslateText.PROFILE_RESET_CONFIRM.getText();
				subject = target.getName();
				hint = TranslateText.PROFILE_RESET_HINT.getText();
				icon = Icon.REFRESH;
				confirm = TranslateText.PROFILE_RESET.getText();
				enabled = !manager().isWorking();
			} else {
				heading = TranslateText.PROFILE_DELETE_CONFIRM.getText();
				subject = target.getName();
				hint = TranslateText.PROFILE_DELETE_HINT.getText();
				icon = Icon.TRASH;
				confirm = TranslateText.PROFILE_DELETE.getText();
				enabled = !manager().isWorking();
			}

			nvg.drawRoundedRect(inX, y + 16, 28, 28, 8, ColorUtils.applyAlpha(palette.getMaterialRed(), 40));
			nvg.drawCenteredText(icon, inX + 14, y + 23.5F, palette.getMaterialRed(), 13, Fonts.ICON);
			nvg.drawText(limit(heading, 11, Fonts.MEDIUM, inWidth - 38), inX + 38, y + 16, textColor(), 11, Fonts.MEDIUM);
			nvg.drawText(limit(subject, 8.5F, Fonts.REGULAR, inWidth - 38), inX + 38, y + 32, mutedColor(), 8.5F, Fonts.REGULAR);

			List<String> lines = wrap(hint, inWidth, 8);
			for(int i = 0; i < Math.min(2, lines.size()); i++) {
				nvg.drawText(lines.get(i), inX, y + 54 + i * 11, mutedColor(), 8, Fonts.REGULAR);
			}
		}

		button("modal-cancel", TranslateText.PROFILE_CANCEL.getText(), null, x + width - 16 - buttonWidth * 2 - 6, buttonY, buttonWidth, 22, Style.CARD, true, this::closeModal);
		button("modal-confirm", confirm, null, x + width - 16 - buttonWidth, buttonY, buttonWidth, 22, naming ? Style.PRIMARY : Style.DANGER, enabled, this::confirmModal);
	}



	// ------------------------------------------------------------------ actions



	private void clickLocal(Profile p) {
		long now = System.currentTimeMillis();
		boolean twice = p.getStableId().equals(lastClick) && now - lastClickTime < 400;

		lastClick = twice ? null : p.getStableId();
		lastClickTime = now;
		selectLocal(p);

		if(twice) {
			use(p);
		}
	}

	private void selectLocal(Profile p) {
		if(p.getStableId().equals(selectedLocal)) {
			return;
		}

		commitServer();
		cancelRename();
		selectedLocal = p.getStableId();
	}


    private void moveSelection(int direction) {
        List<Profile> list=localProfiles();if(list.isEmpty())return;
        int index=0;
        for(int i=0;i<list.size();i++)if(list.get(i).getStableId().equals(selectedLocal))index=Math.max(0,Math.min(list.size()-1,i+direction));
        selectLocal(list.get(index));reveal(index);
    }

	private void reveal(int index) {
		float top = index * ROW_STRIDE, offset = -listScroll.getValue();

		if(top < offset) {
			listScroll.setScrollPosition(-top);
		} else if(top + ROW_HEIGHT > offset + listHeight) {
			listScroll.setScrollPosition(-(top + ROW_HEIGHT - listHeight));
		}
	}

	private void use(Profile p) {
		if(p == null || p == manager().getCurrent() || manager().isWorking()) {
			return;
		}

		switching = p.getStableId();
		manager().use(p);
	}

	private void startRename(Profile p) {
		if(p == null || p.isDefault() || manager().isWorking()) {
			return;
		}

		commitServer();
		blur();
		renaming = true;
		renameError = null;
		rename.setText(p.getName());
		rename.setFocused(true);
	}

	private void finishRename(boolean leaving) {
		Profile p = selected();

		if(!renaming || p == null) {
			cancelRename();
			return;
		}

		String value = rename.getText().trim();

		if(value.equals(p.getName())) {
			cancelRename();
			return;
		}

		String problem = nameProblem(rename.getText());

		if(problem != null || manager().isWorking()) {
			if(leaving) {
				cancelRename();
			} else {
				renameError = problem;
				rename.setFocused(true);
			}
			return;
		}

		manager().rename(p, value);
		cancelRename();
	}

	private void cancelRename() {
		renaming = false;
		renameError = null;
		rename.setFocused(false);
	}

	private void commitServer() {
		Profile p = profile(serverOwner);

		if(p == null || manager().isWorking()) {
			return;
		}

		String value = server.getText().trim();

		if(!value.equals(p.getServerIp())) {
			manager().updateMetadata(p, p.getType(), p.getIcon(), value);
		}
	}

	/** Mirrors {@link ProfileManager#validName(String)} without marking the manager as failed. */
	private String nameProblem(String raw) {
		String value = raw.trim();

		if(value.isEmpty() || value.length() > 80 || raw.matches("(?s).*\\p{Cntrl}.*")) {
			return TranslateText.PROFILE_NAME_INVALID.getText();
		}

		for(Profile p : manager().getProfiles()) {
			if(p.getName().equalsIgnoreCase(value)) {
				return TranslateText.PROFILE_NAME_TAKEN.getText();
			}
		}

		return null;
	}

	private void openModal(Modal next, Profile profile) {
		commitServer();
		cancelRename();
		blur();

		modal = next;
		target = profile;
		modalAnimation.setValue(0);
		pendingSelect = null;

		if(next == Modal.NEW) {
			name.setText("");
		}

		if(next == Modal.COPY) {
			String base = profile.getName() + " " + TranslateText.PROFILE_COPIED.getText(), candidate = base;
			for(int i = 2; nameProblem(candidate) != null && i < 100; i++) {
				candidate = base + " " + i;
			}
			name.setText(candidate.length() > 80 ? candidate.substring(0, 80) : candidate);
		}

		if(next == Modal.NEW || next == Modal.COPY) {
			name.setFocused(true);
		}
	}



	private void confirmModal() {
		switch(modal) {
			case NEW:
			case COPY:
				if(manager().isWorking() || nameProblem(name.getText()) != null) {
					return;
				}
				String value = name.getText().trim();
				if(modal == Modal.NEW) {
					manager().create(value);
				} else {
					manager().create(value, target);
				}
				closeModal();
				pendingSelect = value;
				return;
			case DELETE:
				if(manager().isWorking()) {
					return;
				}
				manager().delete(target);
				break;
			case RESET:
				if(manager().isWorking()) {
					return;
				}
				manager().resetDefault();
				break;
			default:
				return;
		}

		closeModal();
	}

	private void closeModal() {
		modal = Modal.NONE;
		name.setFocused(false);
		title.setFocused(false);
		description.setFocused(false);
	}

	private void blur() {
		for(CompTextBox box : Arrays.asList(search, name, rename, server, title, description)) {
			box.setFocused(false);
		}
	}

	private Profile profile(String id) {
		if(id == null) {
			return null;
		}

		for(Profile p : manager().getProfiles()) {
			if(id.equals(p.getStableId())) {
				return p;
			}
		}

		return null;
	}

	private Profile selected() {
		return profile(selectedLocal);
	}



	private void choose(boolean export, Profile profile) {
		SwingUtilities.invokeLater(() -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("JSON", "json"));

			if(export) {
				chooser.setSelectedFile(new File(profile.getName().replaceAll("[\\\\/:*?\"<>|]", "_") + ".json"));
			}

			int result = export ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);

			if(result == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();

				if(export && !file.getName().toLowerCase(Locale.ROOT).endsWith(".json")) {
					file = new File(file.getParentFile(), file.getName() + ".json");
				}

				File selected = file;
				mc.addScheduledTask(() -> {
					if(export) {
						manager().export(profile, selected);
					} else {
						manager().importFile(selected);
					}
				});
			}
		});
	}



	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(mouseButton != 0) {
			return;
		}

		if(modal != Modal.NONE) {
			if(modal == Modal.NEW || modal == Modal.COPY) {
				name.mouseClicked(mouseX, mouseY, mouseButton);
			}

			boolean inside = MouseUtils.isInside(mouseX, mouseY, modalX, modalY, modalW, modalH);

			if(!runHit(mouseX, mouseY) && !inside) {
				closeModal();
			}

			return;
		}

		if(renaming && !inside(rename, mouseX, mouseY)) {
			finishRename(true);
		}

		if(server.isFocused() && !inside(server, mouseX, mouseY)) {
			commitServer();
		}

		search.mouseClicked(mouseX, mouseY, mouseButton);

		if(serverVisible && true) {
			server.mouseClicked(mouseX, mouseY, mouseButton);
		}

		if(renaming) {
			rename.mouseClicked(mouseX, mouseY, mouseButton);
		}

		runHit(mouseX, mouseY);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		boolean enter = keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER;

		if(keyCode == Keyboard.KEY_ESCAPE) {
			if(escape()) {
				setCanClose(false);
			}
			return;
		}

		if(modal != Modal.NONE) {
			modalKey(typedChar, keyCode, enter);
			return;
		}

		if(renaming && rename.isFocused()) {
			if(enter) {
				finishRename(false);
			} else {
				rename.keyTyped(typedChar, keyCode);
				renameError = null;
			}
			return;
		}

		if(server.isFocused()) {
			if(enter) {
				commitServer();
				server.setFocused(false);
			} else {
				server.keyTyped(typedChar, keyCode);
			}
			return;
		}

		if(search.isFocused()) {
			if(enter || keyCode == Keyboard.KEY_DOWN) {
				search.setFocused(false);
			} else {
				search.keyTyped(typedChar, keyCode);
			}
			return;
		}

		boolean control = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

		switch(keyCode) {
			case Keyboard.KEY_UP:
				moveSelection(-1);
				return;
			case Keyboard.KEY_DOWN:
				moveSelection(1);
				return;
			case Keyboard.KEY_F2:
				if(true) {
					startRename(selected());
				}
				return;
			case Keyboard.KEY_DELETE:
				Profile p = selected();
				if(true && p != null && !p.isDefault() && !manager().isWorking()) {
					openModal(Modal.DELETE, p);
				}
				return;
			default:
				break;
		}

		if(enter) {
			if(true) {
				use(selected());
			}
			return;
		}

		if(control && keyCode == Keyboard.KEY_F) {
			search.setFocused(true);
			return;
		}

		// Typing anywhere on the page starts a search.
		if(!control && typedChar != ' ' && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
			search.setFocused(true);
			search.keyTyped(typedChar, keyCode);
		}
	}

	private boolean escape() {
		if(modal != Modal.NONE) {
			if(true) {
				closeModal();
			}
			return true;
		}

		if(renaming) {
			cancelRename();
			return true;
		}

		if(server.isFocused()) {
			Profile p = profile(serverOwner);
			if(p != null) {
				server.setText(p.getServerIp());
			}
			server.setFocused(false);
			return true;
		}

		if(search.isFocused()) {
			if(search.getText().isEmpty()) {
				search.setFocused(false);
			} else {
				search.setText("");
			}
			return true;
		}

		return false;
	}


    private void modalKey(char typedChar,int keyCode,boolean enter) {
        if(enter)confirmModal();
        else if(modal==Modal.NEW||modal==Modal.COPY)name.keyTyped(typedChar,keyCode);
    }

	private boolean runHit(int mouseX, int mouseY) {
		for(int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);

			if(MouseUtils.isInside(mouseX, mouseY, hit.x, hit.y, hit.width, hit.height)) {
				hit.action.run();
				return true;
			}
		}

		return false;
	}

	private static boolean inside(CompTextBox box, int mouseX, int mouseY) {
		return MouseUtils.isInside(mouseX, mouseY, box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}

	private void beginList(float x, float y, float width, float viewport, int count) {
		float content = count * ROW_STRIDE - (ROW_STRIDE - ROW_HEIGHT), max = Math.max(0, content - viewport);

		listHeight = viewport;
		listScroll.setMaxScroll(max);

		if(!blocked && MouseUtils.isInside(mx, my, x, y, width, viewport)) {
			listScroll.onScroll();
		}

		listScroll.onAnimation();

		if(listScroll.getValue() < -max - 0.5F) {
			listScroll.setScrollPosition(-max);
		}

		nvg.save();
		nvg.intersectScissor(x, y, width, viewport);
	}

	private void endList(float x, float y, float width, float viewport, int count) {
		nvg.restore();

		float content = count * ROW_STRIDE - (ROW_STRIDE - ROW_HEIGHT);

		if(content > viewport) {
			float bar = Math.max(18, viewport * viewport / content), max = content - viewport;
			float barY = y + Math.min(1, -listScroll.getValue() / max) * (viewport - bar);
			nvg.drawRoundedRect(x + width + 2.5F, barY, 2.5F, bar, 1.25F, faintColor());
		}
	}

	private void drawRow(String id, float x, float y, float width, boolean selected, float top, float viewport) {
		boolean hovered = hover(x, y, width, ROW_HEIGHT) && MouseUtils.isInside(mx, my, x, top, width, viewport);
		SimpleAnimation animation = animation(id);
		animation.setAnimation(hovered || selected ? 1 : 0, 16);

		nvg.drawRoundedRect(x, y, width, ROW_HEIGHT, 6, palette.getBackgroundColor(ColorType.DARK));

		if(animation.getValue() > 0.01F) {
			nvg.drawRoundedRect(x, y, width, ROW_HEIGHT, 6, ColorUtils.applyAlpha(accent.getColor1(), (int) (animation.getValue() * (selected ? 36 : 20))));
		}

		if(selected) {
			nvg.drawGradientOutlineRoundedRect(x, y, width, ROW_HEIGHT, 6, 1, accent.getColor1(), accent.getColor2());
		}
	}

	private void drawAvatar(String text, float x, float y, float size, boolean available) {
		if(!available) {
			nvg.drawRoundedRect(x, y, size, size, size / 4, palette.getBackgroundColor(ColorType.NORMAL));
			nvg.drawCenteredText(Icon.X, x + size / 2, y + size * 0.3F, mutedColor(), size * 0.4F, Fonts.ICON);
			return;
		}

		String initial = text.isEmpty() ? "?" : text.substring(0, text.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
		nvg.drawGradientRoundedRect(x, y, size, size, size / 4, accent.getColor1(), accent.getColor2());
		nvg.drawCenteredText(initial, x + size / 2, y + size * 0.27F, Color.WHITE, size * 0.46F, Fonts.MEDIUM);
	}

	private void button(String id, String label, String icon, float x, float y, float width, float height, Style style, boolean enabled, Runnable action) {
		boolean hovered = enabled && hover(x, y, width, height);
		SimpleAnimation animation = animation(id);
		animation.setAnimation(hovered ? 1 : 0, 16);
		float value = animation.getValue();
		Color foreground;

		switch(style) {
			case PRIMARY:
				if(enabled) {
					nvg.drawGradientRoundedRect(x, y, width, height, 6, accent.getColor1(), accent.getColor2());
				} else {
					nvg.drawGradientRoundedRect(x, y, width, height, 6, ColorUtils.applyAlpha(accent.getColor1(), 90), ColorUtils.applyAlpha(accent.getColor2(), 90));
				}
				if(value > 0.01F) {
					nvg.drawRoundedRect(x, y, width, height, 6, new Color(255, 255, 255, (int) (value * 40)));
				}
				foreground = enabled ? Color.WHITE : new Color(255, 255, 255, 150);
				break;
			case DANGER:
				nvg.drawRoundedRect(x, y, width, height, 6, enabled ? palette.getMaterialRed() : ColorUtils.applyAlpha(palette.getMaterialRed(), 90));
				if(value > 0.01F) {
					nvg.drawRoundedRect(x, y, width, height, 6, new Color(255, 255, 255, (int) (value * 40)));
				}
				foreground = enabled ? Color.WHITE : new Color(255, 255, 255, 150);
				break;
			case DANGER_SOFT:
				nvg.drawRoundedRect(x, y, width, height, 6, palette.getBackgroundColor(ColorType.NORMAL));
				if(value > 0.01F) {
					nvg.drawRoundedRect(x, y, width, height, 6, ColorUtils.applyAlpha(palette.getMaterialRed(), (int) (value * 40)));
				}
				foreground = enabled ? palette.getMaterialRed() : faintColor();
				break;
			default:
				nvg.drawRoundedRect(x, y, width, height, 6, palette.getBackgroundColor(style == Style.PAGE ? ColorType.DARK : ColorType.NORMAL));
				if(value > 0.01F) {
					nvg.drawRoundedRect(x, y, width, height, 6, ColorUtils.applyAlpha(accent.getColor1(), (int) (value * 36)));
				}
				foreground = enabled ? textColor() : faintColor();
				break;
		}

		if(label != null && !label.isEmpty()) {
			label = limit(label, 9, Fonts.MEDIUM, width - 12 - (icon != null ? 14 : 0));
		}

		drawContent(icon, label, x + width / 2, y + height / 2, foreground, 9);

		if(enabled) {
			hit(x, y, width, height, action);
		}
	}

	private void iconButton(String id, String icon, String tip, float x, float y, Style style, boolean enabled, boolean spinning, Runnable action) {
		button(id, null, spinning ? null : icon, x, y, 20, 20, style, enabled, action);

		if(spinning) {
			spinner(x + 10, y + 10, 4.5F, textColor());
		}

		if(tip != null && hover(x, y, 20, 20)) {
			tooltip = tip;
		}
	}

	private void iconLink(String id, String icon, String tip, float x, float y, float size, boolean enabled, Runnable action) {
		boolean hovered = enabled && hover(x - 3, y - 3, size + 6, size + 6);

		nvg.drawText(icon, x, y, hovered ? accent.getColor1() : enabled ? mutedColor() : faintColor(), size, Fonts.ICON);

		if(hovered) {
			tooltip = tip;
		}

		if(enabled) {
			hit(x - 3, y - 3, size + 6, size + 6, action);
		}
	}

	private void tile(String id, String icon, String label, float x, float y, float width, Color tint, boolean danger, boolean enabled, Runnable action) {
		float height = 32;
		boolean hovered = enabled && hover(x, y, width, height);
		SimpleAnimation animation = animation("tile-" + id);
		animation.setAnimation(hovered ? 1 : 0, 16);

		nvg.drawRoundedRect(x, y, width, height, 6, palette.getBackgroundColor(ColorType.NORMAL));

		if(animation.getValue() > 0.01F) {
			Color glow = danger ? palette.getMaterialRed() : accent.getColor1();
			nvg.drawRoundedRect(x, y, width, height, 6, ColorUtils.applyAlpha(glow, (int) (animation.getValue() * 40)));
		}

		Color iconColor = !enabled ? faintColor() : tint != null ? tint : textColor();
		Color labelColor = !enabled ? faintColor() : danger ? palette.getMaterialRed() : mutedColor();

		nvg.drawCenteredText(icon, x + width / 2, y + 5, iconColor, 11, Fonts.ICON);
		nvg.drawCenteredText(limit(label, 7.5F, Fonts.REGULAR, width - 2), x + width / 2, y + 19.5F, labelColor, 7.5F, Fonts.REGULAR);

		if(enabled) {
			hit(x, y, width, height, action);
		}
	}

	private void chip(String id, String label, float x, float y, float width, boolean on, boolean enabled, Runnable action) {
		float height = 20;
		boolean hovered = enabled && hover(x, y, width, height);

		if(on) {
			nvg.drawGradientRoundedRect(x, y, width, height, 10, accent.getColor1(), accent.getColor2());
		} else {
			nvg.drawRoundedRect(x, y, width, height, 10, palette.getBackgroundColor(ColorType.NORMAL));
		}

		if(hovered) {
			nvg.drawOutlineRoundedRect(x, y, width, height, 10, 0.8F, on ? Color.WHITE : accent.getColor1());
		}

		Color color = on ? Color.WHITE : enabled ? textColor() : faintColor();
		drawContent(on ? Icon.CHECK : null, limit(label, 8.5F, Fonts.MEDIUM, width - (on ? 26 : 12)), x + width / 2, y + height / 2, color, 8.5F);

		if(enabled) {
			hit(x, y, width, height, action);
		}
	}

	private void stat(String value, String label, float x, float y, float width) {
		nvg.drawRoundedRect(x, y, width, 34, 6, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawText(value, x + 10, y + 5, textColor(), 13, Fonts.MEDIUM);
		nvg.drawText(limit(label, 7.5F, Fonts.REGULAR, width - 20), x + 10, y + 21.5F, mutedColor(), 7.5F, Fonts.REGULAR);
	}

	private void drawNote(String icon, String text, float x, float y, float width, Color color) {
		nvg.drawText(icon, x, y + 0.5F, color, 8.5F, Fonts.ICON);
		nvg.drawText(limit(text, 8, Fonts.REGULAR, width - 14), x + 13, y, color, 8, Fonts.REGULAR);
	}

	private void drawContent(String icon, String label, float centerX, float centerY, Color color, float size) {
		float iconSize = size + 1;
		float iconWidth = icon == null ? 0 : nvg.getTextWidth(icon, iconSize, Fonts.ICON);
		float labelWidth = label == null || label.isEmpty() ? 0 : nvg.getTextWidth(label, size, Fonts.MEDIUM);
		float gap = iconWidth > 0 && labelWidth > 0 ? 4 : 0;
		float startX = centerX - (iconWidth + gap + labelWidth) / 2;

		if(icon != null) {
			nvg.drawText(icon, startX, centerY - iconSize / 2, color, iconSize, Fonts.ICON);
		}

		if(labelWidth > 0) {
			nvg.drawText(label, startX + iconWidth + gap, centerY - size / 2, color, size, Fonts.MEDIUM);
		}
	}

	private void field(CompTextBox box, String placeholder, float x, float y, float width, float height) {
		box.setDefaultText(placeholder);
		box.setPosition(x, y, width, height);
		box.draw(mx, my, ticks);

		if(box.isFocused()) {
			nvg.drawOutlineRoundedRect(x, y, width, height, 6, 1, accent.getColor1());
		}
	}

	private void label(String text, float x, float y) {
		nvg.drawText(text, x, y, mutedColor(), 8, Fonts.MEDIUM);
	}

	private void counter(String text, float right, float y) {
		nvg.drawText(text, right - nvg.getTextWidth(text, 7.5F, Fonts.REGULAR), y + 0.5F, faintColor(), 7.5F, Fonts.REGULAR);
	}

	private int centeredLines(String text, float centerX, float y, float width, float size, Color color, int max) {
		List<String> lines = wrap(text, width, size);
		int count = Math.min(max, lines.size());

		for(int i = 0; i < count; i++) {
			nvg.drawCenteredText(lines.get(i), centerX, y + i * (size + 4), color, size, Fonts.REGULAR);
		}

		return count;
	}

	private void spinner(float centerX, float centerY, float radius, Color color) {
		float angle = (System.currentTimeMillis() % 900L) / 900F * 360F;
		nvg.drawArc(centerX, centerY, radius, angle, angle + 270, Math.max(1, radius / 3), color);
	}

	private void drawTooltip() {
		if(tooltip == null || tooltip.isEmpty()) {
			return;
		}

		float width = nvg.getTextWidth(tooltip, 8, Fonts.REGULAR) + 12, height = 16;
		float x = Math.max(getX() + 4, Math.min(mx - width / 2, getX() + getWidth() - 4 - width));
		float y = my + 14;

		if(y + height > getY() + getHeight() - 4) {
			y = my - height - 8;
		}

		nvg.drawRoundedRect(x, y, width, height, 4, new Color(15, 15, 18, 230));
		nvg.drawText(tooltip, x + 6, y + 4, Color.WHITE, 8, Fonts.REGULAR);
	}

	private List<String> wrap(String text, float width, float size) {
		List<String> lines = new ArrayList<String>();

		if(text == null || text.isEmpty()) {
			return lines;
		}

		for(String paragraph : text.split("\n")) {
			StringBuilder line = new StringBuilder();

			for(int i = 0; i < paragraph.length(); i++) {
				char c = paragraph.charAt(i);

				if(line.length() > 0 && nvg.getTextWidth(line.toString() + c, size, Fonts.REGULAR) > width) {
					int space = line.lastIndexOf(" ");

					if(c != ' ' && space > 0) {
						lines.add(line.substring(0, space));
						line.delete(0, space + 1);
					} else {
						lines.add(line.toString());
						line.setLength(0);
					}

					if(c == ' ') {
						continue;
					}
				}

				line.append(c);
			}

			if(line.length() > 0) {
				lines.add(line.toString());
			}
		}

		return lines;
	}

	private String limit(String value, float size, Font font, float width) {
		return nvg.getLimitText(value == null ? "" : value, size, font, Math.max(0, width));
	}

	private SimpleAnimation animation(String id) {
		return animations.computeIfAbsent(id, key -> new SimpleAnimation());
	}

	private boolean hover(float x, float y, float width, float height) {
		return !blocked && MouseUtils.isInside(mx, my, x, y, width, height);
	}

	private void hit(float x, float y, float width, float height, Runnable action) {
		if(blocked) {
			return;
		}

		hits.add(new Hit(x, y, width, height, action));
		MouseCursor.pointer(mx, my, x, y, width, height);
	}

	private void listHit(float x, float rowY, float width, float top, float viewport, Runnable action) {
		float from = Math.max(rowY, top), to = Math.min(rowY + ROW_HEIGHT, top + viewport);

		if(to > from) {
			hit(x, from, width, to - from, action);
		}
	}

	private Color textColor() {
		return palette.getFontColor(ColorType.DARK);
	}

	private Color mutedColor() {
		return palette.getFontColor(ColorType.DARK, 150);
	}

	private Color faintColor() {
		return palette.getFontColor(ColorType.DARK, 80);
	}

	private static final class Hit {

		final float x, y, width, height;
		final Runnable action;

		Hit(float x, float y, float width, float height, Runnable action) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.action = action;
		}
	}
}
