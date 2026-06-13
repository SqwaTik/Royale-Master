package royale.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import royale.client.draggables.AbstractHudElement;
import royale.util.ColorUtil;
import royale.util.animations.Direction;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.render.shader.Scissor;
import royale.util.repository.staff.StaffUtils;
import royale.util.theme.ClientTheme;

public class Staff
extends AbstractHudElement {
    private static final Identifier STEVE_SKIN = Identifier.of((String)"royale", (String)"textures/entity/player/wide/steve.png");
    private final Map<String, PlayerInfo> playerMap = new LinkedHashMap<String, PlayerInfo>();
    private final Map<String, Float> playerAnimations = new LinkedHashMap<String, Float>();
    private final Set<String> activePlayerIds = new HashSet<String>();
    private float animatedWidth = 90.0f;
    private float animatedHeight = 23.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FACE_SIZE = 8.0f;
    private static final float CIRCLE_SIZE = 5.0f;

    public Staff() {
        super("Players", 300, 150, 90, 23, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    private Identifier getSkinFromEntry(PlayerListEntry entry) {
        try {
            SkinTextures skinTextures = entry.getSkinTextures();
            if (skinTextures != null && skinTextures.body() != null && skinTextures.body().texturePath() != null) {
                return skinTextures.body().texturePath();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        String stripped = Formatting.strip((String)raw);
        return stripped == null ? raw : stripped;
    }

    private boolean isVanished(PlayerListEntry entry, String name) {
        String display = entry.getDisplayName() != null ? this.normalizeText(entry.getDisplayName().getString()) : name;
        String lowerDisplay = display.toLowerCase(Locale.ROOT);
        if (lowerDisplay.contains("vanish") || lowerDisplay.contains("vanished") || lowerDisplay.contains("\u0420\u0405\u0420\u00b5\u0420\u0406\u0420\u0451\u0420\u0491")) {
            return true;
        }
        if (this.mc.world == null) {
            return false;
        }
        Team team = this.mc.world.getScoreboard().getScoreHolderTeam(name);
        if (team == null) {
            return false;
        }
        String teamText = (this.normalizeText(team.getPrefix().getString()) + " " + this.normalizeText(team.getSuffix().getString()) + " " + team.getName()).toLowerCase(Locale.ROOT);
        return teamText.contains("vanish") || teamText.contains("\u0420\u0405\u0420\u00b5\u0420\u0406\u0420\u0451\u0420\u0491");
    }

    @Override
    public void tick() {
        if (this.mc.player == null || this.mc.world == null || this.mc.player.networkHandler == null) {
            this.playerMap.clear();
            this.activePlayerIds.clear();
            this.playerAnimations.clear();
            this.stopAnimation();
            return;
        }
        this.activePlayerIds.clear();
        for (PlayerListEntry entry : this.mc.player.networkHandler.getPlayerList()) {
            String name;
            if (entry.getProfile() == null || entry.getProfile().name() == null || !StaffUtils.isStaff(name = entry.getProfile().name()) || entry.getGameMode() == GameMode.SPECTATOR || this.isVanished(entry, name)) continue;
            this.activePlayerIds.add(name);
            Identifier skin;
            if (!this.playerMap.containsKey(name)) {
                skin = this.getSkinFromEntry(entry);
                if (skin == null) {
                    skin = STEVE_SKIN;
                }
                this.playerMap.put(name, new PlayerInfo(name, skin));
            } else {
                skin = this.getSkinFromEntry(entry);
                if (skin != null) {
                    this.playerMap.get((Object)name).skin = skin;
                }
            }
            this.playerAnimations.putIfAbsent(name, Float.valueOf(0.0f));
        }
        if (!this.activePlayerIds.isEmpty() || !this.playerAnimations.isEmpty()) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
    }

    private float lerp(float current, float target, float deltaTime) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * 8.0f));
        return current + (target - current) * factor;
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Float> entry : this.playerAnimations.entrySet()) {
            float targetAnim;
            String id = entry.getKey();
            float currentAnim = entry.getValue().floatValue();
            float newAnim = this.lerp(currentAnim, targetAnim = this.activePlayerIds.contains(id) ? 1.0f : 0.0f, deltaTime);
            if (Math.abs(newAnim - targetAnim) < 0.01f) {
                newAnim = targetAnim;
            }
            if (newAnim <= 0.01f && targetAnim == 0.0f) {
                toRemove.add(id);
                continue;
            }
            this.playerAnimations.put(id, Float.valueOf(newAnim));
        }
        for (String id : toRemove) {
            this.playerAnimations.remove(id);
            this.playerMap.remove(id);
        }
        float x = this.getX();
        float y = this.getY();
        int offset = 23;
        float targetWidth = 90.0f;
        for (Map.Entry<String, Float> entry : this.playerAnimations.entrySet()) {
            PlayerInfo info;
            String id = entry.getKey();
            float animation = entry.getValue().floatValue();
            if (animation <= 0.0f || (info = this.playerMap.get(id)) == null) continue;
            offset += (int)(animation * 11.0f);
            float nameWidth = Fonts.BOLD.getWidth(info.name, 6.0f);
            targetWidth = Math.max(nameWidth + 55.0f, targetWidth);
        }
        float targetHeight = offset + 2;
        this.animatedWidth = this.lerp(this.animatedWidth, targetWidth, deltaTime);
        this.animatedHeight = this.lerp(this.animatedHeight, targetHeight, deltaTime);
        if (Math.abs(this.animatedWidth - targetWidth) < 0.3f) {
            this.animatedWidth = targetWidth;
        }
        if (Math.abs(this.animatedHeight - targetHeight) < 0.3f) {
            this.animatedHeight = targetHeight;
        }
        this.setWidth((int)Math.ceil(this.animatedWidth));
        this.setHeight((int)Math.ceil(this.animatedHeight));
        float contentHeight = this.animatedHeight;
        int bgAlpha = (int)(255.0f * alphaFactor);
        if (contentHeight > 0.0f) {
            Render2D.gradientRect(x, y, this.getWidth(), contentHeight, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
            Render2D.outline(x, y, this.getWidth(), contentHeight, 0.35f, ClientTheme.blendWithAccentAndAlpha(new Color(90, 90, 90, 255).getRGB(), 0.35f, bgAlpha), 5.0f);
        }
        Scissor.enable(x, y, this.getWidth(), contentHeight, 2.0f);
        Render2D.gradientRect(x + (float)this.getWidth() - 18.5f, y + 5.0f, 14.0f, 12.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
        Fonts.ICONS.draw("E", x + (float)this.getWidth() - 15.5f, y + 7.5f, 8.0f, ClientTheme.accentWithAlpha(bgAlpha));
        Fonts.BOLD.draw("Players", x + 8.0f, y + 6.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
        int moduleOffset = 23;
        for (Map.Entry<String, Float> entry : this.playerAnimations.entrySet()) {
            PlayerInfo info;
            String id = entry.getKey();
            float animation = entry.getValue().floatValue();
            if (animation <= 0.0f || (info = this.playerMap.get(id)) == null) continue;
            Identifier skinToUse = info.skin != null ? info.skin : STEVE_SKIN;
            int textAlpha = (int)(255.0f * animation * alphaFactor);
            float faceX = x + 8.0f;
            float faceY = y + (float)moduleOffset - 2.0f;
            this.drawFace(skinToUse, faceX, faceY, textAlpha);
            float nameX = faceX + 8.0f + 4.0f;
            Fonts.BOLD.draw(info.name, nameX, y + (float)moduleOffset - 1.5f, 6.0f, new Color(255, 255, 255, textAlpha).getRGB());
            float circleX = x + (float)this.getWidth() - 14.0f;
            float circleY = y + (float)moduleOffset - 0.5f;
            this.drawStatusCircle(circleX, circleY, textAlpha);
            moduleOffset += (int)(animation * 11.0f);
        }
        Scissor.disable();
    }

    private void drawFace(Identifier skin, float faceX, float faceY, int alpha) {
        int color = new Color(255, 255, 255, alpha).getRGB();
        Render2D.texture(skin, faceX, faceY, 8.0f, 8.0f, 0.125f, 0.125f, 0.25f, 0.25f, color, 0.0f, 2.0f);
        float hatScale = 1.15f;
        float hatSize = 8.0f * hatScale;
        float hatOffset = (hatSize - 8.0f) / 2.0f;
        Render2D.texture(skin, faceX - hatOffset, faceY - hatOffset, hatSize, hatSize, 0.625f, 0.125f, 0.75f, 0.25f, color, 0.0f, 2.0f);
        Render2D.blur(faceX, faceY, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
    }

    private void drawStatusCircle(float circleX, float circleY, int alpha) {
        Render2D.gradientRect(circleX - 3.0f, circleY - 2.0f, 11.0f, 9.0f, new int[]{new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB()}, 3.0f);
        int outlineGreen = new Color(40, 170, 80, alpha).getRGB();
        int fillGreen = new Color(80, 255, 125, alpha).getRGB();
        Render2D.outline(circleX - 3.0f, circleY - 2.0f, 11.0f, 9.0f, 0.35f, outlineGreen, 3.0f);
        Render2D.rect(circleX, circleY, 5.0f, 5.0f, fillGreen, 2.5f);
    }

    private static class PlayerInfo {
        String name;
        Identifier skin;

        PlayerInfo(String name, Identifier skin) {
            this.name = name;
            this.skin = skin;
        }
    }
}

