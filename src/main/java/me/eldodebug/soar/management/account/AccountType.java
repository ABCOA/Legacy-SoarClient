package me.eldodebug.soar.management.account;

public enum AccountType {
	MICROSOFT(1), OFFLINE(0);
	
	private int id;
	
	private AccountType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public static AccountType getAccountTypeById(int id) {
		
		for(AccountType acc : AccountType.values()) {
			if(acc.getId() == id) {
				return acc;
			}
		}
		
		return null;
	}
}
