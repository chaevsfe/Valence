package com.chaevsfe.valence.modules.satchel;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class SatchelContainer extends SimpleContainer
{
    public static final int SIZE = 9;

    private final ItemStack satchel;

    public SatchelContainer (ItemStack satchel) {
        super(SIZE);
        this.satchel = satchel;
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ItemContainerContents stored = satchel.get(DataComponents.CONTAINER);
        if (stored != null)
            stored.copyInto(items);
        for (int i = 0; i < SIZE; i++)
            setItem(i, items.get(i));
    }

    @Override
    public boolean canPlaceItem (int slot, ItemStack stack) {
        return SeedSatchel.isPlantable(stack);
    }

    @Override
    public void setChanged () {
        satchel.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }

    @Override
    public boolean stillValid (Player player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems())
            if (stack == satchel)
                return true;
        return player.getOffhandItem() == satchel;
    }
}
