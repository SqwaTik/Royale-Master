package royale.util.render.draw;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4i;
import royale.IMinecraft;
public class DrawEngineImpl
implements DrawEngine, IMinecraft
{
public void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height) {
buffer.vertex((Matrix4fc)matrix4f, x, y, 0.0F);
buffer.vertex((Matrix4fc)matrix4f, x, y + height, 0.0F);
buffer.vertex((Matrix4fc)matrix4f, x + width, y + height, 0.0F);
buffer.vertex((Matrix4fc)matrix4f, x + width, y, 0.0F);
}
public void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height, int color) {
buffer.vertex((Matrix4fc)matrix4f, x, y, 0.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x, y + height, 0.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x + width, y + height, 0.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x + width, y, 0.0F).color(color);
}
public void quad(Matrix4f matrix4f, float x, float y, float width, float height, int color) {
BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
buffer.vertex((Matrix4fc)matrix4f, x, y + height, 0.0F).texture(0.0F, 0.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x + width, y + height, 0.0F).texture(0.0F, 1.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x + width, y, 0.0F).texture(1.0F, 1.0F).color(color);
buffer.vertex((Matrix4fc)matrix4f, x, y, 0.0F).texture(1.0F, 0.0F).color(color);
}
public void quadTexture(MatrixStack.Entry entry, BufferBuilder buffer, float x, float y, float width, float height, Vector4i color) {
buffer.vertex(entry, x, y + height, 0.0F).texture(0.0F, 0.0F).color(color.x);
buffer.vertex(entry, x + width, y + height, 0.0F).texture(0.0F, 1.0F).color(color.y);
buffer.vertex(entry, x + width, y, 0.0F).texture(1.0F, 1.0F).color(color.w);
buffer.vertex(entry, x, y, 0.0F).texture(1.0F, 0.0F).color(color.z);
}
}


