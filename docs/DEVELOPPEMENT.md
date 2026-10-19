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
| `item` | objets à comportement (boussole nacrée, Épée, Lance, Arc et Bouclier du Souverain, fragment de couronne, sifflet) |
| `world/dimension` | clés de la dimension, voyage par les portes, protection des brumes, positions des sites |
| `world/gen` | fonctions de densité `les_limbes:aplanir` et `les_limbes:regions`, features Java (lacs, mer, routes) |
| `world/biome` | source de biomes régionale `les_limbes:regions` (Voronoï pondéré, transitions, sous-biomes) ; `LimbesRegions` porte les formules de relief |
| `world/structure` | placement fixe `les_limbes:fixed`, structure composée `les_limbes:composee`, pièce avec marqueurs |
| `world/state` | données du joueur (attachement persistant), état du monde (PersistentState), synchronisation |
| `npc` | entité Habitant, réapparition des personnages essentiels, dialogues (`npc/dialogue`) |
| `quest` | définitions, chargeur, progression, gestionnaire, signaux |
| `power` | techniques royales : registre `Abilities` (33, sept branches), classes par branche (`ArsenalDuSouverain`, `PortesDuPalais`, `DecretsRoyaux`, `GardeAstrale`, `MainDesLimbes`, `PactesRoyaux`, `Emblematiques`), état transitoire `EtatPouvoirs`, règles d'alliance `Allies`, majesté, recharges |
| `entity` | créatures des Prairies ; `entity/region` : archétypes des espèces régionales (paisible, volant, aquatique, ennemi annoncé, ennemi tireur, monture, soutien, garde astrale) et projectile « éclat » ; `entity/duel` : boss des prétendants (`PretendantEntity`, `StyleDuel`) |
| `duel` | définitions de duels (JSON), gestionnaire (phases, participants, barre de boss, échec et reprise), blocs temporaires |
| `capitale` | éveil de la capitale par niveaux, couronnement et délégations |
| `event` | branchement des événements serveur, première connexion |
| `command` | `/limbes` (administration et diagnostic) |
| `LimbesConfig` | budgets du serveur (`config/les_limbes.json`) : Arrivées simultanées, gardes par joueur et durée, éclats par monde, rayon de surveillance |
| `network` | paquets (intentions client → serveur ; affichage serveur → client) |
| `gametest` | tests de jeu |

Client (`src/client/java/fr/leslimbes/client`) : rendu des entités et modèles, particules, ciel et effets de dimension, interface (barre de majesté, suivi de quête, marqueur monde, journal, dialogue), touches, réception des paquets, effets visuels.

Règles : aucune classe cliente n'est chargée sur un serveur dédié (sourcesets séparés par Loom) ; toute décision de jeu est serveur ; les paquets client → serveur sont bornés et validés.

## Données (`src/main/resources`)

