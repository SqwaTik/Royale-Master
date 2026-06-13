package royale.util.network;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import org.apache.commons.lang3.StringUtils;
import royale.IMinecraft;
import royale.events.impl.PacketEvent;
import royale.util.string.PlayerInteractionHelper;
import royale.util.timer.StopWatch;

import java.util.Locale;

public final class Network implements IMinecraft {
    private static final StopWatch PVP_WATCH = new StopWatch();
    private static final String SERVER_NONE = "";

    public static String server = SERVER_NONE;
    public static long timestamp;
    public static int anarchy;
    public static float TPS = 20.0F;
    public static boolean pvpEnd;

    private Network() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int getAnarchy() {
        return anarchy;
    }

    public static boolean isPvpEnd() {
        return pvpEnd;
    }

    public static void tick() {
        anarchy = getAnarchyMode();
        server = getServer();
        pvpEnd = inPvpEnd();
        if (inPvp()) {
            PVP_WATCH.reset();
        }
    }

    public static void packet(PacketEvent event) {
        if (!(event.getPacket() instanceof WorldTimeUpdateS2CPacket)) {
            return;
        }

        long nanoTime = System.nanoTime();
        float maxTps = 20.0F;
        if (timestamp > 0L) {
            float rawTps = maxTps * (1.0E9F / Math.max(1.0F, (float) (nanoTime - timestamp)));
            TPS = net.minecraft.util.math.MathHelper.clamp(rawTps, 0.0F, maxTps);
        }
        timestamp = nanoTime;
    }

    public static String getServer() {
        if (PlayerInteractionHelper.nullCheck()
                || mc.getNetworkHandler() == null
                || mc.getNetworkHandler().getServerInfo() == null
                || mc.getNetworkHandler().getBrand() == null) {
            return SERVER_NONE;
        }

        String serverIp = safe(mc.getNetworkHandler().getServerInfo().address).toLowerCase(Locale.ROOT);
        String brand = safe(mc.getNetworkHandler().getBrand()).toLowerCase(Locale.ROOT);

        if (brand.contains("botfilter")) return "FunTime";
        if (brand.contains("spookycore")) return "SpookyTime";
        if (serverIp.contains("funtime") || serverIp.contains("skytime") || serverIp.contains("space-times") || serverIp.contains("funsky")) return "CopyTime";
        if (brand.contains("holyworld") || brand.contains("vk.com/idwok")) return "HolyWorld";
        if (serverIp.contains("reallyworld")) return "ReallyWorld";
        if (serverIp.contains("gulpvp")) return "GulPvP";
        if (serverIp.contains("aresmine")) return "AresMine";

        return SERVER_NONE;
    }

    private static int getAnarchyMode() {
        if (mc.world == null) {
            return -1;
        }

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return -1;
        }

        switch (server) {
            case "FunTime" -> {
                String[] split = objective.getDisplayName().getString().split("-");
                if (split.length > 1) {
                    try {
                        return Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                        return -1;
                    }
                }
            }
            case "HolyWorld" -> {
                for (ScoreboardEntry scoreboardEntry : scoreboard.getScoreboardEntries(objective)) {
                    String text = Team.decorateName((AbstractTeam) scoreboard.getScoreHolderTeam(scoreboardEntry.owner()), scoreboardEntry.name()).getString();
                    if (text.isEmpty()) {
                        continue;
                    }

                    String value = StringUtils.substringBetween(text, "#", " -в—†-");
                    if (value == null || value.isEmpty()) {
                        continue;
                    }

                    try {
                        return Integer.parseInt(value.replace(" (1.20)", ""));
                    } catch (NumberFormatException ignored) {
                        return -1;
                    }
                }
            }
            default -> {
                return -1;
            }
        }

        return -1;
    }

    public static boolean isPvp() {
        return !PVP_WATCH.finished(500.0D);
    }

    private static boolean inPvp() {
        if (mc.inGameHud == null || mc.inGameHud.getBossBarHud() == null) {
            return false;
        }

        return mc.inGameHud.getBossBarHud().bossBars.values().stream()
                .map(ClientBossBar::getName)
                .map(text -> safe(text.getString()).toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains("pvp") || value.contains("пвп"));
    }

    private static boolean inPvpEnd() {
        if (mc.inGameHud == null || mc.inGameHud.getBossBarHud() == null) {
            return false;
        }

        return mc.inGameHud.getBossBarHud().bossBars.values().stream()
                .map(ClientBossBar::getName)
                .map(text -> safe(text.getString()).toLowerCase(Locale.ROOT))
                .anyMatch(value -> (value.contains("pvp") || value.contains("пвп")) && (value.contains("0") || value.contains("1")));
    }

    public static String getWorldType() {
        return mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "";
    }

    public static boolean isCopyTime() {
        return "CopyTime".equals(server) || "SpookyTime".equals(server) || "FunTime".equals(server);
    }

    public static boolean isFunTime() {
        return "FunTime".equals(server);
    }

    public static boolean isReallyWorld() {
        return "ReallyWorld".equals(server);
    }

    public static boolean isGulPvP() {
        return "GulPvP".equals(server);
    }

    public static boolean isHolyWorld() {
        return "HolyWorld".equals(server);
    }

    public static boolean isSpookyTime() {
        return "SpookyTime".equals(server);
    }

    public static boolean isAresMine() {
        return "AresMine".equals(server) || "aresmine".equalsIgnoreCase(server);
    }

    public static boolean isVanilla() {
        return server == null || server.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
