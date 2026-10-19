package fr.leslimbes.duel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.leslimbes.LesLimbes;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Charge les duels et épreuves depuis data/&lt;ns&gt;/duel/*.json. */
public final class DuelLoader implements SimpleSynchronousResourceReloadListener {
	private static final Map<String, DuelDefinition> DUELS = new LinkedHashMap<>();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DuelLoader());
	}

	@Nullable
	public static DuelDefinition get(String id) {
		return DUELS.get(id);
	}

	public static Map<String, DuelDefinition> tous() {
		return Collections.unmodifiableMap(DUELS);
	}

	/** Chargement direct (tests). */
	public static void chargerDepuisJson(Map<String, String> fichiers) {
		Map<String, DuelDefinition> nouveaux = new LinkedHashMap<>();
		fichiers.forEach((id, texte) -> nouveaux.put(id, DuelDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		DUELS.clear();
		DUELS.putAll(nouveaux);
	}

	@Override
	public Identifier getFabricId() {
		return LesLimbes.id("duels");
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<String, DuelDefinition> nouveaux = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("duel", id -> id.getPath().endsWith(".json")).entrySet()) {
			Identifier rid = entry.getKey();
			String path = rid.getPath();
			String id = path.substring("duel/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				nouveaux.put(id, DuelDefinition.parse(id, json));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Duel invalide {} : {}", rid, e.getMessage());
			}
		}
		DUELS.clear();
		DUELS.putAll(nouveaux);
		LesLimbes.LOGGER.info("{} duel(s) chargé(s).", DUELS.size());
	}
}
