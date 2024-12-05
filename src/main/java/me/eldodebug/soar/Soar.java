package me.eldodebug.soar;

import me.eldodebug.soar.injection.mixin.SoarTweaker;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.account.AccountManager;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.changelog.ChangelogManager;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.command.CommandManager;
import me.eldodebug.soar.management.download.DownloadManager;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.LanguageManager;
import me.eldodebug.soar.management.mods.ModManager;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.notification.NotificationManager;
import me.eldodebug.soar.management.profile.ProfileManager;
import me.eldodebug.soar.management.quickplay.QuickPlayManager;
import me.eldodebug.soar.management.screenshot.ScreenshotManager;
import me.eldodebug.soar.management.security.SecurityFeatureManager;
import me.eldodebug.soar.management.waypoint.WaypointManager;
import me.eldodebug.soar.utils.OptifineUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Soar {

	private static Soar instance = new Soar();
	private Minecraft mc = Minecraft.getMinecraft();
	private SoarAPI api;
	
	private String name, version;
	
	private NanoVGManager nanoVGManager;
	private FileManager fileManager;
	private DownloadManager downloadManager;
	private LanguageManager languageManager;
	private AccountManager accountManager;
	private EventManager eventManager;
	private ModManager modManager;
	private CapeManager capeManager;
	private ColorManager colorManager;
	private ProfileManager profileManager;
	private MusicManager musicManager;
	private CommandManager commandManager;
	private ScreenshotManager screenshotManager;
	private NotificationManager notificationManager;
	private SecurityFeatureManager securityFeatureManager;
	private QuickPlayManager quickPlayManager;
	private ChangelogManager changelogManager;
	private WaypointManager waypointManager;
	
	public Soar() {
		name = "Soar";
		version = "ABCOC's special version";
	}
	
	public void start() {
		
		OptifineUtils.disableFastRender();
		this.removeOptifineZoom();
		
		fileManager = new FileManager();
		downloadManager = new DownloadManager();
		languageManager = new LanguageManager();
		accountManager = new AccountManager();
		eventManager = new EventManager();
		modManager = new ModManager();
		
		modManager.init();
		
		capeManager = new CapeManager();
		colorManager = new ColorManager();
		profileManager = new ProfileManager();
		musicManager = new MusicManager();
		
		api = new SoarAPI();
		api.init();
		
		commandManager = new CommandManager();
		screenshotManager = new ScreenshotManager();
		notificationManager = new NotificationManager();
		securityFeatureManager = new SecurityFeatureManager();
		quickPlayManager = new QuickPlayManager();
		changelogManager = new ChangelogManager();
		waypointManager = new WaypointManager();
		
		eventManager.register(new SoarHandler());
		
		setupLibraryPath();
		
		GlobalSettingsMod.getInstance().setToggled(true);
		mc.updateDisplay();
	}
	
	public void stop() {
		profileManager.save();
		accountManager.save();
	}
	
	private void removeOptifineZoom() {
		if(SoarTweaker.hasOptifine) {
			try {
				this.unregisterKeybind((KeyBinding) GameSettings.class.getField("ofKeyBindZoom").get(mc.gameSettings));
			} catch(Exception e) {
				SoarLogger.error("Failed to unregister zoom key", e);
			}
		}
	}
	
    private void unregisterKeybind(KeyBinding key) {
        if (Arrays.asList(mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings = ArrayUtils.remove(mc.gameSettings.keyBindings, Arrays.asList(mc.gameSettings.keyBindings).indexOf(key));
            key.setKeyCode(0);
        }
    }
	
    private void setupLibraryPath() {
    	
    	File cefDir = new File(fileManager.getExternalDir(), "cef");
    	
        try {
			System.setProperty("jcef.path", cefDir.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public static Soar getInstance() {
		return instance;
	}

	public SoarAPI getApi() {
		return api;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public FileManager getFileManager() {
		return fileManager;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public ModManager getModManager() {
		return modManager;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public NanoVGManager getNanoVGManager() {
		return nanoVGManager;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}

	public MusicManager getMusicManager() {
		return musicManager;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public CapeManager getCapeManager() {
		return capeManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public ScreenshotManager getScreenshotManager() {
		return screenshotManager;
	}

	public void setNanoVGManager(NanoVGManager nanoVGManager) {
		this.nanoVGManager = nanoVGManager;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public SecurityFeatureManager getSecurityFeatureManager() {
		return securityFeatureManager;
	}

	public QuickPlayManager getQuickPlayManager() {
		return quickPlayManager;
	}

	public ChangelogManager getChangelogManager() {
		return changelogManager;
	}

	public WaypointManager getWaypointManager() {
		return waypointManager;
	}
}
