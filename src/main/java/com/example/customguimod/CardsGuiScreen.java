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

    // v1.2 keeps the old server storage contract for now.
    private static final int STORAGE_SLOT_COUNT = 50;
    private static final int VISIBLE_RUNE_SLOTS = 26;
    private static final int WIDE_RUNE_COLUMNS = 13;

    private static final int GUI_MAX_WIDTH = 960;
    private static final int GUI_MAX_HEIGHT = 530;
    private static final int GUI_OUTER_MARGIN = 10;
    private static final int PANEL_GAP = 5;
    private static final int PANEL_PADDING = 8;

    private static final int OVERLAY_COLOR = 0x98000000;
    private static final int PANEL_BG_COLOR = 0xD70C0F12;
    private static final int PANEL_BG_SOFT = 0xC90F1317;
    private static final int PANEL_BORDER_COLOR = 0xDD242A31;
    private static final int PANEL_LINE_COLOR = 0x99404A54;
    private static final int SLOT_BG_COLOR = 0xE00D1115;
    private static final int SLOT_BORDER_COLOR = 0xFF242B32;
    private static final int SLOT_HOVER_COLOR = 0xFF4A5662;
    private static final int SELECTED_COLOR = 0xFF2476D4;
    private static final int TEXT_COLOR = 0xFFF0F0F0;
    private static final int MUTED_TEXT_COLOR = 0xFF777E86;

    private static final int NORMAL_COLOR = 0xFF2476D4;
    private static final int SILVER_COLOR = 0xFFC4C8CE;
    private static final int GOLD_COLOR = 0xFFFFC83D;

    private static final int BUY_BUTTON_ID = 0;
    private static final int AMOUNT_BUTTON_START_ID = 1;
    private static final int ELEMENT_BUTTON_START_ID = 100;
    private static final int MAX_ELEMENTS = 8;

    private static final ItemStack COIN_ICON = new ItemStack(Items.GOLD_NUGGET);
    private static final ItemStack CRYSTAL_ICON = new ItemStack(Items.DIAMOND);
    private static final ItemStack DAMAGE_ICON = new ItemStack(Items.IRON_SWORD);

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

    private static List<CardData>[] slots = new List[STORAGE_SLOT_COUNT];
    private final List<String> deckNames = new ArrayList<>();
    private final List<FlyingCard> flyingCards = new ArrayList<>();

    private int activeDeck = 0;
    private int buyAmount = 1;
    private static RenderItem renderItem;

    private int guiX;
    private int guiY;
    private int guiWidth;
    private int guiHeight;
    private int headerHeight;
    private boolean compactLayout;

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

    private int runeColumns = WIDE_RUNE_COLUMNS;
    private int runeRows = 2;
    private int runeSlotWidth;
    private int runeSlotHeight;
    private int runeSlotGap;
    private int runeGridX;
    private int runeGridY;

    private static class CardData {
        int cardIndex;
        int layer;

        CardData(int cardIndex, int layer) {
            this.cardIndex = cardIndex;
            this.layer = layer;
        }
    }

    private static class FlyingCard {
        double startX;
        double startY;
        double endX;
        double endY;
        double progress;
        int cardIndex;
        int color;

        FlyingCard(double startX, double startY, double endX, double endY, int cardIndex, int color) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.cardIndex = cardIndex;
            this.color = color;
        }

        void update() {
            progress += 0.12;
            if (progress > 1.0) progress = 1.0;
        }

        double getX() {
            return startX + (endX - startX) * progress;
        }

        double getY() {
            return startY + (endY - startY) * progress;
        }

        boolean isDone() {
            return progress >= 1.0;
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
            int background = selected ? 0xE0192D43 : (enabled ? 0xE00C1014 : 0xAA080A0C);
            int textColor = enabled ? TEXT_COLOR : MUTED_TEXT_COLOR;

            drawRect(x, y, x + width, y + height, border);
            drawRect(x + 1, y + 1, x + width - 1, y + height - 1, background);

            if (selected) {
                drawRect(x + 1, y + height - 2, x + width - 1, y + height - 1, SELECTED_COLOR);
            }

            String label = displayString;
            int max = Math.max(1, width - 6);
            if (mc.fontRenderer.getStringWidth(label) > max) {
                label = mc.fontRenderer.trimStringToWidth(label, max);
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

    @Override
    public void initGui() {
        buttonList.clear();

        if (renderItem == null) {
            renderItem = Minecraft.getMinecraft().getRenderItem();
        }

        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            slots[i] = new ArrayList<>();
        }

        updateLayout();
        rebuildControls();

        NetworkHandler.INSTANCE.sendToServer(new PingPacket("get_player_stats"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_decks"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_cards"));
    }

    private void rebuildControls() {
        buttonList.clear();
        updateLayout();

        int pad = Math.max(4, Math.min(8, centerPanelWidth / 22));
        int buttonX = centerPanelX + pad;
        int buttonWidth = Math.max(44, centerPanelWidth - pad * 2);
        int amountHeight = 17;
        int buyHeight = 21;
        int bottom = centerPanelY + centerPanelHeight - pad;

        int amountRows = compactLayout ? 2 : 1;
        int amountBlockHeight = amountRows * amountHeight + (amountRows - 1) * 2;
        int amountY = bottom - amountBlockHeight;
        int buyY = amountY - buyHeight - 4;

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
            int topCount = 3;
            int topWidth = (buttonWidth - gap * (topCount - 1)) / topCount;
            for (int i = 0; i < topCount; i++) {
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + i,
                        buttonX + i * (topWidth + gap),
                        amountY,
                        topWidth,
                        amountHeight,
                        labels[i]
                ));
            }

            int bottomCount = 2;
            int bottomWidth = (buttonWidth - gap) / bottomCount;
            for (int i = 0; i < bottomCount; i++) {
                int index = i + topCount;
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + index,
                        buttonX + i * (bottomWidth + gap),
                        amountY + amountHeight + 2,
                        bottomWidth,
                        amountHeight,
                        labels[index]
                ));
            }
        } else {
            int gap = 2;
            int amountButtonWidth = Math.max(12,
                    (buttonWidth - gap * (labels.length - 1)) / labels.length);
            for (int i = 0; i < labels.length; i++) {
                buttonList.add(new CristalixButton(
                        AMOUNT_BUTTON_START_ID + i,
                        buttonX + i * (amountButtonWidth + gap),
                        amountY,
                        amountButtonWidth,
                        amountHeight,
                        labels[i]
                ));
            }
        }

        rebuildElementButtons();
        updateAmountButtonSelection();
    }

    private void rebuildElementButtons() {
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            int id = buttonList.get(i).id;
            if (id >= ELEMENT_BUTTON_START_ID && id < ELEMENT_BUTTON_START_ID + MAX_ELEMENTS) {
                buttonList.remove(i);
            }
        }

        int count = Math.min(deckNames.size(), MAX_ELEMENTS);
        if (count <= 0) return;

        int x = runePanelX + PANEL_PADDING;
        int y = runePanelY + 31;
        int gap = 3;
        int availableRight = runePanelX + runePanelWidth - PANEL_PADDING;

        for (int i = 0; i < count; i++) {
            String name = deckNames.get(i);
            int desired = Math.max(38, fontRenderer.getStringWidth(name) + 14);
            int remaining = count - i;
            int maxForThis = Math.max(28, (availableRight - x - gap * (remaining - 1)) / remaining);
            int buttonWidth = Math.min(desired, maxForThis);

            if (x + buttonWidth > availableRight) break;

            buttonList.add(new ElementButton(
                    ELEMENT_BUTTON_START_ID + i,
                    x,
                    y,
                    buttonWidth,
                    19,
                    name,
                    i == activeDeck
            ));
            x += buttonWidth + gap;
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

    private void updateLayout() {
        int availableWidth = Math.max(1, width - GUI_OUTER_MARGIN * 2);
        int availableHeight = Math.max(1, height - GUI_OUTER_MARGIN * 2);

        guiWidth = Math.min(availableWidth, GUI_MAX_WIDTH);
        guiHeight = Math.min(availableHeight, GUI_MAX_HEIGHT);
        compactLayout = guiWidth < 700 || guiHeight < 390;

        guiX = (width - guiWidth) / 2;
        guiY = (height - guiHeight) / 2;

        headerHeight = compactLayout ? 24 : Math.max(25, Math.min(31, guiHeight / 12));
        int contentY = guiY + headerHeight;
        int contentHeight = Math.max(80, guiHeight - headerHeight);
        int usableWidth = Math.max(1, guiWidth - PANEL_GAP * 2);

        if (compactLayout) {
            leftPanelWidth = usableWidth * 18 / 100;
            centerPanelWidth = usableWidth * 27 / 100;
        } else {
            leftPanelWidth = usableWidth * 19 / 100;
            centerPanelWidth = usableWidth * 26 / 100;
        }
        runePanelWidth = usableWidth - leftPanelWidth - centerPanelWidth;

        leftPanelX = guiX;
        leftPanelY = contentY;
        leftPanelHeight = contentHeight;

        centerPanelX = leftPanelX + leftPanelWidth + PANEL_GAP;
        centerPanelY = contentY;
        centerPanelHeight = contentHeight;

        runePanelX = centerPanelX + centerPanelWidth + PANEL_GAP;
        runePanelY = contentY;
        runePanelHeight = contentHeight;

        updateRuneGridLayout();
    }

    private void updateRuneGridLayout() {
        int horizontalPadding = Math.max(4, Math.min(PANEL_PADDING, runePanelWidth / 30));
        int topReserved = compactLayout ? 58 : 60;
        int bottomPadding = 10;
        int availableWidth = Math.max(1, runePanelWidth - horizontalPadding * 2);
        int availableHeight = Math.max(1, runePanelHeight - topReserved - bottomPadding);

        runeSlotGap = Math.max(2, Math.min(4, availableWidth / 150));

        int minReadableSlotWidth = 18;
        int maxColumnsByWidth = Math.max(1,
                (availableWidth + runeSlotGap) / (minReadableSlotWidth + runeSlotGap));

        if (maxColumnsByWidth >= 13) runeColumns = 13;
        else if (maxColumnsByWidth >= 9) runeColumns = 9;
        else if (maxColumnsByWidth >= 7) runeColumns = 7;
        else if (maxColumnsByWidth >= 5) runeColumns = 5;
        else runeColumns = Math.max(1, maxColumnsByWidth);

        runeRows = (VISIBLE_RUNE_SLOTS + runeColumns - 1) / runeColumns;

        int widthBased = Math.max(1,
                (availableWidth - runeSlotGap * (runeColumns - 1)) / runeColumns);
        int heightBased = Math.max(1,
                (availableHeight - runeSlotGap * (runeRows - 1)) / runeRows);

        runeSlotWidth = Math.max(8, Math.min(48, widthBased));
        runeSlotHeight = Math.max(12,
                Math.min(58, Math.min(heightBased, runeSlotWidth * 5 / 4)));

        int gridWidth = runeColumns * runeSlotWidth + (runeColumns - 1) * runeSlotGap;
        int gridHeight = runeRows * runeSlotHeight + (runeRows - 1) * runeSlotGap;

        runeGridX = runePanelX + (runePanelWidth - gridWidth) / 2;
        runeGridY = runePanelY + topReserved + Math.max(4, (availableHeight - gridHeight) / 8);
    }

    private int[] getRuneSlotPosition(int visualIndex) {
        int row = visualIndex / runeColumns;
        int col = visualIndex % runeColumns;
        return new int[]{
                runeGridX + col * (runeSlotWidth + runeSlotGap),
                runeGridY + row * (runeSlotHeight + runeSlotGap)
        };
    }

    private CardData getRuneForVisualSlot(int visualIndex) {
        if (visualIndex < 0 || visualIndex >= CARD_NAMES.length) return null;

        CardData best = null;
        for (int slotIndex = 0; slotIndex < STORAGE_SLOT_COUNT; slotIndex++) {
            if (slots[slotIndex] == null) continue;
            for (CardData card : slots[slotIndex]) {
                if (card.cardIndex != visualIndex) continue;
                if (best == null || card.layer > best.layer) best = card;
            }
        }
        return best;
    }

    private void drawPanel(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        drawRect(x, y, x + w, y + h, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_BG_COLOR);
        drawRect(x + 2, y + 2, x + w - 2, y + 3, PANEL_LINE_COLOR);
    }

    private void drawSection(int x, int y, int w, int h) {
        if (w <= 2 || h <= 2) return;
        drawRect(x, y, x + w, y + h, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_BG_SOFT);
    }

    private void drawHeader() {
        int chipGap = compactLayout ? 3 : 4;
        int chipWidth = compactLayout ? Math.max(52, Math.min(76, (guiWidth - chipGap * 2) / 3)) : 80;
        int damageWidth = compactLayout ? chipWidth : 83;
        int totalWidth = chipWidth * 2 + damageWidth + chipGap * 2;
        int x = guiX + guiWidth - totalWidth;
        int y = guiY + (compactLayout ? 2 : 5);

        drawResourceChip(x, y, chipWidth, COIN_ICON, String.valueOf(ClientPlayerStats.getCoins()));
        x += chipWidth + chipGap;
        drawResourceChip(x, y, chipWidth, CRYSTAL_ICON, String.valueOf(ClientPlayerStats.getCrystals()));
        x += chipWidth + chipGap;
        drawResourceChip(x, y, damageWidth, DAMAGE_ICON, "+" + ClientPlayerStats.getTotalDamage());
    }

    private void drawResourceChip(int x, int y, int w, ItemStack icon, String value) {
        int h = 20;
        drawRect(x, y, x + w, y + h, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xD70A0D10);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        renderItem.renderItemIntoGUI(icon, x + 3, y + 2);
        GlStateManager.popMatrix();

        int textX = x + 22;
        int maxText = Math.max(1, w - 25);
        String fitted = value;
        if (fontRenderer.getStringWidth(fitted) > maxText) {
            fitted = fontRenderer.trimStringToWidth(fitted, maxText);
        }
        fontRenderer.drawStringWithShadow(fitted, textX, y + 6, TEXT_COLOR);
    }

    private void drawLeftPanelContent() {
        int x = leftPanelX + PANEL_PADDING;
        int sectionWidth = Math.max(20, leftPanelWidth - PANEL_PADDING * 2);
        int topHeight = compactLayout
                ? Math.max(98, leftPanelHeight * 57 / 100)
                : Math.max(110, leftPanelHeight * 58 / 100);

        drawSection(x, leftPanelY + PANEL_PADDING, sectionWidth, topHeight - PANEL_PADDING);
        int y = leftPanelY + PANEL_PADDING + 9;
        int textX = x + 8;
        int textWidth = Math.max(10, sectionWidth - 16);

        drawFittedString("СТАТИСТИКА", textX, y, textWidth, TEXT_COLOR, true);
        y += 17;
        drawDivider(x + 6, y, sectionWidth - 12);
        y += 8;

        drawStatLine(textX, y, "Монеты", String.valueOf(ClientPlayerStats.getCoins()), 0xFFFFC83D, textWidth);
        y += 14;
        drawStatLine(textX, y, "Кристаллы", String.valueOf(ClientPlayerStats.getCrystals()), 0xFF56D7E8, textWidth);
        y += 14;
        drawStatLine(textX, y, "Урон", "+" + ClientPlayerStats.getTotalDamage(), 0xFFFF7272, textWidth);
        y += 20;

        drawFittedString("ЭФФЕКТЫ", textX, y, textWidth, MUTED_TEXT_COLOR, false);
        y += 14;
        if (!compactLayout || textWidth >= 75) {
            drawFittedString("Будут добавлены", textX, y, textWidth, MUTED_TEXT_COLOR, false);
            y += 11;
            drawFittedString("в следующих версиях", textX, y, textWidth, MUTED_TEXT_COLOR, false);
        } else {
            drawFittedString("Позже", textX, y, textWidth, MUTED_TEXT_COLOR, false);
        }

        int bottomY = leftPanelY + PANEL_PADDING + topHeight + PANEL_GAP;
        int bottomHeight = leftPanelY + leftPanelHeight - PANEL_PADDING - bottomY;
        if (bottomHeight > 35) {
            drawSection(x, bottomY, sectionWidth, bottomHeight);
            drawFittedString("ВЫБРАННАЯ СБОРКА", textX, bottomY + 9, textWidth, TEXT_COLOR, true);
            drawDivider(x + 6, bottomY + 25, sectionWidth - 12);
            if (compactLayout) {
                drawFittedString("Эффекты позже", textX, bottomY + 35, textWidth, MUTED_TEXT_COLOR, false);
            } else {
                drawFittedString("Система эффектов", textX, bottomY + 35, textWidth, MUTED_TEXT_COLOR, false);
                drawFittedString("будет подключена позже", textX, bottomY + 47, textWidth, MUTED_TEXT_COLOR, false);
            }
        }
    }

    private void drawFittedString(String text, int x, int y, int maxWidth, int color, boolean shadow) {
        String fitted = text;
        if (fontRenderer.getStringWidth(fitted) > maxWidth) {
            fitted = fontRenderer.trimStringToWidth(fitted, maxWidth);
        }
        if (shadow) fontRenderer.drawStringWithShadow(fitted, x, y, color);
        else fontRenderer.drawString(fitted, x, y, color);
    }

    private void drawDivider(int x, int y, int w) {
        drawRect(x, y, x + Math.max(1, w), y + 1, PANEL_BORDER_COLOR);
    }

    private void drawStatLine(int x, int y, String name, String value, int color, int availableWidth) {
        int valueWidth = fontRenderer.getStringWidth(value);
        int valueX = x + availableWidth - valueWidth;
        int nameMaxWidth = Math.max(8, valueX - x - 4);
        String fittedName = name;
        if (fontRenderer.getStringWidth(fittedName) > nameMaxWidth) {
            fittedName = fontRenderer.trimStringToWidth(fittedName, nameMaxWidth);
        }
        fontRenderer.drawString(fittedName, x, y, color);
        fontRenderer.drawString(value, valueX, y, TEXT_COLOR);
    }

    private void drawCenterPanelContent(int mouseX, int mouseY) {
        int innerX = centerPanelX + PANEL_PADDING;
        int innerW = centerPanelWidth - PANEL_PADDING * 2;

        drawFittedString("СНАРЯЖЕНИЕ", innerX, centerPanelY + PANEL_PADDING,
                Math.max(20, innerW), TEXT_COLOR, true);
        drawDivider(innerX, centerPanelY + 23, innerW);

        int slotSize = Math.max(14, Math.min(28, centerPanelWidth / 7));
        int gap = compactLayout ? 3 : 4;
        int previewTop = centerPanelY + 33;
        int controlsReserved = compactLayout ? 84 : 64;
        int previewBottom = centerPanelY + centerPanelHeight - controlsReserved - 10;
        int cx = centerPanelX + centerPanelWidth / 2;

        int topWidth = slotSize * 4 + gap * 3;
        int topX = cx - topWidth / 2;
        for (int i = 0; i < 4; i++) {
            drawEquipmentSlot(topX + i * (slotSize + gap), previewTop, slotSize);
        }

        int sideY1 = previewTop + slotSize + 18;
        int sideY2 = sideY1 + slotSize + gap;
        if (!compactLayout || centerPanelWidth >= 175) {
            drawEquipmentSlot(innerX + 4, sideY1, slotSize);
            drawEquipmentSlot(innerX + 4, sideY2, slotSize);
            drawEquipmentSlot(centerPanelX + centerPanelWidth - PANEL_PADDING - 4 - slotSize, sideY1, slotSize);
            drawEquipmentSlot(centerPanelX + centerPanelWidth - PANEL_PADDING - 4 - slotSize, sideY2, slotSize);
        }

        int bottomY = Math.max(sideY2 + slotSize + 8, previewBottom - slotSize);
        for (int i = 0; i < 4; i++) {
            drawEquipmentSlot(topX + i * (slotSize + gap), bottomY, slotSize);
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && bottomY - previewTop > 55) {
            int entityBottom = bottomY - 6;
            int previewScale = Math.max(14, Math.min(42,
                    Math.min(centerPanelWidth / 4, Math.max(14, (bottomY - previewTop) / 5))));

            GuiInventory.drawEntityOnScreen(
                    cx,
                    entityBottom,
                    previewScale,
                    cx - mouseX,
                    entityBottom - previewScale * 2 - mouseY,
                    mc.player
            );
        }

        String selected = "Покупка: " + (buyAmount == Integer.MAX_VALUE ? "xВсе" : "x" + buyAmount);
        int selectedY = centerPanelY + centerPanelHeight - (compactLayout ? 87 : 67);
        int selectedX = cx - fontRenderer.getStringWidth(selected) / 2;
        fontRenderer.drawString(selected, selectedX, selectedY, MUTED_TEXT_COLOR);
    }

    private void drawEquipmentSlot(int x, int y, int size) {
        drawRect(x, y, x + size, y + size, SLOT_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + size - 1, y + size - 1, SLOT_BG_COLOR);
        if (size >= 18) {
            drawRect(x + 2, y + 2, x + 6, y + 3, 0xFF343E48);
            drawRect(x + 2, y + 2, x + 3, y + 6, 0xFF343E48);
        }
    }

    private void drawRunePanelContent(int mouseX, int mouseY) {
        int titleX = runePanelX + PANEL_PADDING;
        int titleY = runePanelY + PANEL_PADDING;

        fontRenderer.drawStringWithShadow("РУНЫ", titleX, titleY, TEXT_COLOR);
        String countText = compactLayout ? "26" : "26 слотов";
        fontRenderer.drawString(countText,
                runePanelX + runePanelWidth - PANEL_PADDING - fontRenderer.getStringWidth(countText),
                titleY,
                MUTED_TEXT_COLOR);

        drawDivider(titleX, runePanelY + 23, runePanelWidth - PANEL_PADDING * 2);

        for (int i = 0; i < VISIBLE_RUNE_SLOTS; i++) {
            int[] pos = getRuneSlotPosition(i);
            drawRuneSlot(pos[0], pos[1], i, mouseX, mouseY);
        }
    }

    private void drawRuneSlot(int x, int y, int visualIndex, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + runeSlotWidth
                && mouseY >= y && mouseY < y + runeSlotHeight;

        int border = hovered ? SLOT_HOVER_COLOR : SLOT_BORDER_COLOR;
        drawRect(x, y, x + runeSlotWidth, y + runeSlotHeight, border);
        drawRect(x + 1, y + 1, x + runeSlotWidth - 1, y + runeSlotHeight - 1, SLOT_BG_COLOR);

        CardData rune = getRuneForVisualSlot(visualIndex);
        if (rune == null) return;

        drawRankCorners(x, y, runeSlotWidth, runeSlotHeight, getRankColor(rune.layer));

        if (visualIndex < CARD_ICONS.length && runeSlotWidth >= 20 && runeSlotHeight >= 24) {
            int iconX = x + (runeSlotWidth - 16) / 2;
            int iconY = y + Math.max(4, (runeSlotHeight - 16) / 2 - 4);
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            renderItem.renderItemIntoGUI(CARD_ICONS[visualIndex], iconX, iconY);
            GlStateManager.popMatrix();
        }

        if (runeSlotWidth >= 30 && runeSlotHeight >= 36 && visualIndex < RUNE_TYPES.length) {
            String type = RUNE_TYPES[visualIndex];
            int maxWidth = Math.max(1, runeSlotWidth - 5);
            if (fontRenderer.getStringWidth(type) > maxWidth) {
                type = fontRenderer.trimStringToWidth(type, maxWidth);
            }
            fontRenderer.drawString(type,
                    x + (runeSlotWidth - fontRenderer.getStringWidth(type)) / 2,
                    y + runeSlotHeight - 10,
                    0xFFAAB1B8);
        }
    }

    private void drawRankCorners(int x, int y, int w, int h, int color) {
        int length = Math.max(4, Math.min(10, w / 4));
        int thickness = w >= 30 ? 2 : 1;

        drawRect(x + 2, y + 2, x + 2 + length, y + 2 + thickness, color);
        drawRect(x + 2, y + 2, x + 2 + thickness, y + 2 + length, color);
        drawRect(x + w - 2 - length, y + 2, x + w - 2, y + 2 + thickness, color);
        drawRect(x + w - 2 - thickness, y + 2, x + w - 2, y + 2 + length, color);
    }

    private void drawRuneTooltip(int mouseX, int mouseY) {
        for (int i = 0; i < VISIBLE_RUNE_SLOTS; i++) {
            int[] pos = getRuneSlotPosition(i);
            int x = pos[0];
            int y = pos[1];

            if (mouseX < x || mouseX >= x + runeSlotWidth
                    || mouseY < y || mouseY >= y + runeSlotHeight) continue;

            CardData rune = getRuneForVisualSlot(i);
            if (rune == null || i >= CARD_NAMES.length) return;

            List<String> tooltip = new ArrayList<>();
            tooltip.add("§b" + CARD_NAMES[i]);
            tooltip.add("");
            tooltip.add("§7Ранг: §f" + getRankName(rune.layer));
            tooltip.add("§7Тип: §f" + (i < RUNE_TYPES.length ? RUNE_TYPES[i] : "Неизвестно"));
            tooltip.add("§7Эффект: §a+" + CARD_STATS[i]);
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

    private void drawFlyingRune(FlyingCard card) {
        int x = (int) card.getX();
        int y = (int) card.getY();
        int w = Math.max(14, Math.min(34, runeSlotWidth));
        int h = Math.max(18, Math.min(42, runeSlotHeight));

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
        drawDefaultBackground();
        updateLayout();

        drawRect(0, 0, width, height, OVERLAY_COLOR);
        drawHeader();

        drawPanel(leftPanelX, leftPanelY, leftPanelWidth, leftPanelHeight);
        drawPanel(centerPanelX, centerPanelY, centerPanelWidth, centerPanelHeight);
        drawPanel(runePanelX, runePanelY, runePanelWidth, runePanelHeight);

        drawLeftPanelContent();
        drawCenterPanelContent(mouseX, mouseY);
        drawRunePanelContent(mouseX, mouseY);

        for (FlyingCard card : flyingCards) {
            drawFlyingRune(card);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawElementTooltip(mouseX, mouseY);
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

        if (slots[slot] == null) slots[slot] = new ArrayList<>();
        slots[slot].add(new CardData(cardIndex, layer));

        if (animate && cardIndex >= 0 && cardIndex < VISIBLE_RUNE_SLOTS) {
            int[] pos = getRuneSlotPosition(cardIndex);
            flyingCards.add(new FlyingCard(
                    centerPanelX + centerPanelWidth / 2.0,
                    centerPanelY + centerPanelHeight - 45.0,
                    pos[0] + runeSlotWidth / 2.0,
                    pos[1] + runeSlotHeight / 2.0,
                    cardIndex,
                    getRankColor(layer)
            ));
        }

        CustomGuiMod.logger.info("Добавлена руна от сервера: слот " + slot + ", индекс " + cardIndex
                + " (анимация: " + animate + ")");
    }

    public void clearCards() {
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            if (slots[i] == null) slots[i] = new ArrayList<>();
            else slots[i].clear();
        }
        flyingCards.clear();
        CustomGuiMod.logger.info("Руны очищены на клиенте");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
