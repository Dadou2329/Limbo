package fr.leslimbes.npc.dialogue;

import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conversation en cours entre un joueur et un personnage. Le serveur conserve le nœud courant et la
 * liste des choix réellement proposés ; le client ne renvoie qu'un index, validé ici.
 */
public final class DialogueSession {
	private static final Map<UUID, DialogueSession> SESSIONS = new ConcurrentHashMap<>();

	private final DialogueGraph graph;
	private final HabitantEntity pnj;
	private DialogueGraph.Noeud noeud;
	private List<DialogueGraph.Choix> choixProposes = List.of();

	private DialogueSession(DialogueGraph graph, HabitantEntity pnj, DialogueGraph.Noeud noeud) {
		this.graph = graph;
		this.pnj = pnj;
		this.noeud = noeud;
	}

	/** Ouvre le dialogue d'un personnage pour un joueur. @return false si aucun dialogue n'existe. */
	public static boolean ouvrir(ServerPlayerEntity player, HabitantEntity pnj) {
		DialogueGraph graph = DialogueLoader.get(pnj.getRole());
		if (graph == null) {
			player.sendMessage(Text.translatable("message.les_limbes.dialogue.silence", pnj.getDisplayName()).formatted(Formatting.GRAY), true);
			return false;
		}
		DialogueGraph.Noeud debut = null;
		for (DialogueGraph.Regle regle : graph.debut()) {
			if (Conditions.toutes(player, regle.conditions())) {
				debut = graph.noeud(regle.noeud()).orElse(null);
				break;
			}
		}
		if (debut == null) {
			return false;
		}
		QuestManager.signaler(player, QuestSignal.parler(pnj.getRole()));
		DialogueSession session = new DialogueSession(graph, pnj, debut);
		SESSIONS.put(player.getUuid(), session);
		pnj.commencerConversation(player);
		player.getServerWorld().playSound(null, pnj.getBlockPos(), LimbesSounds.DIALOGUE_OUVRIR, SoundCategory.NEUTRAL, 0.6F, 1.0F);
		session.entrer(player);
		return true;
	}

	public static void fermer(ServerPlayerEntity player) {
		DialogueSession session = SESSIONS.remove(player.getUuid());
		if (session != null) {
			session.pnj.terminerConversation();
			if (ServerPlayNetworking.canSend(player, LimbesPayloads.FermerDialogue.ID)) {
				ServerPlayNetworking.send(player, new LimbesPayloads.FermerDialogue());
			}
		}
	}

	public static boolean enCours(ServerPlayerEntity player) {
		return SESSIONS.containsKey(player.getUuid());
	}

	/** Traite un choix envoyé par le client. index = -1 : fermeture. */
	public static void choisir(ServerPlayerEntity player, int index) {
		DialogueSession session = SESSIONS.get(player.getUuid());
		if (session == null) {
			return;
		}
		if (index < 0) {
			fermer(player);
			return;
		}
		if (index >= session.choixProposes.size()) {
			return;
		}
		if (!session.pnj.isAlive() || session.pnj.squaredDistanceTo(player) > 64.0) {
			fermer(player);
			return;
		}
		DialogueGraph.Choix choix = session.choixProposes.get(index);
		Actions.executer(player, choix.actions());
		if (choix.suivant().isEmpty()) {
			fermer(player);
			return;
		}
		DialogueGraph.Noeud suivant = session.graph.noeud(choix.suivant().get()).orElse(null);
		if (suivant == null) {
			fermer(player);
			return;
		}
		session.noeud = suivant;
		session.entrer(player);
	}

	private void entrer(ServerPlayerEntity player) {
		Actions.executer(player, noeud.actions());
		List<DialogueGraph.Choix> visibles = new ArrayList<>();
		for (DialogueGraph.Choix c : noeud.choix()) {
			if (Conditions.toutes(player, c.conditions())) {
				visibles.add(c);
			}
		}
		this.choixProposes = List.copyOf(visibles);
		NbtCompound contenu = new NbtCompound();
		contenu.putString("nom", noeud.nomAlternatif().orElse(graph.nom()));
		contenu.putInt("pnj", pnj.getId());
		NbtList lignes = new NbtList();
		for (String l : noeud.texte()) {
			lignes.add(NbtString.of(remplacer(player, l)));
		}
		contenu.put("texte", lignes);
		NbtList choix = new NbtList();
		for (DialogueGraph.Choix c : visibles) {
			choix.add(NbtString.of(c.texte()));
		}
		contenu.put("choix", choix);
		if (ServerPlayNetworking.canSend(player, LimbesPayloads.OuvrirDialogue.ID)) {
			ServerPlayNetworking.send(player, new LimbesPayloads.OuvrirDialogue(contenu));
		}
	}

	/** Remplace les marqueurs dynamiques du texte : {joueur}, {arrivees}. */
	private static String remplacer(ServerPlayerEntity player, String texte) {
		if (texte.indexOf('{') < 0) {
			return texte;
		}
		return texte.replace("{joueur}", player.getName().getString())
				.replace("{arrivees}", fr.leslimbes.arrivee.ArriveeManager.resumePour(player));
	}

	/** Nettoyage à la déconnexion. */
	public static void onDeconnexion(UUID uuid) {
		DialogueSession s = SESSIONS.remove(uuid);
		if (s != null) {
			s.pnj.terminerConversation();
		}
	}
}
