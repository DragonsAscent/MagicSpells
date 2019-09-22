package com.dragonsascent.magicspells.guilds.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;

public class MagicSpellsGuildsCommand implements CommandExecutor {

	private Map<String, GuildSubCommand> subCommands;
	
	private static void registerSubCommand(Map<String, GuildSubCommand> cmdMap, GuildSubCommand subCommand, String... labels) {
		for (String label: labels) {
			cmdMap.put(label.toLowerCase(), subCommand);
		}
	}
	
	public MagicSpellsGuildsCommand(MagicSpellsGuilds plugin) {
		this.subCommands = new HashMap<>();

		// ----- >> Guilds
		// ms guilds create <name>
		// Creates a team using the default perm structure
		registerSubCommand(this.subCommands, new CreateGuildSubCommand(plugin), "create", "new", "make");
		
		// ms guilds list
		registerSubCommand(this.subCommands, new ListGuildsSubCommand(plugin), "list");
		
		// ms guilds info <name>
		registerSubCommand(this.subCommands, new GuildInfoSubCommand(plugin), "info", "about");

		// ----- >> Parties
		// ms parties create
		registerSubCommand(this.subCommands, new CreatePartySubCommand(plugin), "create");

		// ms parties invite
		registerSubCommand(this.subCommands, new PartyInviteSubCommand(plugin), "invite");

		// ms parties accept
		registerSubCommand(this.subCommands, new PartyAcceptSubCommand(plugin), "accept");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			GuildSubCommand sub = this.subCommands.get(args[0].toLowerCase());
			if (sub != null) return sub.process(sender, args);
		}
		return false;
	}
	
}
