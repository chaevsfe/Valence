package com.chaevsfe.valence.modules.inventoryactions.client;

import com.chaevsfe.valence.modules.inventoryactions.SortLogic;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class ClickPlan
{
    public record Step (int slot, ContainerInput type, ItemStack expect) { }

    private final List<Integer> region;
    private final List<ItemStack> sim;
    private final List<Step> steps = new ArrayList<>();
    private ItemStack carried = ItemStack.EMPTY;

    private ClickPlan (AbstractContainerMenu menu, List<Integer> region) {
        this.region = region;
        this.sim = new ArrayList<>(region.size());
        for (int index : region)
            sim.add(menu.slots.get(index).getItem().copy());
    }

    public static List<Step> sort (AbstractContainerMenu menu, List<Integer> region) {
        ClickPlan plan = new ClickPlan(menu, region);
        plan.mergePartials();
        plan.arrange();
        return plan.steps;
    }

    public static List<Step> quickMoveAll (AbstractContainerMenu menu, List<Integer> from) {
        List<Step> steps = new ArrayList<>();
        for (int index : from) {
            ItemStack stack = menu.slots.get(index).getItem();
            if (!stack.isEmpty())
                steps.add(new Step(index, ContainerInput.QUICK_MOVE, stack.copy()));
        }
        return steps;
    }

    public static List<Step> quickMoveMatching (AbstractContainerMenu menu, List<Integer> from, List<Integer> match) {
        List<Step> steps = new ArrayList<>();
        for (int index : from) {
            ItemStack stack = menu.slots.get(index).getItem();
            if (stack.isEmpty())
                continue;
            for (int other : match) {
                if (ItemStack.isSameItemSameComponents(stack, menu.slots.get(other).getItem())) {
                    steps.add(new Step(index, ContainerInput.QUICK_MOVE, stack.copy()));
                    break;
                }
            }
        }
        return steps;
    }

    private void mergePartials () {
        List<ItemStack> done = new ArrayList<>();
        for (int i = 0; i < sim.size(); i++) {
            if (sim.get(i).isEmpty())
                continue;
            ItemStack kind = sim.get(i).copyWithCount(1);
            if (done.stream().anyMatch(other -> ItemStack.isSameItemSameComponents(kind, other)))
                continue;
            done.add(kind);
            while (true) {
                int smallest = partialOf(kind, -1, true);
                int largest = partialOf(kind, smallest, false);
                if (smallest < 0 || largest < 0)
                    break;
                click(smallest);
                click(largest);
                if (!carried.isEmpty())
                    click(smallest);
            }
        }
    }

    private int partialOf (ItemStack kind, int exclude, boolean smallest) {
        int found = -1;
        for (int k = 0; k < sim.size(); k++) {
            if (k == exclude)
                continue;
            ItemStack stack = sim.get(k);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(kind, stack) || stack.getCount() >= stack.getMaxStackSize())
                continue;
            if (found < 0 || smallest == (stack.getCount() < sim.get(found).getCount()))
                found = k;
        }
        return found;
    }

    private void arrange () {
        List<ItemStack> target = new ArrayList<>();
        for (ItemStack stack : sim)
            if (!stack.isEmpty())
                target.add(stack.copy());
        target.sort(SortLogic.ORDER);
        while (target.size() < sim.size())
            target.add(ItemStack.EMPTY);

        for (int i = 0; i < sim.size(); i++) {
            if (ItemStack.matches(sim.get(i), target.get(i)))
                continue;
            int j = -1;
            for (int k = i + 1; k < sim.size(); k++) {
                if (ItemStack.matches(sim.get(k), target.get(i))) {
                    j = k;
                    break;
                }
            }
            if (j < 0)
                continue;
            click(j);
            click(i);
            if (!carried.isEmpty())
                click(j);
        }
    }

    private void click (int regionIndex) {
        ItemStack inSlot = sim.get(regionIndex);
        steps.add(new Step(region.get(regionIndex), ContainerInput.PICKUP, inSlot.copy()));
        if (carried.isEmpty()) {
            sim.set(regionIndex, ItemStack.EMPTY);
            carried = inSlot;
        } else if (inSlot.isEmpty()) {
            sim.set(regionIndex, carried);
            carried = ItemStack.EMPTY;
        } else if (ItemStack.isSameItemSameComponents(carried, inSlot)) {
            int moved = Math.min(inSlot.getMaxStackSize() - inSlot.getCount(), carried.getCount());
            inSlot.grow(moved);
            carried.shrink(moved);
            if (carried.isEmpty())
                carried = ItemStack.EMPTY;
        } else {
            sim.set(regionIndex, carried);
            carried = inSlot;
        }
    }
}
