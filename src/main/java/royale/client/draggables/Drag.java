package royale.client.draggables;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import royale.Initialization;
import royale.modules.impl.render.Hud;
import royale.util.ColorUtil;
import royale.util.config.impl.drag.DragConfig;
import royale.util.render.Render2D;

public class Drag {
    private static final float OUTLINE_OFFSET = 3.0f;
    private static final float OUTLINE_THICKNESS = 1.0f;
    private static final int OUTLINE_COLOR = ColorUtil.rgba(255, 255, 255, 255);
    private static final Set<String> EXCLUDED_ELEMENTS = Set.of("Notifications", "Watermark", "Info");

    private static final int SNAP_DISTANCE = 10;
    private static final int AUTO_CORRECT_DISTANCE = 22;
    private static final int GRID_STEP = 18;
    private static final float SNAP_MIN_PULL = 0.17f;
    private static final float SNAP_MAX_PULL = 0.38f;
    private static final float ASSIST_MIN_PULL = 0.06f;
    private static final float ASSIST_MAX_PULL = 0.18f;

    private static final int GUIDE_BASE_COLOR = ColorUtil.rgba(198, 222, 255, 255);
    private static final float GUIDE_SHOW_SPEED = 11.0f;
    private static final float GUIDE_HIDE_SPEED = 2.4f;
    private static final int GUIDE_ALPHA = 98;

    private static final List<HudElement> enabledElementsScratch = new ArrayList<>();

    private static HudElement draggingElement;
    private static int startX;
    private static int startY;

    private static int activeGuideX;
    private static int activeGuideY;
    private static int visibleGuideX;
    private static int visibleGuideY;
    private static float guideAlphaX;
    private static float guideAlphaY;

    public static void onDraw(DrawContext context, int mouseX, int mouseY, float delta, boolean isChatScreen) {
        HudManager hudManager = Drag.getHudManager();
        if (hudManager == null) {
            return;
        }

        Hud hud = Hud.getInstance();
        if (hud == null || !hud.isState()) {
            return;
        }

        hudManager.collectEnabledElements(enabledElementsScratch);

        if (!isChatScreen) {
            if (draggingElement != null) {
                DragConfig.getInstance().save();
                draggingElement = null;
            }

            activeGuideX = -1;
            activeGuideY = -1;
            visibleGuideX = -1;
            visibleGuideY = -1;
            guideAlphaX = 0.0f;
            guideAlphaY = 0.0f;
        }

        if (isChatScreen && draggingElement != null) {
            Drag.moveDraggingElement(mouseX, mouseY, enabledElementsScratch);
        }

        hudManager.render(context, delta, mouseX, mouseY);

        if (isChatScreen) {
            Drag.drawSnapGuidesIfNeeded(delta);
            Drag.drawHoverOutlines(mouseX, mouseY, enabledElementsScratch);
        }
    }

    private static void drawHoverOutlines(int mouseX, int mouseY, List<HudElement> enabledElements) {
        for (HudElement element : enabledElements) {
            if (!element.visible() || EXCLUDED_ELEMENTS.contains(element.getName())) {
                continue;
            }

            if (!Drag.isHovered(element, mouseX, mouseY)) {
                continue;
            }

            float rounding = element.getRoundingRadius();
            float outlineX = element.getX() - OUTLINE_OFFSET;
            float outlineY = element.getY() - OUTLINE_OFFSET;
            float outlineWidth = element.getWidth() + OUTLINE_OFFSET * 2.0f;
            float outlineHeight = element.getHeight() + OUTLINE_OFFSET * 2.0f;
            float outlineRounding = Math.max(0.0f, rounding + OUTLINE_OFFSET);

            Render2D.outline(
                outlineX,
                outlineY,
                outlineWidth,
                outlineHeight,
                OUTLINE_THICKNESS,
                OUTLINE_COLOR,
                outlineRounding
            );
        }
    }

