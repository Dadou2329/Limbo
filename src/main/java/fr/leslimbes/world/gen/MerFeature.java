package fr.leslimbes.world.gen;

import com.mojang.serialization.Codec;
import fr.leslimbes.world.dimension.LimbesRegions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Remplissage de la Mer suspendue : dans les colonnes de la cuvette (région Mer, distance normalisée ≤ 0,86),
 * tout l'air entre le sol et le niveau de la mer devient de l'eau source. La cuvette est fermée par un rebord
 * de falaises plus haut que le niveau de l'eau (voir {@link LimbesRegions#hauteur}) : l'eau n'a nulle part
 * où couler, aucune cascade ni mise à jour de fluide incontrôlée. Une feature par chunk, étape des lacs.
 */
public class MerFeature extends Feature<DefaultFeatureConfig> {
	public MerFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		ChunkPos chunk = new ChunkPos(context.getOrigin());
		BlockState eau = Blocks.WATER.getDefaultState();
		int poses = 0;
		BlockPos.Mutable m = new BlockPos.Mutable();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = chunk.getStartX() + dx;
				int z = chunk.getStartZ() + dz;
				if (!LimbesRegions.dansLaMer(x + 0.5, z + 0.5)) {
					continue;
				}
				for (int y = LimbesRegions.NIVEAU_MER; y > 8; y--) {
					m.set(x, y, z);
					BlockState s = world.getBlockState(m);
					if (s.isAir()) {
						world.setBlockState(m, eau, Block.NOTIFY_LISTENERS);
						poses++;
					} else if (!s.isOf(Blocks.WATER)) {
						break;
					}
				}
			}
		}
		return poses > 0;
	}
}
