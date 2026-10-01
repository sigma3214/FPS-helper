package com.sigma.fpshelper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class FPSHelperClient implements ClientModInitializer {
    public static FPSHelperConfig config;
    private static KeyBinding toggleMenuKey;

    @Override
    public void onInitializeClient() {
        config = FPSHelperConfig.load();

        toggleMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fpshelper.toggle",
                InputUtil.Type.KEY_SYM,
                GLFW.GLFW_KEY_H,
                "category.fpshelper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleMenuKey.wasPressed()) {
                config.showPlayers = !config.showPlayers;
                config.showMobs = !config.showMobs;
                config.save();
                if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal("§6[FPS Helper] §fИгроки: " + (config.showPlayers ? "§aВкл" : "§cВыкл") + " §f| Мобы: " + (config.showMobs ? "§aВкл" : "§cВыкл")), true);
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            double px = client.player.getX();
            double pz = client.player.getZ();
            float pYaw = client.player.getYaw();

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            int radius = 80; // Радиус круга, по которому крутятся стрелочки

            for (Entity entity : client.world.getEntities()) {
                if (entity == client.player) continue;

                boolean isPlayer = entity instanceof PlayerEntity;
                boolean isMob = entity instanceof MobEntity;

                if ((isPlayer && !config.showPlayers) || (isMob && !config.showMobs)) continue;
                if (!isPlayer && !isMob) continue;

                double ex = entity.getX();
                double ez = entity.getZ();

                double dx = ex - px;
                double dz = ez - pz;

                double angleToEntity = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
                double angleDiff = angleToEntity - pYaw;

                double rad = Math.toRadians(angleDiff);
                
                // Координаты иконки/стрелочки на экране в виде компаса вокруг прицела
                int x = (int) (centerX + radius * Math.sin(rad));
                int y = (int) (centerY - radius * Math.cos(rad));

                int color = isPlayer ? config.arrowColorPlayers : config.arrowColorMobs;

                // Рисуем стрелочку (простой символ или квадратик)
                drawContext.drawText(client.textRenderer, "▲", x - 4, y - 4, color, true);
            }
        });
    }
}