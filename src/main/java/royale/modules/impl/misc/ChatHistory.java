package royale.modules.impl.misc;

import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.chat.ChatHistoryManager;

public class ChatHistory extends ModuleStructure {

    public final BooleanSetting writeToFile = (new BooleanSetting(
            "Логи в файл",
            "Сохранять историю чата в Royale/chat/chat.log"
    )).setValue(true);

    public final BooleanSetting enableSearch = (new BooleanSetting(
            "Поиск в истории",
            "Показывать поиск по истории сообщений в окне чата"
    )).setValue(true);

    public final BooleanSetting optimizedSearch = (new BooleanSetting(
            "Поиск оптимизированный",
            "Быстрый поиск по кешу без лишних лагов"
    )).setValue(true).visible(this.enableSearch::isValue);

    public final SliderSettings searchResultLimit = (new SliderSettings(
            "Лимит результатов",
            "Сколько строк отображать в поиске"
    )).range(3, 20).setValue(8.0F).visible(this.enableSearch::isValue);

    public ChatHistory() {
        super("MoreChatHistory", "Расширенная история чата с быстрым поиском", ModuleCategory.MISC);
        settings(new Setting[]{this.writeToFile, this.enableSearch, this.optimizedSearch, this.searchResultLimit});
    }

    public static ChatHistory getInstance() {
        return Instance.get(ChatHistory.class);
    }

    @Override
    public void activate() {
        ChatHistoryManager.getInstance().ensureLoaded();
        super.activate();
    }
}
