package net.hollowclient.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    // Overwrite the background rendering with a sleek gradient
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        
        // Only apply to HollowClient menus, leave Minecraft vanilla screens alone!
        if (self.getClass().getName().startsWith("net.hollowclient.client.gui.")) {
            // Extract alpha from background color instead of theme color
            int bgColor = net.hollowclient.client.config.HollowConfig.INSTANCE.backgroundColor;
            int alpha = (bgColor >> 24) & 0xFF;
            
            // Draw a subtle gradient for our custom menus
            int color1 = (alpha << 24); // Solid black with configurable alpha
            int color2 = (Math.min(255, alpha + 32) << 24);
            context.fillGradient(0, 0, self.width, self.height, color1, color2);
            ci.cancel();
        }
    }
}

