package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardsGuiScreen extends GuiScreen {

    private static final int STORAGE_SLOT_COUNT = RuneInventory.LEGACY_RECORD_CAPACITY;
    private static final int VISIBLE_RUNE_SLOTS = RuneInventory.BOOST_TYPE_COUNT;

    private static final int GUI_MAX_WIDTH = 1240;
    private static final int GUI_MAX_HEIGHT = 620;
    private static final int GUI_OUTER_MARGIN = 10;

    private static final int OVERLAY_COLOR = 0x50000000;
    private static final int PANEL_BG_COLOR = 0xE10B0E12;
    private static final int PANEL_BG_SOFT = 0xD211151A;
    private static final int PANEL_BORDER_COLOR = 0xFF252B31;
    private static final int PANEL_LINE_COLOR = 0xFF3B434B;
    private static final int SLOT_BG_COLOR = 0xEA0C1014;
    private static final int SLOT_BORDER_COLOR = 0xFF2B3239;
    private static final int SLOT_HOVER_COLOR = 0xFF56616D;
    private static final int SELECTED_COLOR = 0xFF2D7FD6;
    private static final int TEXT_COLOR = 0xFFF1F1F1;
    private static final int MUTED_TEXT_COLOR = 0xFF858B92;

    private static final int NORMAL_COLOR = 0xFF2D7FD6;
    private static final int SILVER_COLOR = 0xFFC9CDD2;
    private static final int GOLD_COLOR = 0xFFFFC83D;

    private static final int BUY_BUTTON_ID = 0;
    private static final int AMOUNT_BUTTON_START_ID = 1;
    private static final int ELEMENT_BUTTON_START_ID = 100;
    private static final int MAX_ELEMENTS = 8;

    private static final int NAV_RUNES_ID = 200;
    private static final int NAV_PLACEHOLDER_START_ID = 201;
    private static final int CLOSE_BUTTON_ID = 299;

    private static final String[] CARD_NAMES = {
            "Булава", "Накидка вора", "Лесной дух", "Щит",
            "Низший голем", "Речной дракончик", "Энт-пугатель",
            "Хижина", "Посох друида", "Книга земли"
    };

    private static final String[] RUNE_TYPES = {
            "Клик", "Клик", "Земля", "Земля", "Земля",
            "Вода", "Земля", "Ресурс", "Земля", "Земля"
    };

    private static final ItemStack[] CARD_ICONS = {
            new ItemStack(Items.DIAMOND_SWORD),
            new ItemStack(Items.LEATHER_CHESTPLATE),
            new ItemStack(Blocks.SAPLING),
            new ItemStack(Items.SHIELD),
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.FISH),
            new ItemStack(Blocks.LEAVES),
            new ItemStack(Items.BED),
            new ItemStack(Items.STICK),
            new ItemStack(Items.BOOK)
    };

    private static final int[] CARD_STATS = {
            4080, 2140, 2920, 1940, 2550,
            3050, 2720, 2410, 3300, 2500
    };

    private final RuneInventory inventory = new RuneInventory();
    private final List<String> deckNames = new ArrayList<>();
    private final List<FlyingCard> flyingCards = new ArrayList<>();

    private int activeDeck = 0;
    private int buyAmount = 1;
    private static RenderItem renderItem;

    private float layoutScale = 1.0F;
    private boolean compactLayout;

    private int guiX;
    private int guiY;
    private int guiWidth;
    private int guiHeight;

    private int mainX;
    private int mainY;
    private int mainWidth;
    private int mainHeight;

    private int leftPanelX;
    private int leftPanelY;
    private int leftPanelWidth;
    private int leftPanelHeight;

    private int centerPanelX;
    private int centerPanelY;
    private int centerPanelWidth;
    private int centerPanelHeight;

    private int runePanelX;
    private int runePanelY;
    private int runePanelWidth;
    private int runePanelHeight;

    private int navPanelX;
    private int navPanelY;
    private int navPanelWidth;
    private int navPanelHeight;

    private int purchaseTop;
    private int runeColumns = 10;
    private int runeRows = 3;
    private int runeSlotWidth;
    private int runeSlotHeight;
    private int runeSlotGap;
    private int runeGridX;
    private int runeGridY;

    private static class FlyingCard {
        double startX;
        double startY;
        double endX;
        double endY;
        double progress;
        int cardIndex;
        int color;

        FlyingCard(double startX, double startY, double endX, double endY,
                   int cardIndex, int color) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.cardIndex = cardIndex;
            this.color = color;
        }

        void update() {
            progress += 0.12D;
            if (progress > 1.0D) progress = 1.0D;
        }

        double getX() {
            return startX + (endX - startX) * progress;
        }

        double getY() {
            return startY + (endY - startY) * progress;
        }

        boolean isDone() {
            return progress >= 1.0D;
        }
    }

    private static class CristalixButton extends GuiButton {
        private boolean selected;

        CristalixButton(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;

            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

            int border = selected ? SELECTED_COLOR : (hovered ? SLOT_HOVER_COLOR : PANEL_BORDER_COLOR);
            int background = selected ? 0xE11A2E43 : (enabled ? 0xE10C1014 : 0xAA080A0C);
            int textColor = enabled ? TEXT_COLOR : MUTED_TEXT_COLOR;

            drawRect(x, y, x + width, y + height, border);
            drawRect(x + 1, y + 1, x + width - 1, y + height - 1, background);
            if (selected) {
                drawRect(x + 1, y + height - 2, x + width - 1, y + height - 1, SELECTED_COLOR);
            }

            String label = displayString;
            int maxWidth = Math.max(1, width - 6);
            if (mc.fontRenderer.getStringWidth(label) > maxWidth) {
                label = mc.fontRenderer.trimStringToWidth(label, maxWidth);
            }

            drawCenteredString(mc.fontRenderer, label,
                    x + width / 2,
                    y + (height - 8) / 2,
                    textColor);
        }
    }

    private static class ElementButton extends CristalixButton {
        private final String elementName;
        private final boolean active;

        ElementButton(int id, int x, int y, int width, int height,
                      String elementName, boolean active) {
            super(id, x, y, width, height, elementName);
            this.elementName = elementName;
            this.active = active;
            setSelected(active);
        }

        String getElementName() {
            return elementName;
        }

        boolean isActiveElement() {
            return active;
        }
    }

    private static class NavButton extends GuiButton {
        private final ItemStack icon;
        private final boolean selected;

        NavButton(int id, int x, int y, int width, int height,
                  ItemStack icon, boolean selected, boolean enabled) {
            super(id, x, y, width, height, "");
            this.icon = icon;
            this.selected = selected;
            this.enabled = enabled;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;

            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            int border = selected ? SELECTED_COLOR : (hovered && enabled ? SLOT_HOVER_COLOR : PANEL_BORDER_COLOR);
            int background = selected ? 0xE11A2E43 : (enabled ? 0xE10C1014 : 0xAA090B0D);

            drawRect(x, y, x + width, y + height, border);
            drawRect(x + 1, y + 1, x + width - 1, y + height - 1, background);

            if (selected) {
                drawRect(x + width - 2, y + 2, x + width - 1, y + height - 2, SELECTED_COLOR);
            }

            if (!icon.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.enableRescaleNormal();
                mc.getRenderItem().renderItemIntoGUI(icon,
                        x + (width - 16) / 2,
                        y + (height - 16) / 2);
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void initGui() {
        buttonList.clear();

        if (renderItem == null) {
            renderItem = Minecraft.getMinecraft().getRenderItem();
        }

        inventory.clear();

        updateLayout();
        rebuildControls();

        NetworkHandler.INSTANCE.sendToServer(new PingPacket("get_player_stats"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_decks"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_cards"));
    }

    private int scaled(int base, int minimum) {
        return Math.max(minimum, Math.round(base * layoutScale));
    }

    private void updateLayout() {
        int availableWidth = Math.max(1, width - GUI_OUTER_MARGIN * 2);
        int availableHeight = Math.max(1, height - GUI_OUTER_MARGIN * 2);

        float widthScale = availableWidth / (float) GUI_MAX_WIDTH;
        float heightScale = availableHeight / (float) GUI_MAX_HEIGHT;
        layoutScale = Math.min(1.0F, Math.min(widthScale, heightScale));
        layoutScale = Math.max(0.1F, layoutScale);

        guiWidth = Math.min(availableWidth, Math.round(GUI_MAX_WIDTH * layoutScale));
        guiHeight = Math.min(availableHeight, Math.round(GUI_MAX_HEIGHT * layoutScale));
        compactLayout = guiWidth < 620 || guiHeight < 330;

        guiX = (width - guiWidth) / 2;
        guiY = (height - guiHeight) / 2;

        int navGap = scaled(4, 2);
        navPanelWidth = scaled(28, 20);
        navPanelHeight = guiHeight;
        navPanelX = guiX + guiWidth - navPanelWidth;
        navPanelY = guiY;

        mainX = guiX;
        mainY = guiY;
        mainWidth = guiWidth - navPanelWidth - navGap;
        mainHeight = guiHeight;

        int innerGap = scaled(3, 1);
        int usableWidth = mainWidth - innerGap * 2;

        leftPanelWidth = usableWidth * 26 / 100;
        centerPanelWidth = usableWidth * 26 / 100;
        runePanelWidth = usableWidth - leftPanelWidth - centerPanelWidth;

        leftPanelX = mainX;
        leftPanelY = mainY;
        leftPanelHeight = Math.min(mainHeight, scaled(150, 110));

        centerPanelX = leftPanelX + leftPanelWidth + innerGap;
        centerPanelY = mainY;
        centerPanelHeight = Math.min(mainHeight, scaled(450, 150));

        runePanelX = centerPanelX + centerPanelWidth + innerGap;
        runePanelY = mainY;
        runePanelHeight = mainHeight;

        updateRuneGridLayout();
    }

    private void updateRuneGridLayout() {
        int pad = scaled(8, 4);
        int topReserved = scaled(88, 58);
        int availableWidth = Math.max(1, runePanelWidth - pad * 2);
        int availableHeight = Math.max(1, runePanelHeight - topReserved - scaled(26, 18));
        runeSlotGap = scaled(5, 2);
        runeColumns = 10;
        runeRows = (VISIBLE_RUNE_SLOTS + runeColumns - 1) / runeColumns;
        int sizeByWidth = (availableWidth - runeSlotGap * (runeColumns - 1)) / runeColumns;
        int sizeByHeight = (availableHeight - runeSlotGap * (runeRows - 1)) / runeRows;
        runeSlotWidth = Math.max(1, Math.min(sizeByWidth, sizeByHeight));
        runeSlotHeight = runeSlotWidth;
        runeGridX = runePanelX + pad;
        runeGridY = runePanelY + topReserved;
    }

    private void rebuildControls() {
        buttonList.clear();
        updateLayout();

        int pad = scaled(7, 3);
        int buttonX = centerPanelX + pad;
        int buttonWidth = Math.max(34, centerPanelWidth - pad * 2);
        int amountHeight = scaled(17, 13);
        int buyHeight = scaled(21, 16);
        int bottom = centerPanelY + centerPanelHeight - pad;

        int amountRows = compactLayout ? 2 : 1;
        int amountBlockHeight = amountRows * amountHeight + (amountRows - 1) * 2;
        int amountY = bottom - amountBlockHeight;
        int buyY = amountY - buyHeight - scaled(4, 2);
        purchaseTop = buyY;

        buttonList.add(new CristalixButton(
                BUY_BUTTON_ID,
                buttonX,
                buyY,
                buttonWidth,
                buyHeight,
                "Купить руну"
        ));

        String[] labels = compactLayout
                ? new String[]{"1", "5", "10", "100", "Все"}
                : new String[]{"x1", "x5", "x10", "x100", "xВсе"};

        if (compactLayout) {
            int gap = 2;
            int topWidth = (buttonWidth - gap * 2) / 3;
            for (int i = 0; i < 3; i++) {
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + i,
                        buttonX + i * (topWidth + gap), amountY,
                        topWidth, amountHeight, labels[i]
                ));
            }

            int bottomWidth = (buttonWidth - gap) / 2;
            for (int i = 0; i < 2; i++) {
                int index = i + 3;
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + index,
                        buttonX + i * (bottomWidth + gap), amountY + amountHeight + 2,
                        bottomWidth, amountHeight, labels[index]
                ));
            }
        } else {
            int gap = scaled(2, 1);
            int amountButtonWidth = Math.max(10,
                    (buttonWidth - gap * (labels.length - 1)) / labels.length);
            for (int i = 0; i < labels.length; i++) {
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + i,
                        buttonX + i * (amountButtonWidth + gap), amountY,
                        amountButtonWidth, amountHeight, labels[i]
                ));
            }
        }

        rebuildElementButtons();
        rebuildNavigationButtons();
        updateAmountButtonSelection();
        updatePurchaseAvailability();
    }

    private void rebuildElementButtons() {
        int count = Math.min(deckNames.size(), MAX_ELEMENTS);
        if (count <= 0) return;

        int x = runePanelX + scaled(8, 4);
        int y = runePanelY + scaled(60, 38);
        int gap = scaled(3, 2);
        int availableRight = runePanelX + runePanelWidth - scaled(8, 4);

        for (int i = 0; i < count; i++) {
            String name = deckNames.get(i);
            int desired = Math.max(scaled(38, 28), fontRenderer.getStringWidth(name) + scaled(14, 7));
            int remaining = count - i;
            int maxForThis = Math.max(1, (availableRight - x - gap * (remaining - 1)) / remaining);
            int buttonWidth = Math.min(desired, maxForThis);
            if (x + buttonWidth > availableRight) break;

            buttonList.add(new ElementButton(
                    ELEMENT_BUTTON_START_ID + i,
                    x, y, buttonWidth, scaled(18, 15),
                    name, i == activeDeck
            ));
            x += buttonWidth + gap;
        }
    }

    private void rebuildNavigationButtons() {
        int pad = scaled(4, 2);
        int buttonSize = navPanelWidth - pad * 2;
        int x = navPanelX + pad;
        int y = navPanelY + pad;
        int gap = scaled(4, 2);

        buttonList.add(new NavButton(
                NAV_RUNES_ID, x, y, buttonSize, buttonSize,
                new ItemStack(Items.ENCHANTED_BOOK), true, true
        ));

        for (int i = 0; i < 5; i++) {
            y += buttonSize + gap;
            buttonList.add(new NavButton(
                    NAV_PLACEHOLDER_START_ID + i, x, y, buttonSize, buttonSize,
                    new ItemStack(new net.minecraft.item.Item[]{Items.GOLDEN_APPLE, Items.POTIONITEM,
                            Items.ENDER_PEARL, Items.BOOK, Items.EMERALD}[i]), false, false
            ));
        }

        int closeSize = buttonSize;
        int closeY = navPanelY + navPanelHeight - pad - closeSize;
        buttonList.add(new NavButton(
                NAV_PLACEHOLDER_START_ID + 5, x, closeY - buttonSize - gap, buttonSize, buttonSize,
                new ItemStack(Items.REDSTONE), false, false
        ));
        buttonList.add(new CristalixButton(
                CLOSE_BUTTON_ID, x, closeY, closeSize, closeSize, "X"
        ));
    }

    private void updatePurchaseAvailability() {
        boolean full = inventory.isPurchaseLimitReached();
        for (GuiButton button : buttonList) {
            if (button.id == BUY_BUTTON_ID) {
                button.enabled = !full;
                button.displayString = full ? "Лимит достигнут" : "Купить руну";
            } else if (button.id >= AMOUNT_BUTTON_START_ID && button.id < AMOUNT_BUTTON_START_ID + 5) {
                button.enabled = !full;
            }
        }
    }

    private void updateAmountButtonSelection() {
        int[] amounts = {1, 5, 10, 100, Integer.MAX_VALUE};
        for (GuiButton guiButton : buttonList) {
            if (!(guiButton instanceof CristalixButton)) continue;
            if (guiButton.id < AMOUNT_BUTTON_START_ID
                    || guiButton.id >= AMOUNT_BUTTON_START_ID + amounts.length) continue;

            ((CristalixButton) guiButton).setSelected(
                    buyAmount == amounts[guiButton.id - AMOUNT_BUTTON_START_ID]
            );
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        if (button.id == CLOSE_BUTTON_ID) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }

        if (button.id == NAV_RUNES_ID) {
            return;
        }

        if (button.id >= ELEMENT_BUTTON_START_ID && button.id < ELEMENT_BUTTON_START_ID + MAX_ELEMENTS) {
            int targetDeck = button.id - ELEMENT_BUTTON_START_ID;
            if (targetDeck >= 0 && targetDeck < deckNames.size() && targetDeck != activeDeck) {
                clearCards();
                NetworkHandler.INSTANCE.sendToServer(new PingPacket("switch_deck:" + targetDeck));
            }
            return;
        }

        if (button.id == BUY_BUTTON_ID) {
            String amount = buyAmount == Integer.MAX_VALUE ? "all" : String.valueOf(buyAmount);
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("buy_cards:" + amount));
            return;
        }

        if (button.id >= AMOUNT_BUTTON_START_ID && button.id < AMOUNT_BUTTON_START_ID + 5) {
            int[] amounts = {1, 5, 10, 100, Integer.MAX_VALUE};
            buyAmount = amounts[button.id - AMOUNT_BUTTON_START_ID];
            updateAmountButtonSelection();
        }
    }

    private void drawPanel(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        drawRect(x, y, x + w, y + h, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_BG_COLOR);
    }

    private void drawSection(int x, int y, int w, int h) {
        if (w <= 2 || h <= 2) return;
        drawRect(x, y, x + w, y + h, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_BG_SOFT);
    }

    private void drawDivider(int x, int y, int w) {
        drawRect(x, y, x + Math.max(1, w), y + 1, PANEL_LINE_COLOR);
    }

    private void drawFittedString(String text, int x, int y, int maxWidth, int color, boolean shadow) {
        int width = fontRenderer.getStringWidth(text);
        if (width <= maxWidth) {
            if (shadow) fontRenderer.drawStringWithShadow(text, x, y, color);
            else fontRenderer.drawString(text, x, y, color);
            return;
        }

        float scale = Math.max(0.55F, maxWidth / (float) Math.max(1, width));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        if (shadow) fontRenderer.drawStringWithShadow(text, 0, 0, color);
        else fontRenderer.drawString(text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    private void drawLeftPanelContent() {
        int pad = scaled(8, 4);
        int x = leftPanelX + pad;
        int textW = Math.max(10, leftPanelWidth - pad * 2);
        int y = leftPanelY + pad;
        drawFittedString("СТАТИСТИКА", x, y, textW, TEXT_COLOR, true);
        y += scaled(18, 13);
        drawDivider(x, y, textW);
        y += scaled(9, 6);
        drawStatLine(x, y, "Монеты", String.valueOf(ClientPlayerStats.getCoins()), 0xFFFFC83D, textW);
        y += scaled(16, 12);
        drawStatLine(x, y, "Кристаллы", String.valueOf(ClientPlayerStats.getCrystals()), 0xFF56D7E8, textW);
        y += scaled(16, 12);
        drawStatLine(x, y, "Урон", "+" + ClientPlayerStats.getTotalDamage(), 0xFFFF7272, textW);
        y += scaled(23, 17);
        drawDivider(x, y, textW);
        y += scaled(10, 7);
        drawFittedString("ВЫБРАННАЯ СБОРКА", x, y, textW, MUTED_TEXT_COLOR, false);
        y += scaled(17, 12);
        String selected = activeDeck >= 0 && activeDeck < deckNames.size()
                ? deckNames.get(activeDeck) : "—";
        drawFittedString(selected, x, y, textW, TEXT_COLOR, true);
    }

    private void drawStatLine(int x, int y, String name, String value, int color, int availableWidth) {
        int valueWidth = fontRenderer.getStringWidth(value);
        int valueX = x + availableWidth - valueWidth;
        int nameMax = Math.max(8, valueX - x - scaled(4, 2));
        drawFittedString(name, x, y, nameMax, color, false);
        fontRenderer.drawString(value, valueX, y, TEXT_COLOR);
    }

    private void drawCenterPanelContent(int mouseX, int mouseY) {
        int pad = scaled(7, 3);
        int innerX = centerPanelX + pad;
        int innerW = centerPanelWidth - pad * 2;
        Minecraft mc = Minecraft.getMinecraft();
        int headerH = scaled(60, 30);
        drawPanel(centerPanelX, centerPanelY, centerPanelWidth, headerH);
        drawFittedString(mc.player == null ? "ИГРОК" : mc.player.getName(),
                innerX, centerPanelY + pad, innerW, TEXT_COLOR, true);
        drawFittedString("СНАРЯЖЕНИЕ", innerX,
                centerPanelY + pad + scaled(19, 12), innerW, MUTED_TEXT_COLOR, false);
        int equipmentY = centerPanelY + headerH + scaled(8, 4);
        drawPanel(centerPanelX, equipmentY, centerPanelWidth,
                centerPanelY + centerPanelHeight - equipmentY);

        int gap = scaled(6, 2);
        int topY = equipmentY + pad;
        int equipmentHeight = purchaseTop - topY - scaled(30, 16);
        int slot = Math.max(1, Math.min(Math.min(scaled(54, 14), (innerW - gap * 4) / 5),
                (equipmentHeight - gap * 4) / 5));
        int rowWidth = slot * 5 + gap * 4;
        int cx = centerPanelX + centerPanelWidth / 2;
        int rowX = cx - rowWidth / 2;
        int bottomY = topY + 4 * (slot + gap);
        for (int i = 0; i < 5; i++) {
            drawEquipmentSlot(rowX + i * (slot + gap), topY, slot);
            drawEquipmentSlot(rowX + i * (slot + gap), bottomY, slot);
        }
        int bodyTop = topY + slot + gap;
        int bodyH = slot * 3 + gap * 2;
        for (int i = 0; i < 3; i++) {
            drawEquipmentSlot(rowX, bodyTop + i * (slot + gap), slot);
            drawEquipmentSlot(rowX + rowWidth - slot, bodyTop + i * (slot + gap), slot);
        }
        int previewX = rowX + slot + gap;
        int previewW = rowWidth - (slot + gap) * 2;
        drawSection(previewX, bodyTop, previewW, bodyH);
        if (mc.player != null && bodyH > 12) {
            int previewScale = Math.max(1, Math.min(previewW / 2, (bodyH - scaled(8, 4)) / 2));
            int entityBottom = bodyTop + (bodyH + previewScale * 2) / 2;
            GuiInventory.drawEntityOnScreen(cx, entityBottom, previewScale,
                    cx - mouseX, entityBottom - previewScale * 2 - mouseY, mc.player);
        }
        String purchaseTitle = inventory.isPurchaseLimitReached()
                ? "ЛИМИТ ПОКУПОК: " + RuneInventory.PURCHASE_LIMIT + "/" + RuneInventory.PURCHASE_LIMIT : "ПОКУПКА РУН";
        drawFittedString(purchaseTitle, innerX, purchaseTop - scaled(17, 12),
                innerW, MUTED_TEXT_COLOR, false);
        drawDivider(innerX, purchaseTop - scaled(5, 2), innerW);
    }

    private void drawEquipmentSlot(int x, int y, int size) {
        drawRect(x, y, x + size, y + size, SLOT_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + size - 1, y + size - 1, SLOT_BG_COLOR);
        if (size >= 14) {
            int corner = Math.max(3, scaled(6, 3));
            drawRect(x + 2, y + 2, x + 2 + corner, y + 3, PANEL_LINE_COLOR);
            drawRect(x + 2, y + 2, x + 3, y + 2 + corner, PANEL_LINE_COLOR);
        }
    }

    private void drawRunePanelContent(int mouseX, int mouseY) {
        int pad = scaled(8, 4);
        int titleX = runePanelX + pad;
        int titleY = runePanelY + pad;
        drawFittedString("РУНЫ", titleX, titleY, runePanelWidth / 2, TEXT_COLOR, true);
        String count = inventory.occupiedTypeCount(CARD_NAMES.length) + " / " + CARD_NAMES.length;
        fontRenderer.drawString(count,
                runePanelX + runePanelWidth - pad - fontRenderer.getStringWidth(count), titleY, MUTED_TEXT_COLOR);
        drawDivider(titleX, runePanelY + scaled(23, 16), runePanelWidth - pad * 2);
        int infoY = runePanelY + scaled(29, 21);
        drawSection(titleX, infoY, runePanelWidth - pad * 2, scaled(21, 13));
        String selected = activeDeck >= 0 && activeDeck < deckNames.size() ? deckNames.get(activeDeck) : "—";
        drawFittedString("Коллекция: " + selected, titleX + scaled(5, 3), infoY + scaled(6, 2),
                runePanelWidth - pad * 2 - scaled(10, 6), MUTED_TEXT_COLOR, false);
        drawDivider(titleX, runeGridY - scaled(5, 2), runePanelWidth - pad * 2);
        for (int i = 0; i < VISIBLE_RUNE_SLOTS; i++) {
            int[] pos = getRuneSlotPosition(i);
            drawRuneSlot(pos[0], pos[1], i, mouseX, mouseY);
        }
        int footerY = runePanelY + runePanelHeight - scaled(18, 12);
        drawDivider(titleX, footerY - scaled(6, 3), runePanelWidth - pad * 2);
        drawFittedString("Доступно: " + CARD_NAMES.length + "  |  Позже: " + (VISIBLE_RUNE_SLOTS - CARD_NAMES.length), titleX, footerY,
                runePanelWidth - pad * 2, MUTED_TEXT_COLOR, false);
    }

    private void drawRuneSlot(int x, int y, int visualIndex, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + runeSlotWidth
                && mouseY >= y && mouseY < y + runeSlotHeight;
        // Reserved catalog positions are not free purchase slots.
        boolean futureType = visualIndex >= CARD_NAMES.length;
        int border = futureType ? PANEL_BORDER_COLOR : (hovered ? SLOT_HOVER_COLOR : SLOT_BORDER_COLOR);
        drawRect(x, y, x + runeSlotWidth, y + runeSlotHeight, border);
        drawRect(x + 1, y + 1, x + runeSlotWidth - 1, y + runeSlotHeight - 1, SLOT_BG_COLOR);
        if (futureType) {
            int markSize = Math.max(3, Math.min(scaled(8, 3), runeSlotWidth - 4));
            int markX = x + (runeSlotWidth - markSize) / 2;
            int markY = y + runeSlotHeight / 2;
            drawRect(markX, markY, markX + markSize, markY + 1, MUTED_TEXT_COLOR);
            return;
        }
        RuneInventory.Entry rune = getRuneForVisualSlot(visualIndex);
        if (rune == null) return;
        drawRankCorners(x, y, runeSlotWidth, runeSlotHeight, getRankColor(rune.layer));
        if (rune.cardIndex >= 0 && rune.cardIndex < CARD_ICONS.length && runeSlotWidth >= 8) {
            int iconSize = Math.max(4, Math.min(16, runeSlotWidth - 6));
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + (runeSlotWidth - iconSize) / 2.0F,
                    y + (runeSlotHeight - iconSize) / 2.0F, 0);
            GlStateManager.scale(iconSize / 16.0F, iconSize / 16.0F, 1);
            GlStateManager.enableRescaleNormal();
            renderItem.renderItemIntoGUI(CARD_ICONS[rune.cardIndex], 0, 0);
            GlStateManager.popMatrix();
        }
    }

    private void drawRankCorners(int x, int y, int w, int h, int color) {
        int length = Math.max(4, Math.min(scaled(12, 6), w / 4));
        int thickness = w >= 30 ? 2 : 1;

        drawRect(x + 2, y + 2, x + 2 + length, y + 2 + thickness, color);
        drawRect(x + 2, y + 2, x + 2 + thickness, y + 2 + length, color);
        drawRect(x + w - 2 - length, y + 2, x + w - 2, y + 2 + thickness, color);
        drawRect(x + w - 2 - thickness, y + 2, x + w - 2, y + 2 + length, color);
    }

    private int[] getRuneSlotPosition(int visualIndex) {
        int row = visualIndex / runeColumns;
        int col = visualIndex % runeColumns;
        return new int[]{
                runeGridX + col * (runeSlotWidth + runeSlotGap),
                runeGridY + row * (runeSlotHeight + runeSlotGap)
        };
    }

    private RuneInventory.Entry getRuneForVisualSlot(int visualIndex) {
        return inventory.getBoost(visualIndex);
    }

    private void drawRuneTooltip(int mouseX, int mouseY) {
        for (int i = 0; i < VISIBLE_RUNE_SLOTS; i++) {
            int[] pos = getRuneSlotPosition(i);
            int x = pos[0];
            int y = pos[1];
            if (mouseX < x || mouseX >= x + runeSlotWidth
                    || mouseY < y || mouseY >= y + runeSlotHeight) continue;

            RuneInventory.Entry rune = getRuneForVisualSlot(i);
            if (rune == null || rune.cardIndex < 0 || rune.cardIndex >= CARD_NAMES.length) return;
            int cardIndex = rune.cardIndex;

            List<String> tooltip = new ArrayList<>();
            tooltip.add("§b" + CARD_NAMES[cardIndex]);
            tooltip.add("");
            tooltip.add("§7Ранг: §f" + getRankName(rune.layer));
            tooltip.add("§7Тип: §f" + (cardIndex < RUNE_TYPES.length ? RUNE_TYPES[cardIndex] : "Неизвестно"));
            tooltip.add("§7Эффект: §a+" + CARD_STATS[cardIndex]);
            tooltip.add("§7Усиление: §f0");
            drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawElementTooltip(int mouseX, int mouseY) {
        for (GuiButton guiButton : buttonList) {
            if (!(guiButton instanceof ElementButton)) continue;
            ElementButton button = (ElementButton) guiButton;
            if (mouseX < button.x || mouseX >= button.x + button.width
                    || mouseY < button.y || mouseY >= button.y + button.height) continue;

            List<String> tooltip = new ArrayList<>();
            tooltip.add("§b" + button.getElementName());
            if (!button.isActiveElement()) {
                tooltip.add("§7Нажмите, чтобы выбрать стихию");
            }
            drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawNavigationTooltip(int mouseX, int mouseY) {
        for (GuiButton button : buttonList) {
            if (button.id != NAV_RUNES_ID) continue;
            if (mouseX < button.x || mouseX >= button.x + button.width
                    || mouseY < button.y || mouseY >= button.y + button.height) continue;
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§bРуны");
            tooltip.add("§7Стихии и коллекция рун");
            drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawFlyingRune(FlyingCard card) {
        int x = (int) card.getX();
        int y = (int) card.getY();
        int w = Math.max(14, Math.min(scaled(38, 22), runeSlotWidth));
        int h = Math.max(18, Math.min(scaled(46, 28), runeSlotHeight));

        drawRect(x - w / 2, y - h / 2, x + w / 2, y + h / 2, SLOT_BORDER_COLOR);
        drawRect(x - w / 2 + 1, y - h / 2 + 1, x + w / 2 - 1, y + h / 2 - 1, SLOT_BG_COLOR);
        drawRankCorners(x - w / 2, y - h / 2, w, h, card.color);

        if (card.cardIndex >= 0 && card.cardIndex < CARD_ICONS.length && w >= 18 && h >= 18) {
            renderItem.renderItemIntoGUI(CARD_ICONS[card.cardIndex], x - 8, y - 8);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (FlyingCard card : new ArrayList<>(flyingCards)) {
            card.update();
            if (card.isDone()) flyingCards.remove(card);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateLayout();

        drawRect(0, 0, width, height, OVERLAY_COLOR);

        drawPanel(leftPanelX, leftPanelY, leftPanelWidth, leftPanelHeight);
        drawPanel(runePanelX, runePanelY, runePanelWidth, runePanelHeight);
        drawPanel(navPanelX, navPanelY, navPanelWidth, navPanelHeight);

        drawLeftPanelContent();
        drawCenterPanelContent(mouseX, mouseY);
        drawRunePanelContent(mouseX, mouseY);

        for (FlyingCard card : flyingCards) {
            drawFlyingRune(card);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawElementTooltip(mouseX, mouseY);
        drawNavigationTooltip(mouseX, mouseY);
        drawRuneTooltip(mouseX, mouseY);
    }

    private int getRankColor(int layer) {
        if (layer == 2) return SILVER_COLOR;
        if (layer == 3) return GOLD_COLOR;
        return NORMAL_COLOR;
    }

    private String getRankName(int layer) {
        if (layer == 2) return "Серебряная";
        if (layer == 3) return "Золотая";
        return "Обычная";
    }

    public void setBalance(int balance) {
        ClientPlayerStats.setCoins(balance);
    }

    public void setDecks(List<String> names, int activeDeck) {
        deckNames.clear();
        deckNames.addAll(names);
        this.activeDeck = activeDeck;
        rebuildControls();

        CustomGuiMod.logger.info("Elements updated on client: " + deckNames.size()
                + ", selected=" + activeDeck);
    }

    public void addCardFromServer(int slot, int cardIndex, int layer, boolean animate) {
        if (slot < 0 || slot >= STORAGE_SLOT_COUNT) return;

        inventory.set(slot, cardIndex, layer);
        updatePurchaseAvailability();

        if (animate && cardIndex >= 0 && cardIndex < CARD_ICONS.length) {
            int[] pos = getRuneSlotPosition(cardIndex);
            flyingCards.add(new FlyingCard(
                    centerPanelX + centerPanelWidth / 2.0D,
                    centerPanelY + centerPanelHeight - scaled(45, 24),
                    pos[0] + runeSlotWidth / 2.0D,
                    pos[1] + runeSlotHeight / 2.0D,
                    cardIndex,
                    getRankColor(layer)
            ));
        }

        CustomGuiMod.logger.info("Добавлена руна от сервера: слот " + slot + ", индекс " + cardIndex
                + " (анимация: " + animate + ")");
    }

    public void clearCards() {
        inventory.clear();
        updatePurchaseAvailability();
        flyingCards.clear();
        CustomGuiMod.logger.info("Руны очищены на клиенте");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
