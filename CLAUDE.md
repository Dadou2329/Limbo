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
- Tout texte joueur en français ; `en_us.json` est une copie de `fr_fr.json`.
