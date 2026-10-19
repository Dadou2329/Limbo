package fr.leslimbes.power;

import fr.leslimbes.item.EpeeDuSouverainItem;
import fr.leslimbes.item.LanceDuSouverainItem;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Branche « Arsenal du Souverain » : les armes invoquées (épée, lance, arc, bouclier), la parade royale, la
 * charge de lance, la marque, le changement d'arme en combo et les armes satellites.
 *
 * <p>Les armes sont des objets invoqués : elles se dissipent si on les lâche et se rappellent avec leur pouvoir.
 * Le troisième coup d'un enchaînement rapide à l'épée frappe en arc autour de la cible.</p>
 */
public final class ArsenalDuSouverain {
	private static final int FENETRE_COMBO_TICKS = 30;
	private static final float DEGATS_ARC = 5.0F;
	private static final double RAYON_ARC = 3.0;
	private static final int PARADE_TICKS = 20;
	private static final int CHARGE_TICKS = 12;
	private static final float DEGATS_CHARGE = 7.0F;
	private static final int MARQUE_TICKS = 300;
	private static final int CHANGEMENT_TICKS = 40;
	private static final int SATELLITES_TICKS = 300;
	private static final double RAYON_SATELLITES = 2.2;
	private static final float DEGATS_SATELLITE = 3.0F;
	private static final Map<UUID, Combo> COMBOS = new HashMap<>();
	/** Garde contre la récursion : les dégâts bonus appliqués dans l'événement « après dégâts ». */
	private static boolean bonusEnCours;

	private record Combo(int etape, long dernierCoup) {
	}

	/** Ordre du changement d'arme ; chaque arme n'est disponible que si son pouvoir est débloqué. */
	private static final String[][] ARMES = {
			{"epee_du_souverain", "epee"}, {"lance_du_souverain", "lance"}, {"arc_du_souverain", "arc"}, {"bouclier_du_souverain", "bouclier"}};

	private static Item arme(String cle) {
		return switch (cle) {
			case "lance" -> LimbesItems.LANCE_DU_SOUVERAIN;
			case "arc" -> LimbesItems.ARC_DU_SOUVERAIN;
			case "bouclier" -> LimbesItems.BOUCLIER_DU_SOUVERAIN;
			default -> LimbesItems.EPEE_DU_SOUVERAIN;
		};
	}

	public static boolean estArmeRoyale(ItemStack stack) {
		return stack.isOf(LimbesItems.EPEE_DU_SOUVERAIN) || stack.isOf(LimbesItems.LANCE_DU_SOUVERAIN) || stack.isOf(LimbesItems.ARC_DU_SOUVERAIN) || stack.isOf(LimbesItems.BOUCLIER_DU_SOUVERAIN);
	}

	// --- Invocations -------------------------------------------------------------------------------

	public static boolean invoquerOuRappeler(ServerPlayerEntity player) {
		return invoquerOuRappeler(player, LimbesItems.EPEE_DU_SOUVERAIN, "epee", LimbesSounds.POUVOIR_EPEE);
	}

	public static boolean invoquerLance(ServerPlayerEntity player) {
		return invoquerOuRappeler(player, LimbesItems.LANCE_DU_SOUVERAIN, "lance", LimbesSounds.POUVOIR_LANCE);
	}

	public static boolean invoquerArc(ServerPlayerEntity player) {
		return invoquerOuRappeler(player, LimbesItems.ARC_DU_SOUVERAIN, "arc", LimbesSounds.POUVOIR_ARC);
	}

	public static boolean invoquerBouclier(ServerPlayerEntity player) {
		return invoquerOuRappeler(player, LimbesItems.BOUCLIER_DU_SOUVERAIN, "bouclier", LimbesSounds.POUVOIR_EPEE);
	}

