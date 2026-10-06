package com.example.customguimod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PlayerStatsPacket implements IMessage {

    private int coins;
    private int crystals;
    private int totalDamage;

    public PlayerStatsPacket() {}

    public PlayerStatsPacket(int coins, int crystals, int totalDamage) {
        this.coins = coins;
        this.crystals = crystals;
        this.totalDamage = totalDamage;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        coins = buf.readInt();
        crystals = buf.readInt();
        totalDamage = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(coins);
        buf.writeInt(crystals);
        buf.writeInt(totalDamage);
    }

    public int getCoins() {
        return coins;
    }

    public int getCrystals() {
        return crystals;
    }

    public int getTotalDamage() {
        return totalDamage;
    }
}
