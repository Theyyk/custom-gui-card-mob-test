package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RunePurchaseUiHandler {
    private static final int BUY_BUTTON_ID = 0;
    private static final int AMOUNT_BUTTON_START_ID = 1;
    private static final int AMOUNT_BUTTON_END_ID = 5;
    private static final int CARD_COST = 100;

    private Field inventoryField;
    private Field buttonListField;
    private Field activeDeckField;
    private Field runeGridXField;
    private Field runeGridYField;
    private Field runeSlotWidthField;
    private Field runeSlotHeightField;
    private Field runeSlotGapField;
    private Field runeColumnsField;

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        applyState(event.getGui());
    }

    @SubscribeEvent
    public void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        applyState(event.getGui());
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (!(event.getGui() instanceof CardsGuiScreen)) return;
        if (Mouse.getEventButton() != 2 || !Mouse.getEventButtonState()) return;

        CardsGuiScreen screen = (CardsGuiScreen) event.getGui();
        RuneInventory inventory = getInventory(screen);
        if (inventory == null || !inventory.hasAllCatalogRunes()) return;
        if (getCollectionRank(inventory) >= RuneInventory.GOLD_RANK) return;
        if (getIntField(screen, getActiveDeckField()) != 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * screen.width / Math.max(1, mc.displayWidth);
        int mouseY = screen.height - Mouse.getEventY() * screen.height / Math.max(1, mc.displayHeight) - 1;

        int gridX = getIntField(screen, getRuneGridXField());
        int gridY = getIntField(screen, getRuneGridYField());
        int slotWidth = getIntField(screen, getRuneSlotWidthField());
        int slotHeight = getIntField(screen, getRuneSlotHeightField());
        int gap = getIntField(screen, getRuneSlotGapField());
        int columns = getIntField(screen, getRuneColumnsField());
        if (slotWidth <= 0 || slotHeight <= 0 || columns <= 0) return;

        for (int rune = 0; rune < RuneInventory.BOOST_TYPE_COUNT; rune++) {
            int row = rune / columns;
            int col = rune % columns;
            int x = gridX + col * (slotWidth + gap);
            int y = gridY + row * (slotHeight + gap);
            if (mouseX < x || mouseX >= x + slotWidth || mouseY < y || mouseY >= y + slotHeight) continue;

            RuneInventory.Entry owned = inventory.get(rune);
            if (owned == null) return;
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("upgrade_rune:" + rune));
            return;
        }
    }

    private void applyState(GuiScreen gui) {
        if (!(gui instanceof CardsGuiScreen)) return;
        CardsGuiScreen screen = (CardsGuiScreen) gui;
        RuneInventory inventory = getInventory(screen);
        if (inventory == null) return;

        boolean full = inventory.hasAllCatalogRunes();
        int collectionRank = getCollectionRank(inventory);
        applyButtons(screen, full, collectionRank);
    }

    private void applyButtons(CardsGuiScreen screen, boolean full, int collectionRank) {
        for (GuiButton button : getButtons(screen)) {
            if (button.id == BUY_BUTTON_ID) {
                if (!full) {
                    button.enabled = true;
                    button.displayString = "Купить руну · " + CARD_COST + " монет";
                } else {
                    button.enabled = false;
                    button.displayString = collectionRank >= RuneInventory.GOLD_RANK
                            ? "Максимальный ранг"
                            : "Пробудить руну";
                }
            } else if (button.id >= AMOUNT_BUTTON_START_ID && button.id <= AMOUNT_BUTTON_END_ID) {
                button.enabled = !full;
            }
        }
    }

    private int getCollectionRank(RuneInventory inventory) {
        int minRank = RuneInventory.GOLD_RANK;
        boolean found = false;
        for (int rune = 0; rune < RuneInventory.BOOST_TYPE_COUNT; rune++) {
            RuneInventory.Entry entry = inventory.get(rune);
            if (entry == null) continue;
            found = true;
            minRank = Math.min(minRank, entry.layer);
        }
        return found ? minRank : RuneInventory.NORMAL_RANK;
    }

    @SuppressWarnings("unchecked")
    private List<GuiButton> getButtons(CardsGuiScreen screen) {
        try {
            if (buttonListField == null) {
                buttonListField = ObfuscationReflectionHelper.findField(
                        GuiScreen.class, "field_146292_n");
                buttonListField.setAccessible(true);
            }
            return (List<GuiButton>) buttonListField.get(screen);
        } catch (RuntimeException | IllegalAccessException e) {
            CustomGuiMod.logger.warn("Could not read GUI button list for rune purchase UI", e);
            return Collections.emptyList();
        }
    }

    private RuneInventory getInventory(CardsGuiScreen screen) {
        try {
            if (inventoryField == null) inventoryField = ownField("inventory");
            return (RuneInventory) inventoryField.get(screen);
        } catch (ReflectiveOperationException e) {
            CustomGuiMod.logger.warn("Could not read rune inventory for purchase UI", e);
            return null;
        }
    }

    private Field getActiveDeckField() { return cachedField(activeDeckField, "activeDeck", f -> activeDeckField = f); }
    private Field getRuneGridXField() { return cachedField(runeGridXField, "runeGridX", f -> runeGridXField = f); }
    private Field getRuneGridYField() { return cachedField(runeGridYField, "runeGridY", f -> runeGridYField = f); }
    private Field getRuneSlotWidthField() { return cachedField(runeSlotWidthField, "runeSlotWidth", f -> runeSlotWidthField = f); }
    private Field getRuneSlotHeightField() { return cachedField(runeSlotHeightField, "runeSlotHeight", f -> runeSlotHeightField = f); }
    private Field getRuneSlotGapField() { return cachedField(runeSlotGapField, "runeSlotGap", f -> runeSlotGapField = f); }
    private Field getRuneColumnsField() { return cachedField(runeColumnsField, "runeColumns", f -> runeColumnsField = f); }

    private interface FieldSetter { void set(Field field); }

    private Field cachedField(Field current, String name, FieldSetter setter) {
        if (current != null) return current;
        try {
            Field field = ownField(name);
            setter.set(field);
            return field;
        } catch (ReflectiveOperationException e) {
            CustomGuiMod.logger.warn("Could not access GUI field " + name, e);
            return null;
        }
    }

    private Field ownField(String name) throws NoSuchFieldException {
        Field field = CardsGuiScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private int getIntField(CardsGuiScreen screen, Field field) {
        if (field == null) return -1;
        try {
            return field.getInt(screen);
        } catch (IllegalAccessException e) {
            CustomGuiMod.logger.warn("Could not read GUI integer field", e);
            return -1;
        }
    }
}
