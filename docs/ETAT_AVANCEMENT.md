# État d'avancement — version 0.4.0 « Le Passage », qualité, domaine et six régions

Dernière mise à jour : 19 octobre 2026. Ce document distingue honnêtement ce qui est **fonctionnel et vérifié**, ce qui est **fonctionnel mais non vérifié en jeu** (compilé, testé hors affichage), ce qui est **partiel** et ce qui **reste à réaliser**.

## Résultat jouable de cette tranche

Dans une nouvelle sauvegarde de survie : lire le carnet, fabriquer la boussole nacrée, trouver un vestige, réunir trois éclats, réveiller la porte, arriver au Sanctuaire, rencontrer Maëlle, descendre le grand pont vers Clairval, parler à Thibault, chasser trois Veilleurs égarés du relais royal, sauver Gaspard, recevoir le fragment de couronne (Épée du Souverain + Pas court), obtenir la clé du manoir auprès d'Orielle, rencontrer Vaëron (décret « Reculez »). Retour possible à tout moment par l'arche du Sanctuaire.

Depuis la tranche de qualité (étape 3) : apprivoiser une raie céleste avec trois fleurs de rosée et la monter (vol au regard, endurance, sifflet de rappel, recréation si elle est perdue, chute lente au démontage) ; voir le ciel se plisser et une Arrivée se poser sur un site réservé des Prairies (« La maison égarée » avec Émile le boulanger, « La caisse tombée », « La barque échouée »), que Lisbeth situe depuis l'observatoire ; remettre en marche le moulin de Clairval (Rosalie) et moudre son blé ; traverser le Lac du Crépuscule sur des dalles qui n'émergent qu'au crépuscule et à l'aube, jusqu'à l'îlot et sa meule royale. Le fragment de couronne est visible par les autres joueurs.

Depuis l'étape du domaine (étape 4) : Orielle ouvre l'écran des **projets du domaine** (dix projets : aile ouest, forge royale, écuries, serres, maison des explorateurs, galerie des merveilles, salle des passages, chambres des compagnons, jardin, marché et ateliers) ; chaque projet montre son emprise dans le monde, signale les constructions du joueur qui la gênent et propose de bâtir autour sans y toucher, consomme des ressources des Prairies et transforme le manoir immédiatement, les inventaires étant préservés. Six compagnons se recrutent par de vraies rencontres (Sabine, Loïc, Hanna, Théo, Marguerite, Ysolde) et s'installent dans l'annexe bâtie ; Sabine affûte l'Épée du Souverain. Les portes de relais touchées en voyage (Sanctuaire, Clairval, relais royal) rejoignent la salle des passages, qui mène ensuite à chaque relais connu. Les vitrines de la galerie exposent un objet avec son histoire. Le journal a gagné les onglets Domaine, Carte et Options (densité des effets, secousses, flashes, marqueurs, indications discrètes, indices de tutoriel).