    private static void drawSnapGuidesIfNeeded(float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        boolean hasActiveX = draggingElement != null && activeGuideX >= 0;
        boolean hasActiveY = draggingElement != null && activeGuideY >= 0;

        if (hasActiveX) {
            visibleGuideX = activeGuideX;
        }

        if (hasActiveY) {
            visibleGuideY = activeGuideY;
        }

        guideAlphaX = Drag.approachAlpha(guideAlphaX, hasActiveX ? 1.0f : 0.0f, delta, hasActiveX ? GUIDE_SHOW_SPEED : GUIDE_HIDE_SPEED);
        guideAlphaY = Drag.approachAlpha(guideAlphaY, hasActiveY ? 1.0f : 0.0f, delta, hasActiveY ? GUIDE_SHOW_SPEED : GUIDE_HIDE_SPEED);

        if (!hasActiveX && guideAlphaX <= 0.01f) {
            visibleGuideX = -1;
            guideAlphaX = 0.0f;
        }

        if (!hasActiveY && guideAlphaY <= 0.01f) {
            visibleGuideY = -1;
            guideAlphaY = 0.0f;
        }

        if (visibleGuideX >= 0 && guideAlphaX > 0.01f) {
            int guideX = Drag.clamp(visibleGuideX, 0, Math.max(0, screenWidth - 1));
            int color = Drag.withAlpha(GUIDE_BASE_COLOR, Math.round(GUIDE_ALPHA * guideAlphaX));
            Render2D.rect(guideX, 0.0f, 1.0f, screenHeight, color);
        }

        if (visibleGuideY >= 0 && guideAlphaY > 0.01f) {
            int guideY = Drag.clamp(visibleGuideY, 0, Math.max(0, screenHeight - 1));
            int color = Drag.withAlpha(GUIDE_BASE_COLOR, Math.round(GUIDE_ALPHA * guideAlphaY));
            Render2D.rect(0.0f, guideY, screenWidth, 1.0f, color);
        }
    }

    private static void moveDraggingElement(int mouseX, int mouseY, List<HudElement> enabledElements) {
        if (draggingElement == null) {
            return;
        }

        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int width = Math.max(1, draggingElement.getWidth());
        int height = Math.max(1, draggingElement.getHeight());

        int x = Drag.clamp(mouseX - startX, 0, Math.max(0, screenWidth - width));
        int y = Drag.clamp(mouseY - startY, 0, Math.max(0, screenHeight - height));

        SnapResult snapX = Drag.applySnapX(x, y, width, height, screenWidth, enabledElements);
        x = snapX.value();
        activeGuideX = snapX.guideCoordinate();

        SnapResult snapY = Drag.applySnapY(x, y, width, height, screenHeight, enabledElements);
        y = snapY.value();
        activeGuideY = snapY.guideCoordinate();

        x = Drag.clamp(x, 0, Math.max(0, screenWidth - width));
        y = Drag.clamp(y, 0, Math.max(0, screenHeight - height));

        draggingElement.setX(x);
        draggingElement.setY(y);
    }

