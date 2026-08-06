package com.chaevsfe.valence.modules.woodworks.client;

import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.woodworks.Woodworks;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public class WoodworksClient implements ClientModule
{
    @Override
    public void initClient () {
        BlockEntityRenderers.register(Woodworks.CHEST_TYPE, ChestRenderer::new);
    }
}
