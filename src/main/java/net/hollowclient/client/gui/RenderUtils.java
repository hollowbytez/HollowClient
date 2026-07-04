package net.hollowclient.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class RenderUtils {
    private static Identifier CIRCLE_TEX = null;

    private static void initCircleTexture() {
        if (CIRCLE_TEX != null) return;
        int size = 128;
        NativeImage img = new NativeImage(size, size, true);
        float center = size / 2f;
        float radius = center - 1f; // Leave 1px border for anti-aliasing fading
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center + 0.5f;
                float dy = y - center + 0.5f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                
                float alpha = 1f;
                if (dist > radius) alpha = 0f;
                else if (dist > radius - 1f) alpha = radius - dist; // Smooth 1px anti-aliasing
                
                int a = (int)(alpha * 255f);
                // Set pixel (NativeImage uses ABGR format! white pixel with varying alpha)
                img.setColor(x, y, (a << 24) | 0x00FFFFFF);
            }
        }
        
        NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
        CIRCLE_TEX = Identifier.of("hollowclient", "circle_cache");
        MinecraftClient.getInstance().getTextureManager().registerTexture(CIRCLE_TEX, tex);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        initCircleTexture();
        
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        // 1. Draw solid body rectangles using DrawContext-like QUADS
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // Center block
        fillQuad(bufferbuilder, matrix, x + radius, y, width - radius * 2, height, color);
        // Left block
        fillQuad(bufferbuilder, matrix, x, y + radius, radius, height - radius * 2, color);
        // Right block
        fillQuad(bufferbuilder, matrix, x + width - radius, y + radius, radius, height - radius * 2, color);
        
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        // 2. Draw the 4 anti-aliased corners using the texture
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, CIRCLE_TEX);
        bufferbuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        
        // Top-Left (uv: 0,0 to 0.5,0.5)
        drawTexQuad(bufferbuilder, matrix, x, y, radius, radius, 0f, 0f, 0.5f, 0.5f, r, g, b, a);
        // Top-Right (uv: 0.5,0 to 1,0.5)
        drawTexQuad(bufferbuilder, matrix, x + width - radius, y, radius, radius, 0.5f, 0f, 1f, 0.5f, r, g, b, a);
        // Bottom-Right (uv: 0.5,0.5 to 1,1)
        drawTexQuad(bufferbuilder, matrix, x + width - radius, y + height - radius, radius, radius, 0.5f, 0.5f, 1f, 1f, r, g, b, a);
        // Bottom-Left (uv: 0,0.5 to 0.5,1)
        drawTexQuad(bufferbuilder, matrix, x, y + height - radius, radius, radius, 0f, 0.5f, 0.5f, 1f, r, g, b, a);
        
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void fillQuad(BufferBuilder b, Matrix4f m, float x, float y, float w, float h, int c) {
        b.vertex(m, x, y, 0).color(c);
        b.vertex(m, x, y + h, 0).color(c);
        b.vertex(m, x + w, y + h, 0).color(c);
        b.vertex(m, x + w, y, 0).color(c);
    }

    private static void drawTexQuad(BufferBuilder b, Matrix4f m, float x, float y, float w, float h, float u1, float v1, float u2, float v2, float r, float g, float blue, float a) {
        b.vertex(m, x, y, 0).texture(u1, v1).color(r, g, blue, a);
        b.vertex(m, x, y + h, 0).texture(u1, v2).color(r, g, blue, a);
        b.vertex(m, x + w, y + h, 0).texture(u2, v2).color(r, g, blue, a);
        b.vertex(m, x + w, y, 0).texture(u2, v1).color(r, g, blue, a);
    }

    public static void drawRoundedOutline(MatrixStack matrices, float x, float y, float width, float height, float radius, float lineWidth, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // Top edge
        fillQuad(bufferbuilder, matrix, x + radius, y, width - radius * 2, lineWidth, color);
        // Bottom edge
        fillQuad(bufferbuilder, matrix, x + radius, y + height - lineWidth, width - radius * 2, lineWidth, color);
        // Left edge
        fillQuad(bufferbuilder, matrix, x, y + radius, lineWidth, height - radius * 2, color);
        // Right edge
        fillQuad(bufferbuilder, matrix, x + width - lineWidth, y + radius, lineWidth, height - radius * 2, color);
        
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        // Draw the 4 corners as thick arcs using QUADS
        drawCornerOutline(matrix, x + radius, y + radius, radius, lineWidth, 180, 270, color);
        drawCornerOutline(matrix, x + width - radius, y + radius, radius, lineWidth, 270, 360, color);
        drawCornerOutline(matrix, x + width - radius, y + height - radius, radius, lineWidth, 0, 90, color);
        drawCornerOutline(matrix, x + radius, y + height - radius, radius, lineWidth, 90, 180, color);
        
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawCornerOutline(Matrix4f matrix, float cx, float cy, float r, float lw, int startAngle, int endAngle, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        float lastOuterX = cx + (float) Math.cos(Math.toRadians(startAngle)) * r;
        float lastOuterY = cy + (float) Math.sin(Math.toRadians(startAngle)) * r;
        float lastInnerX = cx + (float) Math.cos(Math.toRadians(startAngle)) * (r - lw);
        float lastInnerY = cy + (float) Math.sin(Math.toRadians(startAngle)) * (r - lw);

        for (int i = startAngle + 5; i <= endAngle; i += 5) {
            float rad = (float) Math.toRadians(i);
            float outerX = cx + (float) Math.cos(rad) * r;
            float outerY = cy + (float) Math.sin(rad) * r;
            float innerX = cx + (float) Math.cos(rad) * (r - lw);
            float innerY = cy + (float) Math.sin(rad) * (r - lw);
            
            bufferbuilder.vertex(matrix, lastOuterX, lastOuterY, 0).color(color);
            bufferbuilder.vertex(matrix, lastInnerX, lastInnerY, 0).color(color);
            bufferbuilder.vertex(matrix, innerX, innerY, 0).color(color);
            bufferbuilder.vertex(matrix, outerX, outerY, 0).color(color);
            
            lastOuterX = outerX;
            lastOuterY = outerY;
            lastInnerX = innerX;
            lastInnerY = innerY;
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
    }
}