    private static SnapResult applySnapX(int x, int y, int width, int height, int screenWidth, List<HudElement> enabledElements) {
        int bestTarget = x;
        int bestGuide = -1;
        int bestDelta = Integer.MAX_VALUE;

        int maxAligned = Math.max(0, screenWidth - width);
        int gridTarget = Math.round((float)x / GRID_STEP) * GRID_STEP;
        int gridDelta = Math.abs(x - (gridTarget = Drag.clamp(gridTarget, 0, maxAligned)));

        if (gridDelta <= AUTO_CORRECT_DISTANCE && gridDelta < bestDelta) {
            bestDelta = gridDelta;
            bestTarget = gridTarget;
            bestGuide = gridTarget;
        }

        int leftDelta = Math.abs(x);
        if (leftDelta <= AUTO_CORRECT_DISTANCE && leftDelta < bestDelta) {
            bestDelta = leftDelta;
            bestTarget = 0;
            bestGuide = 0;
        }

        int rightDelta = Math.abs(x - maxAligned);
        if (rightDelta <= AUTO_CORRECT_DISTANCE && rightDelta < bestDelta) {
            bestDelta = rightDelta;
            bestTarget = maxAligned;
            bestGuide = screenWidth;
        }

        int top = y - 44;
        int bottom = y + height + 44;

        for (HudElement element : enabledElements) {
            if (element == draggingElement || !element.visible()) {
                continue;
            }

            int otherTop = element.getY();
            int otherBottom = element.getY() + element.getHeight();
            if (otherBottom < top || otherTop > bottom) {
                continue;
            }

            int otherLeft = element.getX();
            int otherRight = element.getX() + element.getWidth();
            int otherCenter = Math.round(otherLeft + element.getWidth() / 2.0f);

            SnapCandidate candidate = Drag.selectBetterCandidate(x, bestDelta, otherLeft, otherLeft);
            if (candidate != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(x, bestDelta, otherRight - width, otherRight)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(x, bestDelta, otherLeft - width, otherLeft)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(x, bestDelta, otherRight, otherRight)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            candidate = Drag.selectBetterCandidate(x, bestDelta, Math.round(otherCenter - width / 2.0f), otherCenter);
            if (candidate != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }
        }

        if (bestDelta == Integer.MAX_VALUE) {
            return new SnapResult(x, -1);
        }

        return new SnapResult(Drag.softSnap(x, bestTarget, bestDelta), bestGuide);
    }

    private static SnapResult applySnapY(int x, int y, int width, int height, int screenHeight, List<HudElement> enabledElements) {
        int bestTarget = y;
        int bestGuide = -1;
        int bestDelta = Integer.MAX_VALUE;

        int maxAligned = Math.max(0, screenHeight - height);
        int gridTarget = Math.round((float)y / GRID_STEP) * GRID_STEP;
        int gridDelta = Math.abs(y - (gridTarget = Drag.clamp(gridTarget, 0, maxAligned)));

        if (gridDelta <= AUTO_CORRECT_DISTANCE && gridDelta < bestDelta) {
            bestDelta = gridDelta;
            bestTarget = gridTarget;
            bestGuide = gridTarget;
        }

        int topDelta = Math.abs(y);
        if (topDelta <= AUTO_CORRECT_DISTANCE && topDelta < bestDelta) {
            bestDelta = topDelta;
            bestTarget = 0;
            bestGuide = 0;
        }

        int bottomDelta = Math.abs(y - maxAligned);
        if (bottomDelta <= AUTO_CORRECT_DISTANCE && bottomDelta < bestDelta) {
            bestDelta = bottomDelta;
            bestTarget = maxAligned;
            bestGuide = screenHeight;
        }

        int left = x - 44;
        int right = x + width + 44;

        for (HudElement element : enabledElements) {
            if (element == draggingElement || !element.visible()) {
                continue;
            }

            int otherLeft = element.getX();
            int otherRight = element.getX() + element.getWidth();
            if (otherRight < left || otherLeft > right) {
                continue;
            }

            int otherTop = element.getY();
            int otherBottom = element.getY() + element.getHeight();
            int otherCenter = Math.round(otherTop + element.getHeight() / 2.0f);

            SnapCandidate candidate = Drag.selectBetterCandidate(y, bestDelta, otherTop, otherTop);
            if (candidate != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(y, bestDelta, otherBottom - height, otherBottom)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(y, bestDelta, otherTop - height, otherTop)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            if ((candidate = Drag.selectBetterCandidate(y, bestDelta, otherBottom, otherBottom)) != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }

            candidate = Drag.selectBetterCandidate(y, bestDelta, Math.round(otherCenter - height / 2.0f), otherCenter);
            if (candidate != null) {
                bestDelta = candidate.delta();
                bestTarget = candidate.target();
                bestGuide = candidate.guide();
            }
        }

        if (bestDelta == Integer.MAX_VALUE) {
            return new SnapResult(y, -1);
        }

        return new SnapResult(Drag.softSnap(y, bestTarget, bestDelta), bestGuide);
    }

    private static SnapCandidate selectBetterCandidate(int value, int currentBestDelta, int candidateTarget, int candidateGuide) {
        int delta = Math.abs(candidateTarget - value);
        if (delta <= AUTO_CORRECT_DISTANCE && delta < currentBestDelta) {
            return new SnapCandidate(candidateTarget, candidateGuide, delta);
        }

        return null;
    }

    private static int softSnap(int from, int to, int delta) {
        if (delta <= 1) {
            return to;
        }

        float normalized = (float)Math.min(delta, AUTO_CORRECT_DISTANCE) / AUTO_CORRECT_DISTANCE;
        float closeness = 1.0f - normalized;

        float minPull = delta <= SNAP_DISTANCE ? SNAP_MIN_PULL : ASSIST_MIN_PULL;
        float maxPull = delta <= SNAP_DISTANCE ? SNAP_MAX_PULL : ASSIST_MAX_PULL;
        float pull = minPull + (maxPull - minPull) * closeness;

        pull = MathHelper.clamp(pull, ASSIST_MIN_PULL, SNAP_MAX_PULL);
        return Math.round(from + (to - from) * pull);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float approachAlpha(float current, float target, float delta, float speed) {
        if (Math.abs(target - current) <= 0.001f) {
            return target;
        }

        float t = MathHelper.clamp(delta * speed, 0.0f, 1.0f);
        return MathHelper.lerp(t, current, target);
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | MathHelper.clamp(alpha, 0, 255) << 24;
    }

    public static void onMouseClick(Click click) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof ChatScreen)) {
            return;
        }

        if (click.button() != 0) {
            return;
        }

        HudManager hudManager = Drag.getHudManager();
        if (hudManager == null) {
            return;
        }

        double mouseX = click.x();
        double mouseY = click.y();
        HudElement element = hudManager.getElementAt(mouseX, mouseY);
        if (!(element instanceof AbstractHudElement abstractElement) || !abstractElement.isDraggable()) {
            return;
        }

        draggingElement = element;
        startX = (int)mouseX - element.getX();
        startY = (int)mouseY - element.getY();

        activeGuideX = -1;
        activeGuideY = -1;
        visibleGuideX = -1;
        visibleGuideY = -1;
        guideAlphaX = 0.0f;
        guideAlphaY = 0.0f;
    }

