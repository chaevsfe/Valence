package com.chaevsfe.valence.modules.verticalslabs;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VerticalSlabs extends ValenceModule
{
    private record Entry (Block parent, Item item) { }

    private final List<Entry> entries = new ArrayList<>();

    public VerticalSlabs () {
        super("vertical_slabs", ModuleCategory.BUILDING, ModuleSide.COMMON,
            "Vertical counterparts for every vanilla slab");
    }

    @Override
    public void register () {
        for (VerticalSlabTable.Row row : VerticalSlabTable.ROWS) {
            Block slab = BuiltInRegistries.BLOCK.getValue(Identifier.parse(row.slabId()));
            if (slab == Blocks.AIR)
                continue;
            String id = row.base() + "_vertical_slab";
            BlockVerticalSlab block = new BlockVerticalSlab(BlockBehaviour.Properties.ofFullCopy(slab)
                .setId(ResourceKey.create(Registries.BLOCK, ModConstants.loc(id))));
            Registry.register(BuiltInRegistries.BLOCK, ModConstants.loc(id), block);
            BlockItem item = new BlockItem(block, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, ModConstants.loc(id)))
                .useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, ModConstants.loc(id), item);
            entries.add(new Entry(slab, item));
        }
    }

    @Override
    public void init () {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            if (!enabled())
                return;
            for (Entry entry : entries)
                output.insertAfter(entry.parent(), entry.item());
        });
    }
}
