package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Redirect(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
        )
    )
    private Object redirectGetGamma(SimpleOption<?> option) {
        if (HollowConfig.INSTANCE.fullbright && option == net.minecraft.client.MinecraftClient.getInstance().options.getGamma()) {
            return 10.0D; // Forces maximum light levels for gameplay lightmap calculation
        }
        return option.getValue();
    }
}

