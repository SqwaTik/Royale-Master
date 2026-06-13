package royale.mixin;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.Initialization;
import royale.client.draggables.HudElement;
import royale.client.draggables.HudManager;
import royale.modules.impl.render.Hud;
@Mixin({PlayerListHud.class})
public abstract class PlayerListHudMixin
{
@Shadow
@Final
private MinecraftClient client;
@Shadow
private Text header;
@Shadow
private Text footer;
@Inject(method = {"render"}, at = {@At("HEAD")})
private void royale$renderHead(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
int offsetY = royale$computeTabOffset(scaledWindowWidth, scoreboard, objective);
if (offsetY <= 0) {
this.royale$tabOffsetApplied = false;
return;
} 
context.getMatrices().pushMatrix();
context.getMatrices().translate(0.0F, offsetY);
this.royale$tabOffsetApplied = true; } @Unique
private boolean royale$tabOffsetApplied = false; @Shadow
protected abstract List<PlayerListEntry> collectPlayerEntries(); @Shadow
public abstract Text getPlayerName(PlayerListEntry paramclass_640); @Inject(method = {"render"}, at = {@At("RETURN")})
private void royale$renderReturn(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
if (!this.royale$tabOffsetApplied) {
return;
}
context.getMatrices().popMatrix();
this.royale$tabOffsetApplied = false;
}
@Unique
private int royale$computeTabOffset(int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
int[] watermark = royale$getWatermarkBounds();
if (watermark == null) {
return 0;
}
int[] tabBounds = royale$estimateTabBounds(scaledWindowWidth, scoreboard, objective);
if (tabBounds == null) {
return 0;
}
if (!royale$intersects(watermark, tabBounds)) {
return 0;
}
int tabTop = tabBounds[1];
int watermarkBottom = watermark[3];
int margin = 4;
return Math.max(0, watermarkBottom + margin - tabTop);
}
@Unique
private int[] royale$getWatermarkBounds() {
Hud hud = Hud.getInstance();
if (hud == null || !hud.isState() || !hud.interfaceSettings.isSelected("Watermark")) {
return null;
}
if (Initialization.getInstance() == null || Initialization.getInstance().getManager() == null) {
return null;
}
HudManager hudManager = Initialization.getInstance().getManager().getHudManager();
if (hudManager == null) {
return null;
}
for (HudElement element : hudManager.getEnabledElements()) {
if (!"Watermark".equalsIgnoreCase(element.getName()) || !element.visible()) {
continue;
}
int left = element.getX();
int top = element.getY();
int right = left + Math.max(1, element.getWidth());
int bottom = top + Math.max(1, element.getHeight());
return new int[] { left, top, right, bottom };
} 
return null;
}
@Unique
private int[] royale$estimateTabBounds(int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
int objectiveWidth;
List<PlayerListEntry> playerEntries = collectPlayerEntries();
if (playerEntries.isEmpty()) {
return null;
}
TextRenderer textRenderer = this.client.textRenderer;
if (textRenderer == null) {
return null;
}
int nameWidth = textRenderer.getWidth("");
int scoreWidth = 0;
for (PlayerListEntry entry : playerEntries) {
nameWidth = Math.max(nameWidth, textRenderer.getWidth((StringVisitable)getPlayerName(entry)));
if (objective != null && objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS && scoreboard != null) {
ReadableScoreboardScore score = scoreboard.getScore(ScoreHolder.fromProfile(entry.getProfile()), objective);
if (score != null) {
MutableText MutableText = ReadableScoreboardScore.getFormattedScore(score, objective
.getNumberFormatOr((NumberFormat)StyledNumberFormat.YELLOW));
int formattedScoreWidth = textRenderer.getWidth((StringVisitable)MutableText);
scoreWidth = Math.max(scoreWidth, (formattedScoreWidth > 0) ? (textRenderer.getWidth("") + formattedScoreWidth) : 0);
} 
} 
} 
int playerCount = playerEntries.size();
int rows = playerCount;
int columns = 1;
while (rows > 20) {
columns++;
rows = (playerCount + columns - 1) / columns;
} 
boolean showSkins = this.client.isInSingleplayer();
if (!showSkins && this.client.getNetworkHandler() != null && this.client.getNetworkHandler().getConnection() != null) {
showSkins = this.client.getNetworkHandler().getConnection().isEncrypted();
}
if (objective == null) {
objectiveWidth = 0;
} else if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
objectiveWidth = 90;
} else {
objectiveWidth = scoreWidth;
} 
int columnWidth = Math.min(columns * ((
showSkins ? 9 : 0) + nameWidth + objectiveWidth + 13), scaledWindowWidth - 50) / columns;
int listWidth = columnWidth * columns + (columns - 1) * 5;
int maxPanelWidth = listWidth;
int headerLines = 0;
int footerLines = 0;
if (this.header != null) {
List<OrderedText> lines = textRenderer.wrapLines((StringVisitable)(Object)this.header, scaledWindowWidth - 50);
headerLines = lines.size();
for (OrderedText line : lines) {
maxPanelWidth = Math.max(maxPanelWidth, textRenderer.getWidth(line));
}
} 
if (this.footer != null) {
List<OrderedText> lines = textRenderer.wrapLines((StringVisitable)(Object)this.footer, scaledWindowWidth - 50);
footerLines = lines.size();
for (OrderedText line : lines) {
maxPanelWidth = Math.max(maxPanelWidth, textRenderer.getWidth(line));
}
} 
int centerX = scaledWindowWidth / 2;
int left = centerX - maxPanelWidth / 2 - 1;
int right = centerX + maxPanelWidth / 2 + 1;
int top = 10;
int bottom = top;
if (headerLines > 0) {
bottom += headerLines * 9 + 1;
}
bottom += rows * 9;
if (footerLines > 0) {
bottom += 1 + footerLines * 9;
}
return new int[] { left, top, right, bottom };
}
@Unique
private boolean royale$intersects(int[] a, int[] b) {
return (a[0] < b[2] && a[2] > b[0] && a[1] < b[3] && a[3] > b[1]);
}
}