Depuis l'étape des régions (étape 5) : les Limbes ont désormais **six régions** (source de biomes régionale, relief propre à chacune, 18 biomes) reliées par des **routes pavées** qui suivent le relief. La **Mer suspendue** (eau stable à y 63, îles, récifs lumineux, Port-Nacre et son prince des routes Kaël, marché flottant, navire perdu, phare du Dessous), les **Jardins des Géants** (Sylvécime perchée dans les géants avec la sève ascendante, Aélis, serre ancienne, clairière où dort un géant), le **Désert des Heures** (Méridiane et ses archives, Sahir, cadrans et pavés horaires, jardin des heures), les **Montagnes de Verre** (Verréole sur son plateau, Orsane, forge, bains, ponts de verre qui se forment à l'approche, pont qui chante) et la **Couronne Immobile** (Haute-Garde, la cité fortifiée de Vaëron tenue par Aubin ; quartier des portes gardé par Elouan). Chaque ville a sa porte de relais ; 63 nouveaux personnages, cinq quêtes régionales (une par prétendant ou cité, avec un choix à conséquence pour l'eau de Sylvécime), 25 créatures régionales dont cinq montures (tortue qui nage, scarabée, gazelle, griffon qui vole, cerf royal) avec pacte, saut, sifflet des montures et recréation ; des ennemis qui annoncent tous leurs coups et des tireurs dont on évite les éclats ; matériaux, cuisine et recettes par région ; pêche de poissons-lanternes.

## Vérifications effectuées dans cet environnement (sans affichage)

| Vérification | Résultat |
|---|---|
| Compilation complète (`./gradlew build`, JAR produit) | OK |
| Tests unitaires (`./gradlew test`) : validité des quêtes/dialogues/Arrivées/projets/lieux/relais, références, langues, textures, modèles d'entités (26, UV dans la texture), cohérence des sites et des gabarits, régions (sites dans leur région, Prairies inchangées), logique de progression, aplanissement, sérialisation | 16/16 OK |
| GameTests headless (`./gradlew runGametest`) : registres de génération, réveil de la porte, quête « Le Passage », gabarits, pièces composées et marqueurs PNJ, raie (pacte, monte, rappel), dalles de rosée, meule du moulin, Arrivée placée avec son occupant, projet (conflits, blocs épargnés, inventaires rendus), lieux/relais/portes, vitrine, 25 espèces régionales créées et vivantes + tireur qui annonce puis lance un éclat, monture terrestre (pacte, monte, sifflet, recréation), ennemi annoncé (préparation sans dégât, coup porté, venin, récupération) | 14/14 OK |
| Serveur dédié réel (`./gradlew runAutotest`) : dimension, lieux à position fixe, terrain aplani, personnages essentiels, Veilleurs, vestige Overworld, observatoire, lac, moulin, Arrivées, raie, portes de relais générées avec leur identifiant, découverte de Clairval, projet « aile ouest » validé sur le manoir réel, voyage Clairval → Sanctuaire, puis régions : biome/eau/profondeur de la Mer, plateau de Verréole, sols du Désert et des Jardins, plateau bâti de Haute-Garde, route pavée, huit portes de relais, neuf personnages des villes et lieux régionaux, 25 espèces créées sur le serveur, apparitions déclarées par les biomes, géant dans sa clairière | 91 vérifications OK (511 chunks générés) |
| Client réel sans écran (`./gradlew runCapture`, Xvfb + Mesa logiciel) : 17 captures du scénario automatique (étapes 1 à 3) | OK, images dans `docs/captures/` ; les écrans de l'étape 4 (chantier, relais, carte, options, vitrine) et les régions, villes, créatures et montures de l'étape 5 seront capturés lors de la campagne de captures de fin de projet (coût d'exécution élevé, demandé en fin de réalisation) |

## Non vérifié (limites de l'environnement : pas de joueur réel, pas d'audio)

- Animations en mouvement (cerf, poisson-lune, raie en vol, ailes du moulin, préparation du Veilleur), particules dans la durée, sons : seules des images fixes ont été observées, sans périphérique audio.
- Sensation du combat contre les Veilleurs, du Pas court, de l'enchaînement de l'épée et du pilotage de la raie (les mécaniques sont testées côté serveur, pas « en main » ; le vol au regard dépend des entrées du client).
- Enchaînement complet des quêtes avec un vrai joueur au clavier (l'enchaînement est testé avec un joueur factice en GameTest).
- Rendu des 26 modèles d'entités régionaux (proportions, animations de marche, d'annonce et de vol), des villes et des biomes des cinq nouvelles régions : vérifiés par les tests serveur (génération, personnages, blocs) mais pas encore observés à l'écran.
- Pilotage des montures terrestres (saut à la barre d'espace, nage de la tortue) et du griffon : mécaniques testées côté serveur (pacte, monte, rappel), pas « en main ».
- Deux joueurs sur serveur dédié.

Ces scénarios sont documentés comme **à jouer** dans `BACKLOG.md` (section A).

## Fonctionnel / partiel / restant

- **Fonctionnel** : chaîne de compilation, dimension à six régions et 18 biomes, routes, Mer suspendue, entrée et retour, structures à position fixe et pièces composées (49 gabarits, 6 villes, 20 lieux régionaux), PNJ essentiels (27) et réapparition, 83 dialogues, quêtes de l'acte I, treize quêtes secondaires (dont cinq aventures régionales), journal et marqueurs, trois pouvoirs et majesté, 30 créatures dont six montures, modèles d'entités en données, système d'Arrivées (trois types, quatre sites), blocs scénarisés (dalle de rosée, moulin, meules, vitrine, porte de relais, sève ascendante, cadran et pavé horaires, pont de verre, cristal résonnant), domaine (dix projets, recrutement, salle des passages, galerie, affûtage), neuf relais, trente lieux, commandes d'administration, générateurs d'assets, tests.
- **Partiel** : cerf-lanterne (sentiers vers des secrets à venir) ; musique (pistes vanilla) ; compagnons installés mais sans scènes de vie ni échanges marchands ; domaine partagé par le monde en multijoueur (pas de domaine par groupe) ; capitale : plateau réservé et quartier des portes seulement (palais à l'étape 6) ; mécanismes temporels de Méridiane limités aux cadrans et pavés horaires.
- **Restant** : voir `BACKLOG.md` — campagne des prétendants (actes II–IV), duels, couronnement et premier acte royal, pouvoirs supérieurs, règne, complétion (quêtes secondaires, Arrivées, décor, audio), optimisation.

## Journal des exécutions

### 18 octobre 2026 — session de création (environnement sans affichage)

| Exécution | Résultat |
|---|---|
| `./gradlew build` | OK, JAR `build/libs/les-limbes-0.1.0.jar` |
| `./gradlew test` (JUnit) | 11/11 OK |
| `./gradlew runGametest` (serveur de test plat) | 4/4 OK : réveil du voile (9 blocs, idempotent), enchaînement complet de « Le Passage » et objectifs manuels avec un joueur factice, chargement des 12 gabarits, génération des pièces composées avec apparition de Brann depuis un marqueur |
| `./gradlew runAutotest` (serveur dédié réel, monde vierge) | dimension présente ; 148 chunks des Limbes générés en 3,3–5,1 s ; arche de retour active en 0/84/−8 ; point d'arrivée libre ; place de Clairval pavée ; plateaux à 65/66 sur six points de contrôle ; Maëlle, Thibault, Brann, Vaëron, Orielle, Gaspard présents à leur poste ; trois Veilleurs au relais ; vestige localisable depuis l'apparition de l'Overworld (deux graines : 144/80 et −624/−624) |

Bugs trouvés et corrigés par ces exécutions : clé `predicate_type` du filtre de minerai ; feature dupliquée (cycle d'ordre) ; `minecraft:lake` qui plantait en bordure de chunk (remplacée par `les_limbes:lac`) ; `beard_thin` qui creusait le sol des villages ; terme des îles qui relevait la surface d'un bloc par interpolation ; constantes de l'arche et du point d'arrivée ; téléportation dans la fontaine.

### 18 octobre 2026 — captures client sous Xvfb (rendu logiciel Mesa)

| Exécution | Résultat |
|---|---|
| `./gradlew runCapture` (client 1.21.1, sauvegarde issue de l'auto-test, 5 passes successives) | 12 captures OK ; traversée réelle de l'arche vers l'Overworld (téléportation serveur), dialogue ouvert par interaction, journal lisible, HUD dans la zone libre à gauche de la barre d'objets |
| `./gradlew runAutotest` (après les corrections, monde vierge) | 22 succès, 0 échec : dimension, 148 chunks en 3,5 s, arche active, arrivée libre, place pavée, dalle libre, six plateaux à 65/66 (végétation tolérée), sols du manoir et du relais, six personnages essentiels, trois Veilleurs, vestige localisable |

Bugs trouvés et corrigés grâce aux captures :
- avertissement vanilla « réglages expérimentaux » à chaque ouverture d'un monde contenant la dimension (deux mixins ciblés déclarent les dimensions du mod stables) ;
- ciel nacré jamais dessiné : le contexte de rendu du ciel ne fournit pas de `MatrixStack` (utilisation de `positionMatrix()`) ; étoiles orientées dos à la caméra (élimination des faces désactivée pendant le rendu du ciel) ; palais relevé au-dessus de la brume et agrandi, halo en dégradé ;
- texte du journal flouté (dessiné avant le flou vanilla ; désormais dans `renderBackground`) ; résumé d'étape non replié ; entrées de l'onglet Pouvoirs qui se chevauchaient ;
- barre de pouvoirs qui recouvrait les cœurs et compteur de majesté sous la barre d'objets (colonne recomposée) ;
- feuilles d'aube qui ressemblaient à de la terre (texture rose nacrée contrastée) ;
- générateurs de textures non déterministes (`hash()` Python) : remplacé par crc32, sorties identiques d'une exécution à l'autre.

### 18 octobre 2026 — tranche de qualité (monture, Arrivées, mini-aventures)

| Exécution | Résultat |
|---|---|
| `./gradlew build` | OK, JAR `build/libs/les-limbes-0.2.0.jar` |
| `./gradlew test` (JUnit) | 14/14 OK (dont : modèles d'entités, Arrivées et sites, sites fixes ⇔ génération) |
| `./gradlew runGametest` | 8/8 OK ; la monte de la raie est testée avec un joueur serveur réellement connecté (le `FakePlayer` de Fabric refuse toute monte par construction) |
| `./gradlew runAutotest` (serveur dédié réel, monde vierge) | 40 succès, 0 échec : 148 chunks, lieux fixes, observatoire et Lisbeth, lac (dalles présentes, îlot, meule royale, cabane de Loïc), moulin immobile généré, sites d'Arrivée aplanis, déclenchement d'une Arrivée (`maison_egaree`) et de son occupant, refus d'un doublon, résolution et archivage, pacte de la raie puis recréation au sifflet |
| `./gradlew runCapture` | 17 captures (5 nouvelles : raie, moulin, observatoire, lac au crépuscule, maison égarée) |

Bugs trouvés et corrigés par ces exécutions :
- pièce composée décalée de (4, 0, 4) : l'origine d'une structure composée est l'angle du chunk, le décalage se calcule depuis ce point ;
- dalles de rosée emportées par l'eau du lac (bloc sans collision remplacé par le fluide) : la dalle cachée contient l'eau (`FluidFillable`) ;
- `FakePlayer` absent de la liste des joueurs : `isOwner` toujours faux → comparaison des UUID ; puis monte impossible → joueur serveur factice connecté ;
- gabarit de test d'Arrivée qui touchait la plateforme (`BARRIER` compté comme sol naturel, origine rehaussée) ;
- toiles de plusieurs gabarits trop petites pour leurs volumes (maison égarée, lampadaire du lac, maison des explorateurs, cabane) ;
- encodage des sons non déterministe (numéro de série Ogg aléatoire) : `ffmpeg -fflags +bitexact`.

### 18 octobre 2026 — domaine (projets, recrutement, salle des passages, journal)

| Exécution | Résultat |
|---|---|
| `./gradlew build` | OK, JAR `build/libs/les-limbes-0.3.0.jar` |
| `./gradlew test` (JUnit) | 15/15 OK (nouveau : projets ⇔ gabarits et plateau du manoir, lieux, relais ⇔ portes dans les gabarits) |
| `./gradlew runGametest` | 11/11 OK (nouveaux : projet et inventaires, lieux/relais/portes avec voyage réel d'un joueur serveur, vitrine) |
| `./gradlew runAutotest` (serveur dédié réel, monde vierge) | 58 succès, 0 échec : les 40 vérifications précédentes, trois portes de relais générées avec leur identifiant et un point d'arrivée sûr, Clairval et son relais découverts en entrant sur la place, projet « Réparer l'aile ouest » refusé sans ressources puis avec un coffre en conflit non ignoré, validé en construisant autour (plancher posé, coffre et contenu intacts, ressources consommées, drapeau du monde, état « construit »), voyage Clairval → Sanctuaire par le réseau |

Bugs trouvés et corrigés par ces exécutions :
- inventaires dupliqués à la pose d'un gabarit : vanilla disperse déjà le contenu d'un conteneur remplacé (`ItemScatterer.onStateReplaced`) mais pas quand le gabarit porte des données de bloc-entité à la même position (conteneur vidé avant remplacement) ; les conteneurs de l'emprise sont désormais vidés avant la pose puis leur contenu rendu, une seule fois ;
- feuilles d'un arbre au-dessus d'un point de contrôle du plateau : la végétation (feuilles, troncs) est tolérée par l'auto-test au-dessus du sol aplani.

### 19 octobre 2026 — six régions (étape 5)

| Exécution | Résultat |
|---|---|
| `./gradlew build` | OK, JAR `build/libs/les-limbes-0.4.0.jar` |
| `./gradlew test` (JUnit) | 16/16 OK (nouveaux : régions ⇔ sites, Prairies inchangées ; relais ⇔ portes dans tous les fichiers de structures ; modèles régionaux) |
| `./gradlew runGametest` | 14/14 OK (nouveaux : 25 espèces vivantes et tireur, monture terrestre, ennemi annoncé) |
| `./gradlew runAutotest` (serveur dédié réel, monde vierge) | 91 succès, 0 échec : 511 chunks des Limbes générés en 26 s ; les 58 vérifications précédentes ; Mer suspendue (biome, eau à 63 et air au-dessus, 30 blocs de profondeur), plateau de Verréole et biome des Montagnes, sol du Désert et des Jardins près des villes, plateau bâti de Haute-Garde, route pavée au sud de Clairval ; huit portes de relais avec identifiant et point d'arrivée sûr ; Kaël, Aélis, Sahir, Orsane, Aubin, Oriane, Ismaël, Nérine, Elouan à leur poste ; 25 espèces créées ; poissons-lanternes déclarés dans la Mer et ennemis propres au Désert ; géant endormi dans sa clairière |

Bugs trouvés et corrigés par ces exécutions :
- bruit de la Mer : rebord sous le niveau de l'eau à la frontière de la région (formule du rebord réécrite) ; musique `music.overworld.jungle_and_forest` inexistante en 1.21.1 ; cycle d'ordre des features végétales entre biomes (tri canonique) ;
- fiche d'espèce nulle à la construction : le constructeur parent appelle `initGoals()` et `createNavigation()` avant l'affectation des champs de la sous-classe (fiche résolue depuis le type d'entité) ;
- fonction de butin `looting_enchant` renommée `enchanted_count_increase` en 1.21 ; respiration sous l'eau par étiquette (méthode finale) ;
- GameTests : créatures retirées dès le premier tick faute de joueur à moins de 128 blocs (persistance), dégâts d'entassement au-delà de 24 entités au même point, entités déchargées hors des chunks forcés du test, mouton sans IA qui flotte au point d'apparition ;
- auto-test : points de contrôle et villes hors des chunks forcés, Aélis au sommet de Sylvécime hors de la boîte de recherche.
