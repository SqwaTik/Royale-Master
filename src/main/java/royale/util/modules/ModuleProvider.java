package royale.util.modules;
import java.util.List;
import java.util.Objects;
import royale.modules.module.ModuleStructure;
public class ModuleProvider
{
private final List<ModuleStructure> moduleStructures;
public ModuleProvider(List<ModuleStructure> moduleStructures) {
this.moduleStructures = moduleStructures;
}
public List<ModuleStructure> getModuleStructures() {
return this.moduleStructures;
}
public <T extends ModuleStructure> T get(String name) {
return (T)this.moduleStructures.stream()
.filter(module -> module.getName().equalsIgnoreCase(name) || module.getStorageName().equalsIgnoreCase(name))
.map(module -> module)
.findFirst()
.orElse(null);
}
public <T extends ModuleStructure> T get(Class<T> clazz) {
Objects.requireNonNull(clazz); return (T)this.moduleStructures.stream().filter(module -> clazz.isAssignableFrom(module.getClass())).map(clazz::cast)
.findFirst()
.orElse(null);
}
}
