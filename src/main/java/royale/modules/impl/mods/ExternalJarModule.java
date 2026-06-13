package royale.modules.impl.mods;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.util.mods.ModsRuntimeManager.ScannedMod;

import java.util.Locale;

public class ExternalJarModule extends ModuleStructure {
    private final String logicalFileName;

    public ExternalJarModule(ScannedMod mod) {
        super(buildModuleName(mod), buildDescription(mod), ModuleCategory.MODS);
        this.logicalFileName = mod.getFileName();

        this.state = mod.isEnabled();
        this.getAnimation().setDirection(this.state
                ? royale.util.animations.Direction.FORWARDS
                : royale.util.animations.Direction.BACKWARDS);
    }

    public String getLogicalFileName() {
        return this.logicalFileName;
    }

    @Override
    public void setState(boolean state) {
        // External mods are displayed as read-only entries in the Mods category.
    }

    private static String buildModuleName(ScannedMod mod) {
        String name = sanitize(mod.getName());
        if (name.isEmpty()) {
            name = stripJarSuffix(sanitize(mod.getFileName()));
        }
        return name;
    }

    private static String buildDescription(ScannedMod mod) {
        String description = sanitize(mod.getDescription());
        if (description.isEmpty()) {
            description = "Нет описания";
        }
        return description;
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static String stripJarSuffix(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jar.disabled")) {
            return fileName.substring(0, fileName.length() - ".jar.disabled".length());
        }
        if (lower.endsWith(".jar")) {
            return fileName.substring(0, fileName.length() - ".jar".length());
        }
        return fileName;
    }
}
