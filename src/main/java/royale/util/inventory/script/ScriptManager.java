package royale.util.inventory.script;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
public class ScriptManager {
private final Map<String, Script> scripts = new ConcurrentHashMap<>();
public Optional<Script> getScript(String name) {
return isNullOrEmpty(name) ? Optional.<Script>empty() : Optional.<Script>of(this.scripts.computeIfAbsent(name, x -> new Script()));
}
public Script addScript(String name, Script script) {
if (isNullOrEmpty(name) || script == null) {
throw new IllegalArgumentException("Script name or instance cannot be null or empty");
}
return this.scripts.put(name, script);
}
public boolean containsScript(String name) {
return (!isNullOrEmpty(name) && this.scripts.containsKey(name));
}
public boolean finished(String name) {
return (!isNullOrEmpty(name) && getScript(name).isPresent() && ((Script)getScript(name).get()).isFinished());
}
public void removeScript(String name) {
if (!isNullOrEmpty(name)) {
this.scripts.remove(name);
}
}
public void cleanupScript(String name) {
if (!isNullOrEmpty(name)) {
this.scripts.computeIfPresent(name, (k, v) -> {
v.cleanup();
return v;
});
}
}
public void cleanupAll() {
this.scripts.forEach((k, v) -> v.cleanup());
}
public void clearAll() {
this.scripts.clear();
}
public void updateScript(String name) {
updateScript(name, () -> Boolean.valueOf(true));
}
public void updateScript(String name, Supplier<Boolean> condition) {
if (((Boolean)condition.get()).booleanValue() && !isNullOrEmpty(name)) {
this.scripts.computeIfPresent(name, (k, v) -> {
v.update();
return v;
});
}
}
public void updateAll() {
this.scripts.values().forEach(Script::update);
}
public Set<String> getAllScriptNames() {
return Collections.unmodifiableSet(this.scripts.keySet());
}
public Map<String, Script> getAllScripts() {
return Collections.unmodifiableMap(this.scripts);
}
private boolean isNullOrEmpty(String str) {
return (str == null || str.trim().isEmpty());
}
}


