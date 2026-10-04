package com.example.customguimod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CardPacket implements IMessage {

    private int slot;
    private int cardIndex;
    private int layer;
    private boolean animate;

    public CardPacket() {}

    public CardPacket(int slot, int cardIndex, int layer, boolean animate) {
        this.slot = slot;
        this.cardIndex = cardIndex;
        this.layer = layer;
        this.animate = animate;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
        cardIndex = buf.readInt();
        layer = buf.readInt();
        animate = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        buf.writeInt(cardIndex);
        buf.writeInt(layer);
        buf.writeBoolean(animate);
    }

    public int getSlot() { return slot; }
    public int getCardIndex() { return cardIndex; }
    public int getLayer() { return layer; }
    public boolean isAnimate() { return animate; }
}