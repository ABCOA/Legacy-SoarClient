package me.eldodebug.soar.injection.interfaces;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;

public interface IMixinMinecraft {
	boolean isRunning();
	Timer getTimer();
	void setSession(Session session);
	void callClickMouse();
	void callRightClickMouse();
	DefaultResourcePack getMcDefaultResourcePack();
    void resizeWindow(int width, int height);
    Entity getRenderViewEntity();
}
