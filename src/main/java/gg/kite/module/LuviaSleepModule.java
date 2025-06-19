package gg.kite.module;

import com.google.inject.AbstractModule;
import gg.kite.Main;
import gg.kite.command.LuviaSleepCommand;
import gg.kite.handler.SleepHandler;
import gg.kite.manager.PlayerConfigManager;

public class LuviaSleepModule extends AbstractModule {
    private final Main plugin;

    public LuviaSleepModule(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Main.class).toInstance(plugin);
        bind(SleepHandler.class).asEagerSingleton();
        bind(PlayerConfigManager.class).asEagerSingleton();
        bind(LuviaSleepCommand.class).asEagerSingleton();
    }
}