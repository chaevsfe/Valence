package com.chaevsfe.valence.modules.trough;

import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class BlockEntityAnimalTrough extends BlockEntity implements WorldlyContainer
{
    private static final int[] ALL_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private int cooldown;

    public BlockEntityAnimalTrough (BlockPos pos, BlockState state) {
        super(AnimalTrough.TYPE, pos, state);
    }

    static void serverTick (Level level, BlockPos pos, BlockEntityAnimalTrough trough) {
        if (--trough.cooldown > 0)
            return;
        trough.cooldown = 30 + level.getRandom().nextInt(20);
        if (trough.isEmpty() || !(level instanceof ServerLevel server))
            return;
        AnimalTrough module = AnimalTrough.instance();
        if (module == null || !module.enabled())
            return;

        int range = module.options().intOf("range");
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(range));
        if (animals.size() >= server.getGameRules().get(GameRules.MAX_ENTITY_CRAMMING))
            return;

        for (Animal animal : animals) {
            if (!animal.isAlive() || animal.isBaby() || animal.getAge() != 0 || !animal.canFallInLove())
                continue;
            if (trough.feed(animal))
                animal.setAttached(AnimalTrough.TROUGH_FED, true);
        }
    }

    private boolean feed (Animal animal) {
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty() || !animal.isFood(stack))
                continue;
            stack.shrink(1);
            setChanged();
            animal.setInLove(null);
            return true;
        }
        return false;
    }

    int insert (ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < items.size() && remaining > 0; slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty()) {
                items.set(slot, stack.copyWithCount(remaining));
                remaining = 0;
            }
            else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int room = existing.getMaxStackSize() - existing.getCount();
                int moved = Math.min(room, remaining);
                existing.grow(moved);
                remaining -= moved;
            }
        }
        if (remaining != stack.getCount())
            setChanged();
        return remaining;
    }

    @Override
    public void preRemoveSideEffects (BlockPos pos, BlockState state) {
        if (level != null)
            Containers.dropContents(level, pos, this);
    }

    @Override
    protected void saveAdditional (ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("DataVersion", SharedConstants.WORLD_VERSION);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void loadAdditional (ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    public int getContainerSize () {
        return items.size();
    }

    @Override
    public boolean isEmpty () {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem (int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem (int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty())
            setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate (int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem (int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid (Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent () {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace (Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace (int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace (int slot, ItemStack stack, Direction side) {
        return true;
    }
}
