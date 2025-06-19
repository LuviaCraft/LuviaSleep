package gg.kite;

import com.google.inject.Guice;
import com.google.inject.Injector;
import gg.kite.handler.SleepHandler;
import gg.kite.module.LuviaSleepModule;
import gg.kite.command.LuviaSleepCommand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private Injector injector;
    private BukkitAudiences adventure;
    private FileConfiguration config;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        this.config = getConfig();

        this.injector = Guice.createInjector(new LuviaSleepModule(this));
        this.adventure = BukkitAudiences.create(this);

        injector.getInstance(SleepHandler.class).register();
        Objects.requireNonNull(getCommand("luviasleep")).setExecutor(injector.getInstance(LuviaSleepCommand.class));
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public FileConfiguration getConfiguration() {
        return config;
    }
}