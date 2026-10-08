package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class RunePurchaseUiHandler {
    private static final int BUY_BUTTON_ID = 0;
    private static final int AMOUNT_BUTTON_START_ID = 1;
    private static final int AMOUNT_BUTTON_END_ID = 5;
    private static final int CARD_COST = 100;

    private Field inventoryField;

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        applyState(event.getGui());
    }

    @SubscribeEvent
    public void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof CardsGuiScreen)) return;

        CardsGuiScreen screen = (CardsGuiScreen) event.getGui();
        RuneInventory inventory = getInventory(screen);
        if (inventory == null) return;

        boolean full = inventory.hasAllCatalogRunes();
        GuiButton buyButton = applyButtons(screen, full);
        if (!full || buyButton == null) return;

        String hint = "Пробудите руну";
        int x = buyButton.x + (buyButton.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(hint)) / 2;
        int y = Math.max(2, buyButton.y - 28);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(hint, x, y, 0xFFFFC83D);
    }

    private void applyState(net.minecraft.client.gui.GuiScreen gui) {
        if (!(gui instanceof CardsGuiScreen)) return;
        CardsGuiScreen screen = (CardsGuiScreen) gui;
        RuneInventory inventory = getInventory(screen);
        if (inventory == null) return;
        applyButtons(screen, inventory.hasAllCatalogRunes());
    }

    private GuiButton applyButtons(CardsGuiScreen screen, boolean full) {
        GuiButton buyButton = null;
        for (GuiButton button : screen.buttonList) {
            if (button.id == BUY_BUTTON_ID) {
                buyButton = button;
                button.enabled = !full;
                button.displayString = "Купить руну · " + CARD_COST + " монет";
            } else if (button.id >= AMOUNT_BUTTON_START_ID && button.id <= AMOUNT_BUTTON_END_ID) {
                button.enabled = !full;
            }
        }
        return buyButton;
    }

    private RuneInventory getInventory(CardsGuiScreen screen) {
        try {
            if (inventoryField == null) {
                inventoryField = CardsGuiScreen.class.getDeclaredField("inventory");
                inventoryField.setAccessible(true);
            }
            return (RuneInventory) inventoryField.get(screen);
        } catch (ReflectiveOperationException e) {
            CustomGuiMod.logger.warn("Could not read rune inventory for purchase UI", e);
            return null;
        }
    }
}
