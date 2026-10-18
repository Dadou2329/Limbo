package fr.leslimbes.arrivee;

import com.google.gson.JsonObject;
import fr.leslimbes.util.JsonUtil;
import net.minecraft.util.math.BlockPos;

/**
 * Emplacement réservé aux Arrivées (data/les_limbes/arrivee_site/&lt;id&gt;.json) : terrain aplani à la
 * génération, jamais au-dessus d'une construction. {@code pos} est le coin de référence (ancre du gabarit).
 * <pre>{"groupe": "prairies", "pos": [60, 66, 200], "rayon": 12}</pre>
 */
public record ArriveeSite(String id, String groupe, BlockPos pos, int rayon) {
	public static ArriveeSite parse(String id, JsonObject json) {
		return new ArriveeSite(id, JsonUtil.string(json, "groupe", "prairies"),
				JsonUtil.blockPos(json, "pos").orElseThrow(() -> new IllegalArgumentException("site d'Arrivée " + id + " : « pos » obligatoire")),
				JsonUtil.integer(json, "rayon", 12));
	}
}
