package com.chaevsfe.valence.modules.woodworks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ValenceChestBlock extends ChestBlock
{
    private static final MapCodec<ValenceChestBlock> CODEC = simpleCodec(ValenceChestBlock::new);

    public ValenceChestBlock (Properties properties) {
        super(() -> Woodworks.CHEST_TYPE, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec () {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity (BlockPos pos, BlockState state) {
        return new ValenceChestBlockEntity(pos, state);
    }
}
