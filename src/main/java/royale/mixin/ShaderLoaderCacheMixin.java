package royale.mixin;

import com.mojang.blaze3d.shaders.ShaderType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gl.ShaderLoader$Cache")
public abstract class ShaderLoaderCacheMixin {
    @Unique
    private static final Map<String, String> royale$shaderFallbackCache = new ConcurrentHashMap<>();

    @Inject(method = "getSource", at = @At("RETURN"), cancellable = true)
    private void royale$provideClasspathShaderSource(
            Identifier id,
            ShaderType type,
            CallbackInfoReturnable<String> cir
    ) {
        if (cir.getReturnValue() != null || id == null || type == null) {
            return;
        }
        if (!"royale".equals(id.getNamespace())) {
            return;
        }

        String extension = royale$getShaderExtension(type);
        if (extension == null) {
            return;
        }

        String resourcePath = "assets/" + id.getNamespace() + "/shaders/" + id.getPath() + extension;
        String source = royale$shaderFallbackCache.computeIfAbsent(
                resourcePath,
                ShaderLoaderCacheMixin::royale$readShaderSource
        );
        if (source != null) {
            cir.setReturnValue(source);
        }
    }

    @Unique
    private static String royale$getShaderExtension(ShaderType type) {
        return switch (type) {
            case VERTEX -> ".vsh";
            case FRAGMENT -> ".fsh";
        };
    }

    @Unique
    private static String royale$readShaderSource(String resourcePath) {
        try (InputStream inputStream = ShaderLoaderCacheMixin.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return null;
        }
    }
}
