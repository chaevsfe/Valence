package com.chaevsfe.valence.modules.inventoryactions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SortLogic
{
    public static final Comparator<ItemStack> ORDER = Comparator
        .comparingInt((ItemStack stack) -> BuiltInRegistries.ITEM.getId(stack.getItem()))
        .thenComparingInt(stack -> stack.getComponents().hashCode())
        .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());

    private SortLogic () { }

    public static List<Integer> containerRegion (AbstractContainerMenu menu, Player player) {
        List<Integer> region = new ArrayList<>();
        for (Slot slot : menu.slots)
            if (slot.container != player.getInventory() && slot.getClass() == Slot.class && slot.mayPickup(player))
                region.add(slot.index);
        return region.size() >= 5 ? region : List.of();
    }

    public static List<Integer> playerRegion (AbstractContainerMenu menu, Player player, boolean hotbar) {
        List<Integer> region = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container != player.getInventory())
                continue;
            int index = slot.getContainerSlot();
            if (index >= 9 && index < 36 || hotbar && index < 9)
                region.add(slot.index);
        }
        return region;
    }

    public static boolean sortRegion (AbstractContainerMenu menu, List<Integer> region, Player player) {
        for (int index : region) {
            Slot slot = menu.getSlot(index);
            if (!slot.mayPickup(player))
                return false;
        }

        List<ItemStack> merged = new ArrayList<>();
        for (int index : region) {
            ItemStack stack = menu.getSlot(index).getItem().copy();
            if (stack.isEmpty())
                continue;
            for (ItemStack existing : merged) {
                if (stack.isEmpty())
                    break;
                if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                    int moved = Math.min(existing.getMaxStackSize() - existing.getCount(), stack.getCount());
                    existing.grow(moved);
                    stack.shrink(moved);
                }
            }
            if (!stack.isEmpty())
                merged.add(stack);
        }
        merged.sort(ORDER);

        for (int i = 0; i < region.size(); i++) {
            ItemStack target = i < merged.size() ? merged.get(i) : ItemStack.EMPTY;
            Slot slot = menu.getSlot(region.get(i));
            if (!target.isEmpty() && !slot.mayPlace(target))
                return false;
            slot.set(target);
        }
        menu.broadcastChanges();
        return true;
    }
}
