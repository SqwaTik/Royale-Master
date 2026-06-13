package royale.util.session;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

public final class SessionChanger {
    private static volatile Consumer<Session> sessionSetter;

    private SessionChanger() {
    }

    public static void setSessionSetter(Consumer<Session> setter) {
        sessionSetter = setter;
    }

    public static void changeUsername(String newUsername) {
        Consumer<Session> setter = sessionSetter;
        if (setter == null || newUsername == null || newUsername.isBlank()) {
            return;
        }

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newUsername).getBytes(StandardCharsets.UTF_8));
        Session newSession = new Session(newUsername, uuid, "", Optional.empty(), Optional.empty());
        setter.accept(newSession);
    }

    public static String getCurrentUsername() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getSession() != null) {
            return mc.getSession().getUsername();
        }
        return "";
    }
}
