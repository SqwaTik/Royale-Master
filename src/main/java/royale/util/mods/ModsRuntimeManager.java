package royale.util.mods;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ModsRuntimeManager {
    private static final ModsRuntimeManager INSTANCE = new ModsRuntimeManager();

    private static final String JAR_EXTENSION = ".jar";
    private static final String DISABLED_SUFFIX = ".disabled";

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]*)\"");

    private final Map<String, Boolean> runtimeEnabledStates = new HashMap<>();
    private ScanResult lastResult = ScanResult.empty(Path.of("mods"));

    private ModsRuntimeManager() {
    }

    public static ModsRuntimeManager getInstance() {
        return INSTANCE;
    }

    public synchronized ScanResult scanNow() {
        Path modsDirectory = resolveModsDirectory();
        try {
            Files.createDirectories(modsDirectory);
        } catch (IOException ignored) {
            this.lastResult = ScanResult.empty(modsDirectory);
            return this.lastResult;
        }

        if (!Files.isDirectory(modsDirectory)) {
            this.lastResult = ScanResult.empty(modsDirectory);
            return this.lastResult;
        }

        List<ScannedMod> scannedMods = new ArrayList<>();
        try (Stream<Path> stream = Files.list(modsDirectory)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedModFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .forEach(path -> scannedMods.add(scanSingleJar(path)));
        } catch (IOException ignored) {
        }

        Set<String> activeKeys = new HashSet<>();
        for (ScannedMod scannedMod : scannedMods) {
            activeKeys.add(normalizeStateKey(scannedMod.getFileName()));
        }
        this.runtimeEnabledStates.keySet().removeIf(key -> !activeKeys.contains(key));

        this.lastResult = new ScanResult(System.currentTimeMillis(), modsDirectory, List.copyOf(scannedMods));
        return this.lastResult;
    }

    public synchronized ScanResult getLastResult() {
        return this.lastResult;
    }

    public synchronized ScannedMod findByLogicalFileName(String logicalFileName) {
        if (logicalFileName == null || logicalFileName.isBlank()) {
            return null;
        }

        ScanResult scanResult = scanNow();
        for (ScannedMod scannedMod : scanResult.getMods()) {
            if (scannedMod.getFileName().equalsIgnoreCase(logicalFileName)) {
                return scannedMod;
            }
        }

        return null;
    }

    public synchronized ToggleResult setEnabled(ScannedMod mod, boolean enabled) {
        if (mod == null) {
            return new ToggleResult(false, "mod is null");
        }

        Path source = resolveCurrentPath(mod);
        if (source == null || !Files.exists(source)) {
            return new ToggleResult(false, "file not found");
        }

        String fileName = source.getFileName().toString();
        String lower = fileName.toLowerCase(Locale.ROOT);
        boolean disabledOnDisk = lower.endsWith(JAR_EXTENSION + DISABLED_SUFFIX);
        boolean enabledOnDisk = lower.endsWith(JAR_EXTENSION);

        if (!disabledOnDisk && !enabledOnDisk) {
            return new ToggleResult(false, "unsupported file type");
        }

        String logicalName = normalizeLogicalFileName(fileName);

        if (enabled) {
            if (enabledOnDisk) {
                this.runtimeEnabledStates.put(normalizeStateKey(logicalName), true);
                scanNow();
                return new ToggleResult(true, "already enabled");
            }

            Path target = source.resolveSibling(logicalName);
            try {
                moveFile(source, target);
                this.runtimeEnabledStates.put(normalizeStateKey(logicalName), true);
                scanNow();
                return new ToggleResult(true, "enabled");
            } catch (IOException exception) {
                return new ToggleResult(false, "enable failed: " + ioMessage(exception));
            }
        }

        if (disabledOnDisk) {
            this.runtimeEnabledStates.put(normalizeStateKey(logicalName), false);
            scanNow();
            return new ToggleResult(true, "already disabled");
        }

        Path target = source.resolveSibling(fileName + DISABLED_SUFFIX);
        try {
            moveFile(source, target);
            this.runtimeEnabledStates.put(normalizeStateKey(logicalName), false);
            scanNow();
            return new ToggleResult(true, "disabled");
        } catch (IOException exception) {
            if (isLikelyFileLock(exception)) {
                this.runtimeEnabledStates.put(normalizeStateKey(logicalName), false);
                scanNow();
                return new ToggleResult(true, "runtime disabled (file lock): " + ioMessage(exception));
            }
            return new ToggleResult(false, "disable failed: " + ioMessage(exception));
        }
    }


    private Path resolveCurrentPath(ScannedMod mod) {
        Path source = mod.getPath();
        if (source != null && Files.exists(source)) {
            return source;
        }

        Path modsDirectory = resolveModsDirectory();
        Path enabledPath = modsDirectory.resolve(mod.getFileName());
        if (Files.exists(enabledPath)) {
            return enabledPath;
        }

        Path disabledPath = modsDirectory.resolve(mod.getFileName() + DISABLED_SUFFIX);
        if (Files.exists(disabledPath)) {
            return disabledPath;
        }

        return source;
    }

    private static void moveFile(Path source, Path target) throws IOException {
        if (source.equals(target)) {
            return;
        }

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException first) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException second) {
                second.addSuppressed(first);
                throw second;
            }
        }
    }
    private static boolean isLikelyFileLock(IOException exception) {
        String message = ioMessage(exception).toLowerCase(Locale.ROOT);
        return message.contains("used by another process")
                || message.contains("being used")
                || message.contains("access is denied")
                || message.contains("permission denied");
    }


    private static String ioMessage(IOException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.replace('\r', ' ').replace('\n', ' ').trim();
    }
    public Path resolveModsDirectory() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Path basePath;
        if (mc != null && mc.runDirectory != null) {
            basePath = mc.runDirectory.toPath();
        } else {
            basePath = Path.of(System.getProperty("user.dir"));
        }
        return basePath.resolve("mods");
    }

    private boolean isSupportedModFile(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(JAR_EXTENSION) || name.endsWith(JAR_EXTENSION + DISABLED_SUFFIX);
    }

    private ScannedMod scanSingleJar(Path jarPath) {
        String physicalFileName = jarPath.getFileName().toString();
        String logicalFileName = normalizeLogicalFileName(physicalFileName);
        boolean enabledOnDisk = !physicalFileName.toLowerCase(Locale.ROOT).endsWith(JAR_EXTENSION + DISABLED_SUFFIX);
        Boolean runtimeState = this.runtimeEnabledStates.get(normalizeStateKey(logicalFileName));
        boolean enabled = enabledOnDisk && (runtimeState == null || runtimeState);

        long size = safeSize(jarPath);
        long lastModified = safeLastModified(jarPath);
        String fallbackName = stripExtension(logicalFileName);

        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            ZipEntry fabricJson = zipFile.getEntry("fabric.mod.json");
            ZipEntry quiltJson = zipFile.getEntry("quilt.mod.json");

            if (fabricJson == null && quiltJson == null) {
                return new ScannedMod(
                        logicalFileName,
                        fallbackName,
                        fallbackName.toLowerCase(Locale.ROOT),
                        "unknown",
                        "No metadata",
                        size,
                        lastModified,
                        enabled,
                        Compatibility.INCOMPATIBLE,
                        "fabric.mod.json/quilt.mod.json not found",
                        jarPath
                );
            }

            ZipEntry metadataEntry = fabricJson != null ? fabricJson : quiltJson;
            String metadata = readEntry(zipFile, metadataEntry);

            String modId = extract(metadata, ID_PATTERN, fallbackName.toLowerCase(Locale.ROOT));
            String modName = extract(metadata, NAME_PATTERN, fallbackName);
            String modVersion = extract(metadata, VERSION_PATTERN, "unknown");
            String description = extract(metadata, DESCRIPTION_PATTERN, "");

            Compatibility compatibility = detectCompatibility(metadata);
            String reason = switch (compatibility) {
                case READY -> "Metadata OK";
                case WARNING -> "Metadata is incomplete";
                case INCOMPATIBLE -> "Not a valid Fabric/Quilt mod";
            };

            return new ScannedMod(
                    logicalFileName,
                    modName,
                    modId,
                    modVersion,
                    description,
                    size,
                    lastModified,
                    enabled,
                    compatibility,
                    reason,
                    jarPath
            );
        } catch (Exception e) {
            return new ScannedMod(
                    logicalFileName,
                    fallbackName,
                    fallbackName.toLowerCase(Locale.ROOT),
                    "unknown",
                    "",
                    size,
                    lastModified,
                    enabled,
                    Compatibility.INCOMPATIBLE,
                    "Read error: " + e.getClass().getSimpleName(),
                    jarPath
            );
        }
    }

    private static String readEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Compatibility detectCompatibility(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Compatibility.INCOMPATIBLE;
        }

        String lower = metadata.toLowerCase(Locale.ROOT);
        boolean hasModId = lower.contains("\"id\"");
        boolean hasDependsMinecraft = lower.contains("minecraft");
        boolean hasLoader =
                lower.contains("fabricloader")
                        || lower.contains("fabric-loader")
                        || lower.contains("quilt_loader")
                        || lower.contains("quilt-loader");

        if (!hasModId) {
            return Compatibility.INCOMPATIBLE;
        }
        if (!hasDependsMinecraft || !hasLoader) {
            return Compatibility.WARNING;
        }
        return Compatibility.READY;
    }

    private static String extract(String source, Pattern pattern, String fallback) {
        if (source == null || source.isBlank()) {
            return fallback;
        }
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            String value = matcher.group(1);
            return value == null || value.isBlank() ? fallback : value.trim();
        }
        return fallback;
    }

    private static long safeSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static long safeLastModified(Path file) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(file);
            return fileTime.toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.toLowerCase(Locale.ROOT).lastIndexOf(JAR_EXTENSION);
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private static String normalizeLogicalFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(JAR_EXTENSION + DISABLED_SUFFIX)) {
            return fileName.substring(0, fileName.length() - DISABLED_SUFFIX.length());
        }
        return fileName;
    }

    private static String normalizeStateKey(String logicalFileName) {
        return logicalFileName.toLowerCase(Locale.ROOT).trim();
    }

    public enum Compatibility {
        READY,
        WARNING,
        INCOMPATIBLE
    }

    public static final class ToggleResult {
        private final boolean success;
        private final String message;

        private ToggleResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public static final class ScanResult {
        private final long scannedAtMs;
        private final Path modsDirectory;
        private final List<ScannedMod> mods;

        private ScanResult(long scannedAtMs, Path modsDirectory, List<ScannedMod> mods) {
            this.scannedAtMs = scannedAtMs;
            this.modsDirectory = modsDirectory;
            this.mods = mods;
        }

        public static ScanResult empty(Path modsDirectory) {
            return new ScanResult(System.currentTimeMillis(), modsDirectory, List.of());
        }

        public long getScannedAtMs() {
            return this.scannedAtMs;
        }

        public Path getModsDirectory() {
            return this.modsDirectory;
        }

        public List<ScannedMod> getMods() {
            return this.mods;
        }

        public int totalCount() {
            return this.mods.size();
        }

        public int enabledCount() {
            int count = 0;
            for (ScannedMod mod : this.mods) {
                if (mod.isEnabled()) {
                    count++;
                }
            }
            return count;
        }

        public int disabledCount() {
            return this.mods.size() - enabledCount();
        }

        public int readyCount() {
            return countBy(Compatibility.READY, true);
        }

        public int warningCount() {
            return countBy(Compatibility.WARNING, true);
        }

        public int incompatibleCount() {
            return countBy(Compatibility.INCOMPATIBLE, true);
        }

        public String digest() {
            StringBuilder builder = new StringBuilder(64 + this.mods.size() * 32);
            builder.append(this.mods.size()).append('|');
            for (ScannedMod mod : this.mods) {
                builder
                        .append(mod.getFileName()).append(':')
                        .append(mod.getSize()).append(':')
                        .append(mod.getLastModified()).append(':')
                        .append(mod.getCompatibility().name()).append(':')
                        .append(mod.isEnabled() ? '1' : '0').append('|');
            }
            return builder.toString();
        }

        private int countBy(Compatibility compatibility, boolean onlyEnabled) {
            int count = 0;
            for (ScannedMod mod : this.mods) {
                if (onlyEnabled && !mod.isEnabled()) {
                    continue;
                }
                if (mod.getCompatibility() == compatibility) {
                    count++;
                }
            }
            return count;
        }
    }

    public static final class ScannedMod {
        private final String fileName;
        private final String name;
        private final String id;
        private final String version;
        private final String description;
        private final long size;
        private final long lastModified;
        private final boolean enabled;
        private final Compatibility compatibility;
        private final String reason;
        private final Path path;

        private ScannedMod(
                String fileName,
                String name,
                String id,
                String version,
                String description,
                long size,
                long lastModified,
                boolean enabled,
                Compatibility compatibility,
                String reason,
                Path path
        ) {
            this.fileName = fileName;
            this.name = name;
            this.id = id;
            this.version = version;
            this.description = description;
            this.size = size;
            this.lastModified = lastModified;
            this.enabled = enabled;
            this.compatibility = compatibility;
            this.reason = reason;
            this.path = path;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getName() {
            return this.name;
        }

        public String getId() {
            return this.id;
        }

        public String getVersion() {
            return this.version;
        }

        public String getDescription() {
            return this.description;
        }

        public long getSize() {
            return this.size;
        }

        public long getLastModified() {
            return this.lastModified;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public Compatibility getCompatibility() {
            return this.compatibility;
        }

        public String getReason() {
            return this.reason;
        }

        public Path getPath() {
            return this.path;
        }
    }
}
