package royale.modules.module.setting.implement;

import royale.modules.module.setting.Setting;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class TextSetting extends Setting {
    private String text = "";
    private int min;
    private int max;
    private String placeholder = "";
    private Predicate<String> validator = value -> true;
    private UnaryOperator<String> normalizer = value -> value == null ? "" : value;

    public TextSetting(String name, String description) {
        super(name, description);
    }

    public TextSetting setText(String text) {
        String normalized = normalize(text);
        if (!isWithinLength(normalized) || !this.validator.test(normalized)) {
            return this;
        }

        this.text = normalized;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public TextSetting setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMin() {
        return this.min;
    }

    public TextSetting setMax(int max) {
        this.max = max;
        return this;
    }

    public int getMax() {
        return this.max;
    }

    public TextSetting setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        return this;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public TextSetting setValidator(Predicate<String> validator) {
        this.validator = Objects.requireNonNullElseGet(validator, () -> value -> true);
        return this;
    }

    public boolean isValidText(String text) {
        String normalized = normalize(text);
        return isWithinLength(normalized) && this.validator.test(normalized);
    }

    public TextSetting setNormalizer(UnaryOperator<String> normalizer) {
        this.normalizer = Objects.requireNonNullElseGet(normalizer, () -> value -> value == null ? "" : value);
        return this;
    }

    public String normalize(String text) {
        return this.normalizer.apply(text);
    }

    public TextSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    private boolean isWithinLength(String text) {
        int minLength = Math.max(this.min, 0);
        int maxLength = this.max > 0 ? this.max : Integer.MAX_VALUE;
        int length = text == null ? 0 : text.length();
        return length >= minLength && length <= maxLength;
    }
}
