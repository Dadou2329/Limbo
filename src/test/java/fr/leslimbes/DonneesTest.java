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
		for (int[] site : new int[][]{{0, 140, 66}, {-120, 150, 66}, {150, 60, 66}, {0, 56, 66}, {70, 210, 66}, {-80, 80, 66}, {220, 150, 66}, {-60, 20, 66},
				{950, -750, 66}, {1050, 350, 72}, {150, 1250, 66}, {-1150, 500, 96}, {-610, -610, 82}, {-900, -720, 78}, {-900, -900, 86}}) {
			assertTrue(texte.contains("\"x\":" + site[0] + ",\"z\":" + site[1] + ",\"y\":" + site[2]), "site aplani manquant : " + site[0] + "," + site[1] + " à " + site[2]);
		}
		assertEquals(0, fr.leslimbes.world.dimension.LimbesSites.CLAIRVAL.getX());
		assertEquals(140, fr.leslimbes.world.dimension.LimbesSites.CLAIRVAL.getZ());
		assertEquals(66, fr.leslimbes.world.dimension.LimbesSites.SOL_PRAIRIES);
		// Ensembles de structures à position fixe : le chunk déclaré contient bien le site.
		assertEquals(-60, fr.leslimbes.world.dimension.LimbesSites.LAC_DU_CREPUSCULE.getX());
		assertEquals(70, fr.leslimbes.world.dimension.LimbesSites.ARRIVEE_PRAIRIES_1.getX());
		for (String[] s : new String[][]{{"clairval", "0", "140"}, {"manoir", "-120", "150"}, {"relais_royal", "150", "60"}, {"sanctuaire", "0", "0"}, {"lac_crepuscule", "-60", "20"}}) {
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

	@Test
	void lesArriveesSontValidesEtLeursSitesAplanis() throws IOException {
		List<Path> defs = fichiers(DATA.resolve("arrivee"), ".json");
		assertFalse(defs.isEmpty(), "aucune Arrivée définie");
		java.util.Map<String, String> textes = new java.util.LinkedHashMap<>();
		for (Path p : defs) {
			textes.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		java.util.Map<String, String> sites = new java.util.LinkedHashMap<>();
		for (Path p : fichiers(DATA.resolve("arrivee_site"), ".json")) {
			sites.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		fr.leslimbes.arrivee.ArriveeLoader.chargerDepuisJson(textes, sites);
		assertEquals(defs.size(), fr.leslimbes.arrivee.ArriveeLoader.definitions().size());
		String terrain = json(DATA.resolve("worldgen/density_function/terrain.json")).toString();
		for (fr.leslimbes.arrivee.ArriveeSite site : fr.leslimbes.arrivee.ArriveeLoader.sites().values()) {
			assertTrue(terrain.contains("\"x\":" + site.pos().getX() + ",\"z\":" + site.pos().getZ()), "site d'Arrivée non aplani : " + site.id());
		}
		for (fr.leslimbes.arrivee.ArriveeDefinition def : fr.leslimbes.arrivee.ArriveeLoader.definitions().values()) {
			String gabarit = def.gabarit().getPath();
			assertTrue(Files.exists(DATA.resolve("structure/" + gabarit + ".nbt")), "gabarit d'Arrivée manquant : " + gabarit);
			assertTrue(fr.leslimbes.arrivee.ArriveeLoader.sites().values().stream().anyMatch(st -> st.groupe().equals(def.groupeSites())), "aucun site pour le groupe " + def.groupeSites());
			assertFalse(def.titre().isBlank());
		}
	}

	@Test
	void lesProjetsLieuxEtRelaisSontCoherents() throws IOException {
		java.util.Map<String, String> projets = new java.util.LinkedHashMap<>();
		for (Path p : fichiers(DATA.resolve("projet"), ".json")) {
			projets.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		java.util.Map<String, String> lieux = new java.util.LinkedHashMap<>();
		for (Path p : fichiers(DATA.resolve("lieu"), ".json")) {
			lieux.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		java.util.Map<String, String> relais = new java.util.LinkedHashMap<>();
		for (Path p : fichiers(DATA.resolve("relais"), ".json")) {
			relais.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		fr.leslimbes.domaine.DomaineLoader.chargerDepuisJson(projets, lieux, relais);
		assertTrue(fr.leslimbes.domaine.DomaineLoader.projets().size() >= 10, "au moins dix projets de domaine");
		assertTrue(fr.leslimbes.domaine.DomaineLoader.lieux().size() >= 10, "au moins dix lieux d'intérêt");
		assertEquals(9, fr.leslimbes.domaine.DomaineLoader.relais().size());
		Set<String> prerequisConnus = Set.of("drapeau", "drapeau_monde", "sans_drapeau", "sans_drapeau_monde", "pouvoir", "quete_terminee", "quete_active", "objet", "tout", "un_de", "non");
		Set<String> actionsConnues = Set.of("message", "drapeau", "drapeau_monde", "deplacer_pnj", "donner", "pouvoir", "decouvrir_relais", "decouvrir_lieu", "titre", "son",
				"experience", "compteur", "modifier_bloc", "demarrer_quete", "objectif", "majeste_max", "apparition");
		List<String> erreurs = new ArrayList<>();
		for (fr.leslimbes.domaine.ProjetDefinition def : fr.leslimbes.domaine.DomaineLoader.projets().values()) {
			if (!Files.exists(DATA.resolve("structure/" + def.gabarit().getPath() + ".nbt"))) {
				erreurs.add(def.id() + " : gabarit manquant " + def.gabarit());
			}
			def.avant().ifPresent(a -> {
				if (!Files.exists(DATA.resolve("structure/" + a.getPath() + ".nbt"))) {
					erreurs.add(def.id() + " : gabarit antérieur manquant " + a);
				}
			});
			if (def.indice().isBlank() || def.description().isBlank()) {
				erreurs.add(def.id() + " : description et indice obligatoires");
			}
			for (fr.leslimbes.domaine.ProjetDefinition.Cout c : def.cout()) {
				if (c.objet().getNamespace().equals("les_limbes") && !lang.containsKey("item.les_limbes." + c.objet().getPath()) && !lang.containsKey("block.les_limbes." + c.objet().getPath())) {
					erreurs.add(def.id() + " : coût inconnu " + c.objet());
				}
			}
			for (JsonObject p : def.prerequis()) {
				for (String k : p.keySet()) {
					if (!prerequisConnus.contains(k)) {
						erreurs.add(def.id() + " : prérequis inconnu " + k);
					}
				}
			}
			for (JsonObject a : def.apres()) {
				if (!actionsConnues.contains(a.get("type").getAsString())) {
					erreurs.add(def.id() + " : action de suite inconnue " + a.get("type"));
				}
			}
			// Toute annexe reste sur le plateau aplani du manoir (rayon 40 autour de -120/150).
			double d = Math.hypot(def.origine().getX() + 120, def.origine().getZ() - 150);
			if (d > 40) {
				erreurs.add(def.id() + " : origine hors du plateau du manoir (" + (int) d + " blocs)");
			}
		}
		Set<String> nomsLieux = new HashSet<>();
		for (fr.leslimbes.domaine.LieuDefinition lieu : fr.leslimbes.domaine.DomaineLoader.lieux().values()) {
			if (!nomsLieux.add(lieu.nom())) {
				erreurs.add("nom de lieu en double : " + lieu.nom());
			}
			if (!lieu.relais().isEmpty() && fr.leslimbes.domaine.DomaineLoader.relais(lieu.relais()) == null) {
				erreurs.add(lieu.id() + " : relais inconnu " + lieu.relais());
			}
		}
		// Chaque relais a sa porte dans un gabarit (identifiant inscrit dans le bloc-entité) et chaque porte un relais défini.
		StringBuilder structures = new StringBuilder();
		for (Path py : fichiers(Paths.get("tools/structgen"), ".py")) {
			structures.append(Files.readString(py, StandardCharsets.UTF_8)).append('\n');
		}
		Set<String> portes = new HashSet<>();
		for (String motif : List.of("\"relais\": \"(\\w+)\"", "relais\\(c, [^\\n]*?\"(\\w+)\"\\)")) {
			java.util.regex.Matcher m = java.util.regex.Pattern.compile(motif).matcher(structures);
			while (m.find()) {
				portes.add(m.group(1));
			}
		}
		for (String id : fr.leslimbes.domaine.DomaineLoader.relais().keySet()) {
			if (!portes.contains(id)) {
				erreurs.add("relais sans porte dans les gabarits : " + id);
			}
		}
		for (String id : portes) {
			if (fr.leslimbes.domaine.DomaineLoader.relais(id) == null) {
				erreurs.add("porte de relais vers un relais inconnu : " + id);
			}
		}
		assertTrue(erreurs.isEmpty(), String.join("\n", erreurs));
	}

	@Test
	void lesDuelsSontValidesEtReferences() throws IOException {
		java.util.Map<String, String> fichiers = new java.util.LinkedHashMap<>();
		for (Path p : fichiers(DATA.resolve("duel"), ".json")) {
			fichiers.put(p.getFileName().toString().replace(".json", ""), Files.readString(p, StandardCharsets.UTF_8));
		}
		assertFalse(fichiers.isEmpty(), "aucun duel trouvé");
		fr.leslimbes.duel.DuelLoader.chargerDepuisJson(fichiers);
		assertTrue(fr.leslimbes.duel.DuelLoader.tous().size() >= 8, "au moins huit duels et épreuves");
		Set<String> actionsConnues = Set.of("message", "drapeau", "drapeau_monde", "deplacer_pnj", "donner", "pouvoir", "decouvrir_relais", "decouvrir_lieu", "titre", "son",
				"experience", "compteur", "modifier_bloc", "demarrer_quete", "objectif", "majeste_max", "apparition", "retirer", "duel", "capitale", "couronner", "delegation");
		List<String> erreurs = new ArrayList<>();
		for (fr.leslimbes.duel.DuelDefinition d : fr.leslimbes.duel.DuelLoader.tous().values()) {
			if (!lang.containsKey("entity.les_limbes." + d.boss().getPath())) {
				erreurs.add(d.id() + " : boss sans nom " + d.boss());
			}
			if (!d.pretendant().isBlank() && !lang.containsKey("pnj.les_limbes." + d.pretendant())) {
				erreurs.add(d.id() + " : prétendant sans nom " + d.pretendant());
			}
			float precedent = 1.0F;
			for (fr.leslimbes.duel.DuelDefinition.Phase ph : d.phases()) {
				if (ph.seuil() >= precedent || ph.seuil() <= 0.0F) {
					erreurs.add(d.id() + " : seuils de phases non décroissants");
				}
				precedent = ph.seuil();
				for (fr.leslimbes.duel.DuelDefinition.Renfort r : ph.invocations()) {
					if (!lang.containsKey("entity.les_limbes." + r.type().getPath())) {
						erreurs.add(d.id() + " : invocation inconnue " + r.type());
					}
				}
				if (!ph.blocs().isBlank() && !Set.of("racines", "forteresse", "miroirs", "sables").contains(ph.blocs())) {
					erreurs.add(d.id() + " : motif de blocs inconnu " + ph.blocs());
				}
			}
			for (fr.leslimbes.duel.DuelDefinition.Renfort r : d.renforts()) {
				if (!lang.containsKey("entity.les_limbes." + r.type().getPath())) {
					erreurs.add(d.id() + " : renfort inconnu " + r.type());
				}
			}
			for (JsonObject a : d.victoire()) {
				if (!actionsConnues.contains(a.get("type").getAsString())) {
					erreurs.add(d.id() + " : action de victoire inconnue " + a.get("type"));
				}
			}
			for (JsonObject a : d.echec()) {
				if (!actionsConnues.contains(a.get("type").getAsString())) {
					erreurs.add(d.id() + " : action d'échec inconnue " + a.get("type"));
				}
			}
			if (d.rayon() < 8 || d.rayon() > 40) {
				erreurs.add(d.id() + " : rayon d'arène déraisonnable " + d.rayon());
			}
		}
		// Tout duel lancé par un dialogue existe.
		java.util.regex.Pattern motif = java.util.regex.Pattern.compile("\\\"type\\\": *\\\"duel\\\", *\\\"id\\\": *\\\"(\\w+)\\\"");
		for (Path p : fichiers(DATA.resolve("dialogue"), ".json")) {
			java.util.regex.Matcher m = motif.matcher(Files.readString(p, StandardCharsets.UTF_8));
			while (m.find()) {
				if (fr.leslimbes.duel.DuelLoader.get(m.group(1)) == null) {
					erreurs.add(p.getFileName() + " : duel inconnu " + m.group(1));
				}
			}
		}
		assertTrue(erreurs.isEmpty(), String.join("\n", erreurs));
	}

	@Test
	void lesModelesDEntitesPilotesParDonneesSontCoherents() throws IOException {
		List<Path> modeles = fichiers(ASSETS.resolve("models/entite"), ".json");
		assertFalse(modeles.isEmpty(), "aucun modèle d'entité");
		for (Path p : modeles) {
			String id = p.getFileName().toString().replace(".json", "");
			fr.leslimbes.client.render.modele.ModeleDonnees m = fr.leslimbes.client.render.modele.ModeleDonnees.parse(json(p));
			Set<String> os = new java.util.HashSet<>();
			for (var o : m.os()) {
				assertTrue(os.add(o.nom()), id + " : os en double " + o.nom());
			}
			for (var o : m.os()) {
				assertTrue(o.parent().isEmpty() || os.contains(o.parent()), id + " : parent inconnu " + o.parent());
				for (var c : o.cubes()) {
					assertTrue(c.u() + 2 * (c.sz() + c.sx()) <= m.largeurTexture() && c.v() + c.sz() + c.sy() <= m.hauteurTexture(), id + "/" + o.nom() + " : cube hors de la texture");
				}
			}
			for (var a : m.animations()) {
				assertTrue(os.contains(a.os()), id + " : animation sur un os inconnu " + a.os());
				assertTrue(Set.of("rx", "ry", "rz", "x", "y", "z", "echelle").contains(a.propriete()), id + " : propriété inconnue " + a.propriete());
			}
			assertTrue(Files.exists(ASSETS.resolve("textures/entity/" + id + ".png")), id + " : texture manquante");
			if (m.lueur()) {
				assertTrue(Files.exists(ASSETS.resolve("textures/entity/" + id + "_lueur.png")), id + " : texture émissive manquante");
			}
		}
	}

	@Test
	void lesObjetsDesQuetesExistent() throws IOException {
		for (Path p : fichiers(DATA.resolve("quest"), ".json")) {
			JsonObject q = json(p);
			for (var e : q.getAsJsonArray("etapes")) {
				for (var o : e.getAsJsonObject().getAsJsonArray("objectifs")) {
					JsonObject obj = o.getAsJsonObject();
					String type = obj.get("type").getAsString();
					if (type.equals("collecter")) {
						net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of(obj.get("cible").getAsString());
						boolean connu = id.getNamespace().equals("minecraft") ? net.minecraft.registry.Registries.ITEM.containsId(id)
								: lang.containsKey("item.les_limbes." + id.getPath()) || lang.containsKey("block.les_limbes." + id.getPath());
						assertTrue(connu, p.getFileName() + " : objet inconnu " + id);
					}
					if (type.equals("vaincre")) {
						net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of(obj.get("cible").getAsString());
						boolean connu = id.getNamespace().equals("minecraft") ? net.minecraft.registry.Registries.ENTITY_TYPE.containsId(id)
								: lang.containsKey("entity.les_limbes." + id.getPath());
						assertTrue(connu, p.getFileName() + " : entité inconnue " + id);
					}
					if (type.equals("parler")) {
						assertTrue(lang.containsKey("pnj.les_limbes." + obj.get("cible").getAsString()), p.getFileName() + " : rôle sans nom " + obj.get("cible"));
					}
				}
			}
		}
	}
}
