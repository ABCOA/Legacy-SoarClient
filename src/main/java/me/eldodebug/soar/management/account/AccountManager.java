package me.eldodebug.soar.management.account;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.account.microsoft.MicrosoftAuthentication;
import me.eldodebug.soar.management.account.skin.SkinDownloader;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.utils.JsonUtils;
import me.eldodebug.soar.utils.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class AccountManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private ArrayList<Account> accounts = new ArrayList<Account>();
	private MicrosoftAuthentication authenticator = new MicrosoftAuthentication();
    private SkinDownloader skinDownloader = new SkinDownloader();
	private String currentAccount;
	
	public AccountManager() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		File microsoftDir = new File(fileManager.getExternalDir(), "microsoft");
		File accountFile = new File(fileManager.getSoarDir(), "Account.json");
		File skinDir = new File(fileManager.getCacheDir(), "skin");
		
		if(!microsoftDir.exists()) {
			fileManager.createDir(microsoftDir);
		}
		
		if(!accountFile.exists()) {
			fileManager.createFile(accountFile);
		}
		
		if(!skinDir.exists()) {
			fileManager.createDir(skinDir);
		}
		
		if(accountFile.length() > 0) {
			load();
		}

        if (syncLaunchSessionAccount(skinDir)) {
            save();
            return;
        }

		if(getAccountByName(currentAccount) != null) {
			
			if(getAccountByName(currentAccount).getType().equals(AccountType.MICROSOFT)) {
				Multithreading.runAsync(()-> {
					authenticator.loginWithRefreshToken(getAccountByName(currentAccount).getRefreshToken());
				});
			}else {
				
				Account acc = getAccountByName(currentAccount);
				File f = new File(skinDir, acc.getName() + ".png");
				
		        ((IMixinMinecraft) mc).setSession(new Session(acc.getName(), normalizeUuid(acc.getUuid()), "0", "mojang"));
		        
		        if(f.exists()) {
		        	acc.setSkinFile(f);
		        } else {
					mc.getTextureManager().bindTexture(new ResourceLocation("textures/entity/steve.png"));
		        }
			}
		}
	}

    private boolean syncLaunchSessionAccount(File skinDir) {
        Session session = mc.getSession();
        FileManager fileManager = Soar.getInstance().getFileManager();
        File headDir = new File(fileManager.getCacheDir(), "head");

        if (session == null || session.getUsername() == null || session.getUsername().trim().isEmpty()) {
            return false;
        }

        String username = session.getUsername().trim();
        String uuid = normalizeUuid(session.getPlayerID());
        Account account = getAccountByName(username);

        if (account == null && !"0".equals(uuid)) {
            account = getAccountByUuid(uuid);
        }

        if (account == null) {
            account = new Account(username, uuid, "0", AccountType.OFFLINE);
            accounts.add(account);
        } else if (!username.equals(account.getName()) || !uuid.equals(normalizeUuid(account.getUuid()))) {
            Account syncedAccount = new Account(username, uuid, account.getRefreshToken(), account.getType());
            accounts.remove(account);
            accounts.add(syncedAccount);
            account = syncedAccount;
        }

        File skinFile = new File(skinDir, account.getName() + ".png");
        if (skinFile.exists()) {
            account.setSkinFile(skinFile);
        }
        if (!headDir.exists()) {
            fileManager.createDir(headDir);
        }
        File headFile = new File(headDir, account.getName() + ".png");
        if (!headFile.exists() && !"0".equals(uuid)) {
            final String accountName = account.getName();
            final String accountUuid = uuid;
            Multithreading.runAsync(() -> skinDownloader.downloadFace(headDir, accountName, UUIDTypeAdapter.fromString(accountUuid)));
        }

        currentAccount = account.getName();
        SoarLogger.info("Synced launch session account: " + currentAccount + " (" + uuid + ")");
        return true;
    }
	
	public void save() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		
		try (FileWriter writer = new FileWriter(new File(fileManager.getSoarDir(), "Account.json"))) {
			 
			Gson gson = new Gson();
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();
			
			jsonObject.addProperty("Current Account", currentAccount);
			
			for(Account acc : accounts) {
				
				JsonObject accJsonObject = new JsonObject();
				
				accJsonObject.addProperty("Name", acc.getName());
				accJsonObject.addProperty("UUID", acc.getUuid());
				accJsonObject.addProperty("Refresh Token", acc.getRefreshToken());
				accJsonObject.addProperty("Account Type", acc.getType().getId());
				
				jsonArray.add(accJsonObject);
			}
			
			jsonObject.add("Accounts", jsonArray);
			
			gson.toJson(jsonObject, writer);
		} catch (Exception e) {
			SoarLogger.error("Failed to save account", e);
		}
	}
	
	public void load() {
		
		FileManager fileManager = Soar.getInstance().getFileManager();
		
		try (FileReader reader = new FileReader(new File(fileManager.getSoarDir(), "Account.json"))) {
			
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
			
			if(jsonObject != null && jsonObject.isJsonObject()) {
				
				JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Accounts");
				
				currentAccount = JsonUtils.getStringProperty(jsonObject, "Current Account", "null");
				
				if(jsonArray != null) {
					
					Iterator<JsonElement> iterator = jsonArray.iterator();
					
					while(iterator.hasNext()) {
						
						JsonElement jsonElement = (JsonElement) iterator.next();
						JsonObject accJsonObject = gson.fromJson(jsonElement, JsonObject.class);
						
						accounts.add(new Account(JsonUtils.getStringProperty(accJsonObject, "Name", "null"), JsonUtils.getStringProperty(accJsonObject, "UUID", "null"),
								JsonUtils.getStringProperty(accJsonObject, "Refresh Token", "0"), AccountType.getAccountTypeById(JsonUtils.getIntProperty(accJsonObject, "Account Type", 0))));
					}
				}
			}
		} catch (Exception e) {
			SoarLogger.error("Failed to load account", e);
		}
	}
	
	public Account getCurrentAccount() {
		return getAccountByName(currentAccount);
	}
	
	public void setCurrentAccount(Account account) {
		this.currentAccount = account.getName();
	}
	
	public Account getAccountByName(String name) {
		
		for(Account acc : accounts) {
			if(acc.getName().equals(name)) {
				return acc;
			}
		}
		
		return null;
	}

    public Account getAccountByUuid(String uuid) {

        for(Account acc : accounts) {
            if(normalizeUuid(acc.getUuid()).equals(normalizeUuid(uuid))) {
                return acc;
            }
        }

        return null;
    }

    private String normalizeUuid(String uuid) {
        if(uuid == null || uuid.trim().isEmpty() || "null".equalsIgnoreCase(uuid)) {
            return "0";
        }

        return uuid.replace("-", "");
    }
	
	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public MicrosoftAuthentication getAuthenticator() {
		return authenticator;
	}
}
