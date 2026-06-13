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
    private float checkAnimation = 0.0F;
    private float hoverAnimation = 0.0F;
    private float stretchAnimation = 0.0F;
    private float velocity = 0.0F;
    private float glowAnimation = 0.0F;
    private float clickRipple = 0.0F;
    private float bounceAnimation = 0.0F;
    private float hueOffset = 0.0F;
    private long lastUpdate = System.currentTimeMillis();

    public CheckboxComponent(BooleanSetting setting) {
        super((Setting)setting);
        this.booleanSetting = setting;
        this.checkAnimation = setting.isValue() ? 1.0F : 0.0F;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float dt = getDeltaTime();
        boolean hovered = isHover(mouseX, mouseY);
        this.hoverAnimation = lerp(this.hoverAnimation, hovered ? 1.0F : 0.0F, dt * 10.0F);
        float target = this.booleanSetting.isValue() ? 1.0F : 0.0F;
        float oldCheck = this.checkAnimation;
        this.checkAnimation = lerp(this.checkAnimation, target, dt * 12.0F);
        this.velocity = this.checkAnimation - oldCheck;
        float absVelocity = Math.abs(this.velocity);
        this.stretchAnimation = lerp(this.stretchAnimation, clamp(absVelocity * 30.0F, 0.0F, 1.0F), dt * (absVelocity > 0.1F ? 20.0F : 8.0F));
        float glowTarget = this.checkAnimation * (0.4F + this.hoverAnimation * 0.6F);
        this.glowAnimation = lerp(this.glowAnimation, glowTarget, dt * 8.0F);
        this.clickRipple = lerp(this.clickRipple, 0.0F, dt * 6.0F);
        this.bounceAnimation = lerp(this.bounceAnimation, 0.0F, dt * 15.0F);
        this.hueOffset += dt * 60.0F;
        if (this.hueOffset > 360.0F) this.hueOffset -= 360.0F;

        int iconAlpha = (int)(200.0F * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("T", this.x + 0.5F, this.y + this.height / 2.0F - 11.0F, 11.0F, (new Color(210, 210, 210, iconAlpha)).getRGB());
        Fonts.BOLD.draw(this.booleanSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, applyAlpha(new Color(210, 210, 220, 200)).getRGB());

        float checkboxSize = 10.0F;
        float checkboxWidth = checkboxSize + 6.0F;
        float checkboxX = this.x + this.width - checkboxWidth - 2.0F;
        drawSettingDescription(this.booleanSetting.getDescription(), this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, checkboxX - this.x - 6.0F);
        float checkboxY = this.y + this.height / 2.0F - checkboxSize / 2.0F + this.bounceAnimation * 2.0F;

        float trackAlpha = 25 + (int)(this.hoverAnimation * 20.0F);
        Color trackColor = new Color(55, 55, 55, (int)(trackAlpha * this.alphaMultiplier));
        Render2D.rect(checkboxX, checkboxY, checkboxWidth, checkboxSize, trackColor.getRGB(), 4.0F);

        Color trackOn = Color.getHSBColor(this.hueOffset / 360.0F, 0.5F, 0.6F);
        int trackOnAlpha = (int)(70.0F * this.checkAnimation * this.alphaMultiplier);
        Color trackOnColor = new Color(trackOn.getRed(), trackOn.getGreen(), trackOn.getBlue(), trackOnAlpha);
        Render2D.rect(checkboxX, checkboxY, checkboxWidth * this.checkAnimation, checkboxSize, trackOnColor.getRGB(), 4.0F);

        int outlineAlpha = 60 + (int)(this.hoverAnimation * 60.0F) + (int)(this.checkAnimation * 50.0F);
        Color outlineColor = lerpColor(new Color(155, 155, 155, outlineAlpha), trackOn, this.checkAnimation * 0.5F);
        Render2D.outline(checkboxX, checkboxY, checkboxWidth, checkboxSize, 0.5F, applyAlpha(new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), clamp(outlineAlpha, 0, 255))).getRGB(), 4.0F);

        float glowSize = checkboxWidth * 1.5F * this.glowAnimation;
        if (glowSize > 0.5F) {
            int glowAlpha = (int)(40.0F * this.glowAnimation * this.alphaMultiplier);
            Color glowColor = Color.getHSBColor(this.hueOffset / 360.0F, 0.6F, 0.9F);
            Render2D.rect(checkboxX - (glowSize - checkboxWidth) / 2.0F, checkboxY - (glowSize - checkboxSize) / 2.0F, glowSize, glowSize, new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), glowAlpha).getRGB(), glowSize / 2.0F);
        }

        float knobBaseSize = checkboxSize - 3.0F;
        float stretchExtra = this.stretchAnimation * 4.0F;
        float knobWidth = knobBaseSize + stretchExtra;
        float knobHeight = knobBaseSize - this.stretchAnimation * 1.0F;
        float padding = 1.5F;
        float travelDistance = checkboxWidth - knobBaseSize - padding * 2.0F;
        float knobBaseX = checkboxX + padding;
        float stretchOffset;
        if (this.velocity > 0.0F) {
            stretchOffset = -stretchExtra * 0.3F;
        } else if (this.velocity < 0.0F) {
            stretchOffset = stretchExtra * 0.3F;
        } else {
            stretchOffset = 0.0F;
        }
        float knobX = knobBaseX + travelDistance * this.checkAnimation - stretchExtra * this.checkAnimation + stretchOffset;
        float knobY = checkboxY + (checkboxSize - knobHeight) / 2.0F;

        Color knobColor = lerpColor(new Color(180, 180, 180), Color.getHSBColor(this.hueOffset / 360.0F, 0.7F, 0.9F), this.checkAnimation);
        float knobShadow = 2.0F;
        Render2D.rect(knobX + 0.5F, knobY + 0.5F, knobWidth, knobHeight, new Color(0, 0, 0, (int)(40.0F * this.alphaMultiplier)).getRGB(), knobShadow);
        Render2D.rect(knobX, knobY, knobWidth, knobHeight, applyAlpha(knobColor).getRGB(), 4.0F);
    }

    private float getDeltaTime() {
        long now = System.currentTimeMillis();
        float dt = Math.min((float)(now - this.lastUpdate) / 1000.0F, 0.05F);
        this.lastUpdate = now;
        return dt;
    }

    private float lerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001F) return target;
        return current + diff * Math.min(speed, 1.0F);
    }

    private Color lerpColor(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(clamp(r, 0, 255), clamp(g, 0, 255), clamp(bl, 0, 255), clamp(al, 0, 255));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY) && button == 0) {
            this.booleanSetting.setValue(!this.booleanSetting.isValue());
            this.clickRipple = 1.0F;
            this.bounceAnimation = 1.0F;
            return true;
        }
        return false;
    }

    public void tick() {}

    public boolean isHover(double mouseX, double mouseY) {
        return (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height));
    }
}


