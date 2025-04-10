package com.wairesd.dcstackedcases.config;

import com.wairesd.dcstackedcases.DCStackedCasesAddon;
import lombok.Getter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public class ConfigManager {
    private final DCStackedCasesAddon addon;
    private ConfigurationNode config;
    private final File configFile, langDir;
    @Getter
    private LanguageMessages currentLanguageMessages;

    public ConfigManager(DCStackedCasesAddon addon, Path configDir) {
        this.addon = addon;
        this.configFile = configDir.resolve("config.yml").toFile();
        this.langDir = configDir.resolve("lang").toFile();
        langDir.mkdirs();
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!configFile.exists()) addon.saveResource("config.yml", false);
            this.config = YamlConfigurationLoader.builder().file(configFile).build().load();
            loadCurrentLanguage(config.node("DCStackedCases", "Languages").getString("en_US"));
        } catch (IOException e) {
            addon.getLogger().log(Level.SEVERE, "Ошибка загрузки config.yml", e);
        }
    }

    public void reload() {
        loadConfig();
    }

    private void loadCurrentLanguage(String lang) {
        File langFile = new File(langDir, lang + ".yml");
        try {
            if (!langFile.exists()) addon.saveResource("lang/" + lang + ".yml", false);
            ConfigurationNode langConfig = YamlConfigurationLoader.builder().file(langFile).build().load();
            currentLanguageMessages = new LanguageMessages(
                    langConfig.node("forward_item_displayname").getString("&cForward"),
                    langConfig.node("back_item_displayname").getString("&cBack"),
                    langConfig.node("default_lore_keys").getString("&7Keys"),
                    langConfig.node("not_keys").getString("&cYou have no keys"),
                    langConfig.node("menu_title").getString("Page %d")
            );
        } catch (IOException e) {
            addon.getLogger().log(Level.WARNING, "Ошибка загрузки языка " + lang, e);
            loadFallbackLanguage();
        }
    }

    private void loadFallbackLanguage() {
        File langFile = new File(langDir, "en_US.yml");
        try {
            if (!langFile.exists()) addon.saveResource("lang/en_US.yml", false);
            ConfigurationNode langConfig = YamlConfigurationLoader.builder().file(langFile).build().load();
            currentLanguageMessages = new LanguageMessages(
                    langConfig.node("forward_item_displayname").getString("&cForward"),
                    langConfig.node("back_item_displayname").getString("&cBack"),
                    langConfig.node("default_lore_keys").getString("&7Keys"),
                    langConfig.node("not_keys").getString("&cYou have no keys"),
                    langConfig.node("menu_title").getString("Page %d")
            );
        } catch (IOException e) {
            addon.getLogger().log(Level.SEVERE, "Ошибка загрузки en_US", e);
            currentLanguageMessages = new LanguageMessages();
        }
    }

    public int getMenuSize() {
        return config.node("DCStackedCases", "settings", "menu_size").getInt(54);
    }

    public int getBackItemSlot() {
        return config.node("DCStackedCases", "settings", "back_item_slot").getInt(54);
    }

    public int getForwardItemSlot() {
        return config.node("DCStackedCases", "settings", "forward_item_slot").getInt(46);
    }

    public boolean isDebug() {
        return config.node("DCStackedCases", "debug").getBoolean(false);
    }

    public int getConfigVersion() {
        return config.node("config", "version").getInt(1);
    }

    public String getConfigType() {
        return config.node("config", "type").getString("CONFIG");
    }
}