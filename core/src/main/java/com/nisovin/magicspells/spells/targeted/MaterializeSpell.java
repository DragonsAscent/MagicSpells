package com.nisovin.magicspells.spells.targeted;

import java.util.*;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.RayTraceResult;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.events.MagicSpellsBlockPlaceEvent;
import com.nisovin.magicspells.events.MagicSpellsBlockBreakEvent;

public class MaterializeSpell extends TargetedSpell implements TargetedLocationSpell {

	/*These extra features were inspired by Shadoward12's Rune/Pattern-Tester spell,
	Thank You! Shadoward12!*/

	private List<Block> blocks;
	private boolean removeBlocks;

	//Normal Features
	private Set<Material> materials;
	private Material defaultMaterial;
	private int resetDelay;
	private boolean falling;
	private boolean applyPhysics;
	private boolean checkPlugins;
	boolean playBreakEffect;
	private String strFailed;

	//Pattern Configuration
	private boolean usePattern;
	private List<String> patterns;
	private Material[][] rowPatterns;
	private boolean hasPatterns;
	private boolean restartPatternEachRow;
	private boolean randomizePattern;
	private boolean stretchPattern;

	//Cuboid Parameters
	private String area;
	private ConfigData<Integer> height;
	private ConfigData<Double> fallHeight;

	//Cuboid Variables;
	private int rowSize;
	private int columnSize;

	public MaterializeSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		String blockType = getConfigString("block-type", "stone");
		defaultMaterial = Util.getMaterial(blockType);
		if (defaultMaterial == null || !defaultMaterial.isBlock()) {
			MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid block-type defined!");
			defaultMaterial = Material.STONE;
		}

		resetDelay = getConfigInt("reset-delay", 0);
		falling = getConfigBoolean("falling", false);
		applyPhysics = getConfigBoolean("apply-physics", true);
		checkPlugins = getConfigBoolean("check-plugins", true);
		playBreakEffect = getConfigBoolean("play-break-effect", true);
		strFailed = getConfigString("str-failed", "");

		usePattern = getConfigBoolean("use-pattern", false);
		patterns = getConfigStringList("patterns", null);
		restartPatternEachRow = getConfigBoolean("restart-pattern-each-row", false);
		randomizePattern = getConfigBoolean("randomize-pattern", false);
		stretchPattern = getConfigBoolean("stretch-pattern", false);

		area = getConfigString("area", "1x1");
		height = getConfigDataInt("height", 1);
		fallHeight = getConfigDataDouble("fall-height", 0.5);

