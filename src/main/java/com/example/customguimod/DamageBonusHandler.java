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
        int totalDamageBonus = PlayerStatsService.getTotalDamageBonus(player.getUniqueID());

        if (totalDamageBonus <= 0) return;

        event.setAmount(event.getAmount() + totalDamageBonus);
    }
}
