package com.example.customguimod;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DeckPacketServerHandler implements IMessageHandler<DeckPacket, IMessage> {
    @Override
    public IMessage onMessage(DeckPacket message, MessageContext ctx) {
        return null;
    }
}
