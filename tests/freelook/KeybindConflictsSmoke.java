import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.event.impl.EventZoomFov;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.impl.FreelookMod;
import me.eldodebug.soar.management.mods.impl.ZoomMod;
import me.eldodebug.soar.management.mods.settings.KeybindConflicts;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.notification.Notification;
import me.eldodebug.soar.management.notification.NotificationManager;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.ui.comp.impl.CompKeybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import org.lwjgl.input.Keyboard;
import sun.misc.Unsafe;

/** Verifies actual key assignment, notifications and independent camera/zoom behavior. */
public final class KeybindConflictsSmoke {
    private static final class RecordingRenderer extends RenderGlobal {
        private RecordingRenderer(Minecraft mc) { super(mc); }
        @Override public void setDisplayListEntitiesDirty() {}
    }
    private static Field field(Class<?> owner, String name) throws Exception {
        Field result = owner.getDeclaredField(name); result.setAccessible(true); return result;
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static float fov() {
        EventZoomFov event = new EventZoomFov(70); event.call(); return event.getFov();
    }
    public static void main(String[] args) throws Exception {
        Unsafe unsafe = (Unsafe) field(Unsafe.class, "theUnsafe").get(null);
        Field minecraft = field(Minecraft.class, "theMinecraft");
        Object previousMinecraft = minecraft.get(null);
        Minecraft mc = (Minecraft) unsafe.allocateInstance(Minecraft.class);
        mc.renderGlobal = (RecordingRenderer) unsafe.allocateInstance(RecordingRenderer.class);
        minecraft.set(null, mc);
        Soar soar = Soar.getInstance();
        Field mods = field(Soar.class, "modManager"), events = field(Soar.class, "eventManager");
        Field notices = field(Soar.class, "notificationManager");
        Object oldMods = mods.get(soar), oldEvents = events.get(soar), oldNotices = notices.get(soar);
        try {
            mods.set(soar, new ModManager()); events.set(soar, new EventManager());
            NotificationManager notifications = new NotificationManager(); notices.set(soar, notifications);
            @SuppressWarnings("unchecked")
            LinkedBlockingQueue<Notification> queue = (LinkedBlockingQueue<Notification>) field(NotificationManager.class, "notifications").get(notifications);
            Field translation = field(TranslateText.class, "text");
            translation.set(TranslateText.FREELOOK, "Freelook"); translation.set(TranslateText.ZOOM, "Zoom");
            translation.set(TranslateText.KEYBIND_CONFLICT, "Keybind conflict");
            translation.set(TranslateText.KEYBIND_CONFLICT_BIND_MESSAGE, "%s is also used by %s");
            translation.set(TranslateText.KEYBIND_CONFLICT_ACTIVE_MESSAGE, "%s is shared by %s");

            FreelookMod freelook = new FreelookMod(); ZoomMod zoom = new ZoomMod();
            freelook.setToggled(true); zoom.setToggled(true);
            KeybindSetting freelookKey = (KeybindSetting) field(FreelookMod.class, "keybindSetting").get(freelook);
            KeybindSetting zoomKey = (KeybindSetting) field(ZoomMod.class, "keybindSetting").get(zoom);
            check(freelookKey.getDefaultKeyCode() == Keyboard.KEY_C
                    && KeybindConflicts.findFor(freelookKey).size() == 1, "Shared C defaults were not detected");
            // Reproduce an existing profile that binds both actions to V.
            freelookKey.setKeyCode(Keyboard.KEY_V);
            check(KeybindConflicts.findFor(freelookKey).isEmpty(), "Distinct V/C shortcuts should not conflict");
            CompKeybind picker = new CompKeybind(75, zoomKey);
            picker.setBinding(true); picker.keyTyped('v', Keyboard.KEY_V);
            check(zoomKey.getKeyCode() == Keyboard.KEY_V && freelookKey.getKeyCode() == Keyboard.KEY_V,
                    "Conflict handling changed intentional shared key bindings");
            check(queue.size() == 1 && queue.peek().getMessageText().contains("V") && queue.peek().getMessageText().contains("Freelook"),
                    "Assigning an occupied key did not identify the conflicting module");
            check(KeybindConflicts.findFor(freelookKey).size() == 1, "Existing shared binding was not detected");
            queue.clear();
            KeybindConflicts.warnForKey(Keyboard.KEY_V);
            check(queue.size() == 1 && queue.peek().getMessageText().contains("Freelook") && queue.peek().getMessageText().contains("Zoom"),
                    "Active shared key warning did not identify both enabled actions");
            for (int i = 0; i < 20; i++) KeybindConflicts.warnForKey(Keyboard.KEY_V);
            check(queue.size() == 1, "Repeated key presses spammed conflict warnings");

            // A warning must not merge camera distance with FOV scaling.
            check(fov() == 70 && freelook.getDistanceSetting().getValue() == 4, "Conflict warning changed preview FOV or the vanilla default");
            field(FreelookMod.class, "active").setBoolean(freelook, true);
            zoom.resetFactor();
            EventCameraRotation camera = new EventCameraRotation(0, 0, 0, 15); camera.call();
            check(camera.getThirdPersonDistance() == 4 && fov() == 17.5F,
                    "Freelook distance and four-times zoom no longer act independently");
            field(FreelookMod.class, "active").setBoolean(freelook, false); zoom.setFactor(1);

            queue.clear(); freelook.setToggled(false);
            check(KeybindConflicts.findFor(zoomKey).size() == 1, "Potential disabled-module conflict vanished from settings");
            KeybindConflicts.warnForKey(Keyboard.KEY_V);
            check(queue.isEmpty(), "Disabled modules caused an active conflict notification");
            picker.setBinding(true); picker.keyTyped('\0', Keyboard.KEY_ESCAPE);
            check(zoomKey.getKeyCode() == Keyboard.KEY_NONE && queue.isEmpty() && KeybindConflicts.findFor(zoomKey).isEmpty(),
                    "Unbinding a key caused a conflict warning");
            picker.setBinding(true); picker.keyTyped('c', Keyboard.KEY_C);
            check(queue.isEmpty() && KeybindConflicts.findFor(freelookKey).isEmpty(), "Resolved bindings still conflict");
            Notification legacy = new Notification(TranslateText.KEYBIND_CONFLICT, TranslateText.KEYBIND_CONFLICT_BIND_MESSAGE, NotificationType.WARNING);
            check(legacy.getMessageText().equals(TranslateText.KEYBIND_CONFLICT_BIND_MESSAGE.getText()), "Existing translated notifications changed");
            System.out.println("PASS: occupied-key binding warning, existing conflict detection, active conflict names, no repeated warnings, disabled/unbound/resolved keys, preserved bindings and independent distance/FOV behavior");
        } finally {
            mods.set(soar, oldMods); events.set(soar, oldEvents); notices.set(soar, oldNotices);
            minecraft.set(null, previousMinecraft);
        }
    }
}
