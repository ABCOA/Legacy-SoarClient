package me.abcoc.soar.event;

import me.eldodebug.soar.management.account.AccountType;
import me.eldodebug.soar.management.event.Event;

public class EventAccountSwitch extends Event {

    private String currentAccount;
    private AccountType accountType;

    public EventAccountSwitch(String currentAccount, AccountType accountType) {
        this.currentAccount = currentAccount;
        this.accountType = accountType;
    }

    public String getCurrentAccount() {
        return this.currentAccount;
    }

    public AccountType getAccountType() {
        return this.accountType;
    }


}
