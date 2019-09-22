package com.dragonsascent.magicspells.guilds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.nisovin.magicspells.util.compat.EventUtil;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;

import com.dragonsascent.magicspells.guilds.types.Guild;
import com.dragonsascent.magicspells.guilds.types.Party;
import com.dragonsascent.magicspells.guilds.command.MagicSpellsGuildsCommand;

public class MagicSpellsGuilds extends JavaPlugin implements Listener {

	private boolean useCache;
	private boolean clearCacheOnDeath;

	private List<Party> parties;
	private List<Party> partyOwners;

	private List<Guild> guilds;
	private Map<String, Guild> guildNames;
	private Map<String, Guild> playerGuilds;

    @Override
	public void onEnable() {
		// Setup containers
		this.partyOwners = new ArrayList<>();
		this.parties = new ArrayList<>();
		this.guilds = new ArrayList<>();
		this.guildNames = new HashMap<>();
		this.playerGuilds = new HashMap<>();
		
		// Get config
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) saveDefaultConfig();
		reloadConfig();
		Configuration config = getConfig();
		
		// Get config
		this.useCache = config.getBoolean("use-cache", true);
		this.clearCacheOnDeath = config.getBoolean("clear-cache-on-death", false);
		
		// Setup guilds
		MagicSpells.debug(1, "Loading guilds...");
		Set<String> guildKeys = config.getConfigurationSection("guilds").getKeys(false);
		for (String name : guildKeys) {
			Guild guild = new Guild(config.getConfigurationSection("guilds." + name), name);
			this.guilds.add(guild);
			this.guildNames.put(name, guild);
			MagicSpells.debug(2, "    Guild " + name + " loaded");
		}
		for (Guild guild : this.guilds) {
			guild.initialize(this);
		}
		getCommand("magicspellsguilds").setExecutor(new MagicSpellsGuildsCommand(this));
		
		// Register events
		EventUtil.register(this, this);
	}
	
	@Override
	public void onDisable() {
		this.partyOwners = null;
		this.parties = null;
		this.guilds = null;
		this.guildNames = null;
		this.playerGuilds = null;
		getCommand("magicspellsguilds").setExecutor(null);
		HandlerList.unregisterAll((Plugin)this);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		Player caster = event.getCaster();
		if (caster == null) return;
		if (!(event.getTarget() instanceof Player)) return;
		
		boolean beneficial = event.getSpell().isBeneficial();
		if (!canTarget(caster, (Player)event.getTarget())) {
			if (!beneficial) event.setCancelled(true);
		} else {
			if (beneficial) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onMagicSpellsLoad(MagicSpellsLoadedEvent event) {
		onDisable();
		onEnable();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (this.useCache) {
			this.playerGuilds.remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (this.useCache) {
			this.parties.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (this.useCache && this.clearCacheOnDeath) {
			this.playerGuilds.remove(event.getEntity().getName());
		}
	}
	
	public Guild getGuild(Player player) {
		String playerName = player.getName();
		if (this.useCache) {
			Guild guild = this.playerGuilds.get(playerName);
			if (guild != null) return guild;
		}
		for (Guild guild : this.guilds) {
			if (guild.inGuild(player)) {
				if (this.useCache) this.playerGuilds.put(playerName, guild);
				return guild;
			}
		}
		return null;
	}
	
	public boolean canTarget(Player caster, Player target) {
		Guild casterGuild = getGuild(caster);
		Guild targetGuild = getGuild(target);
		
		// Allow targeting if one of the players is not in a team
		if (casterGuild == null || targetGuild == null) return true;
		
		// If same team, check friendly fire
		if (casterGuild == targetGuild) return casterGuild.allowFriendlyFire();
		
		// Otherwise check if can target
		return casterGuild.canTarget(targetGuild);
	}

	public Guild getGuildByName(String name) {
		return this.guildNames.get(name);
	}
	
	public Set<String> getGuildNames() {
		return new TreeSet<>(this.guildNames.keySet());
	}
	
}
