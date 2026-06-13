package royale.util.network;

import net.minecraft.client.network.ServerInfo;

public final class ConnectionCompatController {
    private ConnectionCompatController() {
    }

    public static void prepareConnection(ServerInfo serverInfo) {
        if (!ViaFabricPlusBridge.isAvailable()) {
            return;
        }

        if (ViaFabricPlusBridge.setTargetVersionForServer(serverInfo, true)) {
            return;
        }

        ViaFabricPlusBridge.enableAutoDetectTargetVersion(true);
    }

    public static void resetAfterDisconnect() {
        ViaFabricPlusBridge.resetTargetVersion();
    }
}
