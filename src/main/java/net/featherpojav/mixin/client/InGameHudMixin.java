package net.featherpojav.mixin.client;

import net.featherpojav.client.config.FeatherConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (FeatherConfig.INSTANCE.customCrosshair) {
            FeatherConfig cfg = FeatherConfig.INSTANCE;
            int cx = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
            int cy = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
            
            float gap = cfg.crosshairGap;
            float size = cfg.crosshairSize;
            float th = cfg.crosshairThickness;
            int color = cfg.crosshairColor;
            
            // Draw custom Crosshair
            // Left horizontal line
            context.fill((int) (cx - gap - size), (int) (cy - th / 2.0f), (int) (cx - gap), (int) (cy + th / 2.0f + 0.5f), color);
            // Right horizontal line
            context.fill((int) (cx + gap), (int) (cy - th / 2.0f), (int) (cx + gap + size), (int) (cy + th / 2.0f + 0.5f), color);
            // Top vertical line
            context.fill((int) (cx - th / 2.0f), (int) (cy - gap - size), (int) (cx + th / 2.0f + 0.5f), (int) (cy - gap), color);
            // Bottom vertical line
            context.fill((int) (cx - th / 2.0f), (int) (cy + gap), (int) (cx + th / 2.0f + 0.5f), (int) (cy + gap + size), color);
            
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!FeatherConfig.INSTANCE.scoreboard) {
            ci.cancel();
        }
    }
}
