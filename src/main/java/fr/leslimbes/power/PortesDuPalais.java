package fr.leslimbes.power;

import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
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
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Branche « Portes du Palais » : Pas court, porte directionnelle, attaque d'angle, sauvetage d'un allié et
 * liaison royale persistante.
 *
 * <p>Toute destination est vérifiée : espace libre pour le corps, chunk chargé, jamais au-dessus du vide (un
 * sol existe sous la destination dans les 6 blocs) et jamais dans un bloc. Si la destination est obstruée, on
 * recule le long du trajet jusqu'à une case valide.</p>
 */
public final class PortesDuPalais {
	public static final double PORTEE = 7.0;
	public static final double PORTEE_LONGUE = 14.0;
	private static final double PAS = 0.5;
	public static final String COMPTEUR_LIAISON = "liaison_posee";

	public static boolean pasCourt(ServerPlayerEntity player) {
		return pas(player, PORTEE, 0.5, 2, "porte_depart", "porte_arrivee");
	}

	/** Un pas long dans la direction du regard, qui franchit aussi murs et dénivelés (jusqu'à six blocs). */
	public static boolean porteDirectionnelle(ServerPlayerEntity player) {
		return pas(player, PORTEE_LONGUE, 0.9, 6, "porte_longue", "porte_arrivee");
	}

	private static boolean pas(ServerPlayerEntity player, double portee, double pente, int montee, String effetDepart, String effetArrivee) {
		ServerWorld world = player.getServerWorld();
		Vec3d direction = player.getRotationVector();
		Vec3d dir = new Vec3d(direction.x, Math.max(-pente, Math.min(pente, direction.y)), direction.z);
		if (dir.lengthSquared() < 1.0E-4) {
			dir = new Vec3d(0, 0, 1);
		}
		dir = dir.normalize();
		Vec3d depart = player.getPos();
		Vec3d meilleure = null;
		for (double d = portee; d >= 1.5; d -= PAS) {
			Vec3d candidate = depart.add(dir.multiply(d));
			Vec3d ajustee = positionSure(world, player, candidate, montee);
			if (ajustee != null) {
				meilleure = ajustee;
				break;
			}
		}
		if (meilleure == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		franchir(player, meilleure, dir, effetDepart, effetArrivee);
		return true;
	}

	/** Téléporte le joueur par une porte, avec les effets des deux côtés. */
	static void franchir(ServerPlayerEntity player, Vec3d destination, Vec3d dir, String effetDepart, String effetArrivee) {
		ServerWorld world = player.getServerWorld();
		Abilities.effet(world, effetDepart, player.getPos().add(0, 1.0, 0), dir, 0);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 1.1F);
		player.stopRiding();
		player.teleport(world, destination.x, destination.y, destination.z, player.getYaw(), player.getPitch());
		player.fallDistance = 0.0F;
		player.setVelocity(Vec3d.ZERO);
		player.velocityModified = true;
		Abilities.effet(world, effetArrivee, destination.add(0, 1.0, 0), dir, 0);
		world.playSound(null, BlockPos.ofFloored(destination), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 0.9F);
	}

	// --- Attaque d'angle ---------------------------------------------------------------------------

