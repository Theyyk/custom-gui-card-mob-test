package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardsGuiScreen extends GuiScreen {

    private static final int ROWS = 5;
    private static final int COLS = 10;
    private static final int SLOT_COUNT = ROWS * COLS;

    private static final int CARD_WIDTH = 40;
    private static final int CARD_HEIGHT = 50;
    private static final int CARD_GAP = 4;

    private static final int GUI_MAX_WIDTH = 900;
    private static final int GUI_MAX_HEIGHT = 500;
    private static final int GUI_OUTER_MARGIN = 10;
    private static final int PANEL_GAP = 6;

    private static final int OVERLAY_COLOR = 0x88000000;
    private static final int PANEL_BG_COLOR = 0xC0181E26;
    private static final int PANEL_BORDER_COLOR = 0xCC3B5366;
    private static final int PANEL_INNER_BORDER_COLOR = 0x663B5366;

    private static final int DECK_BUTTON_START_ID = 100;
    private static final int MAX_DECKS = 9;
    private static final int DECK_BUTTON_WIDTH = 30;
    private static final int DECK_BUTTON_HEIGHT = 20;
    private static final int DECK_BUTTON_GAP = 3;
    private static final ItemStack DECK_ICON = new ItemStack(Blocks.CHEST);

    private static List<CardData>[] slots = new List[SLOT_COUNT];
    private final Random random = new Random();
    private final List<String> deckNames = new ArrayList<>();
    private int activeDeck = 0;

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

    private static final String[] CARD_NAMES = {
            "Булава", "Накидка вора", "Лесной дух", "Щит",
            "Низший голем", "Речной дракончик", "Энт-пугатель",
            "Хижина", "Посох друида", "Книга земли"
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

    private static final int BRONZE_COLOR = 0xFFCD7F32;
    private static final int SILVER_COLOR = 0xFFC0C0C0;
    private static final int GOLD_COLOR = 0xFFFFD700;
    private static final int CARD_BG_COLOR = 0xFFFFEE99;

    private final List<FlyingCard> flyingCards = new ArrayList<>();
    private int buyAmount = 1;
    private static RenderItem renderItem;

    private static class CardData {
        int cardIndex;
        int layer;

        CardData(int cardIndex, int layer) {
            this.cardIndex = cardIndex;
            this.layer = layer;
        }
    }

    private static class FlyingCard {
        double startX, startY;
        double endX, endY;
        double progress;
        int cardIndex;
        int color;

        FlyingCard(double startX, double startY, double endX, double endY, int cardIndex, int color) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.progress = 0;
            this.cardIndex = cardIndex;
            this.color = color;
        }

        void update() {
            progress += 0.12;
            if (progress > 1) progress = 1;
        }

        double getX() {
            return startX + (endX - startX) * progress;
        }

        double getY() {
            return startY + (endY - startY) * progress;
        }

        boolean isDone() {
            return progress >= 1;
        }
    }

    private static class DeckGuiButton extends GuiButton {
        private final int deckIndex;
        private final String deckName;
        private final boolean active;

        DeckGuiButton(int id, int x, int y, int deckIndex, String deckName, boolean active) {
            super(id, x, y, DECK_BUTTON_WIDTH, DECK_BUTTON_HEIGHT, "");
            this.deckIndex = deckIndex;
            this.deckName = deckName;
            this.active = active;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            super.drawButton(mc, mouseX, mouseY, partialTicks);
            if (!this.visible) return;

            GlStateManager.pushMatrix();
            mc.getRenderItem().renderItemIntoGUI(DECK_ICON, this.x + 2, this.y + 2);
            GlStateManager.popMatrix();

            String number = String.valueOf(deckIndex + 1);
            int numberColor = active ? 0x55FF55 : 0xFFFFFF;
            mc.fontRenderer.drawStringWithShadow(number, this.x + 20, this.y + 6, numberColor);
        }

        int getDeckIndex() {
            return deckIndex;
        }

        String getDeckName() {
            return deckName;
        }

        boolean isActiveDeck() {
            return active;
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        if (renderItem == null) {
            renderItem = Minecraft.getMinecraft().getRenderItem();
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = new ArrayList<>();
        }

        int centerX = this.width / 2;
        int btnY = this.height - 60;

        this.buttonList.add(new GuiButton(0, centerX - 100, btnY, 200, 20, "Купить карточку"));

        this.buttonList.add(new GuiButton(1, centerX - 160, btnY + 25, 50, 18, "x1"));
        this.buttonList.add(new GuiButton(2, centerX - 105, btnY + 25, 50, 18, "x5"));
        this.buttonList.add(new GuiButton(3, centerX - 50, btnY + 25, 50, 18, "x10"));
        this.buttonList.add(new GuiButton(4, centerX + 5, btnY + 25, 50, 18, "x100"));
        this.buttonList.add(new GuiButton(5, centerX + 60, btnY + 25, 50, 18, "xВсе"));

        rebuildDeckButtons();

        NetworkHandler.INSTANCE.sendToServer(new PingPacket("get_player_stats"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_decks"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_cards"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= DECK_BUTTON_START_ID && button.id < DECK_BUTTON_START_ID + MAX_DECKS) {
            int targetDeck = button.id - DECK_BUTTON_START_ID;
            if (targetDeck >= 0 && targetDeck < deckNames.size() && targetDeck != activeDeck) {
                clearCards();
                NetworkHandler.INSTANCE.sendToServer(new PingPacket("switch_deck:" + targetDeck));
            }
            return;
        }

        if (button.id == 0) {
            String amount = buyAmount == Integer.MAX_VALUE ? "all" : String.valueOf(buyAmount);
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("buy_cards:" + amount));
        } else if (button.id == 1) buyAmount = 1;
        else if (button.id == 2) buyAmount = 5;
        else if (button.id == 3) buyAmount = 10;
        else if (button.id == 4) buyAmount = 100;
        else if (button.id == 5) buyAmount = Integer.MAX_VALUE;
    }

    private void rebuildDeckButtons() {
        for (int i = this.buttonList.size() - 1; i >= 0; i--) {
            int id = this.buttonList.get(i).id;
            if (id >= DECK_BUTTON_START_ID && id < DECK_BUTTON_START_ID + MAX_DECKS) {
                this.buttonList.remove(i);
            }
        }

        int count = Math.min(deckNames.size(), MAX_DECKS);
        if (count <= 0) return;

        int totalWidth = count * DECK_BUTTON_WIDTH + (count - 1) * DECK_BUTTON_GAP;
        int startX = (this.width - totalWidth) / 2;
        int y = 41;

        for (int i = 0; i < count; i++) {
            this.buttonList.add(new DeckGuiButton(
                    DECK_BUTTON_START_ID + i,
                    startX + i * (DECK_BUTTON_WIDTH + DECK_BUTTON_GAP),
                    y,
                    i,
                    deckNames.get(i),
                    i == activeDeck
            ));
        }
    }

    private int getCurrentLayer() {
        for (int i = 0; i < SLOT_COUNT; i++) if (slots[i].size() < 1) return 1;
        for (int i = 0; i < SLOT_COUNT; i++) if (slots[i].size() < 2) return 2;
        for (int i = 0; i < SLOT_COUNT; i++) if (slots[i].size() < 3) return 3;
        return -1;
    }

    private int findSlotForLayer(int layer) {
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i].size() == layer - 1) available.add(i);
        }
        if (available.isEmpty()) return -1;
        return available.get(random.nextInt(available.size()));
    }

    private int[] getSlotPosition(int index) {
        int row = index / COLS;
        int col = index % COLS;

        int gridWidth = COLS * CARD_WIDTH + (COLS - 1) * CARD_GAP;
        int gridHeight = ROWS * CARD_HEIGHT + (ROWS - 1) * CARD_GAP;

        int startX = (this.width - gridWidth) / 2;
        int startY = (this.height - gridHeight) / 2 - 30;

        return new int[]{startX + col * (CARD_WIDTH + CARD_GAP),
                startY + row * (CARD_HEIGHT + CARD_GAP)};
    }

    private void updateLayout() {
        int availableWidth = Math.max(1, this.width - GUI_OUTER_MARGIN * 2);
        int availableHeight = Math.max(1, this.height - GUI_OUTER_MARGIN * 2);

        guiWidth = Math.min(availableWidth, GUI_MAX_WIDTH);
        guiHeight = Math.min(availableHeight, GUI_MAX_HEIGHT);

        guiX = (this.width - guiWidth) / 2;
        guiY = (this.height - guiHeight) / 2;

        int contentY = guiY + 38;
        int contentHeight = Math.max(40, guiHeight - 50);
        int usableWidth = Math.max(1, guiWidth - PANEL_GAP * 2);

        leftPanelWidth = usableWidth * 18 / 100;
        centerPanelWidth = usableWidth * 23 / 100;
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
    }

    private void drawPanel(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        drawRect(x, y, x + width, y + height, PANEL_BORDER_COLOR);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BG_COLOR);

        if (width > 4 && height > 4) {
            drawRect(x + 2, y + 2, x + width - 2, y + 3, PANEL_INNER_BORDER_COLOR);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (FlyingCard fc : new ArrayList<>(flyingCards)) {
            fc.update();
            if (fc.isDone()) flyingCards.remove(fc);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        updateLayout();
        drawRect(0, 0, this.width, this.height, OVERLAY_COLOR);

        drawPanel(leftPanelX, leftPanelY, leftPanelWidth, leftPanelHeight);
        drawPanel(centerPanelX, centerPanelY, centerPanelWidth, centerPanelHeight);
        drawPanel(runePanelX, runePanelY, runePanelWidth, runePanelHeight);

        PlayerStatsHudRenderer.draw(Minecraft.getMinecraft(), this.width);

        for (int i = 0; i < SLOT_COUNT; i++) {
            int[] pos = getSlotPosition(i);
            drawSlot(pos[0], pos[1], i, mouseX, mouseY);
        }

        for (FlyingCard fc : flyingCards) {
            drawCard((int) fc.getX(), (int) fc.getY(), fc.cardIndex, fc.color, false);
        }

        drawCenteredString(this.fontRenderer, "Карточки", this.width / 2, 24, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Покупка: x" + (buyAmount == Integer.MAX_VALUE ? "Все" : buyAmount),
                this.width / 2, this.height - 85, 0xFFFF00);

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawDeckTooltip(mouseX, mouseY);
    }

    private void drawDeckTooltip(int mouseX, int mouseY) {
        for (GuiButton guiButton : this.buttonList) {
            if (!(guiButton instanceof DeckGuiButton)) continue;

            DeckGuiButton deckButton = (DeckGuiButton) guiButton;
            if (mouseX < deckButton.x || mouseX > deckButton.x + deckButton.width
                    || mouseY < deckButton.y || mouseY > deckButton.y + deckButton.height) {
                continue;
            }

            List<String> tooltip = new ArrayList<>();
            tooltip.add("§6" + deckButton.getDeckName());
            if (!deckButton.isActiveDeck()) {
                tooltip.add("§eНажмите, чтобы переключить");
            }
            this.drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
    }

    private void drawSlot(int x, int y, int index, int mouseX, int mouseY) {
        List<CardData> stack = slots[index];

        if (stack.isEmpty()) {
            drawRect(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF222222);
            drawRect(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, 0xFF333333);
            return;
        }

        CardData topCard = stack.get(0);
        for (CardData card : stack) {
            if (card.layer > topCard.layer) topCard = card;
        }

        int borderColor = getLayerColor(topCard.layer);
        drawCard(x, y, topCard.cardIndex, borderColor, true);

        if (stack.size() > 1) {
            String count = "x" + stack.size();
            this.fontRenderer.drawStringWithShadow(count,
                    x + CARD_WIDTH - 16, y + CARD_HEIGHT - 10, 0xFFFF00);
        }

        if (mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
            List<String> tooltip = new ArrayList<>();
            tooltip.add("§6" + CARD_NAMES[topCard.cardIndex]);
            tooltip.add("§7Слой: §e" + getLayerName(topCard.layer));
            tooltip.add("§7В слоте: §e" + stack.size() + " карт");
            tooltip.add("§7Стат: §a+" + CARD_STATS[topCard.cardIndex]);
            this.drawHoveringText(tooltip, mouseX, mouseY);
        }
    }

    private void drawCard(int x, int y, int cardIndex, int color, boolean full) {
        drawRect(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, CARD_BG_COLOR);

        drawRect(x, y, x + CARD_WIDTH, y + 2, color);
        drawRect(x, y + CARD_HEIGHT - 2, x + CARD_WIDTH, y + CARD_HEIGHT, color);
        drawRect(x, y, x + 2, y + CARD_HEIGHT, color);
        drawRect(x + CARD_WIDTH - 2, y, x + CARD_WIDTH, y + CARD_HEIGHT, color);

        if (!full) return;

        drawRect(x + 2, y + 2, x + CARD_WIDTH - 2, y + 12, 0xFFAA8844);
        this.fontRenderer.drawStringWithShadow("★89", x + 4, y + 3, 0xFFFFFF);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        renderItem.renderItemIntoGUI(CARD_ICONS[cardIndex], x + CARD_WIDTH / 2 - 8, y + 14);
        GlStateManager.popMatrix();

        String name = CARD_NAMES[cardIndex];
        if (this.fontRenderer.getStringWidth(name) > CARD_WIDTH - 4) {
            name = this.fontRenderer.trimStringToWidth(name, CARD_WIDTH - 6);
        }
        this.fontRenderer.drawStringWithShadow(name,
                x + (CARD_WIDTH - this.fontRenderer.getStringWidth(name)) / 2,
                y + 33, 0xFFFFFF);

        String stat = "+" + CARD_STATS[cardIndex];
        this.fontRenderer.drawStringWithShadow(stat,
                x + (CARD_WIDTH - this.fontRenderer.getStringWidth(stat)) / 2,
                y + 43, 0x55FF55);
    }

    private int getLayerColor(int layer) {
        switch (layer) {
            case 1: return BRONZE_COLOR;
            case 2: return SILVER_COLOR;
            case 3: return GOLD_COLOR;
            default: return BRONZE_COLOR;
        }
    }

    private String getLayerName(int layer) {
        switch (layer) {
            case 1: return "Бронза";
            case 2: return "Серебро";
            case 3: return "Золото";
            default: return "?";
        }
    }

    public void setBalance(int balance) {
        ClientPlayerStats.setCoins(balance);
    }

    public void setDecks(List<String> names, int activeDeck) {
        this.deckNames.clear();
        this.deckNames.addAll(names);
        this.activeDeck = activeDeck;
        rebuildDeckButtons();

        CustomGuiMod.logger.info("Decks updated on client: " + deckNames.size()
                + ", active=" + activeDeck);
    }

    public void addCardFromServer(int slot, int cardIndex, int layer, boolean animate) {
        if (slot < 0 || slot >= SLOT_COUNT) return;

        if (slots[slot] == null) {
            slots[slot] = new ArrayList<>();
        }

        CardData card = new CardData(cardIndex, layer);
        slots[slot].add(card);

        if (animate) {
            int[] pos = getSlotPosition(slot);
            flyingCards.add(new FlyingCard(
                    this.width / 2.0, this.height - 50,
                    pos[0] + CARD_WIDTH / 2.0,
                    pos[1] + CARD_HEIGHT / 2.0,
                    cardIndex, getLayerColor(layer)
            ));
        }

        CustomGuiMod.logger.info("Добавлена карточка от сервера: слот " + slot + ", индекс " + cardIndex
                + " (анимация: " + animate + ")");
    }

    public void clearCards() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] == null) {
                slots[i] = new ArrayList<>();
            } else {
                slots[i].clear();
            }
        }
        flyingCards.clear();
        CustomGuiMod.logger.info("Карточки очищены на клиенте");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
