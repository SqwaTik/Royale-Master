package royale.modules.impl.render.chinahat;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import royale.modules.impl.render.ChinaHat;
import royale.util.ColorUtil;
import royale.util.render.clientpipeline.ClientPipelines;

public class ChinaHatFeatureRenderer
extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final float PI2 = (float)Math.PI * 2;
    private static final int CIRCLE_SEGMENTS = 720;
    private static final int OUTLINE_SEGMENTS = 360;

    public ChinaHatFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    public void render(MatrixStack matrixStack, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ChinaHat chinaHat = ChinaHat.getInstance();
        if (chinaHat == null || !chinaHat.isState()) {
            return;
        }
        if (mc.player == null) {
            return;
        }
        if (mc.options.getPerspective().isFirstPerson()) {
            return;
        }
        if (!this.isLocalPlayer(state, mc)) {
            return;
        }
        matrixStack.push();
        ((PlayerEntityModel)this.getContextModel()).head.applyTransform(matrixStack);
        matrixStack.multiply((Quaternionfc)RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrixStack.multiply((Quaternionfc)RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
        matrixStack.translate(0.0f, 0.42f, 0.0f);
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        this.renderFlatHat(matrixStack, (VertexConsumerProvider)immediate, chinaHat);
        this.renderOutline(matrixStack, (VertexConsumerProvider)immediate, chinaHat);
        immediate.draw();
        matrixStack.pop();
    }

    private boolean isLocalPlayer(PlayerEntityRenderState state, MinecraftClient mc) {
        try {
            if (state.id == mc.player.getId()) {
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (state.playerName != null && mc.player.getName() != null) {
                return state.playerName.getString().equals(mc.player.getName().getString());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private void renderFlatHat(MatrixStack stack, VertexConsumerProvider provider, ChinaHat chinaHat) {
        float z;
        float x;
        float angle;
        int color;
        int i;
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHINA_HAT);
        Matrix4f matrix = stack.peek().getPositionMatrix();
        float width = 0.55f;
        float coneHeight = 0.31f;
        int alpha = 185;
        float animSpeed = 5.0f;
        int centerColor = this.getGradientColor(0, 720, chinaHat, animSpeed);
        centerColor = ColorUtil.replAlpha(centerColor, alpha);
        consumer.vertex((Matrix4fc)matrix, 0.0f, coneHeight, 0.0f).color(centerColor);
        for (i = 0; i <= 720; ++i) {
            color = this.getGradientColor(i, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, alpha);
            angle = (float)i * ((float)Math.PI * 2) / 720.0f;
            x = -MathHelper.sin((double)angle) * width;
            z = MathHelper.cos((double)angle) * width;
            consumer.vertex((Matrix4fc)matrix, x, 0.0f, z).color(color);
        }
        for (i = 720; i >= 0; --i) {
            color = this.getGradientColor(i, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, alpha);
            angle = (float)i * ((float)Math.PI * 2) / 720.0f;
            x = -MathHelper.sin((double)angle) * width;
            z = MathHelper.cos((double)angle) * width;
            consumer.vertex((Matrix4fc)matrix, x, 0.0f, z).color(color);
        }
        consumer.vertex((Matrix4fc)matrix, 0.0f, coneHeight, 0.0f).color(centerColor);
    }

    private void renderOutline(MatrixStack stack, VertexConsumerProvider provider, ChinaHat chinaHat) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHINA_HAT_OUTLINE);
        Matrix4f matrix = stack.peek().getPositionMatrix();
        float width = 0.55f;
        float animSpeed = 5.0f;
        int outlineAlpha = 255;
        for (int i = 0; i <= 360; ++i) {
            int color = this.getGradientColor(i * 2, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, outlineAlpha);
            float angle = (float)i * ((float)Math.PI * 2) / 360.0f;
            float x = -MathHelper.sin((double)angle) * width;
            float z = MathHelper.cos((double)angle) * width;
            consumer.vertex((Matrix4fc)matrix, x, 0.0f, z).color(color);
        }
    }

    private int getGradientColor(int index, int size, ChinaHat chinaHat, float animSpeed) {
        long time = System.currentTimeMillis();
        float timeOffset = (float)time / (1000.0f / animSpeed) % (float)size;
        int adjustedIndex = (int)(((float)index + timeOffset) % (float)size);
        int color1 = chinaHat.color1.getColor();
        int color2 = chinaHat.color2.getColor();
        float progress = (float)adjustedIndex / (float)size;
        if (progress < 0.5f) {
            return ColorUtil.interpolateColor(color1, color2, progress * 2.0f);
        }
        return ColorUtil.interpolateColor(color2, color1, (progress - 0.5f) * 2.0f);
    }
}

