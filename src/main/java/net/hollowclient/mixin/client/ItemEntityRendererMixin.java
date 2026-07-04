package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Shadow @Final private ItemRenderer itemRenderer;

    private int getRenderedAmount(ItemStack stack) {
        int count = stack.getCount();
        if (count > 48) return 5;
        if (count > 32) return 4;
        if (count > 16) return 3;
        if (count > 1) return 2;
        return 1;
    }

    @Inject(
        method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(ItemEntity itemEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.itemPhysics) {
            ci.cancel();

            ItemStack stack = itemEntity.getStack();
            if (stack.isEmpty()) return;

            BakedModel bakedModel = this.itemRenderer.getModel(stack, itemEntity.getWorld(), null, itemEntity.getId());
            int count = getRenderedAmount(stack);

            for (int idx = 0; idx < count; idx++) {
                matrices.push();

                // If on ground or submerged in water/lava, lay flat and stack
                if (itemEntity.isOnGround() || itemEntity.isSubmergedInWater() || itemEntity.isInLava()) {
                    // Float slightly above block surface to prevent Z-fighting/clipping on mobile depth buffers
                    // Stack subsequent items vertically
                    matrices.translate(0.0, 0.02 + (idx * 0.012), 0.0);

                    // Rotate 90 degrees around X-axis to lay it flat
                    matrices.multiply(new Quaternionf().rotationX(1.5707963f));

                    // Apply a static, distinct rotation based on entity ID to scatter items naturally
                    float uniqueRotation = (float) ((itemEntity.getId() + idx) % 16) * 22.5f;
                    matrices.multiply(new Quaternionf().rotationZ(uniqueRotation * 0.017453292f));
                } else {
                    // Falling / Air / In-flight realistic tumbling
                    float age = (float) itemEntity.getItemAge() + tickDelta;
                    double velocityX = itemEntity.getVelocity().x;
                    double velocityY = itemEntity.getVelocity().y;
                    double velocityZ = itemEntity.getVelocity().z;
                    float speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);

                    // Rotation speed scales dynamically with velocity
                    float pitch = age * speed * 2.0f + (idx * 0.5f);
                    float yawRot = age * speed * 1.5f + (idx * 0.3f);
                    matrices.multiply(new Quaternionf().rotationXYZ(pitch, yawRot, 0.0f));
                }

                // Render the 3D model
                this.itemRenderer.renderItem(
                    stack,
                    ModelTransformationMode.GROUND,
                    false,
                    matrices,
                    vertexConsumers,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    bakedModel
                );

                matrices.pop();
            }
        }
    }
}

