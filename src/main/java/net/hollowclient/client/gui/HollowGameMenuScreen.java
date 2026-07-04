package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HollowGameMenuScreen extends Screen {
    private final Screen parent;
    private final List<MenuButton> buttons = new ArrayList<>();
    private final List<IconWidget> topIcons = new ArrayList<>();
    private final List<BottomWidget> bottomButtons = new ArrayList<>();

    public HollowGameMenuScreen(Screen parent) {
        super(Text.of("Hollow Game Menu"));
        this.parent = parent;
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

    private static class BottomWidget {
        String label;
        Runnable onClick;

        BottomWidget(String label, Runnable onClick) {
            this.label = label;
            this.onClick = onClick;
        }
    }

    @Override
    protected void init() {
        buttons.clear();
        topIcons.clear();
        bottomButtons.clear();

        // 1. Center Menu Buttons
        buttons.add(new MenuButton("◀", "Back to Game", false, () -> this.close()));
        buttons.add(new MenuButton("🪶", "Hollow Settings", false, () -> {
            if (this.client != null) this.client.setScreen(new HollowSettingsScreen(this));
        }));


        buttons.add(new MenuButton("⚙", "Options", false, () -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }));
        
        buttons.add(new MenuButton("⏏", "Save and Quit to Title", false, () -> {
            if (this.client != null) {
                boolean singleplayer = this.client.isInSingleplayer();
                if (this.client.world != null) {
                    this.client.world.disconnect();
                }
                if (singleplayer) {
                    this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
                    this.client.setScreen(new TitleScreen());
                } else {
                    this.client.disconnect();
                    this.client.setScreen(new MultiplayerScreen(new TitleScreen()));
                }
            }
        }));

        // 2. Top Right Profile Icons
        topIcons.add(new IconWidget("👁", "Toggle HUD Visiblity", () -> {
            if (this.client != null) this.client.options.hudHidden = !this.client.options.hudHidden;
        }));
        topIcons.add(new IconWidget("📁", "Open Screenshots Folder", () -> {
            if (this.client != null) {
                File screenshots = new File(this.client.runDirectory, "screenshots");
                if (!screenshots.exists()) screenshots.mkdirs();
                Util.getOperatingSystem().open(screenshots);
            }
        }));
        topIcons.add(new IconWidget("⌖", "Reset Draggable HUD Layout", () -> {
            HollowConfig.INSTANCE = new HollowConfig();
            HollowConfig.save();
        }));
        topIcons.add(new IconWidget("💬", "Social Feed / Chat", () -> {
            // No action
        }));

        // 3. Bottom Right Buttons
        bottomButtons.add(new BottomWidget("Advancements", () -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler()));
            }
        }));
        bottomButtons.add(new BottomWidget("Statistics", () -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
            }
        }));
        bottomButtons.add(new BottomWidget("Open to LAN", () -> {
            if (this.client != null) {
                this.client.setScreen(new net.minecraft.client.gui.screen.OpenToLanScreen(this));
            }
        }));
        bottomButtons.add(new BottomWidget("Game Folder", () -> {
            if (this.client != null) {
                Util.getOperatingSystem().open(this.client.runDirectory);
            }
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw slightly darkened transparent background
        context.fill(0, 0, this.width, this.height, 0x88000000);

        // --- Render Left Side Hollow Logo Watermark ---
        int leftAlign = 20;
        int bottomAlign = this.height - 20;
        
        // Render Hollow Logo Texture (translucent white)
        Identifier logoId = Identifier.of("hollowclient", "icon.png");
        context.drawTexture(logoId, 25, this.height / 2 - 80, 0.0f, 0.0f, 160, 160, 1024, 1024);

        // Render scattered crosses "+"
        context.drawText(this.textRenderer, "+", 40, this.height / 2 - 120, 0x30FFFFFF, false);
        context.drawText(this.textRenderer, "+", 220, this.height / 2 - 30, 0x20FFFFFF, false);
        context.drawText(this.textRenderer, "+", 100, this.height / 2 + 100, 0x40FFFFFF, false);
        context.drawText(this.textRenderer, "+", 160, this.height / 2 - 70, 0x15FFFFFF, false);

        // --- Render Center Header & Brand (using new custom title logo) ---
        int centerY = this.height / 2 - 130;
        int logoW = 160;
        int logoH = 90;
        Identifier titleLogo = Identifier.of("hollowclient", "textures/title.png");
        context.drawTexture(titleLogo, this.width / 2 - logoW / 2, centerY - 45, 0.0f, 0.0f, logoW, logoH, 3264, 1836);

        // --- Render Center Buttons ---
        int buttonY = centerY + 50;
        int buttonWidth = 190;
        int buttonHeight = 24;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : buttons) {
            boolean hovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;

            int backgroundCol;
            int borderCol;
            int textCol = 0xFFFFFFFF;

            if (btn.isStore) {
                // STORE Red Gradient theme
                backgroundCol = hovered ? 0xD9E53935 : 0xD9D32F2F;
                borderCol = 0xFFB71C1C;
            } else {
                // Sleek translucent grey
                backgroundCol = hovered ? 0xD92A2A2E : 0xD9141416;
                borderCol = hovered ? 0x80FFFFFF : 0x30FFFFFF;
            }

            RenderUtils.drawRoundedRect(context.getMatrices(), leftX, buttonY, buttonWidth, buttonHeight, 6, backgroundCol);
            RenderUtils.drawRoundedOutline(context.getMatrices(), leftX, buttonY, buttonWidth, buttonHeight, 6, 1.0f, borderCol);

            // Icon and Text rendering
            context.drawText(this.textRenderer, btn.icon, leftX + 12, buttonY + 8, textCol, false);
            context.drawCenteredTextWithShadow(this.textRenderer, btn.label, leftX + buttonWidth / 2, buttonY + 8, textCol);

            buttonY += 32;
        }

        // --- Render Top Right Profile Panel & Shortcut Keys ---
        if (this.client != null && this.client.player != null) {
            int rightX = Math.max(this.width / 2 + 105, this.width - 225);
            int topY = 15;

            // Profile Card (rounded box design)
            RenderUtils.drawRoundedRect(context.getMatrices(), rightX, topY, 110, 28, 6, 0xD9141416);
            RenderUtils.drawRoundedOutline(context.getMatrices(), rightX, topY, 110, 28, 6, 1.0f, 0x30FFFFFF);

            // Renders player head
            try {
                SkinTextures skinTextures = this.client.getSkinProvider().getSkinTextures(this.client.getGameProfile());
                Identifier skinId = skinTextures.texture();
                context.drawTexture(skinId, rightX + 6, topY + 4, 20, 20, 8.0f, 8.0f, 8, 8, 64, 64);
                context.drawTexture(skinId, rightX + 6, topY + 4, 20, 20, 40.0f, 8.0f, 8, 8, 64, 64);
            } catch (Exception ignored) {}

            // Renders username
            String username = this.client.player.getName().getString();
            if (username.length() > 10) username = username.substring(0, 8) + "..";
            context.drawText(this.textRenderer, username.toUpperCase(), rightX + 32, topY + 10, 0xFFFFFFFF, false);

            // Small Icon shortcuts (right of Profile Card)
            int iconX = rightX + 115;
            for (IconWidget iconBtn : topIcons) {
                boolean hovered = mouseX >= iconX && mouseX <= iconX + 26 && mouseY >= topY && mouseY <= topY + 28;
                RenderUtils.drawRoundedRect(context.getMatrices(), iconX, topY, 26, 28, 6, hovered ? 0xD92A2A2E : 0xD9141416);
                RenderUtils.drawRoundedOutline(context.getMatrices(), iconX, topY, 26, 28, 6, 1.0f, hovered ? 0x80FFFFFF : 0x30FFFFFF);
                context.drawCenteredTextWithShadow(this.textRenderer, iconBtn.icon, iconX + 13, topY + 10, 0xFFFFFFFF);
                iconX += 30;
            }
        }

        // --- Render Bottom Right Quick Utility row ---
        int bottomX = this.width - 15;
        int rowY = this.height - 25;
        for (int i = bottomButtons.size() - 1; i >= 0; i--) {
            BottomWidget b = bottomButtons.get(i);
            int textW = this.textRenderer.getWidth(b.label) + 12;
            int btnLeft = bottomX - textW;

            boolean hovered = mouseX >= btnLeft && mouseX <= bottomX && mouseY >= rowY && mouseY <= rowY + 22;
            RenderUtils.drawRoundedRect(context.getMatrices(), btnLeft, rowY, textW, 22, 4, hovered ? 0xD92A2A2E : 0x80141416);
            RenderUtils.drawRoundedOutline(context.getMatrices(), btnLeft, rowY, textW, 22, 4, 1.0f, hovered ? 0x80FFFFFF : 0x20FFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, b.label, btnLeft + textW / 2, rowY + 7, hovered ? 0xFFFFFFFF : 0xFFCCCCCC);

            bottomX -= (textW + 6);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle Center Menu Button Clicks
        int centerY = this.height / 2 - 130;
        int buttonY = centerY + 50;
        int buttonWidth = 190;
        int buttonHeight = 24;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : buttons) {
            if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                btn.onClick.run();
                return true;
            }
            buttonY += 32;
        }

        // Handle Top Right Icon Clicks
        if (this.client != null && this.client.player != null) {
            int rightX = Math.max(this.width / 2 + 105, this.width - 225);
            int topY = 15;
            int iconX = rightX + 115;

            for (IconWidget iconBtn : topIcons) {
                if (mouseX >= iconX && mouseX <= iconX + 26 && mouseY >= topY && mouseY <= topY + 28) {
                    iconBtn.onClick.run();
                    return true;
                }
                iconX += 30;
            }
        }

        // Handle Bottom Right Quick Button Clicks
        int bottomX = this.width - 15;
        int rowY = this.height - 25;
        for (int i = bottomButtons.size() - 1; i >= 0; i--) {
            BottomWidget b = bottomButtons.get(i);
            int textW = this.textRenderer.getWidth(b.label) + 12;
            int btnLeft = bottomX - textW;

            if (mouseX >= btnLeft && mouseX <= bottomX && mouseY >= rowY && mouseY <= rowY + 22) {
                b.onClick.run();
                return true;
            }
            bottomX -= (textW + 6);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing to prevent background blur
    }
}

