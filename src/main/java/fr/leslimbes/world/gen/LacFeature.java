package fr.leslimbes.world.gen;

import com.mojang.serialization.Codec;
import fr.leslimbes.registry.LimbesBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Lac clair des Prairies : bassin elliptique peu profond, bordé de pierre nacrée polie, rempli d'eau.
 *
 * <p>Remplace la feature vanilla {@code minecraft:lake} (dépréciée) qui lit les biomes jusqu'à deux
 * chunks de distance pour poser de la glace et plante la génération. Ici, toutes les lectures et
 * écritures restent dans un rayon de 7 blocs autour de l'origine, soit au plus un chunk voisin.</p>
 */
public class LacFeature extends Feature<DefaultFeatureConfig> {
	private static final int RAYON_MAX = 7;

	public LacFeature(Codec<DefaultFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		Random random = context.getRandom();
		BlockPos origine = context.getOrigin();
		// L'origine est le premier bloc d'air au-dessus de la surface ; le niveau d'eau sera la surface.
		int surface = origine.getY() - 1;
		if (surface <= world.getBottomY() + 6) {
			return false;
		}
		BlockPos sol = new BlockPos(origine.getX(), surface, origine.getZ());
		if (!estSolNaturel(world.getBlockState(sol))) {
			return false;
		}
		int rx = 4 + random.nextInt(RAYON_MAX - 3);
		int rz = 4 + random.nextInt(RAYON_MAX - 3);
		int profondeur = 2 + random.nextInt(2);
		BlockState eau = Blocks.WATER.getDefaultState();
		BlockState bord = LimbesBlocks.PIERRE_NACREE_POLIE.getDefaultState();
		BlockState fond = LimbesBlocks.TERRE_DES_LIMBES.getDefaultState();
		int poses = 0;
		for (int dx = -rx; dx <= rx; dx++) {
			for (int dz = -rz; dz <= rz; dz++) {
				double n = (double) (dx * dx) / (rx * rx) + (double) (dz * dz) / (rz * rz);
				if (n > 1.0) {
					continue;
				}
				int x = origine.getX() + dx;
				int z = origine.getZ() + dz;
				// On ne creuse que les colonnes dont la surface est au même niveau (terrain praticable).
				BlockPos colonne = new BlockPos(x, surface, z);
				if (!estSolNaturel(world.getBlockState(colonne)) || !world.getBlockState(colonne.up()).getCollisionShape(world, colonne.up()).isEmpty()) {
					continue;
				}
				int localProfondeur = n > 0.55 ? 1 : profondeur;
				for (int dy = 0; dy < localProfondeur; dy++) {
					BlockPos p = new BlockPos(x, surface - dy, z);
					world.setBlockState(p, eau, Block.NOTIFY_LISTENERS);
					poses++;
				}
				BlockPos dessous = new BlockPos(x, surface - localProfondeur, z);
				world.setBlockState(dessous, n > 0.55 ? bord : fond, Block.NOTIFY_LISTENERS);
				// Végétation au-dessus de l'eau retirée.
				BlockPos dessus = colonne.up();
				if (!world.getBlockState(dessus).isAir()) {
					world.setBlockState(dessus, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
				}
			}
		}
		if (poses == 0) {
			return false;
		}
		// Margelle : pierre polie sur l'anneau immédiatement extérieur, au niveau de la surface.
		for (int dx = -rx - 1; dx <= rx + 1; dx++) {
			for (int dz = -rz - 1; dz <= rz + 1; dz++) {
				double n = (double) (dx * dx) / ((rx + 1) * (rx + 1)) + (double) (dz * dz) / ((rz + 1) * (rz + 1));
				double nInt = (double) (dx * dx) / (rx * rx) + (double) (dz * dz) / (rz * rz);
				if (n > 1.0 || nInt <= 1.0) {
					continue;
				}
				BlockPos p = new BlockPos(origine.getX() + dx, surface, origine.getZ() + dz);
				if (estSolNaturel(world.getBlockState(p)) && voisinEau(world, p, eau)) {
					world.setBlockState(p, bord, Block.NOTIFY_LISTENERS);
				}
			}
		}
		return true;
	}

	private static boolean estSolNaturel(BlockState state) {
		return state.isOf(LimbesBlocks.HERBE_ARGENTEE) || state.isOf(LimbesBlocks.TERRE_DES_LIMBES) || state.isOf(LimbesBlocks.PIERRE_NACREE);
	}

	private static boolean voisinEau(StructureWorldAccess world, BlockPos p, BlockState eau) {
		return world.getBlockState(p.north()).equals(eau) || world.getBlockState(p.south()).equals(eau)
				|| world.getBlockState(p.east()).equals(eau) || world.getBlockState(p.west()).equals(eau);
	}
}
