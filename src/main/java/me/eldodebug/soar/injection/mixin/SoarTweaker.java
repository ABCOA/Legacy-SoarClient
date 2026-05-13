package me.eldodebug.soar.injection.mixin;

import me.eldodebug.soar.injection.transformer.LwjglTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SoarTweaker implements ITweaker {

	private static final String WINDOW_TITLE_PROPERTY = "soar.windowTitle";
	private static final String WINDOW_TITLE_ARGUMENT = "--windowTitle";
	private static final String SOAR_TITLE_ARGUMENT = "--soarTitle";
	private static String windowTitle;

    private final List<String> launchArguments = new ArrayList<>();

	public static boolean hasOptifine = false;
	
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    	
    	try {
			Class.forName("optifine.Patcher");
			hasOptifine = true;
		}
		catch(ClassNotFoundException e) {
		}
		
        this.launchArguments.addAll(filterSoarArguments(args));

        if (profile != null) {
            launchArguments.add("--version");
            launchArguments.add(profile);
        }

        if (assetsDir != null) {
            launchArguments.add("--assetsDir");
            launchArguments.add(assetsDir.getAbsolutePath());
        }

        if (gameDir != null) {
            launchArguments.add("--gameDir");
            launchArguments.add(gameDir.getAbsolutePath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    	classLoader.registerTransformer(LwjglTransformer.class.getName());
        this.relaxLaunchClassLoaderRestrictions();
    	
        MixinBootstrap.init();

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.soar.json");

        if (env.getObfuscationContext() == null) {
        	env.setObfuscationContext("notch");
        }

        env.setSide(MixinEnvironment.Side.CLIENT);

    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[0]);
    }

	public static String getWindowTitle(String fallbackTitle) {
		String title = windowTitle;

		if(isBlank(title)) {
			title = System.getProperty(WINDOW_TITLE_PROPERTY);
		}

		return isBlank(title) ? fallbackTitle : title.trim();
	}

	private List<String> filterSoarArguments(List<String> args) {
		List<String> filteredArguments = new ArrayList<>();

		for(int i = 0; i < args.size(); i++) {
			String argument = args.get(i);

			if(isWindowTitleArgument(argument)) {
				String inlineTitle = getInlineWindowTitle(argument);

				if(!isBlank(inlineTitle)) {
					windowTitle = inlineTitle;
				} else if(i + 1 < args.size() && !args.get(i + 1).startsWith("--")) {
					windowTitle = args.get(++i);
				}

				continue;
			}

			filteredArguments.add(argument);
		}

		return filteredArguments;
	}

	private static boolean isWindowTitleArgument(String argument) {
		return WINDOW_TITLE_ARGUMENT.equals(argument)
				|| SOAR_TITLE_ARGUMENT.equals(argument)
				|| argument.startsWith(WINDOW_TITLE_ARGUMENT + "=")
				|| argument.startsWith(SOAR_TITLE_ARGUMENT + "=");
	}

	private static String getInlineWindowTitle(String argument) {
		int splitIndex = argument.indexOf('=');
		return splitIndex >= 0 ? argument.substring(splitIndex + 1) : null;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
    
    @SuppressWarnings("unchecked")
    private void relaxLaunchClassLoaderRestrictions() {
        try {
            Field classLoaderExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            classLoaderExceptions.setAccessible(true);
            Object o = classLoaderExceptions.get(Launch.classLoader);
            Set<String> exceptions = (Set<String>) o;
            exceptions.remove("org.lwjgl.");
            // LaunchWrapper excludes the primary tweaker package, which would also
            // exclude our mixin classes because they live under the same prefix.
            exceptions.remove(this.getClass().getPackage().getName() + ".");
        } catch (NoSuchFieldException | IllegalAccessException e) {}
    }
}
