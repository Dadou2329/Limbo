package fr.leslimbes.domaine;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.math.BlockPos;

/**
 * Relais du réseau des passages (data/les_limbes/relais/&lt;id&gt;.json) : un point d'arrivée sûr devant une
 * porte de relais. Un relais se découvre en touchant sa porte ou en découvrant le lieu qui le porte
 * (drapeau du joueur {@code relais:<id>}) ; la salle des passages du domaine mène à chaque relais connu.
 */
public record RelaisDefinition(String id, String nom, String description, BlockPos pos, String dimension, int ordre) {
	public static RelaisDefinition parse(String id, JsonObject json) {
		return new RelaisDefinition(id, JsonUtil.requireString(json, "nom", "relais " + id), JsonUtil.string(json, "description", ""),
				JsonUtil.blockPos(json, "pos").orElseThrow(() -> new IllegalArgumentException("relais " + id + " : « pos » obligatoire")),
				JsonUtil.string(json, "dimension", "les_limbes:limbes"), JsonUtil.integer(json, "ordre", 50));
	}

	public String drapeau() {
		return "relais:" + id;
	}
}
