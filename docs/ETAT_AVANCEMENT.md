# État d'avancement — version 0.2.0 « Le Passage » + tranche de qualité

Dernière mise à jour : 18 octobre 2026. Ce document distingue honnêtement ce qui est **fonctionnel et vérifié**, ce qui est **fonctionnel mais non vérifié en jeu** (compilé, testé hors affichage), ce qui est **partiel** et ce qui **reste à réaliser**.

## Résultat jouable de cette tranche

Dans une nouvelle sauvegarde de survie : lire le carnet, fabriquer la boussole nacrée, trouver un vestige, réunir trois éclats, réveiller la porte, arriver au Sanctuaire, rencontrer Maëlle, descendre le grand pont vers Clairval, parler à Thibault, chasser trois Veilleurs égarés du relais royal, sauver Gaspard, recevoir le fragment de couronne (Épée du Souverain + Pas court), obtenir la clé du manoir auprès d'Orielle, rencontrer Vaëron (décret « Reculez »). Retour possible à tout moment par l'arche du Sanctuaire.

Depuis la tranche de qualité (étape 3) : apprivoiser une raie céleste avec trois fleurs de rosée et la monter (vol au regard, endurance, sifflet de rappel, recréation si elle est perdue, chute lente au démontage) ; voir le ciel se plisser et une Arrivée se poser sur un site réservé des Prairies (« La maison égarée » avec Émile le boulanger, « La caisse tombée », « La barque échouée »), que Lisbeth situe depuis l'observatoire ; remettre en marche le moulin de Clairval (Rosalie) et moudre son blé ; traverser le Lac du Crépuscule sur des dalles qui n'émergent qu'au crépuscule et à l'aube, jusqu'à l'îlot et sa meule royale. Le fragment de couronne est visible par les autres joueurs.

## Vérifications effectuées dans cet environnement (sans affichage)

| Vérification | Résultat |
|---|---|
| Compilation complète (`./gradlew build`, JAR produit) | OK |
| Tests unitaires (`./gradlew test`) : validité des quêtes/dialogues/Arrivées, références, langues, textures, modèles d'entités, cohérence des sites, logique de progression, aplanissement, sérialisation | 14/14 OK |
| GameTests headless (`./gradlew runGametest`) : registres de génération, réveil de la porte, quête « Le Passage », gabarits, pièces composées et marqueurs PNJ, pacte/monte/rappel de la raie (joueur serveur factice connecté), dalles de rosée selon l'heure, meule du moulin, placement d'une Arrivée avec son occupant | 8/8 OK |
| Serveur dédié réel (`./gradlew runAutotest`) : dimension, génération des lieux à position fixe, terrain aplani, personnages essentiels, Veilleurs, vestige Overworld, observatoire, lac et dalles, moulin, sites d'Arrivée, déclenchement/unicité/résolution d'une Arrivée, pacte et recréation de la raie | 40 vérifications OK (voir journal) |
| Client réel sans écran (`./gradlew runCapture`, Xvfb + Mesa logiciel) : 17 captures du scénario automatique — arche, pont, traversée de l'arche vers l'Overworld, dialogue de Maëlle, ciel et palais, journal (quêtes, pouvoirs), Clairval, manoir, relais avec Veilleur, nuit, raie céleste, moulin en marche, observatoire, lac au crépuscule, maison égarée | OK, images dans `docs/captures/` |

## Non vérifié (limites de l'environnement : pas de joueur réel, pas d'audio)

- Animations en mouvement (cerf, poisson-lune, raie en vol, ailes du moulin, préparation du Veilleur), particules dans la durée, sons : seules des images fixes ont été observées, sans périphérique audio.
- Sensation du combat contre les Veilleurs, du Pas court, de l'enchaînement de l'épée et du pilotage de la raie (les mécaniques sont testées côté serveur, pas « en main » ; le vol au regard dépend des entrées du client).
- Enchaînement complet des quêtes avec un vrai joueur au clavier (l'enchaînement est testé avec un joueur factice en GameTest).
- Deux joueurs sur serveur dédié.

Ces scénarios sont documentés comme **à jouer** dans `BACKLOG.md` (section A).

## Fonctionnel / partiel / restant

- **Fonctionnel** : chaîne de compilation, dimension et biomes, entrée et retour, structures à position fixe et pièces composées, PNJ essentiels et réapparition, dialogues et quêtes de l'acte I, huit quêtes secondaires des Prairies, journal et marqueurs, trois pouvoirs et majesté, cinq créatures dont la monture, modèles d'entités en données, système d'Arrivées (trois types, quatre sites), blocs scénarisés (dalle de rosée, moulin, meules), commandes d'administration, générateurs d'assets, tests.
- **Partiel** : manoir (lieu complet, projets de restauration en données, exécution à l'étape 4) ; cerf-lanterne (sentiers vers des secrets à venir) ; musique (pistes vanilla) ; chemins hors plateaux.
- **Restant** : voir `BACKLOG.md` — domaine et recrutement (en cours), cinq régions, prétendants, campagne et couronnement, pouvoirs supérieurs, règne, optimisation.

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
