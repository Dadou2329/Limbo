package fr.leslimbes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.leslimbes.npc.dialogue.DialogueGraph;
import fr.leslimbes.quest.QuestDefinition;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validation des données du mod sans lancer le jeu : quêtes et dialogues valides, références de
 * langue, cohérence des sites fixes entre le code et les fichiers de génération.
 */
class DonneesTest {
	static final Path RES = Paths.get("src/main/resources");
	static final Path DATA = RES.resolve("data/les_limbes");
	static final Path ASSETS = RES.resolve("assets/les_limbes");
	static Map<String, com.google.gson.JsonElement> lang;

	@BeforeAll
	static void bootstrap() throws IOException {
		net.minecraft.SharedConstants.createGameVersion();
		net.minecraft.Bootstrap.initialize();
		lang = JsonParser.parseString(Files.readString(ASSETS.resolve("lang/fr_fr.json"), StandardCharsets.UTF_8)).getAsJsonObject().asMap();
	}

	static List<Path> fichiers(Path dir, String suffixe) throws IOException {
		if (!Files.isDirectory(dir)) {
			return List.of();
		}
		try (Stream<Path> s = Files.walk(dir)) {
			return s.filter(p -> p.toString().endsWith(suffixe)).sorted().toList();
		}
	}

	static JsonObject json(Path p) throws IOException {
		return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	@Test
	void lesQuetesSontValides() throws IOException {
		List<Path> fichiers = fichiers(DATA.resolve("quest"), ".json");
		assertFalse(fichiers.isEmpty(), "aucune quête trouvée");
		Set<String> ids = new HashSet<>();
		for (Path p : fichiers) {
			String id = p.getFileName().toString().replace(".json", "");
			QuestDefinition def = QuestDefinition.parse(id, json(p));
			ids.add(id);
			assertFalse(def.etapes().isEmpty(), id + " : aucune étape");
			for (QuestDefinition.Etape e : def.etapes()) {
				assertFalse(e.resume().isBlank(), id + "/" + e.id() + " : résumé vide");
				for (QuestDefinition.Objectif o : e.objectifs()) {
					assertFalse(o.texte().isBlank(), id + "/" + e.id() + "/" + o.id() + " : texte vide");
					if (o.type() == QuestDefinition.TypeObjectif.ATTEINDRE) {
						assertTrue(o.pos().isPresent() || o.cleLieu().isPresent(), id + "/" + e.id() + "/" + o.id() + " : objectif atteindre sans position ni clé de lieu");
					}
				}
			}
		}
		assertTrue(ids.contains("le_passage") && ids.contains("l_etranger"), "les quêtes de l'acte I doivent exister");
	}

	@Test
	void lesQuetesReferencentDesQuetesConnues() throws IOException {
		Set<String> ids = new HashSet<>();
		for (Path p : fichiers(DATA.resolve("quest"), ".json")) {
			ids.add(p.getFileName().toString().replace(".json", ""));
		}
		List<String> erreurs = new ArrayList<>();
		for (Path p : fichiers(DATA.resolve("quest"), ".json")) {
			verifierActions(json(p).toString(), ids, erreurs, p.getFileName().toString());
		}
		for (Path p : fichiers(DATA.resolve("dialogue"), ".json")) {
			verifierActions(json(p).toString(), ids, erreurs, p.getFileName().toString());
		}
		assertTrue(erreurs.isEmpty(), String.join("\n", erreurs));
	}

	private static void verifierActions(String texte, Set<String> quetes, List<String> erreurs, String fichier) {
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"quete\":\"([a-z_]+)\"").matcher(texte);
		while (m.find()) {
			if (!quetes.contains(m.group(1))) {
				erreurs.add(fichier + " : référence à une quête inconnue « " + m.group(1) + " »");
			}
		}
	}

	@Test
	void lesDialoguesSontValidesEtLeursPnjOntUnNom() throws IOException {
		List<Path> fichiers = fichiers(DATA.resolve("dialogue"), ".json");
		assertFalse(fichiers.isEmpty(), "aucun dialogue trouvé");
		for (Path p : fichiers) {
			String id = p.getFileName().toString().replace(".json", "");
			DialogueGraph graph = DialogueGraph.parse(id, json(p));
			assertTrue(graph.valider().isEmpty(), id + " : " + graph.valider());
			assertTrue(lang.containsKey("pnj.les_limbes." + id), "nom manquant dans fr_fr.json pour le rôle " + id);
			for (DialogueGraph.Noeud n : graph.noeuds().values()) {
				for (String ligne : n.texte()) {
					assertTrue(ligne.length() <= 400, id + "/" + n.id() + " : réplique trop longue pour l'écran");
				}
				assertTrue(n.choix().size() <= 4, id + "/" + n.id() + " : trop de choix (max 4 à l'écran)");
			}
		}
		for (String essentiel : fr.leslimbes.npc.HabitantEntity.ROLES_ESSENTIELS) {
			assertTrue(Files.exists(DATA.resolve("dialogue/" + essentiel + ".json")), "dialogue manquant pour le personnage essentiel " + essentiel);
		}
	}

