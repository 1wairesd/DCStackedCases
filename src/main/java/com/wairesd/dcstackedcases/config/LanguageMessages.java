package com.wairesd.dcstackedcases.config;

public class LanguageMessages {
    private final String forwardItemDisplayName, backItemDisplayName, defaultLoreKeys, notKeys, menuTitle;

    public LanguageMessages(String forward, String back, String keys, String notKeys, String title) {
        this.forwardItemDisplayName = forward;
        this.backItemDisplayName = back;
        this.defaultLoreKeys = keys;
        this.notKeys = notKeys;
        this.menuTitle = title;
    }

    public LanguageMessages() { this("&cForward", "&cBack", "&7Keys", "&cYou have no keys", "Page %d"); }

    public String getForwardItemDisplayName() { return forwardItemDisplayName; }
    public String getBackItemDisplayName() { return backItemDisplayName; }
    public String getDefaultLoreKeys() { return defaultLoreKeys; }
    public String getNotKeys() { return notKeys; }
    public String getMenuTitle() { return menuTitle; }
}