		removeBlocks = getConfigBoolean("remove-blocks", true);
		blocks = new ArrayList<>();
	}

	@Override
	public void initialize() {
		super.initialize();

		parseArea();

		//After the reset-delay passes, we need to remove all the blocks that were materialized.
		//We store them within "materials" and "rowPatterns" as well
		boolean ready;

		materials = new HashSet<>();
		materials.add(defaultMaterial);
		hasPatterns = usePattern && patterns != null && !patterns.isEmpty();

		if (hasPatterns) ready = parseBlocks(patterns);
		else ready = false;

		//If the parser failed, we'll have to force a string inside;
		if (!ready) {
			rowPatterns = new Material[1][1];
			rowPatterns[0][0] = defaultMaterial;
		}
	}

	@Override
	public void turnOff() {
		for (Block b : blocks) {
			b.setType(Material.AIR);
		}

		blocks.clear();
	}

	@Override
	public CastResult cast(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		RayTraceResult result = rayTraceBlocks(data);
		if (result == null) return noTarget(data);

		Block against = result.getHitBlock();
		Block block = against.getRelative(result.getHitBlockFace());

		SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, data, block.getLocation());
		if (!event.callEvent()) return noTarget(strFailed, event);

		data = event.getSpellData();
		block = event.getTargetLocation().getBlock();
		against = resolveSupportBlock(against, block);

		return castAtResolvedBlock(caster, block, against, data);
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		Block targetBlock = resolveTargetBlock(data);
		if (targetBlock == null) return noTarget(strFailed, data);

		Block against = targetBlock.getRelative(BlockFace.DOWN);

		if (!data.hasCaster()) {
			return castAtResolvedBlock(null, targetBlock, against, data);
		}

		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);
		return castAtResolvedBlock(caster, targetBlock, against, data);
	}

	private void parseArea() {
		String[] areaParts = area.toLowerCase(Locale.ROOT).split("x", 2);
		if (areaParts.length != 2) {
			MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid area '" + area + "' defined. Falling back to 1x1.");
			rowSize = 1;
			columnSize = 1;
			return;
		}

		try {
			rowSize = Math.max(1, Integer.parseInt(areaParts[0]));
			columnSize = Math.max(1, Integer.parseInt(areaParts[1]));
		} catch (NumberFormatException e) {
			MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid area '" + area + "' defined. Falling back to 1x1.");
			rowSize = 1;
			columnSize = 1;
		}
	}

	private CastResult castAtResolvedBlock(Player player, Block block, Block against, SpellData data) {
		data = data.location(block.getLocation());
		int configuredHeight = height.get(data);

		boolean done = isSingleBlockPlacement(configuredHeight)
			? materialize(player, block, against, defaultMaterial, data)
			: materializeArea(player, block, data);

		return done ? new CastResult(PostCastAction.HANDLE_NORMALLY, data) : noTarget(strFailed, data);
	}

	private boolean isSingleBlockPlacement(int configuredHeight) {
		return rowSize == 1 && columnSize == 1 && Math.abs(configuredHeight) == 1 && !hasPatterns;
	}

	private Block resolveTargetBlock(SpellData data) {
		Block block = data.location().getBlock();
		if (block.isReplaceable()) return block;

		Block upper = block.getRelative(BlockFace.UP);
		return upper.isReplaceable() ? upper : null;
	}

	private Block resolveSupportBlock(Block previousSupport, Block targetBlock) {
		if (targetBlock.getRelative(BlockFace.DOWN).equals(previousSupport)) return previousSupport;
		return targetBlock.getRelative(BlockFace.DOWN);
	}

	private boolean materializeArea(Player player, Block centerBlock, SpellData data) {
		Location start = centerBlock.getLocation().clone().add(-(rowSize / 2), 0, -(columnSize / 2));

		int configuredHeight = height.get(data);
		int layerDirection = configuredHeight < 0 ? -1 : 1;
		int layerCount = Math.max(Math.abs(configuredHeight), 1);

		int rowPosition = 0;
		for (int layer = 0; layer < layerCount; layer++) {
			int patternPosition = 0;
			int yOffset = layer * layerDirection;

			for (int z = 0; z < columnSize; z++) {
				if (hasPatterns && patternPosition >= rowPatterns.length) patternPosition = 0;

				int rowLength = getRowLength(patternPosition);
				if (restartPatternEachRow) rowPosition = 0;

				for (int x = 0; x < rowSize; x++) {
					Block placeBlock = start.clone().add(x, yOffset, z).getBlock();
					Material blockMaterial = resolveBlockMaterial(placeBlock, patternPosition, rowPosition, layerDirection, layer);

					if (rowPosition >= rowLength) rowPosition = 0;
					rowPosition++;

					Block supportBlock = placeBlock.getRelative(0, -layerDirection, 0);
					SpellData blockData = data.location(placeBlock.getLocation());
					boolean done = materialize(player, placeBlock, supportBlock, blockMaterial, blockData);
					if (!done) return false;
				}

				patternPosition++;
			}
		}

		return true;
	}

	private Material resolveBlockMaterial(Block placeBlock, int patternPosition, int rowPosition, int layerDirection, int layer) {
		if (stretchPattern && layer > 0) return placeBlock.getRelative(0, -layerDirection, 0).getType();
		return blockGenerator(randomizePattern, patternPosition, rowPosition);
	}

	private int getRowLength(int patternPosition) {
		return rowPatterns[patternPosition].length;
	}

	private boolean parseBlocks(List<String> patternList) {
		if (patternList == null) return false;

		int patternSize = patternList.size();
		int iteration = 0;

		rowPatterns = new Material[patternSize][];

		//Let's parse all the rows within patternList
		for (String list : patternList) {
			String[] split = list.split(",");
			int arraySize = split.length;
			int blockPosition = 0;

			rowPatterns[iteration] = new Material[arraySize];

			for (String block : split) {
				Material mat = Util.getMaterial(block);
				if (mat == null) mat = Material.STONE;

				materials.add(mat);
				rowPatterns[iteration][blockPosition] = mat;
				blockPosition++;
			}

			iteration++;
		}
		return true;
	}

	private Material blockGenerator(boolean randomize, int patternPosition, int rowPosition) {
		if (!hasPatterns) return defaultMaterial;

		int randomIndex = random.nextInt(getRowLength(patternPosition));
		return randomize ? rowPatterns[patternPosition][randomIndex] : rowPatterns[patternPosition][rowPosition];
	}

	private boolean materialize(Player player, Block block, Block against, Material blockMaterial, SpellData data) {
		if (!block.isReplaceable()) return false;

		var blockData = blockMaterial.createBlockData();
		if (!falling && !block.canPlace(blockData)) return false;

		BlockState blockState = block.getState();

		if (checkPlugins && player != null) {
			block.setBlockData(blockData, false);
			MagicSpellsBlockPlaceEvent event = new MagicSpellsBlockPlaceEvent(block, blockState, against, player.getEquipment().getItemInMainHand(), player, true);
			EventUtil.call(event);
			if (event.isCancelled()) return false;

			if (falling) blockState.update(true);
			else if (applyPhysics) block.setBlockData(blockData, true);
		}
		else if (!falling) block.setBlockData(blockData, applyPhysics);

		if (falling) {
			Location location = block.getLocation().add(0.5, fallHeight.get(data), 0.5);
			block.getWorld().spawn(location, FallingBlock.class, fb -> fb.setBlockData(blockData));
		}

		playSpellEffects(EffectPosition.TARGET, block.getLocation(), data);
		if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player, data);
			playSpellEffectsTrail(player.getLocation(), block.getLocation(), data);
		}

		if (playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
		if (removeBlocks && !falling) blocks.add(block);

		if (resetDelay > 0 && !falling) {
			MagicSpells.scheduleDelayedTask(() -> {
				if (materials.contains(block.getType())) {
					blocks.remove(block);
					playSpellEffects(EffectPosition.DELAYED, block.getLocation(), data);
					if (checkPlugins && player != null) {
						MagicSpellsBlockBreakEvent event = new MagicSpellsBlockBreakEvent(block, player);
						EventUtil.call(event);
						if (event.isCancelled()) return;
					}
					var placedBlockData = block.getBlockData();
					block.setType(Material.AIR);
					playSpellEffects(EffectPosition.BLOCK_DESTRUCTION, block.getLocation(), data);
					if (playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, placedBlockData);
				}
			}, resetDelay);
		}
		return true;
	}

}
