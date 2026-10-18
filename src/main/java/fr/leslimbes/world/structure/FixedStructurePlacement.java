package fr.leslimbes.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.registry.LimbesWorldgen;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Placement « les_limbes:fixed » : la structure est générée exactement dans les chunks listés,
 * quelle que soit la graine. Utilisé pour les lieux persistants (Sanctuaire, Clairval, manoir...).
 *
 * <pre>{"type": "les_limbes:fixed", "salt": 1, "chunks": [[0, 0], [8, 9]]}</pre>
 */
public class FixedStructurePlacement extends StructurePlacement {
	public static final MapCodec<FixedStructurePlacement> CODEC = RecordCodecBuilder.<FixedStructurePlacement>mapCodec(
			instance -> buildCodec(instance)
					.and(Codec.INT.listOf().listOf().fieldOf("chunks").forGetter(FixedStructurePlacement::getChunks))
					.apply(instance, FixedStructurePlacement::new));

	private final List<List<Integer>> chunks;
	private final Set<Long> ensemble = new HashSet<>();

	public FixedStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod method, float frequency, int salt,
								   Optional<ExclusionZone> exclusionZone, List<List<Integer>> chunks) {
		super(locateOffset, method, frequency, salt, exclusionZone);
		this.chunks = chunks;
		for (List<Integer> c : chunks) {
			if (c.size() == 2) {
				ensemble.add(cle(c.get(0), c.get(1)));
			}
		}
	}

	private static long cle(int x, int z) {
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}

	public List<List<Integer>> getChunks() {
		return chunks;
	}

	@Override
	protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
		return ensemble.contains(cle(chunkX, chunkZ));
	}

	@Override
	public StructurePlacementType<?> getType() {
		return LimbesWorldgen.PLACEMENT_FIXE;
	}
}
