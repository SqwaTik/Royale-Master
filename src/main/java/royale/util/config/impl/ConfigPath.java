package royale.util.config.impl;
import java.nio.file.Path;
import java.nio.file.Paths;
public class ConfigPath
{
private static final String ROOT_DIR = "Royale";
private static final String CONFIG_DIR = "configs";
private static final String AUTO_DIR = "autocfg";
public static final String CONFIG_EXTENSION = ".cfg";
private static final String CONFIG_FILE = "autoconfig.cfg";
private static final String LEGACY_CONFIG_FILE = "autoconfig.json";
private static Path runDirectory;
public static void init() {
runDirectory = Paths.get("", new String[0]).toAbsolutePath();
}
public static Path getConfigDirectory() {
return runDirectory.resolve("Royale").resolve("configs").resolve("autocfg");
}
public static Path getConfigFile() {
return getConfigDirectory().resolve("autoconfig.cfg");
}
public static Path getLegacyConfigFile() {
return getConfigDirectory().resolve("autoconfig.json");
}
}


