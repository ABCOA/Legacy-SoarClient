package me.eldodebug.soar.management.mods.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.abcoc.soar.utils.buffer.StringUtil;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventReceivePacket;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardClaimer extends Mod {
    public static String id;
    public static String csrfToken;
    public static int activeAd;
    public static List<String> cookies = new ArrayList<>();

    public RewardClaimer() {
        super(TranslateText.REWARD_CLAIMER, TranslateText.REWARD_CLAIMER_DESCRIPTION, ModCategory.HYPIXEL);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if(event.getPacket() instanceof S02PacketChat) {
            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            event.setCancelled(true);
            Minecraft mc = Minecraft.getMinecraft();
            IChatComponent chatComponent = chatPacket.getChatComponent();
            String chatMessage = chatComponent.getUnformattedText();
            if(chatMessage.contains("https://rewards.hypixel.net/claim-reward/")) {
                new Thread(() -> {
                    String[] split1 = chatMessage.split("/");
                    String[] split2 = split1[split1.length - 1].split("\n");
                    String rewardStr = split2[0];
                    String urlStr = "https://rewards.hypixel.net/claim-reward/" + rewardStr;
                    String result = null;
                    try {
                        cookies.clear();
                        URL url = new URL(urlStr);
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        huc.setRequestMethod("GET");
                        huc.setRequestProperty("User-Agent", "Mozilla/5.0");

                        Map<String, List<String>> headerFields = huc.getHeaderFields();
                        List<String> setCookieHeaders = headerFields.get("Set-Cookie");
                        if (setCookieHeaders != null) {
                            cookies.addAll(setCookieHeaders);
                        }

                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(huc.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                response.append(line);
                            }
                            result = StringUtil.ExtractString(response.toString(), "window.appData = '", "';");
                            csrfToken = StringUtil.ExtractString(response.toString(), "window.securityToken = \"", "\";");
                            JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
                            id = jsonObject.get("id").getAsString();
                            activeAd = jsonObject.get("activeAd").getAsInt();
                            JsonArray rewardsArray = jsonObject.getAsJsonArray("rewards");
                            int number = 0;
                            for(JsonElement rewardElement : rewardsArray) {
                                JsonObject rewardObject = rewardElement.getAsJsonObject();
                                String gameType = rewardObject.has("gameType") ? rewardObject.get("gameType").getAsString() : "";
                                String amount = rewardObject.has("amount") ? rewardObject.get("amount").getAsString() : "";
                                String rarity = rewardObject.has("rarity") ? rewardObject.get("rarity").getAsString() : "";
                                String reward = rewardObject.has("reward") ? rewardObject.get("reward").getAsString() : "";
                                String color = null;
                                switch (rarity) {
                                    case "COMMON":
                                        color = "§f";
                                        break;
                                    case "RARE":
                                        color = "§9";
                                        break;
                                    case "EPIC":
                                        color = "§5";
                                        break;
                                    case "LEGENDARY":
                                        color = "§6";
                                        break;
                                }

                                IChatComponent rewardComponent = new ChatComponentText( "§l" + color + "[" + rarity + " " + gameType + " " +reward + " " + "X" + " " + amount + "]")
                                        .setChatStyle(new ChatStyle()
                                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd claim " + number))
                                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_CLAIM_THIS.getText()))));
                                mc.ingameGUI.getChatGUI().printChatMessage(rewardComponent);
                                number ++;
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }
    }
}
