package fr.leslimbes.power;

import fr.leslimbes.registry.LimbesAttachments;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Techniques emblématiques : le Jugement des constellations (sept étoiles tombent sur les ennemis), la Marche du
 * Palais (les Portes deviennent libres), l'Audience du Roi (tout ce qui menace est traîné devant le souverain) et
 * l'Avènement (la royauté se révèle pleinement pendant trente secondes).
 */
public final class Emblematiques {
	public static final int ETOILES = 7;
	public static final int DUREE_MARCHE = 200;
	public static final int DUREE_AVENEMENT = 600;

	public static boolean jugementDesConstellations(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<LivingEntity> cibles = Allies.ciblesAutour(player, player.getPos(), 12.0, true);
		if (cibles.isEmpty()) {
			cibles = Allies.ciblesAutour(player, player.getPos(), 12.0, false).stream().filter(e -> e instanceof MobEntity m && m.getTarget() != null).toList();
		}
		if (cibles.isEmpty()) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.aucune_cible").formatted(Formatting.RED), true);
			return false;
		}
		cibles.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		long now = world.getTime();
		for (int i = 0; i < ETOILES; i++) {
			LivingEntity cible = cibles.get(i % cibles.size());
			etat.etoiles.add(new EtatPouvoirs.Etoile(now + 12 + i * 8L, cible.getUuid()));
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_CONSTELLATION, SoundCategory.PLAYERS, 1.2F, 1.0F);
		Abilities.effet(world, "constellation", player.getPos().add(0, 1.0, 0), player.getRotationVector(), Math.min(ETOILES, cibles.size()));
		return true;
	}

	public static boolean marcheDuPalais(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		etat.marcheJusqua = world.getTime() + DUREE_MARCHE;
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DUREE_MARCHE, 1, false, false), player);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 1.2F, 0.7F);
		Abilities.effet(world, "marche_du_palais", player.getPos().add(0, 1.0, 0), player.getRotationVector(), DUREE_MARCHE);
		player.sendMessage(Text.translatable("message.les_limbes.emblematique.marche").formatted(Formatting.GOLD), true);
		return true;
	}

	/** Tout ce qui menace à seize blocs est traîné en demi-cercle devant le souverain, immobile et affaibli ; les alliés sont soignés. */
	public static boolean audienceDuRoi(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		List<LivingEntity> cibles = Allies.ciblesAutour(player, player.getPos(), 16.0, false);
		Vec3d dir = new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		double base = Math.atan2(dir.z, dir.x);
		int n = cibles.size();
		int place = 0;
		for (LivingEntity e : cibles) {
			double angle = n == 1 ? base : base - Math.PI / 2 + Math.PI * place / Math.max(1, n - 1);
			Vec3d cible = player.getPos().add(Math.cos(angle) * 4.5, 0, Math.sin(angle) * 4.5);
			Vec3d sure = PortesDuPalais.positionSure(world, e, cible, 3);
			if (sure != null) {
				Abilities.effet(world, "porte_depart", e.getPos().add(0, 1.0, 0), dir, 0);
				e.refreshPositionAndAngles(sure.x, sure.y, sure.z, e.getYaw(), e.getPitch());
				e.setVelocity(Vec3d.ZERO);
				e.velocityModified = true;
				e.fallDistance = 0.0F;
			}
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 6), player);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1), player);
			if (Allies.estHostile(player, e)) {
				e.damage(world.getDamageSources().indirectMagic(player, player), Math.max(2.0F, e.getHealth() * 0.3F));
			}
			if (e instanceof MobEntity mob) {
				mob.getNavigation().stop();
			}
			place++;
		}
		for (LivingEntity a : Allies.alliesAutour(player, player.getPos(), 16.0)) {
			a.heal(8.0F);
		}
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_DECRET, SoundCategory.PLAYERS, 1.4F, 0.5F);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_CONSTELLATION, SoundCategory.PLAYERS, 1.0F, 0.8F);
		Abilities.effet(world, "audience", player.getPos().add(0, 0.2, 0), dir, n);
		return true;
	}

	/** La royauté se révèle : couronne et manteau visibles de tous, majesté quadruplée, techniques plus rapides, lames satellites. */
	public static boolean avenement(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (!data.aDrapeau("couronne")) {
			player.sendMessage(Text.translatable("message.les_limbes.emblematique.avenement_couronne").formatted(Formatting.RED), true);
			return false;
		}
		ServerWorld world = player.getServerWorld();
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		long now = world.getTime();
		etat.avenementJusqua = now + DUREE_AVENEMENT;
		etat.avenementPublie = true;
		etat.satellitesJusqua = Math.max(etat.satellitesJusqua, now + DUREE_AVENEMENT);
		LimbesAttachments.publier(player);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, DUREE_AVENEMENT, 1, false, false), player);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DUREE_AVENEMENT, 0, false, false), player);
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, DUREE_AVENEMENT, 0, false, false), player);
		data.regenerer(data.majesteMax);
		LimbesPortal.titre(player, Text.translatable("message.les_limbes.emblematique.avenement_titre").formatted(Formatting.GOLD),
				Text.translatable("message.les_limbes.emblematique.avenement_sous_titre"));
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_AVENEMENT, SoundCategory.PLAYERS, 1.5F, 1.0F);
		Abilities.effet(world, "avenement", player.getPos().add(0, 1.0, 0), player.getRotationVector(), DUREE_AVENEMENT);
		Abilities.effet(world, "arrivee_ciel", player.getPos().add(0, 6, 0), Vec3d.ZERO, 0);
		return true;
	}

	/** Effets continus : chute des étoiles, fin de l'Avènement. */
	static void tick(ServerPlayerEntity player, EtatPouvoirs etat, long now) {
		ServerWorld world = player.getServerWorld();
		if (!etat.etoiles.isEmpty()) {
			Iterator<EtatPouvoirs.Etoile> it = etat.etoiles.iterator();
			while (it.hasNext()) {
				EtatPouvoirs.Etoile etoile = it.next();
				if (now < etoile.tick()) {
					continue;
				}
				it.remove();
				Entity e = world.getEntity(etoile.cible());
				if (!(e instanceof LivingEntity vivant) || !vivant.isAlive()) {
					continue;
				}
				vivant.damage(world.getDamageSources().indirectMagic(player, player), 7.0F);
				vivant.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, false, false), player);
				vivant.setVelocity(vivant.getVelocity().add(0, 0.3, 0));
				vivant.velocityModified = true;
				world.playSound(null, vivant.getBlockPos(), LimbesSounds.POUVOIR_CONSTELLATION, SoundCategory.PLAYERS, 0.8F, 1.2F + world.getRandom().nextFloat() * 0.4F);
				Abilities.effet(world, "constellation_etoile", vivant.getPos().add(0, vivant.getHeight() * 0.5, 0), Vec3d.ZERO, 0);
			}
		}
		if (etat.avenementPublie && !etat.avenementActif(now)) {
			etat.avenementPublie = false;
			LimbesAttachments.publier(player);
			Abilities.effet(world, "avenement_fin", player.getPos().add(0, 1.0, 0), Vec3d.ZERO, 0);
			player.sendMessage(Text.translatable("message.les_limbes.emblematique.avenement_fin").formatted(Formatting.GRAY), true);
		}
	}

	static BlockPos sol(ServerPlayerEntity player) {
		return player.getBlockPos();
	}

	private Emblematiques() {
	}
}
