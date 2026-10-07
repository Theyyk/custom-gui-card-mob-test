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

    // Server-side storage is still the v1.1 50-slot card model.
    // v1.2 presents it as a 26-slot rune collection without changing the server contract yet.
    private static final int STORAGE_SLOT_COUNT = 50;
    private static final int VISIBLE_RUNE_SLOTS = 26;
    private static final int WIDE_RUNE_COLUMNS = 13;

    private static final int GUI_MAX_WIDTH = 900;
    private static final int GUI_MAX_HEIGHT = 500;
    private static final int GUI_OUTER_MARGIN = 10;
    private static final int PANEL_GAP = 6;
    private static final int PANEL_PADDING = 8;

    private static final int OVERLAY_COLOR = 0x88000000;
    private static final int PANEL_BG_COLOR = 0xD014181E;
    private static final int PANEL_BORDER_COLOR = 0xCC29323C;
    private static final int PANEL_INNER_BORDER_COLOR = 0x66465B70;
    private static final int SLOT_BG_COLOR = 0xE014181D;
    private static final int SLOT_BORDER_COLOR = 0xFF252D35;
    private static final int SLOT_HOVER_COLOR = 0xFF3A4856;
    private static final int SELECTED_COLOR = 0xFF2E72C7;
    private static final int TEXT_COLOR = 0xFFE5E5E5;
    private static final int MUTED_TEXT_COLOR = 0xFF7C838A;

    private static final int NORMAL_COLOR = 0xFF2E72C7;
    private static final int SILVER_COLOR = 0xFFC4C8CE;
    private static final int GOLD_COLOR = 0xFFFFC940;

    private static final int BUY_BUTTON_ID = 0;
    private static final int AMOUNT_BUTTON_START_ID = 1;
    private static final int ELEMENT_BUTTON_START_ID = 100;
    private static final int MAX_ELEMENTS = 8;

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

    private static class FlatGuiButton extends GuiButton {
        private boolean selected;

        FlatGuiButton(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) return;

            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int border = selected ? SELECTED_COLOR : (hovered ? SLOT_HOVER_COLOR : SLOT_BORDER_COLOR);
            int background = enabled ? 0xE0161B21 : 0xAA101317;
            int text = enabled ? TEXT_COLOR : MUTED_TEXT_COLOR;

            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, border);
            drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, background);
            drawCenteredString(mc.fontRenderer, this.displayString,
                    this.x + this.width / 2,
                    this.y + (this.height - 8) / 2,
                    text);
        }
    }

    private static class ElementGuiButton extends GuiButton {
        private final int elementIndex;
        private final String elementName;
        private final boolean active;

        ElementGuiButton(int id, int x, int y, int width, int height,
                         int elementIndex, String elementName, boolean active) {
            super(id, x, y, width, height, elementName);
            this.elementIndex = elementIndex;
            this.elementName = elementName;
            this.active = active;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!this.visible) return;

            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int border = active ? SELECTED_COLOR : (hovered ? SLOT_HOVER_COLOR : SLOT_BORDER_COLOR);
            int background = active ? 0xE01B2B3B : 0xE014181D;

            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, border);
            drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, background);

            String label = elementName;
            int maxTextWidth = Math.max(1, this.width - 8);
            if (mc.fontRenderer.getStringWidth(label) > maxTextWidth) {
                label = mc.fontRenderer.trimStringToWidth(label, maxTextWidth);
            }

            drawCenteredString(mc.fontRenderer, label,
                    this.x + this.width / 2,
                    this.y + (this.height - 8) / 2,
                    active ? 0xFFFFFFFF : TEXT_COLOR);
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
        this.buttonList.clear();

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
        this.buttonList.clear();
        updateLayout();

        int controlsPadding = Math.max(4, Math.min(8, centerPanelWidth / 20));
        int buttonX = centerPanelX + controlsPadding;
        int buttonWidth = Math.max(40, centerPanelWidth - controlsPadding * 2);
        int amountHeight = 17;
        int buyHeight = 20;
        int bottom = centerPanelY + centerPanelHeight - controlsPadding;

        int amountY = bottom - amountHeight;
        int buyY = amountY - buyHeight - 4;

        this.buttonList.add(new FlatGuiButton(
                BUY_BUTTON_ID,
                buttonX,
                buyY,
                buttonWidth,
                buyHeight,
                "Купить руну"
        ));

        String[] amountLabels = {"x1", "x5", "x10", "x100", "xВсе"};
        int amountGap = 2;
        int amountButtonWidth = Math.max(12,
                (buttonWidth - amountGap * (amountLabels.length - 1)) / amountLabels.length);

        for (int i = 0; i < amountLabels.length; i++) {
            FlatGuiButton button = new FlatGuiButton(
                    AMOUNT_BUTTON_START_ID + i,
                    buttonX + i * (amountButtonWidth + amountGap),
                    amountY,
                    amountButtonWidth,
                    amountHeight,
                    amountLabels[i]
            );
            this.buttonList.add(button);
        }

        rebuildElementButtons();
        updateAmountButtonSelection();
    }

    private void rebuildElementButtons() {
        for (int i = this.buttonList.size() - 1; i >= 0; i--) {
            int id = this.buttonList.get(i).id;
            if (id >= ELEMENT_BUTTON_START_ID && id < ELEMENT_BUTTON_START_ID + MAX_ELEMENTS) {
                this.buttonList.remove(i);
            }
        }

        int count = Math.min(deckNames.size(), MAX_ELEMENTS);
        if (count <= 0) return;

        int gap = 3;
        int availableWidth = Math.max(1, runePanelWidth - PANEL_PADDING * 2);
        int buttonWidth = Math.max(20, (availableWidth - gap * (count - 1)) / count);
        int buttonHeight = 18;
        int y = runePanelY + 24;

        for (int i = 0; i < count; i++) {
            this.buttonList.add(new ElementGuiButton(
                    ELEMENT_BUTTON_START_ID + i,
                    runePanelX + PANEL_PADDING + i * (buttonWidth + gap),
                    y,
                    buttonWidth,
                    buttonHeight,
                    i,
                    deckNames.get(i),
                    i == activeDeck
            ));
        }
    }

    private void updateAmountButtonSelection() {
        int[] amounts = {1, 5, 10, 100, Integer.MAX_VALUE};
        for (GuiButton guiButton : this.buttonList) {
            if (!(guiButton instanceof FlatGuiButton)) continue;
            if (guiButton.id < AMOUNT_BUTTON_START_ID
                    || guiButton.id >= AMOUNT_BUTTON_START_ID + amounts.length) continue;

            ((FlatGuiButton) guiButton).setSelected(
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
        int availableWidth = Math.max(1, this.width - GUI_OUTER_MARGIN * 2);
        int availableHeight = Math.max(1, this.height - GUI_OUTER_MARGIN * 2);

        guiWidth = Math.min(availableWidth, GUI_MAX_WIDTH);
        guiHeight = Math.min(availableHeight, GUI_MAX_HEIGHT);

        guiX = (this.width - guiWidth) / 2;
        guiY = (this.height - guiHeight) / 2;

        int headerHeight = Math.max(28, Math.min(38, guiHeight / 10));
        int contentY = guiY + headerHeight;
        int contentHeight = Math.max(80, guiHeight - headerHeight - 4);
        int usableWidth = Math.max(1, guiWidth - PANEL_GAP * 2);

        leftPanelWidth = usableWidth * 18 / 100;
        centerPanelWidth = usableWidth * 24 / 100;
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
        int topReserved = 58;
        int bottomPadding = 10;
        int availableWidth = Math.max(1, runePanelWidth - horizontalPadding * 2);
        int availableHeight = Math.max(1, runePanelHeight - topReserved - bottomPadding);

        runeSlotGap = Math.max(2, Math.min(4, availableWidth / 150));

        // 13x2 is used when it fits. Narrow screens automatically reflow the same
        // 26 fixed rune positions into more rows instead of pushing slots outside the panel.
        int minReadableSlotWidth = 18;
        int maxColumnsByWidth = Math.max(1,
                (availableWidth + runeSlotGap) / (minReadableSlotWidth + runeSlotGap));

        if (maxColumnsByWidth >= 13) {
            runeColumns = 13;
        } else if (maxColumnsByWidth >= 9) {
            runeColumns = 9;
        } else if (maxColumnsByWidth >= 7) {
            runeColumns = 7;
        } else if (maxColumnsByWidth >= 5) {
            runeColumns = 5;
        } else {
            runeColumns = Math.max(1, maxColumnsByWidth);
        }
        runeRows = (VISIBLE_RUNE_SLOTS + runeColumns - 1) / runeColumns;

        int widthBased = Math.max(1,
                (availableWidth - runeSlotGap * (runeColumns - 1)) / runeColumns);
        int heightBased = Math.max(1,
                (availableHeight - runeSlotGap * (runeRows - 1)) / runeRows);

        // Never force a minimum that can overflow the panel. On very small windows
        // the slot becomes simpler (type/icon can disappear) but remains inside the GUI.
        runeSlotWidth = Math.max(8, Math.min(46, widthBased));
        runeSlotHeight = Math.max(12,
                Math.min(58, Math.min(heightBased, runeSlotWidth * 5 / 4)));

        int gridWidth = runeColumns * runeSlotWidth + (runeColumns - 1) * runeSlotGap;
        int gridHeight = runeRows * runeSlotHeight + (runeRows - 1) * runeSlotGap;

        runeGridX = runePanelX + (runePanelWidth - gridWidth) / 2;
        runeGridY = runePanelY + topReserved + Math.max(0, (availableHeight - gridHeight) / 4);
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
                if (best == null || card.layer > best.layer) {
                    best = card;
                }
            }
        }
        return best;
    }

    private void drawPanel(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;

        drawRect(x, y, x + width, y + height, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BG_COLOR);

        if (width > 4 && height > 4) {
            drawRect(x + 2, y + 2, x + width - 2, y + 3, PANEL_INNER_BORDER_COLOR);
        }
    }

    private void drawHeader() {
        this.fontRenderer.drawStringWithShadow("РУНЫ", guiX + 4, guiY + 8, TEXT_COLOR);

        String resources = "Монеты: " + ClientPlayerStats.getCoins()
                + "   Кристаллы: " + ClientPlayerStats.getCrystals()
                + "   Урон: +" + ClientPlayerStats.getTotalDamage();
        int resourcesX = guiX + guiWidth - this.fontRenderer.getStringWidth(resources) - 4;
        this.fontRenderer.drawStringWithShadow(resources, resourcesX, guiY + 8, 0xFFB8C0C7);
    }

    private void drawLeftPanelContent() {
        int x = leftPanelX + PANEL_PADDING;
        int y = leftPanelY + PANEL_PADDING;

        this.fontRenderer.drawStringWithShadow("СТАТИСТИКА", x, y, TEXT_COLOR);
        y += 18;

        drawStatLine(x, y, "Монеты", String.valueOf(ClientPlayerStats.getCoins()), 0xFFFFC83D);
        y += 14;
        drawStatLine(x, y, "Кристаллы", String.valueOf(ClientPlayerStats.getCrystals()), 0xFF54D8E8);
        y += 14;
        drawStatLine(x, y, "Урон", "+" + ClientPlayerStats.getTotalDamage(), 0xFFFF6D6D);
        y += 22;

        this.fontRenderer.drawStringWithShadow("ЭФФЕКТЫ", x, y, MUTED_TEXT_COLOR);
        y += 15;
        this.fontRenderer.drawString("Будут добавлены", x, y, MUTED_TEXT_COLOR);
        y += 11;
        this.fontRenderer.drawString("в следующих этапах", x, y, MUTED_TEXT_COLOR);
    }

    private void drawStatLine(int x, int y, String name, String value, int color) {
        this.fontRenderer.drawString(name, x, y, color);
        int valueX = leftPanelX + leftPanelWidth - PANEL_PADDING - this.fontRenderer.getStringWidth(value);
        this.fontRenderer.drawString(value, valueX, y, TEXT_COLOR);
    }

    private void drawCenterPanelContent(int mouseX, int mouseY) {
        int x = centerPanelX + PANEL_PADDING;
        int y = centerPanelY + PANEL_PADDING;
        this.fontRenderer.drawStringWithShadow("СНАРЯЖЕНИЕ", x, y, TEXT_COLOR);

        int slotSize = Math.max(16, Math.min(30, centerPanelWidth / 6));
        int slotGap = 3;
        int topY = centerPanelY + 28;
        int totalTopWidth = slotSize * 4 + slotGap * 3;
        int topX = centerPanelX + (centerPanelWidth - totalTopWidth) / 2;

        for (int i = 0; i < 4; i++) {
            drawEmptyEquipmentSlot(topX + i * (slotSize + slotGap), topY, slotSize);
        }

        int bottomY = centerPanelY + centerPanelHeight - 86 - slotSize;
        for (int i = 0; i < 4; i++) {
            drawEmptyEquipmentSlot(topX + i * (slotSize + slotGap), bottomY, slotSize);
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            int previewX = centerPanelX + centerPanelWidth / 2;
            int previewBottom = bottomY - 8;
            int previewScale = Math.max(16, Math.min(42,
                    Math.min(centerPanelWidth / 4, Math.max(16, centerPanelHeight / 7))));

            GuiInventory.drawEntityOnScreen(
                    previewX,
                    previewBottom,
                    previewScale,
                    previewX - mouseX,
                    previewBottom - previewScale * 2 - mouseY,
                    mc.player
            );
        }

        String selected = "Покупка: " + (buyAmount == Integer.MAX_VALUE ? "xВсе" : "x" + buyAmount);
        int textX = centerPanelX + (centerPanelWidth - this.fontRenderer.getStringWidth(selected)) / 2;
        this.fontRenderer.drawString(selected, textX, centerPanelY + centerPanelHeight - 91, MUTED_TEXT_COLOR);
    }

    private void drawEmptyEquipmentSlot(int x, int y, int size) {
        drawRect(x, y, x + size, y + size, SLOT_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + size - 1, y + size - 1, SLOT_BG_COLOR);
    }

    private void drawRunePanelContent(int mouseX, int mouseY) {
        this.fontRenderer.drawStringWithShadow(
                "РУНЫ",
                runePanelX + PANEL_PADDING,
                runePanelY + PANEL_PADDING,
                TEXT_COLOR
        );

        String countText = "26 слотов";
        this.fontRenderer.drawString(
                countText,
                runePanelX + runePanelWidth - PANEL_PADDING - this.fontRenderer.getStringWidth(countText),
                runePanelY + PANEL_PADDING,
                MUTED_TEXT_COLOR
        );

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

        int rankColor = getRankColor(rune.layer);
        drawRankCorners(x, y, runeSlotWidth, runeSlotHeight, rankColor);

        if (visualIndex < CARD_ICONS.length && runeSlotWidth >= 20 && runeSlotHeight >= 24) {
            int iconX = x + (runeSlotWidth - 16) / 2;
            int iconY = y + Math.max(4, (runeSlotHeight - 16) / 2 - 3);

            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            renderItem.renderItemIntoGUI(CARD_ICONS[visualIndex], iconX, iconY);
            GlStateManager.popMatrix();
        }

        if (runeSlotWidth >= 28 && runeSlotHeight >= 34 && visualIndex < RUNE_TYPES.length) {
            String type = RUNE_TYPES[visualIndex];
            int maxWidth = Math.max(1, runeSlotWidth - 4);
            if (this.fontRenderer.getStringWidth(type) > maxWidth) {
                type = this.fontRenderer.trimStringToWidth(type, maxWidth);
            }
            int textX = x + (runeSlotWidth - this.fontRenderer.getStringWidth(type)) / 2;
            this.fontRenderer.drawString(type, textX, y + runeSlotHeight - 10, 0xFFB8C0C7);
        }
    }

    private void drawRankCorners(int x, int y, int width, int height, int color) {
        int length = Math.max(4, Math.min(10, width / 4));
        int thickness = width >= 32 ? 2 : 1;

        drawRect(x + 2, y + 2, x + 2 + length, y + 2 + thickness, color);
        drawRect(x + 2, y + 2, x + 2 + thickness, y + 2 + length, color);

        drawRect(x + width - 2 - length, y + 2, x + width - 2, y + 2 + thickness, color);
        drawRect(x + width - 2 - thickness, y + 2, x + width - 2, y + 2 + length, color);
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
            this.drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawElementTooltip(int mouseX, int mouseY) {
        for (GuiButton guiButton : this.buttonList) {
            if (!(guiButton instanceof ElementGuiButton)) continue;

            ElementGuiButton elementButton = (ElementGuiButton) guiButton;
            if (mouseX < elementButton.x || mouseX >= elementButton.x + elementButton.width
                    || mouseY < elementButton.y || mouseY >= elementButton.y + elementButton.height) {
                continue;
            }

            List<String> tooltip = new ArrayList<>();
            tooltip.add("§b" + elementButton.getElementName());
            if (!elementButton.isActiveElement()) {
                tooltip.add("§7Нажмите, чтобы выбрать стихию");
            }
            this.drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawFlyingRune(FlyingCard card) {
        int x = (int) card.getX();
        int y = (int) card.getY();
        int width = Math.max(14, Math.min(34, runeSlotWidth));
        int height = Math.max(18, Math.min(42, runeSlotHeight));

        drawRect(x - width / 2, y - height / 2, x + width / 2, y + height / 2, SLOT_BORDER_COLOR);
        drawRect(x - width / 2 + 1, y - height / 2 + 1,
                x + width / 2 - 1, y + height / 2 - 1, SLOT_BG_COLOR);
        drawRankCorners(x - width / 2, y - height / 2, width, height, card.color);

        if (card.cardIndex >= 0 && card.cardIndex < CARD_ICONS.length && width >= 18 && height >= 18) {
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
        this.drawDefaultBackground();
        updateLayout();

        drawRect(0, 0, this.width, this.height, OVERLAY_COLOR);
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
        switch (layer) {
            case 2:
                return SILVER_COLOR;
            case 3:
                return GOLD_COLOR;
            case 1:
            default:
                return NORMAL_COLOR;
        }
    }

    private String getRankName(int layer) {
        switch (layer) {
            case 2:
                return "Серебряная";
            case 3:
                return "Золотая";
            case 1:
            default:
                return "Обычная";
        }
    }

    public void setBalance(int balance) {
        ClientPlayerStats.setCoins(balance);
    }

    public void setDecks(List<String> names, int activeDeck) {
        this.deckNames.clear();
        this.deckNames.addAll(names);
        this.activeDeck = activeDeck;
        rebuildControls();

        CustomGuiMod.logger.info("Elements updated on client: " + deckNames.size()
                + ", selected=" + activeDeck);
    }

    public void addCardFromServer(int slot, int cardIndex, int layer, boolean animate) {
        if (slot < 0 || slot >= STORAGE_SLOT_COUNT) return;

        if (slots[slot] == null) {
            slots[slot] = new ArrayList<>();
        }

        CardData card = new CardData(cardIndex, layer);
        slots[slot].add(card);

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
            if (slots[i] == null) {
                slots[i] = new ArrayList<>();
            } else {
                slots[i].clear();
            }
        }
        flyingCards.clear();
        CustomGuiMod.logger.info("Руны очищены на клиенте");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
