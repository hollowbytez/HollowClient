package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void onRenderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.lowFire) {
            // Lower the fire rendering overlay vertically to prevent screen clutter
            matrices.translate(0.0F, -0.3F, 0.0F);
        }
    }
}

