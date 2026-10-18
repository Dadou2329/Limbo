package fr.leslimbes.domaine;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.math.BlockPos;

/**
 * Lieu d'intérêt (data/les_limbes/lieu/&lt;id&gt;.json) : découvert quand un joueur entre dans son rayon ;
 * inscrit au journal et sur la carte (drapeau {@code lieu:<id>}). S'il porte un relais ({@code relais}), celui-ci
 * est découvert en même temps.
 */
public record LieuDefinition(String id, String nom, String description, BlockPos pos, int rayon, String region, String dimension, String relais) {
	public static LieuDefinition parse(String id, JsonObject json) {
		return new LieuDefinition(id, JsonUtil.requireString(json, "nom", "lieu " + id), JsonUtil.string(json, "description", ""),
				JsonUtil.blockPos(json, "pos").orElseThrow(() -> new IllegalArgumentException("lieu " + id + " : « pos » obligatoire")),
				JsonUtil.integer(json, "rayon", 12), JsonUtil.string(json, "region", "prairies"), JsonUtil.string(json, "dimension", "les_limbes:limbes"),
				JsonUtil.string(json, "relais", ""));
	}

	public String drapeau() {
		return "lieu:" + id;
	}
}
