package dev.nullman.compass;

import dev.nullman.compass.command.CompassCommand;
import dev.nullman.compass.config.ConfigManager;
import dev.nullman.compass.config.ModConfig;
import dev.nullman.compass.event.CompassInteractionHandler;
import dev.nullman.compass.event.PlayerLifecycleHandler;
import dev.nullman.compass.tracking.TrackingService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManhuntCompassMod implements ModInitializer {
    public static final String MOD_ID = "manhunt_compass";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ConfigManager configManager;
    private static TrackingService trackingService;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Manhunt Compass");

        configManager = ConfigManager.get();
        trackingService = new TrackingService();

        CompassInteractionHandler.register();
        PlayerLifecycleHandler.register();

        ServerTickEvents.END_SERVER_TICK.register(trackingService::onServerTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LOGGER.info("Registering /pusula commands (environment: {})", environment);
            CompassCommand.register(dispatcher);
        });

        LOGGER.info("Manhunt Compass loaded (config: {}, update interval: {} ticks)",
                ConfigManager.getConfigPath(),
                getConfig().getUpdateInterval());
    }

    public static ModConfig getConfig() {
        return configManager.getConfig();
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static void reloadConfig() {
        configManager.reload();
        trackingService.resetTickCounter();
        LOGGER.info("Manhunt Compass config reloaded from {}", ConfigManager.getConfigPath());
    }

    public static TrackingService getTrackingService() {
        return trackingService;
    }
}
