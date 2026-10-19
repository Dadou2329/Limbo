package fr.leslimbes.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.registry.LimbesBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;

/**
 * Routes royales procédurales : des polylignes (points de passage en x, z) dont chaque chunk pose le tronçon
 * qui le traverse, en suivant le relief (pavage du bloc de surface, marches d'un bloc au plus, végétation
 * dégagée, lampadaires réguliers). Déterministe : le résultat ne dépend que de la définition et du relief.
 *
 * <pre>{"type": "les_limbes:routes", "config": {"routes": [{"nom": "...", "materiau": "les_limbes:pierre_nacree_taillee",
 *  "largeur": 3, "lampadaires": 24, "points": [[0, 140], [150, 60]]}]}}</pre>
 */
public class RoutesFeature extends Feature<RoutesFeature.Config> {
	public record Route(String nom, Identifier materiau, int largeur, int lampadaires, List<List<Integer>> points) {
		public static final Codec<Route> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("nom", "").forGetter(Route::nom),
				Identifier.CODEC.fieldOf("materiau").forGetter(Route::materiau),
				Codec.intRange(1, 7).optionalFieldOf("largeur", 3).forGetter(Route::largeur),
				Codec.intRange(0, 256).optionalFieldOf("lampadaires", 24).forGetter(Route::lampadaires),
				Codec.INT.listOf().listOf().fieldOf("points").forGetter(Route::points)
		).apply(instance, Route::new));
	}

	public record Config(List<Route> routes) implements FeatureConfig {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Route.CODEC.listOf().fieldOf("routes").forGetter(Config::routes)
		).apply(instance, Config::new));
	}

	public RoutesFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<Config> context) {
		StructureWorldAccess world = context.getWorld();
		ChunkPos chunk = new ChunkPos(context.getOrigin());
		int x0 = chunk.getStartX(), z0 = chunk.getStartZ();
		boolean pose = false;
		for (Route route : context.getConfig().routes()) {
			Block materiau = Registries.BLOCK.get(route.materiau());
			List<List<Integer>> pts = route.points();
			double parcouru = 0.0;
			for (int i = 0; i + 1 < pts.size(); i++) {
				double ax = pts.get(i).get(0), az = pts.get(i).get(1);
				double bx = pts.get(i + 1).get(0), bz = pts.get(i + 1).get(1);
				double longueur = Math.hypot(bx - ax, bz - az);
				if (longueur < 1.0) {
					continue;
				}
				// Le tronçon traverse-t-il le chunk (marge = largeur) ?
				double marge = route.largeur() + 2;
				if (Math.max(ax, bx) < x0 - marge || Math.min(ax, bx) > x0 + 16 + marge || Math.max(az, bz) < z0 - marge || Math.min(az, bz) > z0 + 16 + marge) {
					parcouru += longueur;
					continue;
				}
				int pas = (int) Math.ceil(longueur);
				int precedent = Integer.MIN_VALUE;
				for (int s = 0; s <= pas; s++) {
					double t = s / (double) pas;
					int px = MathHelper.floor(ax + (bx - ax) * t);
					int pz = MathHelper.floor(az + (bz - az) * t);
					boolean dedans = px >= x0 && px < x0 + 16 && pz >= z0 && pz < z0 + 16;
					if (!dedans && (px < x0 - 8 || px >= x0 + 24 || pz < z0 - 8 || pz >= z0 + 24)) {
						precedent = Integer.MIN_VALUE;
						continue;
					}
					int h = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, px, pz) - 1;
					if (h < 20) {
						precedent = Integer.MIN_VALUE;
						continue;
					}
					if (precedent != Integer.MIN_VALUE) {
						h = MathHelper.clamp(h, precedent - 1, precedent + 1);
					}
					precedent = h;
					if (!dedans) {
						continue;
					}
					double dxn = -(bz - az) / longueur, dzn = (bx - ax) / longueur;
					int demi = route.largeur() / 2;
					for (int w = -demi; w <= demi; w++) {
						int qx = MathHelper.floor(px + 0.5 + dxn * w), qz = MathHelper.floor(pz + 0.5 + dzn * w);
						if (qx < x0 || qx >= x0 + 16 || qz < z0 || qz >= z0 + 16) {
							continue;
						}
						pose |= paver(world, qx, h, qz, materiau);
					}
					if (route.lampadaires() > 0) {
						int indexLampe = (int) Math.floor((parcouru + t * longueur) / route.lampadaires());
						int indexPrecedent = (int) Math.floor((parcouru + Math.max(0, (s - 1) / (double) pas) * longueur) / route.lampadaires());
						if (indexLampe != indexPrecedent || (s == 0 && i == 0)) {
							int lx = MathHelper.floor(px + 0.5 + dxn * (demi + 1)), lz = MathHelper.floor(pz + 0.5 + dzn * (demi + 1));
							if (lx >= x0 && lx < x0 + 16 && lz >= z0 && lz < z0 + 16) {
								lampadaire(world, lx, h, lz);
							}
						}
					}
				}
				parcouru += longueur;
			}
		}
		return pose;
	}

	/** Pose le pavé à la hauteur voulue : comble ou dégage la colonne d'au plus deux blocs, retire la végétation. */
	private static boolean paver(StructureWorldAccess world, int x, int h, int z, Block materiau) {
		BlockPos sol = new BlockPos(x, h, z);
		BlockState etat = world.getBlockState(sol);
		if (!etat.getFluidState().isEmpty() || etat.isOf(Blocks.WATER)) {
			return false;
		}
		int actuel = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
		if (actuel > h) {
			for (int y = actuel; y > h && y <= h + 2; y--) {
				world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
		} else if (actuel < h) {
			for (int y = actuel + 1; y < h && y >= h - 2; y++) {
				world.setBlockState(new BlockPos(x, y, z), LimbesBlocks.TERRE_DES_LIMBES.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
		}
		world.setBlockState(sol, materiau.getDefaultState(), Block.NOTIFY_LISTENERS);
		BlockPos dessus = sol.up();
		BlockState d = world.getBlockState(dessus);
		if (!d.isAir() && d.getCollisionShape(world, dessus).isEmpty()) {
			world.setBlockState(dessus, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		return true;
	}

	private static void lampadaire(StructureWorldAccess world, int x, int h, int z) {
		BlockPos base = new BlockPos(x, h, z);
		if (!world.getBlockState(base).getFluidState().isEmpty()) {
			return;
		}
		world.setBlockState(base, LimbesBlocks.PIERRE_NACREE_TAILLEE.getDefaultState(), Block.NOTIFY_LISTENERS);
		for (int i = 1; i <= 2; i++) {
			world.setBlockState(base.up(i), LimbesBlocks.BARRIERE_D_AUBE.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		world.setBlockState(base.up(3), LimbesBlocks.LANTERNE_NACREE.getDefaultState(), Block.NOTIFY_LISTENERS);
	}
}
