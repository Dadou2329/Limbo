package fr.leslimbes.domaine;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Charge les projets de domaine et les lieux d'intérêt. */
public final class DomaineLoader implements SimpleSynchronousResourceReloadListener {
	private static final Map<String, ProjetDefinition> PROJETS = new LinkedHashMap<>();
	private static final Map<String, LieuDefinition> LIEUX = new LinkedHashMap<>();
	private static final Map<String, RelaisDefinition> RELAIS = new LinkedHashMap<>();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DomaineLoader());
	}

	@Nullable
	public static ProjetDefinition projet(String id) {
		return PROJETS.get(id);
	}

	public static List<ProjetDefinition> projets(String lieu) {
		List<ProjetDefinition> l = new ArrayList<>();
		for (ProjetDefinition p : PROJETS.values()) {
			if (p.lieu().equals(lieu)) {
				l.add(p);
			}
		}
		l.sort(Comparator.comparingInt(ProjetDefinition::ordre).thenComparing(ProjetDefinition::id));
		return l;
	}

	public static Map<String, ProjetDefinition> projets() {
		return Collections.unmodifiableMap(PROJETS);
	}

	public static Map<String, LieuDefinition> lieux() {
		return Collections.unmodifiableMap(LIEUX);
	}

	@Nullable
	public static LieuDefinition lieu(String id) {
		return LIEUX.get(id);
	}

	public static Map<String, RelaisDefinition> relais() {
		return Collections.unmodifiableMap(RELAIS);
	}

	@Nullable
	public static RelaisDefinition relais(String id) {
		return RELAIS.get(id);
	}

	/** Relais triés par ordre puis identifiant (affichage). */
	public static List<RelaisDefinition> relaisTries() {
		List<RelaisDefinition> l = new ArrayList<>(RELAIS.values());
		l.sort(Comparator.comparingInt(RelaisDefinition::ordre).thenComparing(RelaisDefinition::id));
		return l;
	}

	/** Chargement direct (tests). */
	public static void chargerDepuisJson(Map<String, String> projets, Map<String, String> lieux, Map<String, String> relais) {
		Map<String, ProjetDefinition> p = new LinkedHashMap<>();
		projets.forEach((id, texte) -> p.put(id, ProjetDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		Map<String, LieuDefinition> l = new LinkedHashMap<>();
		lieux.forEach((id, texte) -> l.put(id, LieuDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		Map<String, RelaisDefinition> r = new LinkedHashMap<>();
		relais.forEach((id, texte) -> r.put(id, RelaisDefinition.parse(id, JsonParser.parseString(texte).getAsJsonObject())));
		PROJETS.clear();
		PROJETS.putAll(p);
		LIEUX.clear();
		LIEUX.putAll(l);
		RELAIS.clear();
		RELAIS.putAll(r);
	}

	/** Copie des lieux et relais reçue du serveur (client sur serveur dédié). */
	public static void recevoir(Map<String, LieuDefinition> lieux, Map<String, RelaisDefinition> relais) {
		LIEUX.clear();
		LIEUX.putAll(lieux);
		RELAIS.clear();
		RELAIS.putAll(relais);
	}

	@Override
	public Identifier getFabricId() {
		return LesLimbes.id("domaine");
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<String, ProjetDefinition> p = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("projet", id -> id.getPath().endsWith(".json")).entrySet()) {
			String path = entry.getKey().getPath();
			String id = path.substring("projet/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				p.put(id, ProjetDefinition.parse(id, json));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Projet invalide {} : {}", entry.getKey(), e.getMessage());
			}
		}
		Map<String, LieuDefinition> l = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("lieu", id -> id.getPath().endsWith(".json")).entrySet()) {
			String path = entry.getKey().getPath();
			String id = path.substring("lieu/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				l.put(id, LieuDefinition.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Lieu invalide {} : {}", entry.getKey(), e.getMessage());
			}
		}
		Map<String, RelaisDefinition> r = new LinkedHashMap<>();
		for (Map.Entry<Identifier, Resource> entry : manager.findResources("relais", id -> id.getPath().endsWith(".json")).entrySet()) {
			String path = entry.getKey().getPath();
			String id = path.substring("relais/".length(), path.length() - ".json".length());
			try (BufferedReader reader = entry.getValue().getReader()) {
				r.put(id, RelaisDefinition.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Relais invalide {} : {}", entry.getKey(), e.getMessage());
			}
		}
		PROJETS.clear();
		PROJETS.putAll(p);
		LIEUX.clear();
		LIEUX.putAll(l);
		RELAIS.clear();
		RELAIS.putAll(r);
		LesLimbes.LOGGER.info("{} projet(s) de domaine, {} lieu(x) d'intérêt, {} relais.", PROJETS.size(), LIEUX.size(), RELAIS.size());
	}
}
