package fr.leslimbes.npc.dialogue;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Graphe de dialogue d'un personnage (data/les_limbes/dialogue/&lt;role&gt;.json).
 *
 * <pre>
 * {
 *   "nom": "Maëlle",
 *   "debut": [ {"noeud": "retrouvailles", "si": [{"drapeau": "maelle_rencontree"}]}, {"noeud": "accueil"} ],
 *   "noeuds": {
 *     "accueil": {
 *       "texte": ["Phrase 1.", "Phrase 2 (page suivante)."],
 *       "actions": [ ... ],
 *       "choix": [
 *         {"texte": "Qui êtes-vous ?", "suivant": "presentation", "si": [...], "actions": [...]},
 *         {"texte": "Au revoir."}
 *       ]
 *     }
 *   }
 * }
 * </pre>
 * Un choix sans « suivant » ferme le dialogue. Un nœud sans choix propose « Continuer » qui ferme.
 */
public record DialogueGraph(String id, String nom, List<Regle> debut, Map<String, Noeud> noeuds) {

	public record Regle(String noeud, List<JsonObject> conditions) {
	}

	public record Choix(String texte, Optional<String> suivant, List<JsonObject> conditions, List<JsonObject> actions) {
	}

	public record Noeud(String id, List<String> texte, Optional<String> nomAlternatif, List<JsonObject> actions, List<Choix> choix) {
	}

	public Optional<Noeud> noeud(String id) {
		return Optional.ofNullable(noeuds.get(id));
	}

	/** Vérifie que toutes les références de nœuds existent. @return liste des erreurs (vide si valide). */
	public List<String> valider() {
		List<String> erreurs = new ArrayList<>();
		for (Regle r : debut) {
			if (!noeuds.containsKey(r.noeud())) {
				erreurs.add("dialogue " + id + " : règle de début vers un nœud inconnu « " + r.noeud() + " »");
			}
		}
		if (debut.isEmpty()) {
			erreurs.add("dialogue " + id + " : aucune règle de début");
		}
		for (Noeud n : noeuds.values()) {
			if (n.texte().isEmpty()) {
				erreurs.add("dialogue " + id + "/" + n.id() + " : texte vide");
			}
			for (Choix c : n.choix()) {
				if (c.suivant().isPresent() && !noeuds.containsKey(c.suivant().get())) {
					erreurs.add("dialogue " + id + "/" + n.id() + " : choix vers un nœud inconnu « " + c.suivant().get() + " »");
				}
				if (c.texte().isBlank()) {
					erreurs.add("dialogue " + id + "/" + n.id() + " : choix sans texte");
				}
			}
		}
		return erreurs;
	}

	public static DialogueGraph parse(String id, JsonObject json) {
		String nom = JsonUtil.string(json, "nom", id);
		List<Regle> debut = new ArrayList<>();
		for (JsonObject r : JsonUtil.objects(json, "debut")) {
			debut.add(new Regle(JsonUtil.requireString(r, "noeud", "dialogue " + id + " (debut)"), JsonUtil.objects(r, "si")));
		}
		Map<String, Noeud> noeuds = new LinkedHashMap<>();
		JsonObject nodes = json.getAsJsonObject("noeuds");
		if (nodes != null) {
			for (String nid : nodes.keySet()) {
				JsonObject n = nodes.getAsJsonObject(nid);
				List<Choix> choix = new ArrayList<>();
				for (JsonObject c : JsonUtil.objects(n, "choix")) {
					choix.add(new Choix(JsonUtil.requireString(c, "texte", "dialogue " + id + "/" + nid),
							Optional.ofNullable(JsonUtil.string(c, "suivant", null)), JsonUtil.objects(c, "si"), JsonUtil.objects(c, "actions")));
				}
				noeuds.put(nid, new Noeud(nid, JsonUtil.strings(n, "texte"), Optional.ofNullable(JsonUtil.string(n, "nom", null)),
						JsonUtil.objects(n, "actions"), List.copyOf(choix)));
			}
		}
		DialogueGraph graph = new DialogueGraph(id, nom, List.copyOf(debut), Map.copyOf(noeuds));
		List<String> erreurs = graph.valider();
		if (!erreurs.isEmpty()) {
			throw new IllegalArgumentException(String.join(" ; ", erreurs));
		}
		return graph;
	}
}
