package net.hollowclient.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HollowKeybindSettingScreen extends Screen {
    private final Screen parent;
    private final String modName;
    private final KeyBinding keyBinding;
    private boolean listening = false;
    private ButtonWidget bindButton;

    public HollowKeybindSettingScreen(Screen parent, String modName, KeyBinding keyBinding) {
        super(Text.of(modName + " Keybind Settings"));
        this.parent = parent;
        this.modName = modName;
        this.keyBinding = keyBinding;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        bindButton = ButtonWidget.builder(Text.of(getKeyNameText()), button -> {
            listening = true;
            button.setMessage(Text.of("Press any key..."));
        }).dimensions(cx - 100, cy - 20, 200, 20).build();

        this.addDrawableChild(bindButton);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 50, cy + 30, 100, 20).build());
    }

    private String getKeyNameText() {
        return "Keybind: " + keyBinding.getBoundKeyTranslationKey().replace("key.keyboard.", "").toUpperCase();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            listening = false;
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                keyBinding.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
                if (this.client != null && this.client.options != null) {
                    this.client.options.write();
                }
            }
            bindButton.setMessage(Text.of(getKeyNameText()));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, modName.toUpperCase() + " SETTINGS", this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Click the button below to change the hotkey.", this.width / 2, this.height / 2 - 45, 0xFFBA68C8);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

