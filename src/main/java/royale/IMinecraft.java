package royale;
import net.minecraft.client.MinecraftClient;
import royale.util.render.draw.DrawEngine;
import royale.util.render.draw.DrawEngineImpl;
public interface IMinecraft
{
public static final MinecraftClient mc = MinecraftClient.getInstance();
public static final DrawEngine drawEngine = (DrawEngine)new DrawEngineImpl();
}


