package com.example.customguimod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class MobManager {

    private static final List<MobSpawn> spawns = new ArrayList<>();
    private static World serverWorld;

    public static class MobSpawn {
        public String name;
        public String type;
        public int hp;
        public int money;
        public double x, y, z;
        public EntityLiving entity;
        public int respawnTimer;

        public MobSpawn(String name, String type, int hp, int money, double x, double y, double z) {
            this.name = name;
            this.type = type;
            this.hp = hp;
            this.money = money;
            this.x = x;
            this.y = y;
            this.z = z;
            this.entity = null;
            this.respawnTimer = 0;
        }
    }

    public static void loadFromDatabase(World world) {
    serverWorld = world;

    // Удаляем старые кастомные мобы, которые Minecraft сохранил в мире
    for (Entity entity : new ArrayList<>(world.loadedEntityList)) {
        if (!(entity instanceof EntityZombie) && !(entity instanceof EntitySkeleton)) {
            continue;
        }

        if (!entity.hasCustomName()) {
            continue;
        }

        String rawName = entity.getCustomNameTag();

        if (rawName.startsWith("§c") && rawName.contains("|money|")) {
            entity.setDead();
            CustomGuiMod.logger.info("Removed old persisted custom mob: " + rawName);
        }
    }

    spawns.clear();

    List<MongoManager.CustomMob> mobs = MongoManager.getAllMobs();
    CustomGuiMod.logger.info("Loaded mobs from DB: " + mobs.size());

    for (MongoManager.CustomMob mob : mobs) {
        if (!mob.enabled) continue;

        MobSpawn spawn = new MobSpawn(
                mob.name,
                mob.type,
                mob.hp,
                mob.money,
                mob.x,
                mob.y,
                mob.z
        );

        spawns.add(spawn);
        spawnMob(spawn);
    }
}

    public static void respawnAll() {
        for (MobSpawn spawn : spawns) {
            if (spawn.entity != null && !spawn.entity.isDead) {
                spawn.entity.setDead();
            }
        }
        spawns.clear();
        if (serverWorld != null) {
            loadFromDatabase(serverWorld);
        }
    }

    private static void spawnMob(MobSpawn spawn) {
        EntityLiving entity;

        if (spawn.type.equalsIgnoreCase("skeleton")) {
            entity = new EntitySkeleton(serverWorld);
        } else {
            entity = new EntityZombie(serverWorld);
        }

        entity.setPosition(spawn.x, spawn.y, spawn.z);
        entity.setCustomNameTag("§c" + spawn.name + "|" + spawn.hp + "|" + spawn.hp + "|money|" + spawn.money);
        entity.setAlwaysRenderNameTag(true);
        entity.setFire(0);
        entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(spawn.hp);
        entity.setHealth(spawn.hp);
        entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        entity.setNoAI(true);

        serverWorld.spawnEntity(entity);
        spawn.entity = entity;
        CustomGuiMod.logger.info("Spawned mob: " + spawn.name + " (" + spawn.type + ") at " + spawn.x + ", " + spawn.y + ", " + spawn.z);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving)) return;

        for (MobSpawn spawn : spawns) {
            if (spawn.entity == event.getEntityLiving()) {
                net.minecraft.util.DamageSource source = event.getSource();
                if (source.getTrueSource() instanceof EntityPlayerMP) {
                    return;
                }
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving)) return;

        for (MobSpawn spawn : spawns) {
            if (spawn.entity == event.getEntityLiving()) {
                if (event.getSource().getTrueSource() instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();
                    int balance = MongoManager.getBalance(player.getUniqueID());
                    MongoManager.setBalance(player.getUniqueID(), balance + spawn.money);
                    player.sendMessage(new TextComponentString("§6+" + spawn.money + " монет! Баланс: " + (balance + spawn.money)));
                    NetworkHandler.INSTANCE.sendTo(new PongPacket(balance + spawn.money), player);
                }

                spawn.entity.setDead();
                spawn.entity = null;
                spawn.respawnTimer = 10;
                CustomGuiMod.logger.info("Mob " + spawn.name + " killed. Respawn in 0.5s.");
                break;
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (MobSpawn spawn : spawns) {
            if (spawn.entity != null && !spawn.entity.isDead) {
                if (spawn.entity.isBurning()) {
                    spawn.entity.extinguish();
                }
                continue;
            }

            if (spawn.respawnTimer > 0) {
                spawn.respawnTimer--;
            } else {
                spawnMob(spawn);
            }
        }
    }

    @SubscribeEvent
public void onMobDrops(LivingDropsEvent event) {
    if (!(event.getEntityLiving() instanceof EntityZombie)
            && !(event.getEntityLiving() instanceof EntitySkeleton)) {
        return;
    }

    EntityLiving entity = (EntityLiving) event.getEntityLiving();

    if (!entity.hasCustomName()) return;

    String rawName = entity.getCustomNameTag();

    if (!rawName.startsWith("§c")) return;
    if (!rawName.contains("|money|")) return;

    event.getDrops().clear();
    event.setCanceled(true);
}
}