package net.hollowclient.mixin.client;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.currentScreen == null) {
            // Drop Prevention (action == 1 is key press)
            if (HollowConfig.INSTANCE.dropPrevention && action == 1 && client.options.dropKey.matchesKey(key, scancode)) {
                net.minecraft.item.ItemStack held = client.player.getMainHandStack();
                if (!held.isEmpty() && (held.getItem() instanceof net.minecraft.item.SwordItem || held.getItem() instanceof net.minecraft.item.MiningToolItem)) {
                    client.player.sendMessage(Text.of("§c[Hollow] Drop prevented to protect your tool!"), true);
                    ci.cancel();
                    return;
                }
            }


        }
    }
}

