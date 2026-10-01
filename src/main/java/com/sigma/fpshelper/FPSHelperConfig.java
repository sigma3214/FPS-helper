package com.sigma.fpshelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FPSHelperConfig {
    public boolean enabled = true;
    public boolean showPlayers = true;
    public boolean showMobs = true;
    public boolean showHostile = true;
    public boolean showPassive = true;
    public boolean showInvisible = false;
    public boolean showSelf = false;
    public double maxDistance = 128.0;
    public float radius = 120.0f;
    public float arrowSize = 16.0f;
    public int arrowColor = 0xFFFFFFFF;
    public int outlineColor = 0xB0000000;
    public boolean outline = true;
    public String arrow = "▲";
    public boolean showDistance = true;
    public int distanceColor = 0xFFFFFFFF;
    public float distanceScale = 0.75f;
    public boolean hideWhenOnScreen = false;
    public boolean ignoreSpectators = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static FPSHelperConfig load(Path path) {
        try {
            if (Files.exists(path)) {
                return GSON.fromJson(Files.readString(path), FPSHelperConfig.class);
            }
        } catch (Exception ignored) { }
        FPSHelperConfig config = new FPSHelperConfig();
        config.save(path);
        return config;
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (IOException ignored) { }
    }
}
