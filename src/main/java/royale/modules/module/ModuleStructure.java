package royale.modules.module;

import net.minecraft.client.MinecraftClient;
import royale.IMinecraft;
import royale.events.api.EventManager;
import royale.events.api.events.Event;
import royale.events.impl.ModuleToggleEvent;
import royale.modules.impl.render.Hud;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.SettingRepository;
import royale.screens.hud.Notifications;
import royale.util.animations.Animation;
import royale.util.animations.Decelerate;
import royale.util.animations.Direction;
import royale.util.localization.DescriptionLocalizer;

public class ModuleStructure extends SettingRepository implements IMinecraft {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final Animation animation = (new Decelerate()).setMs(175).setValue(1.0D);

    private int key;
    private int type;

    public boolean state;
    public boolean favorite;

    public ModuleStructure(String name, ModuleCategory category) {
        this.key = -1;
        this.type = 1;
        this.name = name;
        this.category = category;
        this.description = buildDefaultDescription(name);
    }

    public ModuleStructure(String name, String description, ModuleCategory category) {
        this.key = -1;
        this.type = 1;
        this.name = name;
        this.description = (description == null || description.isBlank()) ? buildDefaultDescription(name) : description;
        this.category = category;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return DescriptionLocalizer.sanitizeDisplay(this.name);
    }

    public String getStorageName() {
        return this.name;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public String getDescription() {
        return DescriptionLocalizer.localizeModule(this.name, this.description);
    }

    public int getKey() {
        return this.key;
    }

    public int getType() {
        return this.type;
    }

    public boolean isState() {
        return this.state;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void switchState() {
        setState(!this.state);
    }

    public void setState(boolean state) {
        this.animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (state != this.state) {
            this.state = state;
            handleStateChange();
        }
    }

    public void switchFavorite() {
        setFavorite(!this.favorite);
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    private void handleStateChange() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.world != null) {
            Hud hud = Hud.getInstance();
            Notifications notifications = Notifications.getInstance();
            if (hud != null && hud.isState() && notifications != null && hud.interfaceSettings.isSelected("Notifications")) {
                if (this.state) {
                    notifications.addNotification("Функция " + getName() + " включена", 2000L);
                } else {
                    notifications.addNotification("Функция " + getName() + " выключена", 2000L);
                }
            }

            if (this.state) {
                activate();
            } else {
                deactivate();
            }
        }

        toggleSilent(this.state);
        ModuleToggleEvent event = new ModuleToggleEvent(this, this.state);
        EventManager.callEvent((Event) event);
    }

    private void toggleSilent(boolean activate) {
        if (activate) {
            EventManager.register(this);
        } else {
            EventManager.unregister(this);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    private static String buildDefaultDescription(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return "Настраивает функцию";
        }

        String readable = DescriptionLocalizer.sanitizeDisplay(moduleName)
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();
        if (readable.isEmpty()) {
            return "Настраивает функцию";
        }

        return "Управляет " + readable;
    }
}
