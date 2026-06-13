package royale.client.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import royale.events.impl.PacketEvent;
import royale.modules.impl.misc.Debug;
import royale.modules.impl.render.Hud;
import royale.screens.hud.CoolDowns;
import royale.screens.hud.DebugOverlay;
import royale.screens.hud.HotKeys;
import royale.screens.hud.Info;
import royale.screens.hud.Inventory;
import royale.screens.hud.Notifications;
import royale.screens.hud.Potions;
import royale.screens.hud.Staff;
import royale.screens.hud.TargetHud;
import royale.screens.hud.Watermark;
import royale.util.config.impl.drag.DragConfig;

public class HudManager {
    private final List<HudElement> elements = new ArrayList<>();
    private final List<HudElement> enabledScratch = new ArrayList<>();
    private boolean initialized = false;

    public void initElements() {
        if (this.initialized) {
            return;
        }

        this.register(new Watermark());
        this.register(new DebugOverlay());

        this.register(new HotKeys());
        this.register(new Notifications());
        this.register(new Potions());
        this.register(new CoolDowns());
        this.register(new TargetHud());
        this.register(new Info());
        this.register(new Staff());
        this.register(new Inventory());

        this.initialized = true;
        DragConfig.getInstance().load();
    }

    public void register(HudElement element) {
        this.elements.add(element);
    }

    public void onPacket(PacketEvent e) {
        for (HudElement element : this.elements) {
            element.onPacket(e);
        }
    }

    public void render(DrawContext context, float tickDelta, int mouseX, int mouseY) {
        this.collectEnabledElements(this.enabledScratch);
        for (HudElement element : this.enabledScratch) {
            element.render(context, tickDelta);
        }
    }

    public void tick() {
        this.collectEnabledElements(this.enabledScratch);
        for (HudElement element : this.enabledScratch) {
            element.tick();
        }
    }

    public void collectEnabledElements(List<HudElement> out) {
        out.clear();

        Hud hud = Hud.getInstance();
        Debug debug = Debug.getInstance();
        boolean debugStandalone = debug != null && debug.isState();

        for (HudElement element : this.elements) {
            boolean hudEnabled = hud != null && hud.isState() && hud.interfaceSettings.isSelected(element.getName());
            boolean standaloneDebugOverlay = debugStandalone && element instanceof DebugOverlay;
            if (hudEnabled || standaloneDebugOverlay) {
                out.add(element);
            }
        }
    }

    public HudElement getElementAt(double mouseX, double mouseY) {
        this.collectEnabledElements(this.enabledScratch);

        for (int i = this.enabledScratch.size() - 1; i >= 0; --i) {
            HudElement element = this.enabledScratch.get(i);
            if (!element.visible()) {
                continue;
            }

            if (mouseX < element.getX() || mouseX > element.getX() + element.getWidth()) {
                continue;
            }

            if (mouseY < element.getY() || mouseY > element.getY() + element.getHeight()) {
                continue;
            }

            return element;
        }

        return null;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.collectEnabledElements(this.enabledScratch);

        for (HudElement element : this.enabledScratch) {
            if (element.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    public void saveConfig() {
        DragConfig.getInstance().save();
    }

    public void loadConfig() {
        DragConfig.getInstance().load();
    }

    public List<HudElement> getElements() {
        return this.elements;
    }

    public List<HudElement> getEnabledElements() {
        List<HudElement> enabled = new ArrayList<>();
        this.collectEnabledElements(enabled);
        return enabled;
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}
