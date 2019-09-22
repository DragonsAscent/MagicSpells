package com.dragonsascent.magicspells.guilds.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;
import com.dragonsascent.magicspells.guilds.types.Guild;
import com.dragonsascent.magicspells.guilds.command.GuildSubCommand;

public class CreateGuildSubCommand implements GuildSubCommand {

	private MagicSpellsGuilds plugin;
	
	public CreateGuildSubCommand(MagicSpellsGuilds plugin) {
		this.plugin = plugin;
	}
	// teams.<teamname> is the section to make
	
	@Override
	public boolean process(CommandSender sender, String[] args) {
		// magicspellsteams create <name>
		if (args.length >= 2) {
			String name = args[1];
			if (plugin.getGuildByName(name) != null) {
				sender.sendMessage('\'' + name + "' is already a team!");
				return true;
			}
			sender.sendMessage("Creating team '" + name + "' with membership permission 'magicspells.team." + name + '\'');
			if (makeGuild(name)) sender.sendMessage("Team successfully created! Use /cast reload to apply the changes");
			return true;
		}
		return false;
	}
	
	private boolean makeGuild(String name) {
		ConfigurationSection newGuildSec = plugin.getConfig().createSection("guilds." + name);
		newGuildSec.set("permission", "magicspells.guilds." + name);
		newGuildSec.set("friendly-fire", false);
		plugin.saveConfig();
		return true;
	}

}
