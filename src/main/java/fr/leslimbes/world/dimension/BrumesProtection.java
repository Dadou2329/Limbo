package fr.leslimbes.world.dimension;

import fr.leslimbes.registry.LimbesParticles;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.Optional;

/**
 * Les Brumes : sous les terres suspendues, personne ne tombe dans le vide. Un joueur qui passe sous
 * {@link LimbesDimension#ALTITUDE_BRUMES} est ramené en douceur à son dernier lieu sûr.
 */
public final class BrumesProtection {
	private static final int INTERVALLE_LIEU_SUR = 40;

	public static void tick(ServerWorld limbes) {
		long time = limbes.getTime();
		for (ServerPlayerEntity player : limbes.getPlayers()) {
			if (player.isSpectator()) {
				continue;
			}
			PlayerLimbesData data = PlayerLimbesData.of(player);
			if (player.getY() < LimbesDimension.ALTITUDE_BRUMES) {
				ramener(limbes, player, data);
				continue;
			}
			if (time % INTERVALLE_LIEU_SUR == 0 && player.isOnGround() && player.getY() > LimbesDimension.ALTITUDE_BRUMES + 8) {
				BlockPos pos = player.getBlockPos();
				BlockState sol = limbes.getBlockState(pos.down());
				if (!sol.getCollisionShape(limbes, pos.down()).isEmpty() && !sol.isLiquid()) {
					data.lieuSur = Optional.of(GlobalPos.create(LimbesDimension.MONDE, pos));
				}
			}
		}
	}

	private static void ramener(ServerWorld limbes, ServerPlayerEntity player, PlayerLimbesData data) {
		Vec3d destination = data.lieuSur.filter(gp -> gp.dimension() == LimbesDimension.MONDE)
				.map(gp -> Vec3d.ofBottomCenter(gp.pos()))
				.orElse(LimbesSites.ARRIVEE);
		// Sécurité : si le lieu mémorisé n'est plus praticable, on vise la surface à cet endroit.
		BlockPos bp = BlockPos.ofFloored(destination);
		if (!limbes.getBlockState(bp).getCollisionShape(limbes, bp).isEmpty()) {
			int y = limbes.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, bp.getX(), bp.getZ());
			destination = new Vec3d(bp.getX() + 0.5, Math.max(y, LimbesDimension.ALTITUDE_BRUMES + 20), bp.getZ() + 0.5);
		}
		limbes.spawnParticles(LimbesParticles.BRUME, player.getX(), player.getY() + 1, player.getZ(), 30, 1.0, 1.0, 1.0, 0.02);
		player.teleport(limbes, destination.x, destination.y, destination.z, player.getYaw(), player.getPitch());
		player.fallDistance = 0.0F;
		player.setVelocity(Vec3d.ZERO);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0, false, false, true));
		limbes.spawnParticles(LimbesParticles.BRUME, destination.x, destination.y + 1, destination.z, 30, 1.0, 1.0, 1.0, 0.02);
		limbes.playSound(null, BlockPos.ofFloored(destination), LimbesSounds.BRUMES, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.sendMessage(Text.translatable("message.les_limbes.brumes").formatted(Formatting.AQUA), true);
	}

	private BrumesProtection() {
	}
}
