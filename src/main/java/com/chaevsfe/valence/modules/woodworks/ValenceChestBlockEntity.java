package com.chaevsfe.valence.modules.woodworks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ValenceChestBlockEntity extends ChestBlockEntity
{
    public ValenceChestBlockEntity (BlockPos pos, BlockState state) {
        super(Woodworks.CHEST_TYPE, pos, state);
    }
}
