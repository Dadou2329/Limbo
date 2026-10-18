# État d'avancement — version 0.1.0 « Le Passage »

Dernière mise à jour : 18 octobre 2026. Ce document distingue honnêtement ce qui est **fonctionnel et vérifié**, ce qui est **fonctionnel mais non vérifié en jeu** (compilé, testé hors affichage), ce qui est **partiel** et ce qui **reste à réaliser**.

## Résultat jouable de cette tranche

Dans une nouvelle sauvegarde de survie : lire le carnet, fabriquer la boussole nacrée, trouver un vestige, réunir trois éclats, réveiller la porte, arriver au Sanctuaire, rencontrer Maëlle, descendre le grand pont vers Clairval, parler à Thibault, chasser trois Veilleurs égarés du relais royal, sauver Gaspard, recevoir le fragment de couronne (Épée du Souverain + Pas court), obtenir la clé du manoir auprès d'Orielle, rencontrer Vaëron (décret « Reculez »). Retour possible à tout moment par l'arche du Sanctuaire.

## Vérifications effectuées dans cet environnement (sans affichage)

| Vérification | Résultat |
|---|---|
| Compilation complète (`./gradlew build`, JAR produit) | OK |
| Tests unitaires (`./gradlew test`) : validité des quêtes/dialogues, références, langues, textures, cohérence des sites, logique de progression, aplanissement, sérialisation | 11/11 OK |
| GameTests headless (`./gradlew runGametest`) : registres de génération, réveil de la porte, quête « Le Passage », gabarits, pièces composées et marqueurs PNJ | 4/4 OK |
| Serveur dédié réel (`./gradlew runAutotest`) : dimension, génération des lieux à position fixe, terrain aplani, personnages essentiels, Veilleurs, vestige Overworld | 22 vérifications OK (voir journal) |
| Client réel sans écran (`./gradlew runCapture`, Xvfb + Mesa logiciel) : 12 captures du scénario automatique — arche, pont, traversée de l'arche vers l'Overworld, dialogue de Maëlle, ciel et palais, journal (quêtes, pouvoirs), Clairval, manoir, relais avec Veilleur, nuit | OK, images dans `docs/captures/` |

## Non vérifié (limites de l'environnement : pas de joueur réel, pas d'audio)

- Animations en mouvement (cerf, poisson-lune, préparation du Veilleur), particules dans la durée, sons : seules des images fixes ont été observées, sans périphérique audio.
- Sensation du combat contre les Veilleurs, du Pas court et de l'enchaînement de l'épée (les mécaniques sont testées côté serveur, pas « en main »).
- Enchaînement complet des deux quêtes avec un vrai joueur au clavier (l'enchaînement est testé avec un joueur factice en GameTest).
- Deux joueurs sur serveur dédié.

Ces scénarios sont documentés comme **à jouer** dans `BACKLOG.md` (section A).

## Fonctionnel / partiel / restant

- **Fonctionnel** : chaîne de compilation, dimension et biomes, entrée et retour, structures à position fixe, PNJ essentiels et réapparition, dialogues et quêtes de l'acte I, journal et marqueurs, trois pouvoirs et majesté, quatre créatures, commandes d'administration, générateurs d'assets, tests.
- **Partiel** : manoir (lieu complet, restauration à venir) ; cerf-lanterne (sentiers vers des secrets à venir) ; musique (pistes vanilla) ; chemins hors plateaux.
- **Restant** : voir `BACKLOG.md` — montures, Arrivées, domaine et recrutement, cinq régions, prétendants, campagne et couronnement, pouvoirs supérieurs, règne, optimisation.

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
