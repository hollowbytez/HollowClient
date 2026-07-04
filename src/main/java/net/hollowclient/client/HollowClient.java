package net.hollowclient.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.hollowclient.client.config.HollowConfig;
import net.hollowclient.client.gui.HollowSettingsScreen;
import net.hollowclient.client.hud.HollowHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HollowClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("HollowClient");
    public static KeyBinding openSettingsKey;
    public static KeyBinding openHudEditorKey;
    public static KeyBinding zoomKey;
    public static KeyBinding freelookKey;
    public static KeyBinding stopwatchKey;
    public static KeyBinding autoTextKey;
    public static KeyBinding toggleSprintKey;
    public static double originalGamma = -1.0;

    // Zoom state
    public static boolean isZooming = false;
    public static float zoomLevel = 1.0f;
    public static float targetZoomLevel = 1.0f;

    // Freelook state
    public static boolean isFreelooking = false;
    public static float freelookYaw = 0.0f;
    public static float freelookPitch = 0.0f;
    public static net.minecraft.client.option.Perspective originalPerspective = net.minecraft.client.option.Perspective.FIRST_PERSON;

    @Override
    public void onInitializeClient() {
        // Load configurations
        HollowConfig.load();

        // Cull Logs on Startup
        if (HollowConfig.INSTANCE.cullLogs) {
            try {
                File logsDir = new File(MinecraftClient.getInstance().runDirectory, "logs");
                if (logsDir.exists() && logsDir.isDirectory()) {
                    File[] files = logsDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".log") || file.getName().endsWith(".log.gz")) {
                                file.delete();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Keybindings Registration
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.hollowclient"
        ));

        openHudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                "category.hollowclient"
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.hollowclient"
        ));

        freelookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.freelook",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.hollowclient"
        ));

        stopwatchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.stopwatch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.hollowclient"
        ));

        autoTextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.autotext",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.hollowclient"
        ));

        toggleSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hollowclient.togglesprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.hollowclient"
        ));

        // Register HUD Render Callback
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            HollowHudRenderer.renderHUD(context, tickCounter.getTickDelta(false));
        });

        // Register Attack Entity Callback (for Combo & Reach tracker)
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                HollowHudRenderer.onHit();
                HollowHudRenderer.onAttackEntity(entity);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        // Client tick event handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Handle Hitbox outlines toggle in real-time
            if (client.getEntityRenderDispatcher() != null) {
                client.getEntityRenderDispatcher().setRenderHitboxes(HollowConfig.INSTANCE.hitbox);
            }

            // Handle Menu keybind
            if (openSettingsKey.wasPressed()) {
                client.setScreen(new HollowSettingsScreen(null));
            }

            // Handle HUD Editor keybind
            if (openHudEditorKey.wasPressed()) {
                client.setScreen(new net.hollowclient.client.gui.HollowHudEditorScreen(null));
            }

            // Handle Stopwatch keybind
            if (stopwatchKey.wasPressed() && HollowConfig.INSTANCE.stopwatch) {
                HollowHudRenderer.toggleStopwatch();
            }

            // Handle Auto Text macro keybind (with click consumption)
            if (HollowConfig.INSTANCE.autoText) {
                while (autoTextKey.wasPressed()) {
                    if (client.player.networkHandler != null) {
                        client.player.networkHandler.sendChatMessage(HollowConfig.INSTANCE.autoTextCommand);
                        client.player.sendMessage(Text.of("§a[Hollow] Executed Auto Text Macro (" + HollowConfig.INSTANCE.autoTextCommand + ")"), true);
                    }
                }
            }

            // Handle Toggle Sprint keypress
            while (toggleSprintKey.wasPressed()) {
                HollowConfig.INSTANCE.toggleSprint = !HollowConfig.INSTANCE.toggleSprint;
                HollowConfig.save();
                client.player.sendMessage(Text.of("§a[Hollow] Toggle Sprint: " + (HollowConfig.INSTANCE.toggleSprint ? "§2Enabled" : "§4Disabled")), true);
            }

            // Handle Zoom status
            if (HollowConfig.INSTANCE.zoom) {
                isZooming = zoomKey.isPressed();
            } else {
                isZooming = false;
            }

            // Handle Freelook status
            if (HollowConfig.INSTANCE.freelook) {
                if (freelookKey.isPressed()) {
                    if (!isFreelooking) {
                        isFreelooking = true;
                        freelookYaw = client.player.getYaw();
                        freelookPitch = client.player.getPitch();
                        if (client.options != null) {
                            originalPerspective = client.options.getPerspective();
                            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                        }
                    }
                } else {
                    if (isFreelooking) {
                        isFreelooking = false;
                        if (client.options != null) {
                            client.options.setPerspective(originalPerspective);
                        }
                    }
                }
            } else {
                if (isFreelooking) {
                    isFreelooking = false;
                    if (client.options != null) {
                        client.options.setPerspective(originalPerspective);
                    }
                }
            }

            // Force lightmap update so Fullbright applies instantly in-game
            if (client.gameRenderer != null && client.gameRenderer.getLightmapTextureManager() != null) {
                ((net.hollowclient.mixin.client.LightmapTextureManagerAccessor) client.gameRenderer.getLightmapTextureManager()).setDirty(true);
            }
        });

        // Register client command /Hollow for mobile players on PojavLauncher
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("Hollow")
                .executes(context -> {
                    context.getSource().getClient().execute(() -> {
                        context.getSource().getClient().setScreen(new HollowSettingsScreen(null));
                    });
                    context.getSource().sendFeedback(Text.of("Opening Hollow Settings..."));
                    return 1;
                })
            );
        });
    }
}

