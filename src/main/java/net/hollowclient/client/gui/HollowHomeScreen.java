package net.hollowclient.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HollowHomeScreen extends Screen {
    public static final CubeMapRenderer CUSTOM_PANORAMA = new CubeMapRenderer(Identifier.ofVanilla("textures/gui/title/background/panorama"));
    private final RotatingCubeMapRenderer panoramaRenderer = new RotatingCubeMapRenderer(CUSTOM_PANORAMA);

    private final List<MenuButton> centerButtons = new ArrayList<>();
    private final List<IconWidget> topIcons = new ArrayList<>();
    private final List<ServerShortcut> serverShortcuts = new ArrayList<>();

    public HollowHomeScreen() {
        super(Text.of("Hollow Home Screen"));
    }

    private static class MenuButton {
        String label;
        boolean isAccent;
        Runnable onClick;
        MenuButton(String label, boolean isAccent, Runnable onClick) {
            this.label = label;
            this.isAccent = isAccent;
            this.onClick = onClick;
        }
    }

    private static class IconWidget {
        String icon;
        Runnable onClick;
        IconWidget(String icon, Runnable onClick) { this.icon = icon; this.onClick = onClick; }
    }

    private static class ServerShortcut {
        String ip;
        String name;
        String initial;
        ServerShortcut(String name, String ip, String initial) { this.name = name; this.ip = ip; this.initial = initial; }
    }

    @Override
    protected void init() {
        centerButtons.clear();
        topIcons.clear();
        serverShortcuts.clear();

        // Central Buttons (Removed Cosmetics)
        centerButtons.add(new MenuButton("Singleplayer", false, () -> { if (this.client != null) this.client.setScreen(new SelectWorldScreen(this)); }));
        centerButtons.add(new MenuButton("Multiplayer", false, () -> { if (this.client != null) this.client.setScreen(new MultiplayerScreen(this)); }));
        centerButtons.add(new MenuButton("Screenshots", false, () -> { 
            if (this.client != null) {
                File dir = new File(this.client.runDirectory, "screenshots");
                if (!dir.exists()) dir.mkdirs();
                Util.getOperatingSystem().open(dir);
            }
        }));
        centerButtons.add(new MenuButton("Hollow Settings", true, () -> { if (this.client != null) this.client.setScreen(new HollowSettingsScreen(this)); }));

        // Top Right Actions
        topIcons.add(new IconWidget("⚙", () -> { if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options)); }));
        topIcons.add(new IconWidget("👕", () -> { if (this.client != null) this.client.setScreen(new SkinOptionsScreen(this, this.client.options)); }));
        
        // Left Sidebar Servers
        serverShortcuts.add(new ServerShortcut("PikaNetwork", "play.pikanetwork.net", "P"));
        serverShortcuts.add(new ServerShortcut("Jartex", "play.jartexnetwork.com", "J"));
        serverShortcuts.add(new ServerShortcut("BlocksMC", "blocksmc.com", "B"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fix white hazy mask by cleaning state
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        
        this.panoramaRenderer.render(context, this.width, this.height, 1.0f, delta);
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Crisp dark gradient fade overlay
        context.fillGradient(0, 0, this.width, this.height, 0xA0000000, 0xE0000000);

        // --- Render Top Right Profile Box ---
        if (this.client != null && this.client.getSession() != null) {
            int rightX = this.width - 200;
            int topY = 20;

            // Smooth Rounded Profile Container
            RenderUtils.drawRoundedRect(context.getMatrices(), rightX, topY, 110, 36, 6, 0xD9141416);
            RenderUtils.drawRoundedOutline(context.getMatrices(), rightX, topY, 110, 36, 6, 1.0f, 0x30FFFFFF);
            
            try {
                SkinTextures skinTextures = this.client.getSkinProvider().getSkinTextures(this.client.getGameProfile());
                Identifier skinId = skinTextures.texture();
                context.drawTexture(skinId, rightX + 8, topY + 6, 24, 24, 8.0f, 8.0f, 8, 8, 64, 64);
                context.drawTexture(skinId, rightX + 8, topY + 6, 24, 24, 40.0f, 8.0f, 8, 8, 64, 64);
            } catch (Exception ignored) {}

            String username = this.client.getSession().getUsername();
            if (username.length() > 10) username = username.substring(0, 9) + "..";
            context.drawText(this.textRenderer, username.toUpperCase(), rightX + 40, topY + 14, 0xFFFFFFFF, false);

            // Action Icons
            int iconX = rightX + 118;
            for (IconWidget iconBtn : topIcons) {
                boolean hovered = mouseX >= iconX && mouseX <= iconX + 36 && mouseY >= topY && mouseY <= topY + 36;
                RenderUtils.drawRoundedRect(context.getMatrices(), iconX, topY, 36, 36, 6, hovered ? 0xD92A2A2E : 0xD9141416);
                RenderUtils.drawRoundedOutline(context.getMatrices(), iconX, topY, 36, 36, 6, 1.0f, hovered ? 0x80FFFFFF : 0x30FFFFFF);
                context.drawCenteredTextWithShadow(this.textRenderer, iconBtn.icon, iconX + 18, topY + 14, 0xFFFFFFFF);
                iconX += 42;
            }
        }

        // --- Render Left Server Sidebar ---
        int sideY = this.height / 2 - (serverShortcuts.size() * 50) / 2;
        int sideX = 20;
        for (ServerShortcut sc : serverShortcuts) {
            boolean hov = mouseX >= sideX && mouseX <= sideX + 40 && mouseY >= sideY && mouseY <= sideY + 40;
            RenderUtils.drawRoundedRect(context.getMatrices(), sideX, sideY, 40, 40, 20, hov ? 0xD92A2A2E : 0xD9141416);
            RenderUtils.drawRoundedOutline(context.getMatrices(), sideX, sideY, 40, 40, 20, 1.0f, hov ? 0x80FFFFFF : 0x30FFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, sc.initial, sideX + 20, sideY + 16, 0xFFFFFFFF);
            
            // Green online dot
            RenderUtils.drawRoundedRect(context.getMatrices(), sideX + 30, sideY + 28, 10, 10, 5, 0xFF22C55E);
            
            if (hov) {
                context.drawText(this.textRenderer, sc.name, sideX + 50, sideY + 16, 0xFFFFFFFF, true);
            }
            sideY += 50;
        }

        // --- Render Center Custom Font Title ---
        int centerY = this.height / 2 - 80;
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, centerY - 60, 0);
        context.getMatrices().scale(3.0f, 3.0f, 1.0f);
        Text title = Text.literal("HOLLOW CLIENT").setStyle(Style.EMPTY.withFont(Identifier.of("hollowclient", "eternalo")));
        int tw = this.textRenderer.getWidth(title);
        // Shadow pass
        context.drawText(this.textRenderer, title, -tw / 2 + 1, 1, 0x80000000, false);
        // Main text
        context.drawText(this.textRenderer, title, -tw / 2, 0, 0xFFFFFFFF, false);
        context.getMatrices().pop();

        // --- Render Main Center Buttons ---
        int buttonY = centerY;
        int buttonWidth = 200;
        int buttonHeight = 30;
        int leftX = this.width / 2 - buttonWidth / 2;

        for (MenuButton btn : centerButtons) {
            boolean hovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
            
            int bg = btn.isAccent ? (hovered ? 0xD9E53935 : 0xD9EB4040) : (hovered ? 0xD92A2A2E : 0xD9141416);
            int border = btn.isAccent ? (hovered ? 0xFFFFCDD2 : 0xFFFF8A80) : (hovered ? 0x80FFFFFF : 0x30FFFFFF);
            
            RenderUtils.drawRoundedRect(context.getMatrices(), leftX, buttonY, buttonWidth, buttonHeight, 8, bg);
            RenderUtils.drawRoundedOutline(context.getMatrices(), leftX, buttonY, buttonWidth, buttonHeight, 8, 1.5f, border);
            
            int textY = buttonY + (buttonHeight - this.textRenderer.fontHeight) / 2 + 1;
            context.drawCenteredTextWithShadow(this.textRenderer, btn.label, this.width / 2, textY, 0xFFFFFFFF);
            
            buttonY += 40;
        }

        // Quit Game Button (Clean text only at bottom)
        int quitY = this.height - 30;
        boolean quitHovered = mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 20;
        context.drawCenteredTextWithShadow(this.textRenderer, "Quit Game", this.width / 2, quitY, quitHovered ? 0xFFFF5555 : 0xFFAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Quit Button
        int buttonWidth = 200;
        int leftX = this.width / 2 - buttonWidth / 2;
        int quitY = this.height - 30;
        if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= quitY && mouseY <= quitY + 20) {
            if (this.client != null) this.client.scheduleStop();
            return true;
        }

        // Center Buttons
        int centerY = this.height / 2 - 80;
        int buttonY = centerY;
        for (MenuButton btn : centerButtons) {
            if (mouseX >= leftX && mouseX <= leftX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 30) {
                btn.onClick.run();
                return true;
            }
            buttonY += 40;
        }

        // Top Right Icons
        if (this.client != null && this.client.getSession() != null) {
            int rightX = this.width - 200;
            int topY = 20;
            int iconX = rightX + 118;
            for (IconWidget iconBtn : topIcons) {
                if (mouseX >= iconX && mouseX <= iconX + 36 && mouseY >= topY && mouseY <= topY + 36) {
                    iconBtn.onClick.run();
                    return true;
                }
                iconX += 42;
            }
        }

        // Left Sidebar Servers
        int sideY = this.height / 2 - (serverShortcuts.size() * 50) / 2;
        int sideX = 20;
        for (ServerShortcut sc : serverShortcuts) {
            if (mouseX >= sideX && mouseX <= sideX + 40 && mouseY >= sideY && mouseY <= sideY + 40) {
                ServerInfo info = new ServerInfo(sc.name, sc.ip, ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(this, this.client, ServerAddress.parse(info.address), info, false, null);
                return true;
            }
            sideY += 50;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}

