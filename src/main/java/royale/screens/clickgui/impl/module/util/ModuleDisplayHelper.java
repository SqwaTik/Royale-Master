package royale.screens.clickgui.impl.module.util;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import royale.modules.module.ModuleStructure;
import royale.modules.module.setting.Setting;
public class ModuleDisplayHelper
{
private final Set<ModuleStructure> modulesWithSettings = new HashSet<>();
public void updateModulesWithSettings(List<ModuleStructure> displayModules) {
this.modulesWithSettings.clear();
for (ModuleStructure mod : displayModules) {
if (hasModuleSettings(mod)) {
this.modulesWithSettings.add(mod);
}
} 
}
public boolean hasModuleSettings(ModuleStructure module) {
if (module == null) return false; 
List<Setting> settings = module.settings();
return (settings != null && !settings.isEmpty());
}
public boolean hasSettings(ModuleStructure module) {
return this.modulesWithSettings.contains(module);
}
}


