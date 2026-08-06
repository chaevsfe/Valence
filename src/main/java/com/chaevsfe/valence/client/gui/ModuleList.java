package com.chaevsfe.valence.client.gui;

import com.chaevsfe.valence.core.config.ConfigSchema;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class ModuleList extends ContainerObjectSelectionList<ModuleList.Entry>
{
    public ModuleList (Minecraft minecraft, ValenceConfigScreen screen) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);
        for (String category : screen.state.schema.categories) {
            List<ConfigSchema.ModuleDef> defs = screen.state.schema.inCategory(category);
            if (defs.isEmpty())
                continue;
            addEntry(new CategoryEntry(minecraft.font, Component.translatable("valence.category." + category)));
            for (ConfigSchema.ModuleDef def : defs)
                addEntry(new ModuleEntry(minecraft, screen, def));
        }
    }

    @Override
    public int getRowWidth () {
        return 310;
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> { }

    static class CategoryEntry extends Entry
    {
        private final Font font;
        private final Component label;

        CategoryEntry (Font font, Component label) {
            this.font = font;
            this.label = label;
        }

        @Override
        public void extractContent (GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partial) {
            graphics.text(font, label, getContentX() + (getContentWidth() - font.width(label)) / 2,
                getContentYMiddle() - font.lineHeight / 2, 0xFFFFFF55, true);
        }

        @Override
        public List<? extends GuiEventListener> children () {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables () {
            return List.of();
        }
    }

    static class ModuleEntry extends Entry
    {
        private final Font font;
        private final Component name;
        private final CycleButton<Boolean> toggle;
        private final Button gear;

        ModuleEntry (Minecraft minecraft, ValenceConfigScreen screen, ConfigSchema.ModuleDef def) {
            this.font = minecraft.font;
            this.name = Component.translatable("valence.module." + def.id());
            this.toggle = CycleButton.onOffBuilder(screen.state.enabled.get(def.id()))
                .displayOnlyValue()
                .create(0, 0, 44, 20, name, (button, value) -> screen.state.enabled.put(def.id(), value));
            this.toggle.setTooltip(Tooltip.create(Component.literal(def.description())));
            this.gear = Button.builder(Component.literal("⚙"),
                    button -> ValenceConfigScreen.open(minecraft, new ModuleOptionsScreen(screen, def)))
                .bounds(0, 0, 20, 20)
                .build();
            this.gear.active = !def.options().isEmpty();
        }

        @Override
        public void extractContent (GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partial) {
            graphics.text(font, name, getContentX(), getContentYMiddle() - font.lineHeight / 2, 0xFFFFFFFF, true);
            toggle.setPosition(getContentRight() - 68, getContentY());
            gear.setPosition(getContentRight() - 20, getContentY());
            toggle.extractRenderState(graphics, mouseX, mouseY, partial);
            gear.extractRenderState(graphics, mouseX, mouseY, partial);
        }

        @Override
        public List<? extends GuiEventListener> children () {
            return List.of(toggle, gear);
        }

        @Override
        public List<? extends NarratableEntry> narratables () {
            return List.of(toggle, gear);
        }
    }
}
