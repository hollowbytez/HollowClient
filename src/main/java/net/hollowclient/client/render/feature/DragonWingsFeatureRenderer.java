package net.hollowclient.client.render.feature;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DragonWingsFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    
    // Using vanilla dragon texture as placeholder until user adds their own
    private static final Identifier WING_TEXTURE = Identifier.ofVanilla("textures/entity/enderdragon/dragon.png"); 
    
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public DragonWingsFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        
        // Wing geometry mapped to the vanilla Ender Dragon texture sheet (256x256)
        // Left Wing
        ModelPartData left = modelPartData.addChild("left_wing", ModelPartBuilder.create().uv(112, 88).cuboid(0.0F, -4.0F, 0.0F, 56.0F, 32.0F, 2.0F), ModelTransform.pivot(0.0F, 0.0F, 2.0F));
        // Right Wing (mirrored geometry)
        ModelPartData right = modelPartData.addChild("right_wing", ModelPartBuilder.create().uv(112, 88).cuboid(-56.0F, -4.0F, 0.0F, 56.0F, 32.0F, 2.0F), ModelTransform.pivot(0.0F, 0.0F, 2.0F));

        TexturedModelData texturedModelData = TexturedModelData.of(modelData, 256, 256);
        ModelPart root = texturedModelData.createModel();
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!HollowConfig.INSTANCE.dragonWings || entity.isInvisible()) {
            return;
        }

        matrices.push();

        // 1. Position to match body
        this.getContextModel().body.rotate(matrices);
        
        // Translate to upper back
        matrices.translate(0, 0.1, 0.1); 
        
        // Scale down the giant dragon wings to fit a player
        matrices.scale(0.3f, 0.3f, 0.3f);

        // 2. Animate wings (Flapping math)
        float flapSpeed = entity.isOnGround() ? 0.05F : 0.4F;
        if (entity.isSprinting()) flapSpeed = 0.2F;
        
        float flapAngle = (float) Math.sin(animationProgress * flapSpeed) * 0.5F;
        
        if (!entity.isOnGround() && entity.getVelocity().y < 0) { 
            // Gliding pose when falling
            flapAngle = -0.5F; 
        }

        this.leftWing.yaw = -0.3F + flapAngle;
        this.rightWing.yaw = 0.3F - flapAngle;
        
        this.leftWing.pitch = 0.2f;
        this.rightWing.pitch = 0.2f;

        // 3. Render
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(WING_TEXTURE));
        this.leftWing.render(matrices, vertexConsumer, light, 65536);
        this.rightWing.render(matrices, vertexConsumer, light, 65536);

        matrices.pop();
    }
}
