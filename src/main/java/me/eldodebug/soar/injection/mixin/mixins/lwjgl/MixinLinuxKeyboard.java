package me.eldodebug.soar.injection.mixin.mixins.lwjgl;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "org.lwjgl.opengl.LinuxKeyboard")
public class MixinLinuxKeyboard {

	@Shadow
	@Final
	private int numlock_mask, modeswitch_mask, caps_lock_mask, shift_lock_mask;

	@Overwrite(remap = false)
	private int getKeycode(long eventp, int eventState) {
		
		boolean shift = (eventState & (1 | shift_lock_mask)) != 0;
		int group = (eventState & modeswitch_mask) != 0 ? 1 : 0;
		long keysym;
		
		if((eventState & numlock_mask) != 0 && isKeypadKeysym(keysym = getKeySym(eventp, group, 1))) {
			if(shift)
				keysym = getKeySym(eventp, group, 0);
		}
		else {
			keysym = getKeySym(eventp, group, 0);
			if(shift ^ ((eventState & caps_lock_mask) != 0))
				keysym = toUpper(keysym);
		}
		
		return MixinLinuxKeycodes.mapKeySymToLWJGLKeyCode(keysym);
	}

	@Shadow
	private static boolean isKeypadKeysym(long keysym) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private static long getKeySym(long eventp, int group, int index) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private static native long toUpper(long keysym);

}