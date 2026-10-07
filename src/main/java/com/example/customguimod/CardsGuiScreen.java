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

    private static final int STORAGE_SLOT_COUNT = 50;
    private static final int VISIBLE_RUNE_SLOTS = 26;

    private static final int GUI_MAX_WIDTH = 980;
    private static final int GUI_MAX_HEIGHT = 520;
    private static final int GUI_OUTER_MARGIN = 10;

    private static final int OVERLAY_COLOR = 0x98000000;
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

    private static List<CardData>[] slots = new List[STORAGE_SLOT_COUNT];
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

    private int runeColumns = 9;
    private int runeRows = 3;
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

        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            slots[i] = new ArrayList<>();
        }

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
        layoutScale = Math.max(0.38F, layoutScale);

        guiWidth = Math.min(availableWidth, Math.round(GUI_MAX_WIDTH * layoutScale));
        guiHeight = Math.min(availableHeight, Math.round(GUI_MAX_HEIGHT * layoutScale));
        compactLayout = guiWidth < 620 || guiHeight < 330;

        guiX = (width - guiWidth) / 2;
        guiY = (height - guiHeight) / 2;

        int navGap = scaled(4, 2);
        navPanelWidth = scaled(34, 24);
        navPanelHeight = guiHeight;
        navPanelX = guiX + guiWidth - navPanelWidth;
        navPanelY = guiY;

        mainX = guiX;
        mainY = guiY;
        mainWidth = guiWidth - navPanelWidth - navGap;
        mainHeight = guiHeight;

        int innerGap = scaled(3, 1);
        int usableWidth = mainWidth - innerGap * 2;

        leftPanelWidth = usableWidth * 18 / 100;
        centerPanelWidth = usableWidth * 25 / 100;
        runePanelWidth = usableWidth - leftPanelWidth - centerPanelWidth;

        leftPanelX = mainX;
        leftPanelY = mainY;
        leftPanelHeight = mainHeight;

        centerPanelX = leftPanelX + leftPanelWidth + innerGap;
        centerPanelY = mainY;
        centerPanelHeight = mainHeight;

        runePanelX = centerPanelX + centerPanelWidth + innerGap;
        runePanelY = mainY;
        runePanelHeight = mainHeight;

        updateRuneGridLayout();
    }

    private void updateRuneGridLayout() {
        int pad = scaled(9, 4);
        int topReserved = scaled(58, 42);
        int bottomPadding = scaled(10, 4);
        int availableWidth = Math.max(1, runePanelWidth - pad * 2);
        int availableHeight = Math.max(1, runePanelHeight - topReserved - bottomPadding);

        runeSlotGap = scaled(4, 2);

        int bestColumns = 1;
        int bestSlotW = 12;
        int bestSlotH = 16;
        double bestScore = -Double.MAX_VALUE;

        int maxColumns = Math.min(VISIBLE_RUNE_SLOTS, 14);
        for (int columns = 4; columns <= maxColumns; columns++) {
            int rows = (VISIBLE_RUNE_SLOTS + columns - 1) / columns;
            int slotW = (availableWidth - runeSlotGap * (columns - 1)) / columns;
            int slotHByHeight = (availableHeight - runeSlotGap * (rows - 1)) / rows;
            int slotH = Math.min(slotHByHeight, slotW * 6 / 5);

            if (slotW < 12 || slotH < 16) continue;

            int empty = rows * columns - VISIBLE_RUNE_SLOTS;
            int lastFill = VISIBLE_RUNE_SLOTS % columns;
            if (lastFill == 0) lastFill = columns;

            double readable = Math.min(slotW, slotH / 1.2D);
            double orphanPenalty = lastFill <= 2 ? 28.0D : (lastFill <= 4 ? 10.0D : 0.0D);
            double score = readable * 10.0D - empty * 3.5D - rows * 2.0D - orphanPenalty;

            if (score > bestScore) {
                bestScore = score;
                bestColumns = columns;
                bestSlotW = slotW;
                bestSlotH = slotH;
            }
        }

        runeColumns = bestColumns;
        runeRows = (VISIBLE_RUNE_SLOTS + runeColumns - 1) / runeColumns;
        runeSlotWidth = Math.max(12, Math.min(scaled(54, 24), bestSlotW));
        runeSlotHeight = Math.max(16, Math.min(scaled(64, 30), bestSlotH));

        int gridWidth = runeColumns * runeSlotWidth + (runeColumns - 1) * runeSlotGap;
        int gridHeight = runeRows * runeSlotHeight + (runeRows - 1) * runeSlotGap;

        runeGridX = runePanelX + (runePanelWidth - gridWidth) / 2;
        runeGridY = runePanelY + topReserved + Math.max(scaled(5, 2), (availableHeight - gridHeight) / 8);
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
    }

    private void rebuildElementButtons() {
        int count = Math.min(deckNames.size(), MAX_ELEMENTS);
        if (count <= 0) return;

        int x = runePanelX + scaled(8, 4);
        int y = runePanelY + scaled(30, 22);
        int gap = scaled(3, 2);
        int availableRight = runePanelX + runePanelWidth - scaled(8, 4);

        for (int i = 0; i < count; i++) {
            String name = deckNames.get(i);
            int desired = Math.max(scaled(38, 28), fontRenderer.getStringWidth(name) + scaled(14, 7));
            int remaining = count - i;
            int maxForThis = Math.max(24, (availableRight - x - gap * (remaining - 1)) / remaining);
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

        for (int i = 0; i < 3; i++) {
            y += buttonSize + gap;
            buttonList.add(new NavButton(
                    NAV_PLACEHOLDER_START_ID + i, x, y, buttonSize, buttonSize,
                    new ItemStack(Items.COMPASS), false, false
            ));
        }

        int closeSize = buttonSize;
        int closeY = navPanelY + navPanelHeight - pad - closeSize;
        buttonList.add(new CristalixButton(
                CLOSE_BUTTON_ID, x, closeY, closeSize, closeSize, "X"
        ));
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
        int pad = scaled(7, 3);
        int x = leftPanelX + pad;
        int w = leftPanelWidth - pad * 2;
        int topH = Math.max(scaled(170, 90), leftPanelHeight * 58 / 100);

        drawSection(x, leftPanelY + pad, w, topH - pad);
        int textX = x + scaled(7, 4);
        int textW = Math.max(10, w - scaled(14, 8));
        int y = leftPanelY + pad + scaled(9, 5);

        drawFittedString("СТАТИСТИКА", textX, y, textW, TEXT_COLOR, true);
        y += scaled(17, 12);
        drawDivider(x + scaled(5, 3), y, w - scaled(10, 6));
        y += scaled(8, 5);

        drawStatLine(textX, y, "Монеты", String.valueOf(ClientPlayerStats.getCoins()), 0xFFFFC83D, textW);
        y += scaled(14, 11);
        drawStatLine(textX, y, "Кристаллы", String.valueOf(ClientPlayerStats.getCrystals()), 0xFF56D7E8, textW);
        y += scaled(14, 11);
        drawStatLine(textX, y, "Урон", "+" + ClientPlayerStats.getTotalDamage(), 0xFFFF7272, textW);
        y += scaled(20, 14);
        drawFittedString("ЭФФЕКТЫ", textX, y, textW, MUTED_TEXT_COLOR, false);

        int lowerY = leftPanelY + pad + topH + scaled(3, 2);
        int lowerH = leftPanelY + leftPanelHeight - pad - lowerY;
        if (lowerH > 24) {
            drawSection(x, lowerY, w, lowerH);
            drawFittedString("ВЫБРАННАЯ СБОРКА", textX,
                    lowerY + scaled(9, 5), textW, TEXT_COLOR, true);
            drawDivider(x + scaled(5, 3),
                    lowerY + scaled(25, 17), w - scaled(10, 6));
        }
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

        drawFittedString("СНАРЯЖЕНИЕ", innerX, centerPanelY + pad,
                innerW, TEXT_COLOR, true);
        drawDivider(innerX, centerPanelY + scaled(23, 16), innerW);

        int controlsReserved = compactLayout ? scaled(76, 56) : scaled(58, 42);
        int controlsTop = centerPanelY + centerPanelHeight - controlsReserved;
        int slot = scaled(29, 15);
        int gap = scaled(4, 2);
        int cx = centerPanelX + centerPanelWidth / 2;

        int topY = centerPanelY + scaled(34, 22);
        int rowWidth = slot * 4 + gap * 3;
        int topX = cx - rowWidth / 2;
        for (int i = 0; i < 4; i++) {
            drawEquipmentSlot(topX + i * (slot + gap), topY, slot);
        }

        int bottomY = controlsTop - slot - scaled(10, 4);
        for (int i = 0; i < 4; i++) {
            drawEquipmentSlot(topX + i * (slot + gap), bottomY, slot);
        }

        int frameTop = topY + slot + scaled(7, 3);
        int frameBottom = bottomY - scaled(7, 3);
        int sideGap = scaled(6, 3);
        int frameX = centerPanelX + pad + slot + sideGap;
        int frameRight = centerPanelX + centerPanelWidth - pad - slot - sideGap;
        int frameW = Math.max(24, frameRight - frameX);
        int frameH = Math.max(28, frameBottom - frameTop);
        drawSection(frameX, frameTop, frameW, frameH);

        if (centerPanelWidth >= scaled(170, 110) && frameH >= slot + 10) {
            int sideY = frameTop + (frameH - slot) / 2;
            drawEquipmentSlot(centerPanelX + pad, sideY, slot);
            drawEquipmentSlot(centerPanelX + centerPanelWidth - pad - slot, sideY, slot);
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && frameH > 22) {
            int entityBottom = frameBottom - scaled(4, 2);
            int previewScale = Math.max(10, Math.min(scaled(48, 22), Math.max(10, frameH / 3)));
            GuiInventory.drawEntityOnScreen(
                    cx, entityBottom, previewScale,
                    cx - mouseX,
                    entityBottom - previewScale * 2 - mouseY,
                    mc.player
            );
        }
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

        drawFittedString("РУНЫ", titleX, titleY,
                runePanelWidth / 2, TEXT_COLOR, true);
        String count = compactLayout ? "26" : "26 рун";
        fontRenderer.drawString(count,
                runePanelX + runePanelWidth - pad - fontRenderer.getStringWidth(count),
                titleY,
                MUTED_TEXT_COLOR);
        drawDivider(titleX, runePanelY + scaled(23, 16), runePanelWidth - pad * 2);

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

        if (visualIndex < CARD_ICONS.length && runeSlotWidth >= 18 && runeSlotHeight >= 22) {
            int iconX = x + (runeSlotWidth - 16) / 2;
            int iconY = y + Math.max(3, (runeSlotHeight - 16) / 2 - scaled(4, 2));
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            renderItem.renderItemIntoGUI(CARD_ICONS[visualIndex], iconX, iconY);
            GlStateManager.popMatrix();
        }

        if (runeSlotWidth >= 31 && runeSlotHeight >= 37 && visualIndex < RUNE_TYPES.length) {
            String type = RUNE_TYPES[visualIndex];
            int max = Math.max(1, runeSlotWidth - 6);
            if (fontRenderer.getStringWidth(type) > max) {
                type = fontRenderer.trimStringToWidth(type, max);
            }
            fontRenderer.drawString(type,
                    x + (runeSlotWidth - fontRenderer.getStringWidth(type)) / 2,
                    y + runeSlotHeight - 11,
                    0xFFAAB1B8);
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
        drawDefaultBackground();
        updateLayout();

        drawRect(0, 0, width, height, OVERLAY_COLOR);

        drawPanel(leftPanelX, leftPanelY, leftPanelWidth, leftPanelHeight);
        drawPanel(centerPanelX, centerPanelY, centerPanelWidth, centerPanelHeight);
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

        if (slots[slot] == null) slots[slot] = new ArrayList<>();
        slots[slot].add(new CardData(cardIndex, layer));

        if (animate && cardIndex >= 0 && cardIndex < VISIBLE_RUNE_SLOTS) {
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
