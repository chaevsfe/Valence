package com.chaevsfe.valence.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ValenceConfigScreen extends Screen
{
    final ConfigEditState state = new ConfigEditState();
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 45);
    private final Screen parent;
    private ModuleList list;

    public ValenceConfigScreen (Screen parent) {
        super(Component.translatable("valence.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init () {
        layout.addTitleHeader(title, font);
        list = layout.addToContents(new ModuleList(minecraft, this));
        LinearLayout footer = LinearLayout.vertical().spacing(4);
        footer.addChild(new StringWidget(Component.translatable("valence.config.reload_hint"), font));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());
        layout.addToFooter(footer);
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
        state.save();
        open(minecraft, parent);
    }

    public static void open (Minecraft client, Screen screen) {
        //? if <26.2 {
        /*client.setScreen(screen);
        *///?} else
        client.gui.setScreen(screen);
    }
}
