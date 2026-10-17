import java.lang.reflect.Field;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.impl.FarCameraMod;
import me.eldodebug.soar.management.mods.impl.FreelookMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.ui.comp.impl.CompSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import sun.misc.Unsafe;

/** Exercises camera events and the real slider/menu preview lifecycle without a game window. */
public final class FreelookSmoke {
    private static final Unsafe UNSAFE;
    static {
        try { UNSAFE = (Unsafe) field(Unsafe.class, "theUnsafe").get(null); }
        catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    private static Field field(Class<?> owner, String name) throws Exception {
        Field result = owner.getDeclaredField(name); result.setAccessible(true); return result;
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static final class RecordingRenderer extends RenderGlobal {
        private RecordingRenderer(Minecraft mc) { super(mc); }
        @Override public void setDisplayListEntitiesDirty() {}
    }
    private static final class RecordingProfile extends ProfileManager {
        private int saves;
        @Override public void save() { saves++; }
    }
    private static final class RecordingCategory extends Category {
        private RecordingCategory(GuiModMenu parent) { super(parent, TranslateText.MODULE, "", false); }
    }
    private static EventCameraRotation camera() {
        EventCameraRotation event = new EventCameraRotation(90, 30, 0, 4);
        event.call(); return event;
    }
    private static void endDrag(GuiModMenu menu, FreelookMod mod, int perspective) {
        menu.mouseReleased(900, 70, 0);
        check(!mod.isDistancePreview(), "Mouse release left preview running");
        check(fieldValue(menu, "distancePreviewSlider") == null, "Settings panel remained hidden");
        check(Minecraft.getMinecraft().gameSettings.thirdPersonView == perspective, "Previous perspective was not restored");
    }
    private static Object fieldValue(Object owner, String name) {
        try { return field(owner.getClass(), name).get(owner); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    public static void main(String[] args) throws Exception {
        Field minecraft = field(Minecraft.class, "theMinecraft");
        Object oldMinecraft = minecraft.get(null);
        Minecraft mc = (Minecraft) UNSAFE.allocateInstance(Minecraft.class);
        minecraft.set(null, mc);
        Soar soar = Soar.getInstance();
        Field mods = field(Soar.class, "modManager"), events = field(Soar.class, "eventManager");
        Field profiles = field(Soar.class, "profileManager");
        Object oldMods = mods.get(soar), oldEvents = events.get(soar), oldProfiles = profiles.get(soar);
        try {
            mods.set(soar, new ModManager());
            events.set(soar, new EventManager());
            RecordingProfile profile = (RecordingProfile) UNSAFE.allocateInstance(RecordingProfile.class);
            profiles.set(soar, profile);
            mc.gameSettings = (GameSettings) UNSAFE.allocateInstance(GameSettings.class);
            mc.renderGlobal = (RecordingRenderer) UNSAFE.allocateInstance(RecordingRenderer.class);
            FreelookMod mod = new FreelookMod();
            check(mod.getDistanceSetting().getValue() == 4 && mod.getDistanceSetting().getMinValue() == 0
                    && mod.getDistanceSetting().getMaxValue() == 15,
                    "Camera distance must default to 4 within the 0-15 slider range");
            mod.getDistanceSetting().setValue(50);
            check(mod.getDistanceSetting().getValue() == 15, "An old saved distance exceeded the new slider range");
            mod.getDistanceSetting().reset();
            KeybindSetting key = (KeybindSetting) field(FreelookMod.class, "keybindSetting").get(mod);
            check(key.getKeyCode() == Keyboard.KEY_C, "Freelook must default to C");
            key.setKeyCode(Keyboard.KEY_V); key.reset();
            check(key.getKeyCode() == Keyboard.KEY_C, "Reset did not restore the C shortcut");
            check(!mod.beginDistancePreview(), "Preview started without a world");
            mc.theWorld = (WorldClient) UNSAFE.allocateInstance(WorldClient.class);
            mc.thePlayer = (EntityPlayerSP) UNSAFE.allocateInstance(EntityPlayerSP.class);
            mc.thePlayer.rotationYaw = 12; mc.thePlayer.rotationPitch = 7;
            field(Minecraft.class, "renderViewEntity").set(mc, mc.thePlayer);

            GuiModMenu menu = (GuiModMenu) UNSAFE.allocateInstance(GuiModMenu.class);
            field(GuiScreen.class, "mc").set(menu, mc);
            field(GuiModMenu.class, "currentCategory").set(menu, new RecordingCategory(menu));
            mc.currentScreen = menu;
            CompSlider slider = new CompSlider(100, 70, mod.getDistanceSetting(), 75);
            FarCameraMod far = new FarCameraMod(); far.setToggled(true);
            for (int perspective = 0; perspective < 3; perspective++) {
                mc.gameSettings.thirdPersonView = perspective;
                slider.mouseClicked(130, 70, 0);
                menu.beginFreelookDistancePreview(mod, slider, 0);
                check(mod.isDistancePreview() && !mod.isToggled(), "Preview changed the disabled module preference");
                check(mc.gameSettings.thirdPersonView == 3, "Preview did not enter freelook");
                mod.onTick(new EventTick());
                check(mc.gameSettings.thirdPersonView == 3, "Keydown mode stopped the slider preview");
                slider.updateDrag(-100);
                check(camera().getThirdPersonDistance() == 0, "Slider minimum was not applied to the camera");
                slider.updateDrag(140);
                EventCameraRotation event = camera();
                check(event.getThirdPersonDistance() == mod.getDistanceSetting().getValueFloat()
                        && event.getYaw() == 12 && event.getPitch() == 7, "Live distance or freelook rotation was overridden by Far Camera");
                endDrag(menu, mod, perspective);
                check(!slider.isDragging() && mod.getDistanceSetting().getValue() == 15, "Release position was lost or dragging remained active");
                check(camera().getThirdPersonDistance() == 15, "Temporary preview listener remained registered");
            }

            slider.mouseClicked(130, 70, 0); menu.beginFreelookDistancePreview(mod, slider, 0);
            menu.keyTyped('\0', Keyboard.KEY_ESCAPE);
            check(!mod.isDistancePreview() && !slider.isDragging() && mc.gameSettings.thirdPersonView == 2,
                    "Escape did not return to settings and restore the camera");

            slider.mouseClicked(130, 70, 0); menu.beginFreelookDistancePreview(mod, slider, 0);
            mc.theWorld = null; mc.thePlayer = null;
            menu.updateScreen();
            check(!mod.isDistancePreview() && mc.gameSettings.thirdPersonView == 2,
                    "Leaving the world left the preview active");
            mc.theWorld = (WorldClient) UNSAFE.allocateInstance(WorldClient.class);
            mc.thePlayer = (EntityPlayerSP) UNSAFE.allocateInstance(EntityPlayerSP.class);

            mod.setToggled(true);
            slider.mouseClicked(130, 70, 0); menu.beginFreelookDistancePreview(mod, slider, 0);
            menu.onGuiClosed();
            check(mod.isToggled() && !mod.isDistancePreview() && mc.gameSettings.thirdPersonView == 2 && profile.saves == 1,
                    "Closing the menu did not restore the camera, save settings or retain enabled preference");

            ComboSetting mode = (ComboSetting) field(FreelookMod.class, "modeSetting").get(mod);
            mode.setOption(mode.getOptionByNameKey(TranslateText.TOGGLE.getKey()));
            field(FreelookMod.class, "toggled").setBoolean(mod, true);
            mod.onTick(new EventTick());
            check(mc.gameSettings.thirdPersonView == 3, "Toggle freelook did not start");
            mod.beginDistancePreview(); mod.endDistancePreview();
            check(mc.gameSettings.thirdPersonView == 3, "Preview stopped existing toggle freelook");
            mod.setToggled(false);
            check(mc.gameSettings.thirdPersonView == 2, "Disabling freelook left the camera active");
            mod.getDistanceSetting().reset();
            check(mod.getDistanceSetting().getValue() == 4, "Reset did not restore vanilla camera distance");
            System.out.println("PASS: vanilla default, real-time slider distance, Far Camera priority, temporary/enabled preview, camera restoration, release/Escape/close/world-exit cleanup and saved preference");
        } finally {
            mods.set(soar, oldMods); events.set(soar, oldEvents); profiles.set(soar, oldProfiles);
            minecraft.set(null, oldMinecraft);
        }
    }
}
