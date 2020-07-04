package com.nisovin.magicspells.spells.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.TargetInfo;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class TagEntitySpell extends TargetedSpell implements TargetedEntitySpell {

	private String operation;
	private String tag;
	private String varTag;

	public TagEntitySpell(MagicConfig config, String spellName) {
		super(config, spellName);

		tag = getConfigString("tag", null);
		operation = getConfigString("operation", "add");

	}

	@Override
	public PostCastAction castSpell(LivingEntity livingEntity, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> targetInfo = getTargetedEntity(livingEntity, power);
			if (targetInfo == null) return noTarget(livingEntity);
			LivingEntity target = targetInfo.getTarget();
			if (target == null) return noTarget(livingEntity);
			tag(livingEntity, target);
			playSpellEffects(livingEntity, target);
		}

		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
		tag(caster, target);
		playSpellEffects(caster, target);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		tag(target, target);
		playSpellEffects(EffectPosition.TARGET, target);
		return true;
	}

	private void tag(LivingEntity caster, LivingEntity target) {
		switch (operation) {
			case "add":
			case "insert":
				if (tag.contains("%") && caster instanceof Player) {
					varTag = MagicSpells.doVariableReplacements((Player) caster, tag);
					target.addScoreboardTag(varTag);
				} else target.addScoreboardTag(tag);
				break;
			case "remove":
			case "take":
				if (tag.contains("%") && caster instanceof Player) {
					varTag = MagicSpells.doVariableReplacements((Player) caster, tag);
					target.removeScoreboardTag(varTag);
				} else target.removeScoreboardTag(tag);
				break;
			case "clear":
				Set<String> tags = new HashSet<>(target.getScoreboardTags());
				tags.forEach(target::removeScoreboardTag);
				break;
			default:
				MagicSpells.error("TagEntitySpell '" + internalName + "' has an invalid operation defined!");
		}
	}

}
