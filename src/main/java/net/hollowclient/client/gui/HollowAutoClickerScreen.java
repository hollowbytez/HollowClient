package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HollowAutoClickerScreen extends Screen {
    private final Screen parent;
    private SliderWidget minSlider;
    private SliderWidget maxSlider;

    public HollowAutoClickerScreen(Screen parent) {
        super(Text.of("AutoClicker Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = this.height / 2 - 50;

        minSlider = new SliderWidget(centerX - 100, topY, 200, 20, Text.empty(), (HollowConfig.INSTANCE.autoClickerMinCPS - 1) / 19.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Min CPS: " + getVal()));
            }

            @Override
            protected void applyValue() {
                int val = 1 + (int)(this.value * 19);
                HollowConfig.INSTANCE.autoClickerMinCPS = val;
                HollowConfig.save();
            }

            public int getVal() { return 1 + (int)(this.value * 19); }
            public void setVal(int val) { this.value = (val - 1) / 19.0; this.updateMessage(); }
        };
        
        maxSlider = new SliderWidget(centerX - 100, topY + 30, 200, 20, Text.empty(), (HollowConfig.INSTANCE.autoClickerMaxCPS - 1) / 19.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of("Max CPS: " + getVal()));
            }

            @Override
            protected void applyValue() {
                int val = 1 + (int)(this.value * 19);
                HollowConfig.INSTANCE.autoClickerMaxCPS = val;
                HollowConfig.save();
            }

            public int getVal() { return 1 + (int)(this.value * 19); }
            public void setVal(int val) { this.value = (val - 1) / 19.0; this.updateMessage(); }
        };

        this.addDrawableChild(minSlider);
        this.addDrawableChild(maxSlider);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), btn -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(centerX - 100, topY + 70, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "AutoClicker Customization", this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
