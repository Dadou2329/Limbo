package fr.leslimbes.quest;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.npc.dialogue.Actions;
import fr.leslimbes.quest.QuestDefinition.Etape;
import fr.leslimbes.quest.QuestDefinition.Objectif;
import fr.leslimbes.quest.QuestDefinition.TypeObjectif;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Logique serveur des quêtes : démarrage, signaux, passage d'étape, récompenses.
 * Toutes les opérations sont idempotentes : rejouer un signal ou redémarrer une quête déjà
 * commencée ne duplique rien.
 */
public final class QuestManager {
	/** Démarre une quête si elle n'est ni active ni terminée. @return true si démarrée. */
	public static boolean demarrer(ServerPlayerEntity player, String questId) {
		QuestDefinition def = QuestLoader.get(questId);
		if (def == null) {
			LesLimbes.LOGGER.warn("Quête inconnue : {}", questId);
			return false;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (data.quetes.containsKey(questId)) {
			return false;
		}
		QuestProgress progress = new QuestProgress(questId);
		data.quetes.put(questId, progress);
		if (data.queteSuivie.isEmpty() || data.queteSuivie().isEmpty()) {
			data.queteSuivie = questId;
		}
		notifier(player, "debut", def, def.titre());
		player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.QUETE_ETAPE, SoundCategory.PLAYERS, 0.8F, 1.0F);
		entrerEtape(player, data, def, progress);
		PlayerDataSync.envoyer(player);
		return true;
	}

