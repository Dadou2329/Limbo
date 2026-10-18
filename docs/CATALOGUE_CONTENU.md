# Catalogue du contenu réellement implémenté (version 0.1.0)

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

## Blocs (23) et objets

Pierre nacrée (brute, taillée, polie, sculptée, escalier, dalle, muret), cristal de nacre (lumineux), terre des Limbes, herbe argentée (s'étend, redevient terre), bûche/planches/escalier/dalle/barrière/porte/feuilles/pousse d'aube (arbre fonctionnel), fibres nacrées (lumineuses), herbe de l'aube, fleur de rosée, lanterne nacrée, Porte des Limbes.

Objets : éclat de nacre, boussole nacrée, fibres nacrées, herbe de l'aube, bouillon de l'aube (nourriture + régénération), fragment de couronne, Épée du Souverain, clé du manoir, 4 œufs d'apparition. Recettes : 18 (artisanat et tailleur de pierre).

## Créatures (4 types)

| Créature | Comportement | État |
|---|---|---|
| Habitant (9 apparences, rôles) | dialogue au clic droit, retour au domicile, invulnérable si essentiel, réapparition | F |
| Poisson-lune | vol lent ambiant, lumineux, inoffensif | F (rendu non vérifié) |
| Cerf-lanterne | paisible, fuite si blessé, bois lumineux au crépuscule avec traînée | P (rendu observé ; sentiers vers des secrets : backlog) |
| Veilleur égaré | hostile, préparation visible 18 ticks puis coup en arc, résistance au recul | F (rendu et agressivité observés en capture) |

## Personnages et dialogues (10 dialogues)

Maëlle (accueil, explications, retour), Thibault (accueil, conseils de combat, suite), Gaspard (sauvetage, fragment), Orielle (clé du manoir), Brann (forge), Vaëron (première rencontre, son passé), trois habitants de Clairval (scènes de vie).

## Quêtes (2 principales, 13 étapes)

`le_passage` (5 étapes), `l_etranger` (8 étapes). Journal (J) avec suivi, marqueur HUD et monde, notifications.

## Pouvoirs (3)

Épée du Souverain (invocation/rappel, dissipation si lâchée, enchaînement à 3 coups avec arc), Pas court (téléportation vérifiée : espace libre, sol, chunk chargé), Décret « Reculez » (vague de repoussement). Barre de majesté, 3 emplacements reconfigurables, fragment de couronne rendu au-dessus du joueur local.

## Assets

- 55 textures originales générées (blocs, objets, 9 skins, 3 créatures, particules, interface, icône).
- 19 sons originaux synthétisés (portes, pouvoirs, quêtes, créatures, interface).
- 12 gabarits de structures (≈ 25 000 blocs), 4 particules.
- Musique : pistes vanilla de Minecraft (meadow, cherry grove, forest) en attendant des compositions originales.

## Compteurs par rapport aux objectifs de la version complète

| Objectif complet | Réalisé |
|---|---|
| 6 régions × 3 sous-biomes | 1 région, 3 sous-biomes |
| 6 centres habités + cité de Vaëron | 1 (Clairval) |
| 40 lieux d'intérêt | 5 (vestige, sanctuaire, Clairval, manoir, relais) |
| 5 prétendants | 1 introduit (Vaëron), 4 à réaliser |
| 30 quêtes principales / 40 secondaires | 2 / 0 |
| 12 types d'Arrivées | 0 (système à réaliser) |
| 24 créatures / 5 montures | 4 / 0 |
| 24 techniques royales | 3 |
| 10 épreuves majeures | 1 combat représentatif |
