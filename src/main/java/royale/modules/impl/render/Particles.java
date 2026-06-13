package royale.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import royale.events.api.EventHandler;
import royale.events.impl.AttackEvent;
import royale.events.impl.TickEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.impl.render.particles.Particle3D;
import royale.modules.impl.render.particles.TotemEmitter;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.MultiSelectSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.performance.BuiltinOptimizer;

public class Particles extends ModuleStructure {
    public static final String MODE_CUBES = "Кубы";
    public static final String MODE_CROWN = "Корона";
    public static final String MODE_CUBE_BLAST = "Куб-взрыв";
    public static final String MODE_DOLLAR = "Доллар";
    public static final String MODE_HEART = "Сердце";
    public static final String MODE_LIGHTNING = "Молния";
    public static final String MODE_LINE = "Линия";
    public static final String MODE_RHOMBUS = "Ромб";
    public static final String MODE_SNOWFLAKE = "Снежинка";
    public static final String MODE_STAR = "Звезда";
    public static final String MODE_STAR_ALT = "Звезда 2";
    public static final String MODE_TRIANGLE = "Треугольник";
    public static final String MODE_RANDOM = "Рандом";

    public static final String GLOW_BLOOM = "Bloom";
    public static final String GLOW_BLOOM_SAMPLE = "Bloom Sample";
    public static final String GLOW_BOTH = "Оба";

    public static final String TRIGGER_ATTACK = "Атака";
    public static final String TRIGGER_TOTEM = "Тотем";
    public static final String TRIGGER_WALK = "Ходьба";
    public static final String TRIGGER_PROJECTILES = "Летающие предметы";

