package com.wairesd.dcstackedcases;

import com.jodexindustries.donatecase.api.DCAPI;
import com.jodexindustries.donatecase.api.addon.InternalJavaAddon;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommand;
import com.jodexindustries.donatecase.api.event.Subscriber;
import com.jodexindustries.donatecase.api.event.plugin.DonateCaseReloadEvent;
import com.wairesd.dcstackedcases.config.ConfigManager;
import com.wairesd.dcstackedcases.config.MaterialsConfig;
import com.wairesd.dcstackedcases.listener.InventoryListener;
import com.wairesd.dcstackedcases.manager.InventoryGuiManager;
import com.wairesd.dcstackedcases.manager.PlayerDataManager;
import net.kyori.event.method.annotation.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;

public final class DCStackedCasesAddon extends InternalJavaAddon implements Subscriber {
    private static final String COMMAND_NAME = "stackedcases";
    private static final String PERMISSION = "dcstackedcases.use";
    public static final NamespacedKey CASE_NAME_KEY = new NamespacedKey("dcstackedcases", "case_name");

    private final DCAPI api = DCAPI.getInstance();
    private InventoryGuiManager inventoryGuiManager;
    private PlayerDataManager playerDataManager;
    private MaterialsConfig materialsConfig;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this, getDataFolder().toPath());
        if (configManager.getConfigVersion() != 1 || !configManager.getConfigType().equals("CONFIG"))
            getLogger().warning("Неверная версия или тип конфига");
        if (configManager.isDebug()) getLogger().info("Debug включен");

        api.getEventBus().register(this);

        File materialsFile = new File(getDataFolder(), "materials.yml");
        if (!materialsFile.exists()) saveResource("materials.yml", false);
        try {
            materialsConfig = new MaterialsConfig(materialsFile);
        } catch (IOException e) {
            getLogger().severe("Ошибка загрузки materials.yml");
            return;
        }

        playerDataManager = new PlayerDataManager();
        inventoryGuiManager = new InventoryGuiManager(this, playerDataManager, configManager);

        api.getSubCommandManager().register(SubCommand.builder()
                .addon(this).name(COMMAND_NAME).permission(PERMISSION)
                .executor((s, l, a) -> {
                    if (!(s.getHandler() instanceof Player p)) { s.sendMessage("Только для игроков"); return true; }
                    inventoryGuiManager.openGui(p, 0);
                    return true;
                }).build());

        Plugin donateCase = Bukkit.getPluginManager().getPlugin("DonateCase");
        if (donateCase != null) {
            Bukkit.getPluginManager().registerEvents(
                    new InventoryListener(this, playerDataManager, inventoryGuiManager, configManager.getCurrentLanguageMessages(), configManager),
                    donateCase
            );
        } else getLogger().severe("DonateCase не найден");

        getLogger().info("DCStackedCases успешно запущен");
    }

    @Subscribe
    public void onDonateCaseReload(DonateCaseReloadEvent e) {
        configManager.reload();
        try {
            materialsConfig = new MaterialsConfig(new File(getDataFolder(), "materials.yml"));
        } catch (IOException ex) {
            getLogger().severe("Ошибка перезагрузки materials.yml");
        }
    }

    public MaterialsConfig getMaterialsConfig() { return materialsConfig; }
}