package net.hollowclient.mixin.client;

import net.hollowclient.client.HollowClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Redirect(
        method = "updateMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
        )
    )
    private void redirectChangeLookDirection(ClientPlayerEntity player, double dx, double dy) {
        if (HollowClient.isFreelooking) {
            // Apply camera orientation changes client-side only (scaled by standard 0.15F look conversion factor)
            HollowClient.freelookYaw += dx * 0.15F;
            HollowClient.freelookPitch += dy * 0.15F;

            // Clamp pitch to prevent camera flips
            if (HollowClient.freelookPitch < -90.0f) {
                HollowClient.freelookPitch = -90.0f;
            }
            if (HollowClient.freelookPitch > 90.0f) {
                HollowClient.freelookPitch = 90.0f;
            }
        } else {
            player.changeLookDirection(dx, dy);
        }
    }
}


