package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Shadow public boolean isClient;

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (this.isClient && HollowConfig.INSTANCE.timeChanger) {
            cir.setReturnValue(HollowConfig.INSTANCE.timeChangerTicks);
        }
    }
}

