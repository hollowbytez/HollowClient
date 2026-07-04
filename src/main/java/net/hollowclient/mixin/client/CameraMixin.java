package net.hollowclient.mixin.client;

import net.hollowclient.client.HollowClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyArg(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
        ),
        index = 0
    )
    private float modifySetRotationYaw(float yaw) {
        if (HollowClient.isFreelooking) {
            return HollowClient.freelookYaw;
        }
        return yaw;
    }

    @ModifyArg(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
        ),
        index = 1
    )
    private float modifySetRotationPitch(float pitch) {
        if (HollowClient.isFreelooking) {
            return HollowClient.freelookPitch;
        }
        return pitch;
    }
}


