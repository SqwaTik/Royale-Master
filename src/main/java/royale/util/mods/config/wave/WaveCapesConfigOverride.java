package royale.util.mods.config.wave;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class WaveCapesConfigOverride implements PreLaunchEntrypoint {
    private static final String CONFIG_CONTENT = """
            {
              "configVersion": 2,
              "windMode": "WAVES",
              "capeStyle": "SMOOTH",
              "capeMovement": "BASIC_SIMULATION_3D",
              "gravity": 15,
              "heightMultiplier": 5,
              "straveMultiplier": 5
            }
            """;

    @Override
    public void onPreLaunch() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path waveCapesConfig = configDir.resolve("waveycapes.json");

        try {
            Files.createDirectories(configDir);
            Files.writeString(waveCapesConfig, CONFIG_CONTENT);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
