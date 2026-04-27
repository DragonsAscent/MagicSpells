package com.nisovin.magicspells.spells.buff;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.events.SpellPreImpactEvent;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;

public class ProxySpell extends BuffSpell implements Listener {

    private final Set<UUID> redirecting = new HashSet<>();
	private final Map<UUID, SpellData> proxies = new HashMap<>();

	public ProxySpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}
	}

	@Override
	public boolean castBuff(SpellData data) {
		proxies.put(data.target().getUniqueId(), data);
		return true;
	}

	@Override
	public boolean recastBuff(SpellData data) {
		stopEffects(data.target());
		return castBuff(data);
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return proxies.containsKey(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		proxies.remove(entity.getUniqueId());
	}

	@Override
	protected @NotNull Collection<UUID> getActiveEntities() {
		return proxies.keySet();
	}

	@EventHandler(ignoreCancelled = true)
	public void onSpellTarget(SpellTargetEvent event) {
		LivingEntity target = event.getTarget();
		if (target == null || !target.isValid()) return;

		LivingEntity proxyTarget = getProxyTarget(target);
		if (proxyTarget == null) return;

		event.setTarget(proxyTarget);
		playRedirectEffects(target, proxyTarget, event.getSpellData());

		addUseAndChargeCost(target);
	}

	@EventHandler(ignoreCancelled = true)
	public void onSpellPreImpact(SpellPreImpactEvent event) {
		LivingEntity target = event.getTarget();
		if (target == null || !target.isValid()) return;

		LivingEntity proxyTarget = getProxyTarget(target);
		if (proxyTarget == null) return;

		SpellData subData = new SpellData(event.getCaster(), proxyTarget, event.getPower());
		playRedirectEffects(target, proxyTarget, subData);

		event.setRedirected(true);
		addUseAndChargeCost(target);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof LivingEntity target)) return;

		LivingEntity proxyTarget = getProxyTarget(target);
		if (proxyTarget == null) return;
		if (!redirecting.add(proxyTarget.getUniqueId())) return;

		SpellData subData = new SpellData(event.getDamager() instanceof LivingEntity damager ? damager : null, proxyTarget);
		playRedirectEffects(target, proxyTarget, subData);

		event.setCancelled(true);
		try {
			proxyTarget.damage(event.getDamage(), event.getDamager());
			addUseAndChargeCost(target);
		} finally {
			redirecting.remove(proxyTarget.getUniqueId());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onLegacyDamage(MagicSpellsEntityDamageByEntityEvent event) {
		onEntityDamage(event);
	}

	private LivingEntity getProxyTarget(LivingEntity target) {
		SpellData proxyData = proxies.get(target.getUniqueId());
		if (proxyData == null) return null;

		LivingEntity proxyTarget = proxyData.caster();
		if (proxyTarget != null && proxyTarget.isValid()) return proxyTarget;

		turnOff(target);
		return null;
	}

	private void playRedirectEffects(LivingEntity target, LivingEntity proxyTarget, SpellData data) {
		playSpellEffects(EffectPosition.TARGET, target, data);
		playSpellEffects(EffectPosition.END_POSITION, proxyTarget, data);
	}

}