- `data/les_limbes/quest/*.json` : quêtes (étapes, objectifs, lieu, actions `debut`/`fin`). Types d'objectifs : `parler`, `atteindre`, `vaincre`, `collecter`, `interagir`, `pouvoir`, `drapeau`. Format documenté dans `QuestDefinition.java`.
- `data/les_limbes/duel/<id>.json` : duels et épreuves (titre, prétendant, boss, renforts, arène, entrée, apparition, amical, phases à seuils avec invocations et blocs temporaires, actions de victoire et d'échec). Format dans `DuelDefinition.java`.
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
- **Un pouvoir** : une `Ability` dans `Abilities.java` (branche parmi `BRANCHES`), une exécution serveur dans la classe de la branche (ciblage par `ArsenalDuSouverain.viser`, positions sûres par `PortesDuPalais.positionSure`, alliés par `Allies`), un effet client dans `EffetsClient.java`, les clés `pouvoir.les_limbes.<id>`, `.court`, `.description` (le test JUnit lit la source pour les exiger), un déblocage par l'action `{"type": "pouvoir", "id": ...}` dans une quête ou un dialogue. Un état continu (fenêtre, durée) va dans `EtatPouvoirs` et se tique dans `Abilities.tick`.
- **Un projet de domaine** : un fichier `projet/<id>.json` (titre, description, `indice` lisible pour les prérequis, `ordre`, `prerequis` = conditions de dialogue, `cout`, `gabarit`, `origine`, éventuellement `avant`/`origine_avant` = gabarit d'origine dont les blocs ne comptent pas comme constructions du joueur, `drapeau_monde`, `apres` = actions). Le gabarit vient de `build_structures.py` ; `ProjetManager` fait le reste (aperçu, conflits, ressources, pose, inventaires rendus, drapeau, actions). Les origines doivent rester sur le plateau aplani du manoir (rayon 40), ce que vérifie `lesProjetsLieuxEtRelaisSontCoherents`.
- **Un lieu d'intérêt** : `lieu/<id>.json` (nom, pos, rayon, région, dimension, éventuellement `relais`) ; découverte automatique (drapeau `lieu:<id>`), carte du journal. Un relais : `relais/<id>.json` (nom, pos = point d'arrivée devant la porte) **et** une porte de relais dans un gabarit (`les_limbes:porte_de_relais` avec `{"relais": "<id>"}` dans le bloc-entité).
- **Une créature régionale** : une fiche dans `LimbesEspeces` (archétype, taille, statistiques, sons, appât, combat, couleurs d'œuf), un modèle du même identifiant dans `tools/models/modeles_regions.py` (gabarits `quadrupede`, `bipede`, `insecte`, `oiseau`, `poisson` ou os libres), une texture d'œuf dans `gen_textures.py`, l'identifiant dans `ESPECES_REGIONALES` et une table de butin dans `BUTINS_ENTITES` (`gen_data.py`), des apparitions dans `SPAWNS_REGIONAUX` (le groupe doit correspondre au `SpawnGroup` de l'archétype : paisibles et montures `creature`, volants `ambient`, ennemis `monster`, aquatiques `water_creature`), les clés `entity.les_limbes.<id>` et `item.les_limbes.oeuf_<id>`. Le rendu client est automatique (`RenduDonneesRenderer` sous le même identifiant). La fiche est lue depuis le type d'entité (`LimbesEspeces.especeDe`) parce que le constructeur parent appelle `initGoals()` avant l'affectation des champs.
- **Une région ou un sous-biome** : relief dans `LimbesRegions` (hauteur, pente, règle de sous-biome), biome dans `BIOMES_PAR_REGION` et appel `biome(...)` de `gen_data.py` (règles de surface par biome, végétation triée selon `ORDRE_VEGETAL`), sites aplanis dans `SITES`, gabarits dans `regions_<region>.py` et appels `site(...)` de `build_structures.py`, lieux `lieu/*.json`, relais `relais/*.json` (la porte posée par `relais(c, ...)` dans le gabarit), dialogues et noms des personnages.
- **Un duel** : un fichier `duel/<id>.json` ; le boss est un type de `LimbesPretendants` (fiche + `StyleDuel`, modèle du même identifiant dans `modeles_regions.py`) ou une espèce ordinaire (épreuves de tournoi) ; l'arène est une structure à position fixe (voir `arenes.py`), l'entrée et l'apparition sont dans l'arène ; un dialogue lance le duel par `{"type": "duel", "id": "<id>"}` ; la victoire pose `duel_<id>_gagne` et exécute les actions `victoire` (drapeaux de reconnaissance...). Les motifs de blocs temporaires des phases sont `racines`, `forteresse`, `miroirs`, `sables` ; ils ne remplacent que de l'air et sont toujours retirés.
- **Un quartier de la capitale** : une toile dans `capitale.py` avec lanternes `lanterne_royale_eteinte`, portes `porte_royale[niveau=n]` et ponts `pont_royal`, un `site()` sur le plateau (−900/−900, rayon 90), un lieu `lieu/*.json`, des personnages (apparence, nom, dialogue).
- **Une quête secondaire** : une entrée `quete(...)` dans `tools/datagen/gen_quetes_secondaires.py` (donneur, lieu, étapes, textes d'offre, de rappel et de remise, récompenses), puis exécution du script (il réécrit les 27 quêtes et re-branche les dialogues : à n'exécuter que sur des dialogues sans nœuds `_cours`/`_fin`, par exemple depuis l'état du dépôt avant l'étape 8, ou en ajoutant la quête à la main sur le même modèle).
- **Une Arrivée** : une toile dans `tools/structgen/arrivees.py`, une définition `arrivee/<id>.json` (échelle, gabarit, ancre, groupe de sites, poids, unicité, durée, prérequis, annonce), un coffre dans `gen_data.py`, éventuellement un site `arrivee_site/<id>.json` et son aplanissement dans `SITES`.
- **Une action ou condition de dialogue** : `Actions.java` / `Conditions.java` ; toute action accepte `"si": [conditions]` et `"une_fois": "<drapeau>"`. Actions notables : `pouvoir`, `duel`, `capitale`, `couronner`, `delegation`, `arrivee_declencher`, `poser_structure`, `apparition`, `deplacer_pnj`, `modifier_bloc`.
- **Une structure de l'acte V** : une toile dans `tools/structgen/regne.py`, un `site()` dans `build_structures.py`, un site aplani dans `SITES` et une constante dans `LimbesSites` ; les personnages et créatures par marqueurs `pnj:` / `entite:`.

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

## Duels et capitale : leçons apprises (étape 6)

- **Dégâts d'un boss** : plafonnés par coup (14) sauf pour les sources qui ignorent l'invulnérabilité (commande `kill`, vide) ; sinon un seul coup de l'Épée affûtée raccourcirait l'épreuve.
- **Phases** : évaluées par le gestionnaire à chaque tick sur la fraction de vie, chaque seuil une seule fois ; le boss reçoit `entrerPhase` et interprète selon son style. Les renforts et soutiens sont rattachés au duel et retirés à la fin, quelle qu'elle soit.
- **Épreuve amicale** : un participant à 3 PV ou moins est soigné, renvoyé à l'entrée, et le duel se termine en échec (le boss disparaît) ; la mort d'un joueur termine aussi le duel ; quitter l'arène dix secondes vaut abandon. Tout duel se relance par le dialogue.
- **Boss ordinaires** : un boss qui n'est pas un prétendant (tournoi) compte sa victoire quand `isDead()` passe à vrai (l'entité reste vingt ticks après sa mort).
- **Blocs de la capitale** : les gabarits ne déclenchent pas `onBlockAdded`, donc les portes, lanternes et ponts lisent le niveau d'éveil à leurs ticks aléatoires (`ticksRandomly`) ; `CapitaleManager.eveiller` applique en plus le niveau tout de suite dans les chunks chargés.
- **Délégations** : des exemplaires temporaires d'`HabitantEntity` (`setDelegation`), ignorés par la réapparition des essentiels, qui repartent d'eux-mêmes.
- **Apparences** : le rendu choisit la texture `textures/entity/habitant/<role>.png` si elle existe, sinon l'apparence commune (plus de liste figée côté client).

