package me.eldodebug.soar.management.cape;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.cape.impl.OfficialCape;
import me.eldodebug.soar.management.event.impl.EventLocationCape;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.online.CapeDescriptor;
import me.eldodebug.soar.management.online.OnlineMember;
import me.eldodebug.soar.management.online.PresenceManager;
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
    private final File customDir, cacheDir, remoteDir;
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(64), new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Soar-Cape-Worker");
            thread.setDaemon(true);
            return thread;
        }
    });
    private final LinkedHashMap<String, ResourceLocation> remoteTextures = new LinkedHashMap<String, ResourceLocation>(64, .75F, true);
    private final Set<String> downloads = new HashSet<String>();
    private final Map<String, Long> failedDownloads = new HashMap<String, Long>();
    private CapeSelectionStore store;
    private Cape currentCape;
    private UUID account;
    private String selectedId = "official";
    private CapeDescriptor published = CapeDescriptor.official();
    private long generation;
    private TranslateText syncState = TranslateText.CAPE_SYNC_PENDING;
    private String syncDetail = "";
    private long officialWaitStarted;
    private boolean customUploadPending;
    private NetworkPlayerInfo lastOfficialInfo;
    private long nextMissingRetry;

    public CapeManager() {
        customDir = new File(Soar.getInstance().getFileManager().getSoarDir(), "custom-cape");
        cacheDir = new File(Soar.getInstance().getFileManager().getCacheDir(), "cape");
        remoteDir = new File(cacheDir, "online");
        customDir.mkdirs(); cacheDir.mkdirs(); remoteDir.mkdirs();
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
            if (!FileUtils.isImageFile(source)) continue;
            try {
                final BufferedImage image = CapeImageCodec.readSource(source);
                byte[] normalized = null;
                String error = null;
                try { normalized = CapeImageCodec.normalize(image); }
                catch (IOException invalidForSync) { error = invalidForSync.getMessage(); }
                final byte[] bytes = normalized;
                final String syncError = error;
                final String hash = bytes == null ? null : CapeImageCodec.sha256(bytes);
                final File sample = new File(cacheDir, "local-" + CapeImageCodec.sha256(source.getName().getBytes(StandardCharsets.UTF_8)) + ".png");
                BufferedImage front = ImageUtils.scissor(image, image.getWidth() / 32, image.getHeight() / 16,
                        image.getWidth() / 8, image.getHeight() * 15 / 32);
                ImageIO.write(ImageUtils.resize(front, 160, 272), "png", sample);
                mc.addScheduledTask(new Runnable() { public void run() {
                    ResourceLocation texture = mc.getTextureManager().getDynamicTextureLocation("soar-local-cape", new DynamicTexture(image));
                    String name = source.getName().replace("." + FileUtils.getExtension(source), "");
                    CustomCape cape = new CustomCape(name, sample, source, texture, true, CapeCategory.CUSTOM, bytes, hash, syncError);
                    capes.add(cape);
                    ensureAccount();
                    if (selectedId.equals(cape.getId())) { currentCape = cape; generation++; syncCurrentSelection(); }
                } });
            } catch (Exception error) { SoarLogger.error("Invalid local cape " + source.getName(), error); }
        }
        trimDiskCache();
    }

    private UUID currentAccount() {
        UUID id = mc.getSession().getProfile().getId();
        return id != null ? id : UUID.nameUUIDFromBytes(("OfflinePlayer:" + mc.getSession().getUsername()).getBytes(StandardCharsets.UTF_8));
    }
    private void ensureAccount() {
        UUID next = currentAccount();
        if (next.equals(account)) return;
        account = next; generation++; customUploadPending = false;
        selectedId = store == null ? "official" : store.getSelection(account);
        published = parseDescriptor(store == null ? "official" : store.getPublished(account));
        Cape found = findById(selectedId);
        currentCape = found == null ? capes.get(0) : found;
        officialWaitStarted = System.currentTimeMillis();
        syncState = TranslateText.CAPE_SYNC_PENDING; syncDetail = "";
    }

    public ArrayList<Cape> getCapes() { return capes; }
    public Cape getCurrentCape() { ensureAccount(); return currentCape; }
    public void setCurrentCape(Cape cape) {
        if (cape == null || !capes.contains(cape)) return;
        ensureAccount();
        currentCape = cape; selectedId = cape.getId(); generation++; customUploadPending = false;
        if (store != null) { store.setSelection(account, selectedId); saveLater(); }
        syncCurrentSelection();
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
    public String getSyncStatus() { return syncState.getText(); }
    public String getSyncDetail() { return syncDetail; }

    public void syncCurrentSelection() {
        ensureAccount();
        PresenceManager manager = PresenceManager.getInstance();
        if (manager == null) return;
        if (!selectedId.equals(currentCape.getId())) { manager.setCape(published); return; }
        if (!(currentCape instanceof CustomCape)) { publish(descriptor(currentCape), manager); return; }
        final CustomCape cape = (CustomCape) currentCape;
        if (cape.getNormalizedPng() == null) {
            syncState = TranslateText.CAPE_LOCAL_ONLY; syncDetail = cape.getSyncError();
            manager.setCape(published);
            return;
        }
        String canonical = store == null ? null : store.getCanonicalHash(account, cape.getSha256());
        if (CapeImageCodec.isHash(canonical)) { publish(CapeDescriptor.custom(canonical), manager); return; }
        manager.setCape(published);
        if (customUploadPending) return;
        syncState = TranslateText.CAPE_SYNC_PENDING; syncDetail = "";
        customUploadPending = true;
        final long selectedGeneration = generation;
        final UUID selectedAccount = account;
        manager.uploadCape(cape.getNormalizedPng(), cape.getSha256(), new PresenceManager.UploadCallback() {
            public void onUploaded(String hash) {
                if (!CapeImageCodec.isHash(hash)) { onFailure("Invalid upload response"); return; }
                if (store != null) { store.setCanonicalHash(selectedAccount, cape.getSha256(), hash); saveLater(); }
                ensureAccount();
                if (generation != selectedGeneration || !selectedAccount.equals(account) || currentCape != cape) {
                    // A quick second choice may have waited for this single-account upload slot.
                    if (selectedAccount.equals(account) && currentCape instanceof CustomCape) syncCurrentSelection();
                    return;
                }
                customUploadPending = false;
                publish(CapeDescriptor.custom(hash), PresenceManager.getInstance());
            }
            public void onFailure(String reason) {
                ensureAccount();
                if (generation != selectedGeneration || !selectedAccount.equals(account) || currentCape != cape) {
                    if (selectedAccount.equals(account) && currentCape instanceof CustomCape) syncCurrentSelection();
                    return;
                }
                customUploadPending = false;
                syncState = TranslateText.CAPE_SYNC_FAILED; syncDetail = reason;
            }
        });
    }

    public CapeDescriptor getCurrentDescriptor() {
        ensureAccount();
        if (!selectedId.equals(currentCape.getId())) return published;
        if (currentCape instanceof CustomCape) {
            String hash = store == null ? null : store.getCanonicalHash(account, ((CustomCape) currentCape).getSha256());
            return CapeImageCodec.isHash(hash) ? CapeDescriptor.custom(hash) : published;
        }
        return descriptor(currentCape);
    }

    public void onPublishedCapeRejected(String hash) {
        ensureAccount();
        if (!(currentCape instanceof CustomCape) || !CapeImageCodec.isHash(hash)) return;
        CustomCape selected = (CustomCape) currentCape;
        String cached = store == null ? null : store.getCanonicalHash(account, selected.getSha256());
        if (!hash.equals(cached) && !hash.equals(published.getHash())) return;
        if (hash.equals(cached) && store != null) { store.removeCanonicalHash(account, selected.getSha256()); saveLater(); }
        published = CapeDescriptor.official();
        if (store != null) { store.setPublished(account, "official"); saveLater(); }
        PresenceManager manager = PresenceManager.getInstance();
        if (manager != null) manager.setCape(published);
        if (customUploadPending || System.currentTimeMillis() < nextMissingRetry) return;
        nextMissingRetry = System.currentTimeMillis() + 60000;
        syncCurrentSelection();
    }
    private CapeDescriptor descriptor(Cape cape) {
        if (cape instanceof OfficialCape) return CapeDescriptor.official();
        if ("none".equals(cape.getId())) return CapeDescriptor.none();
        return CapeDescriptor.builtin(cape.getCape().getResourcePath());
    }
    private void publish(CapeDescriptor cape, PresenceManager manager) {
        if (manager == null) return;
        published = cape;
        manager.setCape(cape);
        syncState = manager.isAuthenticated() ? TranslateText.CAPE_SYNCED : TranslateText.CAPE_SYNC_PENDING; syncDetail = "";
        if (store != null) { store.setPublished(account, serializeDescriptor(cape)); saveLater(); }
    }
    private static String serializeDescriptor(CapeDescriptor cape) {
        if ("builtin".equals(cape.getType())) return "builtin:" + cape.getId();
        if ("custom".equals(cape.getType())) return "custom:" + cape.getHash();
        return cape.getType();
    }
    private static CapeDescriptor parseDescriptor(String value) {
        if ("none".equals(value)) return CapeDescriptor.none();
        if (value.startsWith("builtin:")) {
            try { return CapeDescriptor.builtin(value.substring(8)); }
            catch (IllegalArgumentException invalid) { return CapeDescriptor.official(); }
        }
        if (value.startsWith("custom:") && CapeImageCodec.isHash(value.substring(7))) return CapeDescriptor.custom(value.substring(7));
        return CapeDescriptor.official();
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
        UUID player = event.getPlayerId();
        if (player == null) return;
        CapeDescriptor selection;
        if (player.equals(account)) {
            if (currentCape instanceof CustomCape) { override(event, currentCape.getCape()); return; }
            selection = descriptor(currentCape);
        } else {
            PresenceManager manager = PresenceManager.getInstance();
            if (manager == null || !manager.isOnline(player)) return;
            OnlineMember member = manager.getMember(player);
            if (member == null) return;
            selection = member.getCape();
            if (selection == null) return;
        }
        if ("none".equals(selection.getType())) { override(event, null); return; }
        if ("official".equals(selection.getType())) {
            override(event, event.getPlayerInfo() == null ? null : event.getPlayerInfo().getLocationCape());
            return;
        }
        if ("builtin".equals(selection.getType())) {
            Cape builtin = findById("builtin:" + selection.getId());
            if (builtin != null) override(event, builtin.getCape());
            return;
        }
        if ("custom".equals(selection.getType()) && CapeImageCodec.isHash(selection.getHash())) {
            ResourceLocation texture = remoteTextures.get(selection.getHash());
            if (texture != null) override(event, texture);
            else requestTexture(player, selection.getHash());
        }
    }
    private static void override(EventLocationCape event, ResourceLocation cape) { event.setCape(cape); event.setCancelled(true); }

    private void requestTexture(final UUID player, final String hash) {
        if (downloads.contains(hash)) return;
        Long failure = failedDownloads.get(hash);
        if (failure != null && System.currentTimeMillis() - failure < 60000) return;
        final PresenceManager manager = PresenceManager.getInstance();
        if (manager == null) return;
        final String url = manager.getCapeDownloadUrl(hash);
        final Object world = mc.theWorld;
        downloads.add(hash);
        try { workers.execute(new Runnable() { public void run() {
            try {
                File cache = new File(remoteDir, hash + ".png");
                byte[] bytes = null;
                if (cache.isFile() && cache.length() <= CapeImageCodec.MAX_PNG_BYTES) {
                    bytes = Files.readAllBytes(cache.toPath());
                    if (!hash.equals(CapeImageCodec.sha256(bytes))) bytes = null;
                }
                boolean cached = bytes != null;
                if (!cached) bytes = download(url);
                if (!hash.equals(CapeImageCodec.sha256(bytes))) throw new IOException("Cape hash mismatch");
                final BufferedImage image = CapeImageCodec.decodePng(bytes);
                if (!cached) {
                    File temporary = new File(remoteDir, hash + ".tmp");
                    Files.write(temporary.toPath(), bytes);
                    Files.move(temporary.toPath(), cache.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    trimDiskCache();
                }
                cache.setLastModified(System.currentTimeMillis());
                mc.addScheduledTask(new Runnable() { public void run() {
                    downloads.remove(hash);
                    OnlineMember current = manager.getMember(player);
                    if (mc.theWorld != world || !manager.isOnline(player) || current == null || current.getCape() == null
                            || !hash.equals(current.getCape().getHash())) return;
                    ResourceLocation texture = mc.getTextureManager().getDynamicTextureLocation("soar-online-cape", new DynamicTexture(image));
                    remoteTextures.put(hash, texture);
                    if (remoteTextures.size() > 64) {
                        String eldest = remoteTextures.keySet().iterator().next();
                        mc.getTextureManager().deleteTexture(remoteTextures.remove(eldest));
                    }
                    failedDownloads.remove(hash);
                } });
            } catch (Exception error) {
                mc.addScheduledTask(new Runnable() { public void run() {
                    downloads.remove(hash);
                    rememberDownloadFailure(hash);
                } });
            }
        } }); } catch (RejectedExecutionException busy) { downloads.remove(hash); rememberDownloadFailure(hash); }
    }
    private void rememberDownloadFailure(String hash) {
        if (failedDownloads.size() >= 256) failedDownloads.clear();
        failedDownloads.put(hash, System.currentTimeMillis());
    }
    private static byte[] download(String address) throws IOException {
        URL url = new URL(address);
        if (!"https".equalsIgnoreCase(url.getProtocol()) && !("http".equalsIgnoreCase(url.getProtocol())
                && ("localhost".equals(url.getHost()) || "127.0.0.1".equals(url.getHost())))) throw new IOException("HTTPS required");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false); connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        try {
            if (connection.getResponseCode() != 200) throw new IOException("Cape download rejected");
            if (connection.getContentLengthLong() > CapeImageCodec.MAX_PNG_BYTES) throw new IOException("Cape image too large");
            try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (System.nanoTime() > deadline || out.size() + count > CapeImageCodec.MAX_PNG_BYTES) throw new IOException("Cape download limit exceeded");
                    out.write(buffer, 0, count);
                }
                return out.toByteArray();
            }
        } finally { connection.disconnect(); }
    }
    private void trimDiskCache() {
        File[] files = remoteDir.listFiles((dir, name) -> name.matches("[a-f0-9]{64}\\.png"));
        if (files == null || files.length <= 128) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - 128; i++) files[i].delete();
    }

    public void shutdown() {
        if (store != null) {
            try { store.save(); } catch (IOException error) { SoarLogger.error("Failed to save cape selections", error); }
        }
        workers.shutdownNow();
    }
}
