# État d'avancement — version 0.1.0 « Le Passage »

Dernière mise à jour : 18 octobre 2026. Ce document distingue honnêtement ce qui est **fonctionnel et vérifié**, ce qui est **fonctionnel mais non vérifié en jeu** (compilé, testé hors affichage), ce qui est **partiel** et ce qui **reste à réaliser**.

## Résultat jouable de cette tranche

Dans une nouvelle sauvegarde de survie : lire le carnet, fabriquer la boussole nacrée, trouver un vestige, réunir trois éclats, réveiller la porte, arriver au Sanctuaire, rencontrer Maëlle, descendre le grand pont vers Clairval, parler à Thibault, chasser trois Veilleurs égarés du relais royal, sauver Gaspard, recevoir le fragment de couronne (Épée du Souverain + Pas court), obtenir la clé du manoir auprès d'Orielle, rencontrer Vaëron (décret « Reculez »). Retour possible à tout moment par l'arche du Sanctuaire.

## Vérifications effectuées dans cet environnement (sans affichage)

| Vérification | Résultat |
|---|---|
| Compilation complète (`./gradlew build`, JAR produit) | OK |
| Tests unitaires (`./gradlew test`) : validité des quêtes/dialogues, références, langues, textures, cohérence des sites, logique de progression, aplanissement, sérialisation | 11/11 OK |
| GameTests headless (`./gradlew runGametest`) : chargement des registres de génération, réveil de la porte, enchaînement complet de la quête « Le Passage » et objectifs manuels, chargement des 12 gabarits, génération réelle du Sanctuaire (arche active) et de Clairval (place) avec apparition de Maëlle et Thibault | voir section « Journal des exécutions » |
| Serveur dédié (`./gradlew runServer`) : démarrage avec le mod, dimension présente | voir section « Journal des exécutions » |

## Non vérifié (impossible ici : pas d'affichage ni de client)

- Rendu des blocs, des modèles d'entités, des particules, du ciel nacré, de la silhouette du palais, de l'interface (barre, journal, dialogue, marqueur) et du fragment de couronne au-dessus du joueur.
- Sensation du combat contre les Veilleurs, du Pas court et de l'enchaînement de l'épée.
- Génération du vestige dans un Overworld normal (le serveur de test est un monde plat).
- Deux joueurs sur serveur dédié.

Ces scénarios sont documentés comme **à jouer** dans `BACKLOG.md` (section A).

## Fonctionnel / partiel / restant

- **Fonctionnel** : chaîne de compilation, dimension et biomes, entrée et retour, structures à position fixe, PNJ essentiels et réapparition, dialogues et quêtes de l'acte I, journal et marqueurs, trois pouvoirs et majesté, quatre créatures, commandes d'administration, générateurs d'assets, tests.
- **Partiel** : manoir (lieu complet, restauration à venir) ; cerf-lanterne (sentiers vers des secrets à venir) ; ciel (rendu non observé) ; musique (pistes vanilla) ; chemins hors plateaux.
- **Restant** : voir `BACKLOG.md` — montures, Arrivées, domaine et recrutement, cinq régions, prétendants, campagne et couronnement, pouvoirs supérieurs, règne, optimisation.

## Journal des exécutions

(renseigné à la fin de chaque session de validation)
