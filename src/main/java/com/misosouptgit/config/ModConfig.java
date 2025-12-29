package com.misosouptgit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("screenshottoclipboard.json");

    // Singleton instance for real-time access across the mod
    private static ModConfig instance;

    public boolean showMessage = true;
    public NotificationType notificationType = NotificationType.CHAT;

    public enum NotificationType {
        CHAT, TOAST
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ModConfig load() {
        File configFile = CONFIG_PATH.toFile();
        if (!configFile.exists()) {
            return createAndSaveDefault();
        }

        try (FileReader reader = new FileReader(configFile)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            return (config != null) ? config : createAndSaveDefault();
        } catch (Exception e) {
            System.err.println("[ScreenshotToClipboard] Failed to load config, using defaults.");
            return createAndSaveDefault();
        }
    }

    private static ModConfig createAndSaveDefault() {
        ModConfig defaultConfig = new ModConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            System.err.println("[ScreenshotToClipboard] Failed to save config.");
        }
    }
}