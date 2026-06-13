package royale.screens.clickgui.impl.configs.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import royale.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import royale.util.config.ConfigSystem;
import royale.util.config.impl.ConfigPath;

public class ConfigDataHandler {
    private static final String CONFIG_EXTENSION = ".cfg";
    private static final String LEGACY_EXTENSION = ".json";
    private static final String MAIN_CONFIG_NAME = "autoconfig";
    private static final String SELECTED_CONFIG_FILE = ".selected-config.txt";
    private final List<String> configs = new ArrayList<String>();
    private final ConfigAnimationHandler animationHandler;
    private String selectedConfig = null;
    private boolean isCreating = false;
    private String newConfigName = "";
    private String renamingConfig = null;
    private double scrollOffset = 0.0;
    private double targetScrollOffset = 0.0;
    private float scrollTopFade = 0.0f;
    private float scrollBottomFade = 0.0f;

    public ConfigDataHandler(ConfigAnimationHandler animationHandler) {
        this.animationHandler = animationHandler;
    }

    public void refreshConfigs() {
        ArrayList<String> oldConfigs = new ArrayList<String>(this.configs);
        this.configs.clear();
        Path configDir = ConfigPath.getConfigDirectory();
        if (!Files.exists(configDir, new LinkOption[0])) {
            return;
        }
        this.migrateLegacyConfigs(configDir);
        try (Stream<Path> files2 = Files.list(configDir);){
            files2.filter(path -> path.toString().endsWith(CONFIG_EXTENSION)).forEach(path -> {
                String fileName = path.getFileName().toString();
                String configName = fileName.substring(0, fileName.length() - CONFIG_EXTENSION.length());
                if (!configName.equalsIgnoreCase(MAIN_CONFIG_NAME)) {
                    this.configs.add(configName);
                }
            });
        }
        catch (IOException files2) {
            // empty catch block
        }
        this.configs.sort(String.CASE_INSENSITIVE_ORDER);
        if (this.selectedConfig != null) {
            this.selectedConfig = this.findConfigIgnoreCase(this.selectedConfig);
        }
        if (this.selectedConfig == null) {
            String persisted = this.readPersistedSelectedConfig();
            this.selectedConfig = this.findConfigIgnoreCase(persisted);
        }
        if (this.selectedConfig == null && !this.configs.isEmpty()) {
            this.selectedConfig = this.configs.get(0);
        }
        for (String config : this.configs) {
            if (oldConfigs.contains(config)) continue;
            this.animationHandler.getItemAppearAnimations().put(config, Float.valueOf(0.0f));
        }
        this.persistSelectedConfig();
    }

    public void updateScroll(float deltaTime) {
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * 12.0 * (double)deltaTime;
    }

    public void updateScrollFades(float visibleHeight) {
        float contentHeight = (float)this.configs.size() * 27.0f;
        if (contentHeight <= visibleHeight) {
            this.scrollTopFade = 0.0f;
            this.scrollBottomFade = 0.0f;
            return;
        }
        float maxScroll = contentHeight - visibleHeight;
        this.scrollTopFade = (float)Math.min(1.0, -this.scrollOffset / 20.0);
        this.scrollBottomFade = (float)Math.min(1.0, ((double)maxScroll + this.scrollOffset) / 20.0);
    }

    public void handleScroll(double vertical, float visibleHeight) {
        float contentHeight = (float)this.configs.size() * 27.0f;
        float maxScroll = Math.max(0.0f, contentHeight - visibleHeight);
        this.targetScrollOffset += vertical * 25.0;
        this.targetScrollOffset = Math.max((double)(-maxScroll), Math.min(0.0, this.targetScrollOffset));
    }