	/** Réinitialise complètement une quête (administration / réparation). */
	public static void reinitialiser(ServerPlayerEntity player, String questId) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.quetes.remove(questId);
		if (data.queteSuivie.equals(questId)) {
			data.queteSuivie = "";
		}
		PlayerDataSync.envoyer(player);
	}

	/** Force le passage à une étape donnée (administration). */
	public static boolean allerEtape(ServerPlayerEntity player, String questId, int etape) {
		QuestDefinition def = QuestLoader.get(questId);
		if (def == null) {
			return false;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		QuestProgress progress = data.quetes.computeIfAbsent(questId, QuestProgress::new);
		if (etape >= def.etapes().size()) {
			terminer(player, data, def, progress);
		} else {
			progress.terminee = false;
			progress.nouvelleEtape(Math.max(0, etape));
			entrerEtape(player, data, def, progress);
		}
		PlayerDataSync.envoyer(player);
		return true;
	}

	/** Marque un objectif comme fait (utilisé par les dialogues : {"type":"objectif", ...}). */
	public static void marquerObjectif(ServerPlayerEntity player, String questId, String objectifId) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		QuestProgress progress = data.quetes.get(questId);
		QuestDefinition def = QuestLoader.get(questId);
		if (progress == null || def == null || progress.terminee) {
			return;
		}
		Optional<Etape> etape = def.etapeCourante(progress);
		if (etape.isEmpty()) {
			return;
		}
		boolean concerne = etape.get().objectifs().stream().anyMatch(o -> o.id().equals(objectifId));
		if (!concerne) {
			return;
		}
		if (progress.faits.add(objectifId)) {
			verifierEtape(player, data, def, progress);
			PlayerDataSync.envoyer(player);
		}
	}

	/** Propage un signal de jeu à toutes les quêtes actives du joueur. */
	public static void signaler(ServerPlayerEntity player, QuestSignal signal) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		boolean change = false;
		for (QuestProgress progress : new ArrayList<>(data.quetes.values())) {
			if (progress.terminee) {
				continue;
			}
			QuestDefinition def = QuestLoader.get(progress.questId);
			if (def == null) {
				continue;
			}
			Optional<Etape> etape = def.etapeCourante(progress);
			if (etape.isEmpty()) {
				continue;
			}
			boolean local = false;
			for (Objectif o : etape.get().objectifs()) {
				if (o.type() != signal.type() || o.estAtteint(progress)) {
					continue;
				}
				switch (o.type()) {
					case PARLER, DRAPEAU, POUVOIR -> {
						if (o.cible().equals(signal.cible()) && !(o.type() == TypeObjectif.PARLER && o.manuel())) {
							progress.faits.add(o.id());
							local = true;
						}
					}
					case VAINCRE -> {
						if (o.cible().equals(signal.cible())) {
							progress.incrementer(o.id(), 1);
							local = true;
						}
					}
					case INTERAGIR -> {
						boolean bonBloc = o.cible().equals(signal.cible());
						boolean bonLieu = o.pos().isEmpty() || signal.pos().map(p -> p.isWithinDistance(o.pos().get(), o.rayon())).orElse(false);
						if (bonBloc && bonLieu) {
							progress.faits.add(o.id());
							local = true;
						}
					}
					default -> {
					}
				}
			}
			if (local) {
				change = true;
				verifierEtape(player, data, def, progress);
			}
		}
		if (change) {
			PlayerDataSync.envoyer(player);
		}
	}

	/** Vérifications périodiques (position, inventaire). Appelé une fois par seconde par joueur. */
	public static void tick(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		boolean change = false;
		for (QuestProgress progress : new ArrayList<>(data.quetes.values())) {
			if (progress.terminee) {
				continue;
			}
			QuestDefinition def = QuestLoader.get(progress.questId);
			if (def == null) {
				continue;
			}
			Optional<Etape> etape = def.etapeCourante(progress);
			if (etape.isEmpty()) {
				continue;
			}
			boolean local = false;
			for (Objectif o : etape.get().objectifs()) {
				if (o.estAtteint(progress)) {
					continue;
				}
				if (o.type() == TypeObjectif.ATTEINDRE) {
					Optional<BlockPos> cible = o.cleLieu().map(progress.lieux::get).or(o::pos);
					if (cible.isEmpty()) {
						continue;
					}
					Identifier dim = o.dimension().orElse(player.getServerWorld().getRegistryKey().getValue());
					if (!player.getServerWorld().getRegistryKey().getValue().equals(dim)) {
						continue;
					}
					BlockPos c = cible.get();
					double dx = player.getX() - (c.getX() + 0.5);
					double dz = player.getZ() - (c.getZ() + 0.5);
					double dy = player.getY() - c.getY();
					if (dx * dx + dz * dz <= (double) o.rayon() * o.rayon() && Math.abs(dy) <= Math.max(12, o.rayon())) {
						progress.faits.add(o.id());
						local = true;
					}
				} else if (o.type() == TypeObjectif.DRAPEAU && data.aDrapeau(o.cible())) {
					progress.faits.add(o.id());
					local = true;
				} else if (o.type() == TypeObjectif.DRAPEAU_MONDE && fr.leslimbes.world.state.LimbesWorldState.get(player.server).aDrapeau(o.cible())) {
					progress.faits.add(o.id());
					local = true;
				} else if (o.type() == TypeObjectif.COLLECTER) {
					int count = compterObjets(player, o.cible());
					int avant = progress.compteur(o.id());
					if (count != avant) {
						progress.compteurs.put(o.id(), count);
						change = true;
					}
					if (count >= o.nombre()) {
						local = true;
					}
				}
			}
			if (local) {
				change = true;
				verifierEtape(player, data, def, progress);
			}
		}
		if (change) {
			PlayerDataSync.envoyer(player);
		}
	}

	private static int compterObjets(ServerPlayerEntity player, String itemId) {
		Identifier id = Identifier.tryParse(itemId);
		if (id == null) {
			return 0;
		}
		int total = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(id)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static void verifierEtape(ServerPlayerEntity player, PlayerLimbesData data, QuestDefinition def, QuestProgress progress) {
		Optional<Etape> etape = def.etapeCourante(progress);
		if (etape.isEmpty() || !etape.get().estTerminee(progress)) {
			return;
		}
		Actions.executer(player, etape.get().fin());
		int suivante = progress.etape + 1;
		if (suivante >= def.etapes().size()) {
			terminer(player, data, def, progress);
			return;
		}
		progress.nouvelleEtape(suivante);
		player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.QUETE_ETAPE, SoundCategory.PLAYERS, 0.8F, 1.1F);
		notifier(player, "etape", def, def.etapes().get(suivante).resume());
		entrerEtape(player, data, def, progress);
	}

	private static void entrerEtape(ServerPlayerEntity player, PlayerLimbesData data, QuestDefinition def, QuestProgress progress) {
		Optional<Etape> etape = def.etapeCourante(progress);
		if (etape.isEmpty()) {
			return;
		}
		Actions.executer(player, etape.get().debut());
		// Certaines étapes peuvent être déjà satisfaites (objet déjà en poche, lieu déjà atteint) : on l'évalue tout de suite.
		tick(player);
	}

	private static void terminer(ServerPlayerEntity player, PlayerLimbesData data, QuestDefinition def, QuestProgress progress) {
		if (progress.terminee) {
			return;
		}
		progress.terminee = true;
		progress.faits.clear();
		Actions.executer(player, def.fin());
		player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.QUETE_TERMINEE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		notifier(player, "fin", def, def.titre());
		player.sendMessage(Text.translatable("message.les_limbes.quete.terminee", def.titre()).formatted(Formatting.GOLD), false);
		if (data.queteSuivie.equals(def.id())) {
			data.queteSuivie = data.quetes.values().stream().filter(p -> !p.terminee).map(p -> p.questId).findFirst().orElse("");
		}
	}

	private static void notifier(ServerPlayerEntity player, String type, QuestDefinition def, String texte) {
		if (ServerPlayNetworking.canSend(player, LimbesPayloads.NotificationQuete.ID)) {
			ServerPlayNetworking.send(player, new LimbesPayloads.NotificationQuete(type, def.id(), texte));
		}
	}

	private QuestManager() {
	}
}
