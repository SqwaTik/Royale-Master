package royale.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import royale.IMinecraft;
import royale.Initialization;
import royale.events.api.EventManager;
import royale.events.impl.DrawEvent;
import royale.events.impl.HotbarItemRenderEvent;
import royale.modules.impl.render.CustomBar;
import royale.modules.impl.render.Hud;
import royale.modules.impl.render.NoRender;
import royale.util.config.impl.consolelogger.Logger;
import royale.util.render.Render2D;
import royale.util.theme.ClientTheme;

import java.awt.Color;
import java.util.List;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements IMinecraft {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Unique
    private int royale$currentHotbarIndex = 0;
    @Unique
    private float royale$animatedSelectedX = Float.NaN;
    @Unique
    private static boolean royale$hudErrorLogged = false;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderCustomHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!shouldUseCustomBar()) {
            return;
        }

        ClientPlayerEntity player = this.client.player;
        if (player == null || player.isSpectator()) {
            ci.cancel();
            return;
        }

        CustomBar customBar = CustomBar.getInstance();
        boolean showHotbar = customBar == null || !customBar.isState() || customBar.showHotbar.isValue();
        if (!showHotbar) {
            return;
        }

        boolean smoothAnimation = isSmoothHotbarAnimation(customBar);
        boolean roundedType = isRoundedHotbarType(customBar);

        float width = 182.0F;
        float height = 22.0F;
        float x = (this.client.getWindow().getScaledWidth() - width) / 2.0F;
        float y = this.client.getWindow().getScaledHeight() - height - 1.0F;

        float backgroundRadius = roundedType ? 6.0F : 0.0F;
        float selectedRadius = roundedType ? 4.0F : 0.0F;
        Render2D.rect(x, y, width, height, new Color(12, 12, 14, 175).getRGB(), backgroundRadius);
        Render2D.outline(x, y, width, height, 0.6F, new Color(80, 80, 86, 180).getRGB(), backgroundRadius);

        int selectedSlot = player.getInventory().getSelectedSlot();
        float targetSelectedX = x + 1.0F + selectedSlot * 20.0F;
        if (!smoothAnimation) {
            this.royale$animatedSelectedX = targetSelectedX;
        } else if (Float.isNaN(this.royale$animatedSelectedX)) {
            this.royale$animatedSelectedX = targetSelectedX;
        } else {
            float tickDelta = tickCounter.getTickProgress(false);
            float followFactor = MathHelper.clamp(0.24F + tickDelta * 0.42F, 0.0F, 1.0F);
            float deltaX = MathHelper.clamp(targetSelectedX - this.royale$animatedSelectedX, -11.5F, 11.5F);
            this.royale$animatedSelectedX += deltaX * followFactor;
            if (Math.abs(this.royale$animatedSelectedX - targetSelectedX) < 0.06F) {
                this.royale$animatedSelectedX = targetSelectedX;
            }
        }

        float selectedX = smoothAnimation ? this.royale$animatedSelectedX : targetSelectedX;
        Render2D.gradientRect(
                selectedX,
                y + 1.0F,
                20.0F,
                20.0F,
                new int[]{
                        ClientTheme.accentWithAlpha(110),
                        ClientTheme.accentWithAlpha(165),
                        ClientTheme.accentWithAlpha(110),
                        ClientTheme.accentWithAlpha(165)
                },
                selectedRadius
        );

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            HotbarItemRenderEvent event = new HotbarItemRenderEvent(stack, i);
            EventManager.callEvent(event);

            int itemX = (int) (x + 3.0F + i * 20.0F);
            int itemY = (int) (y + 3.0F);
            renderHotbarItem(context, itemX, itemY, tickCounter, player, event.getStack(), i);
        }

        ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void onRenderHotbarStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        this.royale$currentHotbarIndex = 0;
    }

    @WrapOperation(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
            )
    )
    private void onRenderHotbarItem(InGameHud instance, DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, Operation<Void> original) {
        int hotbarIndex = this.royale$currentHotbarIndex;
        if (this.royale$currentHotbarIndex < 9) {
            this.royale$currentHotbarIndex++;
        }

        HotbarItemRenderEvent event = new HotbarItemRenderEvent(stack, hotbarIndex);
        EventManager.callEvent(event);
        original.call(instance, context, x, y, tickCounter, player, event.getStack(), seed);
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNauseaOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Nausea")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboard(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Scoreboard")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBossBarHud", at = @At("HEAD"), cancellable = true)
    private void onRenderBossBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("BossBar")) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderCustomHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.options.hudHidden || this.client.world == null || this.client.player == null || this.client.getOverlay() != null) {
            return;
        }

        try {
            Screen screen = this.client.currentScreen;
            if (isLoadingScreen(screen) || screen instanceof royale.screens.clickgui.ClickGui) {
                return;
            }

            context.createNewRootLayer();
            Render2D.beginOverlay();
            try {
                context.getMatrices().pushMatrix();
                try {
                    DrawEvent event = new DrawEvent(context, drawEngine, tickCounter.getTickProgress(false));
                    EventManager.callEvent(event);
                } finally {
                    context.getMatrices().popMatrix();
                }

                if (shouldRenderHud(screen)) {
                    int mouseX = (int) this.client.mouse.getScaledX(this.client.getWindow());
                    int mouseY = (int) this.client.mouse.getScaledY(this.client.getWindow());
                    float tickDelta = tickCounter.getTickProgress(false);
                    Hud hud = Hud.getInstance();
                    if (hud != null
                            && hud.isState()
                            && Initialization.getInstance() != null
                            && Initialization.getInstance().getManager() != null
                            && Initialization.getInstance().getManager().getHudManager() != null) {
                        try {
                            Initialization.getInstance().getManager().getHudManager().render(context, tickDelta, mouseX, mouseY);
                        } catch (Throwable hudRenderError) {
                            if (!royale$hudErrorLogged) {
                                Logger.error("HUD render error: " + hudRenderError.getClass().getSimpleName() + " - " + hudRenderError.getMessage());
                                royale$hudErrorLogged = true;
                            }
                        }
                    }
                }
            } finally {
                Render2D.endOverlay();
            }
        } catch (Throwable fatalHudHookError) {
            if (!royale$hudErrorLogged) {
                Logger.error("HUD hook error: " + fatalHudHookError.getClass().getSimpleName() + " - " + fatalHudHookError.getMessage());
                royale$hudErrorLogged = true;
            }
        }
    }

    @Unique
    private boolean shouldRenderHud(Screen screen) {
        if (screen == null) {
            return true;
        }
        return !(screen instanceof royale.screens.clickgui.ClickGui)
                && !(screen instanceof ChatScreen)
                && !isLoadingScreen(screen);
    }

    @Unique
    private boolean isLoadingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getSimpleName().toLowerCase();
        String fullName = screen.getClass().getName().toLowerCase();
        return className.contains("loading")
                || className.contains("progress")
                || className.contains("connecting")
                || className.contains("downloading")
                || className.contains("terrain")
                || className.contains("generating")
                || className.contains("saving")
                || className.contains("reload")
                || className.contains("resource")
                || className.contains("pack")
                || fullName.contains("mojang");
    }

    @Unique
    private boolean shouldUseCustomBar() {
        CustomBar customBar = CustomBar.getInstance();
        Hud hud = Hud.getInstance();
        boolean moduleEnabled = customBar != null && customBar.isState();
        boolean hudEnabled = hud != null && hud.isState() && hud.interfaceSettings.isSelected("CustomBar");
        return moduleEnabled || hudEnabled;
    }

    @Unique
    private boolean isSmoothHotbarAnimation(CustomBar customBar) {
        if (customBar == null || customBar.mode == null || customBar.mode.getSelected() == null) {
            return true;
        }
        List<String> modeList = customBar.mode.getList();
        String selected = customBar.mode.getSelected();
        if ("Sharp".equalsIgnoreCase(selected) || "Резкий".equalsIgnoreCase(selected)) {
            return false;
        }
        if (modeList != null && modeList.size() >= 2 && selected != null) {
            String sharpMode = modeList.get(1);
            if (sharpMode != null && sharpMode.equalsIgnoreCase(selected)) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private boolean isRoundedHotbarType(CustomBar customBar) {
        if (customBar == null || customBar.type == null || customBar.type.getSelected() == null) {
            return true;
        }
        List<String> typeList = customBar.type.getList();
        String selected = customBar.type.getSelected();
        if ("None".equalsIgnoreCase(selected) || "Без скругления".equalsIgnoreCase(selected)) {
            return false;
        }
        if (typeList != null && !typeList.isEmpty() && selected != null) {
            String noneType = typeList.get(0);
            if (noneType != null && noneType.equalsIgnoreCase(selected)) {
                return false;
            }
        }
        return true;
    }

}

