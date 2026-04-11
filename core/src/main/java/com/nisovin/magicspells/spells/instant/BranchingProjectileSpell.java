package com.nisovin.magicspells.spells.instant;

import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.CastResult;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.EffectPosition;

import java.util.*;

public class BranchingProjectileSpell extends InstantSpell {

    private final ConfigData<Double> maxDistance;
    private final ConfigData<Integer> maxDuration;
    private final ConfigData<Double> branchProbability;
    private final ConfigData<Integer> minBranchLength;
    private final ConfigData<Integer> maxBranchLength;
    private final ConfigData<Double> branchAngle;
    private final ConfigData<Double> stepLength;
    private final ConfigData<Double> hitRadius;
    private final ConfigData<String> spellToCastName;
    private Subspell spellToCast;

    public BranchingProjectileSpell(MagicConfig config, String spellName) {
        super(config, spellName);
        maxDistance = getConfigDataDouble("max-distance", 20.0);
        maxDuration = getConfigDataInt("max-duration", 40);
        branchProbability = getConfigDataDouble("branch-probability", 0.3);
        minBranchLength = getConfigDataInt("min-branch-length", 3);
        maxBranchLength = getConfigDataInt("max-branch-length", 8);
        branchAngle = getConfigDataDouble("branch-angle", 30.0);
        stepLength = getConfigDataDouble("step-length", 0.8);
        hitRadius = getConfigDataDouble("hit-radius", 1.5);
        spellToCastName = getConfigDataString("spell", "");
    }

    @Override
    public void initialize() {
        super.initialize();
        spellToCast = initSubspell(spellToCastName.get(null), "BranchingProjectileSpell '" + internalName + "' has an invalid spell: '" + spellToCastName.get(null) + "' defined!");
    }

    @Override
    public CastResult cast(SpellData data) {
        Location start = data.caster().getEyeLocation();
        Vector direction = start.getDirection().normalize();
        double maxDist = maxDistance.get(data);
        int maxTicks = maxDuration.get(data);
        new BranchTask(start, direction, maxDist, maxTicks, data, 0, false).runTaskTimer(MagicSpells.plugin, 0, 1);
        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

    private class BranchTask extends BukkitRunnable {
        private final Location current;
        private final Vector direction;
        private final double maxDist;
        private final int maxTicks;
        private final SpellData data;
        private final int branchDepth;
        private final boolean isBranch;
        private double traveled = 0;
        private int ticks = 0;
        private int branchLength = 0;
        private final int branchMaxLength;

        BranchTask(Location start, Vector direction, double maxDist, int maxTicks, SpellData data, int branchDepth, boolean isBranch) {
            this.current = start.clone();
            this.direction = direction.clone();
            this.maxDist = maxDist;
            this.maxTicks = maxTicks;
            this.data = data;
            this.branchDepth = branchDepth;
            this.isBranch = isBranch;
            this.branchMaxLength = isBranch ? randomBetween(minBranchLength.get(data), maxBranchLength.get(data)) : Integer.MAX_VALUE;
        }

        @Override
        public void run() {
            boolean finished = (traveled >= maxDist || ticks >= maxTicks || branchLength >= branchMaxLength);
            if (finished) {
                // Play DELAYED effect at the end of the branch/trunk
                playSpellEffects(EffectPosition.DELAYED, current, data.location(current));
                cancel();
                return;
            }
            // Add a small random curve to the direction for organic movement (branches only)
            if (isBranch) {
                double curveStrength = 0.15; // tweak for more/less curve
                Vector curve = new Vector(
                    (Math.random() - 0.5) * curveStrength,
                    (Math.random() - 0.5) * curveStrength,
                    (Math.random() - 0.5) * curveStrength
                );
                direction.add(curve);
                direction.normalize();
            }
            // Move forward
            current.add(direction.clone().multiply(stepLength.get(data)));
            traveled += stepLength.get(data);
            branchLength++;
            ticks++;
            // Play particle effect using appropriate EffectPosition
            EffectPosition pos = isBranch ? EffectPosition.SPECIAL : EffectPosition.PROJECTILE;
            playSpellEffects(pos, current, data.location(current));
            // Hit detection
            for (LivingEntity entity : getNearbyEntities(current, hitRadius.get(data))) {
                if (entity.equals(data.caster())) continue;
                SpellData hitData = data.target(entity).location(current);
                if (spellToCast != null) spellToCast.subcast(hitData);
            }
            // Branching
            if (!isBranch && Math.random() < branchProbability.get(data)) {
                Vector branchDir = getBranchDirection(direction, branchAngle.get(data));
                new BranchTask(current.clone(), branchDir, maxDist, maxTicks, data, branchDepth + 1, true).runTaskTimer(MagicSpells.plugin, 0, 1);
            }
        }
    }

    private Vector getBranchDirection(Vector base, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg);
        double yaw = Math.atan2(base.getZ(), base.getX());
        double pitch = Math.asin(base.getY());
        double branchYaw = yaw + (Math.random() - 0.5) * angleRad;
        double branchPitch = pitch + (Math.random() - 0.5) * (angleRad / 2);
        double x = Math.cos(branchYaw) * Math.cos(branchPitch);
        double y = Math.sin(branchPitch);
        double z = Math.sin(branchYaw) * Math.cos(branchPitch);
        return new Vector(x, y, z).normalize();
    }

    private int randomBetween(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    private List<LivingEntity> getNearbyEntities(Location loc, double radius) {
        List<LivingEntity> list = new ArrayList<>();
        for (org.bukkit.entity.Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (e instanceof LivingEntity le && le.isValid() && !le.isDead()) {
                list.add(le);
            }
        }
        return list;
    }
}
