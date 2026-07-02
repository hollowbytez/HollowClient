package net.featherpojav.client.gui;

import net.featherpojav.client.config.FeatherConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class FeatherSettingsScreen extends Screen {
    private final Screen parent;
    private Category currentCategory = Category.ALL;
    private final List<ModCard> cards = new ArrayList<>();
    private TextFieldWidget searchField;
    
    // Geometry
    private int boxWidth = 420;
    private int boxHeight = 250;
    private int boxX = 0;
    private int boxY = 0;
    
    // Scroll state
    private double scrollY = 0;
    
    public FeatherSettingsScreen(Screen parent) {
        super(Text.of("Feather Mod Menu"));
        this.parent = parent;
    }
    
    enum Category {
        ALL("All"),
        PVP("PvP"),
        HUD("HUD"),
        NEW("New");
        
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
    
    @Override
    protected void init() {
        boxWidth = Math.min(420, this.width - 20);
        boxHeight = Math.min(250, this.height - 20);
        boxX = this.width / 2 - boxWidth / 2;
        boxY = this.height / 2 - boxHeight / 2;

        int searchX = boxX + 200;
        int searchY = boxY + 6;
        int searchW = boxWidth - 235;
        int searchH = 12;
        this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, searchW, searchH, Text.of("Search"));
        this.searchField.setMaxLength(30);
        this.searchField.setDrawsBackground(false);
        this.searchField.setPlaceholder(Text.of("Search..."));
        this.searchField.setEditableColor(0xFFFFFFFF);
        this.searchField.setUneditableColor(0xFF888888);
        this.addSelectableChild(this.searchField);

        cards.clear();
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        
        // --- HUD Category ---
        cards.add(new ModCard("Keystrokes", "⌨", Category.HUD, () -> cfg.keystrokes, (v) -> cfg.keystrokes = v));
        cards.add(new ModCard("Armor HUD", "🛡", Category.HUD, () -> cfg.armorHUD, (v) -> cfg.armorHUD = v));
        cards.add(new ModCard("Potion HUD", "🧪", Category.HUD, () -> cfg.potionHUD, (v) -> cfg.potionHUD = v));
        cards.add(new ModCard("Coordinates", "📍", Category.HUD, () -> cfg.coordHUD, (v) -> cfg.coordHUD = v));
        cards.add(new ModCard("Direction HUD", "🧭", Category.HUD, () -> cfg.directionHUD, (v) -> cfg.directionHUD = v));
        cards.add(new ModCard("FPS HUD", "📊", Category.HUD, () -> cfg.fpsHUD, (v) -> cfg.fpsHUD = v));
        cards.add(new ModCard("Combo Display", "⚔", Category.HUD, () -> cfg.comboDisplay, (v) -> cfg.comboDisplay = v));
        cards.add(new ModCard("Ping Display", "📶", Category.HUD, () -> cfg.pingDisplay, (v) -> cfg.pingDisplay = v));
        cards.add(new ModCard("Playtime", "⏳", Category.HUD, () -> cfg.playtime, (v) -> cfg.playtime = v));
        cards.add(new ModCard("Reach Display", "📏", Category.HUD, () -> cfg.reachDisplay, (v) -> cfg.reachDisplay = v));
        cards.add(new ModCard("Server IP", "🌐", Category.HUD, () -> cfg.serverAddress, (v) -> cfg.serverAddress = v));
        cards.add(new ModCard("Speed Meter", "👟", Category.HUD, () -> cfg.speedMeter, (v) -> cfg.speedMeter = v));
        cards.add(new ModCard("Stopwatch", "⏱", Category.HUD, () -> cfg.stopwatch, (v) -> cfg.stopwatch = v));
        cards.add(new ModCard("Item Counter", "📦", Category.HUD, () -> cfg.itemCounter, (v) -> cfg.itemCounter = v));
        cards.add(new ModCard("Armor Bar", "🛡", Category.HUD, () -> cfg.armorBar, (v) -> cfg.armorBar = v));
        cards.add(new ModCard("Armor Status", "🛡", Category.HUD, () -> cfg.armorStatus, (v) -> cfg.armorStatus = v));
        cards.add(new ModCard("Boss Bar", "👿", Category.HUD, () -> cfg.bossBar, (v) -> cfg.bossBar = v));
        cards.add(new ModCard("Hearts", "❤", Category.HUD, () -> cfg.hearts, (v) -> cfg.hearts = v));
        cards.add(new ModCard("Pack Display", "🗂", Category.HUD, () -> cfg.packDisplay, (v) -> cfg.packDisplay = v));
        cards.add(new ModCard("Scoreboard", "📋", Category.HUD, () -> cfg.scoreboard, (v) -> cfg.scoreboard = v));

        // --- PvP Category ---
        cards.add(new ModCard("ToggleSprint", "🏃", Category.PVP, () -> cfg.toggleSprint, (v) -> cfg.toggleSprint = v));
        cards.add(new ModCard("Zoom", "🔍", Category.PVP, () -> cfg.zoom, (v) -> cfg.zoom = v));
        cards.add(new ModCard("Freelook", "👁", Category.PVP, () -> cfg.freelook, (v) -> cfg.freelook = v));
        cards.add(new ModCard("AutoGG", "🗣", Category.PVP, () -> cfg.autoGG, (v) -> cfg.autoGG = v));
        cards.add(new ModCard("Hit Color", "⚔", Category.PVP, () -> cfg.animations, (v) -> cfg.animations = v));
        cards.add(new ModCard("Hitbox outlines", "📦", Category.PVP, () -> cfg.hitbox, (v) -> cfg.hitbox = v));
        cards.add(new ModCard("Reach Metric", "📏", Category.PVP, () -> cfg.reachDisplay, (v) -> cfg.reachDisplay = v));
        cards.add(new ModCard("Combo PVP", "⚔", Category.PVP, () -> cfg.comboDisplay, (v) -> cfg.comboDisplay = v));

        // --- New/Gameplay Category (also falls into ALL) ---
        cards.add(new ModCard("Auto Text", "✍", Category.NEW, () -> cfg.autoText, (v) -> cfg.autoText = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherAutoTextScreen(this));
            }));
        
        cards.add(new ModCard("TimeChanger", "☀️", Category.NEW, () -> cfg.timeChanger, (v) -> cfg.timeChanger = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherTimeChangerScreen(this));
            }));

        cards.add(new ModCard("Custom Crosshair", "⌖", Category.NEW, () -> cfg.customCrosshair, (v) -> cfg.customCrosshair = v)
            .withConfig(() -> {
                if (this.client != null) this.client.setScreen(new FeatherCrosshairScreen(this));
            }));

        cards.add(new ModCard("Fullbright", "💡", Category.NEW, () -> cfg.fullbright, (v) -> cfg.fullbright = v));
        cards.add(new ModCard("Item Physics", "🌍", Category.NEW, () -> cfg.itemPhysics, (v) -> cfg.itemPhysics = v));
        cards.add(new ModCard("Cull Logs", "🧹", Category.NEW, () -> cfg.cullLogs, (v) -> cfg.cullLogs = v));
        cards.add(new ModCard("Drop Prevention", "🔒", Category.NEW, () -> cfg.dropPrevention, (v) -> cfg.dropPrevention = v));
        cards.add(new ModCard("Nick Hider", "👤", Category.NEW, () -> cfg.nickHider, (v) -> cfg.nickHider = v));
        cards.add(new ModCard("Scoreboard", "📋", Category.NEW, () -> cfg.scoreboard, (v) -> cfg.scoreboard = v));
    }

    private List<ModCard> getFilteredCards() {
        String query = this.searchField != null ? this.searchField.getText().toLowerCase().trim() : "";
        List<ModCard> list = new ArrayList<>();
        for (ModCard c : cards) {
            boolean matchesCategory = (currentCategory == Category.ALL || c.category == currentCategory);
            boolean matchesSearch = query.isEmpty() || c.name.toLowerCase().contains(query);
            if (matchesCategory && matchesSearch) {
                list.add(c);
            }
        }
        return list;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY + 60 && mouseY <= boxY + boxHeight) {
            int itemsCount = getFilteredCards().size();
            int rowsCount = (itemsCount + 2) / 3;
            int totalHeight = rowsCount * 55;
            double maxScroll = Math.max(0, totalHeight - (boxHeight - 75));
            scrollY -= verticalAmount * 15;
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render game behind screen
        context.fill(0, 0, this.width, this.height, 0x60000000);

        // --- Render Centered Overlay Window Container ---
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF141416);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF2A2A2E);

        // --- Render Header (Top bar tabs) ---
        int topBarY = boxY + 4;
        // Tab background indicator
        context.fill(boxX + 6, topBarY, boxX + 70, topBarY + 22, 0xFF9C27B0);
        context.drawText(this.textRenderer, "MOD MENU", boxX + 12, topBarY + 7, 0xFFFFFFFF, false);
        
        // Icons row
        int iconX = boxX + 85;
        String[] headerIcons = {"💬", "👕", "📺", "⚙", "👥"};
        for (String icon : headerIcons) {
            context.drawText(this.textRenderer, icon, iconX, topBarY + 7, 0xFF888888, false);
            iconX += 20;
        }

        // Close X button
        boolean closeHovered = mouseX >= boxX + boxWidth - 20 && mouseX <= boxX + boxWidth - 5 && mouseY >= topBarY && mouseY <= topBarY + 20;
        context.drawText(this.textRenderer, "X", boxX + boxWidth - 16, topBarY + 7, closeHovered ? 0xFFE53935 : 0xFF888888, false);

        // --- Render Search Box ---
        if (this.searchField != null) {
            int sX = this.searchField.getX();
            int sY = this.searchField.getY();
            int sW = this.searchField.getWidth();
            int sH = this.searchField.getHeight();
            
            context.fill(sX - 4, sY - 3, sX + sW + 4, sY + sH + 3, 0xFF18181A);
            context.drawBorder(sX - 4, sY - 3, sW + 8, sH + 6, this.searchField.isFocused() ? 0xFF9C27B0 : 0x20FFFFFF);
            
            // Render text field
            this.searchField.render(context, mouseX, mouseY, delta);
            
            // Draw search icon "🔍" inside the box on the right
            context.drawText(this.textRenderer, "🔍", sX + sW - 10, sY, 0xFF888888, false);
        }

        // --- Render Sub-Tabs (All, PvP, HUD, New) ---
        int subTabY = boxY + 30;
        int tabX = boxX + 10;
        for (Category cat : Category.values()) {
            boolean active = cat == currentCategory;
            int textW = this.textRenderer.getWidth(cat.name) + 12;
            boolean hovered = mouseX >= tabX && mouseX <= tabX + textW && mouseY >= subTabY && mouseY <= subTabY + 16;
            
            context.fill(tabX, subTabY, tabX + textW, subTabY + 16, active ? 0x809C27B0 : (hovered ? 0xFF2A2A2E : 0x00000000));
            context.drawBorder(tabX, subTabY, textW, 16, active ? 0xFF9C27B0 : 0x20FFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, cat.name, tabX + textW / 2, subTabY + 4, active ? 0xFFFFFFFF : 0xFFCCCCCC);
            
            tabX += textW + 6;
        }

        // --- Render HUD Layout Button ---
        int hudBtnWidth = 80;
        int hudBtnX = boxX + boxWidth - hudBtnWidth - 10;
        boolean hudHovered = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnWidth && mouseY >= subTabY && mouseY <= subTabY + 16;
        context.fill(hudBtnX, subTabY, hudBtnX + hudBtnWidth, subTabY + 16, hudHovered ? 0xFF2A2A2E : 0x409C27B0);
        context.drawBorder(hudBtnX, subTabY, hudBtnWidth, 16, hudHovered ? 0xFFBA68C8 : 0xFF9C27B0);
        context.drawCenteredTextWithShadow(this.textRenderer, "📐 HUD Layout", hudBtnX + hudBtnWidth / 2, subTabY + 4, hudHovered ? 0xFFFFFFFF : 0xFFE1BEE7);

        // --- Render Grid of Mod Cards ---
        int cardAreaY = boxY + 54;
        int cardAreaH = boxHeight - 64;
        int cardWidth = (boxWidth - 30) / 3;
        int cardHeight = 50;

        context.enableScissor(boxX + 5, cardAreaY, boxX + boxWidth - 5, cardAreaY + cardAreaH);
        
        List<ModCard> filtered = getFilteredCards();
        int index = 0;
        for (ModCard card : filtered) {
            int col = index % 3;
            int row = index / 3;
            
            int cardX = boxX + 10 + col * (cardWidth + 5);
            int cardY = cardAreaY + 6 + row * (cardHeight + 5) - (int) scrollY;
            
            if (cardY + cardHeight >= cardAreaY && cardY <= cardAreaY + cardAreaH) {
                boolean cardHovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;
                context.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, cardHovered ? 0xFF222224 : 0xFF18181A);
                context.drawBorder(cardX, cardY, cardWidth, cardHeight, cardHovered ? 0x80FFFFFF : 0x20FFFFFF);
                
                // Mod title
                String displayName = card.name;
                if (displayName.length() > 14) displayName = displayName.substring(0, 12) + "..";
                context.drawText(this.textRenderer, displayName, cardX + 6, cardY + 5, 0xFFFFFFFF, false);
                
                // Center Icon
                context.drawCenteredTextWithShadow(this.textRenderer, card.icon, cardX + cardWidth / 2, cardY + 16, 0xFFCCCCCC);
                
                // Bottom-left Settings Gear (if configurable)
                if (card.onConfigure != null) {
                    boolean gearHovered = mouseX >= cardX + 5 && mouseX <= cardX + 18 && mouseY >= cardY + cardHeight - 16 && mouseY <= cardY + cardHeight - 3;
                    context.drawText(this.textRenderer, "⚙", cardX + 6, cardY + cardHeight - 13, gearHovered ? 0xFFBA68C8 : 0xFF888888, false);
                }
                
                // Bottom-right Toggle Button
                boolean enabled = card.getter.getAsBoolean();
                int btnW = 42;
                int btnH = 12;
                int btnX = cardX + cardWidth - btnW - 6;
                int btnY = cardY + cardHeight - btnH - 5;
                
                boolean btnHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
                int toggleColor = enabled ? 0xFF2E7D32 : 0xFF37474F;
                if (btnHovered) toggleColor = enabled ? 0xFF388E3C : 0xFF455A64;
                
                context.fill(btnX, btnY, btnX + btnW, btnY + btnH, toggleColor);
                context.drawCenteredTextWithShadow(this.textRenderer, enabled ? "Enabled" : "Disabled", btnX + btnW / 2, btnY + 2, 0xFFFFFFFF);
            }
            
            index++;
        }
        
        context.disableScissor();

        // Draw Scrollbar Indicator
        int rowsCount = (filtered.size() + 2) / 3;
        int totalHeight = rowsCount * 55;
        if (totalHeight > cardAreaH) {
            int scrollBarHeight = (int) (((double) cardAreaH / totalHeight) * cardAreaH);
            int scrollBarY = cardAreaY + (int) ((scrollY / (totalHeight - cardAreaH)) * (cardAreaH - scrollBarHeight));
            context.fill(boxX + boxWidth - 4, scrollBarY, boxX + boxWidth - 1, scrollBarY + scrollBarHeight, 0xFF555555);
        }

        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int topBarY = boxY + 4;
        
        // Handle Close X button
        if (mouseX >= boxX + boxWidth - 20 && mouseX <= boxX + boxWidth - 5 && mouseY >= topBarY && mouseY <= topBarY + 20) {
            this.close();
            return true;
        }

        // Handle search field focus
        if (this.searchField != null) {
            boolean clicked = mouseX >= this.searchField.getX() - 4 && mouseX <= this.searchField.getX() + this.searchField.getWidth() + 4
                           && mouseY >= this.searchField.getY() - 2 && mouseY <= this.searchField.getY() + this.searchField.getHeight() + 2;
            this.searchField.setFocused(clicked);
            if (clicked && button == 1) { // Right click to clear
                this.searchField.setText("");
            }
        }

        // Handle Sub-Tabs Clicks
        int subTabY = boxY + 30;
        int tabX = boxX + 10;
        for (Category cat : Category.values()) {
            int textW = this.textRenderer.getWidth(cat.name) + 12;
            if (mouseX >= tabX && mouseX <= tabX + textW && mouseY >= subTabY && mouseY <= subTabY + 16) {
                currentCategory = cat;
                scrollY = 0;
                return true;
            }
            tabX += textW + 6;
        }

        // Handle HUD Layout button click
        int hudBtnWidth = 80;
        int hudBtnX = boxX + boxWidth - hudBtnWidth - 10;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnWidth && mouseY >= subTabY && mouseY <= subTabY + 16) {
            if (this.client != null) {
                this.client.setScreen(new net.featherpojav.client.gui.FeatherHudEditorScreen(this));
            }
            return true;
        }

        // Handle Cards interaction
        int cardAreaY = boxY + 54;
        int cardAreaH = boxHeight - 64;
        int cardWidth = (boxWidth - 30) / 3;
        int cardHeight = 50;

        List<ModCard> filtered = getFilteredCards();
        int index = 0;
        for (ModCard card : filtered) {
            int col = index % 3;
            int row = index / 3;
            
            int cardX = boxX + 10 + col * (cardWidth + 5);
            int cardY = cardAreaY + 6 + row * (cardHeight + 5) - (int) scrollY;
            
            if (cardY + cardHeight >= cardAreaY && cardY <= cardAreaY + cardAreaH) {
                // If gear clicked
                if (card.onConfigure != null) {
                    if (mouseX >= cardX + 5 && mouseX <= cardX + 18 && mouseY >= cardY + cardHeight - 16 && mouseY <= cardY + cardHeight - 3) {
                        card.onConfigure.run();
                        return true;
                    }
                }
                
                // If enabled/disabled button clicked
                int btnW = 42;
                int btnH = 12;
                int btnX = cardX + cardWidth - btnW - 6;
                int btnY = cardY + cardHeight - btnH - 5;
                
                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    card.setter.accept(!card.getter.getAsBoolean());
                    FeatherConfig.save();
                    return true;
                }
            }
            index++;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do nothing to prevent background blur
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
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
// Sub-Settings Configuration Screen: Auto Text
// ==========================================
class FeatherAutoTextScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;

    protected FeatherAutoTextScreen(Screen parent) {
        super(Text.of("Auto Text Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Centered input field
        inputField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Text.of("Edit Macro Command"));
        inputField.setMaxLength(128);
        inputField.setText(FeatherConfig.INSTANCE.autoTextCommand);
        this.addSelectableChild(inputField);

        // Save & Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Back"), button -> {
            FeatherConfig.INSTANCE.autoTextCommand = inputField.getText();
            FeatherConfig.save();
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 50, this.height / 2 + 10, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "AUTO TEXT MACRO CONFIG", this.width / 2, this.height / 2 - 50, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Binds to 'U' key in game", this.width / 2, this.height / 2 - 38, 0xFF888888);
        inputField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

// ==========================================
// Sub-Settings Configuration Screen: TimeChanger
// ==========================================
class FeatherTimeChangerScreen extends Screen {
    private final Screen parent;

    protected FeatherTimeChangerScreen(Screen parent) {
        super(Text.of("TimeChanger Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 95;
        int startY = this.height / 2 - 35;

        // Presets: Morning (0), Day (6000), Sunset (12000), Night (18000)
        this.addDrawableChild(ButtonWidget.builder(Text.of("Morning"), button -> setTime(0, 0)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Day"), button -> setTime(6000, 1)).dimensions(leftX + 100, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Sunset"), button -> setTime(12000, 2)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Night"), button -> setTime(18000, 3)).dimensions(leftX + 100, startY + 25, 90, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 45, startY + 60, 90, 20).build());
    }

    private void setTime(long ticks, int mode) {
        FeatherConfig.INSTANCE.timeChangerTicks = ticks;
        FeatherConfig.INSTANCE.timeChangerMode = mode;
        FeatherConfig.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "TIMECHANGER PRESETS", this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        
        String modeName = "Day";
        if (FeatherConfig.INSTANCE.timeChangerMode == 0) modeName = "Morning";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 2) modeName = "Sunset";
        else if (FeatherConfig.INSTANCE.timeChangerMode == 3) modeName = "Night";
        context.drawCenteredTextWithShadow(this.textRenderer, "Active: " + modeName + " (" + FeatherConfig.INSTANCE.timeChangerTicks + " ticks)", this.width / 2, this.height / 2 - 48, 0xFFBA68C8);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

// ==========================================
// Sub-Settings Configuration Screen: Crosshair
// ==========================================
class FeatherCrosshairScreen extends Screen {
    private final Screen parent;

    protected FeatherCrosshairScreen(Screen parent) {
        super(Text.of("Crosshair Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 95;
        int startY = this.height / 2 - 35;

        // Size adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size +"), button -> adjustSize(0.5f)).dimensions(leftX, startY, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Size -"), button -> adjustSize(-0.5f)).dimensions(leftX + 100, startY, 90, 20).build());

        // Gap adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap +"), button -> adjustGap(0.5f)).dimensions(leftX, startY + 25, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Gap -"), button -> adjustGap(-0.5f)).dimensions(leftX + 100, startY + 25, 90, 20).build());

        // Thickness adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness +"), button -> adjustTh(0.5f)).dimensions(leftX, startY + 50, 90, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Thickness -"), button -> adjustTh(-0.5f)).dimensions(leftX + 100, startY + 50, 90, 20).build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 45, startY + 80, 90, 20).build());
    }

    private void adjustSize(float val) {
        FeatherConfig.INSTANCE.crosshairSize = Math.max(1.0f, FeatherConfig.INSTANCE.crosshairSize + val);
        FeatherConfig.save();
    }

    private void adjustGap(float val) {
        FeatherConfig.INSTANCE.crosshairGap = Math.max(0.0f, FeatherConfig.INSTANCE.crosshairGap + val);
        FeatherConfig.save();
    }

    private void adjustTh(float val) {
        FeatherConfig.INSTANCE.crosshairThickness = Math.max(0.5f, FeatherConfig.INSTANCE.crosshairThickness + val);
        FeatherConfig.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xD0141416);
        context.drawCenteredTextWithShadow(this.textRenderer, "CUSTOM CROSSHAIR CONFIGURATION", this.width / 2, this.height / 2 - 75, 0xFFFFFFFF);
        
        FeatherConfig cfg = FeatherConfig.INSTANCE;
        String desc = String.format("Size: %.1f | Gap: %.1f | Thickness: %.1f", cfg.crosshairSize, cfg.crosshairGap, cfg.crosshairThickness);
        context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, this.height / 2 - 60, 0xFFBA68C8);
        
        // Render a preview of the crosshair in the center top
        int cx = this.width / 2;
        int cy = this.height / 2 - 100;
        float gap = cfg.crosshairGap;
        float size = cfg.crosshairSize;
        float th = cfg.crosshairThickness;
        int color = cfg.crosshairColor;
        context.fill((int)(cx - gap - size), (int)(cy - th/2), (int)(cx - gap), (int)(cy + th/2 + 0.5f), color);
        context.fill((int)(cx + gap), (int)(cy - th/2), (int)(cx + gap + size), (int)(cy + th/2 + 0.5f), color);
        context.fill((int)(cx - th/2), (int)(cy - gap - size), (int)(cx + th/2 + 0.5f), (int)(cy - gap), color);
        context.fill((int)(cx - th/2), (int)(cy + gap), (int)(cx + th/2 + 0.5f), (int)(cy + gap + size), color);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}
