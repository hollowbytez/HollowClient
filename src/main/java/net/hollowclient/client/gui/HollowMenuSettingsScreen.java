package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HollowMenuSettingsScreen extends Screen {
    private final Screen parent;
    
    // Config state for the UI theme
    public static int themeColor = 0xFFEB4040;
    public static int cardBgColor = 0xE61F2026;
    public static float menuScale = 1.0f; // Multiplier for panel dimensions

    private SliderWidget rSlider, gSlider, bSlider, aSlider;

    public HollowMenuSettingsScreen(Screen parent) {
        super(Text.of("Menu Settings"));
        this.parent = parent;
        
        // Load from config
        themeColor = HollowConfig.INSTANCE.themeColor;
        menuScale = HollowConfig.INSTANCE.menuScale;
    }

    private void updateColorFromSliders(double r, double g, double b, double a) {
        int rI = (int) (r * 255);
        int gI = (int) (g * 255);
        int bI = (int) (b * 255);
        int aI = (int) (a * 255);
        themeColor = (aI << 24) | (rI << 16) | (gI << 8) | bI;
        HollowConfig.INSTANCE.themeColor = themeColor;
        HollowConfig.save();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = this.height / 4;

        // Presets
        this.addDrawableChild(ButtonWidget.builder(Text.of("Theme: Solid Red"), btn -> {
            themeColor = 0xFFEB4040;
            cardBgColor = 0xE61F2026;
            HollowConfig.INSTANCE.themeColor = themeColor;
            HollowConfig.save();
            if (this.client != null) this.client.setScreen(new HollowMenuSettingsScreen(parent));
        }).dimensions(centerX - 100, topY, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Theme: Dark Mode"), btn -> {
            themeColor = 0xFF555555;
            cardBgColor = 0xFF141414;
            HollowConfig.INSTANCE.themeColor = themeColor;
            HollowConfig.save();
            if (this.client != null) this.client.setScreen(new HollowMenuSettingsScreen(parent));
        }).dimensions(centerX - 100, topY + 25, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Preset: Vibrant Aqua"), btn -> {
            themeColor = 0xFF00FFCC;
            cardBgColor = 0xFF112233;
            HollowConfig.INSTANCE.themeColor = themeColor;
            HollowConfig.save();
            if (this.client != null) this.client.setScreen(new HollowMenuSettingsScreen(parent));
        }).dimensions(centerX - 100, topY + 50, 200, 20).build());

        // RGB Custom Color Pad
        rSlider = new SliderWidget(centerX - 120, topY + 75, 55, 20, Text.empty(), ((themeColor >> 16) & 0xFF) / 255.0) {
            @Override protected void updateMessage() { this.setMessage(Text.of("R: " + (int)(this.value * 255))); }
            @Override protected void applyValue() { updateColorFromSliders(this.value, gSlider != null ? ((themeColor >> 8) & 0xFF) / 255.0 : 0, bSlider != null ? (themeColor & 0xFF) / 255.0 : 0, aSlider != null ? ((themeColor >> 24) & 0xFF) / 255.0 : 1); }
        };
        gSlider = new SliderWidget(centerX - 60, topY + 75, 55, 20, Text.empty(), ((themeColor >> 8) & 0xFF) / 255.0) {
            @Override protected void updateMessage() { this.setMessage(Text.of("G: " + (int)(this.value * 255))); }
            @Override protected void applyValue() { updateColorFromSliders(rSlider != null ? ((themeColor >> 16) & 0xFF) / 255.0 : 0, this.value, bSlider != null ? (themeColor & 0xFF) / 255.0 : 0, aSlider != null ? ((themeColor >> 24) & 0xFF) / 255.0 : 1); }
        };
        bSlider = new SliderWidget(centerX, topY + 75, 55, 20, Text.empty(), (themeColor & 0xFF) / 255.0) {
            @Override protected void updateMessage() { this.setMessage(Text.of("B: " + (int)(this.value * 255))); }
            @Override protected void applyValue() { updateColorFromSliders(rSlider != null ? ((themeColor >> 16) & 0xFF) / 255.0 : 0, gSlider != null ? ((themeColor >> 8) & 0xFF) / 255.0 : 0, this.value, aSlider != null ? ((themeColor >> 24) & 0xFF) / 255.0 : 1); }
        };
        aSlider = new SliderWidget(centerX + 60, topY + 75, 55, 20, Text.empty(), ((themeColor >> 24) & 0xFF) / 255.0) {
            @Override protected void updateMessage() { this.setMessage(Text.of("A: " + (int)(this.value * 255))); }
            @Override protected void applyValue() { updateColorFromSliders(rSlider != null ? ((themeColor >> 16) & 0xFF) / 255.0 : 0, gSlider != null ? ((themeColor >> 8) & 0xFF) / 255.0 : 0, bSlider != null ? (themeColor & 0xFF) / 255.0 : 0, this.value); }
        };
        this.addDrawableChild(rSlider);
        this.addDrawableChild(gSlider);
        this.addDrawableChild(bSlider);
        this.addDrawableChild(aSlider);

        // Size Button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Menu Size: " + (menuScale == 1.0f ? "Normal" : (menuScale > 1.0f ? "Large" : "Small"))), btn -> {
            if (menuScale == 1.0f) { menuScale = 1.25f; btn.setMessage(Text.of("Menu Size: Large")); }
            else if (menuScale > 1.0f) { menuScale = 0.85f; btn.setMessage(Text.of("Menu Size: Small")); }
            else { menuScale = 1.0f; btn.setMessage(Text.of("Menu Size: Normal")); }
            
            HollowConfig.INSTANCE.menuScale = menuScale;
            HollowConfig.save();
            
            if (parent instanceof HollowSettingsScreen) {
                parent.init(this.client, this.width, this.height);
            }
        }).dimensions(centerX - 100, topY + 100, 200, 20).build());

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), btn -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(centerX - 100, topY + 140, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Menu Theme & Scale Settings", this.width / 2, this.height / 4 - 30, themeColor);
        context.drawCenteredTextWithShadow(this.textRenderer, "Changes apply automatically.", this.width / 2, this.height / 4 - 15, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}

