package com.nisovin.magicspells.spells.targeted;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class MountSpell extends TargetedSpell implements TargetedEntitySpell {

	private int duration;

	private boolean reverse;

	public MountSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		duration = getConfigInt("duration", 0);

		reverse = getConfigBoolean("reverse", false);

		if (duration < 0) duration = 0;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(player, power);
			if (targetInfo == null) return noTarget(player);
			LivingEntity target = targetInfo.getTarget();
			if (target == null) return noTarget(player);
			mount(player, target);
		}

		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		mount(caster, target);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return false;
	}

	private void mount(Player pl, LivingEntity target) {
		if (pl == null || target == null) return;

		if (reverse) {
			if (!pl.getPassengers().isEmpty()) pl.eject();
			if (pl.getVehicle() != null) pl.getVehicle().eject();
			if (target.getVehicle() != null) target.getVehicle().eject();

			pl.addPassenger(target);
			if (duration > 0) {
				LivingEntity finalTarget = target;
				MagicSpells.scheduleDelayedTask(() -> {
					pl.removePassenger(finalTarget);
				}, duration);
			}
			sendMessages(pl, target);
			return;
		}

		if (pl.getVehicle() != null) {
			Entity veh = pl.getVehicle();
			veh.eject();
			List<Entity> passengers = pl.getPassengers();
			if (passengers.isEmpty()) return;

			pl.eject();
			for (Entity e : passengers) {
				veh.addPassenger(e);
				if (duration > 0) {
					MagicSpells.scheduleDelayedTask(() -> {
						veh.removePassenger(e);
					}, duration);
				}
			}
			return;
		}

		for (Entity e : target.getPassengers()) {
			if (!(e instanceof LivingEntity)) continue;
			target = (LivingEntity) e;
			break;
		}

		pl.eject();
		target.addPassenger(pl);
		if (duration > 0) {
			LivingEntity finalTarget1 = target;
			MagicSpells.scheduleDelayedTask(() -> {
				finalTarget1.removePassenger(pl);
			}, duration);
		}
		sendMessages(pl, target);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Entity vehicle = player.getVehicle();
		List<Entity> passengers = player.getPassengers();
		if (!passengers.isEmpty()) player.eject();
		if (vehicle instanceof Player) vehicle.eject();
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Entity vehicle = player.getVehicle();
		List<Entity> passengers = player.getPassengers();
		if (!passengers.isEmpty()) player.eject();
		if (vehicle instanceof Player) vehicle.eject();
	}

}