	private static boolean invoquerOuRappeler(ServerPlayerEntity player, Item arme, String cle, SoundEvent son) {
		ServerWorld world = player.getServerWorld();
		int slot = trouver(player, arme);
		if (slot >= 0) {
			player.getInventory().removeStack(slot);
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.rappel").formatted(Formatting.AQUA), true);
			world.playSound(null, player.getBlockPos(), son, SoundCategory.PLAYERS, 0.8F, 0.7F);
			Abilities.effet(world, "epee_rappel", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
			return true;
		}
		if (!placerEnMain(player, creer(player, arme))) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.inventaire_plein").formatted(Formatting.RED), true);
			return false;
		}
		player.sendMessage(Text.translatable("message.les_limbes.arsenal.invocation_" + cle).formatted(Formatting.GOLD), true);
		world.playSound(null, player.getBlockPos(), son, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, cle.equals("epee") ? "epee_invocation" : cle + "_invocation", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
		return true;
	}

	/** Crée l'exemplaire d'une arme royale pour ce joueur (affûtage de la forge appliqué à l'épée et à la lance). */
	public static ItemStack creer(ServerPlayerEntity player, Item arme) {
		ItemStack stack = new ItemStack(arme);
		int affutage = EpeeDuSouverainItem.affutage(player);
		if (affutage > 0) {
			if (arme == LimbesItems.EPEE_DU_SOUVERAIN) {
				EpeeDuSouverainItem.appliquerAffutage(stack, affutage);
			} else if (arme == LimbesItems.LANCE_DU_SOUVERAIN) {
				LanceDuSouverainItem.appliquerAffutage(stack, affutage);
			}
		}
		return stack;
	}

	private static boolean placerEnMain(ServerPlayerEntity player, ItemStack stack) {
		int main = player.getInventory().selectedSlot;
		ItemStack enMain = player.getInventory().getStack(main);
		if (enMain.isEmpty()) {
			player.getInventory().setStack(main, stack);
			return true;
		}
		return player.getInventory().insertStack(stack);
	}

	private static int trouver(PlayerEntity player, Item arme) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(arme)) {
				return i;
			}
		}
		return -1;
	}

	// --- Parade ------------------------------------------------------------------------------------

	/** Une fenêtre d'un quart de seconde pendant laquelle tout coup reçu est renvoyé. */
	public static boolean parade(ServerPlayerEntity player) {
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		ServerWorld world = player.getServerWorld();
		etat.paradeJusqua = world.getTime() + PARADE_TICKS;
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PARADE, SoundCategory.PLAYERS, 0.9F, 1.2F);
		Abilities.effet(world, "parade", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
		return true;
	}

	/**
	 * Événement « dégâts autorisés » : parade réussie (coup annulé, assaillant repoussé et ralenti, majesté rendue),
	 * projectiles dissipés pendant la fenêtre.
	 */
	public static boolean permettreDegats(LivingEntity entite, DamageSource source, float montant) {
		if (!(entite instanceof ServerPlayerEntity player)) {
			return true;
		}
		EtatPouvoirs etat = EtatPouvoirs.existant(player.getUuid());
		if (etat == null) {
			return true;
		}
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();
		if (!etat.paradeActive(now) || source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return true;
		}
		Entity attaquant = source.getAttacker();
		Entity projectile = source.getSource();
		boolean paree = false;
		if (projectile instanceof ProjectileEntity p && p.isAlive()) {
			p.discard();
			paree = true;
		}
		if (attaquant instanceof LivingEntity vivant && vivant != player && vivant.isAlive() && vivant.squaredDistanceTo(player) <= 36.0) {
			Vec3d dir = vivant.getPos().subtract(player.getPos());
			if (dir.lengthSquared() < 1.0E-4) {
				dir = player.getRotationVector();
			}
			dir = dir.normalize();
			vivant.takeKnockback(1.2, -dir.x, -dir.z);
			vivant.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1), player);
			if (vivant instanceof fr.leslimbes.entity.region.EnnemiAnnonceEntity e) {
				e.interrompre();
			} else if (vivant instanceof fr.leslimbes.entity.VeilleurEgareEntity v) {
				v.interrompre();
			}
			paree = true;
		}
		if (!paree) {
			return true;
		}
		etat.paradeJusqua = 0L;
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.regenerer(10.0F);
		data.incrementerCompteur("parades_reussies");
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PARADE, SoundCategory.PLAYERS, 1.0F, 0.8F);
		Abilities.effet(world, "parade_reussie", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 1);
		player.sendMessage(Text.translatable("message.les_limbes.arsenal.parade_reussie").formatted(Formatting.GOLD), true);
		return false;
	}

	/**
	 * Événement « après dégâts » : dégâts supplémentaires du souverain sur une cible marquée, bonus du changement
	 * d'arme et de l'Avènement ; rempart du bouclier quand un coup est bloqué.
	 */
	public static void apresDegats(LivingEntity entite, DamageSource source, float base, float pris, boolean bloque) {
		if (bonusEnCours) {
			return;
		}
		if (bloque && entite instanceof ServerPlayerEntity player && player.getActiveItem().isOf(LimbesItems.BOUCLIER_DU_SOUVERAIN)
				&& source.getAttacker() instanceof LivingEntity assaillant && assaillant != player) {
			Vec3d dir = assaillant.getPos().subtract(player.getPos()).normalize();
			assaillant.takeKnockback(1.5, -dir.x, -dir.z);
			assaillant.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 0), player);
			ServerWorld world = player.getServerWorld();
			world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PARADE, SoundCategory.PLAYERS, 0.8F, 0.6F);
			Abilities.effet(world, "bouclier_rempart", player.getPos().add(0, 1.0, 0), dir, 0);
			return;
		}
		if (!(source.getAttacker() instanceof ServerPlayerEntity player) || entite == player || !entite.isAlive()) {
			return;
		}
		EtatPouvoirs etat = EtatPouvoirs.existant(player.getUuid());
		if (etat == null) {
			return;
		}
		long now = player.getServerWorld().getTime();
		float bonus = 0.0F;
		if (etat.marque != null && etat.marque.equals(entite.getUuid()) && now < etat.marqueJusqua) {
			bonus += Math.max(1.0F, base * 0.5F);
		}
		if (now < etat.changementJusqua) {
			bonus += 3.0F;
			etat.changementJusqua = 0L;
		}
		if (etat.avenementActif(now)) {
			bonus += Math.max(1.0F, base * 0.5F);
		}
		if (bonus <= 0.0F) {
			return;
		}
		bonusEnCours = true;
		try {
			// Pendant la fenêtre d'invulnérabilité, seul l'excédent par rapport au dernier coup est appliqué : on l'exploite.
			entite.damage(source, base + bonus);
		} finally {
			bonusEnCours = false;
		}
		Abilities.effet(player.getServerWorld(), "coup_bonus", entite.getPos().add(0, entite.getHeight() * 0.6, 0), Vec3d.ZERO, (int) bonus);
	}

	// --- Charge de lance ---------------------------------------------------------------------------

	public static boolean charge(ServerPlayerEntity player) {
		if (!player.getMainHandStack().isOf(LimbesItems.LANCE_DU_SOUVERAIN)) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.lance_requise").formatted(Formatting.RED), true);
			return false;
		}
		ServerWorld world = player.getServerWorld();
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		Vec3d dir = new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		player.setVelocity(dir.multiply(2.0).add(0, 0.15, 0));
		player.velocityModified = true;
		etat.chargeJusqua = world.getTime() + CHARGE_TICKS;
		etat.chargeTouches.clear();
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_LANCE, SoundCategory.PLAYERS, 1.0F, 0.8F);
		Abilities.effet(world, "charge", player.getPos().add(0, 1.0, 0), dir, CHARGE_TICKS);
		return true;
	}

	// --- Marque royale -----------------------------------------------------------------------------

	public static boolean marquer(ServerPlayerEntity player) {
		LivingEntity cible = viser(player, 24.0, e -> Allies.estCible(player, e));
		if (cible == null) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.aucune_cible").formatted(Formatting.RED), true);
			return false;
		}
		ServerWorld world = player.getServerWorld();
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		etat.marque = cible.getUuid();
		etat.marqueJusqua = world.getTime() + MARQUE_TICKS;
		cible.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, MARQUE_TICKS, 0, false, false), player);
		world.playSound(null, cible.getBlockPos(), LimbesSounds.POUVOIR_EPEE_FINAL, SoundCategory.PLAYERS, 0.8F, 1.4F);
		Abilities.effet(world, "marque", cible.getPos().add(0, cible.getHeight() + 0.5, 0), Vec3d.ZERO, MARQUE_TICKS);
		player.sendMessage(Text.translatable("message.les_limbes.arsenal.marque", cible.getDisplayName()).formatted(Formatting.GOLD), true);
		return true;
	}

	// --- Changement d'arme -------------------------------------------------------------------------

	public static boolean changerDArme(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		List<String> disponibles = new ArrayList<>();
		for (String[] a : ARMES) {
			if (data.possedePouvoir(a[0])) {
				disponibles.add(a[1]);
			}
		}
		if (disponibles.size() < 2) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.une_seule_arme").formatted(Formatting.RED), true);
			return false;
		}
		ItemStack enMain = player.getMainHandStack();
		int index = -1;
		for (int i = 0; i < disponibles.size(); i++) {
			if (enMain.isOf(arme(disponibles.get(i)))) {
				index = i;
			}
		}
		String suivante = disponibles.get((index + 1) % disponibles.size());
		ServerWorld world = player.getServerWorld();
		// L'arme en main se dissipe, la suivante prend sa place ; les exemplaires rangés ailleurs se dissipent aussi.
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (estArmeRoyale(player.getInventory().getStack(i)) && !player.getInventory().getStack(i).isOf(LimbesItems.BOUCLIER_DU_SOUVERAIN) || player.getInventory().getStack(i).isOf(LimbesItems.BOUCLIER_DU_SOUVERAIN) && index >= 0) {
				player.getInventory().removeStack(i);
			}
		}
		player.getInventory().setStack(player.getInventory().selectedSlot, creer(player, arme(suivante)));
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		etat.changementJusqua = world.getTime() + CHANGEMENT_TICKS;
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_EPEE, SoundCategory.PLAYERS, 0.7F, 1.5F);
		Abilities.effet(world, "changement", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
		return true;
	}

	// --- Armes satellites --------------------------------------------------------------------------

	public static boolean satellites(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		etat.satellitesJusqua = world.getTime() + SATELLITES_TICKS;
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_EPEE, SoundCategory.PLAYERS, 1.0F, 1.2F);
		Abilities.effet(world, "epee_invocation", player.getPos().add(0, 1.2, 0), player.getRotationVector(), 3);
		return true;
	}

	/** Effets continus : charge en cours, lames satellites. */
	static void tick(ServerPlayerEntity player, EtatPouvoirs etat, long now) {
		ServerWorld world = player.getServerWorld();
		if (etat.chargeActive(now)) {
			for (LivingEntity e : Allies.ciblesAutour(player, player.getPos().add(0, 0.5, 0), 1.8, false)) {
				if (etat.chargeTouches.add(e.getUuid())) {
					e.damage(world.getDamageSources().playerAttack(player), DEGATS_CHARGE);
					Vec3d dir = e.getPos().subtract(player.getPos()).normalize();
					e.takeKnockback(1.0, -dir.x, -dir.z);
					Abilities.effet(world, "charge_impact", e.getPos().add(0, e.getHeight() * 0.5, 0), dir, 0);
				}
			}
		}
		if (etat.satellitesActifs(now)) {
			etat.satellitesAngle += 0.16;
			if (now % 2L == 0L) {
				Abilities.effet(world, "satellites", player.getPos().add(0, 1.2, 0), new Vec3d(etat.satellitesAngle, 0, 0), 3);
			}
			if (now % 10L == 0L) {
				for (int k = 0; k < 3; k++) {
					double a = etat.satellitesAngle + k * Math.PI * 2 / 3;
					Vec3d lame = player.getPos().add(Math.cos(a) * RAYON_SATELLITES, 1.0, Math.sin(a) * RAYON_SATELLITES);
					for (LivingEntity e : Allies.ciblesAutour(player, lame, 1.5, true)) {
						e.damage(world.getDamageSources().playerAttack(player), DEGATS_SATELLITE);
						Vec3d dir = e.getPos().subtract(player.getPos()).normalize();
						e.takeKnockback(0.3, -dir.x, -dir.z);
					}
				}
			}
		}
	}

	// --- Coups à l'épée -----------------------------------------------------------------------------

	/** Appelé lorsqu'un joueur frappe une entité (serveur) : enchaînement à trois coups de l'épée. */
	public static void onCoup(ServerPlayerEntity player, Entity cible) {
		if (!player.getMainHandStack().isOf(LimbesItems.EPEE_DU_SOUVERAIN) || !(cible instanceof LivingEntity vivant)) {
			return;
		}
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();
		Combo combo = COMBOS.get(player.getUuid());
		int etape = (combo == null || now - combo.dernierCoup() > FENETRE_COMBO_TICKS) ? 1 : combo.etape() + 1;
		if (etape >= 3) {
			etape = 0;
			frapperEnArc(player, vivant, world);
		}
		COMBOS.put(player.getUuid(), new Combo(etape, now));
	}

	/** Après une attaque d'angle, le prochain coup d'épée est le troisième de l'enchaînement. */
	public static void armerArc(ServerPlayerEntity player) {
		COMBOS.put(player.getUuid(), new Combo(2, player.getServerWorld().getTime()));
	}

	private static void frapperEnArc(ServerPlayerEntity player, LivingEntity cible, ServerWorld world) {
		Vec3d centre = cible.getPos();
		Box zone = new Box(centre.x - RAYON_ARC, centre.y - 1, centre.z - RAYON_ARC, centre.x + RAYON_ARC, centre.y + 2, centre.z + RAYON_ARC);
		List<LivingEntity> touches = world.getEntitiesByClass(LivingEntity.class, zone, e -> Allies.estCible(player, e) && e.canTakeDamage());
		for (LivingEntity e : touches) {
			e.damage(world.getDamageSources().playerAttack(player), DEGATS_ARC);
			Vec3d pousse = e.getPos().subtract(player.getPos()).normalize();
			e.takeKnockback(0.6, -pousse.x, -pousse.z);
		}
		world.playSound(null, cible.getBlockPos(), LimbesSounds.POUVOIR_EPEE_FINAL, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "epee_arc", player.getPos().add(0, 1.2, 0), player.getRotationVector(), touches.size());
	}

	/** Une arme invoquée lâchée au sol se dissipe. */
	public static boolean dissiperSiInvoquee(Entity entity) {
		if (entity instanceof ItemEntity item && estArmeRoyale(item.getStack())) {
			if (entity.getWorld() instanceof ServerWorld world) {
				Abilities.effet(world, "epee_rappel", entity.getPos(), Vec3d.ZERO, 0);
			}
			entity.discard();
			return true;
		}
		return false;
	}

	/** L'entité vivante visée par le regard du joueur dans la portée donnée, ou null. */
	@Nullable
	public static LivingEntity viser(ServerPlayerEntity player, double portee, Predicate<Entity> filtre) {
		Vec3d depart = player.getEyePos();
		Vec3d fin = depart.add(player.getRotationVector().multiply(portee));
		Box boite = player.getBoundingBox().stretch(player.getRotationVector().multiply(portee)).expand(1.0);
		EntityHitResult hit = ProjectileUtil.raycast(player, depart, fin, boite, e -> e instanceof LivingEntity && filtre.test(e), portee * portee);
		return hit != null && hit.getEntity() instanceof LivingEntity v ? v : null;
	}

	public static void onDeconnexion(UUID uuid) {
		COMBOS.remove(uuid);
	}

	private ArsenalDuSouverain() {
	}
}
