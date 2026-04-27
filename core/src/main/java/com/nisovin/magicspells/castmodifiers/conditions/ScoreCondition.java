package com.nisovin.magicspells.castmodifiers.conditions;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.Name;

/**
 * Usage: score <objective><operator><value> required/denied
 * Example: score kills>3 required
 */
@Name("score")
public class ScoreCondition extends Condition {

    private String objectiveName;
    private String operator;
    private int value;

    @Override
    public boolean initialize(String var) {
        if (var == null) return false;
        var = var.trim();
        String[] ops = {">=","<=","!=",">","<","="};
        for (String op : ops) {
            int idx = var.indexOf(op);
            if (idx > 0) {
                objectiveName = var.substring(0, idx).trim();
                operator = op;
                try {
                    value = Integer.parseInt(var.substring(idx + op.length()).trim());
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean check(LivingEntity entity) {
        return checkScore(entity);
    }

    @Override
    public boolean check(LivingEntity caster, LivingEntity target) {
        return checkScore(target);
    }

    @Override
    public boolean check(LivingEntity caster, Location location) {
        // Not applicable for locations, always false
        return false;
    }

    private boolean checkScore(LivingEntity entity) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = scoreboard.getObjective(objectiveName);
        if (obj == null) return false;
        int score = obj.getScoreFor(entity).getScore();
        return compare(score, operator, value);
    }

    private boolean compare(int score, String op, int val) {
        switch (op) {
            case ">": return score > val;
            case "<": return score < val;
            case ">=": return score >= val;
            case "<=": return score <= val;
            case "=": return score == val;
            case "!=": return score != val;
            default: return false;
        }
    }
}
