package com.chaevsfe.valence.modules.satchel;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;

public class SatchelMenu extends DispenserMenu
{
    public SatchelMenu (int containerId, Inventory playerInventory, Container contents) {
        super(containerId, playerInventory, contents);
    }

    @Override
    public void clicked (int slotId, int button, ContainerInput input, Player player) {
        if (slotId >= 0) {
            if (getSlot(slotId).getItem().is(SeedSatchel.SATCHEL))
                return;
            if (slotId < SatchelContainer.SIZE && !incoming(button, input, player).isEmpty()
                && !SeedSatchel.isPlantable(incoming(button, input, player)))
                return;
        }
        super.clicked(slotId, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack (Player player, int index) {
        if (index >= SatchelContainer.SIZE && !SeedSatchel.isPlantable(getSlot(index).getItem()))
            return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }

    private ItemStack incoming (int button, ContainerInput input, Player player) {
        if (input != ContainerInput.SWAP)
            return getCarried();
        return button >= 0 ? player.getInventory().getItem(button) : ItemStack.EMPTY;
    }
}
