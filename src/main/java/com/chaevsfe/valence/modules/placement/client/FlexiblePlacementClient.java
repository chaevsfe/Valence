package com.chaevsfe.valence.modules.placement.client;

import com.chaevsfe.valence.client.ValenceClient;
import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.placement.FlexiblePlacement;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class FlexiblePlacementClient implements ClientModule
{
    private final FlexiblePlacement module;
    private KeyMapping modifier;
    private boolean dispatching;

    public FlexiblePlacementClient (FlexiblePlacement module) {
        this.module = module;
    }

    @Override
    public void initClient () {
        modifier = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.valence.flexible_placement", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, ValenceClient.KEY_CATEGORY));
        UseBlockCallback.EVENT.register(this::onUseBlock);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, ModConstants.loc("placement_indicator"), this::extract);
    }

    private InteractionResult onUseBlock (Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (dispatching || !armed() || !(player instanceof LocalPlayer local))
            return InteractionResult.PASS;
        if (!(player.getItemInHand(hand).getItem() instanceof BlockItem item))
            return InteractionResult.PASS;

        BlockHitResult steered = steer(level, hit, item.getBlock(), player);
        if (steered == null)
            return InteractionResult.PASS;

        dispatching = true;
        try {
            Minecraft.getInstance().gameMode.useItemOn(local, hand, steered);
        }
        finally {
            dispatching = false;
        }
        return InteractionResult.FAIL;
    }

    private static BlockHitResult steer (Level level, BlockHitResult hit, Block block, Player player) {
        BlockPos target = targetOf(level, hit);
        BlockState existing = level.getBlockState(target);
        if (!existing.canBeReplaced() || existing.is(block))
            return null;

        if (block instanceof RotatedPillarBlock) {
            Direction face = Direction.fromAxisAndDirection(player.getDirection().getAxis(), Direction.AxisDirection.POSITIVE);
            return new BlockHitResult(Vec3.atCenterOf(target), face, target, false);
        }
        if (block instanceof StairBlock || block instanceof SlabBlock || block instanceof TrapDoorBlock) {
            Direction face = hit.getDirection().getAxis().isHorizontal()
                ? hit.getDirection()
                : player.getDirection().getOpposite();
            boolean top = !vanillaWantsTop(hit, target);
            Vec3 at = new Vec3(target.getX() + 0.5, target.getY() + (top ? 0.75 : 0.25), target.getZ() + 0.5);
            return new BlockHitResult(at, face, target, false);
        }
        return null;
    }

    private static boolean vanillaWantsTop (BlockHitResult hit, BlockPos target) {
        Direction face = hit.getDirection();
        if (face == Direction.DOWN)
            return true;
        return face != Direction.UP && hit.getLocation().y - target.getY() > 0.5;
    }

    private static BlockPos targetOf (Level level, BlockHitResult hit) {
        BlockPos clicked = hit.getBlockPos();
        return level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(hit.getDirection());
    }

    private boolean armed () {
        return module.enabled() && modifier != null && modifier.isDown();
    }

    private void extract (GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!armed() || !module.options().bool("indicator"))
            return;
        Minecraft client = Minecraft.getInstance();
        //? if <26.2 {
        /*boolean hidden = client.options.hideGui;
        *///?} else
        boolean hidden = client.gui.hud.isHidden();
        if (hidden || client.getDebugOverlay().showDebugScreen())
            return;
        String mark = "⤓";
        graphics.text(client.font, mark, graphics.guiWidth() / 2 - client.font.width(mark) / 2,
            graphics.guiHeight() / 2 + 10, 0xFFFFFF55, true);
    }
}
