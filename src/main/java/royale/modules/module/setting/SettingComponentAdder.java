package royale.modules.module.setting;
import java.util.List;
import royale.modules.module.setting.implement.BindSetting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ButtonSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.modules.module.setting.implement.TextSetting;
import royale.screens.clickgui.impl.settingsrender.BindComponent;
import royale.screens.clickgui.impl.settingsrender.ButtonComponent;
import royale.screens.clickgui.impl.settingsrender.CheckboxComponent;
import royale.screens.clickgui.impl.settingsrender.ColorComponent;
import royale.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import royale.screens.clickgui.impl.settingsrender.SelectComponent;
import royale.screens.clickgui.impl.settingsrender.SliderComponent;
import royale.screens.clickgui.impl.settingsrender.TextComponent;
import royale.util.interfaces.AbstractSettingComponent;
public class SettingComponentAdder
{
public void addSettingComponent(List<Setting> settings, List<AbstractSettingComponent> components) {
settings.forEach(setting -> {
if (setting instanceof BooleanSetting) {
BooleanSetting booleanSetting = (BooleanSetting)setting;
components.add(new CheckboxComponent(booleanSetting));
} 
if (setting instanceof BindSetting) {
BindSetting bindSetting = (BindSetting)setting;
components.add(new BindComponent(bindSetting));
} 
if (setting instanceof ColorSetting) {
ColorSetting colorSetting = (ColorSetting)setting;
components.add(new ColorComponent(colorSetting));
} 
if (setting instanceof TextSetting) {
TextSetting textSetting = (TextSetting)setting;
components.add(new TextComponent(textSetting));
} 
if (setting instanceof SliderSettings) {
SliderSettings valueSetting = (SliderSettings)setting;
components.add(new SliderComponent(valueSetting));
} 
if (setting instanceof ButtonSetting) {
ButtonSetting buttonSetting = (ButtonSetting)setting;
components.add(new ButtonComponent(buttonSetting));
} 
if (setting instanceof SelectSetting) {
SelectSetting selectSetting = (SelectSetting)setting;
components.add(new SelectComponent(selectSetting));
} 
if (setting instanceof MultiSelectSetting) {
MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
components.add(new MultiSelectComponent(multiSelectSetting));
} 
});
}
}


