package fr.leslimbes.power;

import fr.leslimbes.entity.region.GardeAstraleEntity;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * Branche « Garde astrale » : des gardes de lumière (chevalier, archer, protecteur) répondent au souverain
 * pendant une minute. Au plus {@value #BUDGET} gardes à la fois : la plus ancienne se dissipe pour laisser place
 * à la nouvelle. « Ordre de garde » fait passer toute la garde d'un ordre au suivant : suivre, tenir, attaquer.
 */
public final class GardeAstrale {
	public static final int BUDGET = 3;
	public static final int DUREE_TICKS = 1200;
	public static final String COMPTEUR_ORDRE = "ordre_de_garde";

	public static boolean invoquer(ServerPlayerEntity player, String espece) {
		ServerWorld world = player.getServerWorld();
		EntityType<? extends MobEntity> type = LimbesEspeces.type(espece);
		if (type == null) {
			fr.leslimbes.LesLimbes.LOGGER.warn("Garde astrale : espèce inconnue {}", espece);
			return false;
		}
		Vec3d pos = PortesDuPalais.autour(world, player, player.getPos(), 1.8);
		if (pos == null) {
			fr.leslimbes.LesLimbes.LOGGER.warn("Garde astrale : aucune position sûre autour de {} en {}", player.getName().getString(), player.getBlockPos().toShortString());
			player.sendMessage(Text.translatable("message.les_limbes.portes.obstrue").formatted(Formatting.RED), true);
			return false;
		}
		List<GardeAstraleEntity> gardes = gardesDe(player, 96.0);
		gardes.sort(Comparator.comparingInt(g -> -g.age));
		int budget = fr.leslimbes.LimbesConfig.get().gardesParJoueur;
		while (gardes.size() >= budget) {
			GardeAstraleEntity ancienne = gardes.remove(0);
			ancienne.dissiper();
		}
		Entity e = type.create(world);
		if (!(e instanceof GardeAstraleEntity garde)) {
			fr.leslimbes.LesLimbes.LOGGER.warn("Garde astrale : le type {} n'a pas produit de garde ({})", espece, e);
			return false;
		}
		garde.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), 0.0F);
		garde.lier(player, fr.leslimbes.LimbesConfig.get().dureeGardeTicks);
		garde.setOrdre(PlayerLimbesData.of(player).compteur(COMPTEUR_ORDRE) % 3, player.getBlockPos());
		world.spawnEntity(garde);
		world.playSound(null, garde.getBlockPos(), LimbesSounds.POUVOIR_GARDE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "garde_invocation", pos.add(0, 1.0, 0), player.getRotationVector(), 0);
		player.sendMessage(Text.translatable("message.les_limbes.garde.invoquee", garde.getDisplayName(), gardes.size() + 1, budget).formatted(Formatting.GOLD), true);
		return true;
	}

	public static boolean ordre(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		int ordre = (data.compteur(COMPTEUR_ORDRE) + 1) % 3;
		data.compteurs.put(COMPTEUR_ORDRE, ordre);
		List<GardeAstraleEntity> gardes = gardesDe(player, 96.0);
		for (GardeAstraleEntity g : gardes) {
			g.setOrdre(ordre, player.getBlockPos());
		}
		ServerWorld world = player.getServerWorld();
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_GARDE, SoundCategory.PLAYERS, 0.7F, 1.3F);
		Abilities.effet(world, "ordre_de_garde", player.getPos().add(0, 1.0, 0), player.getRotationVector(), ordre);
		player.sendMessage(Text.translatable("message.les_limbes.garde.ordre." + GardeAstraleEntity.nomOrdre(ordre), gardes.size()).formatted(Formatting.GOLD), true);
		return true;
	}

	/** Gardes vivantes de ce joueur à portée (toutes variantes). */
	public static List<GardeAstraleEntity> gardesDe(ServerPlayerEntity player, double rayon) {
		return player.getServerWorld().getEntitiesByClass(GardeAstraleEntity.class, player.getBoundingBox().expand(rayon),
				g -> g.isAlive() && player.getUuid().equals(g.getProprietaire()));
	}

	private GardeAstrale() {
	}
}
