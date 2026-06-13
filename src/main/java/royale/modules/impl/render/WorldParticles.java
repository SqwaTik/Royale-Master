package royale.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import royale.events.api.EventHandler;
import royale.events.impl.TickEvent;
import royale.events.impl.WorldRenderEvent;
import royale.modules.impl.render.worldparticles.Particle;
import royale.modules.impl.render.worldparticles.ParticleSpawner;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.BooleanSetting;
import royale.modules.module.setting.implement.ColorSetting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.SliderSettings;
import royale.util.Instance;
import royale.util.performance.BuiltinOptimizer;
import royale.util.timer.StopWatch;

public class WorldParticles extends ModuleStructure {
    public static final String TYPE_CUBE_3D = "3D Кубы";
    public static final String TYPE_CROWN = "Корона";
    public static final String TYPE_CUBE_BLAST = "Куб-взрыв";
    public static final String TYPE_DOLLAR = "Доллар";
    public static final String TYPE_HEART = "Сердце";
    public static final String TYPE_LIGHTNING = "Молния";
    public static final String TYPE_LINE = "Линия";
    public static final String TYPE_RHOMBUS = "Ромб";
    public static final String TYPE_SNOWFLAKE = "Снежинка";
    public static final String TYPE_STAR = "Звезда";
    public static final String TYPE_STAR_ALT = "Звезда 2";
    public static final String TYPE_TRIANGLE = "Треугольник";
    public static final String TYPE_GLOW = "Свечение";
    public static final String TYPE_RANDOM = "Рандом";

    private static final double HARD_DESPAWN_DISTANCE_SQ = 32400.0D;
    private static final double RENDER_DISTANCE_SQ = 16900.0D;

    private final List<Particle> particles = new ArrayList<>();
    private final StopWatch timer = new StopWatch();
    private Vec3d lastPlayerPos = Vec3d.ZERO;
    private Vec3d playerVelocity = Vec3d.ZERO;
    private double playerSpeed = 0.0D;

    public SelectSetting mode = (new SelectSetting("Режим", "Визуальный стиль мировых частиц"))
            .value(new String[]{
                    TYPE_CUBE_3D,
                    TYPE_CROWN,
                    TYPE_CUBE_BLAST,
                    TYPE_DOLLAR,
                    TYPE_HEART,
                    TYPE_LIGHTNING,
                    TYPE_LINE,
                    TYPE_RHOMBUS,
                    TYPE_SNOWFLAKE,
                    TYPE_STAR,
                    TYPE_STAR_ALT,
                    TYPE_TRIANGLE,
                    TYPE_GLOW,
                    TYPE_RANDOM
            })
            .selected(TYPE_STAR);

    public SliderSettings cubeCount = (new SliderSettings("Количество", "Максимальное количество частиц"))
            .range(10.0F, 500.0F)
            .setValue(100.0F);

    public SliderSettings lifeTime = (new SliderSettings("Время жизни", "Сколько секунд живет частица"))
            .range(2.0F, 60.0F)
            .setValue(10.0F);

    public SliderSettings size = (new SliderSettings("Размер", "Размер частицы"))
            .range(0.1F, 1.5F)
            .setValue(1.5F);

    public SliderSettings glowSize = (new SliderSettings("Свечение", "Размер свечения у частицы"))
            .range(0.1F, 5.0F)
            .setValue(3.0F);

    public BooleanSetting physics = (new BooleanSetting("Физика", "Учитывать столкновения с блоками"))
            .setValue(false);

    public BooleanSetting randomColor = (new BooleanSetting("Случайный цвет", "Каждая частица получает случайный цвет"))
            .setValue(false);

    public BooleanSetting whiteOnSpawn = (new BooleanSetting("Белые при спавне", "В начале частицы появляются белыми и затем становятся цветными"))
            .setValue(true);

    public BooleanSetting whiteCenter = (new BooleanSetting("Белый центр", "Рисовать белый центр у текстурных частиц"))
            .setValue(false)
            .visible(() -> Boolean.valueOf(!TYPE_CUBE_3D.equals(this.mode.getSelected())));

    public ColorSetting cubeColor = (new ColorSetting("Цвет", "Основной цвет частиц"))
            .value(-7773880)
            .visible(() -> Boolean.valueOf(!this.randomColor.isValue()));

    public WorldParticles() {
        super("WorldParticles", "Добавляет декоративные частицы вокруг игрока", ModuleCategory.RENDER);
        settings(new Setting[]{
                this.mode,
                this.cubeCount,
                this.lifeTime,
                this.size,
                this.glowSize,
                this.physics,
                this.randomColor,
                this.whiteOnSpawn,
                this.whiteCenter,
                this.cubeColor
        });
    }

    public static WorldParticles getInstance() {
        return (WorldParticles) Instance.get(WorldParticles.class);
    }

    public void deactivate() {
        this.particles.clear();
        this.lastPlayerPos = Vec3d.ZERO;
        this.playerVelocity = Vec3d.ZERO;
        this.playerSpeed = 0.0D;
    }