## Pouvoirs supérieurs et règne : leçons apprises (étape 7)

- **Dégâts bonus** (marque, changement d'arme, Avènement) : dans l'événement « après dégâts », rappeler `damage(source, base + bonus)` applique exactement l'excédent pendant la fenêtre d'invulnérabilité (règle vanilla) ; une garde statique évite la récursion de l'événement.
- **Parade** : l'événement « dégâts autorisés » annule le coup ; les projectiles en vol sont retirés ; un joueur fraîchement connecté est invulnérable soixante ticks (`joinInvulnerabilityTicks`), ce qui décale les tests.
- **Déplacements du joueur** : un joueur sans client (factice, test) ne simule pas sa vitesse ; la charge de lance blesse ce qui est déjà au contact, les autres effets de déplacement s'observent avec un vrai client.
- **Pont de lumière** : bloc à tick planifié (survit aux sauvegardes) plutôt qu'une liste en mémoire ; `onBlockAdded` planifie aussi les poses par commande ou gabarit.
- **Garde astrale** : propriétaire en donnée suivie (`OPTIONAL_UUID`), ordres suivre / tenir / attaquer, cible = hostiles, prétendants, soutiens et toute créature qui vise un allié ; téléportée près du souverain au-delà de quarante blocs ; dissipée sans mort ni butin.
- **Avènement** : l'état public (couronne niveau 3) est recalculé par `LimbesAttachments.publier` depuis `EtatPouvoirs`, pas depuis un drapeau ; la fin de l'effet republie le niveau 2.
- **Structure posée par dialogue** : `poser_structure` charge le chunk, place le gabarit avec ses marqueurs et pose un drapeau de monde ; le gabarit du navire contient de l'air explicite aux anciens emplacements des échafaudages (les cases non définies d'une toile ne sont pas écrites).
- **Arrivée appelée** : une Arrivée déclenchée seulement par dialogue garde un poids strictement positif et un prérequis de drapeau de monde posé juste avant l'appel (le tirage au sort pondéré ne tolère pas un total nul).

