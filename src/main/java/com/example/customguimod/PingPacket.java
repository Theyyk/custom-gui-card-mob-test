package com.example.customguimod;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PingPacket implements IMessage {

    private String action;

    public PingPacket() {}

    public PingPacket(String action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, action);
    }

    public String getAction() {
        return action;
    }

    public static class Handler implements IMessageHandler<PingPacket, IMessage> {
        private static final int CARD_COST = 100;

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PingPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                String action = message.getAction();
                CustomGuiMod.logger.info("Packet from " + player.getName() + ": " + action);

                List<String> deckNames = MongoManager.getDeckNames(player.getUniqueID());
                int deckIndex = MongoManager.getActiveDeck(player.getUniqueID());

                if (!deckNames.isEmpty() && (deckIndex < 0 || deckIndex >= deckNames.size())) {
                    deckIndex = 0;
                    MongoManager.setActiveDeck(player.getUniqueID(), deckIndex);
                }

                if (action.equals("buy_card")) {
                    buyCards(player, deckIndex, 1, false);
                } else if (action.startsWith("buy_cards:")) {
                    String rawAmount = action.substring("buy_cards:".length());

                    if (rawAmount.equalsIgnoreCase("all")) {
                        buyCards(player, deckIndex, Integer.MAX_VALUE, true);
                        return;
                    }

                    try {
                        int requestedAmount = Integer.parseInt(rawAmount);
                        if (requestedAmount != 1 && requestedAmount != 5
                                && requestedAmount != 10 && requestedAmount != 100) {
                            player.sendMessage(new TextComponentString("§cНекорректное количество карточек."));
                            return;
                        }
                        buyCards(player, deckIndex, requestedAmount, false);
                    } catch (NumberFormatException e) {
                        player.sendMessage(new TextComponentString("§cНекорректное количество карточек."));
                    }
                } else if (action.equals("get_player_stats")) {
                    PlayerStatsService.sendTo(player);
                } else if (action.equals("get_balance")) {
                    int balance = MongoManager.getBalance(player.getUniqueID());
                    player.sendMessage(new TextComponentString(
                            "§6Твой баланс: " + balance + " монет"));
                    NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
                } else if (action.equals("load_cards")) {
                    sendCards(player, deckIndex);
                } else if (action.equals("load_decks")) {
                    sendDeckState(player);
                } else if (action.startsWith("switch_deck:")) {
                    String rawIndex = action.substring("switch_deck:".length());
                    try {
                        int targetDeck = Integer.parseInt(rawIndex);
                        List<String> decks = MongoManager.getDeckNames(player.getUniqueID());

                        if (targetDeck < 0 || targetDeck >= decks.size()) {
                            player.sendMessage(new TextComponentString("§cТакой колоды не существует."));
                            sendDeckState(player);
                            return;
                        }

                        MongoManager.setActiveDeck(player.getUniqueID(), targetDeck);
                        sendDeckState(player);
                        sendCards(player, targetDeck);
                        player.sendMessage(new TextComponentString(
                                "§aАктивная колода: §e" + decks.get(targetDeck)));
                    } catch (NumberFormatException e) {
                        player.sendMessage(new TextComponentString("§cНекорректный номер колоды."));
                    }
                }
            });
            return null;
        }

        private void buyCards(EntityPlayerMP player, int deckIndex, int requestedAmount, boolean buyAll) {
            List<String> decks = MongoManager.getDeckNames(player.getUniqueID());
            if (decks.isEmpty() || deckIndex < 0 || deckIndex >= decks.size()) {
                player.sendMessage(new TextComponentString("§cУ тебя нет доступных колод."));
                return;
            }

            int balance = MongoManager.getBalance(player.getUniqueID());
            List<Integer> freeSlots = MongoManager.getFreeSlots(player.getUniqueID(), deckIndex);

            if (freeSlots.isEmpty()) {
                player.sendMessage(new TextComponentString("§eДостигнут лимит покупки: " + RuneInventory.CAPACITY
                        + " рун в сборке. Ячейки будущих типов пока недоступны."));
                NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
                return;
            }

            int affordable = balance / CARD_COST;
            if (affordable <= 0) {
                player.sendMessage(new TextComponentString(
                        "§cНедостаточно монет! Нужно минимум " + CARD_COST + ", у тебя " + balance));
                NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
                return;
            }

            int maxPossible = Math.min(affordable, freeSlots.size());
            int amountToBuy = buyAll ? maxPossible : Math.min(requestedAmount, maxPossible);

            if (amountToBuy <= 0) {
                NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
                return;
            }

            Collections.shuffle(freeSlots);
            Random random = new Random();

            for (int i = 0; i < amountToBuy; i++) {
                int slot = freeSlots.get(i);
                int cardIndex = random.nextInt(10);
                int layer = 1;
                int level = 1;

                MongoManager.addCardToDeck(
                        player.getUniqueID(), deckIndex, slot, cardIndex, layer, level);
                NetworkHandler.INSTANCE.sendTo(
                        new CardPacket(slot, cardIndex, layer, true), player);
            }

            int spent = amountToBuy * CARD_COST;
            int newBalance = balance - spent;
            MongoManager.setBalance(player.getUniqueID(), newBalance);
            PlayerStatsService.sendTo(player);

            if (!buyAll && amountToBuy < requestedAmount) {
                player.sendMessage(new TextComponentString(
                        "§eКуплено " + amountToBuy + " из " + requestedAmount
                                + " карточек. Потрачено: " + spent
                                + ". Остаток: " + newBalance));
            } else {
                player.sendMessage(new TextComponentString(
                        "§aКуплено карточек: " + amountToBuy
                                + ". Потрачено: " + spent
                                + ". Остаток: " + newBalance));
            }
        }

        private void sendDeckState(EntityPlayerMP player) {
            List<String> decks = MongoManager.getDeckNames(player.getUniqueID());
            int activeDeck = MongoManager.getActiveDeck(player.getUniqueID());
            NetworkHandler.INSTANCE.sendTo(new DeckPacket(decks, activeDeck), player);
        }

        private void sendCards(EntityPlayerMP player, int deckIndex) {
            NetworkHandler.INSTANCE.sendTo(new CardPacket(-1, 0, 0, false), player);

            List<MongoManager.SavedCard> cards = MongoManager.getCardsInDeck(player.getUniqueID(), deckIndex);
            CustomGuiMod.logger.info("Sending cards to client from deck " + deckIndex + ": " + cards.size());

            for (MongoManager.SavedCard card : cards) {
                NetworkHandler.INSTANCE.sendTo(
                        new CardPacket(card.slot, card.cardIndex, card.layer, false), player);
            }
        }
    }
}
