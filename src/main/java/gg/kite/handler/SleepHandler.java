package gg.kite.handler;

import com.google.inject.Inject;
import gg.kite.Main;
import gg.kite.manager.PlayerConfigManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class SleepHandler implements Listener {
    private final Main plugin;
    private final PlayerConfigManager configManager;
    private final ConcurrentHashMap<World, Integer> sleepingPlayers = new ConcurrentHashMap<>();

    @Inject
    public SleepHandler(Main plugin, PlayerConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerBedEnter(@NotNull PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();

        sleepingPlayers.compute(world, (k, v) -> (v == null) ? 1 : v + 1);

        if (configManager.getPlayerConfig(player.getUniqueId()).soundsEnabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_WOOL_STEP, 1.0f, 1.0f);
        }
        plugin.getAdventure().player(player).sendActionBar(
                Component.text("You climb into bed...", NamedTextColor.GOLD)
        );

        plugin.getAdventure().world(Key.key(world.getKey().asString())).filterAudience(audience -> {
            if (audience instanceof Player p && p != player) {
                if (configManager.getPlayerConfig(p.getUniqueId()).soundsEnabled()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_WOOL_STEP, 0.5f, 1.0f);
                }
                return configManager.getPlayerConfig(p.getUniqueId()).notificationsEnabled();
            }
            return false;
        }).sendMessage(
                Component.text()
                        .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" is now sleeping", NamedTextColor.GRAY))
                        .build()
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                if (sleepingPlayers.getOrDefault(world, 0) > 0) {
                    transitionToMorning(world);
                }
            }
        }.runTaskLater(plugin, 100L); // 5 seconds delay
    }

    @EventHandler
    public void onPlayerBedLeave(@NotNull PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        sleepingPlayers.computeIfPresent(world, (k, v) -> v > 1 ? v - 1 : null);

        if (configManager.getPlayerConfig(player.getUniqueId()).soundsEnabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_WOOL_STEP, 1.0f, 1.2f);
        }
        plugin.getAdventure().player(player).sendActionBar(
                Component.text("You feel refreshed!", NamedTextColor.GREEN)
        );

        applyHealthBoost(player);
    }

    private void transitionToMorning(@NotNull World world) {
        if (world.getTime() >= 12542 && world.getTime() <= 23460) {
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);

            Key worldKey = Key.key(world.getKey().asString());
            plugin.getAdventure().world(worldKey).filterAudience(audience -> {
                if (audience instanceof Player p) {
                    if (configManager.getPlayerConfig(p.getUniqueId()).soundsEnabled()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                    return configManager.getPlayerConfig(p.getUniqueId()).notificationsEnabled();
                }
                return false;
            }).sendMessage(
                    Component.text("The night has passed", NamedTextColor.GREEN)
            );

            plugin.getAdventure().world(worldKey).sendActionBar(
                    Component.text("A new day dawns!", NamedTextColor.YELLOW)
            );

            // Clear sleeping count
            sleepingPlayers.remove(world);
        }
    }

    private void applyHealthBoost(@NotNull Player player) {
        int duration = configManager.getPlayerConfig(player.getUniqueId()).healthBoostDuration();
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HEALTH_BOOST,
                duration * 20, // Convert seconds to ticks
                1,
                true,
                true
        ));

        if (configManager.getPlayerConfig(player.getUniqueId()).notificationsEnabled()) {
            plugin.getAdventure().player(player).sendMessage(
                    Component.text("You feel invigorated after a good sleep!", NamedTextColor.GREEN)
            );
        }
    }
}