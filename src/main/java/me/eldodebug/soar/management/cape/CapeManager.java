package me.eldodebug.soar.management.cape;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.cape.impl.OfficialCape;
import me.eldodebug.soar.management.event.impl.EventLocationCape;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.file.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;

public class CapeManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final ArrayList<Cape> capes = new ArrayList<Cape>();
    private final File customDir, cacheDir;
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(64), new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Soar-Cape-Worker");
            thread.setDaemon(true);
            return thread;
        }
    });
    private CapeSelectionStore store;
    private Cape currentCape;
    private UUID account;
    private String selectedId = "official";
    private long officialWaitStarted;
    private NetworkPlayerInfo lastOfficialInfo;

    public CapeManager() {
        customDir = new File(Soar.getInstance().getFileManager().getSoarDir(), "custom-cape");
        cacheDir = new File(Soar.getInstance().getFileManager().getCacheDir(), "cape");
        customDir.mkdirs(); cacheDir.mkdirs();
        try { store = new CapeSelectionStore(new File(Soar.getInstance().getFileManager().getSoarDir(), "cape-selections.properties")); }
        catch (IOException error) { SoarLogger.error("Failed to read cape selections", error); }
        capes.add(new OfficialCape());
        capes.add(new NormalCape("None", null, null, false, CapeCategory.ALL));
        add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", false, CapeCategory.MINECON);
        add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", false, CapeCategory.MINECON);
        add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", false, CapeCategory.MINECON);
        add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", false, CapeCategory.MINECON);
        add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", false, CapeCategory.MINECON);
        add("Canada", "flag/canada-sample.png", "flag/canada.png", true, CapeCategory.FLAG);
        add("France", "flag/france-sample.png", "flag/france.png", true, CapeCategory.FLAG);
        add("Germany", "flag/germany-sample.png", "flag/germany.png", true, CapeCategory.FLAG);
        add("India", "flag/india-sample.png", "flag/india.png", true, CapeCategory.FLAG);
        add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", true, CapeCategory.FLAG);
        add("Italy", "flag/italy-sample.png", "flag/italy.png", true, CapeCategory.FLAG);
        add("Japan", "flag/japan-sample.png", "flag/japan.png", true, CapeCategory.FLAG);
        add("Korean", "flag/korean-sample.png", "flag/korean.png", true, CapeCategory.FLAG);
        add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", true, CapeCategory.FLAG);
        add("United States", "flag/united-states-sample.png", "flag/united-states.png", true, CapeCategory.FLAG);
        add("Miku", "abcoc/miku-sample.png", "abcoc/miku.png", true, CapeCategory.ABCOC);
        for (int i = 1; i <= 6; i++) add("neko" + i, "nachoneko/neko" + i + "-sample.png", "nachoneko/neko" + i + ".png", true, CapeCategory.NACHONEKO);
        currentCape = capes.get(0);
        ensureAccount();
        for (Cape cape : capes) {
            if (cape instanceof NormalCape && ((NormalCape) cape).getSample() != null)
                Soar.getInstance().getNanoVGManager().loadImage(((NormalCape) cape).getSample());
            if (cape.getCape() != null) mc.getTextureManager().bindTexture(cape.getCape());
        }
        workers.execute(new Runnable() { public void run() { loadCustomCapes(); } });
    }

    private void add(String name, String sample, String texture, boolean premium, CapeCategory category) {
        String base = "soar/cosmetics/cape/";
        capes.add(new NormalCape(name, new ResourceLocation(base + sample), new ResourceLocation(base + texture), premium, category));
    }

    private void loadCustomCapes() {
        File[] files = customDir.listFiles();
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (final File source : files) {
            if (!FileUtils.isImageFile(source) || source.length() > 4 * 1024 * 1024) continue;
            try (javax.imageio.stream.ImageInputStream input = ImageIO.createImageInputStream(source)) {
                if (input == null) continue;
                Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) continue;
                javax.imageio.ImageReader reader = readers.next();
                final BufferedImage image;
                try {
                    reader.setInput(input);
                    int w = reader.getWidth(0), h = reader.getHeight(0);
                    if (w < 32 || h < 16 || w > 2048 || h > 2048) continue;
                    image = reader.read(0);
                } finally { reader.dispose(); }
                final File sample = new File(cacheDir, "local-" + UUID.nameUUIDFromBytes(source.getName().getBytes(StandardCharsets.UTF_8)) + ".png");
                BufferedImage front = ImageUtils.scissor(image, image.getWidth()/32, image.getHeight()/16, image.getWidth()/8, image.getHeight()*15/32);
                ImageIO.write(ImageUtils.resize(front, 160, 272), "png", sample);
                mc.addScheduledTask(() -> {
                    ResourceLocation texture = mc.getTextureManager().getDynamicTextureLocation("soar-local-cape", new DynamicTexture(image));
                    CustomCape cape = new CustomCape(source.getName().replaceFirst("\\.[^.]+$", ""), sample, source, texture, true, CapeCategory.CUSTOM);
                    capes.add(cape);
                    ensureAccount();
                    if (selectedId.equals(cape.getId())) currentCape = cape;
                });
            } catch (Exception error) { SoarLogger.error("Invalid local cape " + source.getName(), error); }
        }
    }

    private UUID currentAccount() {
        UUID id = mc.getSession().getProfile().getId();
        return id != null ? id : UUID.nameUUIDFromBytes(("OfflinePlayer:" + mc.getSession().getUsername()).getBytes(StandardCharsets.UTF_8));
    }
    private void ensureAccount() {
        UUID next = currentAccount();
        if (next.equals(account)) return;
        account = next;
        selectedId = store == null ? "official" : store.getSelection(account);
        Cape found = findById(selectedId);
        currentCape = found == null ? capes.get(0) : found;
        officialWaitStarted = System.currentTimeMillis();
    }

    public ArrayList<Cape> getCapes() { return capes; }
    public Cape getCurrentCape() { ensureAccount(); return currentCape; }
    public void setCurrentCape(Cape cape) {
        if (cape == null || !capes.contains(cape)) return;
        ensureAccount();
        currentCape = cape;
        selectedId = cape.getId();
        if (store != null) { store.setSelection(account, selectedId); saveLater(); }

    }
    public Cape getCapeByName(String name) {
        for (Cape cape : capes) if (cape.getName().equals(name)) return cape;
        return capes.get(0);
    }
    private Cape findById(String id) {
        for (Cape cape : capes) if (cape.getId().equals(id)) return cape;
        return null;
    }
    public String getDisplayName(Cape cape) {
        if (cape instanceof OfficialCape) return TranslateText.OFFICIAL_CAPE.getText();
        if ("none".equals(cape.getId())) return TranslateText.NO_CAPE.getText();
        return cape.getName();
    }




    private void saveLater() {
        if (store == null) return;
        try { workers.execute(new Runnable() { public void run() {
            try { store.save(); } catch (IOException error) { SoarLogger.error("Failed to save cape selections", error); }
        } }); } catch (RejectedExecutionException busy) { SoarLogger.error("Cape worker queue is full"); }
    }

    public ResourceLocation getOfficialCape() {
        ensureAccount();
        NetworkPlayerInfo info = mc.getNetHandler() == null ? null : mc.getNetHandler().getPlayerInfo(account);
        if (info != lastOfficialInfo) { lastOfficialInfo = info; officialWaitStarted = System.currentTimeMillis(); }
        return info == null ? null : info.getLocationCape();
    }
    public String getOfficialStatus() {
        ensureAccount();
        return (System.currentTimeMillis() - officialWaitStarted < 10000 ? TranslateText.CAPE_LOADING : TranslateText.CAPE_NO_OFFICIAL).getText();
    }

    public void resolve(EventLocationCape event) {
        ensureAccount();
        if (!account.equals(event.getPlayerId())) return;
        if (currentCape instanceof OfficialCape) return;
        override(event, "none".equals(currentCape.getId()) ? null : currentCape.getCape());
    }
    private static void override(EventLocationCape event, ResourceLocation cape) { event.setCape(cape); event.setCancelled(true); }



    public void shutdown() {
        if (store != null) {
            try { store.save(); } catch (IOException error) { SoarLogger.error("Failed to save cape selections", error); }
        }
        workers.shutdownNow();
    }
}
