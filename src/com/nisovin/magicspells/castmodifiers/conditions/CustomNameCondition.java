package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;

public class CustomNameCondition extends Condition {

    private String name;
    private boolean isVar;

    @Override
    public boolean setVar(String var) {
        if (var == null || var.isEmpty()) return false;
        name = ChatColor.translateAlternateColorCodes('&', var);
        if (name.contains("%var:")) isVar = true;
        return true;
    }

    @Override
    public boolean check(Player player) {
        return false;
    }

    @Override
    public boolean check(Player player, LivingEntity target) {
        //if (target instanceof Player) return check((Player) target);

        String n = target.getCustomName();
        if (n == null || n.isEmpty()) return false;

        String targetName = name;
        if (isVar) targetName = MagicSpells.doArgumentAndVariableSubstitution(targetName, player, null);
        else targetName = targetName.replace("__", " ");
        
        return targetName.equalsIgnoreCase(n);
    }

    @Override
    public boolean check(Player player, Location location) {
        return false;
    }

}