package com.dragonsascent.magicspells.guilds.command;

import org.bukkit.command.CommandSender;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;

@FunctionalInterface
public interface GuildSubCommand {

	boolean process(CommandSender sender, String[] args);
	
}