## Complétion : leçons apprises (étape 8)

- **Quêtes secondaires générées** : une spécification Python (donneur, lieu, étapes, textes, récompenses) produit les JSON de quêtes et branche les dialogues (choix d'offre inséré en tête des nœuds choisis, nœuds `<quete>_cours` et `<quete>_fin`, entrées de début placées juste avant le premier nœud d'offre pour ne pas devancer les nœuds d'histoire). Les textes restent lisibles et relisibles au même endroit.
- **Arrivées régionales** : un site aplani par région (rayon 16) hors des zones de transition des villes ; la vérification de zone exige du terrain naturel, donc pas de site sous une structure. Une Arrivée uniquement déclenchée par dialogue garde un poids positif et un prérequis de drapeau.
- **Musique** : `sounds.json` accepte des entrées objet `{"name", "stream": true}` ; le test des sons lit les deux formes. La musique d'un biome est la clé `effects.music.sound` ; `biome()` l'affecte par région.
- **Budgets** : `LimbesConfig` est lu au démarrage (fichier créé avec les valeurs par défaut, bornées) ; les compteurs qui parcourent les entités d'un monde (éclats) ne s'exécutent qu'au tir, jamais par tick.
- **Captures** : le scénario client envoie les paquets d'utilisation par identifiant pour montrer la garde, le pont de lumière et l'Avènement (vue à la troisième personne pour la couronne et le manteau).
- **Musique et registre** : le champ `effects.music.sound` d'un biome est un `RegistryEntry<SoundEvent>` ; un son présent seulement dans `sounds.json` fait échouer le chargement de *tous* les registres (serveur de test, serveur dédié, client) avec « Failed to get element … sound_event ». Tout son du mod passe donc par `LimbesSounds` ; le test de données exige l'égalité des deux listes.
- **Terrain naturel** : la vérification de zone d'une Arrivée refuse tout bloc hors de l'étiquette `terrain_naturel` ; la génération ne doit donc jamais poser un bloc de construction (la règle de surface des plafonds et le bord des lacs posaient de la pierre nacrée polie, le sable de nacre des îlots manquait). La liste `TERRAIN_NATUREL` de `gen_data.py` est la seule source ; le test de données la confronte aux règles de surface et aux features.
- **Étiquettes vanilla étendues** : un seul appel `tag()` par fichier dans `gen_data.py` (une seconde écriture remplace la première : les feuilles de géant avaient disparu de `minecraft:leaves`) ; le test de données vérifie les blocs des géants dans `leaves`, `logs`, `logs_that_burn` et `dirt`. Les lanternes perdues posées par la génération sont des « trouvailles » : une Arrivée peut se poser dessus et les fait tomber en objets.
- **Captures après téléportation** : sous rendu logiciel, une capture attend que `WorldRenderer.isTerrainRenderComplete()` soit vrai et que le chunk du joueur soit chargé (au plus trente secondes) ; `limbes arrivee declencher <id> [site]` vise un site précis pour cadrer la caméra.

## Rendu client : leçons apprises (vérifiées par captures)

- **Textes d'un écran et flou vanilla** : comme pour le journal, tout texte dessiné avant `super.renderBackground` (donc avant `super.render`) est flouté avec le monde ; la sélection rapide dessine son titre, son aide et le nom de la branche dans `renderBackground` après l'appel parent, et le détail de la technique survolée après `super.render`.
- **Un seul tampon de secours à la fois** : `VertexConsumerProvider.Immediate` ne pré-alloue que les couches vanilla ; une couche à texture propre (`RenderLayer.getEntityCutoutNoCull(texture_du_mod)`) utilise le tampon de secours partagé, et demander une seconde couche de ce type **termine et dessine** la première. Un rendu qui garde deux de ces `VertexConsumer` et les alterne plante le client (« Not building! », ailes du moulin). Écrire tous les sommets d'une couche avant de demander la suivante.
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
