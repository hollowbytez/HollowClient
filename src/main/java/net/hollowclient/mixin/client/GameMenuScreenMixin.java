package net.hollowclient.mixin.client;

import net.fabricmc.loader.api.FabricLoader;
import net.hollowclient.client.gui.HollowSettingsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addHollowButtons(CallbackInfo ci) {
        boolean hasModMenu = FabricLoader.getInstance().isModLoaded("modmenu");
        
        int btnWidth = hasModMenu ? 98 : 200;
        int startX = this.width - 204;
        int startY = this.height - 24;
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Hollow Setting"), (button) -> {
            if (this.client != null) this.client.setScreen(new HollowSettingsScreen(this));
        }).dimensions(startX, startY, btnWidth, 20).build());

        if (hasModMenu) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Mods"), (button) -> {
                try {
                    Class<?> screenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    Screen modsScreen = (Screen) screenClass.getConstructor(Screen.class).newInstance(this);
                    if (this.client != null) this.client.setScreen(modsScreen);
                } catch (Exception e) {}
            }).dimensions(startX + 102, startY, btnWidth, 20).build());
        }
    }
}

