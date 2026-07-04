package net.hollowclient.client.gui;

import net.hollowclient.client.config.HollowConfig;
import net.hollowclient.client.config.HollowHudConfig;
import net.hollowclient.client.HollowClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class HollowSettingsScreen extends Screen {
    private final Screen parent;
    private Category currentCategory = Category.ALL;
    private final List<ModCard> cards = new ArrayList<>();
    private TextFieldWidget searchField;
    private boolean isSingleListLayout = false;

    // ===== Exact color palette =====
    private static final int BG_PANEL       = 0xD9141416;  // Translucent dark charcoal (85% opaque)
    private static final int BG_CARD        = 0xE61F2026;  // Card background (90% opaque)
    private static final int ACCENT_RED     = 0xFFEB4040;  // Salmon red accent
    private static final int ENABLED_GREEN  = 0xFF226422;  // Enabled toggle
    private static final int ENABLED_GREEN_H= 0xFF2E8E2E;  // Enabled hover
    private static final int DISABLED_GRAY  = 0xFF2A2B36;  // Disabled toggle
    private static final int DISABLED_GRAY_H= 0xFF3A3B46;  // Disabled hover
    private static final int TEXT_WHITE     = 0xFFFFFFFF;  // Primary text
    private static final int TEXT_GRAY      = 0xFF8A8C96;  // Secondary text
    private static final int CLOSE_RED      = 0xFFBA2D2D;  // Close button
    private static final int HEADER_BG      = 0xD9121214;  // Header/nav background
    private static final int ICON_BTN_BG    = 0xFF1A1A1E;  // Icon button background
    private static final int ICON_BTN_HOVER = 0xFF2A2B36;  // Icon button hover
    private static final int CARD_HOVER     = 0xFF262830;  // Card hover
    private static final int SEPARATOR      = 0xFF222226;  // Lines/separators
    private static final int CORNER_CLIP    = 0xFF141416;  // Corner clip color (matches panel)

    // Layout geometry
    private int panelX, panelY, panelW, panelH;
    private int navBarH;
    private int filterBarH;
    private double scrollY = 0;
    private boolean isDraggingScrollbar = false;

    public HollowSettingsScreen(Screen parent) {
        super(Text.of("Hollow Mod Menu"));
        this.parent = parent;
    }

    enum Category {
        ALL("All"), NEW("New"), HUD("HUD"), PVP("PvP"), COSMETICS("Cosmetics");
        final String name;
        Category(String name) { this.name = name; }
    }

    private static class ModCard {
        String name;
        String icon;
        java.util.function.BooleanSupplier getter;
        java.util.function.Consumer<Boolean> setter;
        Category category;
        Runnable onConfigure;

        ModCard(String name, String icon, Category category, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
            this.name = name;
            this.icon = icon;
            this.category = category;
            this.getter = getter;
            this.setter = setter;
        }

        ModCard withConfig(Runnable onConfigure) {
            this.onConfigure = onConfigure;
            return this;
        }
    }

    // ===== Rounded rectangle helpers =====
    // True rounded rect for transparency using RenderUtils anti-aliasing
    private void fillRounded(DrawContext ctx, int x, int y, int w, int h, int color, int cornerColor, int r) {
        RenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, w, h, r, color);
    }
    
    private void fillRoundedTop(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        RenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, w, h, r, color);
        ctx.fill(x, y + r, x + w, y + h, color);
    }

    private void fillRoundedBottom(DrawContext ctx, int x, int y, int w, int h, int color, int r) {
        RenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, w, h, r, color);
        ctx.fill(x, y, x + w, y + h - r, color);
    }

    @Override
    protected void init() {
        // === 4:3 aspect ratio panel, centered on screen ===
        float scale = HollowMenuSettingsScreen.menuScale;
        int maxW = (int)(this.width * 0.72 * scale);
        int maxH = (int)(this.height * 0.75 * scale);
        // Enforce roughly 4:3
        if (maxW > maxH * 4 / 3) {
            panelW = maxH * 4 / 3;
            panelH = maxH;
        } else {
            panelW = maxW;
            panelH = maxW * 3 / 4;
        }
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        navBarH = Math.max(22, (int)(panelH * 0.08));
        filterBarH = Math.max(20, (int)(panelH * 0.07));

        // Search field inside filter bar
        int searchW = Math.min(100, panelW / 4);
        int searchX = panelX + panelW - searchW - 50;
        int searchY = panelY + navBarH + (filterBarH - 10) / 2;
        this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, searchW, 10, Text.of("Search"));
        this.searchField.setMaxLength(30);
        this.searchField.setDrawsBackground(false);
        this.searchField.setPlaceholder(Text.of("Search..."));
        this.searchField.setEditableColor(TEXT_WHITE);
        this.searchField.setUneditableColor(TEXT_GRAY);
        this.addSelectableChild(this.searchField);

        HollowHudConfig.load();
        
        if (cards.isEmpty()) {
            HollowConfig cfg = HollowConfig.INSTANCE;

            // --- HUD Category ---
            cards.add(new ModCard("Armor Bar", "🛡", Category.HUD, () -> HollowHudConfig.get("Armor Bar").enabled, (v) -> { HollowHudConfig.get("Armor Bar").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Armor HUD", "🛡", Category.HUD, () -> HollowHudConfig.get("Armor HUD").enabled, (v) -> { HollowHudConfig.get("Armor HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Armor Status", "🛡", Category.HUD, () -> HollowHudConfig.get("Armor Status").enabled, (v) -> { HollowHudConfig.get("Armor Status").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Boss Bar", "👿", Category.HUD, () -> HollowHudConfig.get("Boss Bar").enabled, (v) -> { HollowHudConfig.get("Boss Bar").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Combo Display", "⚔", Category.HUD, () -> HollowHudConfig.get("Combo Display").enabled, (v) -> { HollowHudConfig.get("Combo Display").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Coordinates", "📍", Category.HUD, () -> HollowHudConfig.get("Coordinates").enabled, (v) -> { HollowHudConfig.get("Coordinates").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Damage Indicator", "💔", Category.HUD, () -> HollowHudConfig.get("Target HUD").enabled, (v) -> { HollowHudConfig.get("Target HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Direction HUD", "🧭", Category.HUD, () -> HollowHudConfig.get("Direction HUD").enabled, (v) -> { HollowHudConfig.get("Direction HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("FPS HUD", "📊", Category.HUD, () -> HollowHudConfig.get("FPS HUD").enabled, (v) -> { HollowHudConfig.get("FPS HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Hearts", "❤", Category.HUD, () -> HollowHudConfig.get("Hearts").enabled, (v) -> { HollowHudConfig.get("Hearts").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Item Counter", "📦", Category.HUD, () -> HollowHudConfig.get("Item Counter").enabled, (v) -> { HollowHudConfig.get("Item Counter").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Keystrokes", "⌨", Category.HUD, () -> HollowHudConfig.get("Keystrokes").enabled, (v) -> { HollowHudConfig.get("Keystrokes").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Pack Display", "🗂", Category.HUD, () -> HollowHudConfig.get("Pack Display").enabled, (v) -> { HollowHudConfig.get("Pack Display").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Ping Display", "📶", Category.HUD, () -> HollowHudConfig.get("Ping Display").enabled, (v) -> { HollowHudConfig.get("Ping Display").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Playtime", "⏳", Category.HUD, () -> HollowHudConfig.get("Playtime").enabled, (v) -> { HollowHudConfig.get("Playtime").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Potion HUD", "🧪", Category.HUD, () -> HollowHudConfig.get("Potion HUD").enabled, (v) -> { HollowHudConfig.get("Potion HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Reach Display", "📏", Category.HUD, () -> HollowHudConfig.get("Reach Display").enabled, (v) -> { HollowHudConfig.get("Reach Display").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Saturation HUD", "🥩", Category.HUD, () -> HollowHudConfig.get("Saturation HUD").enabled, (v) -> { HollowHudConfig.get("Saturation HUD").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Scoreboard", "📋", Category.HUD, () -> HollowHudConfig.get("Scoreboard").enabled, (v) -> { HollowHudConfig.get("Scoreboard").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Server IP", "🌐", Category.HUD, () -> HollowHudConfig.get("Server Address").enabled, (v) -> { HollowHudConfig.get("Server Address").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Speed Meter", "👟", Category.HUD, () -> HollowHudConfig.get("Speed Meter").enabled, (v) -> { HollowHudConfig.get("Speed Meter").enabled = v; HollowHudConfig.save(); }));
        cards.add(new ModCard("Stopwatch", "⏱", Category.HUD, () -> HollowHudConfig.get("Stopwatch").enabled, (v) -> { HollowHudConfig.get("Stopwatch").enabled = v; HollowHudConfig.save(); })
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowKeybindSettingScreen(this, "Stopwatch", HollowClient.stopwatchKey));
            }));
        cards.add(new ModCard("Totem Counter", "🪶", Category.HUD, () -> HollowHudConfig.get("Totem Counter").enabled, (v) -> { HollowHudConfig.get("Totem Counter").enabled = v; HollowHudConfig.save(); }));

        // --- PvP Category ---
        cards.add(new ModCard("AutoGG", "🗣", Category.PVP, () -> cfg.autoGG, (v) -> cfg.autoGG = v));
        cards.add(new ModCard("Crystal Optimizer", "💎", Category.PVP, () -> cfg.crystalOptimizer, (v) -> cfg.crystalOptimizer = v));
        cards.add(new ModCard("Freelook", "👁", Category.PVP, () -> cfg.freelook, (v) -> cfg.freelook = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowKeybindSettingScreen(this, "Freelook", HollowClient.freelookKey));
            }));
        cards.add(new ModCard("Glint Customizer", "✨", Category.COSMETICS, () -> cfg.glint, (v) -> cfg.glint = v));
        cards.add(new ModCard("Dragon Wings", "🐉", Category.COSMETICS, () -> cfg.dragonWings, (v) -> cfg.dragonWings = v));
        cards.add(new ModCard("Loot Beams", "🌟", Category.COSMETICS, () -> cfg.lootBeams, (v) -> cfg.lootBeams = v));
        cards.add(new ModCard("Hitbox Outlines", "📦", Category.PVP, () -> cfg.blockOverlay, (v) -> cfg.blockOverlay = v));
        cards.add(new ModCard("Hurt Cam", "🤕", Category.PVP, () -> cfg.hurtCam, (v) -> cfg.hurtCam = v));
        cards.add(new ModCard("Low Fire", "🔥", Category.PVP, () -> cfg.lowFire, (v) -> cfg.lowFire = v));
        cards.add(new ModCard("ToggleSprint", "🏃", Category.PVP, () -> cfg.toggleSprint, (v) -> cfg.toggleSprint = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowKeybindSettingScreen(this, "ToggleSprint", HollowClient.toggleSprintKey));
            }));
        cards.add(new ModCard("Short Shields", "🛡", Category.PVP, () -> cfg.shortShields, (v) -> cfg.shortShields = v));
        cards.add(new ModCard("AutoClicker", "🖱", Category.PVP, () -> cfg.autoClicker, (v) -> cfg.autoClicker = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowAutoClickerScreen(this));
            }));
        cards.add(new ModCard("No Bobbing", "🪨", Category.PVP, () -> cfg.noBobbing, (v) -> cfg.noBobbing = v));
        cards.add(new ModCard("Zoom", "🔍", Category.PVP, () -> cfg.zoom, (v) -> cfg.zoom = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowZoomScreen(this));
            }));

        // --- New/Gameplay Category ---
        cards.add(new ModCard("Auto Text", "✍", Category.NEW, () -> cfg.autoText, (v) -> cfg.autoText = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowAutoTextScreen(this));
            }));
        cards.add(new ModCard("Cull Logs", "🧹", Category.NEW, () -> cfg.cullLogs, (v) -> cfg.cullLogs = v));
        cards.add(new ModCard("Custom Crosshair", "⌖", Category.NEW, () -> cfg.customCrosshair, (v) -> cfg.customCrosshair = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new HollowCrosshairScreen(this));
            }));
        cards.add(new ModCard("Drop Prevention", "🔒", Category.NEW, () -> cfg.dropPrevention, (v) -> cfg.dropPrevention = v));
        cards.add(new ModCard("Fullbright", "💡", Category.NEW, () -> cfg.fullbright, (v) -> cfg.fullbright = v));
        cards.add(new ModCard("Item Physics", "🌍", Category.NEW, () -> cfg.itemPhysics, (v) -> cfg.itemPhysics = v));
        cards.add(new ModCard("Nick Hider", "👤", Category.NEW, () -> cfg.nickHider, (v) -> cfg.nickHider = v));
            cards.add(new ModCard("TimeChanger", "☀️", Category.NEW, () -> cfg.timeChanger, (v) -> cfg.timeChanger = v)
                .withConfig(() -> {
                    if (this.client != null) this.client.setScreen(new HollowTimeChangerScreen(this));
                }));
        }
    }

    private List<ModCard> getFilteredCards() {
        String query = this.searchField != null ? this.searchField.getText().toLowerCase().trim() : "";
        List<ModCard> list = new ArrayList<>();
        for (ModCard c : cards) {
            boolean matchCat = (currentCategory == Category.ALL || c.category == currentCategory);
            boolean matchSearch = query.isEmpty() || c.name.toLowerCase().contains(query);
            if (matchCat && matchSearch) list.add(c);
        }
        list.sort(java.util.Comparator.comparing(c -> c.name));
        return list;
    }

    private int getCols() {
        if (isSingleListLayout) return 1;
        if (panelW >= 360) return 4;
        if (panelW >= 260) return 3;
        return 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        int gridTop = panelY + navBarH + filterBarH;
        int gridBot = panelY + panelH;
        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= gridTop && mouseY <= gridBot) {
            int cols = getCols();
            int pad = 8;
            int cardH = getCardH();
            int items = getFilteredCards().size();
            int rows = (items + cols - 1) / cols;
            int totalH = rows * (cardH + pad) + pad;
            int gridH = gridBot - gridTop;
            double maxScroll = Math.max(0, totalH - gridH);
            scrollY -= vAmt * 22;
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    private int getCardH() {
        int gridH = panelH - navBarH - filterBarH;
        int h = (gridH - 8 * 4) / 3;
        return Math.max(55, Math.min(h, 85));
    }

    // ============================================================
    // RENDER
    // ============================================================
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // === Transparent blurred background ===
        super.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, this.width, this.height, 0x33000000);

        int cols = getCols();
        int pad = 8;
        int cardH = getCardH();
        int cardW = (panelW - pad * (cols + 1)) / cols;

        // ====================================================
        // A. TOP NAVIGATION DOCK (above main panel body)
        // ====================================================
        int navY = panelY;
        int navBot = navY + navBarH;

        // Nav bar background (translucent, top rounded)
        fillRoundedTop(context, panelX, navY, panelW, navBarH, HEADER_BG, 4);

        // Hollow icon (left)
        Identifier iconId = Identifier.of("hollowclient", "icon.png");
        int icoS = navBarH - 6;
        context.drawTexture(iconId, panelX + 5, navY + 3, 0.0f, 0.0f, icoS, icoS, 500, 500);

        // Red "MOD MENU" pill tab (rounded, left of icons)
        int mmX = panelX + icoS + 10;
        int mmW = 66;
        int mmH = navBarH - 6;
        int mmY = navY + 3;
        fillRounded(context, mmX, mmY, mmW, mmH, HollowMenuSettingsScreen.themeColor, HEADER_BG, 2);
        context.drawText(this.textRenderer, "🪶 MOD MENU", mmX + 4, mmY + (mmH - 8) / 2, TEXT_WHITE, false);

        // Section icon buttons with dark square bounding boxes
        int secX = mmX + mmW + 8;
        String[] secIcons = {"⚙", "👕", "📺", "🎮", "👥", "🔧"};
        int btnS = navBarH - 8;
        for (String si : secIcons) {
            boolean siH = mouseX >= secX && mouseX <= secX + btnS + 4 && mouseY >= navY + 3 && mouseY <= navY + 3 + btnS + 2;
            fillRounded(context, secX, navY + 4, btnS + 4, btnS + 2, siH ? ICON_BTN_HOVER : ICON_BTN_BG, HEADER_BG, 2);
            context.drawCenteredTextWithShadow(this.textRenderer, si, secX + (btnS + 4) / 2, navY + 4 + (btnS - 6) / 2, siH ? TEXT_WHITE : TEXT_GRAY);
            secX += btnS + 8;
        }

        // "HollowClient" branding - normal kerning, left-aligned after icons
        context.drawText(this.textRenderer, "HollowClient", secX + 6, navY + (navBarH - 8) / 2, HollowMenuSettingsScreen.themeColor, true);

        // Username (right side)
        if (this.client != null && this.client.getSession() != null) {
            String uname = this.client.getSession().getUsername();
            if (uname.length() > 10) uname = uname.substring(0, 8) + "..";
            int unW = this.textRenderer.getWidth(uname);
            context.drawText(this.textRenderer, uname, panelX + panelW - unW - 26, navY + (navBarH - 8) / 2, TEXT_WHITE, true);
        }

        // Close X button (rounded dark red square)
        int clS = navBarH - 8;
        int clX = panelX + panelW - clS - 5;
        int clY = navY + 4;
        boolean clH = mouseX >= clX && mouseX <= clX + clS && mouseY >= clY && mouseY <= clY + clS;
        fillRounded(context, clX, clY, clS, clS, clH ? 0xFFE53935 : CLOSE_RED, HEADER_BG, 2);
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", clX + clS / 2, clY + (clS - 8) / 2, TEXT_WHITE);

        // MAIN PANEL BODY (translucent, bottom rounded, below nav bar)
        int bodyY = navBot;
        int bodyH = panelH - navBarH;
        fillRoundedBottom(context, panelX, bodyY, panelW, bodyH, BG_PANEL, 4);

        // ====================================================
        // B. FILTER BAR (pill tabs + search)
        // ====================================================
        int filterY = bodyY + 3;

        // Category pill tabs (rounded)
        int catX = panelX + pad;
        for (Category cat : Category.values()) {
            boolean active = cat == currentCategory;
            int tw = this.textRenderer.getWidth(cat.name) + 14;
            int pH = filterBarH - 8;
            int pY = filterY + 2;
            boolean catH = mouseX >= catX && mouseX <= catX + tw && mouseY >= pY && mouseY <= pY + pH;

            int pillColor;
            if (active) pillColor = HollowMenuSettingsScreen.themeColor;
            else if (catH) pillColor = ICON_BTN_HOVER;
            else pillColor = ICON_BTN_BG;

            fillRounded(context, catX, pY, tw, pH, pillColor, BG_PANEL, 2);
            context.drawCenteredTextWithShadow(this.textRenderer, cat.name, catX + tw / 2, pY + (pH - 8) / 2, active ? TEXT_WHITE : TEXT_GRAY);
            catX += tw + 5;
        }

        // Search field
        if (this.searchField != null) {
            int sX = this.searchField.getX();
            int sY = this.searchField.getY();
            int sW = this.searchField.getWidth();
            int sH = this.searchField.getHeight();
            fillRounded(context, sX - 4, sY - 3, sW + 8, sH + 6, ICON_BTN_BG, BG_PANEL, 2);
            this.searchField.render(context, mouseX, mouseY, delta);
        }

        // Utility icons (heart + grid) right side
        int uS = filterBarH - 10;
        int uX = panelX + panelW - uS - pad;
        int uY = filterY + 4;
        fillRounded(context, uX, uY, uS, uS, ICON_BTN_BG, BG_PANEL, 2);
        context.drawCenteredTextWithShadow(this.textRenderer, "♥", uX + uS / 2, uY + (uS - 8) / 2, TEXT_GRAY);
        uX -= uS + 4;
        fillRounded(context, uX, uY, uS, uS, ICON_BTN_BG, BG_PANEL, 2);
        context.drawCenteredTextWithShadow(this.textRenderer, "▦", uX + uS / 2, uY + (uS - 8) / 2, TEXT_GRAY);

        // Separator
        int sepY = filterY + filterBarH - 2;
        context.fill(panelX + pad, sepY, panelX + panelW - pad, sepY + 1, SEPARATOR);

        // ====================================================
        // C. MOD CARD GRID (4 columns, uniform 8px gaps)
        // ====================================================
        int gridTop = bodyY + filterBarH + 2;
        int gridH = bodyY + bodyH - gridTop - pad; // Bottom padding

        context.enableScissor(panelX, gridTop, panelX + panelW, gridTop + gridH);

        List<ModCard> filtered = getFilteredCards();
        int idx = 0;
        for (ModCard card : filtered) {
            int col = idx % cols;
            int row = idx / cols;

            int cX = panelX + pad + col * (cardW + pad);
            int cY = gridTop + pad + row * (cardH + pad) - (int) scrollY;

            if (cY + cardH >= gridTop && cY <= gridTop + gridH) {
                boolean enabled = card.getter.getAsBoolean();
                boolean hover = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // === Card background (rounded, lighter than panel) ===
                fillRounded(context, cX, cY, cardW, cardH, hover ? CARD_HOVER : HollowMenuSettingsScreen.cardBgColor, BG_PANEL, 2);

                // === "NEW" badge (green, top-left, above name) ===
                int nameY = cY + 5;
                if (card.category == Category.NEW) {
                    fillRounded(context, cX + 4, nameY - 1, 24, 10, 0xFF2E7D32, hover ? CARD_HOVER : HollowMenuSettingsScreen.cardBgColor, 1);
                    context.drawText(this.textRenderer, "NEW", cX + 6, nameY, TEXT_WHITE, false);
                    nameY += 11;
                }

                // === Mod name (top-left, LEFT-ALIGNED) ===
                String dName = card.name;
                int maxNW = cardW - 22;
                while (this.textRenderer.getWidth(dName) > maxNW && dName.length() > 3) {
                    dName = dName.substring(0, dName.length() - 1);
                }
                if (dName.length() < card.name.length()) dName += "..";
                context.drawText(this.textRenderer, dName, cX + 5, nameY, TEXT_WHITE, false);

                // === Heart icon (top-right) ===
                context.drawText(this.textRenderer, "♥", cX + cardW - 12, cY + 5, 0xFF444450, false);

                // === Center icon (vertically+horizontally centered in asset area) ===
                int iconTop = nameY + 12;
                int iconBot = cY + cardH - 20;
                int iconMid = (iconTop + iconBot) / 2 - 3;
                context.drawCenteredTextWithShadow(this.textRenderer, card.icon, cX + cardW / 2, iconMid, 0xFFDDDDDD);

                // === Bottom Control Row ===
                int botY = cY + cardH - 17;

                // Gear icon (EVERY card gets one, rounded square)
                int gS = 13;
                int gX = cX + 4;
                int gY = botY + 1;
                boolean gH = mouseX >= gX && mouseX <= gX + gS && mouseY >= gY && mouseY <= gY + gS;
                fillRounded(context, gX, gY, gS, gS, gH ? 0xFF3A3A44 : 0xFF222228, hover ? CARD_HOVER : HollowMenuSettingsScreen.cardBgColor, 1);
                context.drawCenteredTextWithShadow(this.textRenderer, "⚙", gX + gS / 2, gY + 3, gH ? TEXT_WHITE : TEXT_GRAY);

                // Toggle pill button (rounded, spans rest of bottom)
                int tX = cX + 20;
                int tW = cardW - 24;
                int tH = 13;
                int tY = botY + 1;
                boolean tHov = mouseX >= tX && mouseX <= tX + tW && mouseY >= tY && mouseY <= tY + tH;

                if (enabled) {
                    fillRounded(context, tX, tY, tW, tH, tHov ? ENABLED_GREEN_H : ENABLED_GREEN, hover ? CARD_HOVER : HollowMenuSettingsScreen.cardBgColor, 2);
                    context.drawCenteredTextWithShadow(this.textRenderer, "Enabled", tX + tW / 2, tY + 3, TEXT_WHITE);
                } else {
                    fillRounded(context, tX, tY, tW, tH, tHov ? DISABLED_GRAY_H : DISABLED_GRAY, hover ? CARD_HOVER : HollowMenuSettingsScreen.cardBgColor, 2);
                    context.drawCenteredTextWithShadow(this.textRenderer, "Disabled", tX + tW / 2, tY + 3, TEXT_GRAY);
                }
            }
            idx++;
        }

        context.disableScissor();

        // === Scrollbar ===
        int rows = (filtered.size() + cols - 1) / cols;
        int totalH = rows * (cardH + pad) + pad;
        if (totalH > gridH) {
            int barH = Math.max(14, (int)((double) gridH / totalH * gridH));
            int barY = gridTop + (int)((scrollY / (totalH - gridH)) * (gridH - barH));
            fillRounded(context, panelX + panelW - 5, barY, 3, barH, 0xFF444450, BG_PANEL, 1);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // ============================================================
    // MOUSE CLICK
    // ============================================================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int navY = panelY;

        // Close X
        int clS = navBarH - 8;
        int clX = panelX + panelW - clS - 5;
        int clY = navY + 4;
        if (mouseX >= clX && mouseX <= clX + clS && mouseY >= clY && mouseY <= clY + clS) {
            this.close();
            return true;
        }

        // Section icons
        int icoS = navBarH - 6;
        int mmX = panelX + icoS + 10;
        int mmW = 66;
        int secX = mmX + mmW + 8;
        int btnS = navBarH - 8;
        for (int i = 0; i < 6; i++) {
            if (mouseX >= secX && mouseX <= secX + btnS + 4 && mouseY >= navY + 3 && mouseY <= navY + 3 + btnS + 2) {
                if (i == 0) { // ⚙ Settings
                    if (this.client != null) this.client.setScreen(new HollowMenuSettingsScreen(this));
                } else if (i == 1) { // 👕 Profiles
                    if (this.client != null) this.client.setScreen(new HollowProfilesScreen(this));
                } else if (i == 2) { // 📺 HUD Editor
                    if (this.client != null) this.client.setScreen(new HollowHudEditorScreen(this));
                } else if (i == 3) { // 🎮 Layout Toggle
                    isSingleListLayout = !isSingleListLayout;
                    scrollY = 0;
                } else if (i == 4) { // 👥 Mod Search
                    if (this.searchField != null) {
                        this.searchField.setFocused(true);
                    }
                } else if (i == 5) { // 🔧 Adjust Button
                    // Also toggle layout for the Adjust button
                    isSingleListLayout = !isSingleListLayout;
                    scrollY = 0;
                }
                return true;
            }
            secX += btnS + 8;
        }

        // Search field
        if (this.searchField != null) {
            boolean clicked = mouseX >= this.searchField.getX() - 4 && mouseX <= this.searchField.getX() + this.searchField.getWidth() + 4
                           && mouseY >= this.searchField.getY() - 3 && mouseY <= this.searchField.getY() + this.searchField.getHeight() + 3;
            this.searchField.setFocused(clicked);
            if (clicked && button == 1) this.searchField.setText("");
        }

        // Category tabs
        int bodyY = navY + navBarH;
        int filterY = bodyY + 3;
        int catX = panelX + 8;
        for (Category cat : Category.values()) {
            int tw = this.textRenderer.getWidth(cat.name) + 14;
            int pH = filterBarH - 8;
            int pY = filterY + 2;
            if (mouseX >= catX && mouseX <= catX + tw && mouseY >= pY && mouseY <= pY + pH) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            catX += tw + 5;
        }

        // Card interactions
        int cols = getCols();
        int pad = 8;
        int cardH = getCardH();
        int cardW = (panelW - pad * (cols + 1)) / cols;
        int gridTop = bodyY + filterBarH + 2;
        int gridH = bodyY + (panelH - navBarH) - gridTop - pad;

        // Scrollbar Click Handle
        int trackX = panelX + panelW - 8;
        if (mouseX >= trackX - 5 && mouseX <= trackX + 10 && mouseY >= gridTop && mouseY <= gridTop + gridH) {
            isDraggingScrollbar = true;
            int rows = (int) Math.ceil((double) getFilteredCards().size() / cols);
            int totalH = rows * (cardH + pad);
            if (totalH > gridH) {
                int maxScroll = totalH - gridH;
                int barH = Math.max(10, (int)((gridH / (float)totalH) * gridH));
                double scrollRatio = (mouseY - gridTop - (barH / 2.0)) / (double)(gridH - barH);
                scrollY = Math.max(0, Math.min(maxScroll, scrollRatio * maxScroll));
            }
            return true;
        }

        List<ModCard> filtered = getFilteredCards();
        int idx = 0;
        for (ModCard card : filtered) {
            int col = idx % cols;
            int row = idx / cols;

            int cX = panelX + pad + col * (cardW + pad);
            int cY = gridTop + pad + row * (cardH + pad) - (int) scrollY;

            if (cY + cardH >= gridTop && cY <= gridTop + gridH) {
                boolean cardClicked = mouseX >= cX && mouseX <= cX + cardW && mouseY >= cY && mouseY <= cY + cardH;

                // Right-click opens config
                if (cardClicked && button == 1 && card.onConfigure != null) {
                    card.onConfigure.run();
                    return true;
                }

                // Gear icon click (every card has one, but only configurable ones open settings)
                int gS = 13;
                int gX = cX + 4;
                int gY = cY + cardH - 17 + 1;
                if (mouseX >= gX && mouseX <= gX + gS && mouseY >= gY && mouseY <= gY + gS) {
                    if (card.onConfigure != null) {
                        card.onConfigure.run();
                    }
                    return true;
                }

                // Toggle pill click
                int tX = cX + 20;
                int tW = cardW - 24;
                int tH = 13;
                int tY = cY + cardH - 17 + 1;
                if (mouseX >= tX && mouseX <= tX + tW && mouseY >= tY && mouseY <= tY + tH) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    HollowConfig.save();
                    return true;
                }

                // Click card body toggles
                if (cardClicked && button == 0) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    HollowConfig.save();
                    return true;
                }
            }
            idx++;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            int bodyY = panelY + navBarH;
            int gridTop = bodyY + filterBarH + 2;
            int pad = 8;
            int gridH = bodyY + (panelH - navBarH) - gridTop - pad;
            int cols = getCols();
            int rows = (int) Math.ceil((double) getFilteredCards().size() / cols);
            int totalH = rows * (getCardH() + pad);
            if (totalH > gridH) {
                int maxScroll = totalH - gridH;
                int barH = Math.max(10, (int)((gridH / (float)totalH) * gridH));
                double scrollRatio = (mouseY - gridTop - (barH / 2.0)) / (double)(gridH - barH);
                scrollY = Math.max(0, Math.min(maxScroll, scrollRatio * maxScroll));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField != null && this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            scrollY = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.searchField != null && this.searchField.charTyped(chr, modifiers)) {
            scrollY = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}

