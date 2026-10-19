package fr.leslimbes.duel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Blocs posés pendant un duel (racines, forteresse, miroirs) et retirés ensuite. On ne remplace jamais autre
 * chose que de l'air ou un bloc remplaçable (herbe, fleurs) : le terrain et les constructions restent intacts
 * et chaque bloc retrouve exactement son état précédent, sans duplication ni perte.
 */
public final class BlocsTemporaires {
	private record Entree(BlockState avant, long expiration) {
	}

	private final Map<BlockPos, Entree> poses = new LinkedHashMap<>();

	/** Pose un bloc temporaire ; {@code duree} en ticks (0 = jusqu'à la fin du duel). @return true si posé. */
	public boolean poser(ServerWorld world, BlockPos pos, BlockState etat, int duree) {
		if (poses.containsKey(pos)) {
			return false;
		}
		BlockState actuel = world.getBlockState(pos);
		if (!actuel.isAir() && !actuel.isReplaceable()) {
			return false;
		}
		if (!world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, new net.minecraft.util.math.Box(pos), e -> true).isEmpty()) {
			return false;
		}
		poses.put(pos.toImmutable(), new Entree(actuel, duree > 0 ? world.getTime() + duree : 0L));
		world.setBlockState(pos, etat, Block.NOTIFY_ALL);
		return true;
	}

	/** Retire les blocs dont la durée est écoulée. */
	public void expirer(ServerWorld world) {
		long now = world.getTime();
		Iterator<Map.Entry<BlockPos, Entree>> it = poses.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BlockPos, Entree> e = it.next();
			if (e.getValue().expiration() > 0 && now >= e.getValue().expiration()) {
				restaurer(world, e.getKey(), e.getValue());
				it.remove();
			}
		}
	}

	/** Retire tout. */
	public void tout(ServerWorld world) {
		List<Map.Entry<BlockPos, Entree>> copie = new ArrayList<>(poses.entrySet());
		poses.clear();
		for (Map.Entry<BlockPos, Entree> e : copie) {
			restaurer(world, e.getKey(), e.getValue());
		}
	}

	public int nombre() {
		return poses.size();
	}

	private static void restaurer(ServerWorld world, BlockPos pos, Entree e) {
		if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			world.setBlockState(pos, e.avant(), Block.NOTIFY_ALL);
		}
	}
}
