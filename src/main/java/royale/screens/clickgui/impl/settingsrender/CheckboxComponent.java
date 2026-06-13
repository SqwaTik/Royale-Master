package royale.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.util.interfaces.AbstractSettingComponent;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;

public class CheckboxComponent extends AbstractSettingComponent {
    private final BooleanSetting booleanSetting;
    private float toggleProgress;
    private boolean toggleProgressInitialized;

    public CheckboxComponent(BooleanSetting setting) {
        super((Setting) setting);
        this.booleanSetting = setting;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.toggleProgressInitialized) {
            this.toggleProgress = this.booleanSetting.isValue() ? 1.0F : 0.0F;
            this.toggleProgressInitialized = true;
        }

        float target = this.booleanSetting.isValue() ? 1.0F : 0.0F;
        float speed = Math.min(1.0F, Math.max(0.0F, delta) * 0.28F + 0.18F);
        this.toggleProgress += (target - this.toggleProgress) * speed;
        if (Math.abs(target - this.toggleProgress) < 0.003F) {
            this.toggleProgress = target;
        }

        int iconAlpha = (int) (200.0F * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("T", this.x + 0.5F, this.y + this.height / 2.0F - 11.0F, 11.0F, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.booleanSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());

        float switchHeight = 10.0F;
        float switchWidth = 16.0F;
        float switchX = this.x + this.width - switchWidth - 2.0F;
        float switchY = this.y + this.height / 2.0F - switchHeight / 2.0F;
        drawSettingDescription(this.booleanSetting.getDescription(), this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, switchX - this.x - 6.0F);

        Color track = lerpColor(new Color(45, 45, 48, 210), new Color(82, 82, 86, 225), this.toggleProgress);
        Color outline = lerpColor(new Color(96, 96, 100, 135), new Color(150, 150, 154, 190), this.toggleProgress);
        Color knob = lerpColor(new Color(145, 145, 150, 245), new Color(224, 224, 228, 255), this.toggleProgress);

        Render2D.rect(switchX, switchY, switchWidth, switchHeight, applyAlpha(track).getRGB(), 4.0F);
        Render2D.outline(switchX, switchY, switchWidth, switchHeight, 0.5F, applyAlpha(outline).getRGB(), 4.0F);

        float knobSize = 7.0F;
        float padding = 1.5F;
        float knobX = switchX + padding + (switchWidth - padding * 2.0F - knobSize) * this.toggleProgress;
        float knobY = switchY + (switchHeight - knobSize) / 2.0F;
        Render2D.rect(knobX, knobY, knobSize, knobSize, applyAlpha(knob).getRGB(), knobSize / 2.0F);
    }

    private Color lerpColor(Color from, Color to, float progress) {
        float clamped = Math.min(1.0F, Math.max(0.0F, progress));
        int red = (int) (from.getRed() + (to.getRed() - from.getRed()) * clamped);
        int green = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int blue = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
        int alpha = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * clamped);
        return new Color(red, green, blue, alpha);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isSwitchHover(mouseX, mouseY) && button == 0) {
            this.booleanSetting.setValue(!this.booleanSetting.isValue());
            return true;
        }
        return false;
    }

    public void tick() {}

    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    private boolean isSwitchHover(double mouseX, double mouseY) {
        float switchHeight = 10.0F;
        float switchWidth = 16.0F;
        float switchX = this.x + this.width - switchWidth - 2.0F;
        float switchY = this.y + this.height / 2.0F - switchHeight / 2.0F;
        return mouseX >= switchX && mouseX <= switchX + switchWidth && mouseY >= switchY && mouseY <= switchY + switchHeight;
    }
}
