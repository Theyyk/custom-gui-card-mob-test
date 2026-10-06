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

public class CustomDeckCommand extends CommandBase {

    @Override
    public String getName() {
        return "customdeck";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/customdeck create|switch|list|delete";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("switch");
            completions.add("list");
            completions.add("delete");
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
            player.sendMessage(new TextComponentString("§e/customdeck create <имя>"));
            player.sendMessage(new TextComponentString("§e/customdeck switch <номер>"));
            player.sendMessage(new TextComponentString("§e/customdeck list"));
            player.sendMessage(new TextComponentString("§e/customdeck delete <номер>"));
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
    if (args.length < 2) {
        player.sendMessage(new TextComponentString("§c/customdeck create <имя>"));
        return;
    }
    java.util.List<String> existing = MongoManager.getDeckNames(player.getUniqueID());
    if (existing.contains(args[1])) {
        player.sendMessage(new TextComponentString("§cКолода с таким именем уже существует"));
        return;
    }
    if (existing.size() >= 9) {
        player.sendMessage(new TextComponentString("§cМаксимум 9 колод"));
        return;
    }
    MongoManager.addDeck(player.getUniqueID(), args[1]);
    player.sendMessage(new TextComponentString("§aКолода " + args[1] + " создана"));
} else if (args[0].equalsIgnoreCase("switch")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/customdeck switch <номер>"));
                return;
            }
            try {
                int index = Integer.parseInt(args[1]);
                MongoManager.setActiveDeck(player.getUniqueID(), index);
                player.sendMessage(new TextComponentString("§aАктивная колода: " + index));
            } catch (NumberFormatException e) {
                player.sendMessage(new TextComponentString("§cНомер должен быть числом"));
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            List<String> decks = MongoManager.getDeckNames(player.getUniqueID());
            int active = MongoManager.getActiveDeck(player.getUniqueID());
            player.sendMessage(new TextComponentString("§6Колоды (" + decks.size() + "):"));
            for (int i = 0; i < decks.size(); i++) {
                String marker = (i == active) ? " §a← активная" : "";
                player.sendMessage(new TextComponentString("§7[" + i + "] " + decks.get(i) + marker));
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage(new TextComponentString("§c/customdeck delete <номер>"));
                return;
            }
            try {
                int index = Integer.parseInt(args[1]);
                MongoManager.deleteDeck(player.getUniqueID(), index);
                player.sendMessage(new TextComponentString("§cКолода " + index + " удалена"));
            } catch (NumberFormatException e) {
                player.sendMessage(new TextComponentString("§cНомер должен быть числом"));
            }
        }
    }
}