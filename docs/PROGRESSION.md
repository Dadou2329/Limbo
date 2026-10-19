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

**État** : les actes I à IV sont jouables de bout en bout, jusqu'au couronnement et au premier acte royal (quête `le_couronnement`). L'acte V (le règne) est décrit dans `BACKLOG.md`.

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

## Campagne des actes II à IV (étape 6)

| Quête | Acte | Ce qu'il faut faire |
|---|---|---|
| Le seigneur de Clairval | II | Restaurer l'aile ouest, la forge royale et la salle des passages ; recruter Sabine, Loïc et Hanna ; atteindre Port-Nacre et Sylvécime ; rencontrer les cinq prétendants ; Elouan ouvre les quartiers extérieurs de la capitale. |
| Le Prétendant | III | Mener les cinq aventures régionales ; comprendre les routes éteintes (Théo → Lisbeth → archives royales) ; obtenir quatre reconnaissances ; relier les cinq villes au réseau des passages. |
| Les routes éteintes | III | Théo (cartes), Lisbeth (ciel), Maud à la tour des archives : les routes répondaient à la couronne. |
| La danse des racines | III | Aélis reconnaît ceux qui ont partagé l'eau ; sinon, duel amical au jardin de l'épreuve (racines, chevaliers de bois blanc, fleurs de vie, envolées). |
| Le pont du Vent-Debout / L'expédition de l'archipel | III | Secourir l'expédition du capitaine Lio (trois gardiens des épaves) ou tenir le pont du navire volant de Kaël (portes dans le dos, escales sur les îlots). |
| La salle des miroirs | III | Duel rituel d'Orsane : reflets (la vraie laisse une traînée), puis armure colossale aux trois points de rupture. |
| Le tournoi des heures | III | Trois épreuves dans la cour des heures (Rachid) : scorpion et sentinelle, sentinelles, champion des heures ; Sahir reconnaît le champion. Son duel rituel (salves suspendues, fenêtres) reste proposé. |
| La Couronne | IV | Aldric ouvre le palais ; Bertrand révèle le sort du roi (il tient la lumière des frontières, les Arrivées sont la fonction du trône) ; Aubin envoie vers Vaëron sur la place ; duel final (légions et bannières, forteresse, épée solaire) ; Maëlle prépare le couronnement. |
| Le couronnement | IV | Tiphaine ouvre la cérémonie (délégations présentes, lanternes rallumées, ponts reformés, couronne assemblée, +60 de majesté) ; premier acte royal : attirer le fragment du belvédère et y retrouver le sceau du roi. |

Les duels sont des épreuves amicales (à bout de forces, retour à l'entrée) sauf le duel final, où la défaite ramène aussi à l'entrée : tout duel peut être repris. Chaque boss annonce ses coups ; les phases sont annoncées en clair.

## Six régions (étape 5)

| Région | Centre (x/z) | Ville et prétendant | Première aventure | Monture | Matière |
|---|---|---|---|---|---|
| Prairies de l'Aube | 0 / 0 | Clairval (Vaëron en visite) | acte I | raie céleste (3 fleurs de rosée) | pierre nacrée, bois d'aube |
| Mer suspendue | 1000 / −750 | Port-Nacre — Kaël, prince des routes | « La cargaison perdue » : plonger jusqu'au navire perdu (1150/−950) et remonter son journal de bord | tortue des lagons (3 poissons-lanternes) | sable et coraux de nacre, perles |
| Jardins des Géants | 1050 / 400 | Sylvécime — Aélis | « La source tarie » : un cristal de nacre dans le bassin de la serre ancienne (900/520), puis le choix de l'eau (partagée ou gardée) | scarabée de sève (3 fruits de géant) | bois des géants, pétales, sève ascendante |
| Désert des Heures | 150 / 1250 | Méridiane — Sahir | « Le jardin des heures » : un sablier d'opale troqué à Nérine (marché flottant, une perle) rendu à Ismaël (380/1130) | gazelle d'opale (3 dattes) | grès et pierre des heures, cadrans, pavés horaires |
| Montagnes de Verre | −1150 / 500 | Verréole — Orsane | « Le pont qui chante » : trois éclats résonnants pris sur le pont (−1150/680) | griffon de verre (3 thés des cimes), vol | pierre de verre, cristal résonnant, ponts de verre |
| Couronne Immobile | −800 / −800 | Haute-Garde — Vaëron (Aubin, l'intendant) ; quartier des portes (Elouan) | « Les murs de Haute-Garde » : cinq pains de la cour et deux lanternes royales | cerf royal (3 pains de la cour) | pierre de couronne, lanternes royales |

Chaque ville a une porte de relais que son dialogue d'accueil ajoute au réseau des passages. Les routes pavées relient Clairval au quai des Falaises (Mer), à Sylvécime, à Méridiane, à Verréole et à Haute-Garde ; les lampadaires jalonnent le chemin.

Montures : approcher la bête avec son appât en main, la nourrir trois fois ; clic droit pour monter ; avancer dans la direction du regard ; barre d'espace pour sauter (au sol) ; sifflet des montures pour la rappeler (accroupi : changer de monture liée). Les montures volantes (raie, griffon) montent et descendent avec le regard et ont une endurance.

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
| Port-Nacre (capitainerie) | 950 / 66 / −750 ; porte de relais 950 / 66 / −761 |
| Sylvécime | 1050 / 72 / 350 ; porte 1040 / 72 / 362 |
| Méridiane | 150 / 66 / 1250 ; porte 150 / 66 / 1234 |
| Verréole (palais des miroirs) | −1150 / 96 / 500 ; porte −1150 / 96 / 487 |
| Haute-Garde (place d'armes) | −610 / 82 / −610 ; porte −622 / 82 / −610 |
| Quartier des portes | −900 / 78 / −720 |
| Place des cérémonies (capitale) | −900 / 86 / −900 ; palais au nord (grande porte −900 / 88 / −941) ; marché −955 / −880 ; ambassades −845 / −880 ; archives −955 / −945 ; ateliers −900 / −840 ; résidences −838 / −945 |
| Arènes | jardin de l'épreuve 1150 / 72 / 300 ; Vent-Debout 1000 / 90 / −850 ; salle des miroirs −1230 / 96 / 500 ; cour des heures 230 / 66 / 1330 ; campement de l'archipel 1250 / 66 / −650 |
| Sites d'Arrivée du royaume | −700 / 86 / −1060 ; −1060 / 86 / −700 |
| Palais (silhouette céleste) | cap nord-ouest, non chargé |
