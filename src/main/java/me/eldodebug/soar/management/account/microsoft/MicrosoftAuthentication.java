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
        System.out.println("loginWithRefreshToken");
//        JsonObject response = HttpUtils.readJson("https://login.live.com/oauth20_token.srf?client_id=d1ed1b72-9f7c-41bc-9702-365d2cbd2e38&grant_type=refresh_token&refresh_token=" + refreshToken, null);
        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.postURL("https://login.live.com/oauth20_token.srf?client_id=d1ed1b72-9f7c-41bc-9702-365d2cbd2e38&grant_type=refresh_token&refresh_token=" + refreshToken, null), JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if(response.get("access_token") == null) {
        	return;
        }

        getXboxLiveToken(response.get("access_token").getAsString(), refreshToken);
	}
	
	public void loginWithUrl(String url) {
		try {
            System.out.println("loginWithUrl");
			getMicrosoftToken(new URL(url));
		} catch (MalformedURLException e) {}
	}
	
	public void loginWithPopUpWindow(Runnable afterLogin) throws URISyntaxException, IOException {
		new MicrosoftLoginBrowser(afterLogin);
        System.out.println("loginWithPopUpWindow");
	}
	
    private void getMicrosoftToken(URL tokenURL) {
        System.out.println("Token URL: " + tokenURL.toString());
        String code = tokenURL.toString();


        code = code.substring(code.lastIndexOf('=') + 1);
        String token = "https://login.live.com/oauth20_token.srf";
        String oauth = null;
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", "d1ed1b72-9f7c-41bc-9702-365d2cbd2e38");
        tokenParams.put("code", code);
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("redirect_uri", "http://127.0.0.1:17342");
        try {
            oauth = Http.postURL(token, tokenParams);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println("oauth:"+oauth);

        String accessToken = Http.gson().fromJson(oauth, JsonObject.class).get("access_token").getAsString();
        String refreshToken = Http.gson().fromJson(oauth, JsonObject.class).get("refresh_token").getAsString();
        System.out.println(accessToken);
//        JsonObject response = HttpUtils.readJson("https://login.live.com/oauth20_token.srf?client_id=d1ed1b72-9f7c-41bc-9702-365d2cbd2e38&grant_type=authorization_code&http://127.0.0.1:17342&code=" + tokenURL.toString().split("=")[1], null);
        getXboxLiveToken(accessToken, refreshToken);
    }
    
    private void getXboxLiveToken(String accessToken, String refreshToken) {
        System.out.println("getXboxLiveToken");
        JsonObject xbl = null;
//        JsonObject properties = new JsonObject();
//        properties.addProperty("AuthMethod", "RPS");
//        properties.addProperty("SiteName", "user.auth.xboxlive.com");
//        properties.addProperty("RpsTicket", "d=" + token);

//        JsonObject request = new JsonObject();
//        request.add("Properties", properties);
//        request.addProperty("RelyingParty", "http://auth.xboxlive.com");
//        request.addProperty("TokenType", "JWT");
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //        JsonObject response = HttpUtils.postJson("https://user.auth.xboxlive.com/user/authenticate", request);
        String xbl_token = xbl.get("Token").getAsString();

        getXSTS(xbl_token, refreshToken);
    }
    
    private void getXSTS(String xbl_token, String refreshToken) {
//        System.out.println("getXSTS");
//        JsonPrimitive jsonToken = new JsonPrimitive(token);
//        JsonArray userTokens = new JsonArray();
//        userTokens.add(jsonToken);
//
//        JsonObject properties = new JsonObject();
//        properties.addProperty("SandboxId", "RETAIL");
//        properties.add("UserTokens", userTokens);
//
//        JsonObject request = new JsonObject();
//        request.add("Properties", properties);
//        request.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
//        request.addProperty("TokenType", "JWT");
//
//        JsonObject response = HttpUtils.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", request);
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String xstsToken = xsts.get("Token").getAsString();
        String xstsUhs = xsts.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();
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

            getMinecraftToken(xstsUhs, xstsToken, refreshToken);
        }
    }
    
    private void getMinecraftToken(String xstsUhs, String xstsToken, String refreshToken) {
        System.out.println("getMinecraftToken");
//        JsonObject request = new JsonObject();
//        request.addProperty("identityToken", String.format("XBL3.0 x=%s;%s", uhs, token));
//
//        JsonObject response = HttpUtils.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", request);
        JsonObject mcJson = null;
        Map<String, Object> loginParams = new HashMap<>();
        loginParams.put("identityToken", String.format("XBL3.0 x=%s;%s", xstsUhs, xstsToken));
        try {
            mcJson = Http.gson().fromJson(Http.postJSON("https://api.minecraftservices.com/authentication/login_with_xbox", loginParams), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String mcToken = mcJson.get("access_token").getAsString();

        checkMinecraftOwnership(mcToken, refreshToken);
    }
    
    private void checkMinecraftOwnership(String mcToken, String refreshToken) {
        System.out.println("checkMinecraftOwnership");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + mcToken);
        boolean ownsMinecraft = false;
        
//        JsonObject request = HttpUtils.readJson("https://api.minecraftservices.com/entitlements/mcstore", headers);
        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.get("https://api.minecraftservices.com/entitlements/mcstore", headers), JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response);
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
        System.out.println("getMinecraftProfile");
    	Soar instance = Soar.getInstance();
    	AccountManager accountManager = instance.getAccountManager();
    	FileManager fileManager = instance.getFileManager();
    	File headDir = new File(fileManager.getCacheDir(), "head");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        
//        JsonObject request = HttpUtils.readJson("https://api.minecraftservices.com/minecraft/profile", headers);
        JsonObject response = null;
        try {
            response = Http.gson().fromJson(Http.get("https://api.minecraftservices.com/minecraft/profile", headers), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = response.get("name").getAsString();
        String uuid = response.get("id").getAsString();
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
        System.out.println("check");

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
