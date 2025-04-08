package com.wairesd.dcstackedcases;

import com.wairesd.dcstackedcases.listeners.InventoryListener;
import com.jodexindustries.donatecase.api.DCAPI;
import com.jodexindustries.donatecase.api.addon.InternalJavaAddon;
import com.jodexindustries.donatecase.api.data.subcommand.SubCommand;
import com.wairesd.dcstackedcases.managers.InventoryGuiManager;
import com.wairesd.dcstackedcases.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Main plugin class for DCStackedCases, integrating with DonateCase.
 */
public final class DCStackedCasesAddon extends InternalJavaAddon {
    private static final String COMMAND_NAME = "stackedcases";
    private static final String PERMISSION = "dcstackedcases.use";
    public static final NamespacedKey CASE_NAME_KEY = new NamespacedKey("dcstackedcases", "case_name");

    private final DCAPI api = DCAPI.getInstance();
    private InventoryGuiManager inventoryGuiManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onLoad() {
        getLogger().info("DCStackedCases is loading...");
    }

    @Override
    public void onEnable() {
        playerDataManager = new PlayerDataManager();
        inventoryGuiManager = new InventoryGuiManager(this, playerDataManager);

        // Register the "stackedcases" subcommand
        api.getSubCommandManager().register(SubCommand.builder()
                .addon(this)
                .name(COMMAND_NAME)
                .permission(PERMISSION)
                .executor((sender, label, args) -> {
                    org.bukkit.command.CommandSender bukkitSender = (org.bukkit.command.CommandSender) sender.getHandler();
                    if (!(bukkitSender instanceof Player)) {
                        sender.sendMessage("This command can only be used by players.");
                        return true;
                    }
                    Player player = (Player) bukkitSender;
                    inventoryGuiManager.openGui(player, 0);
                    return true;
                })
                .build());

        Plugin donateCase = Bukkit.getPluginManager().getPlugin("DonateCase");
        if (donateCase != null) {
            // Register inventory event listener
            Bukkit.getPluginManager().registerEvents(new InventoryListener(this, playerDataManager, inventoryGuiManager), donateCase);
        } else {
            getLogger().severe("DonateCase plugin not found!");
        }

        getLogger().info("DCStackedCases enabled successfully!");
    }
}