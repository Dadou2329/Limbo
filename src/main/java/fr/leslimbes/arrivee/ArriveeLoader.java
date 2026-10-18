package fr.leslimbes.arrivee;

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

/** Charge les types d'Arrivées et leurs sites réservés. */
public final class ArriveeLoader implements SimpleSynchronousResourceReloadListener {
	private static final Map<String, ArriveeDefinition> DEFINITIONS = new LinkedHashMap<>();
	private static final Map<String, ArriveeSite> SITES = new LinkedHashMap<>();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ArriveeLoader());
	}

	@Nullable
	public static ArriveeDefinition get(String id) {
		return DEFINITIONS.get(id);
	}

	public static Map<String, ArriveeDefinition> definitions() {
		return Collections.unmodifiableMap(DEFINITIONS);
	}

	public static Map<String, ArriveeSite> sites() {
		return Collections.unmodifiableMap(SITES);
	}

	/** Chargement direct (tests). */
	public static void chargerDepuisJson(Map<String, String> definitions, Map<String, String> sites) {
		Map<String, ArriveeDefinition> d = new LinkedHashMap<>();
		definitions.forEach((id, texte) -> d.put(id, ArriveeDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		Map<String, ArriveeSite> s = new LinkedHashMap<>();
		sites.forEach((id, texte) -> s.put(id, ArriveeSite.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		DEFINITIONS.clear();
		DEFINITIONS.putAll(d);
		SITES.clear();
		SITES.putAll(s);
	}

	@Override
	public Identifier getFabricId() {
		return LesLimbes.id("arrivees");
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<String, ArriveeDefinition> d = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("arrivee", id -> id.getPath().endsWith(".json")).entrySet()) {
			String path = entry.getKey().getPath();
			String id = path.substring("arrivee/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				d.put(id, ArriveeDefinition.parse(id, json));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Arrivée invalide {} : {}", entry.getKey(), e.getMessage());
			}
		}
		Map<String, ArriveeSite> s = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("arrivee_site", id -> id.getPath().endsWith(".json")).entrySet()) {
			String path = entry.getKey().getPath();
			String id = path.substring("arrivee_site/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				s.put(id, ArriveeSite.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Site d'Arrivée invalide {} : {}", entry.getKey(), e.getMessage());
			}
		}
		DEFINITIONS.clear();
		DEFINITIONS.putAll(d);
		SITES.clear();
		SITES.putAll(s);
		LesLimbes.LOGGER.info("{} type(s) d'Arrivée, {} site(s) réservé(s).", DEFINITIONS.size(), SITES.size());
	}
}
