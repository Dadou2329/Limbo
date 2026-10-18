# Vision — Les Limbes

> Document de synthèse. Le cahier des charges complet est dans `CAHIER_DES_CHARGES.md`.

## La promesse

Une immense dimension mystérieuse et belle où arrivent les lieux, objets, créatures et peuples disparus d'autres mondes. Le joueur s'y construit un foyer, voyage entre six régions, développe une grande magie royale, affronte cinq prétendants au trône et finit par devenir roi des Limbes. Le couronnement ouvre une véritable période de règne.

Trois envies qui se nourrissent : **explorer** (merveilles, histoires, ressources, personnes), **gagner en puissance** (nouveaux lieux, prétendants), **ramener ses découvertes chez soi** (domaine puis royaume).

Les Limbes ne sont pas une dimension d'horreur : mystère, curiosité, émerveillement, humour, villages accueillants. Des dangers et des combats grandioses, mais un monde où l'on a envie de vivre.

## Direction artistique

- Palette royale : or pâle, bleu profond, verre lumineux, constellations.
- Prairies de l'Aube : herbe argentée, eaux claires, ciel nacré, bois d'aube rosé, lanternes.
- Architecture : pierre nacrée et bois d'aube ; intérieurs lisibles, meublés, habités.
- Les Arrivées : parfois le ciel se plisse, et quelque chose rejoint les Limbes.

## Piliers de conception

1. **Serveur autoritaire** : dégâts, coûts, téléportations, progression, inventaires et constructions sont décidés côté serveur ; le client affiche.
2. **Contenu en données** : quêtes, dialogues, biomes, structures, génération sont des fichiers JSON/NBT ; les outils du dossier `tools/` les produisent de façon reproductible.
3. **Positions persistantes** : les lieux critiques sont placés à des coordonnées fixes (placement `les_limbes:fixed`) sur un terrain aplani (`les_limbes:aplanir`), quelle que soit la graine.
4. **Jamais bloqué** : objets critiques récupérables, PNJ essentiels invulnérables et réapparaissant, brumes qui ramènent, retour toujours possible.
5. **Lisibilité** : télégraphes de combat, journal avec objectif et marqueur, dialogues simples et naturels, tout en français.

## Les six régions (cible)

| Région | Ville | Prétendant | État |
|---|---|---|---|
| Prairies de l'Aube | Clairval | — (Vaëron de passage) | tranche 1 jouable |
| Mer suspendue | Port-Nacre | Kaël | à réaliser |
| Jardins des Géants | Sylvécime | Aélis | à réaliser |
| Désert des Heures | Méridiane | Sahir | à réaliser |
| Montagnes de Verre | Verréole | Orsane | à réaliser |
| Couronne Immobile | la capitale | Vaëron (cité propre) | silhouette céleste seulement |

## La grande magie royale (six branches)

Arsenal du Souverain, Portes du Palais, Décrets royaux, Garde astrale, Main des Limbes, Pactes royaux. Réserve de majesté qui se régénère vite ; techniques emblématiques (Jugement des constellations, Marche du Palais, Audience du Roi, Avènement). Tranche 1 : Épée du Souverain (enchaînement), Pas court, décret « Reculez ».

## Le domaine et le royaume

Manoir vieillissant près de Clairval → domaine restauré → bourg animé → résidence de prétendant → domaine royal relié à la capitale. Installations obligatoires : forge royale, écuries et volières, serres, maison des explorateurs, galerie des merveilles, salle des passages, chambres de compagnons, auberge/marché/ateliers, jardins. Gestion simple : choisir un projet, réunir des ressources, valider, voir la transformation. Respect absolu des constructions du joueur.
