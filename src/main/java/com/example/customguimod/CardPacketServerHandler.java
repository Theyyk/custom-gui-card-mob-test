package com.example.customguimod;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CardPacketServerHandler implements IMessageHandler<CardPacket, IMessage> {
    @Override
    public IMessage onMessage(CardPacket message, MessageContext ctx) {
        return null;
    }
}