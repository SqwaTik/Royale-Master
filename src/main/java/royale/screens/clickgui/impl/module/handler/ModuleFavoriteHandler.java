package royale.screens.clickgui.impl.module.handler;
import java.util.List;
import royale.modules.module.ModuleStructure;
public class ModuleFavoriteHandler
{
public void toggleFavorite(ModuleStructure module, List<ModuleStructure> displayModules, ModuleAnimationHandler animationHandler) {
if (module == null)
return; 
module.switchFavorite();
for (ModuleStructure mod : displayModules) {
float posAnim = ((Float)animationHandler.getPositionAnimations().getOrDefault(mod, Float.valueOf(1.0F))).floatValue();
if (posAnim >= 0.99F) {
animationHandler.getPositionAnimations().put(mod, Float.valueOf(0.0F));
}
if (!animationHandler.getModuleAlphaAnimations().containsKey(mod)) {
animationHandler.getModuleAlphaAnimations().put(mod, Float.valueOf(1.0F));
}
} 
animationHandler.getModuleAlphaAnimations().put(module, Float.valueOf(0.0F));
}
}


