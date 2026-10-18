package fr.leslimbes.npc.dialogue;

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

/** Charge les dialogues depuis data/&lt;ns&gt;/dialogue/*.json. */
public final class DialogueLoader implements SimpleSynchronousResourceReloadListener {
	private static final Map<String, DialogueGraph> DIALOGUES = new LinkedHashMap<>();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DialogueLoader());
	}

	@Nullable
	public static DialogueGraph get(String id) {
		return DIALOGUES.get(id);
	}

	public static Map<String, DialogueGraph> tous() {
		return Collections.unmodifiableMap(DIALOGUES);
	}

	/** Chargement direct (tests). */
	public static void chargerDepuisJson(Map<String, String> fichiers) {
		Map<String, DialogueGraph> nouveaux = new LinkedHashMap<>();
		fichiers.forEach((id, texte) -> nouveaux.put(id, DialogueGraph.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		DIALOGUES.clear();
		DIALOGUES.putAll(nouveaux);
	}

	@Override
	public Identifier getFabricId() {
		return LesLimbes.id("dialogues");
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<String, DialogueGraph> nouveaux = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("dialogue", id -> id.getPath().endsWith(".json")).entrySet()) {
			Identifier rid = entry.getKey();
			String path = rid.getPath();
			String id = path.substring("dialogue/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				nouveaux.put(id, DialogueGraph.parse(id, json));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Dialogue invalide {} : {}", rid, e.getMessage());
			}
		}
		DIALOGUES.clear();
		DIALOGUES.putAll(nouveaux);
		LesLimbes.LOGGER.info("{} dialogue(s) chargé(s).", DIALOGUES.size());
	}
}
