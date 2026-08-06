package com.chaevsfe.valence.modules.helditem.client;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.helditem.HeldItemReadout;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HeldItemReadoutClient implements ClientModule
{
    private final HeldItemReadout module;
    private final Item[] lastItem = new Item[2];
    private final int[] lastDamage = new int[2];
    private final int[] lastCount = new int[2];
    private final int[] showTicks = new int[2];

    public HeldItemReadoutClient (HeldItemReadout module) {
        this.module = module;
    }

    @Override
    public void initClient () {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ModConstants.loc("held_item_readout"), this::extract);
    }

    private void tick (Minecraft client) {
        if (!module.enabled() || client.player == null) {
            showTicks[0] = showTicks[1] = 0;
            lastItem[0] = lastItem[1] = null;
            return;
        }
        update(0, client.player.getMainHandItem());
        update(1, client.player.getOffhandItem());
    }

    private void update (int hand, ItemStack stack) {
        if (stack.isEmpty() || (!stack.isDamageableItem() && stack.getMaxStackSize() <= 1)) {
            showTicks[hand] = 0;
            lastItem[hand] = null;
            return;
        }
        boolean changed = stack.getItem() != lastItem[hand]
            || stack.getDamageValue() != lastDamage[hand]
            || stack.getCount() != lastCount[hand];
        if (changed)
            showTicks[hand] = module.options().intOf("fade_ticks");
        else if (showTicks[hand] > 0)
            showTicks[hand]--;
        lastItem[hand] = stack.getItem();
        lastDamage[hand] = stack.getDamageValue();
        lastCount[hand] = stack.getCount();
    }

    private void extract (GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!module.enabled())
            return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return;

        int xOffset = module.options().intOf("x_offset");
        int yOffset = module.options().intOf("y_offset");
        int y = graphics.guiHeight() - 19 - yOffset;

        if (showTicks[0] > 0)
            draw(graphics, client, client.player.getMainHandItem(), graphics.guiWidth() / 2 + 98 + xOffset, y, false);
        if (showTicks[1] > 0 && module.options().bool("show_offhand"))
            draw(graphics, client, client.player.getOffhandItem(), graphics.guiWidth() / 2 - 114 - xOffset, y, true);
    }

    private void draw (GuiGraphicsExtractor graphics, Minecraft client, ItemStack stack, int x, int y, boolean leftward) {
        if (stack.isEmpty())
            return;
        String label;
        int color;
        if (stack.isDamageableItem()) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            label = remaining + "/" + stack.getMaxDamage();
            float fraction = (float) remaining / stack.getMaxDamage();
            color = 0xFF000000 | (int) ((1 - fraction) * 255) << 16 | (int) (fraction * 255) << 8;
        }
        else {
            label = String.valueOf(stack.getCount());
            color = 0xFFFFFFFF;
        }
        int textX = leftward ? x - 2 - client.font.width(label) : x + 18;
        graphics.item(stack, x, y);
        graphics.text(client.font, label, textX, y + 4, color, true);
    }
}
