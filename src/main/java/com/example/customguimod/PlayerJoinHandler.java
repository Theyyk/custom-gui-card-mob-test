package com.example.customguimod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerJoinHandler {

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        java.util.UUID uuid = player.getUniqueID();

        // Создаём основную колоду, если её нет
        if (MongoManager.getDeckNames(uuid).isEmpty()) {
            MongoManager.addDeck(uuid, "Основная");
            CustomGuiMod.logger.info("Created default deck for " + player.getName());
        }

        PlayerStatsService.sendTo(player);
        CustomGuiMod.logger.info("Player joined: " + player.getName());
    }
}
