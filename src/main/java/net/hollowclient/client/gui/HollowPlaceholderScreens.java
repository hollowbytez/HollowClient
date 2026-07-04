package net.hollowclient.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

class HollowPlaceholderScreen extends Screen {
    protected final Screen parent;
    private final String title;

    protected HollowPlaceholderScreen(Screen parent, String title) {
        super(Text.of(title));
        this.parent = parent;
        this.title = title;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF141416);
        context.drawCenteredTextWithShadow(this.textRenderer, title.toUpperCase(), this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Press ESC to return", this.width / 2, this.height / 2 + 10, 0xFF8A8C96);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}

class HollowGlobalSettingsScreen extends HollowPlaceholderScreen {
    HollowGlobalSettingsScreen(Screen parent) { super(parent, "Global Settings (WIP)"); }
}

class HollowCosmeticsScreen extends HollowPlaceholderScreen {
    HollowCosmeticsScreen(Screen parent) { super(parent, "Cosmetics (WIP)"); }
}

class HollowMacrosScreen extends HollowPlaceholderScreen {
    HollowMacrosScreen(Screen parent) { super(parent, "Macros & Keybinds (WIP)"); }
}

class HollowFriendsScreen extends HollowPlaceholderScreen {
    HollowFriendsScreen(Screen parent) { super(parent, "Profiles & Friends (WIP)"); }
}

