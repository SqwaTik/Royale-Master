package royale.modules.impl.render.particles;

import net.minecraft.entity.Entity;

public class TotemEmitter {
    private final Entity entity;
    private final int maxAge;
    private int age;

    public TotemEmitter(Entity entity, int maxAge) {
        this.entity = entity;
        this.maxAge = maxAge;
        this.age = 0;
    }

    public void tick() {
        ++this.age;
    }

    public boolean isAlive() {
        return this.age < this.maxAge && this.entity != null && !this.entity.isRemoved();
    }

    public float getProgress() {
        return (float)this.age / (float)this.maxAge;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public int getAge() {
        return this.age;
    }
}

