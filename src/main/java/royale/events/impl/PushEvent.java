package royale.events.impl;

import royale.events.api.events.callables.EventCancellable;

public class PushEvent
extends EventCancellable {
    private Type type;

    public Type getType() {
        return this.type;
    }

    public PushEvent(Type type) {
        this.type = type;
    }

    public static enum Type {
        COLLISION,
        BLOCK,
        WATER;

    }
}

