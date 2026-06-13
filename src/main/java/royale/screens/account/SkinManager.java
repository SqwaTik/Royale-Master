package royale.screens.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;

public class SkinManager {
    private static final Map<String, Identifier> SKIN_CACHE = new ConcurrentHashMap<String, Identifier>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<String, Boolean>();
    private static final Identifier STEVE_SKIN = Identifier.of((String)"minecraft", (String)"textures/entity/player/wide/steve.png");
    private static final Identifier ALEX_SKIN = Identifier.of((String)"minecraft", (String)"textures/entity/player/wide/alex.png");
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "SkinLoader");
        t.setDaemon(true);
        return t;
    });

    public static Identifier getSkin(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return STEVE_SKIN;
        }
        String key = playerName.toLowerCase();
        Identifier cached = SKIN_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (!LOADING.containsKey(key)) {
            LOADING.put(key, true);
            SkinManager.loadSkinAsync(playerName);
        }
        return SkinManager.getDefaultSkin(playerName);
    }

    private static Identifier getDefaultSkin(String playerName) {
        UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
        return (offlineUUID.hashCode() & 1) == 0 ? STEVE_SKIN : ALEX_SKIN;
    }

    private static void loadSkinAsync(String playerName) {
        String key = playerName.toLowerCase();
        CompletableFuture.runAsync(() -> {
            try {
                Identifier skinId;
                SkinTextures textures;
                UUID uuid = SkinManager.fetchUUID(playerName);
                if (uuid == null) {
                    LOADING.remove(key);
                    return;
                }
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null) {
                    LOADING.remove(key);
                    return;
                }
                GameProfile profile = new GameProfile(uuid, playerName);
                PlayerSkinProvider provider = client.getSkinProvider();
                CompletableFuture skinFuture = provider.fetchSkinTextures(profile);
                Optional texturesOpt = (Optional)skinFuture.join();
                if (texturesOpt.isPresent() && (textures = (SkinTextures)texturesOpt.get()).body() != null && (skinId = textures.body().texturePath()) != null) {
                    SKIN_CACHE.put(key, skinId);
                }
            }
            catch (Exception exception) {
            }
            finally {
                LOADING.remove(key);
            }
        }, EXECUTOR);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static UUID fetchUUID(String playerName) {
        try {
            URI uri = URI.create("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                connection.disconnect();
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream());){
                JsonObject json = new JsonParser().parse((Reader) reader).getAsJsonObject();
                if (json.has("id")) {
                    String id = json.get("id").getAsString();
                    connection.disconnect();
                    UUID uUID = SkinManager.parseUUID(id);
                    return uUID;
                }
            }
            connection.disconnect();
            return null;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static UUID parseUUID(String id) {
        try {
            if (((String)id).length() == 32) {
                id = ((String)id).substring(0, 8) + "-" + ((String)id).substring(8, 12) + "-" + ((String)id).substring(12, 16) + "-" + ((String)id).substring(16, 20) + "-" + ((String)id).substring(20);
            }
            return UUID.fromString((String)id);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static void clearCache() {
        SKIN_CACHE.clear();
        LOADING.clear();
    }

    public static void removeSkin(String playerName) {
        if (playerName != null) {
            SKIN_CACHE.remove(playerName.toLowerCase());
            LOADING.remove(playerName.toLowerCase());
        }
    }

    public static void reloadSkin(String playerName) {
        SkinManager.removeSkin(playerName);
        SkinManager.getSkin(playerName);
    }
}

