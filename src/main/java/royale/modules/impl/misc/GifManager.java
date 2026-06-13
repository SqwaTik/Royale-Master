package royale.modules.impl.misc;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ButtonSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.screens.hud.Notifications;
import royale.util.Instance;
import royale.util.render.gif.GifRender;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GifManager extends ModuleStructure {
    private static final long SOURCE_REFRESH_INTERVAL_MS = 1800L;
    private long lastSourceRefreshAt = 0L;

    public final SelectSetting avatarSet = (new SelectSetting(
            "Avatar GIF",
            "Selects the avatar animation for ClickGUI"
    )).value("Original").selected("Original");

    public final SelectSetting backgroundSet = (new SelectSetting(
            "Background GIF",
            "Selects the background animation for ClickGUI"
    )).value("Original").selected("Original");

    public final SelectSetting avatarStyle = (new SelectSetting(
            "Avatar",
            "Changes the ClickGUI avatar tint"
    )).value("Original", "Soft Pink", "Moonlight", "Sunset", "Aqua Glow", "Crimson Night").selected("Original");

    public final SelectSetting backgroundStyle = (new SelectSetting(
            "Background",
            "Changes the ClickGUI background tint"
    )).value("Original", "Warm Bloom", "Night Blue", "Violet Mist", "Aqua Wave", "Rose Dust").selected("Original");

    public final SliderSettings avatarSpeed = (new SliderSettings(
            "Avatar Speed",
            "Changes the avatar animation speed"
    )).range(0.55F, 1.85F).setValue(1.0F);

    public final SliderSettings backgroundSpeed = (new SliderSettings(
            "Background Speed",
            "Changes the background animation speed"
    )).range(0.55F, 1.85F).setValue(1.0F);

    public final ButtonSetting openAvatarFolder = (new ButtonSetting(
            "Avatar Folder",
            "Opens the folder with custom avatar files"
    )).setButtonName("Open").setRunnable(this::openAvatarFolder);

    public final ButtonSetting openBackgroundFolder = (new ButtonSetting(
            "Background Folder",
            "Opens the folder with custom background files"
    )).setButtonName("Open").setRunnable(this::openBackgroundFolder);

    public GifManager() {
        super("GifManager", "Changes ClickGUI animations", ModuleCategory.MISC);
        settings(new Setting[]{
                this.avatarSet,
                this.backgroundSet,
                this.avatarStyle,
                this.backgroundStyle,
                this.avatarSpeed,
                this.backgroundSpeed,
                this.openAvatarFolder,
                this.openBackgroundFolder
        });
        refreshAvailableSetsIfNeeded(true);
    }

    public static GifManager getInstance() {
        return Instance.get(GifManager.class);
    }

    public String getAvatarSetKey() {
        refreshAvailableSetsIfNeeded(false);
        String selected = this.avatarSet.getSelected();
        return selected == null || selected.isBlank() || "Original".equalsIgnoreCase(selected) ? "default" : selected;
    }

    public String getBackgroundSetKey() {
        refreshAvailableSetsIfNeeded(false);
        String selected = this.backgroundSet.getSelected();
        return selected == null || selected.isBlank() || "Original".equalsIgnoreCase(selected) ? "default" : selected;
    }

    public int getAvatarTint() {
        if (!isState()) {
            return 0xFFFFFFFF;
        }

        return switch (this.avatarStyle.getSelected()) {
            case "Soft Pink" -> 0xFFFFD2F2;
            case "Moonlight" -> 0xFFD6E4FF;
            case "Sunset" -> 0xFFFFD7BA;
            case "Aqua Glow" -> 0xFFCFFBFF;
            case "Crimson Night" -> 0xFFFFD1DC;
            default -> 0xFFFFFFFF;
        };
    }

    public int getBackgroundTint() {
        if (!isState()) {
            return 0xFFFFFFFF;
        }

        return switch (this.backgroundStyle.getSelected()) {
            case "Warm Bloom" -> 0xFFFFE2C7;
            case "Night Blue" -> 0xFFD7E4FF;
            case "Violet Mist" -> 0xFFE7DBFF;
            case "Aqua Wave" -> 0xFFD7FFF8;
            case "Rose Dust" -> 0xFFFFE1EC;
            default -> 0xFFFFFFFF;
        };
    }

    public long getAvatarFrameDelay() {
        return resolveDelay(33L, this.avatarSpeed.getValue());
    }

    public long getBackgroundFrameDelay() {
        return resolveDelay(50L, this.backgroundSpeed.getValue());
    }

    private void refreshAvailableSets() {
        GifRender.refreshCustomEntriesNow();
        this.avatarSet.setValues(buildAvailableOptions(true));
        this.backgroundSet.setValues(buildAvailableOptions(false));
    }

    private void refreshAvailableSetsIfNeeded(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - this.lastSourceRefreshAt < SOURCE_REFRESH_INTERVAL_MS) {
            return;
        }

        refreshAvailableSets();
        this.lastSourceRefreshAt = now;
    }

    private List<String> buildAvailableOptions(boolean avatar) {
        List<String> options = new ArrayList<>();
        options.add("Original");
        if (avatar) {
            options.addAll(GifRender.getAvailableAvatarSources());
        } else {
            options.addAll(GifRender.getAvailableBackgroundSources());
        }
        return options;
    }

    private void openAvatarFolder() {
        openFolder(resolveCustomRootDirectory(true));
    }

    private void openBackgroundFolder() {
        openFolder(resolveCustomRootDirectory(false));
    }

    private Path resolveCustomRootDirectory(boolean avatar) {
        return GifRender.getCustomGifRoot().resolve(avatar ? "avatar" : "background");
    }

    private void openFolder(Path directory) {
        try {
            Files.createDirectories(directory);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(directory.toFile());
            } else {
                new ProcessBuilder("explorer.exe", directory.toAbsolutePath().toString()).start();
            }

            Notifications notifications = Notifications.getInstance();
            if (notifications != null) {
                notifications.addNotification("GIF folder opened", 1800L);
            }
            refreshAvailableSetsIfNeeded(true);
        } catch (Exception exception) {
            Notifications notifications = Notifications.getInstance();
            if (notifications != null) {
                notifications.addNotification("Could not open GIF folder", 2200L);
            }
        }
    }

    private long resolveDelay(long baseDelay, float speed) {
        float safeSpeed = Math.max(0.2F, speed);
        return Math.max(12L, Math.round(baseDelay / safeSpeed));
    }
}
