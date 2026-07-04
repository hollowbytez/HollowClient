package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.hollowclient.client.config.HollowHudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HollowProfilesScreen extends Screen {
    private final Screen parent;
    private String statusMessage = "";
    private int statusTimer = 0;

    public HollowProfilesScreen(Screen parent) {
        super(Text.of("Config Profiles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = this.height / 4;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Save Current Layout & Settings"), btn -> {
            HollowConfig.save();
            HollowHudConfig.save();
            statusMessage = "Successfully saved to disk!";
            statusTimer = 60;
        }).dimensions(centerX - 100, topY, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Reload From Disk"), btn -> {
            HollowConfig.load();
            HollowHudConfig.load();
            statusMessage = "Settings reloaded!";
            statusTimer = 60;
        }).dimensions(centerX - 100, topY + 25, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), btn -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(centerX - 100, topY + 80, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Config Profiles Management", this.width / 2, this.height / 4 - 30, HollowMenuSettingsScreen.themeColor);
        
        if (statusTimer > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, statusMessage, this.width / 2, this.height / 4 - 15, 0xFF00FF00);
            statusTimer--;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
}

