package fr.leslimbes.client.render.modele;

import com.google.gson.JsonParser;
import fr.leslimbes.LesLimbes;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/** Cache des modèles pilotés par données, vidé à chaque rechargement des ressources. */
public final class ModeleDonneesRegistry {
	private static final Map<String, ModeleDonnees> CACHE = new HashMap<>();
	private static int generation;

	public static ModeleDonnees get(String id) {
		return CACHE.computeIfAbsent(id, ModeleDonneesRegistry::charger);
	}

	/** Change à chaque rechargement : les rendus reconstruisent leur modèle quand elle diffère. */
	public static int generation() {
		return generation;
	}

	private static ModeleDonnees charger(String id) {
		Identifier res = LesLimbes.id("models/entite/" + id + ".json");
		try (Reader reader = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(res).getReader()) {
			return ModeleDonnees.parse(JsonParser.parseReader(reader).getAsJsonObject());
		} catch (Exception e) {
			LesLimbes.LOGGER.error("Modèle d'entité illisible {} : {}", res, e.toString());
			return ModeleDonnees.vide();
		}
	}

	public static void init() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return LesLimbes.id("modeles_entites");
			}

			@Override
			public void reload(ResourceManager manager) {
				CACHE.clear();
				generation++;
			}
		});
	}

	private ModeleDonneesRegistry() {
	}
}
