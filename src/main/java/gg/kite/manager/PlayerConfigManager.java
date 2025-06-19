package gg.kite.manager;

import com.google.inject.Inject;
import gg.kite.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerConfigManager {
    private final Main plugin;
    private final File playerDataFolder;
    private final ConcurrentHashMap<UUID, PlayerConfig> playerConfigs;
    private final boolean defaultNotificationsEnabled;
    private final boolean defaultSoundsEnabled;
    private final int defaultHealthBoostDuration;

    @Inject
    public PlayerConfigManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.playerConfigs = new ConcurrentHashMap<>();
        this.defaultNotificationsEnabled = plugin.getConfiguration().getBoolean("default-settings.notifications-enabled", true);
        this.defaultSoundsEnabled = plugin.getConfiguration().getBoolean("default-settings.sounds-enabled", true);
        this.defaultHealthBoostDuration = plugin.getConfiguration().getInt("default-settings.health-boost-duration", 30);

        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public PlayerConfig getPlayerConfig(UUID playerId) {
        return playerConfigs.computeIfAbsent(playerId, id -> {
            File playerFile = new File(playerDataFolder, id.toString() + ".yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            PlayerConfig playerConfig = new PlayerConfig(
                    config.getBoolean("notifications-enabled", defaultNotificationsEnabled),
                    config.getBoolean("sounds-enabled", defaultSoundsEnabled),
                    config.getInt("health-boost-duration", defaultHealthBoostDuration)
            );

            // Save initial config if it doesn't exist, without updating the map
            if (!playerFile.exists()) {
                savePlayerConfigInternal(id, playerConfig);
            }
            return playerConfig;
        });
    }

    public void savePlayerConfig(@NotNull UUID playerId, @NotNull PlayerConfig config) {
        savePlayerConfigInternal(playerId, config);
        playerConfigs.put(playerId, config); // Update cache after saving
    }

    private void savePlayerConfigInternal(@NotNull UUID playerId, @NotNull PlayerConfig config) {
        File playerFile = new File(playerDataFolder, playerId + ".yml");
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.set("notifications-enabled", config.notificationsEnabled());
        fileConfig.set("sounds-enabled", config.soundsEnabled());
        fileConfig.set("health-boost-duration", config.healthBoostDuration());

        try {
            fileConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player config for " + playerId + ": " + e.getMessage());
        }
    }
}