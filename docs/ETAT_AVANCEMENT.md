# État d'avancement — version 0.3.0 « Le Passage », tranche de qualité et domaine

Dernière mise à jour : 18 octobre 2026. Ce document distingue honnêtement ce qui est **fonctionnel et vérifié**, ce qui est **fonctionnel mais non vérifié en jeu** (compilé, testé hors affichage), ce qui est **partiel** et ce qui **reste à réaliser**.

## Résultat jouable de cette tranche

Dans une nouvelle sauvegarde de survie : lire le carnet, fabriquer la boussole nacrée, trouver un vestige, réunir trois éclats, réveiller la porte, arriver au Sanctuaire, rencontrer Maëlle, descendre le grand pont vers Clairval, parler à Thibault, chasser trois Veilleurs égarés du relais royal, sauver Gaspard, recevoir le fragment de couronne (Épée du Souverain + Pas court), obtenir la clé du manoir auprès d'Orielle, rencontrer Vaëron (décret « Reculez »). Retour possible à tout moment par l'arche du Sanctuaire.

Depuis la tranche de qualité (étape 3) : apprivoiser une raie céleste avec trois fleurs de rosée et la monter (vol au regard, endurance, sifflet de rappel, recréation si elle est perdue, chute lente au démontage) ; voir le ciel se plisser et une Arrivée se poser sur un site réservé des Prairies (« La maison égarée » avec Émile le boulanger, « La caisse tombée », « La barque échouée »), que Lisbeth situe depuis l'observatoire ; remettre en marche le moulin de Clairval (Rosalie) et moudre son blé ; traverser le Lac du Crépuscule sur des dalles qui n'émergent qu'au crépuscule et à l'aube, jusqu'à l'îlot et sa meule royale. Le fragment de couronne est visible par les autres joueurs.

Depuis l'étape du domaine (étape 4) : Orielle ouvre l'écran des **projets du domaine** (dix projets : aile ouest, forge royale, écuries, serres, maison des explorateurs, galerie des merveilles, salle des passages, chambres des compagnons, jardin, marché et ateliers) ; chaque projet montre son emprise dans le monde, signale les constructions du joueur qui la gênent et propose de bâtir autour sans y toucher, consomme des ressources des Prairies et transforme le manoir immédiatement, les inventaires étant préservés. Six compagnons se recrutent par de vraies rencontres (Sabine, Loïc, Hanna, Théo, Marguerite, Ysolde) et s'installent dans l'annexe bâtie ; Sabine affûte l'Épée du Souverain. Les portes de relais touchées en voyage (Sanctuaire, Clairval, relais royal) rejoignent la salle des passages, qui mène ensuite à chaque relais connu. Les vitrines de la galerie exposent un objet avec son histoire. Le journal a gagné les onglets Domaine, Carte et Options (densité des effets, secousses, flashes, marqueurs, indications discrètes, indices de tutoriel).

## Vérifications effectuées dans cet environnement (sans affichage)

| Vérification | Résultat |
|---|---|
| Compilation complète (`./gradlew build`, JAR produit) | OK |
| Tests unitaires (`./gradlew test`) : validité des quêtes/dialogues/Arrivées/projets/lieux/relais, références, langues, textures, modèles d'entités, cohérence des sites et des gabarits, logique de progression, aplanissement, sérialisation | 15/15 OK |
| GameTests headless (`./gradlew runGametest`) : registres de génération, réveil de la porte, quête « Le Passage », gabarits, pièces composées et marqueurs PNJ, raie (pacte, monte, rappel), dalles de rosée, meule du moulin, Arrivée placée avec son occupant, projet (conflits, blocs épargnés, inventaires rendus), lieux/relais/portes (découverte, réseau éteint puis éveillé, voyage), vitrine | 11/11 OK |
| Serveur dédié réel (`./gradlew runAutotest`) : dimension, lieux à position fixe, terrain aplani, personnages essentiels, Veilleurs, vestige Overworld, observatoire, lac, moulin, Arrivées, raie, portes de relais générées avec leur identifiant, découverte de Clairval, projet « aile ouest » validé sur le manoir réel (conflit détecté, construction autour, coffre et contenu intacts, ressources consommées), voyage Clairval → Sanctuaire | 58 vérifications OK (voir journal) |
| Client réel sans écran (`./gradlew runCapture`, Xvfb + Mesa logiciel) : 17 captures du scénario automatique (étapes 1 à 3) | OK, images dans `docs/captures/` ; les écrans de l'étape 4 (chantier, relais, carte, options, vitrine, porte de relais) seront capturés à la prochaine campagne de captures |

## Non vérifié (limites de l'environnement : pas de joueur réel, pas d'audio)

- Animations en mouvement (cerf, poisson-lune, raie en vol, ailes du moulin, préparation du Veilleur), particules dans la durée, sons : seules des images fixes ont été observées, sans périphérique audio.
- Sensation du combat contre les Veilleurs, du Pas court, de l'enchaînement de l'épée et du pilotage de la raie (les mécaniques sont testées côté serveur, pas « en main » ; le vol au regard dépend des entrées du client).
- Enchaînement complet des quêtes avec un vrai joueur au clavier (l'enchaînement est testé avec un joueur factice en GameTest).
- Deux joueurs sur serveur dédié.

Ces scénarios sont documentés comme **à jouer** dans `BACKLOG.md` (section A).

## Fonctionnel / partiel / restant

- **Fonctionnel** : chaîne de compilation, dimension et biomes, entrée et retour, structures à position fixe et pièces composées, PNJ essentiels et réapparition, dialogues et quêtes de l'acte I, huit quêtes secondaires des Prairies, journal (quêtes, pouvoirs, domaine, carte, options) et marqueurs, trois pouvoirs et majesté, cinq créatures dont la monture, modèles d'entités en données, système d'Arrivées (trois types, quatre sites), blocs scénarisés (dalle de rosée, moulin, meules, vitrine, porte de relais), domaine (dix projets, recrutement, salle des passages, galerie, affûtage), commandes d'administration, générateurs d'assets, tests.
- **Partiel** : cerf-lanterne (sentiers vers des secrets à venir) ; musique (pistes vanilla) ; chemins hors plateaux ; compagnons installés mais sans scènes de vie ni échanges marchands ; domaine partagé par le monde en multijoueur (pas de domaine par groupe).
- **Restant** : voir `BACKLOG.md` — cinq régions, prétendants, campagne et couronnement, pouvoirs supérieurs, règne, optimisation.

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
