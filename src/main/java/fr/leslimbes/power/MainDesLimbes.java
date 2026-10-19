package fr.leslimbes.power;

import fr.leslimbes.duel.Duel;
import fr.leslimbes.duel.DuelManager;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * Branche « Main des Limbes » : une main de lumière prolonge le geste du souverain. Elle saisit une créature ou
 * un objet, soulève le souverain par-dessus un obstacle, et, couronnée, frappe tout ce qui lui fait face.
 */
public final class MainDesLimbes {
	public static boolean saisir(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		LivingEntity cible = ArsenalDuSouverain.viser(player, 16.0, e -> Allies.estCible(player, e));
		if (cible != null) {
			Vec3d vers = player.getEyePos().subtract(cible.getPos());
			double distance = vers.length();
			Vec3d v = vers.normalize().multiply(Math.min(1.8, 0.4 + distance * 0.12)).add(0, 0.25, 0);
			cible.setVelocity(v);
			cible.velocityModified = true;
			cible.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1), player);
			world.playSound(null, cible.getBlockPos(), LimbesSounds.POUVOIR_MAIN, SoundCategory.PLAYERS, 1.0F, 1.0F);
			Abilities.effet(world, "main_saisir", cible.getPos().add(0, cible.getHeight() * 0.5, 0), vers.normalize(), 0);
			return true;
		}
		List<ItemEntity> objets = world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(10.0), i -> i.isAlive() && !i.cannotPickup());
		if (objets.isEmpty()) {
			player.sendMessage(Text.translatable("message.les_limbes.main.rien_a_saisir").formatted(Formatting.RED), true);
			return false;
		}
		objets.sort(Comparator.comparingDouble(i -> i.squaredDistanceTo(player)));
		int pris = 0;
		for (ItemEntity objet : objets) {
			if (pris >= 8) {
				break;
			}
			Abilities.effet(world, "main_saisir", objet.getPos(), player.getPos().subtract(objet.getPos()).normalize(), 1);
			objet.setPosition(player.getX(), player.getY() + 0.5, player.getZ());
			objet.setVelocity(Vec3d.ZERO);
			objet.setPickupDelay(0);
			pris++;
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_MAIN, SoundCategory.PLAYERS, 0.8F, 1.3F);
		return true;
	}

	/** La main vous dépose sur l'obstacle qui vous fait face (jusqu'à sept blocs de haut, quatre blocs devant). */
	public static boolean passage(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d dir = new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		int yPieds = (int) Math.floor(player.getY());
		for (int d = 1; d <= 4; d++) {
			Vec3d devant = player.getPos().add(dir.multiply(d));
			BlockPos colonne = BlockPos.ofFloored(devant.x, yPieds, devant.z);
			BlockState obstacle = world.getBlockState(colonne);
			if (obstacle.getCollisionShape(world, colonne).isEmpty()) {
				continue;
			}
			for (int h = 1; h <= 7; h++) {
				Vec3d dessus = new Vec3d(devant.x, yPieds + h, devant.z);
				Vec3d sure = PortesDuPalais.positionSure(world, player, dessus, 0);
				if (sure != null) {
					world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_MAIN, SoundCategory.PLAYERS, 1.0F, 0.9F);
					Abilities.effet(world, "main_passage", player.getPos().add(0, 0.5, 0), dir, h);
					player.stopRiding();
					player.teleport(world, sure.x, sure.y, sure.z, player.getYaw(), player.getPitch());
					player.fallDistance = 0.0F;
					player.setVelocity(Vec3d.ZERO);
					player.velocityModified = true;
					Abilities.effet(world, "porte_arrivee", sure.add(0, 1.0, 0), dir, 0);
					return true;
				}
			}
			break;
		}
		player.sendMessage(Text.translatable("message.les_limbes.main.aucun_passage").formatted(Formatting.RED), true);
		return false;
	}

	/** La main couronnée s'abat devant vous : dégâts, étourdissement, blocs d'arène brisés. */
	public static boolean couronnee(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		Vec3d dir = new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		Vec3d impact = player.getPos().add(dir.multiply(3.0));
		int touches = 0;
		for (LivingEntity e : Allies.ciblesAutour(player, player.getPos(), 7.0, true)) {
			Vec3d vers = e.getPos().subtract(player.getPos());
			if (vers.lengthSquared() > 0.01 && vers.normalize().dotProduct(dir) < 0.3) {
				continue;
			}
			e.damage(world.getDamageSources().playerAttack(player), 8.0F);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4), player);
			Vec3d pousse = vers.normalize();
			e.takeKnockback(1.2, -pousse.x, -pousse.z);
			touches++;
		}
		Duel duel = DuelManager.duelDuParticipant(player.getUuid());
		int brises = duel == null ? 0 : duel.blocs.retirerAutour(world, BlockPos.ofFloored(impact), 3);
		world.playSound(null, BlockPos.ofFloored(impact), LimbesSounds.POUVOIR_MAIN, SoundCategory.PLAYERS, 1.2F, 0.6F);
		Abilities.effet(world, "main_couronnee", impact.add(0, 0.2, 0), dir, touches + brises);
		return true;
	}

	static Box devant(ServerPlayerEntity player, Vec3d dir, double portee) {
		Vec3d a = player.getPos();
		Vec3d b = a.add(dir.multiply(portee));
		return new Box(a, b).expand(2.0, 2.0, 2.0);
	}

	private MainDesLimbes() {
	}
}
