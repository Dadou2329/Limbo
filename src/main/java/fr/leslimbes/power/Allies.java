package fr.leslimbes.power;

import fr.leslimbes.entity.duel.PretendantEntity;
import fr.leslimbes.entity.region.GardeAstraleEntity;
import fr.leslimbes.entity.region.SoutienEntity;
import fr.leslimbes.npc.HabitantEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;

/**
 * Qui est l'ami du souverain : les joueurs, les habitants, la garde astrale et les montures liées par un pacte.
 * Les pouvoirs de zone s'appuient sur ces règles pour ne jamais frapper un allié.
 */
public final class Allies {
	public static boolean estAllie(ServerPlayerEntity souverain, Entity e) {
		if (e == souverain || e instanceof PlayerEntity || e instanceof HabitantEntity || e instanceof GardeAstraleEntity) {
			return true;
		}
		return e instanceof TameableEntity t && t.isTamed();
	}

	/** Une créature vivante que les pouvoirs peuvent viser ou repousser (ni allié, ni décor). */
	public static boolean estCible(ServerPlayerEntity souverain, Entity e) {
		return e instanceof LivingEntity v && v.isAlive() && !(e instanceof ArmorStandEntity) && !estAllie(souverain, e) && !v.isInvulnerable();
	}

	/** Une créature manifestement hostile : les pouvoirs qui blessent ne touchent que celles-là. */
	public static boolean estHostile(ServerPlayerEntity souverain, Entity e) {
		if (!estCible(souverain, e)) {
			return false;
		}
		if (e instanceof HostileEntity || e instanceof PretendantEntity || e instanceof SoutienEntity) {
			return true;
		}
		return e instanceof MobEntity m && m.getTarget() != null && estAllie(souverain, m.getTarget());
	}

	public static List<LivingEntity> ciblesAutour(ServerPlayerEntity souverain, Vec3d centre, double rayon, boolean hostilesSeulement) {
		ServerWorld world = souverain.getServerWorld();
		Box zone = new Box(centre.x - rayon, centre.y - 2, centre.z - rayon, centre.x + rayon, centre.y + 3, centre.z + rayon);
		Predicate<Entity> filtre = hostilesSeulement ? e -> estHostile(souverain, e) : e -> estCible(souverain, e);
		return world.getEntitiesByClass(LivingEntity.class, zone, e -> filtre.test(e) && e.squaredDistanceTo(centre) <= rayon * rayon);
	}

	/** Alliés vivants autour d'un point, souverain inclus. */
	public static List<LivingEntity> alliesAutour(ServerPlayerEntity souverain, Vec3d centre, double rayon) {
		ServerWorld world = souverain.getServerWorld();
		Box zone = new Box(centre.x - rayon, centre.y - 2, centre.z - rayon, centre.x + rayon, centre.y + 3, centre.z + rayon);
		return world.getEntitiesByClass(LivingEntity.class, zone, e -> e.isAlive() && estAllie(souverain, e) && e.squaredDistanceTo(centre) <= rayon * rayon);
	}

	private Allies() {
	}
}
