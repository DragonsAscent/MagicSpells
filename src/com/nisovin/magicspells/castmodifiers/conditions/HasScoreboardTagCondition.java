package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;

public class HasScoreboardTagCondition extends Condition {

    private String var;
    private String tag;
    private boolean isVar;
    private Set<String> enttags = new HashSet<>();

    @Override
    public boolean setVar(String var) {
        if (var == null || var.isEmpty()) return false;
        if (var.contains("%var:")) isVar = true;
        tag = var;
        return true;
    }

    @Override
    public boolean check(Player player) {
        if (isVar) tag = MagicSpells.doArgumentAndVariableSubstitution(var, player, null);
        enttags = player.getScoreboardTags();
        return enttags.contains(tag);
    }

    @Override
    public boolean check(Player player, LivingEntity target) {
        if (isVar) tag = MagicSpells.doArgumentAndVariableSubstitution(var, player, null);
        enttags = target.getScoreboardTags();
        return enttags.contains(tag);
    }

    @Override
    public boolean check(Player player, Location location) {
        return false;
    }
}
