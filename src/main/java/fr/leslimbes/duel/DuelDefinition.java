package fr.leslimbes.duel;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Définition d'un duel ou d'une épreuve (data/les_limbes/duel/&lt;id&gt;.json).
 *
 * <pre>
 * {
 *   "titre": "L'épreuve des Jardins", "pretendant": "aelis", "boss": "les_limbes:aelis_duel",
 *   "renforts": [{"entite": "les_limbes:chevalier_de_bois_blanc", "nombre": 1}],
 *   "arene": {"centre": [1150, 72, 300], "rayon": 18, "dimension": "les_limbes:limbes"},
 *   "entree": [1150, 72, 322], "yaw": 180, "apparition": [1150, 72, 294],
 *   "amical": true,
 *   "phases": [{"seuil": 0.66, "message": "...", "invocations": [...], "blocs": "racines"}, {"seuil": 0.33, ...}],
 *   "victoire": [actions], "echec": [actions]
 * }
 * </pre>
 * La victoire pose le drapeau {@code duel_<id>_gagne} à chaque participant ; un duel amical renvoie le joueur à
 * l'entrée quand il est au bout de ses forces au lieu de le laisser mourir.
 */
public record DuelDefinition(String id, String titre, String pretendant, Identifier boss, List<Renfort> renforts, BlockPos centre, int rayon, Identifier dimension,
							 BlockPos entree, float yaw, BlockPos apparition, boolean amical, List<Phase> phases, List<JsonObject> victoire, List<JsonObject> echec) {
	public record Renfort(Identifier type, int nombre) {
	}

	/** Une phase : franchie quand la vie du boss passe sous {@code seuil} (fraction) ; déclenche message, invocations et blocs temporaires. */
	public record Phase(float seuil, String message, List<Renfort> invocations, String blocs, int dureeBlocs) {
	}

	public String drapeauVictoire() {
		return "duel_" + id + "_gagne";
	}

	public static DuelDefinition parse(String id, JsonObject json) {
		String titre = JsonUtil.requireString(json, "titre", "duel " + id);
		String pretendant = JsonUtil.string(json, "pretendant", "");
		Identifier boss = Identifier.of(JsonUtil.requireString(json, "boss", "duel " + id));
		JsonObject arene = JsonUtil.object(json, "arene").orElseThrow(() -> new IllegalArgumentException("duel " + id + " : « arene » obligatoire"));
		BlockPos centre = JsonUtil.blockPos(arene, "centre").orElseThrow(() -> new IllegalArgumentException("duel " + id + " : centre d'arène obligatoire"));
		int rayon = JsonUtil.integer(arene, "rayon", 16);
		Identifier dimension = Identifier.of(JsonUtil.string(arene, "dimension", "les_limbes:limbes"));
		BlockPos entree = JsonUtil.blockPos(json, "entree").orElse(centre);
		BlockPos apparition = JsonUtil.blockPos(json, "apparition").orElse(centre);
		float yaw = (float) JsonUtil.floating(json, "yaw", 0.0F);
		List<Phase> phases = new ArrayList<>();
		for (JsonObject p : JsonUtil.objects(json, "phases")) {
			phases.add(new Phase((float) JsonUtil.floating(p, "seuil", 0.5F), JsonUtil.string(p, "message", ""), renforts(p, "invocations"),
					JsonUtil.string(p, "blocs", ""), JsonUtil.integer(p, "duree_blocs", 300)));
		}
		phases.sort((a, b) -> Float.compare(b.seuil(), a.seuil()));
		return new DuelDefinition(id, titre, pretendant, boss, renforts(json, "renforts"), centre, rayon, dimension, entree, yaw, apparition,
				JsonUtil.bool(json, "amical", true), List.copyOf(phases), JsonUtil.objects(json, "victoire"), JsonUtil.objects(json, "echec"));
	}

	private static List<Renfort> renforts(JsonObject json, String cle) {
		List<Renfort> l = new ArrayList<>();
		for (JsonObject r : JsonUtil.objects(json, cle)) {
			l.add(new Renfort(Identifier.of(JsonUtil.requireString(r, "entite", cle)), JsonUtil.integer(r, "nombre", 1)));
		}
		return List.copyOf(l);
	}
}