// ==========================================
// Sub-Settings: Auto Text
// ==========================================
class HollowAutoTextScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;
    private boolean listening = false;
    private ButtonWidget bindButton;

    protected HollowAutoTextScreen(Screen parent) {
        super(Text.of("Auto Text Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2, cy = this.height / 2;
        inputField = new TextFieldWidget(this.textRenderer, cx - 100, cy - 35, 200, 20, Text.of("Edit Macro"));
        inputField.setMaxLength(128);
        inputField.setText(HollowConfig.INSTANCE.autoTextCommand);
        this.addSelectableChild(inputField);
        bindButton = ButtonWidget.builder(Text.of(getKeyNameText()), b -> { listening = true; b.setMessage(Text.of("Press any key...")); }).dimensions(cx - 100, cy - 5, 200, 20).build();
        this.addDrawableChild(bindButton);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Back"), b -> { HollowConfig.INSTANCE.autoTextCommand = inputField.getText(); HollowConfig.save(); if (this.client != null) this.client.setScreen(parent); }).dimensions(cx - 50, cy + 25, 100, 20).build());
    }

    private String getKeyNameText() { return "Keybind: " + HollowClient.autoTextKey.getBoundKeyTranslationKey().replace("key.keyboard.", "").toUpperCase(); }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) { listening = false; if (keyCode != GLFW.GLFW_KEY_ESCAPE) { HollowClient.autoTextKey.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode)); if (this.client != null && this.client.options != null) this.client.options.write(); } bindButton.setMessage(Text.of(getKeyNameText())); return true; }
        if (inputField.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char chr, int mod) { if (inputField.charTyped(chr, mod)) return true; return super.charTyped(chr, mod); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "AUTO TEXT MACRO CONFIG", this.width / 2, this.height / 2 - 65, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Edit the command macro and click the keybind button.", this.width / 2, this.height / 2 - 50, 0xFFEB4040);
        inputField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public void close() { if (this.client != null) this.client.setScreen(parent); }
}

