package fr.leslimbes.quest;

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

/** Charge les quêtes depuis data/&lt;ns&gt;/quest/*.json à chaque rechargement des données. */
public final class QuestLoader implements SimpleSynchronousResourceReloadListener {
	private static final Map<String, QuestDefinition> QUETES = new LinkedHashMap<>();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new QuestLoader());
	}

	@Nullable
	public static QuestDefinition get(String id) {
		return QUETES.get(id);
	}

	public static Map<String, QuestDefinition> toutes() {
		return Collections.unmodifiableMap(QUETES);
	}

	/** Chargement direct (tests). */
	public static void chargerDepuisJson(Map<String, String> fichiers) {
		Map<String, QuestDefinition> nouvelles = new LinkedHashMap<>();
		fichiers.forEach((id, texte) -> nouvelles.put(id, QuestDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		QUETES.clear();
		QUETES.putAll(nouvelles);
	}

	@Override
	public Identifier getFabricId() {
		return LesLimbes.id("quetes");
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<String, QuestDefinition> nouvelles = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("quest", id -> id.getPath().endsWith(".json")).entrySet()) {
			Identifier rid = entry.getKey();
			String path = rid.getPath();
			String id = path.substring("quest/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				nouvelles.put(id, QuestDefinition.parse(id, json));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Quête invalide {} : {}", rid, e.getMessage());
			}
		}
		QUETES.clear();
		QUETES.putAll(nouvelles);
		LesLimbes.LOGGER.info("{} quête(s) chargée(s).", QUETES.size());
	}
}