    private static final float GLOW_SIZE = 7.5F;
    private static final int TOTEM_DURATION = 20;
    private static final int MAX_PARTICLES = 700;
    private static final int PROJECTILE_SCAN_INTERVAL_TICKS = 2;
    private static final int MAX_PROJECTILES_PER_SCAN = 24;
    private static final double PROJECTILE_SCAN_RADIUS = 42.0D;
    private static final double RENDER_DISTANCE_SQ = 6400.0D;
    private static final double HARD_CULL_DISTANCE_SQ = 14400.0D;
    private static final int[] RANDOM_COLORS = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};

    private final List<Particle3D> particles = new ArrayList<>();
    private final List<TotemEmitter> totemEmitters = new ArrayList<>();

    public SelectSetting mode = (new SelectSetting("Режим", "Визуальный стиль частиц"))
            .value(new String[]{
                    MODE_CUBES,
                    MODE_CROWN,
                    MODE_CUBE_BLAST,
                    MODE_DOLLAR,
                    MODE_HEART,
                    MODE_LIGHTNING,
                    MODE_LINE,
                    MODE_RHOMBUS,
                    MODE_SNOWFLAKE,
                    MODE_STAR,
                    MODE_STAR_ALT,
                    MODE_TRIANGLE,
                    MODE_RANDOM
            })
            .selected(MODE_STAR);

    public SelectSetting glowMode = (new SelectSetting("Свечение", "Тип свечения у частицы"))
            .value(new String[]{GLOW_BLOOM, GLOW_BLOOM_SAMPLE, GLOW_BOTH})
            .selected(GLOW_BLOOM_SAMPLE);

    public MultiSelectSetting triggers = (new MultiSelectSetting("Триггеры", "События, при которых создаются частицы"))
            .value(new String[]{TRIGGER_ATTACK, TRIGGER_TOTEM, TRIGGER_WALK, TRIGGER_PROJECTILES})
            .selected(new String[]{TRIGGER_ATTACK, TRIGGER_TOTEM, TRIGGER_WALK, TRIGGER_PROJECTILES});

    public SliderSettings amount = (new SliderSettings("Количество", "Количество частиц при атаке"))
            .range(10, 40).setValue(40.0F);

    public SliderSettings walkAmount = (new SliderSettings("Кол-во при ходьбе", "Количество частиц при обычном движении"))
            .range(10, 30).setValue(30.0F)
            .visible(() -> Boolean.valueOf(this.triggers.isSelected(TRIGGER_WALK)));

    public SliderSettings spread = (new SliderSettings("Разброс", "Сила случайного разброса частиц"))
            .range(0.5F, 3.0F).setValue(1.0F);

    public SliderSettings speed = (new SliderSettings("Скорость", "Скорость движения частиц"))
            .range(0.1F, 3.0F).setValue(2.0F);

    public SliderSettings lifeTime = (new SliderSettings("Время жизни", "Сколько секунд живет частица"))
            .range(0.5F, 10.0F).setValue(2.5F);

    public SliderSettings size = (new SliderSettings("Размер", "Размер частицы"))
            .range(0.1F, 1.0F).setValue(1.0F);

    public BooleanSetting randomColor = (new BooleanSetting("Случайный цвет", "Каждая частица получает случайный цвет"))
            .setValue(false);

    public ColorSetting color = (new ColorSetting("Цвет", "Основной цвет частиц"))
            .value(-7773880)
            .visible(() -> Boolean.valueOf(!this.randomColor.isValue()));

    private float walkParticleAccumulator = 0.0F;
    private int projectileScanTick = 0;

    public Particles() {
        super("Particles", "Создает декоративные частицы при событиях игрока", ModuleCategory.RENDER);
        settings(new Setting[]{
                this.mode,
                this.glowMode,
                this.triggers,
                this.amount,
                this.walkAmount,
                this.spread,
                this.speed,
                this.lifeTime,
                this.size,
                this.randomColor,
                this.color
        });
    }

    public static Particles getInstance() {
        return (Particles) Instance.get(Particles.class);
    }

    public void deactivate() {
        this.particles.clear();
        this.totemEmitters.clear();
        this.walkParticleAccumulator = 0.0F;
        this.projectileScanTick = 0;
    }

    private int getParticleColor() {
        if (this.randomColor.isValue()) {
            return RANDOM_COLORS[ThreadLocalRandom.current().nextInt(RANDOM_COLORS.length)];
        }
        return this.color.getColor();
    }

    private float getGravity() {
        return 0.0040000007F;
    }

    private float getSpeedMultiplier() {
        return this.speed.getValue();
    }

    private Particle3D.ParticleMode getParticleMode() {
        return switch (this.mode.getSelected()) {
            case MODE_CROWN -> Particle3D.ParticleMode.CROWN;
            case MODE_CUBE_BLAST -> Particle3D.ParticleMode.CUBE_BLAST;
            case MODE_DOLLAR -> Particle3D.ParticleMode.DOLLAR;
            case MODE_HEART -> Particle3D.ParticleMode.HEART;
            case MODE_LIGHTNING -> Particle3D.ParticleMode.LIGHTNING;
            case MODE_LINE -> Particle3D.ParticleMode.LINE;
            case MODE_RHOMBUS -> Particle3D.ParticleMode.RHOMBUS;
            case MODE_SNOWFLAKE -> Particle3D.ParticleMode.SNOWFLAKE;
            case MODE_STAR -> Particle3D.ParticleMode.STAR;
            case MODE_STAR_ALT -> Particle3D.ParticleMode.STAR_ALT;
            case MODE_TRIANGLE -> Particle3D.ParticleMode.TRIANGLE;
            case MODE_RANDOM -> Particle3D.ParticleMode.RANDOM;
            case MODE_CUBES -> Particle3D.ParticleMode.CUBES;
            default -> Particle3D.ParticleMode.CUBES;
        };
    }

    private Particle3D.GlowMode getGlowMode() {
        return switch (this.glowMode.getSelected()) {
            case GLOW_BLOOM -> Particle3D.GlowMode.BLOOM;
            case GLOW_BLOOM_SAMPLE -> Particle3D.GlowMode.BLOOM_SAMPLE;
            case GLOW_BOTH -> Particle3D.GlowMode.BOTH;
            default -> Particle3D.GlowMode.BOTH;
        };
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (BuiltinOptimizer.shouldSuspendDecorativeParticles()) {
            this.totemEmitters.clear();
            int keep = Math.min(48, this.particles.size());
            if (this.particles.size() > keep) {
                this.particles.subList(0, this.particles.size() - keep).clear();
            }
            return;
        }

        if (isTriggerSelected(TRIGGER_WALK)) {
            handleWalkParticles();
        }

        if (isTriggerSelected(TRIGGER_PROJECTILES)) {
            if (this.projectileScanTick++ % PROJECTILE_SCAN_INTERVAL_TICKS == 0) {
                handleProjectileParticles();
            }
        } else {
            this.projectileScanTick = 0;
        }

        Iterator<TotemEmitter> emitterIterator = this.totemEmitters.iterator();
        while (emitterIterator.hasNext()) {
            TotemEmitter emitter = emitterIterator.next();
            emitter.tick();
            if (emitter.isAlive()) {
                spawnTotemParticlesBurst(emitter.getEntity(), emitter.getProgress());
                continue;
            }
            emitterIterator.remove();
        }

        Vec3d playerPos = mc.player.getEntityPos();
        Iterator<Particle3D> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            Particle3D particle = iterator.next();
            if (particle.distanceSquaredTo(playerPos) > HARD_CULL_DISTANCE_SQ) {
                iterator.remove();
                continue;
            }
            particle.update();
            if (particle.isDead()) {
                iterator.remove();
            }
        }

        trimParticleList();
    }

    private boolean isTriggerSelected(String value) {
        return this.triggers.isSelected(value);
    }

    private void handleWalkParticles() {
        if (mc.player == null) {
            return;
        }

        double velocitySq = mc.player.getVelocity().lengthSquared();
        boolean isMoving = velocitySq > 1.0E-4D && !mc.player.isSneaking();
        if (!isMoving) {
            this.walkParticleAccumulator = 0.0F;
            return;
        }

        float particlesPerSecond = this.walkAmount.getValue();
        float particlesPerTick = particlesPerSecond / 20.0F;
        this.walkParticleAccumulator += particlesPerTick;

        int particlesToSpawn = (int) this.walkParticleAccumulator;
        this.walkParticleAccumulator -= particlesToSpawn;

        particlesToSpawn = Math.min(particlesToSpawn, 5);
        particlesToSpawn = BuiltinOptimizer.getDecorativeSpawnBudget(particlesToSpawn);
        if (particlesToSpawn <= 0) {
            return;
        }

        float yaw = mc.player.getYaw();
        double radian = Math.toRadians(yaw + 90.0F);
        double offsetX = Math.cos(radian) * 0.5D;
        double offsetZ = Math.sin(radian) * 0.5D;

        float spreadValue = this.spread.getValue() * 0.05F;
        float speedMult = getSpeedMultiplier();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < particlesToSpawn && this.particles.size() < getParticleCap(); i++) {
            double px = mc.player.getX() - offsetX + (random.nextDouble() - 0.5D) * 0.3D;
            double py = mc.player.getY() + 0.3D + random.nextDouble() * (mc.player.getHeight() - 0.3D);
            double pz = mc.player.getZ() - offsetZ + (random.nextDouble() - 0.5D) * 0.3D;

            Vec3d pos = new Vec3d(px, py, pz);
            Vec3d velocity = new Vec3d(
                    (random.nextDouble() - 0.5D) * spreadValue * speedMult,
                    (random.nextDouble() - 0.5D) * spreadValue * 0.5D * speedMult,
                    (random.nextDouble() - 0.5D) * spreadValue * speedMult
            );

            this.particles.add(
                    new Particle3D(pos, velocity, getParticleColor(), this.size.getValue() * 0.6F, this.lifeTime.getValue() * 0.5F)
                            .setGravity(getGravity())
                            .setVelocityMultiplier(0.99F)
                            .setMode(getParticleMode())
                            .setGlowMode(getGlowMode())
            );
        }
    }

    private void handleProjectileParticles() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        float spreadValue = this.spread.getValue() * 0.03F;
        float speedMult = getSpeedMultiplier();

        Box scanBox = mc.player.getBoundingBox().expand(PROJECTILE_SCAN_RADIUS);
        List<ProjectileEntity> projectiles = mc.world.getEntitiesByClass(
                ProjectileEntity.class,
                scanBox,
                projectile -> projectile instanceof net.minecraft.entity.projectile.thrown.ThrownEntity
                        || projectile instanceof net.minecraft.entity.projectile.ArrowEntity
                        || projectile instanceof net.minecraft.entity.projectile.TridentEntity
        );

        int processed = 0;
        int scanBudget = BuiltinOptimizer.getProjectileScanBudget(MAX_PROJECTILES_PER_SCAN);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (ProjectileEntity projectile : projectiles) {
            if (processed++ >= scanBudget || this.particles.size() >= getParticleCap()) {
                break;
            }

            boolean movedBetweenTicks = Math.abs(projectile.getX() - projectile.lastX) > 0.01D
                    || Math.abs(projectile.getY() - projectile.lastY) > 0.01D
                    || Math.abs(projectile.getZ() - projectile.lastZ) > 0.01D;
            if (!movedBetweenTicks && projectile.getVelocity().lengthSquared() <= 0.01D) {
                continue;
            }

            for (int i = 0; i < 2 && this.particles.size() < getParticleCap(); i++) {
                double px = projectile.getX() + (random.nextDouble() - 0.5D) * 0.5D;
                double py = projectile.getY() + random.nextDouble() * projectile.getHeight();
                double pz = projectile.getZ() + (random.nextDouble() - 0.5D) * 0.5D;

                Vec3d pos = new Vec3d(px, py, pz);
                Vec3d velocity = new Vec3d(
                        (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult,
                        (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult,
                        (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult
                );

                this.particles.add(
                        new Particle3D(pos, velocity, getParticleColor(), this.size.getValue() * 0.5F, this.lifeTime.getValue() * 0.3F)
                                .setGravity(getGravity())
                                .setVelocityMultiplier(0.99F)
                                .setMode(getParticleMode())
                                .setGlowMode(getGlowMode())
                );
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (!isTriggerSelected(TRIGGER_ATTACK) || e.getTarget() == null || this.particles.size() >= getParticleCap()) {
            return;
        }

        Entity target = e.getTarget();
        float spreadValue = this.spread.getValue() * 0.15F;
        float speedMult = getSpeedMultiplier();

        int count = Math.min(
                BuiltinOptimizer.getDecorativeSpawnBudget(this.amount.getInt()),
                Math.max(0, getParticleCap() - this.particles.size())
        );

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            Vec3d pos = new Vec3d(target.getX(), target.getY() + random.nextDouble() * target.getHeight(), target.getZ());
            Vec3d velocity = new Vec3d(
                    (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult,
                    (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult,
                    (random.nextDouble() - 0.5D) * 2.0D * spreadValue * speedMult
            );

            this.particles.add(
                    new Particle3D(pos, velocity, getParticleColor(), this.size.getValue(), this.lifeTime.getValue())
                            .setGravity(getGravity())
                            .setVelocityMultiplier(0.99F)
                            .setMode(getParticleMode())
                            .setGlowMode(getGlowMode())
            );
        }
    }

    public void onTotemPop(Entity entity) {
        if (!isTriggerSelected(TRIGGER_TOTEM)) {
            return;
        }
        this.totemEmitters.add(new TotemEmitter(entity, TOTEM_DURATION));
    }

    private void spawnTotemParticlesBurst(Entity entity, float progress) {
        if (entity == null || entity.isRemoved() || this.particles.size() >= getParticleCap()) {
            return;
        }

        float spreadMultiplier = 1.0F - progress * 0.5F;
        float spreadValue = this.spread.getValue();
        float speedMult = getSpeedMultiplier();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int burstBudget = BuiltinOptimizer.getDecorativeSpawnBudget(4);

        for (int i = 0; i < burstBudget && this.particles.size() < getParticleCap(); i++) {
            double d = random.nextDouble() * 2.0D - 1.0D;
            double e = random.nextDouble() * 2.0D - 1.0D;
            double f = random.nextDouble() * 2.0D - 1.0D;
            if (d * d + e * e + f * f <= 1.0D) {
                Vec3d pos = new Vec3d(
                        entity.getX() + d * entity.getWidth() * 0.5D,
                        entity.getBodyY(0.5D) + e * entity.getHeight() * 0.5D,
                        entity.getZ() + f * entity.getWidth() * 0.5D
                );

                double velocityScale = spreadValue * 0.18D * spreadMultiplier * speedMult;
                double initialUpward = random.nextDouble() < 0.4D
                        ? (0.15D + random.nextDouble() * 0.2D) * speedMult
                        : (0.03D + random.nextDouble() * 0.07D) * speedMult;

                Vec3d velocity = new Vec3d(d * velocityScale, initialUpward, f * velocityScale);

                int[] totemColors = {-8586240, -10496, -13447886, -23296, -16711936, -5374161};
                int particleColor = totemColors[random.nextInt(totemColors.length)];

                this.particles.add(
                        new Particle3D(pos, velocity, particleColor, this.size.getValue() * 0.8F, this.lifeTime.getValue() * 0.8F)
                                .setGravity(getGravity())
                                .setVelocityMultiplier(0.98F)
                                .setMode(getParticleMode())
                                .setGlowMode(getGlowMode())
                );
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        if (this.particles.isEmpty() || BuiltinOptimizer.shouldSuspendDecorativeParticles()) {
            return;
        }

        MatrixStack stack = e.getStack();
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        float partialTicks = e.getPartialTicks();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        for (Particle3D particle : this.particles) {
            if (particle.distanceSquaredTo(cameraPos) > RENDER_DISTANCE_SQ) {
                continue;
            }
            particle.render(stack, immediate, GLOW_SIZE, partialTicks);
        }

        immediate.draw();
    }

    private void trimParticleList() {
        int overflow = this.particles.size() - getParticleCap();
        if (overflow > 0) {
            this.particles.subList(0, overflow).clear();
        }
    }

    private int getParticleCap() {
        return BuiltinOptimizer.getDynamicParticleCap(MAX_PARTICLES);
    }
}
