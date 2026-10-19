# Documentation de développement

## Chaîne d'outils

- Minecraft 1.21.1, Fabric Loader 0.20.0, Fabric API 0.116.17+1.21.1, Yarn 1.21.1+build.3, Loom 1.17.21, Gradle 9.8.0, JDK 21.
- `./gradlew build` compile et produit `build/libs/les-limbes-<version>.jar`.
- `./gradlew test` : JUnit (validation des données, logique pure).
- `./gradlew runGametest` : serveur headless exécutant les GameTests (`fr.leslimbes.gametest`).
- `./gradlew runAutotest` : serveur dédié réel (`run/autotest`, EULA à accepter) qui génère les Limbes, vérifie les lieux fixes et les personnages, journalise « AUTOTEST OK/ECHEC » puis s'arrête (`AutoTest.java`).
- `./gradlew runCapture` : client lancé sur la sauvegarde `Limbes` (`run/client/saves/Limbes`) avec le scénario de captures d'écran automatiques (`client/dev/CaptureAutomatique.java`) ; fonctionne sans écran via Xvfb et Mesa logiciel (`DISPLAY=:99 LIBGL_ALWAYS_SOFTWARE=1`). Les images sont écrites dans `run/client/screenshots/`.
- `./gradlew runServer` : serveur dédié de développement dans `run/server` (acceptez l'EULA dans `run/server/eula.txt`).
- `./gradlew genSources` : sources décompilées de Minecraft (vérification des signatures d'API).

## Organisation du code (`src/main/java/fr/leslimbes`)

| Paquet | Responsabilité |
|---|---|
| `registry` | enregistrement des blocs, objets, entités, particules, sons, attachements, réseau, types de génération |
| `block` | blocs à comportement (Porte des Limbes, herbe argentée, plantes, cristal) |
| `item` | objets à comportement (boussole nacrée, Épée du Souverain, fragment de couronne) |
| `world/dimension` | clés de la dimension, voyage par les portes, protection des brumes, positions des sites |
| `world/gen` | fonctions de densité `les_limbes:aplanir` et `les_limbes:regions`, features Java (lacs, mer, routes) |
| `world/biome` | source de biomes régionale `les_limbes:regions` (Voronoï pondéré, transitions, sous-biomes) ; `LimbesRegions` porte les formules de relief |
| `world/structure` | placement fixe `les_limbes:fixed`, structure composée `les_limbes:composee`, pièce avec marqueurs |
| `world/state` | données du joueur (attachement persistant), état du monde (PersistentState), synchronisation |
| `npc` | entité Habitant, réapparition des personnages essentiels, dialogues (`npc/dialogue`) |
| `quest` | définitions, chargeur, progression, gestionnaire, signaux |
| `power` | techniques royales (registre, arsenal, portes, décrets), majesté, recharges |
| `entity` | créatures des Prairies (poisson-lune, cerf-lanterne, Veilleur égaré, raie céleste) ; `entity/region` : archétypes des espèces régionales (paisible, volant, aquatique, ennemi annoncé, ennemi tireur, monture) et projectile « éclat » |
| `event` | branchement des événements serveur, première connexion |
| `command` | `/limbes` (administration et diagnostic) |
| `network` | paquets (intentions client → serveur ; affichage serveur → client) |
| `gametest` | tests de jeu |

Client (`src/client/java/fr/leslimbes/client`) : rendu des entités et modèles, particules, ciel et effets de dimension, interface (barre de majesté, suivi de quête, marqueur monde, journal, dialogue), touches, réception des paquets, effets visuels.

Règles : aucune classe cliente n'est chargée sur un serveur dédié (sourcesets séparés par Loom) ; toute décision de jeu est serveur ; les paquets client → serveur sont bornés et validés.

## Données (`src/main/resources`)

- `data/les_limbes/quest/*.json` : quêtes (étapes, objectifs, lieu, actions `debut`/`fin`). Types d'objectifs : `parler`, `atteindre`, `vaincre`, `collecter`, `interagir`, `pouvoir`, `drapeau`. Format documenté dans `QuestDefinition.java`.
- `data/les_limbes/dialogue/<role>.json` : graphes de dialogue (règles de début conditionnelles, nœuds, choix, actions). Format dans `DialogueGraph.java` ; conditions dans `Conditions.java` ; actions dans `Actions.java`.
- `data/les_limbes/worldgen/` : dimension, type de dimension, réglages de bruit, fonctions de densité, biomes, features, structures composées et ensembles de structures.
- `data/les_limbes/structure/**/*.nbt` : gabarits produits par `tools/structgen/build_structures.py`.
- `assets/les_limbes/lang/fr_fr.json` : toutes les chaînes (copiée en `en_us.json` par le générateur : le jeu est français quelle que soit la langue choisie).

Tout fichier généré est regénéré par les scripts ci-dessous ; ne modifiez pas les fichiers générés à la main.

## Outils (`tools/`)

| Script | Rôle |
|---|---|
| `tools/textures/gen_textures.py` | toutes les textures (blocs, objets, skins, créatures, particules, interface, icône), déterministes |
| `tools/models/gen_models.py` | modèles d'entités JSON et textures peintes cube par cube depuis `modeles.py` (raie) et `modeles_regions.py` (25 espèces par gabarits : quadrupède, bipède, insecte, oiseau, poisson, méduse ; UV attribuées automatiquement) |
| `tools/structgen/regions_*.py` | gabarits des villes et lieux des cinq régions (un fichier par région) |
| `tools/sounds/gen_sounds.py` | sons originaux synthétisés (numpy → WAV → OGG via ffmpeg) |
| `tools/datagen/gen_data.py` | blockstates, modèles, tags, butin, recettes, progrès, génération de monde, biomes, ensemble de structures des vestiges, copie de langue |
| `tools/structgen/build_structures.py --rapport` | gabarits NBT des structures, JSON des structures composées à position fixe, rapport d'inspection (statistiques, vues ASCII, rendus PNG dans `tools/structgen/rapport/`) |
| `tools/structgen/canvas.py`, `builders.py`, `nbt.py` | DSL voxel, aides architecturales, écriture/lecture NBT sans dépendance |

Ordre recommandé après modification : textures → sons → données → structures → `./gradlew build`.

## Ajouter du contenu

- **Une quête** : un fichier JSON dans `quest/` ; référencez-la depuis un dialogue (`demarrer_quete`) ou une autre quête (`fin`). Les tests JUnit vérifient la validité et les références.
- **Un personnage** : marqueur `pnj:<role>` dans un gabarit (ou commande `/limbes pnj reapparaitre <role>`), texture `textures/entity/habitant/<role>.png` (ajoutez le rôle dans `SKINS` du générateur et dans `HabitantRenderer`), nom `pnj.les_limbes.<role>` dans la langue, dialogue `dialogue/<role>.json`. Ajoutez le rôle à `ROLES_ESSENTIELS` pour l'invulnérabilité et la réapparition.
- **Un lieu à position fixe** : coordonnées dans `LimbesSites.java`, site aplani dans `gen_data.py` (`SITES`), gabarit et structure composée dans `build_structures.py`. Le test `lesSitesFixesSontCoherentsAvecLaGeneration` garde le tout cohérent.
- **Un pouvoir** : une `Ability` dans `Abilities.java`, une exécution serveur, un effet client dans `EffetsClient.java`, les clés `pouvoir.les_limbes.<id>`, `.court`, `.description`.
- **Un projet de domaine** : un fichier `projet/<id>.json` (titre, description, `indice` lisible pour les prérequis, `ordre`, `prerequis` = conditions de dialogue, `cout`, `gabarit`, `origine`, éventuellement `avant`/`origine_avant` = gabarit d'origine dont les blocs ne comptent pas comme constructions du joueur, `drapeau_monde`, `apres` = actions). Le gabarit vient de `build_structures.py` ; `ProjetManager` fait le reste (aperçu, conflits, ressources, pose, inventaires rendus, drapeau, actions). Les origines doivent rester sur le plateau aplani du manoir (rayon 40), ce que vérifie `lesProjetsLieuxEtRelaisSontCoherents`.
- **Un lieu d'intérêt** : `lieu/<id>.json` (nom, pos, rayon, région, dimension, éventuellement `relais`) ; découverte automatique (drapeau `lieu:<id>`), carte du journal. Un relais : `relais/<id>.json` (nom, pos = point d'arrivée devant la porte) **et** une porte de relais dans un gabarit (`les_limbes:porte_de_relais` avec `{"relais": "<id>"}` dans le bloc-entité).
- **Une créature régionale** : une fiche dans `LimbesEspeces` (archétype, taille, statistiques, sons, appât, combat, couleurs d'œuf), un modèle du même identifiant dans `tools/models/modeles_regions.py` (gabarits `quadrupede`, `bipede`, `insecte`, `oiseau`, `poisson` ou os libres), une texture d'œuf dans `gen_textures.py`, l'identifiant dans `ESPECES_REGIONALES` et une table de butin dans `BUTINS_ENTITES` (`gen_data.py`), des apparitions dans `SPAWNS_REGIONAUX` (le groupe doit correspondre au `SpawnGroup` de l'archétype : paisibles et montures `creature`, volants `ambient`, ennemis `monster`, aquatiques `water_creature`), les clés `entity.les_limbes.<id>` et `item.les_limbes.oeuf_<id>`. Le rendu client est automatique (`RenduDonneesRenderer` sous le même identifiant). La fiche est lue depuis le type d'entité (`LimbesEspeces.especeDe`) parce que le constructeur parent appelle `initGoals()` avant l'affectation des champs.
- **Une région ou un sous-biome** : relief dans `LimbesRegions` (hauteur, pente, règle de sous-biome), biome dans `BIOMES_PAR_REGION` et appel `biome(...)` de `gen_data.py` (règles de surface par biome, végétation triée selon `ORDRE_VEGETAL`), sites aplanis dans `SITES`, gabarits dans `regions_<region>.py` et appels `site(...)` de `build_structures.py`, lieux `lieu/*.json`, relais `relais/*.json` (la porte posée par `relais(c, ...)` dans le gabarit), dialogues et noms des personnages.
- **Une action ou condition de dialogue** : `Actions.java` / `Conditions.java` ; toute action accepte `"si": [conditions]` et `"une_fois": "<drapeau>"`.

## Commandes d'administration (`/limbes`, niveau 2)

`tp sanctuaire|clairval|manoir|relais|overworld`, `quete liste|demarrer|etape|reinit`, `pouvoir <id>|tout`, `majeste <valeur>`, `drapeau poser|retirer`, `pnj reapparaitre <role>`, `porte reveiller` (bloc visé), `structure <id> [pos]` (place un gabarit pour inspection), `arrivee liste|declencher|resoudre`, `projet liste|construire <id> [ignorer]|conflits <id>`, `relais decouvrir <id>|tout`, `lieu tout`, `diag`.

## Génération de monde : leçons apprises (vérifiées sur serveur dédié)

- **Interpolation par cellules** : la densité finale est échantillonnée aux coins de cellules (8 blocs en horizontal, 4 en vertical) puis interpolée. Tout terme qui « plafonne » la densité négative au-dessus du sol (ex. `max(sol, -0,03)`) rapproche le zéro du coin supérieur et relève la surface d'un bloc. Les termes inactifs doivent rester franchement négatifs (les îles valent −0,6 hors de leur bande).
- **Zéro à y − 0,5** : `les_limbes:aplanir` place le zéro de densité à `y − 0,5` : bloc de surface à `y − 1`, air à `y`, sans ambiguïté de signe.
- **Pas de `beard_thin` sur les plateaux** : l'adaptation de terrain vanilla retire de la densité au niveau du bas des pièces (elle creuse le sol autour des villages). Les lieux des Limbes utilisent `terrain_adaptation: none` ; le vestige de l'Overworld garde `beard_thin` pour se caler sur un relief irrégulier.
- **`minecraft:lake` est dépréciée et plante** en bordure de chunk (lecture de biomes à deux chunks pour la glace). `les_limbes:lac` (Java) reste dans un rayon d'un chunk.
- **Une feature listée deux fois** dans une étape de biome provoque un cycle d'ordre au chargement des registres : utilisez une feature placée distincte.
- **Serveur de GameTest** : monde plat sans dimensions de datapack ; la génération réelle se vérifie avec `./gradlew runAutotest` (AutoTest.java), qui journalise « AUTOTEST OK/ECHEC » puis s'arrête.

## Régions et créatures : leçons apprises (étape 5)

- **Source de biomes** : `RegionBiomeSource` choisit la région par distance normalisée (d / rayon) aux centres, avec une bande de transition de 12 % où la frontière est déplacée par le bruit ; la fonction de densité `les_limbes:regions` lit les mêmes formules (`LimbesRegions.hauteur`) pour que relief et biome coïncident. Les Prairies gardent exactement leur relief d'avant (test `lesRegionsPlacentLesSitesEtGardentLesPrairiesInchangees`).
- **Mer stable** : l'eau est posée par une feature Java (`MerFeature`) de y 63 vers le bas dans les colonnes « mer » ; le fond est à 41–46 et le rebord de la région est garanti au-dessus de l'eau (formule `rebord` de `LimbesRegions`), sinon la mer déborde dans la région voisine.
- **Ordre des features** : Minecraft exige un ordre cohérent des features d'une même étape entre tous les biomes (« Feature order cycle found ») : `biome()` trie la végétation selon `ORDRE_VEGETAL`.
- **Musiques vanilla** : seules les clés `music.overworld.<biome>` existantes sont acceptées au chargement des registres (liste dans `gen_data.py`).
- **Apparitions** : le groupe d'apparition du biome doit correspondre au `SpawnGroup` du type ; les animaux vanilla provisoires (cabillaud, tortue, chèvre) sont remplacés par `REMPLACEMENTS_APPARITIONS`.
- **Respiration et chute** : `LivingEntity.canBreatheInWater()` est finale et lit l'étiquette `minecraft:can_breathe_under_water` ; l'immunité à la chute passe par `minecraft:fall_damage_immune` (les deux sont écrites par `gen_data.py`).
- **Tables de butin 1.21** : la fonction de pillage s'appelle `minecraft:enchanted_count_increase` (avec `enchantment: minecraft:looting`), plus `looting_enchant`.
- **GameTests et créatures** : le monde de test n'a aucun joueur à moins de 128 blocs des structures : une créature non persistante est retirée dès son premier tick (`setPersistent()` dans les tests) ; plus de 24 entités au même point déclenchent les dégâts d'entassement de vanilla.

## Rendu client : leçons apprises (vérifiées par captures)

- **Ciel personnalisé (Fabric `DimensionRenderingRegistry`)** : pendant `renderSky`, `WorldRenderContext.matrixStack()` vaut `null` ; utilisez `positionMatrix()` (rotation de la caméra) et dessinez avec `BufferRenderer.drawWithGlobalProgram`. Désactivez l'élimination des faces (`RenderSystem.disableCull()`) pendant le rendu du ciel : un quad construit dans le mauvais sens disparaît sans erreur.
- **Écrans** : tout ce qui est dessiné dans `render` avant `super.render` est flouté par le fond vanilla de 1.21. Dessinez le panneau et son texte dans `renderBackground` après `super.renderBackground`, ou neutralisez le flou (`DialogueScreen`).
- **HUD** : la barre d'objets vanilla commence à `largeur/2 − 91` ; tout élément en bas à gauche doit tenir avant cette limite (427 px logiques au minimum, soit 114 px disponibles).
- **Captures sans écran** : `Xvfb :99 -screen 0 1280x720x24`, `LIBGL_ALWAYS_SOFTWARE=1`, `MESA_GL_VERSION_OVERRIDE=4.5` ; le client tourne à quelques images par seconde mais les ticks restent exacts, ce qui suffit au scénario de `CaptureAutomatique`.

## Mixins

Trois mixins, volontairement minimaux. Côté commun, `DimensionOptionsRegistryHolderMixin` et `DimensionsConfigMixin` déclarent stables les dimensions du mod : sans eux, Minecraft marque « expérimentale » toute dimension non vanilla et affiche un avertissement bloquant (« Je sais ce que je fais ! ») à chaque ouverture d'un monde en solo. Côté client, `CameraMixin` ajoute un décalage d'angle à la caméra pour les secousses (option désactivable) sans jamais toucher à l'orientation du joueur. Les listes des fichiers `les_limbes.mixins.json` (commun) et `les_limbes.client.mixins.json` (client) sont les seuls endroits à modifier pour en ajouter.

## Domaine (étape 4)

- **Projets** (`ProjetManager`) : l'écran de chantier s'ouvre depuis un dialogue (`ouvrir_chantier`) ; le client n'envoie que l'identifiant du projet et le choix « ignorer la zone occupée ». Le serveur recalcule état, coûts et conflits, pose le gabarit avec un `StructureProcessor` qui saute les positions occupées, et rend les inventaires remplacés (vidés avant la pose pour ne jamais dupliquer ce que vanilla disperse déjà). Un projet est partagé par le monde (drapeau `domaine_<id>`) : sur un serveur, le manoir est commun.
- **Lieux** (`LieuxManager`) : vérification par seconde et par joueur ; `lieu:<id>` dans les drapeaux du joueur ; `SyncLieux` envoie définitions et état des projets au client à la connexion et après chaque projet.
- **Relais** (`RelaisManager`) : découverte par la porte ou par le lieu ; voyage autorisé si le réseau est éveillé (`domaine_passages`), le joueur près d'une porte et le relais connu ; destination vérifiée (sol, espace, chunk chargé) ; délai de 3 s entre deux voyages.
- **Vitrines** : bloc-entité synchronisé (`toUpdatePacket`), rendu de l'objet par `VitrineRenderer`, texte d'histoire `vitrine.les_limbes.<objet>` affiché par l'interface quand on la regarde.
- **Options client** (`OptionsLimbes`, `config/les_limbes.json`) : densité des effets, secousses, flashes, marqueurs, indications discrètes, indices de tutoriel vus.

## Multijoueur

Progression, pouvoirs et quêtes sont par joueur (attachement persistant, conservé à la mort). L'état partagé du monde (`LimbesWorldState`) contient les drapeaux globaux et les ancrages des personnages essentiels. Les quêtes sont idempotentes et rejouables (apparition garantie des Veilleurs à l'entrée de l'étape, objets donnés une seule fois par joueur).
