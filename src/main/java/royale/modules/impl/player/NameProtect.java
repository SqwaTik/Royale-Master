package royale.modules.impl.player;

import royale.events.api.EventHandler;
import royale.events.api.events.render.TextFactoryEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.TextSetting;
import royale.util.repository.friend.FriendUtils;

public class NameProtect extends ModuleStructure {
    private final TextSetting nameSetting = (new TextSetting("Имя", "Псевдоним для замены вашего ника"))
            .setText("Player")
            .setMax(32);

    private final BooleanSetting friendsSetting = (new BooleanSetting("Друзья", "Заменять ники друзей тоже"))
            .setValue(true);

    public NameProtect() {
        super("StreamerMode", "Скрывает реальные имена в интерфейсе", ModuleCategory.PLAYER);
        settings(new Setting[]{this.nameSetting, this.friendsSetting});
    }

    @EventHandler
    public void onTextFactory(TextFactoryEvent event) {
        String replacement = getReplacementName();
        event.replaceText(mc.getSession().getUsername(), replacement);
        if (this.friendsSetting.isValue()) {
            FriendUtils.getFriends().forEach(friend -> event.replaceText(friend.getName(), replacement));
        }
    }

    private String getReplacementName() {
        String value = this.nameSetting.getText();
        return (value == null || value.isBlank()) ? "Player" : value;
    }
}
