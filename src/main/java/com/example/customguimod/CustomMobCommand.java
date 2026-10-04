package com.example.customguimod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CustomMobCommand extends CommandBase {

    @Override
    public String getName() {
        return "custommob";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/custommob create|setspawn|unspawn|delete|list";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("setspawn");
            completions.add("unspawn");
            completions.add("delete");
            completions.add("list");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("setspawn")
                || args[0].equalsIgnoreCase("unspawn")
                || args[0].equalsIgnoreCase("delete"))) {
            for (MongoManager.CustomMob mob : MongoManager.getAllMobs()) {
                completions.add(mob.name);
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            completions.add("zombie");
            completions.add("skeleton");
        }

        return completions;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("§cOnly for players"));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length == 0) {
            player.sendMessage(new TextComponentString("§e/custommob create <имя> [деньги] [хп] [тип]"));
            player.sendMessage(new TextComponentString("§e/custommob setspawn <имя>"));
            player.sendMessage(new TextComponentString("§e/custommob unspawn <имя>"));
            player.sendMessage(new TextComponentString("§e/custommob delete <имя>"));
            player.sendMessage(new TextComponentString("§e/custommob list"));
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/custommob create <имя> [деньги] [хп] [тип]"));
                return;
            }
            String name = args[1];

            if (MongoManager.getMob(name) != null) {
                player.sendMessage(new TextComponentString("§cМоб с именем " + name + " уже существует!"));
                return;
            }

            int money = 50;
            int hp = 10;
            String type = "zombie";

            if (args.length >= 3) {
                try {
                    money = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    type = args[2];
                }
            }
            if (args.length >= 4) {
                try {
                    hp = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    type = args[3];
                }
            }
            if (args.length >= 5) {
                type = args[4];
            }

            MongoManager.createMob(name, type, hp, money);
            player.sendMessage(new TextComponentString("§aМоб создан: " + name + " (" + type + ", HP " + hp + ", " + money + " монет)"));
        } else if (args[0].equalsIgnoreCase("setspawn")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/custommob setspawn <имя>"));
                return;
            }
            String name = args[1];
            if (MongoManager.getMob(name) == null) {
                player.sendMessage(new TextComponentString("§cМоб не найден: " + name));
                return;
            }
            MongoManager.setMobSpawn(name, player.posX, player.posY, player.posZ);
            player.sendMessage(new TextComponentString("§aТочка спавна для " + name + " установлена"));
            MobManager.respawnAll();
        } else if (args[0].equalsIgnoreCase("unspawn")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/custommob unspawn <имя>"));
                return;
            }
            MongoManager.unspawnMob(args[1]);
            player.sendMessage(new TextComponentString("§cМоб " + args[1] + " отключён"));
            MobManager.respawnAll();
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/custommob delete <имя>"));
                return;
            }
            if (MongoManager.getMob(args[1]) == null) {
                player.sendMessage(new TextComponentString("§cМоб не найден: " + args[1]));
                return;
            }
            MongoManager.deleteMob(args[1]);
            player.sendMessage(new TextComponentString("§cМоб " + args[1] + " удалён"));
            MobManager.respawnAll();
        } else if (args[0].equalsIgnoreCase("list")) {
            List<MongoManager.CustomMob> mobs = MongoManager.getAllMobs();
            player.sendMessage(new TextComponentString("§6Мобы (" + mobs.size() + "):"));
            for (MongoManager.CustomMob mob : mobs) {
                player.sendMessage(new TextComponentString("§7- " + mob.name + " (" + mob.type + ", HP " + mob.hp + ", " + mob.money + " монет) " + (mob.enabled ? "§aвкл" : "§cвыкл")));
            }
        }
    }
}