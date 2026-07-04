package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {}

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (HollowConfig.INSTANCE.blockOverlay) {
            // Draw a custom neon colored box instead of a boring thin black line
            VoxelShape shape = state.getOutlineShape(entity.getWorld(), pos, ShapeContext.of(entity));
            if (!shape.isEmpty()) {
                matrices.push();
                matrices.translate(pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ);
                
                // Get theme color
                int color = HollowConfig.INSTANCE.themeColor;
                float r = (float)(color >> 16 & 255) / 255.0F;
                float g = (float)(color >> 8 & 255) / 255.0F;
                float b = (float)(color & 255) / 255.0F;
                
                // We must use a line drawing vertex consumer with thicker lines or just standard
                drawCuboidShapeOutline(matrices, vertexConsumer, shape, 0.0, 0.0, 0.0, r, g, b, 1.0f);
                
                matrices.pop();
            }
            ci.cancel();
        }
    }
}
