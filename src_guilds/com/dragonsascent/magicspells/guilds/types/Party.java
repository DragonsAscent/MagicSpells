package com.dragonsascent.magicspells.guilds.types;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;

public class Party implements MagicSpellsGuilds {

	private String name;
	private boolean friendlyFire;
	
	private List<String> canTargetNames;
	private List<String> cantTargetNames;

	public Party(ConfigurationSection config, String name) {
		this.name = name;
		this.friendlyFire = config.getBoolean("friendly-fire", false);
		this.canTargetNames = config.getStringList("can-target");
		this.cantTargetNames = config.getStringList("cant-target");
	}
	
	public void initialize(MagicSpellsGuilds plugin) {
		if (this.canTargetNames != null && !this.canTargetNames.isEmpty()) {
			this.canTargetGuilds = new ArrayList<>();
			for (String name : this.canTargetNames) {
				Party party = plugin.getParty(uuid);
				if (party != null) {
					this.canTargetGuilds.add(guild);
				} else {
					MagicSpells.error("Invalid guild defined in can-target list");
				}
			}
			if (this.canTargetGuilds.isEmpty()) {
				this.canTargetGuilds = null;
			}
		}
		this.canTargetNames = null;
		if (this.cantTargetNames != null && !this.cantTargetNames.isEmpty()) {
			this.cantTargetGuilds = new ArrayList<>();
			for (String name : this.cantTargetNames) {
				Guild guild = plugin.getGuildByName(name);
				if (guild != null) {
					this.cantTargetGuilds.add(guild);
				} else {
					MagicSpells.error("Invalid guild defined in cant-target list");
				}
			}
			if (this.cantTargetGuilds.isEmpty()) {
				this.cantTargetGuilds = null;
			}
		}
		this.cantTargetNames = null;
	}

	public boolean inParty(Player player) {
		return player.hasPermission(this.permissionNode);
	}

	public boolean isPartyOwner(Player player) {
		return player.hasPermission(this.permissionNode);
	}
	
	public boolean allowFriendlyFire() {
		return this.friendlyFire;
	}
	
	public boolean canTarget(Party party) {
		if (this.canTargetGuilds != null && !this.canTargetGuilds.contains(party)) return false;
		if (this.cantTargetGuilds != null && this.cantTargetGuilds.contains(party)) return false;
		return true;
	}
	
	public String getOwner() {
		return this.partyOwner;
	}
	
	public Set<String> getCanTarget() {
		return new TreeSet<>(this.canTargetNames);
	}
	
	public Set<String> getCantTarget() {
		return new TreeSet<>(this.cantTargetNames);
	}

	public void newParty(Player sender, UUID uuid) {
		this.parties = UUID.randomUUID();
		this.partyOwners = sender;

	}
}
