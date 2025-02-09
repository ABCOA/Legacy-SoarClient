package me.eldodebug.soar.management.command.impl;

import me.abcoc.soar.utils.buffer.StringUtil;
import me.eldodebug.soar.management.command.Command;
import me.eldodebug.soar.management.mods.impl.RewardClaimer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class ClaimCommand extends Command {

    public ClaimCommand() {
        super("claim");
    }

    @Override
    public void onCommand(String number) {
        new Thread(() -> {
            String urlStr = "https://rewards.hypixel.net/claim-reward/claim?option=" + number + "&id=" + RewardClaimer.id + "&activeAd=" + RewardClaimer.activeAd
                    + "&_csrf=" + RewardClaimer.csrfToken + "&watchedFallback=false&skipped=0";
            try {
                URL url = new URL(urlStr);
                HttpURLConnection huc = (HttpURLConnection) new URL(urlStr).openConnection();
                huc.addRequestProperty("Cookie", StringUtil.cookieToString(RewardClaimer.cookies));
                huc.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0");
                huc.setRequestMethod("POST");
                huc.connect();
                System.out.println(RewardClaimer.cookies);
                System.out.println(huc.getResponseCode() + "");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
    }
}
