package com.example.customguimod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerJoinHandler {

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof net.minecraft.entity.player.EntityPlayerMP)) return;
        java.util.UUID uuid = event.player.getUniqueID();

        // Создаём основную колоду, если её нет
        if (MongoManager.getDeckNames(uuid).isEmpty()) {
            MongoManager.addDeck(uuid, "Основная");
            CustomGuiMod.logger.info("Created default deck for " + event.player.getName());
        }

        CustomGuiMod.logger.info("Player joined: " + event.player.getName());
    }
}