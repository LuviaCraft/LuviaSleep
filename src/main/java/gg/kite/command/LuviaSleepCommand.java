package gg.kite.command;

import com.google.inject.Inject;
import gg.kite.Main;
import gg.kite.manager.PlayerConfig;
import gg.kite.manager.PlayerConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LuviaSleepCommand implements CommandExecutor, Listener {
    private static final String GUI_TITLE = "LuviaSleep Settings";
    private final Main plugin;
    private final PlayerConfigManager configManager;

    @Inject
    public LuviaSleepCommand(@NotNull Main plugin, PlayerConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        openSettingsGUI(player);
        return true;
    }

    private void openSettingsGUI(@NotNull Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(GUI_TITLE));
        var config = configManager.getPlayerConfig(player.getUniqueId());

        ItemStack notificationsItem = new ItemStack(config.notificationsEnabled() ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta notificationsMeta = notificationsItem.getItemMeta();
        notificationsMeta.displayName(Component.text("Sleep Notifications", NamedTextColor.YELLOW)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        notificationsMeta.lore(List.of(
                Component.text(config.notificationsEnabled() ? "Enabled" : "Disabled", NamedTextColor.GRAY)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                Component.text("Click to toggle", NamedTextColor.BLUE)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));
        notificationsItem.setItemMeta(notificationsMeta);
        gui.setItem(11, notificationsItem);

        ItemStack soundsItem = new ItemStack(config.soundsEnabled() ? Material.NOTE_BLOCK : Material.JUKEBOX);
        ItemMeta soundsMeta = soundsItem.getItemMeta();
        soundsMeta.displayName(Component.text("Sound Effects", NamedTextColor.YELLOW)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        soundsMeta.lore(List.of(
                Component.text(config.soundsEnabled() ? "Enabled" : "Disabled", NamedTextColor.GRAY)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                Component.text("Click to toggle", NamedTextColor.BLUE)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));
        soundsItem.setItemMeta(soundsMeta);
        gui.setItem(13, soundsItem);

        ItemStack durationItem = new ItemStack(Material.CLOCK);
        ItemMeta durationMeta = durationItem.getItemMeta();
        durationMeta.displayName(Component.text("Health Boost Duration", NamedTextColor.YELLOW)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        durationMeta.lore(List.of(
                Component.text(config.healthBoostDuration() + " seconds", NamedTextColor.GRAY)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false),
                Component.text("Left-click: +10s, Right-click: -10s", NamedTextColor.BLUE)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        ));
        durationItem.setItemMeta(durationMeta);
        gui.setItem(15, durationItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(GUI_TITLE))) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getCurrentItem() == null) {
            return;
        }

        var config = configManager.getPlayerConfig(player.getUniqueId());
        int slot = event.getSlot();

        if (slot == 11) {
            var newConfig = new PlayerConfig(!config.notificationsEnabled(), config.soundsEnabled(), config.healthBoostDuration());
            configManager.savePlayerConfig(player.getUniqueId(), newConfig);
            player.sendMessage(Component.text("Notifications " + (!config.notificationsEnabled() ? "enabled" : "disabled"), NamedTextColor.GREEN));
            openSettingsGUI(player);
        } else if (slot == 13) {
            var newConfig = new PlayerConfig(config.notificationsEnabled(), !config.soundsEnabled(), config.healthBoostDuration());
            configManager.savePlayerConfig(player.getUniqueId(), newConfig);
            player.sendMessage(Component.text("Sounds " + (!config.soundsEnabled() ? "enabled" : "disabled"), NamedTextColor.GREEN));
            openSettingsGUI(player);
        } else if (slot == 15) {
            int newDuration = config.healthBoostDuration();
            if (event.isLeftClick()) {
                newDuration = Math.min(newDuration + 10, 300); // Max 5 minutes
            } else if (event.isRightClick()) {
                newDuration = Math.max(newDuration - 10, 10); // Min 10 seconds
            }
            var newConfig = new PlayerConfig(config.notificationsEnabled(), config.soundsEnabled(), newDuration);
            configManager.savePlayerConfig(player.getUniqueId(), newConfig);
            player.sendMessage(Component.text("Health boost duration set to " + newDuration + " seconds", NamedTextColor.GREEN));
            openSettingsGUI(player);
        }
    }

    public Main getPlugin() {
        return plugin;
    }
}