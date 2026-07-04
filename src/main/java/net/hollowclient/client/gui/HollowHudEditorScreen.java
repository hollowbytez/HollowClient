package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.hollowclient.client.config.HollowHudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HollowHudEditorScreen extends Screen {
    private final Screen parent;
    
    // Bounding Box definitions for HUD elements
    private static class HudElement {
        String name;
        java.util.function.IntSupplier getX;
        java.util.function.IntConsumer setX;
        java.util.function.IntSupplier getY;
        java.util.function.IntConsumer setY;
        java.util.function.BooleanSupplier isEnabled;
        java.util.function.DoubleSupplier getScale;
        java.util.function.DoubleConsumer setScale;
        int width;
        int height;

        HudElement(String name, java.util.function.IntSupplier getX, java.util.function.IntConsumer setX,
                   java.util.function.IntSupplier getY, java.util.function.IntConsumer setY,
                   java.util.function.DoubleSupplier getScale, java.util.function.DoubleConsumer setScale,
                   java.util.function.BooleanSupplier isEnabled, int width, int height) {
            this.name = name;
            this.getX = getX;
            this.setX = setX;
            this.getY = getY;
            this.setY = setY;
            this.getScale = getScale;
            this.setScale = setScale;
            this.isEnabled = isEnabled;
            this.width = width;
            this.height = height;
        }
    }
    
    private HudElement[] elements;
    private HudElement draggedElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HollowHudEditorScreen(Screen parent) {
        super(Text.of("HUD Layout Editor"));
        this.parent = parent;
    }
    
    private HudElement createElem(String name, int defWidth, int defHeight) {
        return new HudElement(name,
            () -> (int) HollowHudConfig.get(name).x,
            (x) -> HollowHudConfig.get(name).x = x,
            () -> (int) HollowHudConfig.get(name).y,
            (y) -> HollowHudConfig.get(name).y = y,
            () -> HollowHudConfig.get(name).scale,
            (s) -> HollowHudConfig.get(name).scale = (float) s,
            () -> HollowHudConfig.get(name).enabled,
            defWidth, defHeight);
    }

    @Override
    protected void init() {
        elements = new HudElement[]{
            createElem("Keystrokes", 60, 60),
            createElem("Armor HUD", 90, 22),
            createElem("Potion HUD", 100, 40),
            createElem("Coordinates", 120, 26),
            createElem("Direction HUD", 160, 20),
            createElem("FPS HUD", 50, 14),
            createElem("Combo Display", 65, 14),
            createElem("Ping Display", 60, 14),
            createElem("Playtime", 100, 14),
            createElem("Reach Display", 70, 14),
            createElem("Server Address", 110, 14),
            createElem("Speed Meter", 85, 14),
            createElem("Stopwatch", 95, 14),
            createElem("Item Counter", 65, 14),
            createElem("Pack Display", 110, 14),
            createElem("Target HUD", 120, 36),
            createElem("Totem Counter", 65, 14),
            createElem("Saturation HUD", 80, 14),
            createElem("Armor Bar", 100, 20),
            createElem("Armor Status", 80, 60),
            createElem("Boss Bar", 140, 20),
            createElem("Hearts", 100, 20),
            createElem("Scoreboard", 100, 100)
        };
        
        // Return button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Exit"), button -> {
            if (this.client != null) {
                this.client.setScreen(parent);
            }
        }).dimensions(this.width / 2 - 60, this.height - 30, 120, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Translucent dark background overlay so game is visible underneath
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        // Render actual HUD elements as a preview!
        net.hollowclient.client.hud.HollowHudRenderer.renderHUD(context, delta);
        
        // Draw grid and hints
        context.drawCenteredTextWithShadow(this.textRenderer, "HUD Layout Editor", this.width / 2, 10, 0xFF9C27B0);
        context.drawCenteredTextWithShadow(this.textRenderer, "Drag boxes to reposition them. Touch-friendly for PojavLauncher.", this.width / 2, 22, 0xFF888888);
        
        for (HudElement el : elements) {
            boolean enabled = el.isEnabled.getAsBoolean();
            
            int x = el.getX.getAsInt();
            int y = el.getY.getAsInt();
            double scale = el.getScale.getAsDouble();
            int w = (int) (el.width * scale);
            int h = (int) (el.height * scale);
            
            boolean isHovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean isDragged = el == draggedElement;
            
            // Render bounding box with active status colors
            int borderColor = isDragged ? 0xFF9C27B0 : (isHovered ? 0xFFBA68C8 : (enabled ? 0xAA777777 : 0xAAFF5555));
            int boxBgColor = isDragged ? 0x409C27B0 : (isHovered ? 0x20BA68C8 : (enabled ? 0x10FFFFFF : 0x40FF0000));
            
            context.fill(x, y, x + w, y + h, boxBgColor);
            context.drawBorder(x, y, w, h, borderColor);
            
            // Element label inside the box
            context.getMatrices().push();
            context.getMatrices().translate(x + 3, y + 3, 0);
            context.getMatrices().scale((float)Math.min(1.0, scale), (float)Math.min(1.0, scale), 1f);
            context.drawText(this.textRenderer, el.name, 0, 0, 0xFFFFFFFF, true);
            context.getMatrices().pop();
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing to prevent background blur and allow clear view of the world
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (HudElement el : elements) {
            // Allow dragging even if disabled so users can arrange their HUD freely
            
            int x = el.getX.getAsInt();
            int y = el.getY.getAsInt();
            double scale = el.getScale.getAsDouble();
            int w = (int) (el.width * scale);
            int h = (int) (el.height * scale);
            
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                draggedElement = el;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggedElement != null) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;
            
            // Screen clamping / boundaries
            int maxW = this.width - draggedElement.width;
            int maxH = this.height - draggedElement.height;
            
            if (newX < 0) newX = 0;
            if (newX > maxW) newX = maxW;
            if (newY < 0) newY = 0;
            if (newY > maxH) newY = maxH;
            
            // Edge snapping (10 pixels snap zone)
            if (newX < 10) newX = 0;
            if (Math.abs(newX - maxW) < 10) newX = maxW;
            if (newY < 10) newY = 0;
            if (Math.abs(newY - maxH) < 10) newY = maxH;
            
            // Center snapping
            int centerX = this.width / 2 - draggedElement.width / 2;
            if (Math.abs(newX - centerX) < 10) newX = centerX;
            
            draggedElement.setX.accept(newX);
            draggedElement.setY.accept(newY);
            
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedElement != null) {
            draggedElement = null;
            HollowHudConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        for (HudElement el : elements) {
            // Allow scrolling scale even if disabled

            int x = el.getX.getAsInt();
            int y = el.getY.getAsInt();
            double scale = el.getScale.getAsDouble();
            int w = (int) (el.width * scale);
            int h = (int) (el.height * scale);

            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                double newScale = scale + (vAmt * 0.1);
                if (newScale < 0.5) newScale = 0.5;
                if (newScale > 3.0) newScale = 3.0;
                el.setScale.accept(newScale);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    @Override
    public void close() {
        HollowHudConfig.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}

