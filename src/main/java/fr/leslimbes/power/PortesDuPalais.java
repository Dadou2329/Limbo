package fr.leslimbes.power;

import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Branche « Portes du Palais ». Le Pas court ouvre une porte quelques blocs devant le joueur.
 *
 * <p>La destination est vérifiée : espace libre pour le joueur, chunk chargé, jamais au-dessus du
 * vide (un sol existe sous la destination dans les 6 blocs) et jamais dans un bloc. Si la
 * destination est obstruée, on recule le long du trajet jusqu'à une case valide.</p>
 */
public final class PortesDuPalais {
	public static final double PORTEE = 7.0;
	private static final double PAS = 0.5;

	public static boolean pasCourt(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d direction = player.getRotationVector();
		// On limite l'angle vertical pour rester un pas, pas un vol.
		Vec3d dir = new Vec3d(direction.x, Math.max(-0.5, Math.min(0.5, direction.y)), direction.z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		Vec3d depart = player.getPos();
		Vec3d meilleure = null;
		for (double d = PORTEE; d >= 1.5; d -= PAS) {
			Vec3d candidate = depart.add(dir.multiply(d));
			Vec3d ajustee = ajusterAuSol(world, player, candidate);
			if (ajustee != null) {
				meilleure = ajustee;
				break;
			}
		}
		if (meilleure == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		Abilities.effet(world, "porte_depart", depart.add(0, 1.0, 0), dir, 0);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 1.1F);
		player.teleport(world, meilleure.x, meilleure.y, meilleure.z, player.getYaw(), player.getPitch());
		player.fallDistance = 0.0F;
		player.setVelocity(Vec3d.ZERO);
		player.velocityModified = true;
		Abilities.effet(world, "porte_arrivee", meilleure.add(0, 1.0, 0), dir, 0);
		world.playSound(null, BlockPos.ofFloored(meilleure), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 0.9F);
		return true;
	}

	/** Trouve une position valide proche de {@code candidate} : libre, chargée, avec un sol en dessous. */
	private static Vec3d ajusterAuSol(ServerWorld world, ServerPlayerEntity player, Vec3d candidate) {
		BlockPos bp = BlockPos.ofFloored(candidate);
		if (!world.isChunkLoaded(bp.getX() >> 4, bp.getZ() >> 4)) {
			return null;
		}
		// Essai à la hauteur demandée puis légèrement au-dessus (marche, pente).
		for (int dy = 0; dy <= 2; dy++) {
			Vec3d p = new Vec3d(candidate.x, Math.floor(candidate.y) + dy, candidate.z);
			if (estLibre(world, player, p) && solSous(world, p, 6)) {
				return p;
			}
		}
		return null;
	}

	private static boolean estLibre(ServerWorld world, ServerPlayerEntity player, Vec3d p) {
		double w = player.getWidth() / 2.0;
		Box box = new Box(p.x - w, p.y, p.z - w, p.x + w, p.y + player.getHeight(), p.z + w);
		return world.isSpaceEmpty(player, box) && !world.getFluidState(BlockPos.ofFloored(p)).isIn(net.minecraft.registry.tag.FluidTags.LAVA);
	}

	private static boolean solSous(ServerWorld world, Vec3d p, int profondeur) {
		BlockPos bp = BlockPos.ofFloored(p);
		for (int i = 1; i <= profondeur; i++) {
			BlockPos below = bp.down(i);
			if (below.getY() < world.getBottomY()) {
				return false;
			}
			BlockState s = world.getBlockState(below);
			if (!s.getCollisionShape(world, below).isEmpty() || s.isLiquid()) {
				return true;
			}
		}
		return false;
	}

	private PortesDuPalais() {
	}
}
