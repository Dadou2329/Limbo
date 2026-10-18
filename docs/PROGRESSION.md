# Schéma de progression

## Campagne (ossature)

```
ACTE I — L'ÉTRANGER            ACTE II — LE SEIGNEUR DE CLAIRVAL     ACTE III — LE PRÉTENDANT
 Overworld : vestige, porte     Restaurer les installations           Grandes aventures régionales
 Sanctuaire, Maëlle             Recruter des habitants                Soutiens des cinq prétendants
 Clairval, Thibault             Deux régions, relais                  Arsenal complet, relations
 Relais royal : Veilleurs       Rencontrer les cinq prétendants       Révélations sur les routes
 Gaspard, fragment de couronne  Quartiers extérieurs de la capitale
 Manoir (Orielle), Vaëron
       ↓                                  ↓                                     ↓
ACTE IV — LA COURONNE          COURONNEMENT JOUABLE                  ACTE V — LE RÈGNE
 Derniers accès, conflit        Délégations, ponts, lumière           Cité arrivée, tournoi,
 avec Vaëron, épreuve finale    Cérémonie courte, premier acte royal  navire royal, frontières
```

**État** : l'acte I est jouable de bout en bout (quêtes `le_passage` et `l_etranger`). Les actes suivants sont décrits dans `BACKLOG.md`.

## Quêtes de l'acte I

### Le Passage (Overworld)
1. Lire le carnet, fabriquer une boussole nacrée (boussole + 4 éclats d'améthyste).
2. Suivre la boussole jusqu'à un vestige (structure `les_limbes:vestige`, prairies/forêts/savanes/taïgas…).
3. Réunir trois éclats de nacre (coffre du vestige, cave sous la trappe, butin des Veilleurs plus tard).
4. Réveiller la porte : clic droit avec trois éclats sur la pierre gravée.
5. Franchir le voile → arrivée au Sanctuaire ; démarre « L'Étranger ».

### L'Étranger (Limbes)
1. Parler à Maëlle (Sanctuaire).
2. Descendre le pont jusqu'à Clairval.
3. Parler à Thibault (auberge du Moulin).
4. Vaincre trois Veilleurs égarés au relais royal (apparition garantie à l'entrée de l'étape).
5. Parler à Gaspard (relais) → **fragment de couronne**, Épée du Souverain, Pas court, +40 majesté max.
6. Rapporter la nouvelle à Thibault.
7. Rencontrer Orielle au manoir → **clé du manoir**.
8. Parler à Vaëron sur la place → décret « Reculez » ; fin de l'acte I (+3 niveaux).

## Pouvoirs

| Pouvoir | Branche | Coût | Recharge | Débloqué par |
|---|---|---|---|---|
| Épée du Souverain | Arsenal | 10 | 1 s | fragment de couronne (Gaspard) |
| Pas court | Portes | 15 | 1,5 s | fragment de couronne (Gaspard) |
| Décret : Reculez | Décrets | 20 | 5 s | rencontre de Vaëron |

Majesté : 60 au départ, +40 avec le fragment ; régénération 0,4/tick (8/s).

## Domaine (acte II, début)

Orielle (manoir) ouvre l'écran des **projets du domaine** une fois la clé reçue. Ordre conseillé : aile ouest → forge royale (Sabine) → écuries (Loïc) → serres (Hanna) → maison des explorateurs (Théo, Ysolde) → galerie des merveilles → salle des passages (Pas court) → chambres des compagnons → jardin → marché et ateliers (Marguerite). Chaque projet coûte des ressources des Prairies (planches et pierre nacrée surtout) et pose une annexe autour du manoir ; les recrues s'y installent.

Recrutements (quêtes secondaires) : Sabine à la forge de Clairval (5 cristaux de nacre), Loïc à la cabane du lac, Hanna au jardin à l'est de Clairval, Théo au pied du pont, Marguerite à l'auberge, Ysolde avec l'Arrivée « La barque échouée ». Après recrutement, chacun attend que son installation soit bâtie, puis y vit et propose de nouveaux services (affûtage de l'épée chez Sabine).

Réseau des passages : touchez les portes de relais (Sanctuaire, place de Clairval, relais royal) pour les apprendre ; la salle des passages du manoir éveille le réseau et chaque porte mène alors à n'importe quel relais connu.

## Déblocages par exploration / rencontres

- Vestige (Overworld) → entrée.
- Sanctuaire → retour fiable, premier dialogue.
- Clairval → auberge, forge, maisons, moulin, place ; habitants avec dialogues.
- Relais royal → combat représentatif, Gaspard, butin.
- Manoir → foyer, puis domaine (10 projets), compagnons, salle des passages.
- Lieux d'intérêt (10 dans les Prairies) → inscrits à la carte du journal en s'en approchant.
- Vergers suspendus / vallée des lanternes → biomes à découvrir ; cerfs-lanternes, poissons-lune, lanternes perdues.

## Positions persistantes (toutes graines)

| Lieu | Coordonnées (Limbes) |
|---|---|
| Sanctuaire de l'Arrivée (île) | 0 / 82 / 0 ; arche de retour 0 / 84 / −8 |
| Pied du pont | 0 / 66 / 56 |
| Clairval (place) | 0 / 66 / 140 |
| Manoir | −120 / 66 / 150 |
| Relais royal | 150 / 66 / 60 |
| Observatoire | 30 / 66 / 112 |
| Lac du Crépuscule | −60 / 66 / 20 |
| Portes de relais | Sanctuaire −8 / 83 / 0 ; Clairval 0 / 66 / 149 ; relais royal 148 / 66 / 54 ; manoir −120 / 66 / 174 (salle des passages) |
| Palais (silhouette céleste) | cap nord-ouest, non chargé |
