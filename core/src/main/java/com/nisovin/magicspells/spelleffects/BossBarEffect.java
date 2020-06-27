package com.nisovin.magicspells.spelleffects;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.util.managers.BossBarManager.Bar;

public class BossBarEffect extends SpellEffect {

	private String namespace;
	private String title;
	private String color;
	private String style;

	private String strVar;
	private String strVarMax;
	private Variable variable;
	private Variable variableMax;
	private double maxValue;

	private BarColor barColor;
	private BarStyle barStyle;

	private int duration;
	private double progress;

	private boolean broadcast;

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		namespace = config.getString("namespace");
		title = Util.colorize(config.getString("title", ""));
		color = config.getString("color", "red");
		style = config.getString("style", "solid");
		strVar = config.getString("variable", "");
		strVarMax = config.getString("variable-max", "");
		maxValue = config.getDouble("max-value", 100);

		variable = MagicSpells.getVariableManager().getVariable(strVar);
		variableMax = MagicSpells.getVariableManager().getVariable(strVarMax);
		if (variable == null && !strVar.isEmpty()) {
			MagicSpells.error("Wrong variable defined! '" + strVar + "'");
		}

		if (variableMax == null && !strVarMax.isEmpty()) {
			MagicSpells.error("Wrong variable defined! '" + strVarMax + "'");
		}

		if (namespace != null && !MagicSpells.getBossBarManager().isNameSpace(namespace)) {
			MagicSpells.error("Wrong namespace defined! '" + namespace + "'");
		}

		try {
			barColor = BarColor.valueOf(color.toUpperCase());
		}
		catch (IllegalArgumentException ignored) {
			barColor = BarColor.WHITE;
			MagicSpells.error("Wrong bar color defined! '" + color + "'");
		}

		try {
			barStyle = BarStyle.valueOf(style.toUpperCase());
		}
		catch (IllegalArgumentException ignored) {
			barStyle = BarStyle.SOLID;
			MagicSpells.error("Wrong bar style defined! '" + style + "'");
		}

		duration = config.getInt("duration", 60);
		progress = config.getDouble("progress", 1);
		if (progress > 1) progress = 1;
		if (progress < 0) progress = 0;

		broadcast = config.getBoolean("broadcast", false);
	}

	@Override
	protected Runnable playEffectEntity(Entity entity) {
		if (barStyle == null || barColor == null) return null;
		if (broadcast) Util.forEachPlayerOnline(this::createBar);
		else if (entity instanceof Player) createBar((Player) entity);
		return null;
	}

	private void createBar(Player player) {
		Bar bar = MagicSpells.getBossBarManager().getBar(player, namespace);
		if (variableMax == null) {
			if (variable == null) {
				bar.set(title, progress, barStyle, barColor);
			} else {
				double diff = variable.getValue(player) / maxValue;
				if (diff > 0 && diff < 1) bar.set(title, diff, barStyle, barColor);
			}
			if (duration > 0) MagicSpells.scheduleDelayedTask(bar::remove, duration);
		} else {
			if (variable == null) {
				double diff = progress / variableMax.getValue(player);
				if (diff > 0 && diff < 1) bar.set(title, diff, barStyle, barColor);
			} else {
				double diff = variable.getValue(player) / variableMax.getValue(player);
				if (variableMax.getValue(player) <= variable.getValue(player)) {
					bar.set(title, 1, barStyle, barColor);
				}
				if (variableMax.getValue(player) > variable.getValue(player)) {
					if (diff > 0 && diff < 1) bar.set(title, diff, barStyle, barColor);
				}
			}
			if (duration > 0) MagicSpells.scheduleDelayedTask(bar::remove, duration);
		}
	}
}
