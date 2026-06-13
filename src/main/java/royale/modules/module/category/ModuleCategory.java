package royale.modules.module.category;

import royale.util.localization.DescriptionLocalizer;

public enum ModuleCategory {
    COMBAT("Бой"),
    MOVEMENT("Движение"),
    RENDER("Рендер"),
    PLAYER("Игрок"),
    MISC("Разное"),
    MODS("Моды"),
    AUTOBUY("Конфиги");

    final String readableName;

    ModuleCategory(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return DescriptionLocalizer.sanitizeDisplay(this.readableName);
    }
}
