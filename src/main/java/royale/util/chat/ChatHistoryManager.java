package royale.util.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import royale.modules.impl.misc.ChatHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ChatHistoryManager {
    private static final ChatHistoryManager INSTANCE = new ChatHistoryManager();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile("^\\[(.+?)] \\[(\\w+)] (.*)$");
    private static final Pattern ANGLE_CHAT_PATTERN = Pattern.compile("^\\s*<([\\p{L}0-9_]{2,32})>\\s*(.+)$");
    private static final Pattern STANDARD_CHAT_PATTERN = Pattern.compile("^\\s*(?:\\[[^\\]]+\\]\\s*)*([\\p{L}0-9_]{2,32})\\s*(?:[:\\u00BB]|->|\\|)\\s*(.+)$");
    private static final Pattern TRAILING_NICK_PATTERN = Pattern.compile("([\\p{L}0-9_]{2,32})\\s*$");
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{2,16}$");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final String[] CHAT_DELIMITERS = new String[]{" \u00BB ", ": ", " -> ", " | ", " > "};
    private static final Set<String> SYSTEM_NAMES = Set.of(
            "system", "server", "console", "admin", "notice", "notification"
    );
    private static final int IN_MEMORY_LIMIT = -1;
    private static final int SEARCH_CACHE_LIMIT = 128;
    private static final int DEFAULT_SEARCH_LIMIT = 10;

    private final Object lock = new Object();
    private final List<String> entries = new ArrayList<>();
    private final List<String> entriesLower = new ArrayList<>();
    private final List<HistoryEntry> historyEntries = new ArrayList<>();
    private final Map<String, List<String>> searchCache = new LinkedHashMap<>(SEARCH_CACHE_LIMIT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
            return size() > SEARCH_CACHE_LIMIT;
        }
    };
    private volatile boolean loaded;

    private ChatHistoryManager() {
    }

    public static ChatHistoryManager getInstance() {
        return INSTANCE;
    }

    public void ensureLoaded() {
        if (this.loaded) {
            return;
        }
        synchronized (this.lock) {
            if (this.loaded) {
                return;
            }

            Path file = getLogFilePath();
            try {
                ensureFileExists(file);
                List<String> lines = readAllLines(file);
                this.entries.clear();
                this.entriesLower.clear();
                this.historyEntries.clear();
                for (String line : lines) {
                    addLoadedLine(line);
                }
                this.searchCache.clear();
            } catch (Exception ignored) {
                this.entries.clear();
                this.entriesLower.clear();
                this.historyEntries.clear();
                this.searchCache.clear();
            }
            this.loaded = true;
        }
    }

    public void appendIncoming(Text text) {
        if (text == null) {
            return;
        }
        appendRaw("IN", text.getString());
    }

    public void appendOutgoing(String message) {
        appendRaw("OUT", message);
    }

    public void appendSystem(String message) {
        appendRaw("SYS", message);
    }

    public List<String> search(String query, int limit) {
        ensureLoaded();
        if (query == null || query.isBlank() || limit <= 0) {
            return Collections.emptyList();
        }

        String needle = normalizeQuery(query);
        if (needle.isEmpty()) {
            return Collections.emptyList();
        }

        if (!isOptimizedSearchEnabled()) {
            return computeSearch(needle, limit);
        }

        String cacheKey = needle + "|" + limit;
        synchronized (this.lock) {
            List<String> cached = this.searchCache.get(cacheKey);
            if (cached != null) {
                return new ArrayList<>(cached);
            }
        }

        List<String> computed = computeSearch(needle, limit);
        synchronized (this.lock) {
            this.searchCache.put(cacheKey, new ArrayList<>(computed));
        }
        return computed;
    }

    public List<HistoryEntry> searchEntries(String query, int limit) {
        ensureLoaded();
        if (query == null || query.isBlank() || limit <= 0) {
            return Collections.emptyList();
        }

        String needle = normalizeQuery(query);
        if (needle.isEmpty()) {
            return Collections.emptyList();
        }

        String[] tokens = tokenize(needle);
        List<HistoryEntry> result = new ArrayList<>(Math.min(limit, 16));
        synchronized (this.lock) {
            for (int i = this.entriesLower.size() - 1; i >= 0 && result.size() < limit; i--) {
                if (matchesAllTokens(this.entriesLower.get(i), tokens)) {
                    result.add(this.historyEntries.get(i));
                }
            }
        }
        return result;
    }

    public List<HistoryEntry> getRecentEntries(int limit) {
        return getRecentEntries(limit, 0);
    }

    public List<HistoryEntry> getRecentEntries(int limit, int offsetFromLatest) {
        ensureLoaded();
        if (limit <= 0) {
            return Collections.emptyList();
        }
        synchronized (this.lock) {
            int size = this.historyEntries.size();
            int safeOffset = Math.max(0, offsetFromLatest);
            int end = Math.max(0, size - safeOffset);
            int from = Math.max(0, end - limit);
            List<HistoryEntry> result = new ArrayList<>(Math.max(0, end - from));
            for (int i = from; i < end; i++) {
                result.add(this.historyEntries.get(i));
            }
            return result;
        }
    }

    public int getEntryCount() {
        ensureLoaded();
        synchronized (this.lock) {
            return this.historyEntries.size();
        }
    }

    public int getSearchLimit() {
        ChatHelper helper = ChatHelper.getInstance();
        if (helper == null) {
            return DEFAULT_SEARCH_LIMIT;
        }
        return DEFAULT_SEARCH_LIMIT;
    }

    public boolean isSearchEnabled() {
        ChatHelper helper = ChatHelper.getInstance();
        return helper != null && helper.isState() && helper.moreChatHistory.isValue();
    }

    public boolean isOptimizedSearchEnabled() {
        return true;
    }

    public boolean shouldWriteToFile() {
        return true;
    }

    private List<String> computeSearch(String needle, int limit) {
        String[] tokens = tokenize(needle);
        List<String> result = new ArrayList<>(Math.min(limit, 16));

        synchronized (this.lock) {
            for (int i = this.entriesLower.size() - 1; i >= 0 && result.size() < limit; i--) {
                String lineLower = this.entriesLower.get(i);
                if (matchesAllTokens(lineLower, tokens)) {
                    result.add(this.entries.get(i));
                }
            }
        }
        return result;
    }

    private boolean matchesAllTokens(String value, String[] tokens) {
        if (tokens.length == 0) {
            return false;
        }
        for (String token : tokens) {
            if (!value.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private String normalizeQuery(String query) {
        return normalizeSpaces(query).toLowerCase(Locale.ROOT);
    }

    private String[] tokenize(String query) {
        String normalized = normalizeQuery(query);
        if (normalized.isEmpty()) {
            return new String[0];
        }
        return Stream.of(normalized.split(" "))
                .filter(token -> !token.isBlank())
                .toArray(String[]::new);
    }

    private void appendRaw(String type, String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        ensureLoaded();
        String cleanMessage = message.replace('\n', ' ').replace('\r', ' ').trim();
        if (cleanMessage.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String timestamp = now.format(TIME_FORMAT);
        String line = "[" + timestamp + "] [" + type + "] " + cleanMessage;

        synchronized (this.lock) {
            addInMemory(line, timestamp, type, cleanMessage, nowMillis);
        }

        if (shouldWriteToFile()) {
            Path file = getLogFilePath();
            try {
                ensureFileExists(file);
                Files.writeString(
                        file,
                        line + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                );
            } catch (IOException ignored) {
            }
        }
    }

    private void addLoadedLine(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        Matcher matcher = LOG_LINE_PATTERN.matcher(line);
        if (matcher.matches()) {
            String timestamp = matcher.group(1);
            String type = matcher.group(2);
            String message = matcher.group(3);
            long timeMillis = parseTimestampMillis(timestamp);
            addInMemory(line, timestamp, type, message, timeMillis);
            return;
        }
        addInMemory(line, "", "IN", line, 0L);
    }

    private void addInMemory(String fullLine, String timestamp, String type, String message, long timeMillis) {
        this.entries.add(fullLine);
        this.entriesLower.add(fullLine.toLowerCase(Locale.ROOT));
        this.historyEntries.add(buildHistoryEntry(timestamp, type, message, fullLine, timeMillis));
        trimInMemoryIfNeeded();
        this.searchCache.clear();
    }

    private HistoryEntry buildHistoryEntry(String timestamp, String type, String message, String rawLine, long timeMillis) {
        SenderParseResult parsed;
        if ("OUT".equals(type)) {
            parsed = new SenderParseResult(resolveSelfName(), message);
        } else {
            parsed = parseSender(message);
        }

        return new HistoryEntry(
                timeMillis,
                timestamp,
                type,
                parsed.sender,
                parsed.content,
                rawLine
        );
    }

    private void trimInMemoryIfNeeded() {
        if (IN_MEMORY_LIMIT <= 0) {
            return;
        }
        while (this.entries.size() > IN_MEMORY_LIMIT) {
            this.entries.remove(0);
            this.entriesLower.remove(0);
            this.historyEntries.remove(0);
        }
    }

    private long parseTimestampMillis(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return 0L;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, TIME_FORMAT);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private SenderParseResult parseSender(String message) {
        if (message == null || message.isBlank()) {
            return new SenderParseResult(null, "");
        }
        String trimmed = normalizeSpaces(message);
        Matcher angleMatcher = ANGLE_CHAT_PATTERN.matcher(trimmed);
        if (angleMatcher.matches()) {
            String sender = normalizeSpaces(angleMatcher.group(1));
            String content = normalizeSpaces(angleMatcher.group(2));
            if (isValidSender(sender)) {
                return new SenderParseResult(sender, content);
            }
        }

        Matcher standardMatcher = STANDARD_CHAT_PATTERN.matcher(trimmed);
        if (standardMatcher.matches()) {
            String sender = normalizeSpaces(standardMatcher.group(1));
            String content = normalizeSpaces(standardMatcher.group(2));
            if (isValidSender(sender)) {
                return new SenderParseResult(sender, content);
            }
        }

        for (String delimiter : CHAT_DELIMITERS) {
            int index = trimmed.indexOf(delimiter);
            if (index <= 0) {
                continue;
            }
            String left = trimmed.substring(0, index).trim();
            String right = normalizeSpaces(trimmed.substring(index + delimiter.length()));
            if (right.isEmpty()) {
                continue;
            }
            String sender = extractNicknameFromLeft(left);
            if (isValidSender(sender)) {
                return new SenderParseResult(sender, right);
            }
        }
        return new SenderParseResult(null, trimmed);
    }

    private boolean isValidSender(String sender) {
        if (sender == null || sender.isBlank()) {
            return false;
        }
        String lower = sender.toLowerCase(Locale.ROOT);
        return PLAYER_NAME_PATTERN.matcher(sender).matches() && !SYSTEM_NAMES.contains(lower);
    }

    private String extractNicknameFromLeft(String leftPart) {
        if (leftPart == null || leftPart.isBlank()) {
            return null;
        }
        String work = normalizeSpaces(leftPart);
        while (work.startsWith("[")) {
            int bracketEnd = work.indexOf(']');
            if (bracketEnd < 0) {
                break;
            }
            work = normalizeSpaces(work.substring(bracketEnd + 1));
        }
        Matcher matcher = TRAILING_NICK_PATTERN.matcher(work);
        if (!matcher.find()) {
            return null;
        }
        return normalizeSpaces(matcher.group(1));
    }

    private String resolveSelfName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            return mc.player.getName().getString();
        }
        return "You";
    }

    private String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return SPACE_PATTERN.matcher(value.replace('\u00A0', ' ')).replaceAll(" ").trim();
    }

    private List<String> readAllLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
            stream.forEach(lines::add);
        }
        return lines;
    }

    private Path getLogFilePath() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Path basePath;
        if (mc != null && mc.runDirectory != null) {
            basePath = mc.runDirectory.toPath();
        } else {
            basePath = Path.of(System.getProperty("user.dir"));
        }
        return basePath.resolve("Royale").resolve("chat").resolve("chat.log");
    }

    private void ensureFileExists(Path file) throws IOException {
        if (file == null) {
            return;
        }
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    public static final class HistoryEntry {
        private final long timeMillis;
        private final String timestamp;
        private final String type;
        private final String sender;
        private final String content;
        private final String rawLine;

        public HistoryEntry(long timeMillis, String timestamp, String type, String sender, String content, String rawLine) {
            this.timeMillis = timeMillis;
            this.timestamp = timestamp;
            this.type = type;
            this.sender = sender;
            this.content = content;
            this.rawLine = rawLine;
        }

        public long getTimeMillis() {
            return this.timeMillis;
        }

        public String getTimestamp() {
            return this.timestamp;
        }

        public String getType() {
            return this.type;
        }

        public String getSender() {
            return this.sender;
        }

        public String getContent() {
            return this.content;
        }

        public String getRawLine() {
            return this.rawLine;
        }
    }

    private static final class SenderParseResult {
        private final String sender;
        private final String content;

        private SenderParseResult(String sender, String content) {
            this.sender = sender;
            this.content = content == null ? "" : content;
        }
    }
}
