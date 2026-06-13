package royale.util.render;
import royale.util.theme.ClientTheme;
public final class BrandMarkRenderer
{
public static void drawR(float centerX, float centerY, float size, int alpha) {
int clampedAlpha = Math.max(0, Math.min(255, alpha));
if (clampedAlpha <= 0 || size <= 1.0F) {
return;
}
int accentColor = withAlpha(ClientTheme.accent(), Math.min(255, (int)(clampedAlpha * 0.98F)));
int highlightColor = mixWithWhite(accentColor, 0.22F);
int shadowColor = withAlpha(0, Math.min(255, (int)(clampedAlpha * 0.34F)));
float left = centerX - size * 0.34F;
float top = centerY - size * 0.42F;
float stemWidth = Math.max(1.0F, size * 0.17F);
float radius = Math.max(0.8F, size * 0.08F);
drawCore(left + 0.7F, top + 0.9F, size, stemWidth, radius, shadowColor);
drawCore(left, top, size, stemWidth, radius, accentColor);
drawHighlight(left, top, size, radius, highlightColor);
}
private static void drawCore(float left, float top, float size, float stemWidth, float radius, int baseColor) {
float mainHeight = size * 0.78F;
float topBarWidth = size * 0.62F;
float topBarHeight = size * 0.16F;
float midBarWidth = size * 0.48F;
float midBarHeight = size * 0.14F;
float midBarY = top + size * 0.33F;
float rightStemX = left + topBarWidth - stemWidth * 0.95F;
float rightStemHeight = size * 0.2F;
Render2D.rect(left, top, stemWidth, mainHeight, baseColor, radius);
Render2D.rect(left, top, topBarWidth, topBarHeight, baseColor, radius);
Render2D.rect(left, midBarY, midBarWidth, midBarHeight, baseColor, radius);
Render2D.rect(rightStemX, top + topBarHeight * 0.88F, stemWidth * 0.95F, rightStemHeight, baseColor, radius);
for (int i = 0; i < 5; i++) {
float segmentX = left + size * (0.33F + i * 0.08F);
float segmentY = top + size * (0.48F + i * 0.08F);
Render2D.rect(segmentX, segmentY, stemWidth * 0.92F, size * 0.12F, baseColor, radius * 0.8F);
} 
}
private static void drawHighlight(float left, float top, float size, float radius, int highlightColor) {
float lineWidth = size * 0.24F;
float lineHeight = Math.max(1.0F, size * 0.06F);
float lineX = left + size * 0.32F;
float lineY = top + size * 0.05F;
Render2D.rect(lineX, lineY, lineWidth, lineHeight, highlightColor, radius * 0.7F);
}
private static int withAlpha(int color, int alpha) {
return color & 0xFFFFFF | Math.max(0, Math.min(255, alpha)) << 24;
}
private static int mixWithWhite(int color, float t) {
float clamped = Math.max(0.0F, Math.min(1.0F, t));
int r = color >> 16 & 0xFF;
int g = color >> 8 & 0xFF;
int b = color & 0xFF;
r = (int)(r + (255 - r) * clamped);
g = (int)(g + (255 - g) * clamped);
b = (int)(b + (255 - b) * clamped);
return r << 16 | g << 8 | b;
}
}