// ==========================================
// Sub-Settings: TimeChanger
// ==========================================
class HollowTimeChangerScreen extends Screen {
    private final Screen parent;
    protected HollowTimeChangerScreen(Screen parent) { super(Text.of("TimeChanger")); this.parent = parent; }

    @Override
    protected void init() {
        int lx = this.width / 2 - 95, sy = this.height / 2 - 35;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Morning"), b -> setTime(0, 0)).dimensions(lx, sy, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Day"), b -> setTime(6000, 1)).dimensions(lx + 100, sy, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Sunset"), b -> setTime(12000, 2)).dimensions(lx, sy + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Night"), b -> setTime(18000, 3)).dimensions(lx + 100, sy + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), b -> { if (this.client != null) this.client.setScreen(parent); }).dimensions(this.width / 2 - 45, sy + 60, 90, 20).build());
    }

    private void setTime(long t, int m) { HollowConfig.INSTANCE.timeChangerTicks = t; HollowConfig.INSTANCE.timeChangerMode = m; HollowConfig.save(); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "TIMECHANGER PRESETS", this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        String n = "Day"; if (HollowConfig.INSTANCE.timeChangerMode == 0) n = "Morning"; else if (HollowConfig.INSTANCE.timeChangerMode == 2) n = "Sunset"; else if (HollowConfig.INSTANCE.timeChangerMode == 3) n = "Night";
        context.drawCenteredTextWithShadow(this.textRenderer, "Active: " + n + " (" + HollowConfig.INSTANCE.timeChangerTicks + " ticks)", this.width / 2, this.height / 2 - 48, 0xFFEB4040);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public void close() { if (this.client != null) this.client.setScreen(parent); }
}

