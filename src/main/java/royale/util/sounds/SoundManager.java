package royale.util.sounds;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import royale.IMinecraft;
import royale.util.string.PlayerInteractionHelper;
public final class SoundManager
implements IMinecraft
{
private SoundManager() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
} public static SoundEvent KOLOKOLNIA_KILL = SoundEvent.of(Identifier.of("royale:kolokolnia_kill"));
public static SoundEvent MOAN1 = SoundEvent.of(Identifier.of("royale:moan1"));
public static SoundEvent MOAN2 = SoundEvent.of(Identifier.of("royale:moan2"));
public static SoundEvent MOAN3 = SoundEvent.of(Identifier.of("royale:moan3"));
public static SoundEvent MOAN4 = SoundEvent.of(Identifier.of("royale:moan4"));
public static SoundEvent MODULE_DISABLE = SoundEvent.of(Identifier.of("royale:module_disable"));
public static SoundEvent MODULE_ENABLE = SoundEvent.of(Identifier.of("royale:module_enable"));
public static SoundEvent OFF = SoundEvent.of(Identifier.of("royale:off"));
public static SoundEvent ON = SoundEvent.of(Identifier.of("royale:on"));
public static SoundEvent CRIME = SoundEvent.of(Identifier.of("royale:crime"));
public static SoundEvent METALLIC = SoundEvent.of(Identifier.of("royale:metallic"));
public static SoundEvent WELCOME = SoundEvent.of(Identifier.of("royale:welcome"));
public static void init() {
Registry.register(Registries.SOUND_EVENT, KOLOKOLNIA_KILL.id(), KOLOKOLNIA_KILL);
Registry.register(Registries.SOUND_EVENT, MOAN1.id(), MOAN1);
Registry.register(Registries.SOUND_EVENT, MOAN2.id(), MOAN2);
Registry.register(Registries.SOUND_EVENT, MOAN3.id(), MOAN3);
Registry.register(Registries.SOUND_EVENT, MOAN4.id(), MOAN4);
Registry.register(Registries.SOUND_EVENT, MODULE_DISABLE.id(), MODULE_DISABLE);
Registry.register(Registries.SOUND_EVENT, MODULE_ENABLE.id(), MODULE_ENABLE);
Registry.register(Registries.SOUND_EVENT, OFF.id(), OFF);
Registry.register(Registries.SOUND_EVENT, ON.id(), ON);
Registry.register(Registries.SOUND_EVENT, CRIME.id(), CRIME);
Registry.register(Registries.SOUND_EVENT, METALLIC.id(), METALLIC);
Registry.register(Registries.SOUND_EVENT, WELCOME.id(), WELCOME);
}
public static void playSound(SoundEvent sound) {
playSound(sound, 1.0F, 1.0F);
}
public static void playSound(SoundEvent sound, float volume, float pitch) {
if (!PlayerInteractionHelper.nullCheck()) {
mc.world.playSound((Entity)mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume, pitch);
}
}
public static void playSoundDirect(SoundEvent sound, float volume, float pitch) {
mc.getSoundManager().play((SoundInstance)PositionedSoundInstance.ui(sound, pitch, volume));
}
}


