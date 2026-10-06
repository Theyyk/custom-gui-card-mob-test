package com.example.customguimod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageBonusHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLivingBase)) return;

        if (!(event.getSource().getTrueSource() instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();

        int totalCards = 0;

int deckCount = MongoManager.getDeckNames(player.getUniqueID()).size();

for (int deckIndex = 0; deckIndex < deckCount; deckIndex++) {
    totalCards += MongoManager.getCardsInDeck(
            player.getUniqueID(),
            deckIndex
    ).size();
}

if (totalCards <= 0) return;

float newDamage = event.getAmount() + totalCards;
event.setAmount(newDamage);
    }
}