    public boolean saveConfig(String name) {
        String normalizedName;
        String string = normalizedName = name == null ? "" : name.trim();
        if (!this.isValidName(normalizedName)) {
            return false;
        }
        if (normalizedName.equalsIgnoreCase(MAIN_CONFIG_NAME)) {
            return false;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Files.createDirectories(configDir, new FileAttribute[0]);
            Path newConfig = configDir.resolve(normalizedName + CONFIG_EXTENSION);
            if (Files.exists(newConfig, new LinkOption[0])) {
                return false;
            }
            ConfigSystem.getInstance().save();
            Path currentConfig = ConfigPath.getConfigFile();
            Files.copy(currentConfig, newConfig, StandardCopyOption.REPLACE_EXISTING);
            this.selectedConfig = normalizedName;
            this.persistSelectedConfig();
            this.refreshConfigs();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean loadConfig(String name) {
        String normalizedName;
        String string = normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            return false;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(normalizedName + CONFIG_EXTENSION);
            if (!Files.exists(configFile, new LinkOption[0])) {
                return false;
            }
            Path currentConfig = ConfigPath.getConfigFile();
            Files.copy(configFile, currentConfig, StandardCopyOption.REPLACE_EXISTING);
            ConfigSystem.getInstance().load();
            this.selectedConfig = normalizedName;
            this.persistSelectedConfig();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean refreshConfig(String name) {
        String normalizedName;
        String string = normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            return false;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(normalizedName + CONFIG_EXTENSION);
            if (!Files.exists(configFile, new LinkOption[0])) {
                return false;
            }
            ConfigSystem.getInstance().save();
            Path currentConfig = ConfigPath.getConfigFile();
            Files.copy(currentConfig, configFile, StandardCopyOption.REPLACE_EXISTING);
            this.selectedConfig = normalizedName;
            this.persistSelectedConfig();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean deleteConfig(String name) {
        String normalizedName;
        String string = normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            return false;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(normalizedName + CONFIG_EXTENSION);
            if (Files.exists(configFile, new LinkOption[0])) {
                Files.delete(configFile);
                if (normalizedName.equalsIgnoreCase(this.selectedConfig)) {
                    this.selectedConfig = null;
                }
                this.persistSelectedConfig();
                this.refreshConfigs();
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean renameConfig(String oldName, String newName) {
        String normalizedNew;
        String normalizedOld = oldName == null ? "" : oldName.trim();
        String string = normalizedNew = newName == null ? "" : newName.trim();
        if (normalizedOld.isEmpty() || normalizedNew.isEmpty()) {
            return false;
        }
        if (!this.isValidName(normalizedNew)) {
            return false;
        }
        if (normalizedNew.equalsIgnoreCase(MAIN_CONFIG_NAME)) {
            return false;
        }
        if (normalizedOld.equalsIgnoreCase(normalizedNew)) {
            this.selectedConfig = normalizedNew;
            this.persistSelectedConfig();
            this.refreshConfigs();
            return true;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path oldConfig = configDir.resolve(normalizedOld + CONFIG_EXTENSION);
            Path newConfig = configDir.resolve(normalizedNew + CONFIG_EXTENSION);
            if (!Files.exists(oldConfig, new LinkOption[0]) || Files.exists(newConfig, new LinkOption[0])) {
                return false;
            }
            Files.move(oldConfig, newConfig, StandardCopyOption.ATOMIC_MOVE);
            if (normalizedOld.equalsIgnoreCase(this.selectedConfig)) {
                this.selectedConfig = normalizedNew;
            }
            this.persistSelectedConfig();
            this.refreshConfigs();
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    public void toggleCreating() {
        if (this.isCreating) {
            this.cancelEditing();
            return;
        }
        this.startCreating();
    }

    public void startCreating() {
        this.isCreating = true;
        this.renamingConfig = null;
        this.newConfigName = "";
    }

    public void startRenaming(String configName) {
        this.isCreating = true;
        this.renamingConfig = configName;
        this.newConfigName = configName == null ? "" : configName;
    }

    public void cancelEditing() {
        this.isCreating = false;
        this.renamingConfig = null;
        this.newConfigName = "";
    }

    public boolean isRenaming() {
        return this.renamingConfig != null;
    }

    public void appendChar(char chr) {
        if (this.newConfigName.length() < 32 && this.isAllowedChar(chr)) {
            this.newConfigName = this.newConfigName + chr;
        }
    }

    public void removeLastChar() {
        if (!this.newConfigName.isEmpty()) {
            this.newConfigName = this.newConfigName.substring(0, this.newConfigName.length() - 1);
        }
    }

    public void clearNewConfigName() {
        this.newConfigName = "";
    }

    private boolean isAllowedChar(char chr) {
        return Character.isLetterOrDigit(chr) || chr == '_' || chr == '-';
    }

    private boolean isValidName(String name) {
        if (name == null || name.isBlank() || name.length() > 32) {
            return false;
        }
        for (int i = 0; i < name.length(); ++i) {
            if (this.isAllowedChar(name.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public boolean isSelectedConfig(String configName) {
        return configName != null && this.selectedConfig != null && configName.equalsIgnoreCase(this.selectedConfig);
    }

    private String findConfigIgnoreCase(String configName) {
        if (configName == null) {
            return null;
        }
        for (String config : this.configs) {
            if (!config.equalsIgnoreCase(configName)) continue;
            return config;
        }
        return null;
    }

    private Path getSelectedConfigPath() {
        return ConfigPath.getConfigDirectory().resolve(SELECTED_CONFIG_FILE);
    }

    private String readPersistedSelectedConfig() {
        try {
            Path path = this.getSelectedConfigPath();
            if (!Files.exists(path, new LinkOption[0])) {
                return null;
            }
            String value = Files.readString(path).trim();
            return value.isEmpty() ? null : value;
        }
        catch (IOException ignored) {
            return null;
        }
    }

    private void persistSelectedConfig() {
        try {
            Path dir = ConfigPath.getConfigDirectory();
            Files.createDirectories(dir, new FileAttribute[0]);
            Path path = this.getSelectedConfigPath();
            if (this.selectedConfig == null || this.selectedConfig.isBlank()) {
                Files.deleteIfExists(path);
                return;
            }
            Files.writeString(path, (CharSequence)this.selectedConfig.trim(), new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void migrateLegacyConfigs(Path configDir) {
        try (Stream<Path> files = Files.list(configDir);){
            files.filter(path -> path.toString().endsWith(LEGACY_EXTENSION)).forEach(path -> {
                String fileName = path.getFileName().toString();
                String configName = fileName.substring(0, fileName.length() - LEGACY_EXTENSION.length());
                if (configName.equalsIgnoreCase(MAIN_CONFIG_NAME)) {
                    return;
                }
                Path target = configDir.resolve(configName + CONFIG_EXTENSION);
                if (Files.exists(target, new LinkOption[0])) {
                    return;
                }
                try {
                    Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public List<String> getConfigs() {
        return this.configs;
    }

    public ConfigAnimationHandler getAnimationHandler() {
        return this.animationHandler;
    }

    public String getSelectedConfig() {
        return this.selectedConfig;
    }

    public boolean isCreating() {
        return this.isCreating;
    }

    public String getNewConfigName() {
        return this.newConfigName;
    }

    public String getRenamingConfig() {
        return this.renamingConfig;
    }

    public double getScrollOffset() {
        return this.scrollOffset;
    }

    public double getTargetScrollOffset() {
        return this.targetScrollOffset;
    }

    public float getScrollTopFade() {
        return this.scrollTopFade;
    }

    public float getScrollBottomFade() {
        return this.scrollBottomFade;
    }

    public void setSelectedConfig(String selectedConfig) {
        this.selectedConfig = selectedConfig;
    }

    public void setCreating(boolean isCreating) {
        this.isCreating = isCreating;
    }

    public void setNewConfigName(String newConfigName) {
        this.newConfigName = newConfigName;
    }

    public void setRenamingConfig(String renamingConfig) {
        this.renamingConfig = renamingConfig;
    }

    public void setScrollOffset(double scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public void setTargetScrollOffset(double targetScrollOffset) {
        this.targetScrollOffset = targetScrollOffset;
    }

    public void setScrollTopFade(float scrollTopFade) {
        this.scrollTopFade = scrollTopFade;
    }

    public void setScrollBottomFade(float scrollBottomFade) {
        this.scrollBottomFade = scrollBottomFade;
    }
}

