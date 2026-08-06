package com.chaevsfe.valence.modules.trough;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AnimalTrough extends ValenceModule
{
    public static final AttachmentType<Boolean> TROUGH_FED = AttachmentRegistry.create(
        ModConstants.loc("trough_fed"), builder -> builder.persistent(Codec.BOOL));

    private static AnimalTrough instance;

    public static BlockAnimalTrough BLOCK;
    public static BlockEntityType<BlockEntityAnimalTrough> TYPE;

    private Item item;

    public AnimalTrough () {
        super("animal_trough", ModuleCategory.UTILITY, ModuleSide.COMMON,
            "A trough that feeds and breeds nearby animals without experience drops");
        instance = this;
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.intOf("range", 5, 2, 12, "Radius in blocks scanned for animals")
            .bool("suppress_xp", true, "Trough-bred animals drop no experience");
    }

    @Override
    public void register () {
        BLOCK = new BlockAnimalTrough(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
            .setId(ResourceKey.create(Registries.BLOCK, ModConstants.loc("animal_trough"))));
        Registry.register(BuiltInRegistries.BLOCK, ModConstants.loc("animal_trough"), BLOCK);
        item = new BlockItem(BLOCK, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ModConstants.loc("animal_trough")))
            .useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, ModConstants.loc("animal_trough"), item);
        TYPE = FabricBlockEntityTypeBuilder.create(BlockEntityAnimalTrough::new, BLOCK).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModConstants.loc("animal_trough"), TYPE);
    }

    @Override
    public void addTabItems (CreativeModeTab.Output output) {
        output.accept(item);
    }

    static AnimalTrough instance () {
        return instance;
    }

    public static boolean consumeSuppression (Animal parent, Animal otherParent) {
        boolean fed = parent.getAttachedOrElse(TROUGH_FED, false)
            && otherParent.getAttachedOrElse(TROUGH_FED, false);
        if (fed) {
            parent.removeAttached(TROUGH_FED);
            otherParent.removeAttached(TROUGH_FED);
        }
        return fed && instance != null && instance.enabled() && instance.options().bool("suppress_xp");
    }
}
