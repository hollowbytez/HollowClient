package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void onRenderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.shortSwords && stack != null && stack.getItem() instanceof SwordItem) {
            if (renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
                // Scale down the sword slightly to look shorter
                matrices.scale(0.85f, 0.85f, 0.85f);
                // Translate it slightly down and back so it doesn't float weirdly
                matrices.translate(leftHanded ? 0.05 : -0.05, -0.05, 0.0);
            }
        } else if (HollowConfig.INSTANCE.shortShields && stack != null && stack.getItem() instanceof ShieldItem) {
            if (renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
                // Scale down the shield significantly and lower it to free up FOV
                matrices.scale(0.7f, 0.7f, 0.7f);
                matrices.translate(leftHanded ? 0.2 : -0.2, -0.2, 0.1);
            }
        }
    }
}

