package me.eldodebug.soar.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OnlineChatMetadata {
    public static final class Sender {
        private final UUID uuid;
        private final boolean firstLine, suppressed;
        Sender(UUID uuid, boolean firstLine, boolean suppressed) { this.uuid = uuid; this.firstLine = firstLine; this.suppressed = suppressed; }
        public UUID getUuid() { return uuid; }
        public boolean isFirstLine() { return firstLine; }
        public boolean isSuppressed() { return suppressed; }
    }
    private static final Pattern PUBLIC_HYPIXEL = Pattern.compile("^(?:\\[[0-9]{1,4}[\\u272b\\u272a\\u22c6]\\] )?(?:\\[(?:VIP\\+?|MVP\\+{0,2}|YOUTUBE|ADMIN|MOD|HELPER|GM)\\] )?([A-Za-z0-9_]{1,16}): .*$", Pattern.DOTALL);
    private static final ReferenceQueue<IChatComponent> QUEUE = new ReferenceQueue<IChatComponent>();
    private static final Map<IdentityReference, Sender> METADATA = new HashMap<IdentityReference, Sender>();
    private OnlineChatMetadata() { }

    private static final class IdentityReference extends WeakReference<IChatComponent> {
        private final int hash;
        IdentityReference(IChatComponent value, ReferenceQueue<IChatComponent> queue) { super(value, queue); hash = System.identityHashCode(value); }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return this == other || other instanceof IdentityReference && get() != null && get() == ((IdentityReference)other).get();
        }
    }
    public static synchronized Sender get(IChatComponent component) {
        drain(); return component == null ? null : METADATA.get(new IdentityReference(component, null));
    }
    public static synchronized void put(IChatComponent component, Sender sender) {
        drain(); if (component != null && sender != null) METADATA.put(new IdentityReference(component, QUEUE), sender);
    }
    private static void drain() {
        IdentityReference collected;
        while ((collected = (IdentityReference)QUEUE.poll()) != null) METADATA.remove(collected);
    }
    public static void suppress(IChatComponent component) { put(component, new Sender(null, true, true)); }
    public static void record(IChatComponent component, byte type) {
        if (component == null || type != 0) return;
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() == null) return;
        
        UUID sender = resolve(component, false, name -> {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(name);
            return info == null || info.getGameProfile() == null ? null : info.getGameProfile().getId();
        });
        if (sender != null) put(component, new Sender(sender, true, false));
    }
    public static UUID resolve(IChatComponent component, boolean hypixel, Function<String, UUID> lookup) {
        if (component == null || lookup == null) return null;
        if (component instanceof ChatComponentTranslation) {
            ChatComponentTranslation translation = (ChatComponentTranslation)component;
            if (!"chat.type.text".equals(translation.getKey()) || translation.getFormatArgs().length < 2) return null;
            Object arg = translation.getFormatArgs()[0];
            if (arg instanceof IChatComponent && isObfuscated((IChatComponent)arg)) return null;
            String name = arg instanceof IChatComponent ? ((IChatComponent)arg).getUnformattedText() : String.valueOf(arg);
            if (name.matches("(?s).*\u00a7[kK].*")) return null;
            return name.matches("[A-Za-z0-9_]{1,16}") ? lookup.apply(name) : null;
        }
        if (!hypixel || isObfuscated(component)) return null;
        Matcher match = PUBLIC_HYPIXEL.matcher(component.getUnformattedText().replaceAll("\u00a7.", ""));
        return match.matches() ? lookup.apply(match.group(1)) : null;
    }
    public static void propagate(IChatComponent original, List<IChatComponent> lines) {
        Sender source = get(original);
        if (source == null) return;
        for (int i = 0; i < lines.size(); i++) put(lines.get(i), new Sender(source.uuid, source.firstLine && i == 0, source.suppressed));
    }
    private static boolean isObfuscated(IChatComponent component) {
        return component.getChatStyle().getObfuscated() || component.getFormattedText().matches("(?s).*\u00a7[kK].*");
    }
}
