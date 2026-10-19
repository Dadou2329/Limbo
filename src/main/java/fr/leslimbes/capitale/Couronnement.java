package fr.leslimbes.capitale;

import com.google.gson.JsonObject;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.npc.dialogue.Conditions;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesAttachments;
import fr.leslimbes.registry.LimbesEntities;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.util.JsonUtil;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.dimension.LimbesSites;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Le couronnement : la ville s'illumine (niveau 3 de la capitale), la couronne s'assemble au-dessus du joueur,
 * la majesté grandit, et le monde retient qu'un souverain règne. Les délégations (personnages réellement aidés)
 * se tiennent sur la place pour un moment, puis repartent chez elles.
 */
public final class Couronnement {
	public static final int ETAT_PUBLIC_COURONNE = 2;

	public static void couronner(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (data.aDrapeau("couronne")) {
			return;
		}
		data.poserDrapeau("couronne");
		data.augmenterMajesteMax(60.0F);
		LimbesWorldState etat = LimbesWorldState.get(player.server);
		etat.poserDrapeau("couronnement_fait");
		CapitaleManager.eveiller(player.server, 3, false);
		data.poserDrapeau("couronne_assemblee");
		LimbesAttachments.publier(player);
		LimbesPortal.titre(player, Text.translatable("message.les_limbes.couronnement.titre").formatted(Formatting.GOLD),
				Text.translatable("message.les_limbes.couronnement.sous_titre"));
		player.sendMessage(Text.translatable("message.les_limbes.couronnement.message").formatted(Formatting.GOLD), false);
		ServerWorld world = player.getServerWorld();
		world.playSound(null, player.getBlockPos(), LimbesSounds.QUETE_TERMINEE, SoundCategory.PLAYERS, 1.5F, 0.8F);
		world.playSound(null, player.getBlockPos(), LimbesSounds.PORTE_EVEIL, SoundCategory.PLAYERS, 1.5F, 1.0F);
		Abilities.effet(world, "pacte", player.getPos().add(0, 2.2, 0), Vec3d.ZERO, 0);
		Abilities.effet(world, "arrivee_ciel", player.getPos().add(0, 8, 0), Vec3d.ZERO, 0);
		for (ServerPlayerEntity autre : player.server.getPlayerManager().getPlayerList()) {
			if (autre != player) {
				autre.sendMessage(Text.translatable("message.les_limbes.couronnement.annonce", player.getName()).formatted(Formatting.GOLD), false);
			}
		}
		QuestManager.signaler(player, QuestSignal.drapeau("couronne"));
		PlayerDataSync.envoyer(player);
		LesLimbes.LOGGER.info("Couronnement de {}", player.getName().getString());
	}

	/**
	 * Fait venir une délégation : des exemplaires temporaires des personnages dont la condition est vraie, placés
	 * en cercle autour d'une position. <pre>{"type": "delegation", "pos": [-900, 86, -900], "rayon": 7, "duree": 12000,
	 * "roles": [{"role": "kael", "si": [{"drapeau": "kael_allie"}]}, {"role": "thibault"}]}</pre>
	 */
	public static void delegation(ServerPlayerEntity player, JsonObject a) {
		ServerWorld world = player.getServerWorld();
		BlockPos centre = JsonUtil.blockPos(a, "pos").orElse(LimbesSites.PLACE_DES_CEREMONIES);
		int rayon = JsonUtil.integer(a, "rayon", 7);
		int duree = JsonUtil.integer(a, "duree", 12000);
		java.util.List<JsonObject> roles = JsonUtil.objects(a, "roles");
		int n = 0;
		for (JsonObject r : roles) {
			String role = JsonUtil.requireString(r, "role", "delegation");
			if (r.has("si") && !Conditions.toutes(player, JsonUtil.objects(r, "si"))) {
				continue;
			}
			// Jamais deux fois le même délégué sur la place.
			boolean present = !world.getEntitiesByClass(HabitantEntity.class, new net.minecraft.util.math.Box(centre).expand(rayon + 6), h -> h.estDelegation() && h.getRole().equals(role)).isEmpty();
			if (present) {
				continue;
			}
			double angle = (n * 2 * Math.PI / Math.max(1, roles.size())) + Math.PI / 2;
			double x = centre.getX() + 0.5 + Math.cos(angle) * rayon;
			double z = centre.getZ() + 0.5 + Math.sin(angle) * rayon;
			int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
			HabitantEntity h = LimbesEntities.HABITANT.create(world);
			if (h == null) {
				continue;
			}
			h.setRole(role);
			h.setDelegation(duree);
			h.refreshPositionAndAngles(x, y, z, (float) Math.toDegrees(Math.atan2(-(centre.getX() + 0.5 - x), centre.getZ() + 0.5 - z)), 0.0F);
			h.setPersistent();
			world.spawnEntity(h);
			n++;
		}
		if (n > 0) {
			world.playSound(null, centre, LimbesSounds.ARRIVEE, SoundCategory.PLAYERS, 1.0F, 1.2F);
			Abilities.effet(world, "arrivee_ciel", Vec3d.ofCenter(centre).add(0, 6, 0), Vec3d.ZERO, 0);
		}
		LesLimbes.LOGGER.info("Délégation : {} personnage(s) sur la place.", n);
	}

	public static Optional<BlockPos> place() {
		return Optional.of(LimbesSites.PLACE_DES_CEREMONIES);
	}

	private Couronnement() {
	}
}