    private Particle.ParticleType getParticleType() {
        return switch (this.mode.getSelected()) {
            case TYPE_CROWN -> Particle.ParticleType.CROWN;
            case TYPE_CUBE_BLAST -> Particle.ParticleType.CUBE_BLAST;
            case TYPE_DOLLAR -> Particle.ParticleType.DOLLAR;
            case TYPE_HEART -> Particle.ParticleType.HEART;
            case TYPE_LIGHTNING -> Particle.ParticleType.LIGHTNING;
            case TYPE_LINE -> Particle.ParticleType.LINE;
            case TYPE_RHOMBUS -> Particle.ParticleType.RHOMBUS;
            case TYPE_SNOWFLAKE -> Particle.ParticleType.SNOWFLAKE;
            case TYPE_STAR -> Particle.ParticleType.STAR;
            case TYPE_STAR_ALT -> Particle.ParticleType.STAR_ALT;
            case TYPE_TRIANGLE -> Particle.ParticleType.TRIANGLE;
            case TYPE_GLOW -> Particle.ParticleType.GLOW;
            case TYPE_RANDOM -> Particle.ParticleType.RANDOM;
            case TYPE_CUBE_3D -> Particle.ParticleType.CUBE_3D;
            default -> Particle.ParticleType.CUBE_3D;
        };
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        Vec3d currentPos = mc.player.getEntityPos();
        if (this.lastPlayerPos != Vec3d.ZERO) {
            this.playerVelocity = currentPos.subtract(this.lastPlayerPos);
            this.playerSpeed = this.playerVelocity.horizontalLength();
        }
        this.lastPlayerPos = currentPos;

        long now = System.currentTimeMillis();
        double despawnDistSq = ParticleSpawner.getDespawnDistanceSquared();

        Iterator<Particle> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            if (particle.getDistanceSquaredTo(currentPos) > HARD_DESPAWN_DISTANCE_SQ) {
                iterator.remove();
                continue;
            }
            if (!particle.isFadingOut() && particle.getHorizontalDistanceSquaredTo(currentPos) > despawnDistSq) {
                particle.startFadeOut();
            }
            particle.update(now);
            if (particle.shouldRemove()) {
                iterator.remove();
            }
        }

        int maxParticles = BuiltinOptimizer.getDynamicWorldParticleCap(this.cubeCount.getInt());
        int actualDelay = ParticleSpawner.calculateSpawnDelay(this.playerSpeed);

        if (BuiltinOptimizer.shouldSuspendDecorativeParticles()) {
            int keep = Math.min(24, this.particles.size());
            if (this.particles.size() > keep) {
                this.particles.subList(0, this.particles.size() - keep).clear();
            }
            return;
        }

        if (this.particles.size() < maxParticles && this.timer.finished(actualDelay)) {
            int spawnCount = BuiltinOptimizer.getDecorativeSpawnBudget(
                    ParticleSpawner.calculateSpawnCount(this.playerSpeed, this.particles.size(), maxParticles)
            );

            long lifeTimeMs = (long) (this.lifeTime.getValue() * 1000.0F);
            Particle.ParticleType type = getParticleType();
            boolean physicsEnabled = this.physics.isValue();
            float sizeValue = this.size.getValue();

            for (int i = 0; i < spawnCount && this.particles.size() < maxParticles; i++) {
                Particle particle = ParticleSpawner
                        .createParticle(currentPos, this.playerVelocity, this.playerSpeed, lifeTimeMs, type)
                        .setPhysics(physicsEnabled)
                        .setSize(sizeValue);
                this.particles.add(particle);
            }

            this.timer.reset();
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.particles.isEmpty() || BuiltinOptimizer.shouldSuspendDecorativeParticles()) {
            return;
        }

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        MatrixStack matrices = e.getStack();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        float partialTicks = e.getPartialTicks();

        long now = System.currentTimeMillis();
        float cameraYaw = mc.gameRenderer.getCamera().getYaw();
        float cameraPitch = mc.gameRenderer.getCamera().getPitch();
        float rotation = (float) (now % 9000L) / 9000.0F * 360.0F;

        int baseColor = this.cubeColor.getColor();
        float glow = this.glowSize.getValue();
        boolean useRandomColor = this.randomColor.isValue();
        boolean useWhiteOnSpawn = this.whiteOnSpawn.isValue();
        boolean useWhiteCenter = this.whiteCenter.isValue();

        for (Particle particle : this.particles) {
            if (particle.getDistanceSquaredTo(cameraPos) > RENDER_DISTANCE_SQ) {
                continue;
            }
            particle.render(
                    matrices,
                    immediate,
                    cameraPos,
                    baseColor,
                    rotation,
                    cameraYaw,
                    cameraPitch,
                    glow,
                    useRandomColor,
                    useWhiteOnSpawn,
                    useWhiteCenter,
                    partialTicks
            );
        }

        immediate.draw();
    }
}
