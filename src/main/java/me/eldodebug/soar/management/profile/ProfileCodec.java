package me.eldodebug.soar.management.profile;

import com.google.gson.*;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.*;
import me.eldodebug.soar.management.language.*;
import me.eldodebug.soar.management.mods.*;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.management.mods.settings.impl.*;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.utils.ColorUtils;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;

final class ProfileCodec {
    private final BackgroundManager backgroundManager;
    ProfileCodec(BackgroundManager backgroundManager) { this.backgroundManager = backgroundManager; }

    JsonObject snapshot(String serverIp, ProfileType type, ProfileIcon icon) {
        Soar instance = Soar.getInstance();
        ModManager modManager = instance.getModManager();
        ColorManager colorManager = instance.getColorManager();
			JsonObject jsonObject = new JsonObject();
			JsonObject appJsonObject = new JsonObject();
			JsonObject modJsonObject = new JsonObject();
			JsonObject profileData = new JsonObject();
			
			profileData.addProperty("Icon", icon.getId());
			profileData.addProperty("Type", type.getId());
			profileData.addProperty("Server", serverIp);
			
			jsonObject.add("Profile Data", profileData);
			
			appJsonObject.addProperty("Accent Color", colorManager.getCurrentColor().getName());
			appJsonObject.addProperty("Theme", colorManager.getTheme().getId());
			appJsonObject.addProperty("Background", backgroundManager.getCurrentBackground().getId());
			appJsonObject.addProperty("Language", instance.getLanguageManager().getCurrentLanguage().getId());
			
			jsonObject.add("Appearance", appJsonObject);
			
			for(Mod m : modManager.getMods()) {
				
				JsonObject mJsonObject = new JsonObject();
				
				mJsonObject.addProperty("Toggle", m.isToggled());
				
				if(m instanceof HUDMod) {
					
					HUDMod hMod = (HUDMod)m;
					
					mJsonObject.addProperty("Toggle", hMod.isToggled());
					mJsonObject.addProperty("X", hMod.getX());
					mJsonObject.addProperty("Y", hMod.getY());
					mJsonObject.addProperty("Width", hMod.getWidth());
					mJsonObject.addProperty("Height", hMod.getHeight());
					mJsonObject.addProperty("Scale", hMod.getScale());
				}
				
				modJsonObject.add(m.getNameKey(), mJsonObject);
				
				if(modManager.getSettingsByMod(m) != null) {
					
					JsonObject sJsonObject = new JsonObject();
					
					for(Setting s : modManager.getSettingsByMod(m)) {
						
						if(s instanceof ColorSetting) {
							
							ColorSetting cSetting = (ColorSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), cSetting.getColor().getRGB());
						}
						
						if(s instanceof BooleanSetting) {
							
							BooleanSetting bSetting = (BooleanSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), bSetting.isToggled());
						}
						
						if(s instanceof ComboSetting) {
							
							ComboSetting cSetting = (ComboSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), cSetting.getOption().getNameKey());
						}
						
						if(s instanceof NumberSetting) {
							
							NumberSetting nSetting = (NumberSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), nSetting.getValue());
						}
						
