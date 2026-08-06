package com.chaevsfe.valence.core;

import com.chaevsfe.valence.core.module.Modules;
import com.chaevsfe.valence.core.module.ValenceModule;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ValenceTab
{
    private ValenceTab () { }

    public static void register () {
        CreativeModeTab tab = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.valence"))
            .icon(() -> new ItemStack(icon()))
            .displayItems((parameters, output) -> {
                for (ValenceModule m : Modules.ALL)
                    if (m.enabled())
                        m.addTabItems(output);
            })
            .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModConstants.loc("valence"), tab);
    }

    private static Item icon () {
        Item item = BuiltInRegistries.ITEM.getValue(ModConstants.loc("oak_vertical_slab"));
        return item == Items.AIR ? Items.CHEST : item;
    }
}