	@Test
	void lesLanguesCouvrentBlocsObjetsEntitesEtPouvoirs() throws IOException {
		List<String> manquantes = new ArrayList<>();
		for (Path p : fichiers(ASSETS.resolve("blockstates"), ".json")) {
			String id = p.getFileName().toString().replace(".json", "");
			if (!lang.containsKey("block.les_limbes." + id)) {
				manquantes.add("block.les_limbes." + id);
			}
		}
		for (Path p : fichiers(ASSETS.resolve("models/item"), ".json")) {
			String id = p.getFileName().toString().replace(".json", "");
			if (!lang.containsKey("item.les_limbes." + id) && !lang.containsKey("block.les_limbes." + id)) {
				manquantes.add("item.les_limbes." + id);
			}
		}
		for (String pouvoir : List.of("epee_du_souverain", "pas_court", "decret_reculez")) {
			for (String suffixe : List.of("", ".court", ".description")) {
				if (!lang.containsKey("pouvoir.les_limbes." + pouvoir + suffixe)) {
					manquantes.add("pouvoir.les_limbes." + pouvoir + suffixe);
				}
			}
		}
		JsonObject sounds = json(ASSETS.resolve("sounds.json"));
		for (String key : sounds.keySet()) {
			String subtitle = sounds.getAsJsonObject(key).get("subtitle").getAsString();
			if (!lang.containsKey(subtitle)) {
				manquantes.add(subtitle);
			}
			for (var s : sounds.getAsJsonObject(key).getAsJsonArray("sounds")) {
				String path = s.getAsString().substring("les_limbes:".length());
				assertTrue(Files.exists(ASSETS.resolve("sounds/" + path + ".ogg")), "fichier son manquant : " + path);
			}
		}
		assertTrue(manquantes.isEmpty(), "clés de langue manquantes : " + manquantes);
		assertEquals(Files.readString(ASSETS.resolve("lang/fr_fr.json")), Files.readString(ASSETS.resolve("lang/en_us.json")), "en_us doit être la copie de fr_fr (jeu entièrement en français)");
	}

	@Test
	void lesTexturesDesModelesExistent() throws IOException {
		List<String> manquantes = new ArrayList<>();
		for (Path p : fichiers(ASSETS.resolve("models"), ".json")) {
			JsonObject model = json(p);
			if (!model.has("textures")) {
				continue;
			}
			for (Map.Entry<String, com.google.gson.JsonElement> e : model.getAsJsonObject("textures").entrySet()) {
				String tex = e.getValue().getAsString();
				if (!tex.startsWith("les_limbes:")) {
					continue;
				}
				Path png = ASSETS.resolve("textures/" + tex.substring("les_limbes:".length()) + ".png");
				if (!Files.exists(png)) {
					manquantes.add(p.getFileName() + " -> " + tex);
				}
			}
		}
		assertTrue(manquantes.isEmpty(), "textures manquantes : " + manquantes);
	}

	@Test
	void lesSitesFixesSontCoherentsAvecLaGeneration() throws IOException {
		JsonObject terrain = json(DATA.resolve("worldgen/density_function/terrain.json"));
		String texte = terrain.toString();
		assertTrue(texte.contains("\"les_limbes:aplanir\""), "la fonction d'aplanissement doit être utilisée");
		for (int[] site : new int[][]{{0, 140}, {-120, 150}, {150, 60}, {0, 56}}) {
			assertTrue(texte.contains("\"x\":" + site[0] + ",\"z\":" + site[1] + ",\"y\":66"), "site aplani manquant : " + site[0] + "," + site[1]);
		}
		assertEquals(0, fr.leslimbes.world.dimension.LimbesSites.CLAIRVAL.getX());
		assertEquals(140, fr.leslimbes.world.dimension.LimbesSites.CLAIRVAL.getZ());
		assertEquals(66, fr.leslimbes.world.dimension.LimbesSites.SOL_PRAIRIES);
		// Ensembles de structures à position fixe : le chunk déclaré contient bien le site.
		for (String[] s : new String[][]{{"clairval", "0", "140"}, {"manoir", "-120", "150"}, {"relais_royal", "150", "60"}, {"sanctuaire", "0", "0"}}) {
			JsonObject set = json(DATA.resolve("worldgen/structure_set/" + s[0] + ".json"));
			var chunk = set.getAsJsonObject("placement").getAsJsonArray("chunks").get(0).getAsJsonArray();
			assertEquals(Integer.parseInt(s[1]) >> 4, chunk.get(0).getAsInt(), s[0] + " : chunk x");
			assertEquals(Integer.parseInt(s[2]) >> 4, chunk.get(1).getAsInt(), s[0] + " : chunk z");
			JsonObject structure = json(DATA.resolve("worldgen/structure/" + s[0] + ".json"));
			for (var piece : structure.getAsJsonArray("pieces")) {
				String gabarit = piece.getAsJsonObject().get("gabarit").getAsString().substring("les_limbes:".length());
				assertTrue(Files.exists(DATA.resolve("structure/" + gabarit + ".nbt")), "gabarit manquant : " + gabarit);
			}
		}
	}
}
