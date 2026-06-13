package royale.screens.pause;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import royale.IMinecraft;
import royale.util.ColorUtil;
import royale.util.render.Render2D;

public class BackgroundPauseScreen extends Screen implements IMinecraft {

    private static final Text TITLE_TEXT = Text.literal("Пауза");
    private static final Text HINT_TEXT = Text.literal("Нажмите любую клавишу или подвигайте мышь");

    public BackgroundPauseScreen() {
        super(Text.literal("background_pause"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int fixedWidth = Render2D.getFixedScaledWidth();
        int fixedHeight = Render2D.getFixedScaledHeight();

        Render2D.blur(0.0F, 0.0F, fixedWidth, fixedHeight, 8.0F, ColorUtil.rgba(10, 16, 26, 115));
        Render2D.rect(0.0F, 0.0F, fixedWidth, fixedHeight, ColorUtil.rgba(0, 0, 0, 110), 0.0F);

        int centerY = this.height / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, TITLE_TEXT, this.width / 2, centerY - 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, HINT_TEXT, this.width / 2, centerY + 7, 0xFFD0D6E0);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        closeFromInput();
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        closeFromInput();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        closeFromInput();
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        closeFromInput();
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        closeFromInput();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void closeFromInput() {
        if (mc.currentScreen == this) {
            mc.setScreen(null);
        }
    }
}
