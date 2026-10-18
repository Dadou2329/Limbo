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

## Déblocages par exploration / rencontres

- Vestige (Overworld) → entrée.
- Sanctuaire → retour fiable, premier dialogue.
- Clairval → auberge, forge, maisons, moulin, place ; habitants avec dialogues.
- Relais royal → combat représentatif, Gaspard, butin.
- Manoir → foyer (restauration : backlog).
- Vergers suspendus / vallée des lanternes → biomes à découvrir ; cerfs-lanternes, poissons-lune, lanternes perdues.

## Positions persistantes (toutes graines)

| Lieu | Coordonnées (Limbes) |
|---|---|
| Sanctuaire de l'Arrivée (île) | 0 / 82 / 0 ; arche de retour 0 / 84 / −8 |
| Pied du pont | 0 / 66 / 56 |
| Clairval (place) | 0 / 66 / 140 |
| Manoir | −120 / 66 / 150 |
| Relais royal | 150 / 66 / 60 |
| Palais (silhouette céleste) | cap nord-ouest, non chargé |
