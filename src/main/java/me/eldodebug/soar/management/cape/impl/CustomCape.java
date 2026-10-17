package me.eldodebug.soar.management.cape.impl;
import java.io.File;
import me.eldodebug.soar.management.cape.CapeCategory;
import net.minecraft.util.ResourceLocation;
public class CustomCape extends Cape {
    private final File sample;
    public CustomCape(String name, File sample, File source, ResourceLocation cape, boolean premium, CapeCategory category) {
        super(name, cape, premium, category, "custom:" + source.getName());
        this.sample = sample;
    }
    public File getSample() { return sample; }
}
