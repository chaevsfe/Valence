package com.chaevsfe.valence.modules.satchel;

import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ItemSeedSatchel extends Item
{
    private static final int SCAN_INTERVAL = 20;

    public ItemSeedSatchel (Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use (Level level, Player player, InteractionHand hand) {
        ItemStack satchel = player.getItemInHand(hand);
        if (!SeedSatchel.instance().enabled())
            return InteractionResult.PASS;
        if (player.isSecondaryUseActive())
            return toggleCollecting(satchel, player);
        player.openMenu(new SimpleMenuProvider(
            (containerId, inventory, opener) -> new SatchelMenu(containerId, inventory, new SatchelContainer(satchel)),
            satchel.getHoverName()));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult toggleCollecting (ItemStack satchel, Player player) {
        boolean on = !SeedSatchel.collecting(satchel);
        satchel.set(SeedSatchel.COLLECTING, on);
        if (player instanceof ServerPlayer server)
            server.sendSystemMessage(Component.translatable(
                on ? "valence.seed_satchel.collecting_on" : "valence.seed_satchel.collecting_off"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn (UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !SeedSatchel.instance().enabled())
            return InteractionResult.PASS;
        if (player.isSecondaryUseActive())
            return InteractionResult.PASS;

        ItemStack satchel = context.getItemInHand();
        SatchelContainer contents = new SatchelContainer(satchel);
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
            context.getClickedPos(), context.isInside());

        for (int slot = 0; slot < SatchelContainer.SIZE; slot++) {
            ItemStack seed = contents.getItem(slot);
            if (seed.isEmpty())
                continue;
            InteractionResult result = seed.useOn(
                new SatchelPlaceContext(context.getLevel(), player, context.getHand(), seed, hit));
            if (result.consumesAction()) {
                contents.setItem(slot, seed.isEmpty() ? ItemStack.EMPTY : seed);
                contents.setChanged();
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick (ItemStack satchel, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player) || level.getGameTime() % SCAN_INTERVAL != 0)
            return;
        SeedSatchel module = SeedSatchel.instance();
        if (!module.enabled() || !module.options().bool("absorb") || !SeedSatchel.collecting(satchel))
            return;
        if (player.containerMenu instanceof SatchelMenu)
            return;

        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> items = inventory.getNonEquipmentItems();
        SatchelContainer contents = null;
        for (int i = 0; i < items.size(); i++) {
            if (i == inventory.getSelectedSlot())
                continue;
            ItemStack stack = items.get(i);
            if (stack == satchel || stack.isEmpty() || !SeedSatchel.isPlantable(stack))
                continue;
            if (contents == null)
                contents = new SatchelContainer(satchel);
            ItemStack left = contents.addItem(stack);
            items.set(i, left.isEmpty() ? ItemStack.EMPTY : left);
        }
        if (contents != null)
            contents.setChanged();
    }

    @Override
    public void appendHoverText (ItemStack satchel, TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> lines, TooltipFlag flag) {
        ItemContainerContents stored = satchel.get(DataComponents.CONTAINER);
        if (stored != null)
            lines.accept(Component.translatable("valence.seed_satchel.fill",
                stored.nonEmptyItemCopyStream().count(), SatchelContainer.SIZE));
        lines.accept(Component.translatable(SeedSatchel.collecting(satchel)
            ? "valence.seed_satchel.collecting_on" : "valence.seed_satchel.collecting_off"));
    }

    private static class SatchelPlaceContext extends UseOnContext
    {
        SatchelPlaceContext (Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
            super(level, player, hand, stack, hit);
        }
    }
}
