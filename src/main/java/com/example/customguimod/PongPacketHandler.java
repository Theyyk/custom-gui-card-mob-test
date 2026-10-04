package com.example.customguimod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PongPacketHandler implements IMessageHandler<PongPacket, IMessage> {

    @Override
    public IMessage onMessage(PongPacket message, MessageContext ctx) {
        CustomGuiMod.logger.info("PongPacket получен! Баланс: " + message.getBalance());

        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().currentScreen instanceof CardsGuiScreen) {
                ((CardsGuiScreen) Minecraft.getMinecraft().currentScreen).setBalance(message.getBalance());
                CustomGuiMod.logger.info("Баланс обновлён в GUI: " + message.getBalance());
            } else {
                CustomGuiMod.logger.info("GUI не открыт — баланс не обновлён");
            }
        });
        return null;
    }
}