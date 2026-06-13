package royale.util.config;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import royale.util.config.impl.ConfigFileHandler;
import royale.util.config.impl.ConfigPath;
import royale.util.config.impl.ConfigSerializer;
import royale.util.config.impl.autosaver.ConfigAutoSaver;
import royale.util.config.impl.consolelogger.Logger;

public class ConfigSystem {
    private static ConfigSystem instance;
    private final ConfigSerializer serializer;
    private final ConfigFileHandler fileHandler;
    private final ConfigAutoSaver autoSaver;
    private final AtomicBoolean initialized;
    private final AtomicBoolean saving;
    private final AtomicBoolean shuttingDown;

    public ConfigSystem() {
        instance = this;
        this.serializer = new ConfigSerializer();
        this.fileHandler = new ConfigFileHandler();
        this.autoSaver = new ConfigAutoSaver(this::save);
        this.initialized = new AtomicBoolean(false);
        this.saving = new AtomicBoolean(false);
        this.shuttingDown = new AtomicBoolean(false);
    }

    public static ConfigSystem getInstance() {
        return instance;
    }

    public void init() {
        if (this.initialized.compareAndSet(false, true)) {
            ConfigPath.init();
            this.fileHandler.createDirectories();
            this.load();
            this.autoSaver.start();
            this.registerShutdownHook();
            Logger.success("AutoConfiguration: System initialized!");
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("AutoConfiguration: Shutdown detected, saving...");
            this.shutdown();
        }, "Royale-ConfigShutdown"));
    }

    public void save() {
        if (!this.initialized.get()) {
            return;
        }
        if (!this.saving.compareAndSet(false, true)) {
            return;
        }
        try {
            String data = this.serializer.serialize();
            boolean success = this.fileHandler.write(data);
            if (success) {
                Logger.success("AutoConfiguration: autoconfig.cfg saved successfully!");
            } else {
                Logger.error("AutoConfiguration: autoconfig.cfg save failed!");
            }
        }
        catch (Exception e) {
            Logger.error("AutoConfiguration: Save error! " + e.getMessage());
        }
        finally {
            this.saving.set(false);
        }
    }

    public CompletableFuture<Void> saveAsync() {
        return CompletableFuture.runAsync(this::save);
    }

    public void load() {
        this.migrateLegacyAutoConfig();
        if (!this.fileHandler.exists()) {
            Logger.info("AutoConfiguration: No config found, creating new...");
            this.save();
            return;
        }
        try {
            String data = this.fileHandler.read();
            if (data != null && !data.isEmpty()) {
                this.serializer.deserialize(data);
                Logger.success("AutoConfiguration: autoconfig.cfg loaded successfully!");
            }
        }
        catch (Exception e) {
            Logger.error("AutoConfiguration: Load error! " + e.getMessage());
        }
    }

    private void migrateLegacyAutoConfig() {
        try {
            if (this.fileHandler.exists()) {
                return;
            }
            Path legacyPath = ConfigPath.getLegacyConfigFile();
            if (Files.exists(legacyPath, new LinkOption[0])) {
                Files.copy(legacyPath, ConfigPath.getConfigFile(), StandardCopyOption.REPLACE_EXISTING);
                Logger.info("AutoConfiguration: Migrated legacy autoconfig.json to autoconfig.cfg");
            }
        }
        catch (Exception e) {
            Logger.error("AutoConfiguration: Legacy migration failed! " + e.getMessage());
        }
    }

    public void shutdown() {
        if (!this.initialized.get()) {
            return;
        }
        if (!this.shuttingDown.compareAndSet(false, true)) {
            return;
        }
        this.autoSaver.shutdown();
        this.save();
        this.initialized.set(false);
        Logger.success("AutoConfiguration: Shutdown complete!");
    }

    public void reload() {
        this.load();
        Logger.success("AutoConfiguration: Config reloaded!");
    }

    public boolean isInitialized() {
        return this.initialized.get();
    }

    public boolean isSaving() {
        return this.saving.get();
    }

    public ConfigAutoSaver getAutoSaver() {
        return this.autoSaver;
    }
}
