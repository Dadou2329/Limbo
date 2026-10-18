package fr.leslimbes.power;

import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Branche « Décrets royaux ». « Reculez » : vague de repoussement autour du souverain. */
public final class DecretsRoyaux {
	private static final double RAYON = 6.0;
	private static final float DEGATS = 2.0F;

	public static boolean reculez(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d centre = player.getPos();
		Box zone = new Box(centre.x - RAYON, centre.y - 2, centre.z - RAYON, centre.x + RAYON, centre.y + 3, centre.z + RAYON);
		List<LivingEntity> cibles = world.getEntitiesByClass(LivingEntity.class, zone,
				e -> e != player && e.isAlive() && !(e instanceof PlayerEntity) && !(e instanceof HabitantEntity)
						&& e.squaredDistanceTo(player) <= RAYON * RAYON);
		int repousses = 0;
		for (LivingEntity e : cibles) {
			Vec3d dir = e.getPos().subtract(centre);
			if (dir.lengthSquared() < 0.01) {
				dir = new Vec3d(0, 0, 1);
			}
			dir = dir.normalize();
			double force = 1.6 * (1.0 - Math.min(1.0, e.distanceTo(player) / RAYON) * 0.5);
			if (e instanceof MobEntity mob && mob.isAttacking()) {
				e.damage(world.getDamageSources().playerAttack(player), DEGATS);
			}
			e.takeKnockback(force, -dir.x, -dir.z);
			e.setVelocity(e.getVelocity().add(0, 0.25, 0));
			e.velocityModified = true;
			repousses++;
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "decret_reculez", centre.add(0, 0.2, 0), player.getRotationVector(), repousses);
		return true;
	}

	private DecretsRoyaux() {
	}
}
