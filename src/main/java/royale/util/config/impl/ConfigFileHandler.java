package royale.util.config.impl;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import royale.util.config.impl.consolelogger.Logger;
public class ConfigFileHandler
{
private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
public void createDirectories() {
try {
Files.createDirectories(ConfigPath.getConfigDirectory(), (FileAttribute<?>[])new FileAttribute[0]);
} catch (IOException e) {
Logger.error("AutoConfiguration: Failed to create directories!");
} 
}
public boolean write(String content) {
this.lock.writeLock().lock();
try {
Path configFile = ConfigPath.getConfigFile();
Path tempFile = configFile.resolveSibling(String.valueOf(configFile.getFileName()) + ".tmp");
Files.writeString(tempFile, content, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
Files.move(tempFile, configFile, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE });
return true;
} catch (IOException e) {
Logger.error("AutoConfiguration: Write failed! " + e.getMessage());
return false;
} finally {
this.lock.writeLock().unlock();
} 
}
public String read() {
this.lock.readLock().lock();
try {
Path configFile = ConfigPath.getConfigFile();
if (!Files.exists(configFile, new java.nio.file.LinkOption[0])) {
return null;
}
return Files.readString(configFile, StandardCharsets.UTF_8);
} catch (IOException e) {
Logger.error("AutoConfiguration: Read failed! " + e.getMessage());
return null;
} finally {
this.lock.readLock().unlock();
} 
}
public boolean exists() {
return Files.exists(ConfigPath.getConfigFile(), new java.nio.file.LinkOption[0]);
}
}


