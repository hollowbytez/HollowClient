package net.hollowclient.client.gui;

import net.hollowclient.client.HollowClient;
import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HollowZoomScreen extends Screen {
    private final Screen parent;
    private boolean listening = false;
    private ButtonWidget bindButton;

    public HollowZoomScreen(Screen parent) {
        super(Text.of("Zoom Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Keybind configuration
        bindButton = ButtonWidget.builder(Text.of(getKeyNameText(HollowClient.zoomKey)), button -> {
            listening = true;
            button.setMessage(Text.of("Press any key..."));
        }).dimensions(cx - 100, cy - 30, 200, 20).build();

        this.addDrawableChild(bindButton);

        // Magnification factors
        this.addDrawableChild(ButtonWidget.builder(Text.of("Magnification +"), button -> adjustMag(1.0f)).dimensions(cx - 100, cy - 5, 95, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Magnification -"), button -> adjustMag(-1.0f)).dimensions(cx + 5, cy - 5, 95, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 50, cy + 30, 100, 20).build());
    }

    private String getKeyNameText(KeyBinding keyBinding) {
        return "Keybind: " + keyBinding.getBoundKeyTranslationKey().replace("key.keyboard.", "").toUpperCase();
    }

    private void adjustMag(float val) {
        HollowConfig.INSTANCE.zoomMagnification = Math.max(2.0f, Math.min(10.0f, HollowConfig.INSTANCE.zoomMagnification + val));
        HollowConfig.save();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            listening = false;
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                HollowClient.zoomKey.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
                if (this.client != null && this.client.options != null) {
                    this.client.options.write();
                }
            }
            bindButton.setMessage(Text.of(getKeyNameText(HollowClient.zoomKey)));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "ZOOM CONFIGURATION", this.width / 2, this.height / 2 - 70, 0xFFFFFFFF);
        
        String desc = String.format("Current Magnification: %.1fx", HollowConfig.INSTANCE.zoomMagnification);
        context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, this.height / 2 - 50, 0xFFBA68C8);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}


