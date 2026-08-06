package com.chaevsfe.valence.client.gui;

import com.chaevsfe.valence.core.config.ConfigSchema;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModuleOptionsScreen extends Screen
{
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final ValenceConfigScreen parent;
    private final ConfigSchema.ModuleDef def;
    private OptionList list;

    ModuleOptionsScreen (ValenceConfigScreen parent, ConfigSchema.ModuleDef def) {
        super(Component.translatable("valence.module." + def.id()));
        this.parent = parent;
        this.def = def;
    }

    @Override
    protected void init () {
        layout.addTitleHeader(title, font);
        list = layout.addToContents(new OptionList(minecraft, this, def, parent.state.values.get(def.id())));
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements () {
        layout.arrangeElements();
        if (list != null)
            list.updateSize(width, layout);
    }

    @Override
    public void onClose () {
        ValenceConfigScreen.open(minecraft, parent);
    }
}
