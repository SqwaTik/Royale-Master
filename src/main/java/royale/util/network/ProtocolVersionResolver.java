package royale.util.network;

import net.minecraft.SharedConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ProtocolVersionResolver {
    private static final Map<String, Integer> RELEASE_PROTOCOLS = new HashMap<>();
    private static final Map<String, String> CANONICAL_RELEASES = new HashMap<>();

    static {
        add(4, "1.7.2", "1.7.3", "1.7.4", "1.7.5");
        add(5, "1.7.6", "1.7.7", "1.7.8", "1.7.9", "1.7.10");
        add(47, "1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9");
        add(107, "1.9");
        add(108, "1.9.1");
        add(109, "1.9.2");
        add(110, "1.9.3", "1.9.4");
        add(210, "1.10", "1.10.1", "1.10.2");
        add(315, "1.11");
        add(316, "1.11.1", "1.11.2");
        add(335, "1.12");
        add(338, "1.12.1");
        add(340, "1.12.2");
        add(393, "1.13");
        add(401, "1.13.1");
        add(404, "1.13.2");
        add(477, "1.14");
        add(480, "1.14.1");
        add(485, "1.14.2");
        add(490, "1.14.3");
        add(498, "1.14.4");
        add(573, "1.15");
        add(575, "1.15.1");
        add(578, "1.15.2");
        add(735, "1.16");
        add(736, "1.16.1");
        add(751, "1.16.2");
        add(753, "1.16.3");
        add(754, "1.16.4", "1.16.5");
        add(755, "1.17");
        add(756, "1.17.1");
        add(757, "1.18", "1.18.1");
        add(758, "1.18.2");
        add(759, "1.19");
        add(760, "1.19.1", "1.19.2");
        add(761, "1.19.3");
        add(762, "1.19.4");
        add(763, "1.20", "1.20.1");
        add(764, "1.20.2");
        add(765, "1.20.3", "1.20.4");
        add(766, "1.20.5", "1.20.6");
        add(767, "1.21", "1.21.1");
        add(768, "1.21.2", "1.21.3");
        add(769, "1.21.4");
        add(SharedConstants.getProtocolVersion(), SharedConstants.getGameVersion().name());
    }

    private ProtocolVersionResolver() {
    }

    public static int resolve(String versionText) {
        String normalized = normalize(versionText);
        Integer protocol = RELEASE_PROTOCOLS.get(normalized);
        if (protocol != null) {
            return protocol;
        }

        return SharedConstants.getProtocolVersion();
    }

    public static boolean isSupportedVersion(String versionText) {
        return CANONICAL_RELEASES.containsKey(normalize(versionText));
    }

    public static String canonicalizeVersion(String versionText) {
        String normalized = normalize(versionText);
        String canonical = CANONICAL_RELEASES.get(normalized);
        if (canonical != null) {
            return canonical;
        }

        return normalized;
    }

    public static String getVersionPlaceholder() {
        return "1.8.9 / 1.16.5 / " + SharedConstants.getGameVersion().name();
    }

    private static void add(int protocol, String... versions) {
        if (versions.length == 0) {
            return;
        }

        String canonical = versions[versions.length - 1];
        for (String version : versions) {
            String normalized = normalize(version);
            RELEASE_PROTOCOLS.put(normalized, protocol);
            CANONICAL_RELEASES.put(normalized, canonical);
        }
    }

    private static String normalize(String versionText) {
        if (versionText == null) {
            return "";
        }

        String normalized = versionText
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(',', '.')
                .replace('_', '.');

        if (normalized.startsWith("minecraft")) {
            normalized = normalized.substring("minecraft".length()).trim();
        }
        if (normalized.startsWith("version")) {
            normalized = normalized.substring("version".length()).trim();
        }
        if (normalized.startsWith("v")) {
            normalized = normalized.substring(1).trim();
        }
        while (normalized.endsWith(".0")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        return normalized;
    }
}
