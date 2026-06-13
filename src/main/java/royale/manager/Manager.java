package royale.manager;

import royale.client.draggables.HudManager;
import royale.command.CommandManager;
import royale.events.api.EventManager;
import royale.modules.module.ModuleRepository;
import royale.screens.clickgui.ClickGui;
import royale.util.config.ConfigSystem;
import royale.util.config.impl.bind.BindConfig;
import royale.util.config.impl.drag.DragConfig;
import royale.util.config.impl.friend.FriendConfig;
import royale.util.config.impl.prefix.PrefixConfig;
import royale.util.config.impl.proxy.ProxyConfig;
import royale.util.config.impl.staff.StaffConfig;
import royale.util.modules.ModuleProvider;
import royale.util.modules.ModuleSwitcher;
import royale.util.launcher.LauncherStatsService;
import royale.util.render.font.FontInitializer;
import royale.util.render.shader.RenderCore;
import royale.util.render.shader.Scissor;
import royale.util.repository.macro.MacroRepository;
import royale.util.repository.way.WayRepository;
import royale.util.tps.TPSCalculate;

public class Manager {
    private EventManager eventManager;
    private RenderCore renderCore;
    private Scissor scissor;
    private ModuleProvider moduleProvider;
    private ModuleRepository moduleRepository;
    private ModuleSwitcher moduleSwitcher;
    private ClickGui clickgui;
    private ConfigSystem configSystem;
    private CommandManager commandManager;
    private TPSCalculate tpsCalculate;
    private HudManager hudManager = new HudManager();

    public void init() {
        MacroRepository.getInstance().init();
        WayRepository.getInstance().init();
        FriendConfig.getInstance().load();
        PrefixConfig.getInstance().load();
        StaffConfig.getInstance().load();
        ProxyConfig.getInstance().load();
        DragConfig.getInstance().load();
        BindConfig.getInstance();
        FontInitializer.register();
        LauncherStatsService.getInstance().register();
        this.tpsCalculate = new TPSCalculate();
        this.clickgui = new ClickGui();
        this.eventManager = new EventManager();
        this.renderCore = new RenderCore();
        this.scissor = new Scissor();
        this.hudManager = new HudManager();
        this.hudManager.initElements();
        this.moduleRepository = new ModuleRepository();
        this.moduleRepository.setup();
        this.moduleProvider = new ModuleProvider(this.moduleRepository.modules());
        this.moduleSwitcher = new ModuleSwitcher(this.moduleRepository.modules(), this.eventManager);
        this.configSystem = new ConfigSystem();
        this.configSystem.init();
        this.commandManager = new CommandManager();
        this.commandManager.init();
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public RenderCore getRenderCore() {
        return this.renderCore;
    }

    public Scissor getScissor() {
        return this.scissor;
    }

    public ModuleProvider getModuleProvider() {
        return this.moduleProvider;
    }

    public ModuleRepository getModuleRepository() {
        return this.moduleRepository;
    }

    public ModuleSwitcher getModuleSwitcher() {
        return this.moduleSwitcher;
    }

    public ClickGui getClickgui() {
        return this.clickgui;
    }

    public ConfigSystem getConfigSystem() {
        return this.configSystem;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public TPSCalculate getTpsCalculate() {
        return this.tpsCalculate;
    }

    public HudManager getHudManager() {
        return this.hudManager;
    }
}
