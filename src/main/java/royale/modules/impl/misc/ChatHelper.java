package royale.modules.impl.misc;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.TextSetting;
import royale.util.Instance;
import royale.util.chat.ChatHistoryManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ChatHelper extends ModuleStructure {
    private static final String FORMAT_TIME = "HH:mm:ss";
    private static final String FORMAT_TIME_SHORT = "HH:mm";
    private static final String FORMAT_DATE_TIME = "dd.MM.yyyy HH:mm:ss";
    private static final String FORMAT_DATE = "dd.MM.yyyy";
    private static final String FORMAT_CUSTOM = "Свой";
    private static final String FALLBACK_PATTERN = "HH:mm:ss";

    public final BooleanSetting showTime = (new BooleanSetting(
            "Показывать время",
            "Добавляет перед сообщением метку текущего времени"
    )).setValue(true);

    public final SelectSetting timeFormat = (new SelectSetting(
            "Формат времени",
            "Выбор шаблона времени и даты"
    )).value(
            FORMAT_TIME,
            FORMAT_TIME_SHORT,
            FORMAT_DATE_TIME,
            FORMAT_DATE,
            FORMAT_CUSTOM
    ).selected(FORMAT_TIME).visible(this.showTime::isValue);

    public final TextSetting customFormat = (new TextSetting(
            "Свой шаблон",
            "Паттерн DateTimeFormatter, например HH:mm:ss"
    )).setText(FALLBACK_PATTERN).setMin(2).setMax(48)
            .visible(() -> this.showTime.isValue() && this.timeFormat.isSelected(FORMAT_CUSTOM));

    public final ColorSetting timeColor = (new ColorSetting(
            "Цвет времени",
            "Цвет префикса времени в чате"
    )).value(0xFFAAAAAA).visible(this.showTime::isValue);

    public final BooleanSetting moreChatHistory = (new BooleanSetting(
            "MoreChatHistory",
            "Включает отображение расширенной истории чата"
    )).setValue(true);

    public ChatHelper() {
        super("ChatHelper", "Улучшает внешний вид чата и формат входящих сообщений", ModuleCategory.MISC);
        settings(new Setting[]{
                this.showTime,
                this.timeFormat,
                this.customFormat,
                this.timeColor,
                this.moreChatHistory
        });
    }

    public static ChatHelper getInstance() {
        return Instance.get(ChatHelper.class);
    }

    @Override
    public void activate() {
        ChatHistoryManager.getInstance().ensureLoaded();
        super.activate();
    }

    public Text decorateIncomingMessage(Text message) {
        if (message == null) {
            return null;
        }

        Text prepared = message.copy();
        if (!this.showTime.isValue()) {
            return prepared;
        }

        MutableText result = Text.empty();
        result.append(Text.literal("[" + formatNow() + "] ").setStyle(Style.EMPTY.withColor(getTimePrefixColor())));
        result.append(prepared.copy());
        return result;
    }

    public String formatNow() {
        DateTimeFormatter formatter = resolveFormatter();
        return LocalDateTime.now().format(formatter);
    }

    public int getTimePrefixColor() {
        return this.timeColor.getColorNoAlpha();
    }

    private DateTimeFormatter resolveFormatter() {
        String pattern = resolvePattern();
        try {
            return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        } catch (Exception ignored) {
            return DateTimeFormatter.ofPattern(FALLBACK_PATTERN, Locale.ROOT);
        }
    }

    private String resolvePattern() {
        if (this.timeFormat.isSelected(FORMAT_CUSTOM)) {
            String value = this.customFormat.getText();
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
            return FALLBACK_PATTERN;
        }
        return this.timeFormat.getSelected();
    }
}