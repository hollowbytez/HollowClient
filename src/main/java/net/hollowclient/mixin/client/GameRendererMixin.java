package net.hollowclient.mixin.client;

import net.hollowclient.client.HollowClient;
import net.hollowclient.client.config.HollowConfig;
import net.hollowclient.client.gui.HollowHomeScreen;
import net.hollowclient.client.gui.HollowSettingsScreen;
import net.hollowclient.client.gui.HollowGameMenuScreen;
import net.hollowclient.client.gui.HollowHudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (HollowClient.isZooming) {
            HollowClient.targetZoomLevel = HollowConfig.INSTANCE.zoomMagnification;
        } else {
            HollowClient.targetZoomLevel = 1.0f;
        }

        // Smoothly interpolate zoom transition (20% step per frame)
        HollowClient.zoomLevel += (HollowClient.targetZoomLevel - HollowClient.zoomLevel) * 0.2f;

        if (HollowClient.zoomLevel > 1.01f) {
            cir.setReturnValue(cir.getReturnValue() / HollowClient.zoomLevel);
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!HollowConfig.INSTANCE.hurtCam) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.noBobbing) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void onRenderBlur(float delta, CallbackInfo ci) {
        if (this.client != null && this.client.currentScreen != null) {
            Screen s = this.client.currentScreen;
            if (s instanceof HollowHomeScreen ||
                s instanceof HollowSettingsScreen ||
                s instanceof HollowGameMenuScreen ||
                s instanceof HollowHudEditorScreen ||
                s.getClass().getName().startsWith("net.hollowclient.client.gui.")) {
                ci.cancel();
            }
        }
    }
}


