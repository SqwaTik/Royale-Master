package royale.modules.impl.misc;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.util.Instance;

public class Client extends ModuleStructure {
    private static final int DEFAULT_CLIENT_COLOR = -2461482;

    public final ColorSetting clientColor = (new ColorSetting(
            "Цвет клиента",
            "Основной цвет интерфейса и акцентов"
    )).value(DEFAULT_CLIENT_COLOR);

    public Client() {
        super("Client", "Меняет основной цвет клиента", ModuleCategory.MISC);
        settings(new Setting[]{this.clientColor});
    }

    public static Client getInstance() {
        return Instance.get(Client.class);
    }

    public int getClientColor() {
        return this.clientColor.getColorNoAlpha();
    }

    public static int getResolvedClientColor() {
        try {
            Client module = getInstance();
            if (module != null && module.isState()) {
                return module.getClientColor();
            }
        } catch (Throwable ignored) {
        }
        return DEFAULT_CLIENT_COLOR;
    }
}
