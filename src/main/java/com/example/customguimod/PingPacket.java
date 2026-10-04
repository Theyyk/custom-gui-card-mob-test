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
        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(PingPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                String action = message.getAction();
                CustomGuiMod.logger.info("Получен пакет от " + player.getName() + ": " + action);

                if (action.equals("buy_card")) {
    int balance = MongoManager.getBalance(player.getUniqueID());
    if (balance >= 100) {
        // Ищем свободные слоты
        java.util.List<Integer> freeSlots = MongoManager.getFreeSlots(player.getUniqueID());
        if (freeSlots.isEmpty()) {
            player.sendMessage(new TextComponentString("§cВсе слоты заняты!"));
            NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
            return;
        }

        MongoManager.setBalance(player.getUniqueID(), balance - 100);

        int slot = freeSlots.get(new java.util.Random().nextInt(freeSlots.size()));
        int cardIndex = new java.util.Random().nextInt(10);
        int layer = 1;

        MongoManager.addCard(player.getUniqueID(), slot, cardIndex, layer);
        NetworkHandler.INSTANCE.sendTo(new CardPacket(slot, cardIndex, layer, true), player);

        player.sendMessage(new TextComponentString(
                "§aКуплена карточка за 100 монет. Остаток: " + (balance - 100)));
        NetworkHandler.INSTANCE.sendTo(new PongPacket(balance - 100), player);
    } else {
        player.sendMessage(new TextComponentString(
                "§cНедостаточно монет! Нужно 100, у тебя " + balance));
        NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
    }
} else if (action.equals("spawn_mob")) {
                    // Устаревшая команда — теперь мобы через /custommob
                    player.sendMessage(new TextComponentString("§eИспользуй /custommob create"));
                } else if (action.equals("get_balance")) {
                    int balance = MongoManager.getBalance(player.getUniqueID());
                    player.sendMessage(new TextComponentString(
                            "§6Твой баланс: " + balance + " монет"));
                    NetworkHandler.INSTANCE.sendTo(new PongPacket(balance), player);
                } else if (action.equals("load_cards")) {
    java.util.List<MongoManager.SavedCard> cards = MongoManager.getCards(player.getUniqueID());
    CustomGuiMod.logger.info("Отправка карточек клиенту: " + cards.size());
    for (MongoManager.SavedCard card : cards) {
        NetworkHandler.INSTANCE.sendTo(new CardPacket(card.slot, card.cardIndex, card.layer, false), player); // animate = false
    }
}
            });
            return null;
        }
    }
}