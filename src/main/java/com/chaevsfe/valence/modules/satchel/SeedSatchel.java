package com.chaevsfe.valence.modules.satchel;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.config.OptionBuilder;
import com.chaevsfe.valence.core.module.ModuleCategory;
import com.chaevsfe.valence.core.module.ModuleSide;
import com.chaevsfe.valence.core.module.ValenceModule;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

public class SeedSatchel extends ValenceModule
{
    public static final TagKey<Item> PLANTABLES = TagKey.create(Registries.ITEM, ModConstants.loc("seed_satchel_plantables"));

    public static Item SATCHEL;
    public static DataComponentType<Boolean> COLLECTING;
    private static SeedSatchel instance;

    public SeedSatchel () {
        super("seed_satchel", ModuleCategory.UTILITY, ModuleSide.COMMON,
            "A bag that gathers plantable items and sows them from its contents");
        instance = this;
    }

    public static SeedSatchel instance () {
        return instance;
    }

    public static boolean isPlantable (ItemStack stack) {
        return !stack.isEmpty() && stack.is(PLANTABLES);
    }

    @Override
    public void defineOptions (OptionBuilder builder) {
        builder.bool("absorb", true, "Matching items in the inventory get pulled into the satchel");
    }

    @Override
    public void register () {
        COLLECTING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ModConstants.loc("collecting"),
            DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL.cast())
                .build());

        SATCHEL = new ItemSeedSatchel(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, ModConstants.loc("seed_satchel")))
            .stacksTo(1)
            .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
            .component(COLLECTING, true));
        Registry.register(BuiltInRegistries.ITEM, ModConstants.loc("seed_satchel"), SATCHEL);
    }

    public static boolean collecting (ItemStack satchel) {
        return satchel.getOrDefault(COLLECTING, true);
    }

    @Override
    public void init () {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            if (enabled())
                output.insertAfter(Items.BUNDLE, SATCHEL);
        });
    }

    @Override
    public void addTabItems (CreativeModeTab.Output output) {
        output.accept(SATCHEL);
    }
}
