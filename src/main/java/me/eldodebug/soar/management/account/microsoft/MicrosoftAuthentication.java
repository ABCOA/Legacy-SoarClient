package me.eldodebug.soar.management.account.microsoft;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.util.UUIDTypeAdapter;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.SoarAPI;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.account.Account;
import me.eldodebug.soar.management.account.AccountManager;
import me.eldodebug.soar.management.account.AccountType;
import me.eldodebug.soar.management.account.skin.SkinDownloader;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.utils.network.HttpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class MicrosoftAuthentication {
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private SkinDownloader skinDownloader;
	
	public MicrosoftAuthentication() {
		skinDownloader = new SkinDownloader();
	}
	
	public void loginWithRefreshToken(String refreshToken) {
		
        JsonObject response = HttpUtils.readJson("https://login.live.com/oauth20_token.srf?client_id=00000000402b5328&grant_type=refresh_token&refresh_token=" + refreshToken, null);

        if(response.get("access_token") == null) {
        	return;
        }

        getXboxLiveToken(response.get("access_token").getAsString(), refreshToken);
	}
	
	public void loginWithUrl(String url) {
		try {
			getMicrosoftToken(new URL(url));
		} catch (MalformedURLException e) {}
	}
	
	public void loginWithPopUpWindow(Runnable afterLogin) {
		new MicrosoftLoginWindow(afterLogin);
	}
	
    private void getMicrosoftToken(URL tokenURL) {
    	
        JsonObject response = HttpUtils.readJson("https://login.live.com/oauth20_token.srf?client_id=00000000402b5328&grant_type=authorization_code&redirect_uri=https://login.live.com/oauth20_desktop.srf&code=" + tokenURL.toString().split("=")[1], null);

        getXboxLiveToken(response.get("access_token").getAsString(), response.get("refresh_token").getAsString());
    }
    
    private void getXboxLiveToken(String token, String refreshToken) {
    	
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d=" + token);

        JsonObject request = new JsonObject();
        request.add("Properties", properties);
        request.addProperty("RelyingParty", "http://auth.xboxlive.com");
        request.addProperty("TokenType", "JWT");

        JsonObject response = HttpUtils.postJson("https://user.auth.xboxlive.com/user/authenticate", request);
        
        getXSTS(response.get("Token").getAsString(), refreshToken);
    }
    
    private void getXSTS(String token, String refreshToken) {
    	
        JsonPrimitive jsonToken = new JsonPrimitive(token);
        JsonArray userTokens = new JsonArray();
        userTokens.add(jsonToken);

        JsonObject properties = new JsonObject();
        properties.addProperty("SandboxId", "RETAIL");
        properties.add("UserTokens", userTokens);

        JsonObject request = new JsonObject();
        request.add("Properties", properties);
        request.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        request.addProperty("TokenType", "JWT");

        JsonObject response = HttpUtils.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", request);
        
        if (response.has("XErr")) {
            switch (response.get("XErr").getAsString()) {
                case "2148916233":
                	SoarLogger.error("This account doesn't have an Xbox account.");
                    break;
                case "2148916235":
                	SoarLogger.error("Xbox isn't available in your country.");
                    break;
                case "2148916238":
                	SoarLogger.error("The account is under 18 and must be added to a Family (https://start.ui.xboxlive.com/AddChildToFamily)");
                    break;
            }
        } else {
            getMinecraftToken(response.getAsJsonObject("DisplayClaims").get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString(), response.get("Token").getAsString(), refreshToken);
        }
    }
    
    private void getMinecraftToken(String uhs, String token, String refreshToken) {

        JsonObject request = new JsonObject();
        request.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", uhs, token));

        JsonObject response = HttpUtils.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", request);

        checkMinecraftOwnership(response.get("access_token").getAsString(), refreshToken);
    }
    
    private void checkMinecraftOwnership(String token, String refreshToken) {
        Map<String, String> headers = new HashMap<>();
        boolean ownsMinecraft = false;

        headers.put("Authorization", "Bearer " + token);
        
        JsonObject request = HttpUtils.readJson("https://api.minecraftservices.com/entitlements/mcstore", headers);

        for (int i = 0; i < request.get("items").getAsJsonArray().size(); i++) {
            String itemName = request.get("items").getAsJsonArray().get(i).getAsJsonObject().get("name").getAsString();
            if (itemName.equals("product_minecraft") || itemName.equals("game_minecraft")) {
                ownsMinecraft = true;
                break;
            }
        }

        if (!ownsMinecraft) {
        	SoarLogger.error("User doesn't own Minecraft");
        } else {
        	getMinecraftProfile(token, refreshToken);
        }
    }
    
    private void getMinecraftProfile(String token, String refreshToken) {
    	
    	Soar instance = Soar.getInstance();
    	AccountManager accountManager = instance.getAccountManager();
    	FileManager fileManager = instance.getFileManager();
    	File headDir = new File(fileManager.getCacheDir(), "head");
    	
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        
        JsonObject request = HttpUtils.readJson("https://api.minecraftservices.com/minecraft/profile", headers);

        String name = request.get("name").getAsString();
        String uuid = request.get("id").getAsString();
        Account account = new Account(name, uuid, refreshToken, AccountType.MICROSOFT);
        
        if(!headDir.exists()) {
        	fileManager.createDir(headDir);
        }
        
        skinDownloader.downloadFace(headDir, name, UUIDTypeAdapter.fromString(uuid));
        
        ((IMixinMinecraft) mc).setSession(new Session(name, uuid, token, "mojang"));
        
		if(accountManager.getAccountByName(account.getName()) == null) {
            accountManager.getAccounts().add(account);
		}
        
        accountManager.setCurrentAccount(account);

        check();
    }

    private void check() {

        Soar instance = Soar.getInstance();
        SoarAPI api = Soar.getInstance().getApi();
        CapeManager capeManager = instance.getCapeManager();
        BackgroundManager backgroundManager = instance.getProfileManager().getBackgroundManager();

        if(!api.isSpecialUser()) {

            if(capeManager.getCurrentCape().isPremium()) {
                capeManager.setCurrentCape(capeManager.getCapeByName("None"));
            }

            if(backgroundManager.getCurrentBackground() instanceof CustomBackground) {
                backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(0));
            }
        }
    }

    public SkinDownloader getSkinDownloader() {
		return skinDownloader;
	}
}
