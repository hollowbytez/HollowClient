package net.hollowclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class HollowConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hollowclient.json");
    
    public static HollowConfig INSTANCE = new HollowConfig();

    // General Client Mod Toggles
    // Category: HUD
    public boolean keystrokes = true;
    public boolean armorHUD = true;
    public boolean potionHUD = true;
    public boolean directionHUD = true;
    public boolean coordHUD = true;
    public boolean fpsHUD = true;
    public boolean comboDisplay = true;
    public boolean pingDisplay = true;
    public boolean playtime = true;
    public boolean reachDisplay = true;
    public boolean serverAddress = true;
    public boolean speedMeter = true;
    public boolean stopwatch = true;
    public boolean itemCounter = true;
    public boolean armorBar = true;
    public boolean armorStatus = true;
    public boolean bossBar = true;
    public boolean hearts = true;
    public boolean packDisplay = true;
    public boolean scoreboard = true;

    // Category: Gameplay
    public boolean autoText = false;
    public boolean dropPrevention = false;
    public boolean toggleSprint = true;
    public boolean zoom = true;
    public boolean freelook = true;
    public boolean autoGG = true;
    public boolean timeChanger = false;

    // Category: Performance
    public boolean cullLogs = false;

    // Category: Cosmetics
    public boolean blockOverlay = false;
    public boolean fullbright = false;
    public boolean customCrosshair = false;
    public boolean dragonWings = false;
    public boolean damageIndicator = true;
    public boolean crystalOptimizer = true;
    public boolean totemCounter = true;
    public boolean saturationHUD = true;
    public boolean glint = false;
    public boolean hitbox = false;
    public boolean lootBeams = false;
    public boolean nickHider = false;
    public boolean itemPhysics = true;
    public boolean lowFire = true;
    public boolean hurtCam = true;
    public boolean shortSwords = false;
    public boolean shortShields = false;
    public boolean autoClicker = false;
    public int autoClickerMinCPS = 8;
    public int autoClickerMaxCPS = 12;
    public boolean noBobbing = false;

    // Custom mod details
    public String autoTextCommand = "/lobby";
    public int timeChangerMode = 1; // 0 = Morning, 1 = Day, 2 = Sunset, 3 = Night

    // HUD Positions
    public int keystrokesX = 10;
    public int keystrokesY = 100;
    
    public int armorHUDX = 10;
    public int armorHUDY = 240;
    public boolean armorHUDVertical = true;
    
    public int potionHUDX = 10;
    public int potionHUDY = 320;
    
    public int directionHUDX = 200;
    public int directionHUDY = 10;
    
    public int coordHUDX = 10;
    public int coordHUDY = 10;
    
    public int fpsHUDX = 10;
    public int fpsHUDY = 50;
    
    public int targetHudX = 10;
    public int targetHudY = 110;

    public int totemCounterX = 10;
    public int totemCounterY = 330;

    public int saturationHUDX = 10;
    public int saturationHUDY = 350;

    public int comboDisplayX = 10;
    public int comboDisplayY = 70;

    public int pingDisplayX = 10;
    public int pingDisplayY = 150;

    public int playtimeX = 10;
    public int playtimeY = 170;

    public int reachDisplayX = 10;
    public int reachDisplayY = 190;

    public int serverAddressX = 10;
    public int serverAddressY = 210;

    public int speedMeterX = 10;
    public int speedMeterY = 230;

    public int stopwatchX = 10;
    public int stopwatchY = 250;

    public int itemCounterX = 10;
    public int itemCounterY = 270;

    public int packDisplayX = 10;
    public int packDisplayY = 290;
    
    // Crosshair Customize
    public int crosshairColor = 0xFF00FF00;
    public float crosshairSize = 5.0f;
    public float crosshairThickness = 1.0f;
    public float crosshairGap = 2.0f;
    public int crosshairPreset = 0;
    public float zoomMagnification = 4.0f;
    
    public long timeChangerTicks = 6000;
    public float particleMultiplier = 1.0f;
    
    public int themeColor = 0xFF9C27B0;
    public int backgroundColor = 0x80000000;
    public int textColor = 0xFFFFFFFF;
    public float menuScale = 1.0f;
    
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, HollowConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new HollowConfig();
                }
            } catch (Exception e) {
                System.err.println("[hollowclient] Failed to load config: " + e.getMessage());
                INSTANCE = new HollowConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            System.err.println("[hollowclient] Failed to save config: " + e.getMessage());
        }
    }
}

