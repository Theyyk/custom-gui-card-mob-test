package com.example.customguimod;

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

    private int balance = 0;
    private static final int CARD_COST = 100;
    private static final int ROWS = 5;
    private static final int COLS = 10;
    private static final int SLOT_COUNT = ROWS * COLS; // 50

    private static final int CARD_WIDTH = 40;
    private static final int CARD_HEIGHT = 50;
    private static final int CARD_GAP = 4;

    private static List<CardData>[] slots = new List[SLOT_COUNT];
    private final Random random = new Random();

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

    @Override
    public void initGui() {
        this.buttonList.clear();

        if (renderItem == null) {
            renderItem = net.minecraft.client.Minecraft.getMinecraft().getRenderItem();
        }

        // Всегда очищаем слоты при открытии GUI
for (int i = 0; i < SLOT_COUNT; i++) {
    slots[i] = new ArrayList<>();
}

        int centerX = this.width / 2;
        int btnY = this.height - 60;

        // Кнопка «Купить карточку»
        this.buttonList.add(new GuiButton(0, centerX - 100, btnY, 200, 20, "Купить карточку"));

        // Кнопки количества
        this.buttonList.add(new GuiButton(1, centerX - 160, btnY + 25, 50, 18, "x1"));
        this.buttonList.add(new GuiButton(2, centerX - 105, btnY + 25, 50, 18, "x5"));
        this.buttonList.add(new GuiButton(3, centerX - 50, btnY + 25, 50, 18, "x10"));
        this.buttonList.add(new GuiButton(4, centerX + 5, btnY + 25, 50, 18, "x100"));
        this.buttonList.add(new GuiButton(5, centerX + 60, btnY + 25, 50, 18, "xВсе"));

        // Кнопка «Спавн моба»
        this.buttonList.add(new GuiButton(6, centerX - 160, btnY - 25, 100, 18, "Спавн моба"));

        // Кнопка «Баланс»
        this.buttonList.add(new GuiButton(7, centerX + 60, btnY - 25, 100, 18, "Баланс"));

        // Запрашиваем баланс и карточки при открытии GUI
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("get_balance"));
        NetworkHandler.INSTANCE.sendToServer(new PingPacket("load_cards"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("buy_card"));
        } else if (button.id == 1) buyAmount = 1;
        else if (button.id == 2) buyAmount = 5;
        else if (button.id == 3) buyAmount = 10;
        else if (button.id == 4) buyAmount = 100;
        else if (button.id == 5) buyAmount = Integer.MAX_VALUE;
        else if (button.id == 6) {
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("spawn_mob"));
        } else if (button.id == 7) {
            NetworkHandler.INSTANCE.sendToServer(new PingPacket("get_balance"));
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

        for (int i = 0; i < SLOT_COUNT; i++) {
            int[] pos = getSlotPosition(i);
            drawSlot(pos[0], pos[1], i, mouseX, mouseY);
        }

        for (FlyingCard fc : flyingCards) {
            drawCard((int) fc.getX(), (int) fc.getY(), fc.cardIndex, fc.color, false);
        }

        drawCenteredString(this.fontRenderer, "Карточки", this.width / 2, 10, 0xFFFFFF);
        drawCenteredString(this.fontRenderer, "Покупка: x" + (buyAmount == Integer.MAX_VALUE ? "Все" : buyAmount),
                this.width / 2, this.height - 85, 0xFFFF00);
        drawCenteredString(this.fontRenderer,
                "§6Монет: §e" + balance + " §7| §6Карточка: §e" + CARD_COST,
                this.width / 2, 25, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
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
        this.balance = balance;
        CustomGuiMod.logger.info("Баланс обновлён: " + balance);
    }

    public void addCardFromServer(int slot, int cardIndex, int layer, boolean animate) {
    if (slot < 0 || slot >= SLOT_COUNT) return;

    if (slots[slot] == null) {
        slots[slot] = new ArrayList<>();
    }

    CardData card = new CardData(cardIndex, layer);
    slots[slot].add(card);

    // Анимация — только если animate = true
    if (animate) {
        int[] pos = getSlotPosition(slot);
        flyingCards.add(new FlyingCard(
                this.width / 2.0, this.height - 50,
                pos[0] + CARD_WIDTH / 2.0,
                pos[1] + CARD_HEIGHT / 2.0,
                cardIndex, getLayerColor(layer)
        ));
    }

    CustomGuiMod.logger.info("Добавлена карточка от сервера: слот " + slot + ", индекс " + cardIndex + " (анимация: " + animate + ")");
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