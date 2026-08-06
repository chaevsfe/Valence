package com.chaevsfe.valence.modules.infohud.client;

import com.chaevsfe.valence.client.ValenceClient;
import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.config.ConfigView;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.infohud.InfoHud;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LightLayer;
import org.lwjgl.glfw.GLFW;

public class InfoHudClient implements ClientModule
{
    private final InfoHud module;
    private KeyMapping toggle;
    private boolean visible;
    private final List<String> lines = new ArrayList<>();

    public InfoHudClient (InfoHud module) {
        this.module = module;
    }

    @Override
    public void initClient () {
        toggle = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.valence.info_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, ValenceClient.KEY_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.addLast(ModConstants.loc("info_hud"), this::extract);
    }

    private void tick (Minecraft client) {
        while (toggle.consumeClick())
            visible = !visible;
        if (!visible || !module.enabled() || client.player == null || client.level == null)
            return;
        rebuild(client);
    }

    private void rebuild (Minecraft client) {
        ConfigView options = module.options();
        lines.clear();
        BlockPos pos = client.player.blockPosition();

        if (options.bool("show_coords"))
            lines.add(String.format("XYZ: %.1f / %.1f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ()));
        if (options.bool("show_facing")) {
            Direction facing = client.player.getDirection();
            lines.add("Facing: " + facing.getName() + " (" + axisOf(facing) + ")");
        }
        if (options.bool("show_biome"))
            lines.add("Biome: " + client.level.getBiome(pos).unwrapKey()
                .map(key -> prettify(key.identifier())).orElse("?"));
        if (options.bool("show_light"))
            lines.add("Light: " + client.level.getBrightness(LightLayer.BLOCK, pos)
                + " block, " + client.level.getBrightness(LightLayer.SKY, pos) + " sky");
        if (options.bool("show_fps"))
            lines.add(client.getFps() + " fps");
        if (options.bool("show_time")) {
            long ticks = client.level.getDefaultClockTime();
            long day = ticks / SharedConstants.TICKS_PER_GAME_DAY;
            long timeOfDay = ticks % SharedConstants.TICKS_PER_GAME_DAY;
            long hour = (timeOfDay / 1000 + 6) % 24;
            long minute = timeOfDay % 1000 * 60 / 1000;
            lines.add(String.format("Day %d, %02d:%02d", day, hour, minute));
        }
    }

    private static String axisOf (Direction facing) {
        return switch (facing) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            default -> "?";
        };
    }

    private static String prettify (Identifier id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }

    private void extract (GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!visible || !module.enabled())
            return;
        Minecraft client = Minecraft.getInstance();
        //? if <26.2 {
        /*boolean hidden = client.options.hideGui;
        *///?} else
        boolean hidden = client.gui.hud.isHidden();
        if (client.player == null || hidden || client.getDebugOverlay().showDebugScreen())
            return;
        int x = 4 + module.options().intOf("x_offset");
        int y = 4 + module.options().intOf("y_offset");
        for (String line : lines) {
            graphics.text(client.font, line, x, y, 0xFFFFFFFF, true);
            y += client.font.lineHeight + 2;
        }
    }
}
