package com.dragonsascent.magicspells.guilds.types;

import java.util.Set;
import java.util.List;
import java.util.TreeSet;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;

import com.dragonsascent.magicspells.guilds.MagicSpellsGuilds;

public class Guild {

	private String name;
	private String permissionNode;
	private boolean friendlyFire;
	
	private List<String> canTargetNames;
	private List<Guild> canTargetGuilds;
	private List<String> cantTargetNames;
	private List<Guild> cantTargetGuilds;
	
	public Guild(ConfigurationSection config, String name) {
		this.name = name;
		this.permissionNode = config.getString("permission", "magicspells.guild." + name);
		this.friendlyFire = config.getBoolean("friendly-fire", false);
		this.canTargetNames = config.getStringList("can-target");
		this.cantTargetNames = config.getStringList("cant-target");
	}
	
	public void initialize(MagicSpellsGuilds plugin) {
		if (this.canTargetNames != null && !this.canTargetNames.isEmpty()) {
			this.canTargetGuilds = new ArrayList<>();
			for (String name : this.canTargetNames) {
				Guild guild = plugin.getGuildByName(name);
				if (guild != null) {
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
	
	public boolean inGuild(Player player) {
		return player.hasPermission(this.permissionNode);
	}
	
	public boolean allowFriendlyFire() {
		return this.friendlyFire;
	}
	
	public boolean canTarget(Guild guild) {
		if (this.canTargetGuilds != null && !this.canTargetGuilds.contains(guild)) return false;
		if (this.cantTargetGuilds != null && this.cantTargetGuilds.contains(guild)) return false;
		return true;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPermission() {
		return this.permissionNode;
	}
	
	public Set<String> getCanTarget() {
		return new TreeSet<>(this.canTargetNames);
	}
	
	public Set<String> getCantTarget() {
		return new TreeSet<>(this.cantTargetNames);
	}
	
}