// ==========================================
// Sub-Settings: Crosshair
// ==========================================
class HollowCrosshairScreen extends Screen {
    private final Screen parent;
    protected HollowCrosshairScreen(Screen parent) { super(Text.of("Crosshair")); this.parent = parent; }

    private static final int[] COLORS = {0xFF00FF00, 0xFFFF0000, 0xFF00BFFF, 0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF, 0xFFFFFFFF, 0xFFFF8C00, 0xFFFF69B4};
    private static final String[] COLOR_NAMES = {"Green", "Red", "Blue", "Yellow", "Cyan", "Magenta", "White", "Orange", "Pink"};

    private String getPresetText() { String[] p = {"Cross","Dot","Circle","Circle/Dot","T-Shape","X-Shape","Square","Chevron","Tri-Bar","Box/Dot"}; int i = HollowConfig.INSTANCE.crosshairPreset; if (i < 0 || i >= p.length) i = 0; return "Style: " + p[i]; }
    private void cyclePreset(ButtonWidget b) { HollowConfig.INSTANCE.crosshairPreset = (HollowConfig.INSTANCE.crosshairPreset + 1) % 10; HollowConfig.save(); b.setMessage(Text.of(getPresetText())); }
    private String getColorText() { int c = HollowConfig.INSTANCE.crosshairColor; for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == c) return "Color: " + COLOR_NAMES[i]; return "Color: Custom"; }
    private void cycleColor(ButtonWidget b) { int c = HollowConfig.INSTANCE.crosshairColor; int n = 0; for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == c) { n = (i + 1) % COLORS.length; break; } HollowConfig.INSTANCE.crosshairColor = COLORS[n]; HollowConfig.save(); b.setMessage(Text.of(getColorText())); }

    @Override
    protected void init() {
        int lx = this.width / 2 - 95, sy = this.height / 2 - 40;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size +"), b -> adj(0)).dimensions(lx, sy, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size -"), b -> adj(1)).dimensions(lx + 100, sy, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap +"), b -> adj(2)).dimensions(lx, sy + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap -"), b -> adj(3)).dimensions(lx + 100, sy + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thick +"), b -> adj(4)).dimensions(lx, sy + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thick -"), b -> adj(5)).dimensions(lx + 100, sy + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(getPresetText()), b -> cyclePreset(b)).dimensions(lx, sy + 75, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of(getColorText()), b -> cycleColor(b)).dimensions(lx + 100, sy + 75, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), b -> { if (this.client != null) this.client.setScreen(parent); }).dimensions(this.width / 2 - 45, sy + 105, 90, 20).build());
    }

    private void adj(int t) { HollowConfig c = HollowConfig.INSTANCE; switch(t){case 0:c.crosshairSize=Math.max(1f,c.crosshairSize+.5f);break;case 1:c.crosshairSize=Math.max(1f,c.crosshairSize-.5f);break;case 2:c.crosshairGap=Math.max(0f,c.crosshairGap+.5f);break;case 3:c.crosshairGap=Math.max(0f,c.crosshairGap-.5f);break;case 4:c.crosshairThickness=Math.max(.5f,c.crosshairThickness+.5f);break;case 5:c.crosshairThickness=Math.max(.5f,c.crosshairThickness-.5f);break;} HollowConfig.save(); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "CUSTOM CROSSHAIR", this.width / 2, this.height / 2 - 75, 0xFFFFFFFF);
        HollowConfig cfg = HollowConfig.INSTANCE;
        context.drawCenteredTextWithShadow(this.textRenderer, String.format("Size: %.1f | Gap: %.1f | Thick: %.1f", cfg.crosshairSize, cfg.crosshairGap, cfg.crosshairThickness), this.width / 2, this.height / 2 - 60, 0xFFEB4040);

        int cx = this.width / 2, cy = this.height / 2 - 100;
        float gap = cfg.crosshairGap, size = cfg.crosshairSize, th = cfg.crosshairThickness;
        int color = cfg.crosshairColor, preset = cfg.crosshairPreset;
        int t = Math.max(1, (int) th), h1 = t / 2, h2 = t / 2 + (t % 2);

        switch (preset) {
            case 1: context.fill(cx-h1,cy-h1,cx+h2,cy+h2,color); break;
            case 2: case 3: int rad=(int)(gap+size); for(int a=0;a<360;a+=10){double r=Math.toRadians(a);int px=(int)Math.round(cx+Math.cos(r)*rad);int py=(int)Math.round(cy+Math.sin(r)*rad);context.fill(px-h1,py-h1,px+h2,py+h2,color);} if(preset==3)context.fill(cx-h1,cy-h1,cx+h2,cy+h2,color); break;
            case 4: context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color);context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color);context.fill(cx-h1,(int)(cy+gap),cx+h2,(int)(cy+gap+size),color); break;
            case 5: for(int i=0;i<(int)size;i++){int f=(int)(gap+i);context.fill(cx-f-h1,cy-f-h1,cx-f+h2,cy-f+h2,color);context.fill(cx+f-h1,cy-f-h1,cx+f+h2,cy-f+h2,color);context.fill(cx-f-h1,cy+f-h1,cx-f+h2,cy+f+h2,color);context.fill(cx+f-h1,cy+f-h1,cx+f+h2,cy+f+h2,color);} break;
            case 6: case 9: int rr=(int)(gap+size);context.fill(cx-rr,cy-rr-h1,cx+rr,cy-rr+h2,color);context.fill(cx-rr,cy+rr-h1,cx+rr,cy+rr+h2,color);context.fill(cx-rr-h1,cy-rr,cx-rr+h2,cy+rr,color);context.fill(cx+rr-h1,cy-rr,cx+rr+h2,cy+rr,color);if(preset==9)context.fill(cx-h1,cy-h1,cx+h2,cy+h2,color); break;
            case 7: for(int i=0;i<(int)size;i++){context.fill(cx-i-h1,(int)(cy-gap+i)-h1,cx-i+h2,(int)(cy-gap+i)+h2,color);context.fill(cx+i-h1,(int)(cy-gap+i)-h1,cx+i+h2,(int)(cy-gap+i)+h2,color);} break;
            case 8: context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color);context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color);context.fill(cx-h1,(int)(cy-gap-size),cx+h2,(int)(cy-gap),color); break;
            default: context.fill((int)(cx-gap-size),cy-h1,(int)(cx-gap),cy+h2,color);context.fill((int)(cx+gap),cy-h1,(int)(cx+gap+size),cy+h2,color);context.fill(cx-h1,(int)(cy-gap-size),cx+h2,(int)(cy-gap),color);context.fill(cx-h1,(int)(cy+gap),cx+h2,(int)(cy+gap+size),color); break;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public void close() { if (this.client != null) this.client.setScreen(parent); }
}


