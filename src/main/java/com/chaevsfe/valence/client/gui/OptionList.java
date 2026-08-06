package com.chaevsfe.valence.client.gui;

import com.chaevsfe.valence.core.config.ConfigSchema;
import com.chaevsfe.valence.core.config.Option;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class OptionList extends ContainerObjectSelectionList<OptionList.OptionEntry>
{
    public OptionList (Minecraft minecraft, ModuleOptionsScreen screen, ConfigSchema.ModuleDef def, Map<String, Object> values) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 25);
        for (Option option : def.options()) {
            AbstractWidget control = control(minecraft.font, option, values);
            if (control != null)
                addEntry(new OptionEntry(minecraft.font, option, control));
        }
    }

    @Override
    public int getRowWidth () {
        return 310;
    }

    private static AbstractWidget control (Font font, Option option, Map<String, Object> values) {
        AbstractWidget control = switch (option.kind) {
            case BOOL -> CycleButton.onOffBuilder((Boolean) values.get(option.key))
                .displayOnlyValue()
                .create(0, 0, 100, 20, Component.empty(), (button, value) -> values.put(option.key, value));
            case INT, DOUBLE -> new OptionSlider(option, values);
            case STRING -> stringBox(font, option, values);
            case STRING_LIST -> null;
        };
        if (control != null)
            control.setTooltip(Tooltip.create(Component.literal(option.comment + option.rangeHint())));
        return control;
    }

    private static EditBox stringBox (Font font, Option option, Map<String, Object> values) {
        EditBox box = new EditBox(font, 0, 0, 100, 20, Component.empty());
        box.setMaxLength(128);
        box.setValue((String) values.get(option.key));
        box.setResponder(text -> values.put(option.key, text));
        return box;
    }

    static class OptionSlider extends AbstractSliderButton
    {
        private final Option option;
        private final Map<String, Object> values;

        OptionSlider (Option option, Map<String, Object> values) {
            super(0, 0, 100, 20, Component.empty(),
                (((Number) values.get(option.key)).doubleValue() - option.min) / (option.max - option.min));
            this.option = option;
            this.values = values;
            updateMessage();
        }

        @Override
        protected void updateMessage () {
            setMessage(Component.literal(display()));
        }

        @Override
        protected void applyValue () {
            if (option.kind == Option.Kind.INT)
                values.put(option.key, (int) Math.round(option.min + value * (option.max - option.min)));
            else
                values.put(option.key, Math.round((option.min + value * (option.max - option.min)) * 10.0) / 10.0);
        }

        private String display () {
            double raw = option.min + value * (option.max - option.min);
            return option.kind == Option.Kind.INT
                ? String.valueOf((int) Math.round(raw))
                : String.valueOf(Math.round(raw * 10.0) / 10.0);
        }
    }

    public static class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry>
    {
        private final Font font;
        private final Component label;
        private final AbstractWidget control;

        OptionEntry (Font font, Option option, AbstractWidget control) {
            this.font = font;
            this.label = Component.literal(prettify(option.key));
            this.control = control;
        }

        @Override
        public void extractContent (GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partial) {
            graphics.text(font, label, getContentX(), getContentYMiddle() - font.lineHeight / 2, 0xFFFFFFFF, true);
            control.setPosition(getContentRight() - control.getWidth(), getContentY());
            control.extractRenderState(graphics, mouseX, mouseY, partial);
        }

        @Override
        public List<? extends GuiEventListener> children () {
            return List.of(control);
        }

        @Override
        public List<? extends NarratableEntry> narratables () {
            return List.of(control);
        }

        private static String prettify (String key) {
            String words = key.replace('_', ' ');
            return Character.toUpperCase(words.charAt(0)) + words.substring(1);
        }
    }
}
