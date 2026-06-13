package royale.util.render.gif;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import royale.modules.impl.misc.GifManager;
import royale.util.render.Render2D;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GifRender {
    private static final List<Identifier> DEFAULT_AVATAR_FRAMES = new ArrayList<>();
    private static final List<Identifier> DEFAULT_BACKGROUND_FRAMES = new ArrayList<>();
    private static final Map<String, List<Identifier>> CUSTOM_AVATAR_FRAMES = new HashMap<>();
    private static final Map<String, List<Identifier>> CUSTOM_BACKGROUND_FRAMES = new HashMap<>();
    private static final List<String> AVAILABLE_AVATAR_SOURCES = new ArrayList<>();
    private static final List<String> AVAILABLE_BACKGROUND_SOURCES = new ArrayList<>();
    private static final Map<String, String> ENTRY_SIGNATURES = new HashMap<>();
    private static final Set<String> SUPPORTED_EXTENSIONS = buildSupportedExtensions();
    private static final int MAX_CUSTOM_FRAMES = 240;
    private static final int AVATAR_TARGET_SIZE = 256;
    private static final int BACKGROUND_TARGET_WIDTH = 640;
    private static final int BACKGROUND_TARGET_HEIGHT = 320;
    private static final long CUSTOM_SCAN_INTERVAL_MS = 1800L;
    private static long lastAvatarTime = 0L;
    private static long lastBackgroundTime = 0L;
    private static long lastCustomScanAt = 0L;
    private static int avatarFrameIndex = 0;
    private static int backgroundFrameIndex = 0;
    private static String lastAvatarSourceKey = "default";
    private static String lastBackgroundSourceKey = "default";
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        DEFAULT_AVATAR_FRAMES.clear();
        DEFAULT_BACKGROUND_FRAMES.clear();

        for (int i = 1; i <= 100; i++) {
            String frameName = String.format("image%03d", i);
            DEFAULT_AVATAR_FRAMES.add(Identifier.of("royale", "images/gifs/avatar/" + frameName + ".png"));
        }

        for (int i = 0; i <= 16; i++) {
            String frameName = String.format("frame_%02d_delay-0.05s", i);
            DEFAULT_BACKGROUND_FRAMES.add(Identifier.of("royale", "images/gifs/back/" + frameName + ".png"));
        }

        ensureCustomEntries(true);
        lastAvatarTime = System.currentTimeMillis();
        lastBackgroundTime = System.currentTimeMillis();
        initialized = true;
    }

    public static void tick() {
        if (!initialized) {
            return;
        }

        ensureCustomEntries();
        long currentTime = System.currentTimeMillis();
        String avatarSourceKey = resolveAvatarSourceKey();
        String backgroundSourceKey = resolveBackgroundSourceKey();

        if (!lastAvatarSourceKey.equals(avatarSourceKey)) {
            lastAvatarSourceKey = avatarSourceKey;
            resetAvatar();
        }

        if (!lastBackgroundSourceKey.equals(backgroundSourceKey)) {
            lastBackgroundSourceKey = backgroundSourceKey;
            resetBackground();
        }

        List<Identifier> avatarFrames = getAvatarFrames(avatarSourceKey);
        List<Identifier> backgroundFrames = getBackgroundFrames(backgroundSourceKey);

        if (!avatarFrames.isEmpty() && currentTime - lastAvatarTime >= getAvatarDelay()) {
            avatarFrameIndex = (avatarFrameIndex + 1) % avatarFrames.size();
            lastAvatarTime = currentTime;
        }

        if (!backgroundFrames.isEmpty() && currentTime - lastBackgroundTime >= getBackgroundDelay()) {
            backgroundFrameIndex = (backgroundFrameIndex + 1) % backgroundFrames.size();
            lastBackgroundTime = currentTime;
        }
    }

    public static void drawAvatar(float x, float y, float width, float height, int color) {
        drawAvatar(x, y, width, height, 15.0F, color);
    }

    public static void drawAvatar(float x, float y, float width, float height, float radius, int color) {
        if (!initialized) {
            init();
        }

        List<Identifier> frames = getAvatarFrames(resolveAvatarSourceKey());
        if (frames.isEmpty()) {
            return;
        }

        Identifier frame = frames.get(Math.floorMod(avatarFrameIndex, frames.size()));
        Render2D.texture(frame, x, y, width, height, 1.0F, radius, color);
    }

    public static void drawBackground(float x, float y, float width, float height, int color) {
        drawBackground(x, y, width, height, 0.0F, color);
    }

    public static void drawBackground(float x, float y, float width, float height, float radius, int color) {
        if (!initialized) {
            init();
        }

        List<Identifier> frames = getBackgroundFrames(resolveBackgroundSourceKey());
        if (frames.isEmpty()) {
            return;
        }

        Identifier frame = frames.get(Math.floorMod(backgroundFrameIndex, frames.size()));
        Render2D.texture(frame, x, y, width, height, 1.0F, radius, color);
    }

    public static void resetAvatar() {
        avatarFrameIndex = 0;
        lastAvatarTime = System.currentTimeMillis();
    }

    public static void resetBackground() {
        backgroundFrameIndex = 0;
        lastBackgroundTime = System.currentTimeMillis();
    }

    public static void reset() {
        resetAvatar();
        resetBackground();
    }

    public static Path getCustomGifRoot() {
        net.minecraft.client.MinecraftClient client = getClient();
        if (client != null && client.runDirectory != null) {
            return client.runDirectory.toPath().resolve("royale-master").resolve("gifs");
        }
        return Path.of(System.getProperty("user.dir"), "royale-master", "gifs");
    }

    public static List<String> getAvailableAvatarSources() {
        ensureCustomEntries();
        return List.copyOf(AVAILABLE_AVATAR_SOURCES);
    }

    public static List<String> getAvailableBackgroundSources() {
        ensureCustomEntries();
        return List.copyOf(AVAILABLE_BACKGROUND_SOURCES);
    }

    public static void refreshCustomEntriesNow() {
        ensureCustomEntries(true);
    }

    private static List<Identifier> getAvatarFrames(String sourceKey) {
        List<Identifier> frames = CUSTOM_AVATAR_FRAMES.get(sourceKey);
        return frames != null && !frames.isEmpty() ? frames : DEFAULT_AVATAR_FRAMES;
    }

    private static List<Identifier> getBackgroundFrames(String sourceKey) {
        List<Identifier> frames = CUSTOM_BACKGROUND_FRAMES.get(sourceKey);
        return frames != null && !frames.isEmpty() ? frames : DEFAULT_BACKGROUND_FRAMES;
    }

    private static String resolveAvatarSourceKey() {
        GifManager gifManager = GifManager.getInstance();
        return gifManager != null ? gifManager.getAvatarSetKey() : "default";
    }

    private static String resolveBackgroundSourceKey() {
        GifManager gifManager = GifManager.getInstance();
        return gifManager != null ? gifManager.getBackgroundSetKey() : "default";
    }

    private static long getAvatarDelay() {
        GifManager gifManager = GifManager.getInstance();
        return gifManager != null ? gifManager.getAvatarFrameDelay() : 33L;
    }

    private static long getBackgroundDelay() {
        GifManager gifManager = GifManager.getInstance();
        return gifManager != null ? gifManager.getBackgroundFrameDelay() : 50L;
    }

    private static void ensureCustomEntries() {
        ensureCustomEntries(false);
    }

    private static void ensureCustomEntries(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastCustomScanAt < CUSTOM_SCAN_INTERVAL_MS) {
            return;
        }

        syncCustomRoot(getCustomGifRoot().resolve("avatar"), "avatar", CUSTOM_AVATAR_FRAMES, AVAILABLE_AVATAR_SOURCES);
        syncCustomRoot(getCustomGifRoot().resolve("background"), "background", CUSTOM_BACKGROUND_FRAMES, AVAILABLE_BACKGROUND_SOURCES);
        lastCustomScanAt = now;
    }

    private static void syncCustomRoot(Path root, String type, Map<String, List<Identifier>> registry, List<String> available) {
        try {
            Files.createDirectories(root);
        } catch (Exception ignored) {
            available.clear();
            registry.clear();
            return;
        }

        List<SourceEntry> discovered = discoverEntries(root);
        available.clear();
        available.addAll(discovered.stream().map(SourceEntry::key).toList());

        Set<String> aliveKeys = discovered.stream().map(SourceEntry::key).collect(Collectors.toSet());
        registry.keySet().removeIf(key -> !aliveKeys.contains(key));
        ENTRY_SIGNATURES.keySet().removeIf(key -> key.startsWith(type + ":") && !aliveKeys.contains(key.substring((type + ":").length())));

        for (SourceEntry entry : discovered) {
            String signature = buildEntrySignature(entry.path());
            String signatureKey = type + ":" + entry.key();
            if (signature.equals(ENTRY_SIGNATURES.get(signatureKey)) && registry.containsKey(entry.key())) {
                continue;
            }

            ENTRY_SIGNATURES.put(signatureKey, signature);
            List<Identifier> frames = entry.directory()
                    ? loadFramesFromDirectory(entry.path(), type, entry.key())
                    : registerFrameTextures(entry.path(), type, entry.key(), 0, MAX_CUSTOM_FRAMES);

            if (frames.isEmpty()) {
                registry.remove(entry.key());
            } else {
                registry.put(entry.key(), frames);
            }
        }
    }

    private static List<SourceEntry> discoverEntries(Path root) {
        List<SourceEntry> discovered = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();

        try (Stream<Path> stream = Files.list(root)) {
            List<Path> children = stream
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();

            for (Path child : children) {
                boolean directory = Files.isDirectory(child);
                boolean file = Files.isRegularFile(child);
                if (!directory && !file) {
                    continue;
                }
                if (directory && !containsSupportedFiles(child)) {
                    continue;
                }
                if (file && !isSupportedFrameFile(child)) {
                    continue;
                }

                String key = buildUniqueEntryKey(child, directory, usedKeys);
                discovered.add(new SourceEntry(key, child, directory));
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        return discovered;
    }

    private static String buildUniqueEntryKey(Path child, boolean directory, Set<String> usedKeys) {
        String baseName = child.getFileName().toString();
        String candidate = directory ? baseName : stripExtension(baseName);
        if (candidate.isBlank()) {
            candidate = baseName;
        }
        if ("original".equalsIgnoreCase(candidate)) {
            candidate = directory ? "Original Folder" : "Original File";
        }

        String unique = candidate;
        String uniqueKey = unique.toLowerCase(Locale.ROOT);
        if (usedKeys.add(uniqueKey)) {
            return unique;
        }

        String suffix = directory ? "folder" : getExtension(child);
        if (suffix.isBlank()) {
            suffix = "file";
        }

        unique = candidate + " (" + suffix + ")";
        uniqueKey = unique.toLowerCase(Locale.ROOT);
        int index = 2;
        while (!usedKeys.add(uniqueKey)) {
            unique = candidate + " (" + suffix + " " + index++ + ")";
            uniqueKey = unique.toLowerCase(Locale.ROOT);
        }
        return unique;
    }

    private static boolean containsSupportedFiles(Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.anyMatch(path -> Files.isRegularFile(path) && isSupportedFrameFile(path));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String buildEntrySignature(Path entryPath) {
        try {
            if (Files.isRegularFile(entryPath)) {
                return entryPath.getFileName() + ":" + Files.size(entryPath) + ":" + Files.getLastModifiedTime(entryPath).toMillis();
            }

            try (Stream<Path> stream = Files.walk(entryPath)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(GifRender::isSupportedFrameFile)
                        .sorted(Comparator.comparing(path -> path.toString().toLowerCase(Locale.ROOT)))
                        .map(path -> {
                            try {
                                return entryPath.relativize(path) + ":" + Files.size(path) + ":" + Files.getLastModifiedTime(path).toMillis();
                            } catch (Exception ignored) {
                                return entryPath.relativize(path).toString();
                            }
                        })
                        .collect(Collectors.joining("|"));
            }
        } catch (Exception ignored) {
            return "";
        }
    }

    private static List<Identifier> loadFramesFromDirectory(Path directory, String type, String sourceKey) {
        List<Identifier> frames = new ArrayList<>();

        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(GifRender::isSupportedFrameFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();

            int frameCursor = 0;
            for (Path framePath : files) {
                if (frameCursor >= MAX_CUSTOM_FRAMES) {
                    break;
                }

                List<Identifier> loaded = registerFrameTextures(framePath, type, sourceKey, frameCursor, MAX_CUSTOM_FRAMES - frameCursor);
                frames.addAll(loaded);
                frameCursor += loaded.size();
            }
        } catch (Exception ignored) {
            return List.of();
        }

        return frames;
    }

    private static List<Identifier> registerFrameTextures(Path framePath, String type, String sourceKey, int startIndex, int remaining) {
        if (remaining <= 0) {
            return List.of();
        }

        String extension = getExtension(framePath);
        if ("gif".equals(extension)) {
            return registerGifTextures(framePath, type, sourceKey, startIndex, remaining);
        }

        Identifier identifier = registerStaticTexture(framePath, type, sourceKey, startIndex);
        return identifier == null ? List.of() : List.of(identifier);
    }

    private static Identifier registerStaticTexture(Path framePath, String type, String sourceKey, int index) {
        try {
            BufferedImage buffered = ImageIO.read(framePath.toFile());
            if (buffered == null) {
                return null;
            }

            NativeImage nativeImage = toNativeImage(scaleForTarget(buffered, type));
            if (nativeImage == null) {
                return null;
            }

            return registerFrameTexture(nativeImage, framePath, type, sourceKey, index, 0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<Identifier> registerGifTextures(Path gifPath, String type, String sourceKey, int startIndex, int remaining) {
        List<Identifier> identifiers = new ArrayList<>();
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(gifPath.toFile())) {
            if (inputStream == null) {
                return List.of();
            }

            Iterator<ImageReader> iterator = ImageIO.getImageReadersBySuffix("gif");
            if (!iterator.hasNext()) {
                return List.of();
            }

            ImageReader reader = iterator.next();
            try {
                reader.setInput(inputStream, false, false);
                int frameCount;
                try {
                    frameCount = reader.getNumImages(true);
                } catch (Exception ignored) {
                    frameCount = remaining;
                }

                int safeFrameCount = frameCount > 0 ? Math.min(frameCount, remaining) : remaining;
                for (int frameIndex = 0; frameIndex < safeFrameCount; frameIndex++) {
                    BufferedImage buffered;
                    try {
                        buffered = reader.read(frameIndex);
                    } catch (Exception exception) {
                        break;
                    }
                    if (buffered == null) {
                        continue;
                    }

                    NativeImage nativeImage = toNativeImage(scaleForTarget(buffered, type));
                    if (nativeImage == null) {
                        continue;
                    }

                    Identifier id = registerFrameTexture(nativeImage, gifPath, type, sourceKey, startIndex + identifiers.size(), frameIndex);
                    if (id != null) {
                        identifiers.add(id);
                    }
                }
            } finally {
                reader.dispose();
            }
        } catch (Exception ignored) {
            return List.of();
        }

        return identifiers;
    }

    private static Identifier registerFrameTexture(NativeImage image, Path sourcePath, String type, String sourceKey, int index, int frameIndex) {
        try {
            NativeImageBackedTexture texture = createBackedTexture(image);
            if (texture == null) {
                image.close();
                return null;
            }

            long lastModified = Files.getLastModifiedTime(sourcePath).toMillis();
            String sanitizedKey = sourceKey.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._\\-]", "_");
            String sourceName = sourcePath.getFileName().toString().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._\\-]", "_");
            Identifier id = Identifier.of(
                    "royale",
                    "dynamic_" + type + "_" + sanitizedKey + "_" + sourceName + "_" + index + "_" + frameIndex + "_" + Long.toHexString(lastModified)
            );
            net.minecraft.client.MinecraftClient client = getClient();
            if (client == null) {
                image.close();
                return null;
            }
            client.getTextureManager().registerTexture(id, (AbstractTexture) texture);
            return id;
        } catch (Exception ignored) {
            image.close();
            return null;
        }
    }

    private static BufferedImage scaleForTarget(BufferedImage source, String type) {
        int targetWidth = "avatar".equals(type) ? AVATAR_TARGET_SIZE : BACKGROUND_TARGET_WIDTH;
        int targetHeight = "avatar".equals(type) ? AVATAR_TARGET_SIZE : BACKGROUND_TARGET_HEIGHT;

        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        int sourceWidth = Math.max(1, source.getWidth());
        int sourceHeight = Math.max(1, source.getHeight());
        double scale = Math.min((double) targetWidth / sourceWidth, (double) targetHeight / sourceHeight);
        int drawWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int drawHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        int drawX = (targetWidth - drawWidth) / 2;
        int drawY = (targetHeight - drawHeight) / 2;
        graphics.drawImage(source, drawX, drawY, drawWidth, drawHeight, null);
        graphics.dispose();
        return canvas;
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return NativeImage.read(new ByteArrayInputStream(output.toByteArray()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static NativeImageBackedTexture createBackedTexture(NativeImage image) {
        try {
            Constructor<NativeImageBackedTexture> supplierCtor = NativeImageBackedTexture.class.getDeclaredConstructor(Supplier.class, NativeImage.class);
            supplierCtor.setAccessible(true);
            return supplierCtor.newInstance((Supplier<String>) () -> "royale_gif_frame", image);
        } catch (Exception ignored) {
        }

        try {
            Constructor<NativeImageBackedTexture> stringCtor = NativeImageBackedTexture.class.getDeclaredConstructor(String.class, NativeImage.class);
            stringCtor.setAccessible(true);
            return stringCtor.newInstance("royale_gif_frame", image);
        } catch (Exception ignored) {
        }

        try {
            Constructor<NativeImageBackedTexture> plainCtor = NativeImageBackedTexture.class.getDeclaredConstructor(NativeImage.class);
            plainCtor.setAccessible(true);
            return plainCtor.newInstance(image);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Set<String> buildSupportedExtensions() {
        Set<String> extensions = new TreeSet<>();
        Collections.addAll(extensions, "gif", "png", "jpg", "jpeg", "bmp", "wbmp", "tif", "tiff", "ico", "webp", "apng");
        Collections.addAll(extensions, ImageIO.getReaderFileSuffixes());
        return extensions.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static boolean isSupportedFrameFile(Path path) {
        String extension = getExtension(path);
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    private static String getExtension(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    private static String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private static net.minecraft.client.MinecraftClient getClient() {
        return net.minecraft.client.MinecraftClient.getInstance();
    }

    private record SourceEntry(String key, Path path, boolean directory) {
    }
}
