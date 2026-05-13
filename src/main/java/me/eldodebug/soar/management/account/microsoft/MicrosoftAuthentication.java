package me.eldodebug.soar.management.account.microsoft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import me.abcoc.soar.utils.netwok.Http;
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
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MicrosoftAuthentication {
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private SkinDownloader skinDownloader;
	
	public MicrosoftAuthentication() {
		skinDownloader = new SkinDownloader();
	}
	
    public void loginWithRefreshToken(String refreshToken) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", "3ab948ab-bfab-4ab9-88c3-132e3d385e09");
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);

        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.postURL("https://login.live.com/oauth20_token.srf", params), JsonObject.class);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (response != null && response.has("access_token")) {
            String accessToken = response.get("access_token").getAsString();
            String nextRefreshToken = response.has("refresh_token")
                    ? response.get("refresh_token").getAsString()
                    : refreshToken;
            getXboxLiveToken(accessToken, nextRefreshToken);
        } else {
            SoarLogger.error("Failed to obtain access token via refresh: " + response);
        }

	}
	
	public void loginWithUrl(String url) {
		try {
			getMicrosoftToken(new URL(url));
		} catch (MalformedURLException e) {}
	}
	
	public void loginWithPopUpWindow(Runnable afterLogin) throws URISyntaxException, IOException {
		new MicrosoftLoginBrowser(afterLogin);
	}
	
    private void getMicrosoftToken(URL tokenURL) {
        String code = tokenURL.toString();

        code = code.substring(code.lastIndexOf('=') + 1);
        String token = "https://login.live.com/oauth20_token.srf";
        String oauth = null;
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", "3ab948ab-bfab-4ab9-88c3-132e3d385e09");
        tokenParams.put("code", code);
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("redirect_uri", "http://127.0.0.1:39802");
        try {
            oauth = Http.postURL(token, tokenParams);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        JsonObject oauthJson = Http.gson().fromJson(oauth, JsonObject.class);
        String accessToken = oauthJson.get("access_token").getAsString();
        String refreshToken = oauthJson.get("refresh_token").getAsString();
        getXboxLiveToken(accessToken, refreshToken);
    }
    
    private void getXboxLiveToken(String accessToken, String refreshToken) {
        JsonObject xbl = null;
        Map<String, Object> xblParams = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", "d=" + accessToken);
        xblParams.put("Properties", properties);
        xblParams.put("RelyingParty", "http://auth.xboxlive.com");
        xblParams.put("TokenType", "JWT");
        try {
            xbl = Http.gson().fromJson(Http.postJSON("https://user.auth.xboxlive.com/user/authenticate", xblParams), JsonObject.class);
        } catch (IOException | URISyntaxException e) {
            SoarLogger.error("Failed to obtain Xbox Live token", e);
            return;
        }
        if (xbl == null || !xbl.has("Token")) {
            SoarLogger.error("Xbox Live authentication returned no token: " + xbl);
            return;
        }
        String xbl_token = xbl.get("Token").getAsString();

        getXSTS(xbl_token, refreshToken);
    }
    
    private void getXSTS(String xbl_token, String refreshToken) {

        JsonObject xsts = null;
        Map<String, Object> xstsParams = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put("SandboxId", "RETAIL");
        properties.put("UserTokens", new String[]{xbl_token});
        xstsParams.put("Properties", properties);
        xstsParams.put("RelyingParty", "rp://api.minecraftservices.com/");
        xstsParams.put("TokenType", "JWT");
        try {
            xsts = Http.gson().fromJson(Http.postJSON("https://xsts.auth.xboxlive.com/xsts/authorize", xstsParams), JsonObject.class);
        } catch (IOException | URISyntaxException e) {
            SoarLogger.error("Failed to obtain XSTS token", e);
            return;
        }

        if (xsts == null) {
            SoarLogger.error("XSTS response was empty");
            return;
        }
        if (xsts.has("XErr")) {
            switch (xsts.get("XErr").getAsString()) {
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
            String xstsToken = xsts.get("Token").getAsString();
            String xstsUhs = xsts.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

            getMinecraftToken(xstsUhs, xstsToken, refreshToken);
        }
    }
    
    private void getMinecraftToken(String xstsUhs, String xstsToken, String refreshToken) {
        JsonObject mcJson = null;
        Map<String, Object> loginParams = new HashMap<>();
        loginParams.put("identityToken", String.format("XBL3.0 x=%s;%s", xstsUhs, xstsToken));
        try {
            mcJson = Http.gson().fromJson(Http.postJSON("https://api.minecraftservices.com/authentication/login_with_xbox", loginParams), JsonObject.class);
        } catch (IOException | URISyntaxException e) {
            SoarLogger.error("Failed to obtain Minecraft token", e);
            return;
        }
        if (mcJson == null || !mcJson.has("access_token")) {
            SoarLogger.error("Minecraft authentication returned no access token: " + mcJson);
            return;
        }
        String mcToken = mcJson.get("access_token").getAsString();

        checkMinecraftOwnership(mcToken, refreshToken);
    }
    
    private void checkMinecraftOwnership(String mcToken, String refreshToken) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + mcToken);
        boolean ownsMinecraft = false;

        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.get("https://api.minecraftservices.com/entitlements/mcstore", headers), JsonObject.class);
        } catch (IOException e) {
            SoarLogger.error("Failed to check Minecraft ownership", e);
            return;
        }
        if (response != null && response.has("items")) {
            for (JsonElement item : response.getAsJsonArray("items")) {
                String itemName = item.getAsJsonObject().get("name").getAsString();
                if ("product_minecraft".equals(itemName) || "game_minecraft".equals(itemName)) {
                    ownsMinecraft = true;
                    break;
                }
            }
        }


        if (!ownsMinecraft) {
        	SoarLogger.error("User doesn't own Minecraft");
        } else {
        	getMinecraftProfile(mcToken, refreshToken);
        }
    }
    
    private void getMinecraftProfile(String token, String refreshToken) {
    	Soar instance = Soar.getInstance();
    	AccountManager accountManager = instance.getAccountManager();
    	FileManager fileManager = instance.getFileManager();
    	File headDir = new File(fileManager.getCacheDir(), "head");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.get("https://api.minecraftservices.com/minecraft/profile", headers), JsonObject.class);
        } catch (IOException e) {
            SoarLogger.error("Failed to obtain Minecraft profile", e);
            return;
        }
        if (response == null || !response.has("name") || !response.has("id")) {
            SoarLogger.error("Minecraft profile response was invalid: " + response);
            return;
        }
        String name = response.get("name").getAsString();
        String uuid = response.get("id").getAsString();
        Account account = new Account(name, uuid, refreshToken, AccountType.MICROSOFT);
        
        if(!headDir.exists()) {
        	fileManager.createDir(headDir);
        }
        
        skinDownloader.downloadFace(headDir, name, UUIDTypeAdapter.fromString(uuid));
        
        ((IMixinMinecraft) mc).setSession(new Session(name, uuid, token, "mojang"));

        if(accountManager.getAccountByName(account.getName()) != null) {
            accountManager.getAccounts().remove(accountManager.getAccountByName(account.getName()));
        }
        accountManager.getAccounts().add(account);
        accountManager.setCurrentAccount(account);
        accountManager.save();

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
