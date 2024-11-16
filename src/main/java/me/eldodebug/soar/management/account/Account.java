package me.eldodebug.soar.management.account;

import java.io.File;

public class Account {

	private String name, uuid, refreshToken;
	private File skinFile;
	private AccountType type;
	
	public Account(String name, String uuid, String refreshToken, AccountType type) {
		this.name = name;
		this.uuid = uuid;
		this.refreshToken = refreshToken;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getUuid() {
		return uuid;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public AccountType getType() {
		return type;
	}

	public File getSkinFile() {
		return skinFile;
	}

	public void setSkinFile(File skinFile) {
		this.skinFile = skinFile;
	}
}
