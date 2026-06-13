package royale.util.render.font;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import royale.Initialization;
public final class SafeTextRenderer
{
public static void draw(DrawContext context, Font font, String text, float x, float y, float size, int color) {
if (tryDrawCustom(font, text, x, y, size, color, false)) {
return;
}
drawVanilla(context, text, x, y, size, color, false);
}
public static void drawCentered(DrawContext context, Font font, String text, float centerX, float y, float size, int color) {
if (tryDrawCustom(font, text, centerX, y, size, color, true)) {
return;
}
drawVanilla(context, text, centerX, y, size, color, true);
}
public static float getWidth(Font font, String text, float size) {
if (canUseCustom(font, text)) {
return font.getWidth(text, size);
}
TextRenderer renderer = getVanillaRenderer();
if (renderer == null || text == null) {
return 0.0F;
}
return renderer.getWidth(text) * getVanillaScale(size);
}
public static float getHeight(Font font, float size) {
if (canUseCustom(font, "")) {
return font.getHeight(size);
}
return 9.0F * getVanillaScale(size);
}
public static boolean canUseCustom(Font font, String text) {
if (font == null) {
return false;
}
FontRenderer renderer = getFontRenderer();
if (renderer == null || !renderer.isInitialized()) {
return false;
}
FontAtlas atlas = renderer.getFont(font.getName());
if (atlas == null) {
return false;
}
atlas.ensureLoaded();
if (!atlas.isLoaded()) {
return false;
}
if (text == null || text.isEmpty()) {
return true;
}
return hasAllGlyphs(atlas, text);
}
private static boolean tryDrawCustom(Font font, String text, float x, float y, float size, int color, boolean centered) {
if (text == null || text.isEmpty() || !canUseCustom(font, text)) {
return false;
}
try {
if (centered) {
font.drawCentered(text, x, y, size, color);
} else {
font.draw(text, x, y, size, color);
} 
return true;
} catch (Throwable ignored) {
return false;
} 
}
private static void drawVanilla(DrawContext context, String text, float x, float y, float size, int color, boolean centered) {
if (context == null || text == null || text.isEmpty()) {
return;
}
TextRenderer renderer = getVanillaRenderer();
if (renderer == null) {
return;
}
float scale = getVanillaScale(size);
float width = renderer.getWidth(text) * scale;
float drawX = centered ? (x - width * 0.5F) : x;
float drawY = y;
if (Math.abs(scale - 1.0F) < 0.01F) {
context.drawText(renderer, text, Math.round(drawX), Math.round(drawY), color, false);
return;
} 
context.getMatrices().pushMatrix();
context.getMatrices().translate(drawX, drawY);
context.getMatrices().scale(scale, scale);
context.drawText(renderer, text, 0, 0, color, false);
context.getMatrices().popMatrix();
}
private static boolean hasAllGlyphs(FontAtlas atlas, String text) {
for (int i = 0; i < text.length(); ) {
int codePoint = text.codePointAt(i);
i += Character.charCount(codePoint);
if (Character.isWhitespace(codePoint)) {
continue;
}
if (!atlas.hasGlyph(codePoint)) {
return false;
}
} 
return true;
}
private static FontRenderer getFontRenderer() {
try {
if (Initialization.getInstance() == null || 
Initialization.getInstance().getManager() == null || 
Initialization.getInstance().getManager().getRenderCore() == null) {
return null;
}
return Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
} catch (Throwable ignored) {
return null;
} 
}
private static TextRenderer getVanillaRenderer() {
MinecraftClient mc = MinecraftClient.getInstance();
return (mc == null) ? null : mc.textRenderer;
}
private static float getVanillaScale(float size) {
return MathHelper.clamp(size / 9.0F, 0.5F, 4.0F);
}
}


