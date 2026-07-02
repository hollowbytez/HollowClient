package net.featherpojav.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.featherpojav.client.config.FeatherConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FeatherHomeScreen extends Screen {
    public static final CubeMapRenderer CUSTOM_PANORAMA = new CubeMapRenderer(Identifier.of("featherpojav", "background/panorama"));
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(CUSTOM_PANORAMA);

    private final List<MenuButton> buttons = new ArrayList<>();
    private final List<IconWidget> topIcons = new ArrayList<>();

    public FeatherHomeScreen() {
        super(Text.of("Feather Home Screen"));
    }

    private static class MenuButton {
        String icon;
        String label;
        boolean isStore;
        Runnable onClick;

        MenuButton(String icon, String label, boolean isStore, Runnable onClick) {
            this.icon = icon;
            this.label = label;
            this.isStore = isStore;
            this.onClick = onClick;
        }
    }

    private static class IconWidget {
        String icon;
        String tooltip;
        Runnable onClick;

        IconWidget(String icon, String tooltip, Runnable onClick) {
            this.icon = icon;
            this.tooltip = tooltip;
            this.onClick = onClick;
        }
    }

    @Override
    protected void init() {
        buttons.clear();
        topIcons.clear();

        // 1. Singleplayer
        buttons.add(new MenuButton("👤", "Singleplayer", false, () -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }));
        
        // 2. Multiplayer
        buttons.add(new MenuButton("👥", "Multiplayer", false, () -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }));

        // 3. Mods (only if Mod Menu is loaded by the user)
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            buttons.add(new MenuButton("🧩", "Mods", false, () -> {
                try {
                    Class<?> screenClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    Screen modsScreen = (Screen) screenClass.getConstructor(Screen.class).newInstance(this);
                    if (this.client != null) this.client.setScreen(modsScreen);
                } catch (Exception e) {
                    if (this.client != null) this.client.setScreen(new FeatherSettingsScreen(this));
                }
            }));
        }

        // 4. STORE
        buttons.add(new MenuButton("🛒", "STORE", true, () -> {
            Util.getOperatingSystem().open("https://store.feather.gg");
        }));

        // Top Right Icon Shortcuts
        topIcons.add(new IconWidget("👁", "Toggle HUD", () -> {
            if (this.client != null) this.client.options.hudHidden = !this.client.options.hudHidden;
        }));
        topIcons.add(new IconWidget("📁", "Screenshots", () -> {
            if (this.client != null) {
                File screenshots = new File(this.client.runDirectory, "screenshots");
                if (!screenshots.exists()) screenshots.mkdirs();
                Util.getOperatingSystem().open(screenshots);
            }
        }));
        topIcons.add(new IconWidget("⚙", "Settings", () -> {
            if (this.client != null) this.client.setScreen(new FeatherSettingsScreen(this));
        }));
        topIcons.add(new IconWidget("💬", "Social Feed", () -> {
            Util.getOperatingSystem().open("https://feather.gg");
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render 3D rotating custom panorama
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);

        // Dark grey atmospheric overlay for visual contrast (D0 is 80% opacity)
        context.fill(0, 0, this.width, this.height, 0xD0101012);

        // --- Render Center Header & Brand ---
        int centerY = this.height / 2 - 110;
        Identifier logoId = Identifier.of("featherpojav", "icon.png");
        context.drawTexture(logoId, this.width / 2 - 60, centerY, 0.0f, 0.0f, 20, 20, 96, 96);
        context.drawText(this.textRenderer, "FEATHER CLIENT", this.width / 2 - 35, centerY + 6, 0xFFFFFFFF, true);

        // --- Render Center Main Buttons ---
        int buttonY = centerY + 30;
        int buttonWidth = 190;
        int buttonHeight = 24;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : buttons) {
            boolean hovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

            int backgroundCol;
            int borderCol;
            int textCol = 0xFFFFFFFF;

            if (btn.isStore) {
                backgroundCol = hovered ? 0xFFE53935 : 0xFFD32F2F;
                borderCol = 0xFFB71C1C;
            } else {
                backgroundCol = hovered ? 0xFF2A2A2E : 0xFF141416;
                borderCol = hovered ? 0x80FFFFFF : 0x20FFFFFF;
            }

            context.fill(leftX, buttonY, leftX + buttonWidth, buttonY + buttonHeight, backgroundCol);
            context.drawBorder(leftX, buttonY, buttonWidth, buttonHeight, borderCol);

            context.drawText(this.textRenderer, btn.icon, leftX + 10, buttonY + 8, textCol, false);
            context.drawText(this.textRenderer, btn.label, leftX + 28, buttonY + 8, textCol, false);

            buttonY += 28;
        }

        // --- Render Centered QUIT GAME Button ---
        int quitY = buttonY + 6;
        boolean quitHovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 22;
        context.fill(leftX, quitY, leftX + buttonWidth, quitY + 22, quitHovered ? 0xFF3E2723 : 0x603E2723);
        context.drawBorder(leftX, quitY, buttonWidth, 22, quitHovered ? 0xFFD84315 : 0x20D84315);
        context.drawCenteredTextWithShadow(this.textRenderer, "QUIT GAME", this.width / 2, quitY + 7, 0xFFE64A19);

        // --- Render Top Right Profile Box & Shortcuts ---
        if (this.client != null && this.client.getSession() != null) {
            int rightX = Math.max(this.width / 2 + 105, this.width - 225);
            int topY = 15;

            // Profile card
            context.fill(rightX, topY, rightX + 110, topY + 28, 0xFF141416);
            context.drawBorder(rightX, topY, 110, 28, 0x20FFFFFF);

            try {
                SkinTextures skinTextures = this.client.getSkinProvider().getSkinTextures(this.client.getGameProfile());
                Identifier skinId = skinTextures.texture();
                context.drawTexture(skinId, rightX + 6, topY + 4, 20, 20, 8.0f, 8.0f, 8, 8, 64, 64);
                context.drawTexture(skinId, rightX + 6, topY + 4, 20, 20, 40.0f, 8.0f, 8, 8, 64, 64);
            } catch (Exception ignored) {}

            String username = this.client.getSession().getUsername();
            if (username.length() > 10) username = username.substring(0, 8) + "..";
            context.drawText(this.textRenderer, username.toUpperCase(), rightX + 32, topY + 10, 0xFFFFFFFF, false);

            // Icon shortcuts
            int iconX = rightX + 115;
            for (IconWidget iconBtn : topIcons) {
                boolean hovered = mouseX >= iconX && mouseX <= iconX + 22 && mouseY >= topY && mouseY <= topY + 28;
                context.fill(iconX, topY, iconX + 22, topY + 28, hovered ? 0xFF2A2A2E : 0xFF141416);
                context.drawBorder(iconX, topY, 22, 28, hovered ? 0x80FFFFFF : 0x20FFFFFF);
                context.drawCenteredTextWithShadow(this.textRenderer, iconBtn.icon, iconX + 11, topY + 10, 0xFFFFFFFF);
                iconX += 26;
            }
        }

        // --- Render Bottom Left Version Info ---
        String versionStr = "Feather fabric-loader-0.17.3-1.21.1 (release/hollowbytez)";
        context.drawText(this.textRenderer, versionStr, 15, this.height - 18, 0x80FFFFFF, false);

        // --- Render Bottom Right GitHub Panel ---
        int adWidth = 140;
        int adHeight = 45;
        int adX = this.width - adWidth - 15;
        int adY = this.height - adHeight - 15;
        
        boolean gitHovered = mouseX >= adX && mouseX <= adX + adWidth && mouseY >= adY && mouseY <= adY + adHeight;
        context.fill(adX, adY, adX + adWidth, adY + adHeight, gitHovered ? 0xFF221133 : 0xFF14121A);
        context.drawBorder(adX, adY, adWidth, adHeight, gitHovered ? 0xFFD050FF : 0xFF9C27B0);
        context.drawText(this.textRenderer, "OWNER GITHUB", adX + 8, adY + 8, 0xFF9C27B0, true);
        context.drawText(this.textRenderer, "github.com/hollowbytez", adX + 8, adY + 22, 0xFFCCCCCC, false);
        context.drawText(this.textRenderer, "Click to visit profile", adX + 8, adY + 32, 0xFF888888, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle Main buttons clicks
        int centerY = this.height / 2 - 110;
        int buttonY = centerY + 30;
        int buttonWidth = 190;
        int buttonHeight = 24;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : buttons) {
            if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                btn.onClick.run();
                return true;
            }
            buttonY += 28;
        }

        // Handle QUIT GAME click
        int quitY = buttonY + 6;
        if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 22) {
            if (this.client != null) this.client.scheduleStop();
            return true;
        }

        // Handle Top Right Shortcuts clicks
        if (this.client != null && this.client.getSession() != null) {
            int rightX = Math.max(this.width / 2 + 105, this.width - 225);
            int topY = 15;
            int iconX = rightX + 115;

            for (IconWidget iconBtn : topIcons) {
                if (mouseX >= iconX && mouseX <= iconX + 22 && mouseY >= topY && mouseY <= topY + 28) {
                    iconBtn.onClick.run();
                    return true;
                }
                iconX += 26;
            }
        }

        // Handle Bottom Right GitHub Box click
        int adWidth = 140;
        int adHeight = 45;
        int adX = this.width - adWidth - 15;
        int adY = this.height - adHeight - 15;
        if (mouseX >= adX && mouseX <= adX + adWidth && mouseY >= adY && mouseY <= adY + adHeight) {
            Util.getOperatingSystem().open("https://github.com/hollowbytez");
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
