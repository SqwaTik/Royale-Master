package royale.util;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import royale.Initialization;
import royale.modules.module.ModuleStructure;
public final class Instance
{
private Instance() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
} private static final ConcurrentMap<Class<? extends ModuleStructure>, ModuleStructure> instanceModules = new ConcurrentHashMap<>();
public static <T extends ModuleStructure> T get(Class<T> clazz) {
return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Initialization.getInstance().getManager().getModuleProvider().get(instance)));
}
public static <T extends ModuleStructure> T get(String module) {
return (T)Initialization.getInstance().getManager().getModuleProvider().get(module);
}
}


