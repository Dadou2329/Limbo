package fr.leslimbes.npc;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.registry.LimbesEntities;
import fr.leslimbes.world.state.LimbesWorldState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Map;

/**
 * Réapparition contrôlée des personnages essentiels : si un personnage ancré a disparu (bug, chunk
 * corrompu, commande), il réapparaît à son domicile dès que la zone est chargée.
 */
public final class EssentialNpcs {
	private static final int INTERVALLE_TICKS = 200;
	private static final long DELAI_MIN_ENTRE_REAPPARITIONS = 1200L;

	public static void tick(ServerWorld world) {
		if (world.getTime() % INTERVALLE_TICKS != 0L) {
			return;
		}
		LimbesWorldState state = LimbesWorldState.get(world.getServer());
		for (Map.Entry<String, BlockPos> entry : state.ancrages().entrySet()) {
			String role = entry.getKey();
			BlockPos domicile = entry.getValue();
			if (!world.isChunkLoaded(domicile.getX() >> 4, domicile.getZ() >> 4)) {
				continue;
			}
			if (world.getTime() - state.derniereReapparition(role) < DELAI_MIN_ENTRE_REAPPARITIONS) {
				continue;
			}
			Box zone = new Box(domicile).expand(64.0, 32.0, 64.0);
			List<HabitantEntity> presents = world.getEntitiesByClass(HabitantEntity.class, zone, h -> h.getRole().equals(role) && h.isAlive() && !h.estDelegation());
			if (!presents.isEmpty()) {
				continue;
			}
			reapparaitre(world, role, domicile);
			state.noterReapparition(role, world.getTime());
		}
	}

	public static HabitantEntity reapparaitre(ServerWorld world, String role, BlockPos domicile) {
		HabitantEntity pnj = LimbesEntities.HABITANT.create(world);
		if (pnj == null) {
			return null;
		}
		pnj.refreshPositionAndAngles(domicile.getX() + 0.5, domicile.getY(), domicile.getZ() + 0.5, 0.0F, 0.0F);
		pnj.setRole(role);
		pnj.setDomicile(domicile);
		pnj.setPersistent();
		world.spawnEntity(pnj);
		LesLimbes.LOGGER.info("Personnage essentiel « {} » réapparu en {}", role, domicile.toShortString());
		return pnj;
	}

	private EssentialNpcs() {
	}
}
