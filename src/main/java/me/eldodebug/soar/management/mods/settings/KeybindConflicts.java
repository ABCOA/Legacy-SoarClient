package me.eldodebug.soar.management.mods.settings;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.notification.NotificationManager;
import me.eldodebug.soar.management.notification.NotificationType;
import org.lwjgl.input.Keyboard;

import java.util.*;

public final class KeybindConflicts {
    private static final Set<String> warnedActiveBindings = new HashSet<String>();
    private KeybindConflicts() {}

    private static List<KeybindSetting> find(int keyCode, boolean enabledOnly, KeybindSetting excluded) {
        List<KeybindSetting> result = new ArrayList<KeybindSetting>();
        if (keyCode <= Keyboard.KEY_NONE) return result;
        for (Setting setting : Soar.getInstance().getModManager().getSettings()) {
            if (setting instanceof KeybindSetting && setting != excluded) {
                KeybindSetting binding = (KeybindSetting) setting;
                if (binding.getKeyCode() == keyCode && (!enabledOnly || binding.getParent().isToggled())) result.add(binding);
            }
        }
        return result;
    }

    public static List<KeybindSetting> findFor(KeybindSetting setting) {
        return find(setting.getKeyCode(), false, setting);
    }

    public static String names(List<KeybindSetting> bindings) {
        Set<String> result = new LinkedHashSet<String>();
        for (KeybindSetting binding : bindings) {
            String name = binding.getParent().getName();
            if (name == null) name = binding.getParent().getNameKey();
            int sameParent = 0;
            for (KeybindSetting other : bindings) if (other.getParent() == binding.getParent()) sameParent++;
            if (sameParent > 1) name += " / " + binding.getName();
            result.add(name);
        }
        return String.join(", ", result);
    }

    public static String keyName(int keyCode) {
        String name = keyCode >= 0 && keyCode < Keyboard.KEYBOARD_SIZE ? Keyboard.getKeyName(keyCode) : null;
        return name == null ? Integer.toString(keyCode) : name;
    }

    public static void warnForBinding(KeybindSetting setting) {
        List<KeybindSetting> conflicts = findFor(setting);
        NotificationManager notifications = Soar.getInstance().getNotificationManager();
        if (conflicts.isEmpty() || notifications == null) return;
        notifications.post(TranslateText.KEYBIND_CONFLICT,
                String.format(Locale.ROOT, TranslateText.KEYBIND_CONFLICT_BIND_MESSAGE.getText(), keyName(setting.getKeyCode()), names(conflicts)),
                NotificationType.WARNING);
    }

    public static void warnForKey(int keyCode) {
        List<KeybindSetting> bindings = find(keyCode, true, null);
        NotificationManager notifications = Soar.getInstance().getNotificationManager();
        if (bindings.size() < 2 || notifications == null) return;
        List<String> identifiers = new ArrayList<String>();
        for (KeybindSetting binding : bindings) identifiers.add(binding.getParent().getNameKey() + "/" + binding.getNameKey());
        java.util.Collections.sort(identifiers);
        String signature = keyCode + ":" + String.join("|", identifiers);
        if (!warnedActiveBindings.add(signature)) return;
        notifications.post(TranslateText.KEYBIND_CONFLICT,
                String.format(Locale.ROOT, TranslateText.KEYBIND_CONFLICT_ACTIVE_MESSAGE.getText(), keyName(keyCode), names(bindings)),
                NotificationType.WARNING);
    }
}
