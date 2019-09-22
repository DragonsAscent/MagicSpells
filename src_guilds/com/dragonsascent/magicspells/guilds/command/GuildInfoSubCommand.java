package com.dragonsascent.magicspells.guilds.command;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;
import com.dragonsascent.magicspells.guilds.types.Guild;
import com.dragonsascent.magicspells.guilds.command.GuildSubCommand;

public class GuildInfoSubCommand implements GuildSubCommand {

	private MagicSpellsGuilds plugin;
	
	public GuildInfoSubCommand(MagicSpellsGuilds plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean process(CommandSender sender, String[] args) {
		if (args.length >= 2) {
			String name = args[1];
			Guild guild = this.plugin.getGuildByName(name);
			if (guild == null) {
				sender.sendMessage("There is no team by that name!");
				return true;
			}
			sendGuildInfo(sender, guild);
			return true;
		}
		return false;
	}
	
	private void sendGuildInfo(CommandSender sender, Guild guild) {
		sender.sendMessage(new String[] {
			"Name: " + guild.getName(),
			"Permission: " + guild.getPermission(),
			"Friendly fire: " + guild.allowFriendlyFire(),
			"Can target: " + Arrays.toString(guild.getCanTarget().toArray()),
			"Can't target: " + Arrays.toString(guild.getCantTarget().toArray())
		});
	}

}
