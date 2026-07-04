package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.hollowclient.client.gui.HollowHomeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public abstract void setScreen(Screen screen);
    @Shadow protected abstract boolean doAttack();

    @Unique
    private int autoClickerTicks = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(net.minecraft.client.RunArgs args, CallbackInfo ci) {
        // Fix white flash at startup by forcing clear color to black
        com.mojang.blaze3d.systems.RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // Prevent infinite loops and safely inject our custom Home Screen
        if (screen != null && screen.getClass() == TitleScreen.class) {
            this.setScreen(new net.hollowclient.client.gui.HollowHomeScreen());
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player == null || client.currentScreen != null) return;
        
        if (HollowConfig.INSTANCE.autoClicker && client.options.attackKey.isPressed()) {
            if (autoClickerTicks <= 0) {
                this.doAttack();
                int minCPS = Math.max(1, HollowConfig.INSTANCE.autoClickerMinCPS);
                int maxCPS = Math.max(minCPS, HollowConfig.INSTANCE.autoClickerMaxCPS);
                int randomCPS = minCPS + (int)(Math.random() * ((maxCPS - minCPS) + 1));
                autoClickerTicks = 20 / randomCPS;
            } else {
                autoClickerTicks--;
            }
        } else {
            autoClickerTicks = 0;
        }
    }
}

