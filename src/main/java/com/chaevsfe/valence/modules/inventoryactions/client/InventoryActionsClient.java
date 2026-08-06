package com.chaevsfe.valence.modules.inventoryactions.client;

import com.chaevsfe.valence.client.ValenceClient;
import com.chaevsfe.valence.core.module.ClientModule;
import com.chaevsfe.valence.modules.inventoryactions.InventoryActions;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class InventoryActionsClient implements ClientModule
{
    private final InventoryActions module;
    private KeyMapping sortKey;
    private ActiveRun active;

    private record ActiveRun (int containerId, ArrayDeque<ClickPlan.Step> steps) { }

    public InventoryActionsClient (InventoryActions module) {
        this.module = module;
    }

    @Override
    public void initClient () {
        sortKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.valence.sort", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, ValenceClient.KEY_CATEGORY));
        ScreenEvents.AFTER_INIT.register(this::onScreenInit);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void onScreenInit (Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || client.player == null)
            return;
        if (screen instanceof CreativeModeInventoryScreen || screen instanceof MerchantScreen || screen instanceof BeaconScreen)
            return;
        AbstractContainerMenu menu = containerScreen.getMenu();
        List<Integer> containerSlots = containerRegion(menu, client.player);
        List<Integer> inventorySlots = playerRegion(menu, client.player, false);
        List<Integer> matchSlots = playerRegion(menu, client.player, true);

        if (module.enabled() && module.options().bool("screen_buttons")) {
            List<AbstractWidget> widgets = Screens.getWidgets(screen);
            int x = containerScreen.leftPos + containerScreen.imageWidth + 2;
            if (!containerSlots.isEmpty()) {
                widgets.add(action("⇅", "valence.button.sort_container", x, containerScreen.topPos,
                    () -> ClickPlan.sort(menu, containerSlots)));
                widgets.add(action("▲", "valence.button.deposit", x, containerScreen.topPos + 14,
                    () -> ClickPlan.quickMoveMatching(menu, inventorySlots, containerSlots)));
                widgets.add(action("▼", "valence.button.extract", x, containerScreen.topPos + 28,
                    () -> ClickPlan.quickMoveMatching(menu, containerSlots, matchSlots)));
            }
            if (!inventorySlots.isEmpty())
                widgets.add(action("⇅", "valence.button.sort_player", x, containerScreen.topPos + containerScreen.imageHeight - 12,
                    () -> ClickPlan.sort(menu, inventorySlots)));
        }

        ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) -> {
            if (!module.enabled() || sortKey.isUnbound() || !sortKey.matches(event))
                return true;
            Slot hovered = containerScreen.hoveredSlot;
            if (hovered != null && containerSlots.contains(hovered.index))
                start(ClickPlan.sort(menu, containerSlots));
            else if (!inventorySlots.isEmpty())
                start(ClickPlan.sort(menu, inventorySlots));
            return false;
        });
    }

    private Button action (String label, String tooltip, int x, int y, Supplier<List<ClickPlan.Step>> plan) {
        return Button.builder(Component.literal(label), pressed -> {
                if (module.enabled())
                    start(plan.get());
            })
            .bounds(x, y, 12, 12)
            .tooltip(Tooltip.create(Component.translatable(tooltip)))
            .build();
    }

    private void start (List<ClickPlan.Step> steps) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || steps.isEmpty() || active != null)
            return;
        if (!client.player.containerMenu.getCarried().isEmpty())
            return;
        active = new ActiveRun(client.player.containerMenu.containerId, new ArrayDeque<>(steps));
    }

    private void tick (Minecraft client) {
        if (active == null)
            return;
        //? if <26.2 {
        /*Screen open = client.screen;
        *///?} else
        Screen open = client.gui.screen();
        if (client.player == null || !(open instanceof AbstractContainerScreen)) {
            active = null;
            return;
        }
        AbstractContainerMenu menu = client.player.containerMenu;
        if (menu.containerId != active.containerId()) {
            active = null;
            return;
        }
        int budget = module.options().intOf("clicks_per_tick");
        while (budget-- > 0 && !active.steps().isEmpty()) {
            ClickPlan.Step step = active.steps().poll();
            ItemStack current = menu.slots.get(step.slot()).getItem();
            if (step.type() == ContainerInput.PICKUP && !ItemStack.matches(current, step.expect())) {
                active = null;
                return;
            }
            if (step.type() == ContainerInput.QUICK_MOVE && !ItemStack.isSameItemSameComponents(current, step.expect()))
                continue;
            client.gameMode.handleContainerInput(menu.containerId, step.slot(), 0, step.type(), client.player);
        }
        if (active.steps().isEmpty())
            active = null;
    }

    private static List<Integer> containerRegion (AbstractContainerMenu menu, LocalPlayer player) {
        List<Integer> region = new ArrayList<>();
        for (Slot slot : menu.slots)
            if (slot.container != player.getInventory() && slot.getClass() == Slot.class && slot.mayPickup(player))
                region.add(slot.index);
        return region.size() >= 5 ? region : List.of();
    }

    private static List<Integer> playerRegion (AbstractContainerMenu menu, LocalPlayer player, boolean hotbar) {
        List<Integer> region = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container != player.getInventory())
                continue;
            int index = slot.getContainerSlot();
            if (index >= 9 && index < 36 || hotbar && index < 9)
                region.add(slot.index);
        }
        return region;
    }
}
