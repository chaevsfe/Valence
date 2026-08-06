package com.chaevsfe.valence.modules.woodworks;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.chaevsfe.valence.modules.woodworks.client.WoodworksClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Woodworks extends ValenceModule
{
    public static BlockEntityType<ValenceChestBlockEntity> CHEST_TYPE;

    private final List<Item> ladders = new ArrayList<>();
    private final List<Item> bookshelves = new ArrayList<>();
    private final List<Item> chests = new ArrayList<>();
    private final List<Item> posts = new ArrayList<>();
    private final List<Block> flammableShelves = new ArrayList<>();
    private final List<Block> flammablePosts = new ArrayList<>();

    public Woodworks () {
        super("woodworks", ModuleCategory.BUILDING, ModuleSide.COMMON,
            "Ladders, bookshelves, posts and chests for every wood type");
    }

    @Override
    public void register () {
        List<Block> chestBlocks = new ArrayList<>();
        for (WoodworksTable.Row row : WoodworksTable.ROWS) {
            Block planks = BuiltInRegistries.BLOCK.getValue(Identifier.parse(row.planksBlock()));
            if (planks == Blocks.AIR)
                continue;

            if (!row.name().equals("oak")) {
                Block ladder = new BlockWoodLadder(copy(Blocks.LADDER, row.name() + "_ladder"));
                ladders.add(registerWithItem(row.name() + "_ladder", ladder));

                Block shelf = new Block(copy(Blocks.BOOKSHELF, row.name() + "_bookshelf"));
                bookshelves.add(registerWithItem(row.name() + "_bookshelf", shelf));
                if (!row.stem())
                    flammableShelves.add(shelf);

                Block chest = new ValenceChestBlock(copy(Blocks.CHEST, row.name() + "_chest"));
                chests.add(registerWithItem(row.name() + "_chest", chest));
                chestBlocks.add(chest);
            }

            if (row.logBlock() != null) {
                Block log = BuiltInRegistries.BLOCK.getValue(Identifier.parse(row.logBlock()));
                if (log != Blocks.AIR) {
                    Block post = new BlockPost(copy(log, row.name() + "_post"));
                    posts.add(registerWithItem(row.name() + "_post", post));
                    if (!row.stem())
                        flammablePosts.add(post);
                }
            }
        }
        CHEST_TYPE = FabricBlockEntityTypeBuilder.create(ValenceChestBlockEntity::new, chestBlocks.toArray(new Block[0])).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModConstants.loc("wood_chest"), CHEST_TYPE);
    }

    private static BlockBehaviour.Properties copy (Block source, String id) {
        return BlockBehaviour.Properties.ofFullCopy(source)
            .setId(ResourceKey.create(Registries.BLOCK, ModConstants.loc(id)));
    }

    private static Item registerWithItem (String id, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, ModConstants.loc(id), block);
        Item item = new BlockItem(block, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ModConstants.loc(id)))
            .useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, ModConstants.loc(id), item);
        return item;
    }

    @Override
    public void init () {
        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammableShelves.forEach(block -> flammable.add(block, 30, 20));
        flammablePosts.forEach(block -> flammable.add(block, 5, 5));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            if (!enabled())
                return;
            output.insertAfter(Blocks.LADDER, ladders.toArray(new ItemLike[0]));
            output.insertAfter(Blocks.BOOKSHELF, bookshelves.toArray(new ItemLike[0]));
            output.insertAfter(Blocks.CHEST, chests.toArray(new ItemLike[0]));
        });
    }

    @Override
    public void addTabItems (CreativeModeTab.Output output) {
        ladders.forEach(output::accept);
        bookshelves.forEach(output::accept);
        posts.forEach(output::accept);
        chests.forEach(output::accept);
    }

    @Override
    public Supplier<ClientModule> client () {
        return WoodworksClient::new;
    }

    private static class BlockWoodLadder extends LadderBlock
    {
        BlockWoodLadder (Properties properties) {
            super(properties);
        }
    }
}
