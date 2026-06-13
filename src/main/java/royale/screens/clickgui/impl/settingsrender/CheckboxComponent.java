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

    public CheckboxComponent(BooleanSetting setting) {
        super((Setting) setting);
        this.booleanSetting = setting;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int iconAlpha = (int) (200.0F * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("T", this.x + 0.5F, this.y + this.height / 2.0F - 11.0F, 11.0F, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.booleanSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());

        float switchHeight = 10.0F;
        float switchWidth = 16.0F;
        float switchX = this.x + this.width - switchWidth - 2.0F;
        float switchY = this.y + this.height / 2.0F - switchHeight / 2.0F;
        drawSettingDescription(this.booleanSetting.getDescription(), this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, switchX - this.x - 6.0F);

        boolean enabled = this.booleanSetting.isValue();
        Color track = enabled ? new Color(92, 92, 96, 225) : new Color(45, 45, 48, 210);
        Color outline = enabled ? new Color(150, 150, 154, 190) : new Color(96, 96, 100, 135);
        Color knob = enabled ? new Color(224, 224, 228, 255) : new Color(145, 145, 150, 245);

        Render2D.rect(switchX, switchY, switchWidth, switchHeight, applyAlpha(track).getRGB(), 4.0F);
        Render2D.outline(switchX, switchY, switchWidth, switchHeight, 0.5F, applyAlpha(outline).getRGB(), 4.0F);

        float knobSize = 7.0F;
        float padding = 1.5F;
        float knobX = enabled ? switchX + switchWidth - padding - knobSize : switchX + padding;
        float knobY = switchY + (switchHeight - knobSize) / 2.0F;
        Render2D.rect(knobX, knobY, knobSize, knobSize, applyAlpha(knob).getRGB(), knobSize / 2.0F);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY) && button == 0) {
            this.booleanSetting.setValue(!this.booleanSetting.isValue());
            return true;
        }
        return false;
    }

    public void tick() {}

    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }
}