						if(s instanceof TextSetting) {
							
							TextSetting tSetting = (TextSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), tSetting.getText());
						}
						
						if(s instanceof KeybindSetting) {
							
							KeybindSetting kSetting = (KeybindSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), kSetting.getKeyCode());
						}
						
						if(s instanceof ImageSetting) {
							
							ImageSetting iSetting = (ImageSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), iSetting.getImage() == null ? "null" : iSetting.getImage().getName());
						}
						
						if(s instanceof SoundSetting) {
							
							SoundSetting sSetting = (SoundSetting) s;
							
							sJsonObject.addProperty(s.getNameKey(), sSetting.getSound() == null ? "null" : sSetting.getSound().getName());
						}
					}
					
					mJsonObject.add("Settings", sJsonObject);
				}
			}
			
			jsonObject.add("Mods", modJsonObject);
			

        return jsonObject;
    }
    static boolean global(Setting setting) {
        return setting.getParent() instanceof GlobalSettingsMod
                && setting != GlobalSettingsMod.getInstance().getModThemeSetting();
    }
    JsonObject preferences() {
        JsonObject out = new JsonObject();
        JsonObject snapshot = snapshot("", ProfileType.ALL, ProfileIcon.GRASS);
        out.add("Appearance", snapshot.get("Appearance"));
        JsonObject settings = new JsonObject();
        for (Setting setting : Soar.getInstance().getModManager().getSettingsByMod(GlobalSettingsMod.getInstance())) {
            if (global(setting)) settings.add(setting.getNameKey(), snapshot.getAsJsonObject("Mods")
                    .getAsJsonObject(setting.getParent().getNameKey()).getAsJsonObject("Settings").get(setting.getNameKey()));
        }
        out.add("Global Settings", settings); return out;
    }
    void applyPreferences(JsonObject object) {
        Soar soar = Soar.getInstance();
        JsonObject app = object.has("Appearance") ? object.getAsJsonObject("Appearance") : new JsonObject();
        if (app.has("Accent Color")) soar.getColorManager().setCurrentColor(soar.getColorManager().getColorByName(app.get("Accent Color").getAsString()));
        if (app.has("Theme")) soar.getColorManager().setTheme(Theme.getThemeById(app.get("Theme").getAsInt()));
        if (app.has("Background")) backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(app.get("Background").getAsInt()));
        if (app.has("Language")) soar.getLanguageManager().setCurrentLanguage(Language.getLanguageById(app.get("Language").getAsString()));
        JsonObject settings = object.has("Global Settings") ? object.getAsJsonObject("Global Settings") : new JsonObject();
        if (object.has("Mods")) {
            JsonObject global = object.getAsJsonObject("Mods").getAsJsonObject(GlobalSettingsMod.getInstance().getNameKey());
            if (global != null && global.has("Settings")) settings = global.getAsJsonObject("Settings");
        }
        for (Setting setting : soar.getModManager().getSettingsByMod(GlobalSettingsMod.getInstance()))
            if (global(setting) && settings.has(setting.getNameKey())) apply(setting, settings.get(setting.getNameKey()));
    }
    static JsonObject prepare(JsonObject factory, JsonObject input) throws IOException {
        for (String key : new String[]{"Profile Data", "Appearance", "Mods"})
            if (!input.has(key) || !input.get(key).isJsonObject()) throw new IOException("Invalid configuration: " + key);
        JsonObject result = ProfileFiles.copy(factory);
        result.add("Profile Data", ProfileFiles.copy(input.getAsJsonObject("Profile Data")));
        JsonObject mods = result.getAsJsonObject("Mods");
        for (Map.Entry<String, JsonElement> mod : input.getAsJsonObject("Mods").entrySet()) {
            if (!mod.getValue().isJsonObject()) throw new IOException("Invalid module");
            if (!mods.has(mod.getKey())) continue;
            JsonObject destination = mods.getAsJsonObject(mod.getKey());
            for (Map.Entry<String, JsonElement> field : mod.getValue().getAsJsonObject().entrySet()) {
                if (!destination.has(field.getKey())) continue;
                if (field.getKey().equals("Settings")) {
                    if (!field.getValue().isJsonObject()) throw new IOException("Invalid settings");
                    JsonObject settings = destination.getAsJsonObject("Settings");
                    for (Map.Entry<String, JsonElement> setting : field.getValue().getAsJsonObject().entrySet()) {
                        if (settings.has(setting.getKey())) {
                            compatible(settings.get(setting.getKey()), setting.getValue());
                            settings.add(setting.getKey(), setting.getValue());
                        }
                    }
                } else { compatible(destination.get(field.getKey()), field.getValue()); destination.add(field.getKey(), field.getValue()); }
            }
        }
        return result;
    }
    private static void compatible(JsonElement expected, JsonElement actual) throws IOException {
        if (!expected.isJsonPrimitive() || !actual.isJsonPrimitive()) throw new IOException("Invalid setting type");
        JsonPrimitive a = actual.getAsJsonPrimitive(), e = expected.getAsJsonPrimitive();
        if (e.isBoolean() != a.isBoolean() || e.isString() != a.isString() || e.isNumber() != a.isNumber())
            throw new IOException("Invalid setting type");
        if (a.isNumber() && (Double.isNaN(a.getAsDouble()) || Double.isInfinite(a.getAsDouble()))) throw new IOException("Invalid setting number");
    }
    void apply(JsonObject object) {
        ModManager manager = Soar.getInstance().getModManager();
        for (Mod mod : manager.getMods()) if (!(mod instanceof GlobalSettingsMod) && mod.isToggled()) mod.setToggled(false);
        for (Mod mod : manager.getMods()) {
            JsonObject data = object.getAsJsonObject("Mods").getAsJsonObject(mod.getNameKey());
            if (mod instanceof HUDMod) {
                HUDMod hud = (HUDMod) mod;
                hud.setX(data.get("X").getAsInt()); hud.setY(data.get("Y").getAsInt());
                hud.setWidth(data.get("Width").getAsInt()); hud.setHeight(data.get("Height").getAsInt());
                float scale = data.get("Scale").getAsFloat();
                hud.setScale(Math.max(0.1f, Math.min(10f, scale)));
            }
            if (data.has("Settings") && manager.getSettingsByMod(mod) != null)
                for (Setting setting : manager.getSettingsByMod(mod))
                    if (!global(setting) && data.getAsJsonObject("Settings").has(setting.getNameKey()))
                        apply(setting, data.getAsJsonObject("Settings").get(setting.getNameKey()));
        }
        for (Mod mod : manager.getMods())
            if (!(mod instanceof GlobalSettingsMod) && object.getAsJsonObject("Mods").getAsJsonObject(mod.getNameKey()).get("Toggle").getAsBoolean()) mod.setToggled(true);
    }
    private void apply(Setting setting, JsonElement value) {
        if (setting instanceof BooleanSetting) ((BooleanSetting) setting).setToggled(value.getAsBoolean());
        else if (setting instanceof ComboSetting) ((ComboSetting) setting).setOption(((ComboSetting) setting).getOptionByNameKey(value.getAsString()));
        else if (setting instanceof NumberSetting) {
            NumberSetting number = (NumberSetting) setting;
            number.setValue(Math.max(number.getMinValue(), Math.min(number.getMaxValue(), value.getAsDouble())));
        } else if (setting instanceof TextSetting) ((TextSetting) setting).setText(value.getAsString());
        else if (setting instanceof KeybindSetting) ((KeybindSetting) setting).setKeyCode(value.getAsInt());
        else if (setting instanceof ColorSetting) ((ColorSetting) setting).setColor(ColorUtils.getColorByInt(value.getAsInt()));
        else if (setting instanceof ImageSetting) ((ImageSetting) setting).setImage(resource("custom-image", value.getAsString()));
        else if (setting instanceof SoundSetting) ((SoundSetting) setting).setSound(resource("custom-sound", value.getAsString()));
    }
    private File resource(String kind, String name) {
        if (name.equals("null") || name.isEmpty() || name.contains("/") || name.contains("\\") || name.contains(":") || name.equals(".") || name.equals("..")) return null;
        File file = new File(new File(Soar.getInstance().getFileManager().getCacheDir(), kind), name);
        return file.isFile() ? file : null;
    }
    static JsonObject state(JsonObject object) {
        JsonObject mods = ProfileFiles.copy(object.getAsJsonObject("Mods"));
        GlobalSettingsMod global = GlobalSettingsMod.getInstance();
        if (global != null && mods.has(global.getNameKey())) {
            JsonObject data = mods.getAsJsonObject(global.getNameKey()), settings = data.getAsJsonObject("Settings");
            if (settings != null) for (Setting s : Soar.getInstance().getModManager().getSettingsByMod(global)) if (global(s)) settings.remove(s.getNameKey());
        }
        for (Map.Entry<String, JsonElement> entry : mods.entrySet()) if (entry.getValue().isJsonObject()) {
            entry.getValue().getAsJsonObject().remove("Width"); entry.getValue().getAsJsonObject().remove("Height");
        }
        return mods;
    }
}

