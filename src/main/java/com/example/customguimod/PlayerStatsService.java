package com.example.customguimod;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.UUID;

public final class PlayerStatsService {

    private PlayerStatsService() {}

    public static int getTotalDamageBonus(UUID uuid) {
        int totalCards = 0;
        int deckCount = MongoManager.getDeckNames(uuid).size();

        for (int deckIndex = 0; deckIndex < deckCount; deckIndex++) {
            totalCards += MongoManager.getCardsInDeck(uuid, deckIndex).size();
        }

        return totalCards;
    }

    public static void sendTo(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        int coins = MongoManager.getBalance(uuid);
        int crystals = MongoManager.getCrystals(uuid);
        int totalDamage = getTotalDamageBonus(uuid);

        NetworkHandler.INSTANCE.sendTo(
                new PlayerStatsPacket(coins, crystals, totalDamage),
                player
        );
    }
}
