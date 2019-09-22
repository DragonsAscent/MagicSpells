package com.dragonsascent.magicspells.guilds.command;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;
import com.dragonsascent.magicspells.guilds.types.Guild;
import com.dragonsascent.magicspells.guilds.command.GuildSubCommand;

public class ListGuildsSubCommand implements GuildSubCommand {

	private MagicSpellsGuilds plugin;
	
	public ListGuildSubCommand(MagicSpellsGuilds plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean process(CommandSender sender, String[] args) {
		Set<String> names = this.plugin.getGuildNames();
		sender.sendMessage("Guild Names");
		for (String name: names) {
			sender.sendMessage(name);
		}
		return true;
	}

}
