package dev.nullman.compass.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.nullman.compass.ManhuntCompassMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and persists {@link ModConfig} as JSON via Gson.
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("manhunt-compass.json");

    private static ConfigManager instance;

    private ModConfig config;

    private ConfigManager() {
        this.config = loadFromDisk();
    }

    public static ConfigManager get() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    public ModConfig getConfig() {
        return config;
    }

    public void reload() {
        config = loadFromDisk();
    }

    public void save() {
        config.sanitize();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            ManhuntCompassMod.LOGGER.error("Failed to save config to {}", CONFIG_PATH, exception);
        }
    }

    private ModConfig loadFromDisk() {
        if (!Files.exists(CONFIG_PATH)) {
            ModConfig defaults = new ModConfig();
            defaults.sanitize();
            writeToDisk(defaults);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded == null) {
                loaded = new ModConfig();
            }
            loaded.sanitize();
            return loaded;
        } catch (IOException exception) {
            ManhuntCompassMod.LOGGER.error("Failed to load config from {}, using defaults", CONFIG_PATH, exception);
            ModConfig defaults = new ModConfig();
            defaults.sanitize();
            return defaults;
        }
    }

    private void writeToDisk(ModConfig config) {
        this.config = config;
        save();
    }
}
