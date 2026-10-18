package fr.leslimbes.arrivee;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Locale;

/**
 * Type d'Arrivée (data/les_limbes/arrivee/&lt;id&gt;.json) : quelque chose venu d'ailleurs qui rejoint les Limbes.
 *
 * <pre>
 * {
 *   "titre": "La maison égarée", "description": "...", "echelle": "moyenne",
 *   "gabarit": "les_limbes:arrivees/maison_egaree", "ancre": [6, 0, 6],
 *   "sites": "prairies", "poids": 10, "unique": true, "duree": 0,
 *   "prerequis": [{"drapeau_monde": "moulin_repare"}],
 *   "annonce": "Une lumière est tombée"
 * }
 * </pre>
 * {@code duree} (ticks) : 0 = reste jusqu'à sa résolution ; sinon l'événement expire et sa structure est retirée.
 */
public record ArriveeDefinition(String id, String titre, String description, Echelle echelle, Identifier gabarit, BlockPos ancre,
								String groupeSites, int poids, boolean unique, long duree, List<JsonObject> prerequis, String annonce) {

	public enum Echelle {
		PETITE, MOYENNE, MAJEURE
	}

	public static ArriveeDefinition parse(String id, JsonObject json) {
		String titre = JsonUtil.requireString(json, "titre", "arrivée " + id);
		Echelle echelle = Echelle.valueOf(JsonUtil.string(json, "echelle", "petite").toUpperCase(Locale.ROOT));
		Identifier gabarit = Identifier.of(JsonUtil.requireString(json, "gabarit", "arrivée " + id));
		BlockPos ancre = JsonUtil.blockPos(json, "ancre").orElse(BlockPos.ORIGIN);
		String sites = JsonUtil.string(json, "sites", "prairies");
		int poids = Math.max(1, JsonUtil.integer(json, "poids", 10));
		boolean unique = JsonUtil.bool(json, "unique", echelle != Echelle.PETITE);
		long duree = JsonUtil.integer(json, "duree", echelle == Echelle.PETITE ? 48000 : 0);
		return new ArriveeDefinition(id, titre, JsonUtil.string(json, "description", ""), echelle, gabarit, ancre, sites, poids, unique, duree,
				JsonUtil.objects(json, "prerequis"), JsonUtil.string(json, "annonce", "Le ciel se plisse"));
	}
}
