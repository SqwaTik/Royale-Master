package royale.modules.module;

import royale.modules.impl.combat.AutoEat;
import royale.modules.impl.combat.HitSound;
import royale.modules.impl.combat.ShiftAnim;
import royale.modules.impl.combat.TapeMouse;
import royale.modules.impl.misc.Brand;
import royale.modules.impl.misc.Client;
import royale.modules.impl.misc.ClientSounds;
import royale.modules.impl.misc.ChatHelper;
import royale.modules.impl.misc.Debug;
import royale.modules.impl.misc.GifManager;
import royale.modules.impl.misc.Rpc;
import royale.modules.impl.misc.ServerRP;
import royale.modules.impl.misc.ServerRPSpoofer;
import royale.modules.impl.mods.ExternalJarModule;
import royale.modules.impl.movement.AutoSprint;
import royale.modules.impl.player.AutoRespawn;
import royale.modules.impl.player.FreeLook;
import royale.modules.impl.player.ItemScroller;
import royale.modules.impl.player.NameProtect;
import royale.modules.impl.player.Zoom;
import royale.modules.impl.render.Arrows;
import royale.modules.impl.render.BlockOverlay;
import royale.modules.impl.render.ChinaHat;
import royale.modules.impl.render.ChunkAnimator;
import royale.modules.impl.render.CropTimer;
import royale.modules.impl.render.CustomBar;
import royale.modules.impl.render.Esp;
import royale.modules.impl.render.FullBright;
import royale.modules.impl.render.GlassHands;
import royale.modules.impl.render.HitEffect;
import royale.modules.impl.render.Hud;
import royale.modules.impl.render.ItemPhysic;
import royale.modules.impl.render.JumpCircle;
import royale.modules.impl.render.NoRender;
import royale.modules.impl.render.Particles;
import royale.modules.impl.render.SwingAnimation;
import royale.modules.impl.render.TargetESP;
import royale.modules.impl.render.Trajectories;
import royale.modules.impl.render.ViewModel;
import royale.modules.impl.render.WorldParticles;
import royale.util.mods.ModsRuntimeManager;
import royale.util.mods.ModsRuntimeManager.ScannedMod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ModuleRepository {
    private final List<ModuleStructure> moduleStructures = new ArrayList<>();
    private final List<ModuleStructure> hiddenModules = new ArrayList<>();
    private final Set<String> registeredKeys = new HashSet<>();

    public void setup() {
        Rpc rpc = new Rpc();

        builder()
                .add(new Hud())
                .add(new CropTimer())
                .add(new Trajectories())
                .add(new CustomBar())
                .add(new HitEffect())
                .add(new Esp())
                .add(new WorldParticles())
                .add(new Arrows())
                .add(new Particles())
                .add(new GlassHands())
                .add(new ChunkAnimator())
                .add(new TapeMouse())
                .add(new AutoEat())
                .add(new ChinaHat())
                .add(new ClientSounds())
                .add(new Client())
                .add(new Brand())
                .add(new Debug())
                .add(new GifManager())
                .add(new ChatHelper())
                .add(new TargetESP())
                .add(new BlockOverlay())
                .add(new HitSound())
                .add(new JumpCircle())
                .add(new ItemScroller())
                .add(new FreeLook())
                .add(new Zoom())
                .add(new FullBright())
                .add(new ItemPhysic())
                .add(new ServerRP())
                .add(new ServerRPSpoofer())
                .add(rpc)
                .add(new NoRender())
                .add(new ShiftAnim())
                .add(new NameProtect())
                .add(new ViewModel())
                .add(new AutoRespawn())
                .add(new SwingAnimation())
                .add(new AutoSprint());

        reloadExternalMods();
    }

    public synchronized void reloadExternalMods() {
        this.moduleStructures.removeIf(module -> module instanceof ExternalJarModule);
        this.registeredKeys.removeIf(key -> key.startsWith(ExternalJarModule.class.getName() + "#"));

        ModsRuntimeManager.ScanResult result = ModsRuntimeManager.getInstance().scanNow();
        for (ScannedMod scannedMod : result.getMods()) {
            if ("copyright".equalsIgnoreCase(scannedMod.getId())) {
                continue;
            }

            try {
                registerModule(new ExternalJarModule(scannedMod), false);
            } catch (Exception ignored) {
            }
        }
    }

    public ModuleBuilder builder() {
        return new ModuleBuilder(this);
    }

    void registerModule(ModuleStructure module, boolean hidden) {
        String key = buildKey(module);
        if (this.registeredKeys.contains(key)) {
            throw new DuplicateModuleException(module.getName());
        }

        this.registeredKeys.add(key);
        if (hidden) {
            this.hiddenModules.add(module);
            module.setState(true);
        } else {
            this.moduleStructures.add(module);
        }
    }

    private String buildKey(ModuleStructure module) {
        if (module instanceof ExternalJarModule externalJarModule) {
            return module.getClass().getName() + "#" + externalJarModule.getLogicalFileName().toLowerCase(Locale.ROOT);
        }

        return module.getClass().getName() + "#" + module.getStorageName().toLowerCase(Locale.ROOT);
    }

    public List<ModuleStructure> modules() {
        return this.moduleStructures;
    }

    public List<ModuleStructure> hiddenModules() {
        return this.hiddenModules;
    }

    public List<ModuleStructure> allModules() {
        List<ModuleStructure> all = new ArrayList<>(this.moduleStructures);
        all.addAll(this.hiddenModules);
        return all;
    }
}


