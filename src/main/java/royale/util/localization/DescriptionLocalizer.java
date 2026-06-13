package royale.util.localization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class DescriptionLocalizer {
    private static final Map<String, String> MODULE_FALLBACKS = Map.ofEntries(
            Map.entry("67", "Проигрывает мем 67 и делает анимацию руками"),
            Map.entry("67 meme", "Проигрывает мем 67 и делает анимацию руками"),
            Map.entry("arrows", "Показывает стрелки к игрокам за экраном"),
            Map.entry("autoeat", "Автоматически ест при низком здоровье или голоде"),
            Map.entry("autorespawn", "Автоматически возрождает после смерти"),
            Map.entry("autosprint", "Автоматически включает бег"),
            Map.entry("blockoverlay", "Подсвечивает блок под прицелом"),
            Map.entry("brand", "Меняет brand клиента при подключении к серверу"),
            Map.entry("chathistory", "Сохраняет и показывает историю чата"),
            Map.entry("chathelper", "Добавляет полезные действия и подсказки для чата"),
            Map.entry("chinahat", "Рисует китайскую шляпу над игроком"),
            Map.entry("chunkanimator", "Плавно анимирует появление чанков"),
            Map.entry("client", "Настраивает цвета и внешний вид клиента"),
            Map.entry("clientsounds", "Проигрывает звуки при включении и выключении функций"),
            Map.entry("croptimer", "Показывает рост растений и время до созревания"),
            Map.entry("custombar", "Меняет хотбар и полосы здоровья с едой"),
            Map.entry("debug", "Показывает полезную отладочную информацию"),
            Map.entry("esp", "Подсвечивает игроков и объекты через стены"),
            Map.entry("boxes", "Подсвечивает игроков и предметы"),
            Map.entry("freelook", "Позволяет осматриваться отдельно от движения"),
            Map.entry("fullbright", "Осветляет темные зоны"),
            Map.entry("gifmanager", "Настраивает анимации и GIF в меню"),
            Map.entry("glasshands", "Делает руки прозрачными или стеклянными"),
            Map.entry("hiteffect", "Добавляет визуальный эффект при ударе"),
            Map.entry("hitsound", "Проигрывает звук при ударе по цели"),
            Map.entry("hud", "Рисует HUD клиента"),
            Map.entry("itemphysic", "Добавляет физику предметам на земле"),
            Map.entry("itemscroller", "Ускоряет перемещение предметов в инвентаре"),
            Map.entry("jumpcircle", "Рисует круг при прыжке"),
            Map.entry("nameprotect", "Скрывает ник игрока в интерфейсе"),
            Map.entry("norender", "Отключает лишние визуальные эффекты"),
            Map.entry("particles", "Добавляет декоративные частицы"),
            Map.entry("rpc", "Показывает статус клиента в Discord"),
            Map.entry("serverrp", "Принимает и кэширует серверные ресурспаки без фризов"),
            Map.entry("srpoff", "Пропускает серверный ресурспак без скачивания"),
            Map.entry("shiftanim", "Меняет анимацию приседания"),
            Map.entry("swinganimation", "Меняет анимацию удара рукой"),
            Map.entry("tapemouse", "Помогает удерживать клики мыши"),
            Map.entry("targetesp", "Выделяет текущую цель"),
            Map.entry("trajectories", "Показывает дуги бросков и время падения"),
            Map.entry("viewmodel", "Настраивает положение предметов в руках"),
            Map.entry("worldparticles", "Добавляет декоративные частицы в мире"),
            Map.entry("zoom", "Приближает камеру"),
            Map.entry("cheststealer", "Автоматизирует работу с контейнерами")
    );

    private static final Map<String, String> SETTING_FALLBACKS = Map.ofEntries(
            Map.entry("режим", "Меняет режим работы"),
            Map.entry("тип", "Меняет тип отображения"),
            Map.entry("цвет", "Меняет цвет элемента"),
            Map.entry("прозрачность", "Меняет прозрачность"),
            Map.entry("клиент", "Меняет client brand"),
            Map.entry("аватар", "Меняет аниме-аватар"),
            Map.entry("фон", "Меняет фон ClickGUI"),
            Map.entry("скорость аватара", "Меняет скорость аватара"),
            Map.entry("скорость фона", "Меняет скорость фона"),
            Map.entry("показывать хотбар", "Включает кастомный хотбар"),
            Map.entry("показывать hp bar", "Включает блок здоровья"),
            Map.entry("показывать food bar", "Включает блок сытости"),
            Map.entry("кастомный бренд", "Задает свое значение brand"),
            Map.entry("тип звука", "Выбирает звук при ударе"),
            Map.entry("громкость", "Меняет громкость звука"),
            Map.entry("длина", "Меняет длину просчета"),
            Map.entry("толщина", "Меняет толщину линии"),
            Map.entry("радиус игроков", "Меняет дистанцию поиска игроков")
    );

    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern MOJIBAKE_RUN_PATTERN = Pattern.compile("(?:[ГђГ‘ГѓГ‚Р ][^\\s]){3,}");

    private DescriptionLocalizer() {
    }

    public static String sanitizeDisplay(String value) {
        return tryFixMojibake(normalizeSpaces(value));
    }

    public static String localizeModule(String moduleName, String description) {
        String fallback = buildModuleFallback(moduleName);
        String value = normalizeSpaces(description);
        String defaultDescription = "Управляет " + safeName(moduleName);
        if (normalizeKey(value).equals(normalizeKey(defaultDescription))) {
            return fallback;
        }
        return localize(description, fallback);
    }

    public static String localizeSetting(String settingName, String description) {
        return localize(description, buildSettingFallback(settingName));
    }

    private static String localize(String description, String fallback) {
        String value = normalizeSpaces(description);
        if (value.isEmpty()) {
            return fallback;
        }

        String repaired = tryFixMojibake(value);
        if (repaired.isEmpty() || looksLikeMojibake(repaired) || looksMostlyEnglish(repaired)) {
            return fallback;
        }
        return repaired;
    }

    private static String tryFixMojibake(String value) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalizeSpaces(value));

        String cp1251 = decode(value, WINDOWS_1251);
        String latin1 = decode(value, StandardCharsets.ISO_8859_1);
        candidates.add(normalizeSpaces(cp1251));
        candidates.add(normalizeSpaces(latin1));
        candidates.add(normalizeSpaces(decode(cp1251, WINDOWS_1251)));
        candidates.add(normalizeSpaces(decode(latin1, StandardCharsets.ISO_8859_1)));
        candidates.add(normalizeSpaces(decode(cp1251, StandardCharsets.ISO_8859_1)));
        candidates.add(normalizeSpaces(decode(latin1, WINDOWS_1251)));

        List<String> ordered = new ArrayList<>(candidates);
        String best = "";
        int bestScore = Integer.MIN_VALUE;
        for (String candidate : ordered) {
            int score = scoreText(candidate);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return normalizeSpaces(best);
    }

    private static String decode(String value, Charset sourceCharset) {
        try {
            return new String(value.getBytes(sourceCharset), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return value;
        }
    }

    private static boolean looksMostlyEnglish(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        int cyrillic = 0;
        int latin = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '\u0400' && ch <= '\u04FF') {
                cyrillic++;
            } else if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                latin++;
            }
        }
        return latin >= 4 && cyrillic == 0;
    }

    private static boolean looksLikeMojibake(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        if (MOJIBAKE_RUN_PATTERN.matcher(value).find()) {
            return true;
        }

        int letters = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetter(ch)) {
                letters++;
            }
        }
        int suspicious = countOccurrences(value, "Гђ")
                + countOccurrences(value, "Г‘")
                + countOccurrences(value, "Гѓ")
                + countOccurrences(value, "Г‚")
                + countOccurrences(value, "Р ");

        return letters > 0 && suspicious * 100 / letters > 35;
    }

    private static int scoreText(String value) {
        if (value == null || value.isEmpty()) {
            return Integer.MIN_VALUE / 4;
        }

        int score = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= '\u0400' && ch <= '\u04FF') {
                score += 4;
            } else if (Character.isLetterOrDigit(ch)) {
                score += 2;
            } else if (Character.isWhitespace(ch) || ",.!?:;()-_[]{}'\"/\\".indexOf(ch) >= 0) {
                score += 1;
            } else if (ch == '\uFFFD') {
                score -= 8;
            } else if (Character.isISOControl(ch)) {
                score -= 6;
            }
        }

        if (looksLikeMojibake(value)) {
            score -= 120;
        }
        return score;
    }

    private static String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return SPACE_PATTERN.matcher(value.replace('\u00A0', ' ')).replaceAll(" ").trim();
    }

    private static String safeName(String value) {
        String name = sanitizeDisplay(value);
        return name.isEmpty() ? "модуля" : name;
    }

    private static String buildModuleFallback(String moduleName) {
        String normalized = normalizeKey(moduleName);
        String fallback = MODULE_FALLBACKS.get(normalized);
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "Управляет " + safeName(moduleName);
    }

    private static String buildSettingFallback(String settingName) {
        String normalized = normalizeKey(settingName);
        String fallback = SETTING_FALLBACKS.get(normalized);
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "Настраивает " + safeName(settingName);
    }

    private static String normalizeKey(String value) {
        return sanitizeDisplay(value)
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int countOccurrences(String value, String token) {
        if (value == null || value.isEmpty() || token == null || token.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
