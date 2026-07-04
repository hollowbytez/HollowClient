package net.hollowclient.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class HollowHudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "hollowclient");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "hud_config.json");

    public static class HudElementData {
        public boolean enabled = true;
        public float x = 10f;
        public float y = 10f;
        public float scale = 1.0f;
    }

    public static Map<String, HudElementData> ELEMENTS = new HashMap<>();

    public static HudElementData get(String id) {
        return ELEMENTS.computeIfAbsent(id, k -> new HudElementData());
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Map<String, HudElementData> loaded = GSON.fromJson(reader, new TypeToken<Map<String, HudElementData>>(){}.getType());
            if (loaded != null) ELEMENTS = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(ELEMENTS, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

