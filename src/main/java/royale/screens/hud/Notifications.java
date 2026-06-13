package royale.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import royale.client.draggables.AbstractHudElement;
import royale.util.animations.Animation;
import royale.util.animations.Direction;
import royale.util.animations.OutBack;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;
import royale.util.theme.ClientTheme;

public class Notifications
extends AbstractHudElement {
    private static final int FORCED_GUI_SCALE = 2;
    private static Notifications instance;
    private final List<Notification> list = new ArrayList<Notification>();
    private static final float NOTIFICATION_HEIGHT = 16.0f;
    private static final float NOTIFICATION_GAP = 3.0f;

    public static Notifications getInstance() {
        return instance;
    }

    public Notifications() {
        super("Notifications", 0, 0, 110, 16, false);
        instance = this;
    }

    private float getVirtualWidth() {
        return (float)this.mc.getWindow().getFramebufferWidth() / 2.0f;
    }

    private float getVirtualHeight() {
        return (float)this.mc.getWindow().getFramebufferHeight() / 2.0f;
    }

    @Override
    public boolean visible() {
        return !this.list.isEmpty();
    }

    @Override
    public void tick() {
        boolean hasHiNotification;
        this.list.forEach(notif -> {
            if (System.currentTimeMillis() > notif.removeTime || notif.text.contains("Hi I'm a notification") && !this.isChat(this.mc.currentScreen)) {
                notif.anim.setDirection(Direction.BACKWARDS);
            }
        });
        this.list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));
        if (this.isChat(this.mc.currentScreen) && !(hasHiNotification = this.list.stream().anyMatch(n -> n.text.contains("Hi I'm a notification")))) {
            this.addNotification("Hi I'm a notification", 99999999L);
        }
        this.updatePosition();
    }

    private void updatePosition() {
        if (this.mc.getWindow() == null) {
            return;
        }
        float virtualWidth = this.getVirtualWidth();
        float virtualHeight = this.getVirtualHeight();
        float crosshairX = virtualWidth / 2.0f;
        float crosshairY = virtualHeight / 2.0f;
        this.setX((int)(crosshairX - 60.0f));
        this.setY((int)(crosshairY + 100.0f));
    }

    public void addNotification(String text, long duration) {
        Animation anim = new OutBack().setMs(700).setValue(1.0);
        anim.setDirection(Direction.FORWARDS);
        int targetIndex = this.list.size();
        float targetY = (float)targetIndex * 19.0f;
        Notification notification = new Notification(text, anim, System.currentTimeMillis(), System.currentTimeMillis() + duration);
        notification.currentY = targetY;
        notification.targetY = targetY;
        notification.velocityY = 0.0f;
        this.list.add(notification);
        if (this.list.size() > 12) {
            this.list.removeFirst();
        }
        this.list.sort(Comparator.comparingDouble(notif -> -notif.removeTime));
        this.updateTargetPositions();
    }

    private void updateTargetPositions() {
        float offsetY = 0.0f;
        for (int i = 0; i < this.list.size(); ++i) {
            Notification notif = this.list.get(i);
            float anim = notif.anim.getOutput().floatValue();
            notif.targetY = offsetY;
            offsetY += 19.0f * anim;
        }
    }

    private int clampAlpha(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private int clampAlpha(float value) {
        return Math.max(0, Math.min(255, (int)value));
    }

    @Override
    public void drawDraggable(DrawContext context, int alpha) {
        if ((alpha = this.clampAlpha(alpha)) <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        this.updatePosition();
        this.updateTargetPositions();
        float springStiffness = 180.0f;
        float damping = 12.0f;
        float deltaTime = 0.016f;
        for (Notification notification : this.list) {
            float diff = notification.targetY - notification.currentY;
            float springForce = diff * springStiffness;
            float dampingForce = notification.velocityY * damping;
            float acceleration = springForce - dampingForce;
            notification.velocityY += acceleration * deltaTime;
            notification.currentY += notification.velocityY * deltaTime;
            if (!(Math.abs(diff) < 0.01f) || !(Math.abs(notification.velocityY) < 0.01f)) continue;
            notification.currentY = notification.targetY;
            notification.velocityY = 0.0f;
        }
        float offsetX = 5.0f;
        float maxWidth = 0.0f;
        float totalHeight = 0.0f;
        for (Notification notification : this.list) {
            float anim = notification.anim.getOutput().floatValue();
            if (anim <= 0.01f) continue;
            anim = Math.max(0.0f, Math.min(1.0f, anim));
            float textWidth = Fonts.BOLD.getWidth(notification.text, 6.0f);
            float width = textWidth + offsetX * 2.0f + 22.0f;
            maxWidth = Math.max(maxWidth, width);
            float startY = (float)this.getY() + notification.currentY;
            float startX = (float)this.getX() + (120.0f - width) / 2.0f;
            int bgAlpha = this.clampAlpha(225.0f * anim * alphaFactor);
            int icAlpha = this.clampAlpha(155.0f * anim * alphaFactor);
            if (bgAlpha > 0) {
                Render2D.gradientRect(startX, startY, width, 16.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 4.0f);
                Render2D.outline(startX, startY, width, 16.0f, 0.35f, ClientTheme.accentWithAlpha(bgAlpha), 4.0f);
                Render2D.outline(startX + 2.75f, startY + 2.0f, 12.0f, 12.0f, 0.35f, ClientTheme.accentWithAlpha(bgAlpha), 4.0f);
                Fonts.BOLD.draw(notification.text, startX + offsetX + 16.0f, startY + 4.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
                Fonts.GUI_ICONS.draw("C", startX + 5.0f, startY + 4.0f, 8.0f, ClientTheme.accentWithAlpha(icAlpha));
            }
            totalHeight = Math.max(totalHeight, notification.currentY + 16.0f);
        }
        if (maxWidth > 0.0f) {
            this.setWidth((int)Math.ceil(maxWidth));
        }
        this.setHeight((int)Math.ceil(Math.max(16.0f, totalHeight)));
    }

    public static class Notification {
        String text;
        Animation anim;
        long startTime;
        long removeTime;
        float currentY;
        float targetY;
        float velocityY;

        Notification(String text, Animation anim, long startTime, long removeTime) {
            this.text = text;
            this.anim = anim;
            this.startTime = startTime;
            this.removeTime = removeTime;
            this.currentY = 0.0f;
            this.targetY = 0.0f;
            this.velocityY = 0.0f;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > this.removeTime;
        }
    }
}