	/** Porte dans le dos de la cible visée, coup porté à l'arrivée ; le prochain coup d'épée frappe en arc. */
	public static boolean attaqueDAngle(ServerPlayerEntity player) {
		LivingEntity cible = ArsenalDuSouverain.viser(player, 16.0, e -> Allies.estCible(player, e));
		if (cible == null) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.aucune_cible").formatted(Formatting.RED), true);
			return false;
		}
		ServerWorld world = player.getServerWorld();
		Vec3d regard = new Vec3d(cible.getRotationVector().x, 0, cible.getRotationVector().z);
		if (regard.lengthSquared() < 1.0E-4) {
			regard = cible.getPos().subtract(player.getPos());
		}
		regard = regard.normalize();
		double recul = cible.getWidth() / 2.0 + 1.2;
		Vec3d destination = null;
		Vec3d[] essais = {cible.getPos().subtract(regard.multiply(recul)), cible.getPos().add(new Vec3d(-regard.z, 0, regard.x).multiply(recul)),
				cible.getPos().add(new Vec3d(regard.z, 0, -regard.x).multiply(recul)), cible.getPos().add(regard.multiply(recul))};
		for (Vec3d essai : essais) {
			destination = positionSure(world, player, essai, 2);
			if (destination != null) {
				break;
			}
		}
		if (destination == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		franchir(player, destination, regard, "porte_depart", "attaque_d_angle");
		player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, cible.getEyePos());
		float degats = 6.0F + fr.leslimbes.item.EpeeDuSouverainItem.affutage(player);
		cible.damage(world.getDamageSources().playerAttack(player), degats);
		Vec3d pousse = cible.getPos().subtract(player.getPos()).normalize();
		cible.takeKnockback(0.8, -pousse.x, -pousse.z);
		ArsenalDuSouverain.armerArc(player);
		world.playSound(null, cible.getBlockPos(), LimbesSounds.POUVOIR_EPEE_FINAL, SoundCategory.PLAYERS, 0.9F, 1.2F);
		return true;
	}

	// --- Sauvetage ---------------------------------------------------------------------------------

	/** Ramène un allié (visé, sinon le plus mal en point à 16 blocs) à vos côtés et le soigne. */
	public static boolean sauvetage(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		LivingEntity allie = ArsenalDuSouverain.viser(player, 24.0, e -> e != player && e instanceof LivingEntity && Allies.estAllie(player, e));
		if (allie == null) {
			List<LivingEntity> allies = Allies.alliesAutour(player, player.getPos(), 16.0);
			allies.remove(player);
			allie = allies.stream().filter(a -> a.getHealth() < a.getMaxHealth() || a instanceof ServerPlayerEntity)
					.min(Comparator.comparingDouble(a -> a.getHealth() / a.getMaxHealth())).orElse(null);
		}
		if (allie == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.aucun_allie").formatted(Formatting.RED), true);
			return false;
		}
		Vec3d lat = new Vec3d(-player.getRotationVector().z, 0, player.getRotationVector().x).normalize();
		Vec3d destination = null;
		for (Vec3d essai : new Vec3d[]{player.getPos().add(lat.multiply(1.5)), player.getPos().subtract(lat.multiply(1.5)),
				player.getPos().subtract(new Vec3d(player.getRotationVector().x, 0, player.getRotationVector().z).normalize().multiply(1.5)), player.getPos()}) {
			destination = positionSure(world, allie, essai, 2);
			if (destination != null) {
				break;
			}
		}
		if (destination == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		Abilities.effet(world, "porte_depart", allie.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
		world.playSound(null, allie.getBlockPos(), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 1.1F);
		allie.stopRiding();
		if (allie instanceof ServerPlayerEntity autre) {
			autre.teleport(world, destination.x, destination.y, destination.z, autre.getYaw(), autre.getPitch());
		} else {
			allie.refreshPositionAndAngles(destination.x, destination.y, destination.z, allie.getYaw(), allie.getPitch());
		}
		allie.fallDistance = 0.0F;
		allie.setVelocity(Vec3d.ZERO);
		allie.velocityModified = true;
		allie.heal(6.0F);
		allie.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0), player);
		Abilities.effet(world, "sauvetage", destination.add(0, 1.0, 0), Vec3d.ZERO, 0);
		world.playSound(null, BlockPos.ofFloored(destination), LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 0.9F, 0.9F);
		player.sendMessage(Text.translatable("message.les_limbes.portes.sauvetage", allie.getDisplayName()).formatted(Formatting.GOLD), true);
		return true;
	}

	// --- Liaison royale ----------------------------------------------------------------------------

	/**
	 * Accroupi : fixe un point de liaison à l'endroit où l'on se tient (Limbes seulement). Debout : revient au point fixé.
	 * Le point survit aux déconnexions (compteurs du joueur).
	 */
	public static boolean liaisonRoyale(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		PlayerLimbesData data = PlayerLimbesData.of(player);
		boolean posee = data.compteur(COMPTEUR_LIAISON) == 1;
		if (player.isSneaking() || !posee) {
			if (!LimbesDimension.estDansLesLimbes(world)) {
				player.sendMessage(Text.translatable("message.les_limbes.portes.liaison_limbes").formatted(Formatting.RED), true);
				return false;
			}
			BlockPos ici = player.getBlockPos();
			data.compteurs.put("liaison_x", ici.getX());
			data.compteurs.put("liaison_y", ici.getY());
			data.compteurs.put("liaison_z", ici.getZ());
			data.compteurs.put(COMPTEUR_LIAISON, 1);
			world.playSound(null, ici, LimbesSounds.POUVOIR_PAS, SoundCategory.PLAYERS, 1.0F, 0.6F);
			Abilities.effet(world, "liaison", player.getPos().add(0, 0.1, 0), player.getRotationVector(), 1);
			player.sendMessage(Text.translatable("message.les_limbes.portes.liaison_fixee", ici.getX(), ici.getY(), ici.getZ()).formatted(Formatting.GOLD), true);
			return true;
		}
		if (!LimbesDimension.estDansLesLimbes(world)) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.liaison_limbes").formatted(Formatting.RED), true);
			return false;
		}
		BlockPos ancre = new BlockPos(data.compteur("liaison_x"), data.compteur("liaison_y"), data.compteur("liaison_z"));
		if (!world.isChunkLoaded(ancre.getX() >> 4, ancre.getZ() >> 4)) {
			world.getChunk(ancre);
		}
		Vec3d destination = positionSure(world, player, Vec3d.ofBottomCenter(ancre), 3);
		if (destination == null) {
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		franchir(player, destination, player.getRotationVector(), "liaison", "porte_arrivee");
		player.sendMessage(Text.translatable("message.les_limbes.portes.liaison_voyage").formatted(Formatting.GOLD), true);
		return true;
	}

	// --- Vérification des destinations --------------------------------------------------------------

	/**
	 * Trouve une position valide proche de {@code candidate} pour {@code corps} : libre, chargée, avec un sol en dessous.
	 * On essaie à la hauteur demandée, puis jusqu'à {@code montee} blocs plus haut (marche, pente, muret), puis deux
	 * blocs plus bas.
	 */
	@Nullable
	public static Vec3d positionSure(ServerWorld world, LivingEntity corps, Vec3d candidate, int montee) {
		BlockPos bp = BlockPos.ofFloored(candidate);
		if (!world.isChunkLoaded(bp.getX() >> 4, bp.getZ() >> 4)) {
			return null;
		}
		for (int dy = 0; dy <= montee; dy++) {
			Vec3d p = new Vec3d(candidate.x, Math.floor(candidate.y) + dy, candidate.z);
			if (estLibre(world, corps, p) && solSous(world, p, 6)) {
				return p;
			}
		}
		for (int dy = -1; dy >= -2; dy--) {
			Vec3d p = new Vec3d(candidate.x, Math.floor(candidate.y) + dy, candidate.z);
			if (estLibre(world, corps, p) && solSous(world, p, 6)) {
				return p;
			}
		}
		return null;
	}

	private static boolean estLibre(ServerWorld world, LivingEntity corps, Vec3d p) {
		double w = corps.getWidth() / 2.0;
		Box box = new Box(p.x - w, p.y, p.z - w, p.x + w, p.y + corps.getHeight(), p.z + w);
		return world.isSpaceEmpty(corps, box) && !world.getFluidState(BlockPos.ofFloored(p)).isIn(net.minecraft.registry.tag.FluidTags.LAVA);
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

	/** Positions candidates autour d'un point (quatre côtés puis le point), pour placer un allié ou une garde. */
	@Nullable
	public static Vec3d autour(ServerWorld world, LivingEntity corps, Vec3d centre, double distance) {
		for (Vec3d d : new Vec3d[]{new Vec3d(distance, 0, 0), new Vec3d(-distance, 0, 0), new Vec3d(0, 0, distance), new Vec3d(0, 0, -distance), Vec3d.ZERO}) {
			Vec3d p = positionSure(world, corps, centre.add(d), 2);
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	static Set<net.minecraft.network.packet.s2c.play.PositionFlag> aucunDrapeau() {
		return Set.of();
	}

	private PortesDuPalais() {
	}
}
