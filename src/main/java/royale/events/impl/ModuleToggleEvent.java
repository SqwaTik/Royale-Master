package royale.events.impl;

import royale.events.api.events.Event;
import royale.modules.module.ModuleStructure;

public class ModuleToggleEvent
implements Event {
    private final ModuleStructure module;
    private final boolean enabled;

    public ModuleStructure getModule() {
        return this.module;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public ModuleToggleEvent(ModuleStructure module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }
}

