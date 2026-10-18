# Catalogue du contenu réellement implémenté (version 0.2.0)

Légende : **F** fonctionnel · **P** partiel · **—** absent (voir `BACKLOG.md`).

## Accès et monde

| Élément | État | Accès / conditions | Vérification |
|---|---|---|---|
| Carnet du voyageur (livre français) | F | première connexion | GameTest quêtes ; reçu en main à la première connexion (capture client) |
| Boussole nacrée (indication en barre d'action) | F | recette boussole + 4 améthystes | JUnit directions |
| Vestige des Limbes (Overworld, structure composée, cave cachée, journal, porte dormante) | F | random_spread, biomes de prairies/forêts/savanes/taïgas | gabarit chargé et pièce générée (GameTest) ; localisable sur serveur dédié (AutoTest) |
| Porte des Limbes (dormante → réveil avec 3 éclats, voile connecté ≤ 32 blocs) | F | clic droit avec éclats | GameTest réveil/idempotence |
| Voyage Overworld ↔ Limbes, retour exact au point de départ | F | marcher dans le voile | traversée Limbes → Overworld observée en capture client (marche dans le voile de l'arche) ; le sens Overworld → Limbes est couvert par le GameTest de la porte |
| Dimension des Limbes : terrain suspendu, îles flottantes, brumes, lacs, cristaux | F | — | AutoTest serveur dédié (génération réelle) ; terrain, îles et arbres observés en captures client |
| Biomes : plaines nacrées, vergers suspendus, vallée des lanternes | F | multi-bruit (température/humidité) | — |
| Ciel nacré, constellations, silhouette du palais, nuages bas | F | client | voûte, palais (jour et nuit, halo) et étoiles observés en captures client |
| Protection des brumes (retour au dernier lieu sûr, chute lente) | F | y < 12 | non vérifié en jeu |
| Sanctuaire de l'Arrivée (île, esplanade, arche de retour, Maëlle, grand pont) | F | position fixe 0/82/0 | AutoTest : arche active, arrivée libre, Maëlle présente |
| Clairval (place, fontaine, auberge, forge, 3 maisons, moulin, chemins, jardins) | F | position fixe 0/66/140 | AutoTest : place pavée, Thibault, Brann, Vaëron présents |
| Manoir du joueur (hall, bibliothèque, cuisine, réserve, 4 chambres, aile à restaurer, jardin, serre) | P | clé donnée par Orielle ; restauration non implémentée | AutoTest : généré, Orielle présente |
| Ancien relais royal (ruine, écurie, Gaspard, 3 Veilleurs) | F | position fixe 150/66/60 | AutoTest : généré, Gaspard et 3 Veilleurs présents |
| Observatoire de Clairval (tour, lunette, registre, Lisbeth qui situe les Arrivées) | F | Clairval, 30/66/112 | AutoTest : Lisbeth présente |
| Lac du Crépuscule (dalles de rosée qui n'émergent qu'au crépuscule et à l'aube, îlot-sanctuaire, meule royale) | F | position fixe −60/66/20 | GameTest (émergence selon l'heure), AutoTest (lac, dalles, meule) |
| Moulin de Clairval immobilisé puis remis en marche (ailes animées, meule qui transforme le blé en farine) | F | quête « Le moulin immobilisé » (Rosalie) | GameTest (meule, action modifier_bloc), AutoTest (axe immobile généré) |
| Arrivées : le ciel se plisse, quelque chose rejoint les Limbes (types en données, sites réservés aplanis, vérification de la zone, limite simultanée, annonce, effet de ciel, archivage) | F | automatique après le début de l'acte I ; `/limbes arrivee` pour l'administration | GameTest (placement + occupant), AutoTest (déclenchement réel, unicité, résolution) |
| Arrivée « La maison égarée » (Émile le boulanger, quête, choix : rester ou s'installer à Clairval) | F | après « L'Étranger » commencé | AutoTest : maison et Émile en place |
| Arrivée « La caisse tombée » (petite découverte, expire après un jour et demi) | F | aléatoire | GameTest (gabarit) |
| Raie céleste : pacte (3 fleurs de rosée), monte, vol (regard haut/bas), endurance, rappel au sifflet, recréation si perdue, chute lente au démontage | F | Prairies (apparition naturelle) | GameTest (pacte, monte, rappel), AutoTest (pacte et recréation) |

## Blocs (27) et objets

Pierre nacrée (brute, taillée, polie, sculptée, escalier, dalle, muret), cristal de nacre (lumineux), terre des Limbes, herbe argentée (s'étend, redevient terre), bûche/planches/escalier/dalle/barrière/porte/feuilles/pousse d'aube (arbre fonctionnel), fibres nacrées (lumineuses), herbe de l'aube, fleur de rosée, lanterne nacrée, Porte des Limbes, dalle de rosée (appui au crépuscule), meule royale, meule du moulin (blé → farine), ailes de moulin (bloc-entité animé).

Objets : éclat de nacre, boussole nacrée, fibres nacrées, herbe de l'aube, bouillon de l'aube (nourriture + régénération), fragment de couronne, Épée du Souverain, clé du manoir, sifflet de raie, sac de farine, pain d'Émile (absorption), 5 œufs d'apparition. Recettes : 20 (artisanat et tailleur de pierre).

## Créatures (5 types)

| Créature | Comportement | État |
|---|---|---|
| Habitant (9 apparences, rôles) | dialogue au clic droit, retour au domicile, invulnérable si essentiel, réapparition | F |
| Poisson-lune | vol lent ambiant, lumineux, inoffensif | F (rendu non vérifié) |
| Cerf-lanterne | paisible, fuite si blessé, bois lumineux au crépuscule avec traînée | P (rendu observé ; sentiers vers des secrets : backlog) |
| Veilleur égaré | hostile, préparation visible 18 ticks puis coup en arc, résistance au recul | F (rendu et agressivité observés en capture) |
| Raie céleste | vol ambiant, approche d'une fleur de rosée, pacte, monture volante avec endurance, rappel, recréation | F (modèle piloté par données ; rendu à vérifier en capture) |

## Personnages et dialogues (13 dialogues)

Maëlle (accueil, explications, retour), Thibault (accueil, conseils de combat, suite), Gaspard (sauvetage, fragment), Orielle (clé du manoir), Brann (forge), Vaëron (première rencontre, son passé), Lisbeth (observatoire : situe les Arrivées en direct dans le texte), Rosalie (meunière), Émile (boulanger tombé du ciel, choix d'installation), trois habitants de Clairval (scènes de vie).

## Quêtes (2 principales et 2 secondaires, 20 étapes)

Principales : `le_passage` (5 étapes), `l_etranger` (8 étapes). Secondaires : `le_moulin_immobilise` (3 étapes, transformation visible du moulin), `la_maison_egaree` (4 étapes, Arrivée, choix à conséquence). Journal (J) avec suivi, marqueur HUD et monde, notifications.

## Pouvoirs (3)

Épée du Souverain (invocation/rappel, dissipation si lâchée, enchaînement à 3 coups avec arc), Pas court (téléportation vérifiée : espace libre, sol, chunk chargé), Décret « Reculez » (vague de repoussement). Barre de majesté, 3 emplacements reconfigurables, fragment de couronne rendu au-dessus du joueur local.

## Assets

- 67 textures originales générées (blocs, objets, 12 skins, 4 créatures dont une peinte automatiquement à partir de son modèle, particules, interface, icône).
- 24 sons originaux synthétisés (portes, pouvoirs, quêtes, créatures, interface, Arrivée, moulin).
- 16 gabarits de structures (≈ 34 000 blocs), 4 particules.
- Modèles d'entités pilotés par données (`tools/models`) : un modèle = os, cubes, animations et peintures ; JSON et texture produits ensemble.
- Musique : pistes vanilla de Minecraft (meadow, cherry grove, forest) en attendant des compositions originales.

## Compteurs par rapport aux objectifs de la version complète

| Objectif complet | Réalisé |
|---|---|
| 6 régions × 3 sous-biomes | 1 région, 3 sous-biomes |
| 6 centres habités + cité de Vaëron | 1 (Clairval) |
| 40 lieux d'intérêt | 8 (vestige, sanctuaire, Clairval, manoir, relais, observatoire, lac du Crépuscule, maison égarée) |
| 5 prétendants | 1 introduit (Vaëron), 4 à réaliser |
| 30 quêtes principales / 40 secondaires | 2 / 2 |
| 12 types d'Arrivées | 2 (système complet : sites, vérification, annonce, archivage) |
| 24 créatures / 5 montures | 5 / 1 (raie céleste) |
| 24 techniques royales | 3 |
| 10 épreuves majeures | 1 combat représentatif |
