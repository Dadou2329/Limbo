package fr.leslimbes.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Petites aides de lecture JSON (Gson) avec valeurs par défaut explicites. */
public final class JsonUtil {
	public static String string(JsonObject obj, String key, String def) {
		JsonElement e = obj.get(key);
		return e == null || e.isJsonNull() ? def : e.getAsString();
	}

	public static String requireString(JsonObject obj, String key, String contexte) {
		JsonElement e = obj.get(key);
		if (e == null || e.isJsonNull()) {
			throw new IllegalArgumentException(contexte + " : champ obligatoire manquant « " + key + " »");
		}
		return e.getAsString();
	}

	public static int integer(JsonObject obj, String key, int def) {
		JsonElement e = obj.get(key);
		return e == null || e.isJsonNull() ? def : e.getAsInt();
	}

	public static float floating(JsonObject obj, String key, float def) {
		JsonElement e = obj.get(key);
		return e == null || e.isJsonNull() ? def : e.getAsFloat();
	}

	public static boolean bool(JsonObject obj, String key, boolean def) {
		JsonElement e = obj.get(key);
		return e == null || e.isJsonNull() ? def : e.getAsBoolean();
	}

	public static List<String> strings(JsonObject obj, String key) {
		List<String> out = new ArrayList<>();
		JsonElement e = obj.get(key);
		if (e == null || e.isJsonNull()) {
			return out;
		}
		if (e.isJsonPrimitive()) {
			out.add(e.getAsString());
			return out;
		}
		for (JsonElement item : e.getAsJsonArray()) {
			out.add(item.getAsString());
		}
		return out;
	}

	public static List<JsonObject> objects(JsonObject obj, String key) {
		List<JsonObject> out = new ArrayList<>();
		JsonElement e = obj.get(key);
		if (e == null || e.isJsonNull()) {
			return out;
		}
		for (JsonElement item : e.getAsJsonArray()) {
			out.add(item.getAsJsonObject());
		}
		return out;
	}

	public static Optional<BlockPos> blockPos(JsonObject obj, String key) {
		JsonElement e = obj.get(key);
		if (e == null || e.isJsonNull()) {
			return Optional.empty();
		}
		JsonArray a = e.getAsJsonArray();
		return Optional.of(new BlockPos(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()));
	}

	public static Optional<JsonObject> object(JsonObject obj, String key) {
		JsonElement e = obj.get(key);
		return e == null || !e.isJsonObject() ? Optional.empty() : Optional.of(e.getAsJsonObject());
	}

	private JsonUtil() {
	}
}
