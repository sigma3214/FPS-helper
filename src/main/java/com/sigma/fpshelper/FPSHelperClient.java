package com.sigma.fpshelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FPSHelperClient implements ClientModInitializer {
    public static FPSHelperConfig config;
    private static Path configPath;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/fpshelper.json");
        config = FPSHelperConfig.load(configPath);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fpshelper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.fpshelper"
        ));

        HudRenderCallback.EVENT.register(FPSHelperClient::renderHud);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> config.save(configPath));
    }

    private static void renderHud(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        while (toggleKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save(configPath);
            if (client.player != null) {
                client.player.sendMessage(Text.literal("FPS HELPER: " + (config.enabled ? "ON" : "OFF")), true);
            }
        }

        if (!config.enabled || client.world == null || client.player == null || client.cameraEntity == null) return;
        if (client.currentScreen != null) return;

        List<Target> targets = new ArrayList<>();
        for (Entity entity : client.world.iterateEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == client.player && !config.showSelf) continue;
            if (entity.isSpectator() && config.ignoreSpectators) continue;
            if (!config.showInvisible && entity.isInvisible()) continue;

            double distance = client.player.distanceTo(entity);
            if (distance <= 0.05 || distance > config.maxDistance) continue;

            if (entity instanceof PlayerEntity) {
                if (!config.showPlayers) continue;
            } else {
                if (!config.showMobs) continue;
                boolean hostile = entity instanceof Monster;
                if (hostile && !config.showHostile) continue;
                if (!hostile && !config.showPassive) continue;
            }
            targets.add(new Target(entity, distance));
        }

        targets.sort(Comparator.comparingDouble(t -> t.distance));
        for (Target target : targets) drawArrow(ctx, client, target.entity, target.distance);
    }

    private static void drawArrow(DrawContext ctx, MinecraftClient client, Entity entity, double distance) {
        Entity camera = client.cameraEntity;
        double dx = entity.getX() - camera.getX();
        double dz = entity.getZ() - camera.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.001) return;

        float yaw = camera.getYaw();
        float relative = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(dx, dz)) - yaw);

        // If requested, do not draw an arrow for targets in the front 90-degree cone.
        if (config.hideWhenOnScreen && Math.abs(relative) < 45.0f) return;

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        float cx = width / 2.0f;
        float cy = height / 2.0f;
        float radius = Math.min(config.radius, Math.min(width, height) / 2.0f - config.arrowSize);

        float x = cx + MathHelper.sin(relative * MathHelper.RADIANS_PER_DEGREE) * radius;
        float y = cy - MathHelper.cos(relative * MathHelper.RADIANS_PER_DEGREE) * radius;

        String arrow = (config.arrow == null || config.arrow.isEmpty()) ? "▲" : config.arrow;
        TextRenderer font = client.textRenderer;
        int textWidth = font.getWidth(arrow);
        float textX = x - textWidth / 2.0f;
        float textY = y - config.arrowSize / 2.0f;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(relative));
        ctx.getMatrices().scale(config.arrowSize / 16.0f, config.arrowSize / 16.0f, 1.0f);
        ctx.drawText(font, arrow, -textWidth / 2, -8, config.arrowColor, config.outline);
        ctx.getMatrices().pop();

        if (config.showDistance) {
            String label = Math.round(distance) + "m";
            float scale = config.distanceScale;
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x, y + config.arrowSize * 0.65f, 0);
            ctx.getMatrices().scale(scale, scale, 1.0f);
            ctx.drawText(font, label, -font.getWidth(label) / 2, 0, config.distanceColor, true);
            ctx.getMatrices().pop();
        }
    }

    private record Target(Entity entity, double distance) {}
}
