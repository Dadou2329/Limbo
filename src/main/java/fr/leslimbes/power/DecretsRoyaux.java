package fr.leslimbes.power;

import fr.leslimbes.entity.VeilleurEgareEntity;
import fr.leslimbes.entity.region.EclatProjectileEntity;
import fr.leslimbes.entity.region.EnnemiAnnonceEntity;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Branche « Décrets royaux » : la parole du souverain s'impose à ce qui l'entoure. « Reculez » repousse,
 * « Déposez les armes » désarme, « Tenez bon » protège les alliés, « Que le chemin s'ouvre » jette un pont de
 * lumière, « Restez » immobilise, « Élevez-vous » soulève les alliés.
 */
public final class DecretsRoyaux {
	private static final double RAYON = 6.0;
	private static final float DEGATS = 2.0F;
	public static final int DUREE_PONT = 400;
	public static final int LONGUEUR_PONT = 14;

	public static boolean reculez(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d centre = player.getPos();
		int repousses = 0;
		for (LivingEntity e : Allies.ciblesAutour(player, centre, RAYON, false)) {
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

	/** Les assaillants lâchent prise : plus de cible, coups annoncés interrompus, faiblesse, projectiles dissipés. */
	public static boolean deposezLesArmes(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d centre = player.getPos();
		int desarmes = 0;
		for (LivingEntity e : Allies.ciblesAutour(player, centre, 10.0, false)) {
			if (e instanceof MobEntity mob) {
				mob.setTarget(null);
				mob.setAttacking(false);
			}
			if (e instanceof EnnemiAnnonceEntity a) {
				a.interrompre();
			} else if (e instanceof VeilleurEgareEntity v) {
				v.interrompre();
			}
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 160, 2), player);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1), player);
			desarmes++;
		}
		Box zone = new Box(centre.x - 10, centre.y - 4, centre.z - 10, centre.x + 10, centre.y + 6, centre.z + 10);
		for (EclatProjectileEntity eclat : world.getEntitiesByClass(EclatProjectileEntity.class, zone, p -> p.isAlive() && p.getOwner() != player)) {
			eclat.discard();
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 0.8F);
		Abilities.effet(world, "decret_deposez", centre.add(0, 1.0, 0), player.getRotationVector(), desarmes);
		player.sendMessage(Text.translatable("message.les_limbes.decret.deposez", desarmes).formatted(Formatting.GOLD), true);
		return true;
	}

	/** Les alliés proches (vous compris) tiennent : résistance, régénération, absorption. */
	public static boolean tenezBon(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<LivingEntity> allies = Allies.alliesAutour(player, player.getPos(), 10.0);
		for (LivingEntity a : allies) {
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 1), player);
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 240, 0), player);
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 240, 0), player);
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 1.2F);
		Abilities.effet(world, "decret_tenez_bon", player.getPos().add(0, 0.2, 0), player.getRotationVector(), allies.size());
		return true;
	}

	/** Un pont de lumière se déroule devant vous sur quatorze blocs ; il s'efface au bout de vingt secondes. */
	public static boolean queLeCheminSOuvre(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d dir = new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		int y = (int) Math.floor(player.getY()) - 1;
		Vec3d lat = new Vec3d(-dir.z, 0, dir.x);
		int poses = 0;
		for (int d = 1; d <= LONGUEUR_PONT; d++) {
			Vec3d p = player.getPos().add(dir.multiply(d));
			for (double cote : new double[]{-0.5, 0.5}) {
				BlockPos bp = BlockPos.ofFloored(p.x + lat.x * cote, y, p.z + lat.z * cote);
				if (poserLumiere(world, bp)) {
					poses++;
				}
			}
		}
		if (poses == 0) {
			player.sendMessage(Text.translatable("message.les_limbes.decret.chemin_ferme").formatted(Formatting.RED), true);
			return false;
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 1.4F);
		Abilities.effet(world, "decret_chemin", player.getPos().add(0, 0.1, 0), dir, LONGUEUR_PONT);
		return true;
	}

	/** Pose un bloc de pont de lumière s'il y a de la place (air, eau, plantes) ; sa disparition est planifiée. */
	public static boolean poserLumiere(ServerWorld world, BlockPos pos) {
		if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4) || pos.getY() < world.getBottomY() + 1) {
			return false;
		}
		BlockState actuel = world.getBlockState(pos);
		if (!actuel.isAir() && !actuel.isReplaceable()) {
			return false;
		}
		if (!world.getEntitiesByClass(LivingEntity.class, new Box(pos), e -> true).isEmpty()) {
			return false;
		}
		world.setBlockState(pos, LimbesBlocks.PONT_DE_LUMIERE.getDefaultState(), 3);
		world.scheduleBlockTick(pos, LimbesBlocks.PONT_DE_LUMIERE, DUREE_PONT);
		return true;
	}

	/** Les créatures alentour restent clouées sur place quelques secondes. */
	public static boolean restez(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<LivingEntity> cibles = Allies.ciblesAutour(player, player.getPos(), 8.0, false);
		for (LivingEntity e : cibles) {
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 6), player);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1), player);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0, false, false), player);
			if (e instanceof MobEntity mob) {
				mob.getNavigation().stop();
				mob.setVelocity(Vec3d.ZERO);
				mob.velocityModified = true;
			}
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 0.6F);
		Abilities.effet(world, "decret_restez", player.getPos().add(0, 0.2, 0), player.getRotationVector(), cibles.size());
		return true;
	}

	/** Vous et vos alliés proches êtes soulevés, puis portés par une chute lente. */
	public static boolean elevezVous(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<LivingEntity> allies = Allies.alliesAutour(player, player.getPos(), 6.0);
		for (LivingEntity a : allies) {
			a.setVelocity(a.getVelocity().x, 1.25, a.getVelocity().z);
			a.velocityModified = true;
			a.fallDistance = 0.0F;
			a.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 160, 0), player);
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.0F, 1.6F);
		Abilities.effet(world, "decret_elevez", player.getPos().add(0, 0.2, 0), player.getRotationVector(), allies.size());
		return true;
	}

	private DecretsRoyaux() {
	}
}