    public static void onMouseRelease(Click click) {
        if (click.button() == 0 && draggingElement != null) {
            DragConfig.getInstance().save();
            draggingElement = null;
            activeGuideX = -1;
            activeGuideY = -1;
        }
    }

    public static void resetDragging() {
        if (draggingElement != null) {
            DragConfig.getInstance().save();
            draggingElement = null;
        }

        activeGuideX = -1;
        activeGuideY = -1;
        visibleGuideX = -1;
        visibleGuideY = -1;
        guideAlphaX = 0.0f;
        guideAlphaY = 0.0f;
    }

    public static boolean isDragging() {
        return draggingElement != null;
    }

    private static boolean isHovered(HudElement element, double mouseX, double mouseY) {
        int x = element.getX();
        int y = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static HudManager getHudManager() {
        if (Initialization.getInstance() == null) {
            return null;
        }

        if (Initialization.getInstance().getManager() == null) {
            return null;
        }

        return Initialization.getInstance().getManager().getHudManager();
    }

    public static void tick() {
        HudManager hudManager = Drag.getHudManager();
        if (hudManager != null) {
            hudManager.tick();
        }
    }

    static {
        activeGuideX = -1;
        activeGuideY = -1;
        visibleGuideX = -1;
        visibleGuideY = -1;
        guideAlphaX = 0.0f;
        guideAlphaY = 0.0f;
    }

    private record SnapResult(int value, int guideCoordinate) {
    }

    private record SnapCandidate(int target, int guide, int delta) {
    }
}
