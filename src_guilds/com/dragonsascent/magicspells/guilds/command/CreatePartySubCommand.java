package com.dragonsascent.magicspells.guilds.command;

import org.bukkit.command.CommandSender;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.dragonsascent.magicspells.guilds.types.Party;
import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;

import java.util.UUID;

public class CreatePartySubCommand {

    private MagicSpellsGuilds plugin;

    public CreatePartySubCommand(MagicSpellsGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean process(CommandSender sender, String[] args, Party party) {
        // ms party create
        sender.sendMessage("Party created with owner: '" + sender);
        party.newParty(sender);
        return true;
    }
}