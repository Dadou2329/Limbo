package fr.leslimbes.quest;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Définition d'une quête (data/les_limbes/quest/&lt;id&gt;.json). Immuable, rechargeable.
 *
 * <pre>
 * {
 *   "titre": "L'Étranger", "description": "...", "acte": 1,
 *   "etapes": [
 *     {"id": "accueil", "resume": "Parler à Maëlle",
 *      "objectifs": [{"id": "maelle", "type": "parler", "cible": "maelle", "texte": "Parler à Maëlle"}],
 *      "lieu": {"nom": "Sanctuaire de l'Arrivée", "pos": [0, 93, 6], "dimension": "les_limbes:limbes"},
 *      "debut": [actions], "fin": [actions]}
 *   ],
 *   "fin": [actions]
 * }
 * </pre>
 */
public record QuestDefinition(String id, String titre, String description, int acte, List<Etape> etapes, List<JsonObject> fin, boolean cachee) {

	public enum TypeObjectif {
		PARLER, ATTEINDRE, VAINCRE, COLLECTER, INTERAGIR, POUVOIR, DRAPEAU
	}

	/** Un objectif d'étape. {@code cible} dépend du type (rôle de PNJ, identifiant d'entité, d'objet, de bloc, de pouvoir ou nom de drapeau). */
	public record Objectif(String id, TypeObjectif type, String cible, int nombre, Optional<BlockPos> pos, int rayon,
						   Optional<Identifier> dimension, String texte, boolean manuel, Optional<String> cleLieu) {
		public boolean estAtteint(QuestProgress progress) {
			if (progress.estFait(id)) {
				return true;
			}
			return switch (type) {
				case VAINCRE, COLLECTER -> progress.compteur(id) >= nombre;
				default -> false;
			};
		}

		public Text texteAffiche(QuestProgress progress) {
			if (type == TypeObjectif.VAINCRE || type == TypeObjectif.COLLECTER) {
				return Text.literal(texte + " (" + Math.min(progress.compteur(id), nombre) + "/" + nombre + ")");
			}
			return Text.literal(texte);
		}
	}

	public record Lieu(Text nom, BlockPos pos, Identifier dimension) {
	}

	public record Etape(String id, String resume, List<Objectif> objectifs, Optional<Lieu> lieu, Optional<String> cleLieu,
						List<JsonObject> debut, List<JsonObject> fin) {
		public boolean estTerminee(QuestProgress progress) {
			for (Objectif o : objectifs) {
				if (!o.estAtteint(progress)) {
					return false;
				}
			}
			return true;
		}
	}

	public Optional<Etape> etape(int index) {
		return index >= 0 && index < etapes.size() ? Optional.of(etapes.get(index)) : Optional.empty();
	}

	public Optional<Etape> etapeCourante(QuestProgress progress) {
		return progress.terminee ? Optional.empty() : etape(progress.etape);
	}

	/** Lieu de l'étape courante : fixe (JSON) ou dynamique (mémorisé dans la progression sous une clé). */
	public Optional<Lieu> lieuPour(QuestProgress progress) {
		Optional<Etape> etape = etapeCourante(progress);
		if (etape.isEmpty()) {
			return Optional.empty();
		}
		Etape e = etape.get();
		if (e.cleLieu().isPresent()) {
			BlockPos dyn = progress.lieux.get(e.cleLieu().get());
			if (dyn != null) {
				Identifier dim = e.lieu().map(Lieu::dimension).orElse(Identifier.of("minecraft", "overworld"));
				Text nom = e.lieu().map(Lieu::nom).orElse(Text.literal(e.resume()));
				return Optional.of(new Lieu(nom, dyn, dim));
			}
		}
		return e.lieu();
	}

	public static QuestDefinition parse(String id, JsonObject json) {
		String titre = JsonUtil.requireString(json, "titre", "quête " + id);
		String description = JsonUtil.string(json, "description", "");
		int acte = JsonUtil.integer(json, "acte", 1);
		boolean cachee = JsonUtil.bool(json, "cachee", false);
		List<Etape> etapes = new ArrayList<>();
		int index = 0;
		for (JsonObject e : JsonUtil.objects(json, "etapes")) {
			String eid = JsonUtil.string(e, "id", "etape_" + index);
			String resume = JsonUtil.requireString(e, "resume", "quête " + id + ", étape " + eid);
			List<Objectif> objectifs = new ArrayList<>();
			int oi = 0;
			for (JsonObject o : JsonUtil.objects(e, "objectifs")) {
				String oid = JsonUtil.string(o, "id", "objectif_" + oi);
				TypeObjectif type = TypeObjectif.valueOf(JsonUtil.requireString(o, "type", "quête " + id + "/" + eid).toUpperCase());
				String cible = JsonUtil.string(o, "cible", "");
				if (type != TypeObjectif.ATTEINDRE && cible.isEmpty()) {
					throw new IllegalArgumentException("quête " + id + "/" + eid + "/" + oid + " : « cible » obligatoire pour " + type);
				}
				Optional<Identifier> dim = Optional.ofNullable(JsonUtil.string(o, "dimension", null)).map(Identifier::of);
				objectifs.add(new Objectif(oid, type, cible, JsonUtil.integer(o, "nombre", 1), JsonUtil.blockPos(o, "pos"),
						JsonUtil.integer(o, "rayon", 8), dim, JsonUtil.string(o, "texte", resume), JsonUtil.bool(o, "manuel", false),
						Optional.ofNullable(JsonUtil.string(o, "cle_lieu", null))));
				oi++;
			}
			if (objectifs.isEmpty()) {
				throw new IllegalArgumentException("quête " + id + "/" + eid + " : aucune étape sans objectif");
			}
			Optional<Lieu> lieu = JsonUtil.object(e, "lieu").map(l -> new Lieu(
					Text.literal(JsonUtil.requireString(l, "nom", "lieu de " + id + "/" + eid)),
					JsonUtil.blockPos(l, "pos").orElse(BlockPos.ORIGIN),
					Identifier.of(JsonUtil.string(l, "dimension", "les_limbes:limbes"))));
			Optional<String> cleLieu = Optional.ofNullable(JsonUtil.string(e, "cle_lieu", null));
			etapes.add(new Etape(eid, resume, List.copyOf(objectifs), lieu, cleLieu, JsonUtil.objects(e, "debut"), JsonUtil.objects(e, "fin")));
			index++;
		}
		if (etapes.isEmpty()) {
			throw new IllegalArgumentException("quête " + id + " : aucune étape");
		}
		return new QuestDefinition(id, titre, description, acte, List.copyOf(etapes), JsonUtil.objects(json, "fin"), cachee);
	}
}
