package royale.screens.clickgui.impl.configs.render;

import java.awt.Color;
import royale.util.render.Render2D;
import royale.util.render.font.Fonts;

public class ConfigNotificationRenderer {
    private String notification = null;
    private NotificationType notificationType = NotificationType.SUCCESS;
    private float notificationAlpha = 0.0f;
    private long notificationTime = 0L;
    private long lastUpdateTime = System.currentTimeMillis();

    public void render(float x, float y, float alpha) {
        this.updateAnimation();
        if (this.notification == null || this.notificationAlpha < 0.01f) {
            return;
        }
        float notifY = y + 204.0f - 25.0f;
        float notifAlpha = this.notificationAlpha * alpha;
        Color bgColor = this.notificationType.getBgColor();
        Color textColor = this.notificationType.getTextColor();
        float textWidth = Fonts.BOLD.getWidth(this.notification, 5.0f);
        float notifW = textWidth + 20.0f;
        float notifX = x + (298.0f - notifW) / 2.0f;
        Render2D.rect(notifX, notifY, notifW, 18.0f, new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(60.0f * notifAlpha)).getRGB(), 4.0f);
        Fonts.BOLD.draw(this.notification, notifX + 10.0f, notifY + 6.0f, 5.0f, new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(255.0f * notifAlpha)).getRGB());
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        if (this.notification != null) {
            if (System.currentTimeMillis() - this.notificationTime < 2000L) {
                this.notificationAlpha += (1.0f - this.notificationAlpha) * 8.0f * deltaTime;
            } else {
                this.notificationAlpha += (0.0f - this.notificationAlpha) * 4.0f * deltaTime;
                if (this.notificationAlpha < 0.01f) {
                    this.notification = null;
                }
            }
        }
    }

    public void show(String message, NotificationType type) {
        this.notification = message;
        this.notificationType = type;
        this.notificationTime = System.currentTimeMillis();
        this.notificationAlpha = 0.0f;
    }

    public static enum NotificationType {
        SUCCESS(new Color(60, 120, 60), new Color(180, 255, 180)),
        ERROR(new Color(120, 60, 60), new Color(255, 180, 180)),
        INFO(new Color(60, 100, 140), new Color(180, 220, 255));

        private final Color bgColor;
        private final Color textColor;

        private NotificationType(Color bgColor, Color textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
        }

        public Color getBgColor() {
            return this.bgColor;
        }

        public Color getTextColor() {
            return this.textColor;
        }
    }
}

