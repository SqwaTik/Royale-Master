package royale.mixin;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import royale.modules.impl.misc.Client;

@Mixin(value={AbstractClientPlayerEntity.class})
public class CustomCapeMixin {
    @Unique
    private static final Identifier BASE_CAPE_TEXTURE = Identifier.of((String)"royale", (String)"textures/capes/cape.png");
    @Unique
    private static final Identifier FALLBACK_CAPE_ID = Identifier.of((String)"royale", (String)"capes/cape");
    @Unique
    private static final AssetInfo.TextureAssetInfo FALLBACK_CAPE_ASSET = new AssetInfo.TextureAssetInfo(FALLBACK_CAPE_ID);
    @Unique
    private static Identifier dynamicCapeId;
    @Unique
    private static int dynamicCapeColor;

    @Inject(method={"getSkin"}, at={@At(value="RETURN")}, cancellable=true)
    private void replaceCape(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)(Object)this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !player.getUuid().equals(client.player.getUuid())) {
            return;
        }
        SkinTextures old = (SkinTextures)cir.getReturnValue();
        AssetInfo.TextureAssetInfo cape = CustomCapeMixin.resolveCapeAsset(client);
        cir.setReturnValue(new SkinTextures(old.body(), (AssetInfo.TextureAsset)cape, (AssetInfo.TextureAsset)cape, old.model(), old.secure()));
    }

    @Unique
    private static AssetInfo.TextureAssetInfo resolveCapeAsset(MinecraftClient client) {
        Identifier generated;
        int clientColor = Client.getResolvedClientColor();
        if ((dynamicCapeId == null || dynamicCapeColor != clientColor) && (generated = CustomCapeMixin.generateDynamicCape(client, clientColor)) != null) {
            dynamicCapeId = generated;
            dynamicCapeColor = clientColor;
        }
        if (dynamicCapeId == null) {
            return FALLBACK_CAPE_ASSET;
        }
        return new AssetInfo.TextureAssetInfo(dynamicCapeId);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Unique
    private static Identifier generateDynamicCape(MinecraftClient client, int accentColor) {
        try (InputStream stream = client.getResourceManager().open(BASE_CAPE_TEXTURE);){
            NativeImage image = NativeImage.read((InputStream)stream);
            CustomCapeMixin.repaintCape(image, accentColor);
            NativeImageBackedTexture texture = CustomCapeMixin.createBackedTexture(image);
            if (texture == null) {
                image.close();
                Identifier class_29603 = null;
                return class_29603;
            }
            Identifier dynamicId = Identifier.of((String)"royale", (String)("dynamic_cape_" + Integer.toHexString(accentColor)));
            client.getTextureManager().registerTexture(dynamicId, (AbstractTexture)texture);
            Identifier class_29602 = dynamicId;
            return class_29602;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    @Unique
    private static NativeImageBackedTexture createBackedTexture(NativeImage image) {
        try {
            Constructor supplierCtor = NativeImageBackedTexture.class.getDeclaredConstructor(Supplier.class, NativeImage.class);
            supplierCtor.setAccessible(true);
            return (NativeImageBackedTexture)supplierCtor.newInstance(new Object[]{(Supplier<String>)(() -> "royale_cape"), image});
        }
        catch (Exception supplierCtor) {
            try {
                Constructor stringCtor = NativeImageBackedTexture.class.getDeclaredConstructor(String.class, NativeImage.class);
                stringCtor.setAccessible(true);
                return (NativeImageBackedTexture)stringCtor.newInstance("royale_cape", image);
            }
            catch (Exception stringCtor) {
                try {
                    Constructor plainCtor = NativeImageBackedTexture.class.getDeclaredConstructor(NativeImage.class);
                    plainCtor.setAccessible(true);
                    return (NativeImageBackedTexture)plainCtor.newInstance(image);
                }
                catch (Exception ignored) {
                    return null;
                }
            }
        }
    }

    @Unique
    private static void repaintCape(NativeImage image, int accentColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int pixel = image.getColorArgb(x, y);
                int alpha = pixel >>> 24 & 0xFF;
                if (alpha <= 0) continue;
                image.setColorArgb(x, y, alpha << 24 | 0x80808);
            }
        }
        int scaleX = Math.max(1, width / 64);
        int scaleY = Math.max(1, height / 32);
        int logoMain = 0xF2F2F2;
        int logoAccent = CustomCapeMixin.mixColor(0xE6E6E6, accentColor & 0xFFFFFF, 0.46f);
        CustomCapeMixin.drawCapeLogo(image, 1 * scaleX, 1 * scaleY, 10 * scaleX, 16 * scaleY, logoMain, logoAccent);
        CustomCapeMixin.drawCapeLogo(image, 12 * scaleX, 1 * scaleY, 10 * scaleX, 16 * scaleY, logoMain, logoAccent);
    }

    @Unique
    private static void drawCapeLogo(NativeImage image, int x, int y, int w, int h, int mainColor, int accentColor) {
        int padX = Math.max(1, w / 6);
        int padY = Math.max(1, h / 12);
        int areaX = x + padX;
        int areaY = y + padY;
        int areaW = Math.max(1, w - padX * 2);
        int areaH = Math.max(1, h - padY * 2);
        int logoX = areaX + Math.max(1, Math.round((float)areaW * 0.02f));
        int logoY = areaY + Math.max(1, Math.round((float)areaH * 0.02f));
        int logoW = Math.max(1, Math.round((float)areaW * 0.88f));
        int logoH = Math.max(1, Math.round((float)areaH * 0.88f));
        CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.0f, 0.0f, 0.17f, 0.78f, mainColor);
        CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.0f, 0.0f, 0.62f, 0.16f, mainColor);
        CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.0f, 0.33f, 0.48f, 0.14f, mainColor);
        CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.46f, 0.14f, 0.17f, 0.2f, mainColor);
        for (int i = 0; i < 5; ++i) {
            CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.33f + (float)i * 0.08f, 0.48f + (float)i * 0.08f, 0.16f, 0.12f, mainColor);
        }
        CustomCapeMixin.fillLogoRect(image, logoX, logoY, logoW, logoH, 0.32f, 0.05f, 0.24f, 0.06f, accentColor);
    }

    @Unique
    private static void fillLogoRect(NativeImage image, int x, int y, int w, int h, float rx, float ry, float rw, float rh, int rgb) {
        int px = x + Math.round((float)w * rx);
        int py = y + Math.round((float)h * ry);
        int pw = Math.max(1, Math.round((float)w * rw));
        int ph = Math.max(1, Math.round((float)h * rh));
        CustomCapeMixin.paintOpaqueRect(image, px, py, pw, ph, rgb);
    }

    @Unique
    private static void paintOpaqueRect(NativeImage image, int x, int y, int w, int h, int rgb) {
        int maxX = image.getWidth();
        int maxY = image.getHeight();
        int x1 = Math.max(0, x);
        int y1 = Math.max(0, y);
        int x2 = Math.min(maxX, x + w);
        int y2 = Math.min(maxY, y + h);
        for (int py = y1; py < y2; ++py) {
            for (int px = x1; px < x2; ++px) {
                image.setColorArgb(px, py, 0xFF000000 | rgb & 0xFFFFFF);
            }
        }
    }

    @Unique
    private static int mixColor(int c1, int c2, float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int r = (int)((float)r1 + (float)(r2 - r1) * clamped);
        int g = (int)((float)g1 + (float)(g2 - g1) * clamped);
        int b = (int)((float)b1 + (float)(b2 - b1) * clamped);
        return r << 16 | g << 8 | b;
    }

    static {
        dynamicCapeColor = Integer.MIN_VALUE;
    }
}

