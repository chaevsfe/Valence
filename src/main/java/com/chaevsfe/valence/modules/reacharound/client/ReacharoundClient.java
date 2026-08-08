package com.chaevsfe.valence.modules.reacharound.client;

import com.chaevsfe.valence.client.ServerSession;
import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.reacharound.Reacharound;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ReacharoundClient implements ClientModule
{
    private final Reacharound module;
    private BlockHitResult target;

    public ReacharoundClient (Reacharound module) {
        this.module = module;
    }

    @Override
    public void initClient () {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        UseItemCallback.EVENT.register(this::onUseItem);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, ModConstants.loc("reacharound_indicator"), this::extract);
    }

    private void tick (Minecraft client) {
        target = null;
        if (!module.enabled() || client.player == null || client.level == null || !armed(client))
            return;
        target = compute(client);
    }

    private static boolean armed (Minecraft client) {
        return client.hasSingleplayerServer() || ServerSession.allows("reacharound");
    }

    private BlockHitResult compute (Minecraft client) {
        LocalPlayer player = client.player;
        if (!player.onGround())
            return null;
        if (client.hitResult != null && client.hitResult.getType() != HitResult.Type.MISS)
            return null;
        if (player.getXRot() < module.options().doubleOf("pitch_threshold"))
            return null;
        if (!(player.getMainHandItem().getItem() instanceof BlockItem) && !(player.getOffhandItem().getItem() instanceof BlockItem))
            return null;

        Level level = client.level;
        BlockPos feet = player.blockPosition();
        BlockPos support = level.getBlockState(feet).canBeReplaced() ? feet.below() : feet;
        if (level.getBlockState(support).isAir())
            return null;
        if (!player.isWithinBlockInteractionRange(support, 1.0))
            return null;
        if (player.getXRot() > 70.0f && level.getBlockState(support.below()).canBeReplaced())
            return new BlockHitResult(Vec3.atCenterOf(support).add(0.0, -0.5, 0.0), Direction.DOWN, support, false);
        Direction facing = player.getDirection();
        if (!level.getBlockState(support.relative(facing)).canBeReplaced())
            return null;
        Vec3 hit = Vec3.atCenterOf(support).add(facing.getStepX() * 0.5, 0.0, facing.getStepZ() * 0.5);
        return new BlockHitResult(hit, facing, support, false);
    }

    private InteractionResult onUseItem (Player player, Level level, InteractionHand hand) {
        if (target == null || !module.enabled() || !level.isClientSide())
            return InteractionResult.PASS;
        Minecraft client = Minecraft.getInstance();
        if (player != client.player || !(player.getItemInHand(hand).getItem() instanceof BlockItem))
            return InteractionResult.PASS;
        InteractionResult result = client.gameMode.useItemOn(client.player, hand, target);
        if (result.consumesAction())
            player.swing(hand);
        target = null;
        return InteractionResult.CONSUME;
    }

    private void extract (GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (target == null || !module.enabled() || !module.options().bool("indicator"))
            return;
        Minecraft client = Minecraft.getInstance();
        //? if <26.2 {
        /*boolean hidden = client.options.hideGui;
        *///?} else
        boolean hidden = client.gui.hud.isHidden();
        if (hidden || client.getDebugOverlay().showDebugScreen())
            return;
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 - 4;
        graphics.text(client.font, "[", x - 9 - client.font.width("["), y, 0xFFFFFFFF, true);
        graphics.text(client.font, "]", x + 9, y, 0xFFFFFFFF, true);
    }
}
