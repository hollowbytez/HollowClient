package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof net.hollowclient.client.gui.HollowSettingsScreen ||
            client.currentScreen instanceof net.hollowclient.client.gui.HollowGameMenuScreen ||
            client.currentScreen instanceof net.hollowclient.client.gui.HollowHudEditorScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof net.hollowclient.client.gui.HollowHudEditorScreen)) {
            net.hollowclient.client.hud.HollowHudRenderer.renderHUD(context, tickCounter.getTickDelta(true));
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.customCrosshair) {
            HollowConfig cfg = HollowConfig.INSTANCE;
            MinecraftClient client = MinecraftClient.getInstance();
            double scale = client.getWindow().getScaleFactor();
            net.minecraft.client.util.math.MatrixStack matrices = context.getMatrices();

            matrices.push();
            // Scale matrices down to physical screen pixels for high-resolution rendering
            matrices.scale((float) (1.0 / scale), (float) (1.0 / scale), 1.0f);

            int cx = client.getWindow().getWidth() / 2;
            int cy = client.getWindow().getHeight() / 2;

            // Scale configuration parameters (size and gap) so crosshair size remains consistent across UI scales,
            // while thickness and alignment remain pixel-perfect relative to physical resolution.
            float gap = cfg.crosshairGap * (float) scale;
            float size = cfg.crosshairSize * (float) scale;
            float th = cfg.crosshairThickness;
            int color = cfg.crosshairColor;
            int preset = cfg.crosshairPreset;

            int t = Math.max(1, (int) th);
            int hOffset1 = t / 2;
            int hOffset2 = t / 2 + (t % 2);

            // Draw custom Crosshair based on preset
            switch (preset) {
                case 1: // Dot
                    context.fill(cx - hOffset1, cy - hOffset1, cx + hOffset2, cy + hOffset2, color);
                    break;
                case 2: // Circle
                case 3: // Circle with Dot
                    int radius = (int) (gap + size);
                    for (int angle = 0; angle < 360; angle += 10) {
                        double rad = Math.toRadians(angle);
                        int px = (int) Math.round(cx + Math.cos(rad) * radius);
                        int py = (int) Math.round(cy + Math.sin(rad) * radius);
                        context.fill(px - hOffset1, py - hOffset1, px + hOffset2, py + hOffset2, color);
                    }
                    if (preset == 3) {
                        context.fill(cx - hOffset1, cy - hOffset1, cx + hOffset2, cy + hOffset2, color);
                    }
                    break;
                case 4: // T-Shape
                    context.fill((int) (cx - gap - size), cy - hOffset1, (int) (cx - gap), cy + hOffset2, color);
                    context.fill((int) (cx + gap), cy - hOffset1, (int) (cx + gap + size), cy + hOffset2, color);
                    context.fill(cx - hOffset1, (int) (cy + gap), cx + hOffset2, (int) (cy + gap + size), color);
                    break;
                case 5: // X-Shape
                    float offset = gap;
                    for (int idx = 0; idx < (int) size; idx++) {
                        int f = (int) (offset + idx);
                        context.fill(cx - f - hOffset1, cy - f - hOffset1, cx - f + hOffset2, cy - f + hOffset2, color);
                        context.fill(cx + f - hOffset1, cy - f - hOffset1, cx + f + hOffset2, cy - f + hOffset2, color);
                        context.fill(cx - f - hOffset1, cy + f - hOffset1, cx - f + hOffset2, cy + f + hOffset2, color);
                        context.fill(cx + f - hOffset1, cy + f - hOffset1, cx + f + hOffset2, cy + f + hOffset2, color);
                    }
                    break;
                case 6: // Square
                case 9: // Box with Dot
                    int r = (int) (gap + size);
                    context.fill(cx - r, cy - r - hOffset1, cx + r, cy - r + hOffset2, color);
                    context.fill(cx - r, cy + r - hOffset1, cx + r, cy + r + hOffset2, color);
                    context.fill(cx - r - hOffset1, cy - r, cx - r + hOffset2, cy + r, color);
                    context.fill(cx + r - hOffset1, cy - r, cx + r + hOffset2, cy + r, color);
                    if (preset == 9) {
                        context.fill(cx - hOffset1, cy - hOffset1, cx + hOffset2, cy + hOffset2, color);
                    }
                    break;
                case 7: // Arrow / Chevron
                    for (int idx = 0; idx < (int) size; idx++) {
                        context.fill(cx - idx - hOffset1, (int) (cy - gap + idx) - hOffset1, cx - idx + hOffset2, (int) (cy - gap + idx) + hOffset2, color);
                        context.fill(cx + idx - hOffset1, (int) (cy - gap + idx) - hOffset1, cx + idx + hOffset2, (int) (cy - gap + idx) + hOffset2, color);
                    }
                    break;
                case 8: // Tri-Bar
                    context.fill((int) (cx - gap - size), cy - hOffset1, (int) (cx - gap), cy + hOffset2, color);
                    context.fill((int) (cx + gap), cy - hOffset1, (int) (cx + gap + size), cy + hOffset2, color);
                    context.fill(cx - hOffset1, (int) (cy - gap - size), cx + hOffset2, (int) (cy - gap), color);
                    break;
                default: // 0: Classic Cross
                    context.fill((int) (cx - gap - size), cy - hOffset1, (int) (cx - gap), cy + hOffset2, color);
                    context.fill((int) (cx + gap), cy - hOffset1, (int) (cx + gap + size), cy + hOffset2, color);
                    context.fill(cx - hOffset1, (int) (cy - gap - size), cx + hOffset2, (int) (cy - gap), color);
                    context.fill(cx - hOffset1, (int) (cy + gap), cx + hOffset2, (int) (cy + gap + size), color);
                    break;
            }

            matrices.pop();
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!HollowConfig.INSTANCE.scoreboard) {
            ci.cancel();
        }
    }
}

