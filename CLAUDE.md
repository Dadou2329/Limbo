# Les Limbes — notes pour reprendre le développement

Mod Minecraft Java 1.21.1 / Fabric, entièrement en français pour le joueur. Cahier des charges : `docs/CAHIER_DES_CHARGES.md` (référence jusqu'à la dimension complète). État et suite : `docs/ETAT_AVANCEMENT.md`, `docs/BACKLOG.md`.

## Chaîne
- JDK 21, Gradle 9.8.0 (wrapper), Loom 1.17.21 (Loom ≥ 1.18 exige un JVM 25), Yarn 1.21.1+build.3, Fabric API 0.116.17+1.21.1.
- `./gradlew build` · `./gradlew test` · `./gradlew runGametest` (serveur headless, EULA déjà acceptée dans `run/gametest/eula.txt`) · `./gradlew runServer`.
- `./gradlew runAutotest` : serveur dédié réel qui génère les Limbes et vérifie lieux/PNJ (`AutoTest.java`, journal « AUTOTEST OK/ECHEC »), EULA dans `run/autotest/eula.txt`.
- `./gradlew runCapture` : client sur la sauvegarde `run/client/saves/Limbes` avec captures automatiques (`CaptureAutomatique.java`) ; sans écran : Xvfb + `LIBGL_ALWAYS_SOFTWARE=1`.
- Mixins (`fr.leslimbes.mixin`, liste dans `les_limbes.mixins.json`) : seulement ceux qui déclarent la dimension stable (sinon avertissement « réglages expérimentaux » à chaque ouverture de monde).
- Sources décompilées (vérifier une signature d'API) : `./gradlew genSources` puis `.gradle/loom-cache/minecraftMaven/.../*-sources.jar`.

## Générateurs (source des assets et données ; ne pas éditer les fichiers générés)
1. `python3 tools/textures/gen_textures.py`
2. `python3 tools/sounds/gen_sounds.py` (ffmpeg)
3. `python3 tools/datagen/gen_data.py`
4. `python3 tools/structgen/build_structures.py --rapport`
Puis `./gradlew build`. Les fichiers écrits à la main : `assets/les_limbes/lang/fr_fr.json`, `data/les_limbes/quest/*.json`, `data/les_limbes/dialogue/*.json`.

## Invariants
- Serveur autoritaire ; aucune classe cliente côté serveur (sourcesets `main` / `client`).
- Sites fixes : `LimbesSites.java` ⇔ `SITES` dans `gen_data.py` ⇔ pièces dans `build_structures.py` (test JUnit `lesSitesFixesSontCoherentsAvecLaGeneration`).
- Actions/conditions de dialogues et quêtes : `npc/dialogue/Actions.java`, `Conditions.java` ; objectifs : `quest/QuestDefinition.java`.
- Rôles essentiels (invulnérables, réapparition) : `HabitantEntity.ROLES_ESSENTIELS` ; chaque rôle a un skin, un nom `pnj.les_limbes.<role>` et un dialogue.
- Projets de domaine : `data/les_limbes/projet/*.json` ⇔ gabarits `manoir/*` dans `build_structures.py` (origines sur le plateau aplani du manoir, rayon 40) ; relais : `data/les_limbes/relais/*.json` ⇔ portes `les_limbes:porte_de_relais` posées dans les gabarits (test JUnit `lesProjetsLieuxEtRelaisSontCoherents`).
- Créatures régionales : une fiche par espèce dans `registry/LimbesEspeces.java` (archétypes `entity/region`), un modèle du même identifiant dans `tools/models/modeles_regions.py`, un œuf `oeuf_<id>` (texture dans `gen_textures.py`, modèle dans `gen_data.py`), des clés `entity.les_limbes.<id>` et `item.les_limbes.oeuf_<id>`, des apparitions dans `SPAWNS_REGIONAUX` (`gen_data.py`) dont le groupe correspond au `SpawnGroup` de l'archétype.
- Duels : `data/les_limbes/duel/*.json` (boss = type d'entité de `LimbesPretendants` ou espèce ordinaire, arène, phases) lancés par l'action de dialogue `duel` ; la victoire pose `duel_<id>_gagne`. Les boss ont un modèle du même identifiant dans `modeles_regions.py` et un nom `entity.les_limbes.<id>` (test JUnit `lesDuelsSontValidesEtReferences`).
- Capitale : blocs `porte_royale` (niveau 1 quartiers, 2 palais), `lanterne_royale_eteinte`, `pont_royal` ; éveil par `CapitaleManager.eveiller` (action `capitale`), drapeaux de monde `capitale_niveau_<n>` ; couronnement par l'action `couronner` (drapeau joueur `couronne`, monde `couronnement_fait`).
- Pouvoirs : chaque `Ability` de `power/Abilities.java` (33, branches `BRANCHES`) a ses clés `pouvoir.les_limbes.<id>`, `.court`, `.description` (test JUnit lisant la source) ; exécution serveur dans la classe de sa branche, effet client dans `EffetsClient`, état transitoire dans `EtatPouvoirs` ; les techniques de zone passent par `Allies` (jamais d'allié touché).
- Garde astrale : espèces d'archétype `GARDE` (`chevalier_astral`, `archer_astral`, `protecteur_astral`) servies par `GardeAstraleEntity`, budget `GardeAstrale.BUDGET`.
- Action de dialogue `poser_structure` (gabarit posé une fois, drapeau de monde) ; structures de l'acte V dans `tools/structgen/regne.py`.
- Quêtes secondaires de l'étape 8 : générées par `tools/datagen/gen_quetes_secondaires.py` (spécification en Python → `quest/*.json` + nœuds de dialogue `<quete>_cours`, `<quete>_fin`, choix d'offre) ; relancer le script réécrit les 27 fichiers et re-branche les dialogues (idempotent sur les quêtes, pas sur les dialogues : ne le relancer que sur des dialogues sans ces nœuds).
- Arrivées : `data/les_limbes/arrivee/*.json` ⇔ gabarits `arrivees/*` (`tools/structgen/arrivees.py`, `arenes.py`, `regne.py`, `build_structures.py`) ⇔ sites `arrivee_site/*.json` aplanis dans `SITES` (test JUnit `lesArriveesSontValidesEtLeursSitesAplanis`).
- Musique : `musique.<region>` dans `sounds.json` (flux) générée par `gen_sounds.py` (`musiques()`), affectée aux biomes par région dans `biome()` de `gen_data.py` ; **chaque son de `sounds.json` doit être enregistré dans `LimbesSounds`** (le champ `music` des biomes est résolu dans le registre : sinon aucun monde ne se charge) — test JUnit `lesMusiquesDesBiomesSontDesSonsEnregistres`.
- Terrain naturel : tout bloc que la génération peut poser (règles de surface, features, `LacFeature`) figure dans `TERRAIN_NATUREL` de `gen_data.py` → étiquette `les_limbes:terrain_naturel`, lue par `ArriveeManager.naturel` ; jamais un bloc de construction (taillé, poli, planches…) dans la génération (test JUnit `leTerrainNaturelCouvreLaGenerationSansBlocsDeConstruction`).
- Budgets serveur : `config/les_limbes.json` (`LimbesConfig`) ; options client : `OptionsLimbes` (densité, profil graphique, secousses, flashes, marqueurs, indications).
- Tout texte joueur en français ; `en_us.json` est une copie de `fr_fr.json`.
