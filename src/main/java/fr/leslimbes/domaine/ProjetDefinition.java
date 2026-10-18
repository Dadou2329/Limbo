package fr.leslimbes.domaine;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Projet de restauration ou de construction d'un domaine (data/les_limbes/projet/&lt;id&gt;.json).
 * Un projet pose un gabarit à une origine fixe, consomme des ressources, pose un drapeau du monde et
 * exécute des actions. S'il remplace une partie d'un gabarit antérieur ({@code avant}), les blocs
 * de ce gabarit ne comptent pas comme des constructions du joueur.
 */
public record ProjetDefinition(String id, String titre, String description, String indice, String lieu, int ordre, List<JsonObject> prerequis,
							   List<Cout> cout, Identifier gabarit, BlockPos origine, Optional<Identifier> avant, BlockPos origineAvant,
							   String drapeauMonde, List<JsonObject> apres) {

	public record Cout(Identifier objet, int nombre) {
	}

	public static ProjetDefinition parse(String id, JsonObject json) {
		List<Cout> couts = new ArrayList<>();
		for (JsonObject c : JsonUtil.objects(json, "cout")) {
			couts.add(new Cout(Identifier.of(JsonUtil.requireString(c, "objet", "projet " + id)), Math.max(1, JsonUtil.integer(c, "nombre", 1))));
		}
		Optional<Identifier> avant = Optional.ofNullable(JsonUtil.string(json, "avant", null)).map(Identifier::of);
		return new ProjetDefinition(id, JsonUtil.requireString(json, "titre", "projet " + id), JsonUtil.string(json, "description", ""),
				JsonUtil.string(json, "indice", ""), JsonUtil.string(json, "lieu", "manoir"), JsonUtil.integer(json, "ordre", 99), JsonUtil.objects(json, "prerequis"), List.copyOf(couts),
				Identifier.of(JsonUtil.requireString(json, "gabarit", "projet " + id)),
				JsonUtil.blockPos(json, "origine").orElseThrow(() -> new IllegalArgumentException("projet " + id + " : « origine » obligatoire")),
				avant, JsonUtil.blockPos(json, "origine_avant").orElse(BlockPos.ORIGIN),
				JsonUtil.string(json, "drapeau_monde", "domaine_" + id), JsonUtil.objects(json, "apres"));
	}
}
