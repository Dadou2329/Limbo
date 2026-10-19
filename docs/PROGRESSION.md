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

**État** : les cinq actes sont jouables de bout en bout : couronnement, premier acte royal, puis le règne (acte V : la cité arrivée, le grand tournoi, le navire royal, les frontières et l'Avènement).

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

## Pouvoirs (33 techniques, 7 branches)

Quatre emplacements (touches **R**, **V**, **G**, **B**) et une sélection radiale (**X**, clic = utiliser, Maj + clic = équiper). Le journal (J), onglet Pouvoirs, liste les techniques par branche avec coût et recharge. La Marche du Palais rend les Portes gratuites ; l'Avènement divise par deux coût et recharge de tout le reste.

| Technique | Branche | Coût | Recharge | Débloquée par |
|---|---|---|---|---|
| Épée du Souverain | Arsenal | 10 | 1 s | fragment de couronne (Gaspard) |
| Parade royale | Arsenal | 8 | 3 s | Sabine à la forge royale |
| Lance du Souverain | Arsenal | 12 | 1 s | Sabine à la forge royale |
| Charge de lance | Arsenal | 18 | 6 s | Sabine, après un affûtage |
| Changement d'arme | Arsenal | 5 | 0,5 s | Sabine, après deux affûtages |
| Arc du Souverain | Arsenal | 12 | 1 s | fin de l'acte II |
| Marque royale | Arsenal | 10 | 8 s | fin de l'acte II |
| Bouclier du Souverain | Arsenal | 12 | 1 s | Orsane reconnue |
| Armes satellites | Arsenal | 30 | 20 s | la salle des miroirs |
| Pas court | Portes | 15 | 1,5 s | fragment de couronne (Gaspard) |
| Porte directionnelle | Portes | 15 | 2 s | Aélis reconnue |
| Attaque d'angle | Portes | 20 | 4 s | fin de l'acte III |
| Sauvetage | Portes | 20 | 10 s | l'expédition de l'archipel ou le pont du Vent-Debout |
| Liaison royale | Portes | 25 | 30 s | les routes éteintes |
| Décret : Reculez | Décrets | 20 | 5 s | rencontre de Vaëron |
| Décret : Déposez les armes | Décrets | 25 | 15 s | le tournoi des heures |
| Décret : Tenez bon | Décrets | 20 | 20 s | Kaël reconnu |
| Décret : Que le chemin s'ouvre | Décrets | 30 | 30 s | Vaëron vaincu |
| Décret : Restez | Décrets | 20 | 10 s | Sahir reconnu |
| Décret : Élevez-vous | Décrets | 20 | 10 s | Vaëron vaincu |
| Chevalier astral | Garde astrale | 25 | 10 s | les murs de Haute-Garde (Aubin) |
| Archer astral | Garde astrale | 25 | 10 s | fin de l'acte III |
| Protecteur astral | Garde astrale | 30 | 15 s | fin de l'acte III |
| Ordre de garde | Garde astrale | 5 | 1 s | fin de l'acte III |
| Main des Limbes : saisir | Main des Limbes | 15 | 3 s | la danse des racines |
| Main des Limbes : passage | Main des Limbes | 20 | 5 s | couronnement |
| Main couronnée | Main des Limbes | 35 | 15 s | le sceau du roi (fin du couronnement) |
| Appel de monture | Pactes royaux | 10 | 5 s | couronnement |
| Pacte de combat | Pactes royaux | 20 | 15 s | couronnement |
| Jugement des constellations | Emblématique | 50 | 60 s | le sceau du roi |
| Marche du Palais | Emblématique | 40 | 45 s | le sceau du roi |
| Audience du Roi | Emblématique | 45 | 60 s | couronnement |
| Avènement | Emblématique | 80 | 300 s | les frontières (acte V) |

Majesté : 60 au départ, +40 avec le fragment, +60 au couronnement ; régénération 0,4/tick (8/s), quadruplée pendant l'Avènement. Les armes royales (épée, lance, arc, bouclier) sont invoquées : lâchées, elles se dissipent ; l'affûtage de Sabine s'applique à l'épée et à la lance. La garde astrale compte trois gardes au plus pendant une minute chacune ; les alliés (joueurs, habitants, gardes, montures liées) ne sont jamais touchés par les techniques de zone.

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

## Le règne (acte V, étape 7)

| Quête | Ce qu'il faut faire |
|---|---|
| La cité arrivée | Aldric appelle Brumeval (Arrivée majeure sur le site de l'ouest, −1060/−700) ; Hortense, la maire, demande quatre lanternes royales ; la place s'éclaire et sa porte rejoint le réseau des passages. |
| Le grand tournoi | Rachid ouvre cinq épreuves amicales dans la cour des heures : Aélis, Kaël, Orsane, Sahir, puis Vaëron ; titre de champion des Limbes. |
| Le navire royal | Le capitaine Lio (campement de l'archipel) demande 24 planches d'aube, 12 fibres nacrées et 3 perles ; son équipage achève le navire au chantier naval (1030/−800, porte de relais à bord) ; cap sur l'île des ruines (1300/−1000), où chante la baleine des hauteurs, pour retrouver le journal de l'expédition royale. |
| Les frontières | Bertrand envoie à la lisière des brumes (−900/−1380) : l'ancien roi tient la lumière ; tenir avec lui contre quatre Veilleurs et deux sentinelles, puis décider de son sort (rentrer ou garder la frontière). Récompense : l'**Avènement**. |

Lisbeth, à l'observatoire, permet ensuite de préparer une Arrivée (la caisse tombée) à la demande du souverain.

## Quêtes secondaires des régions (étape 8)

Chaque donneur propose sa quête dans son dialogue (premier choix) dès qu'on lui parle ; le journal la suit ; la remise vérifie les objets et donne la récompense.

| Région | Donneur (lieu) | Quête | À faire | Récompense |
|---|---|---|---|---|
| Prairies | Brann (forge de Clairval) | Les lanternes du pont | 6 lanternes nacrées | 4 cristaux de nacre |
| Prairies | Gaspard (relais royal) | La patrouille de Gaspard | vaincre 5 Veilleurs égarés | lanterne royale, pains d'Émile |
| Prairies | Lisbeth (observatoire) | Les trois ciels | observatoire des heures, phare du Dessous, atelier d'instruments | 3 perles |
| Prairies | Ysolde (manoir) | Les cartes d'Ysolde | quai des Falaises, sanctuaire des bancs, navire perdu | 3 perles |
| Mer | Maude (criée) | Les filets de Maude | 8 poissons-lanternes | 3 perles |
| Mer | Isaure (Port-Nacre) | La plongée d'Isaure | sanctuaire des bancs + 4 coraux de nacre | 4 coraux d'azur |
| Mer | Anselme (phare) | Le phare d'Anselme | vaincre 3 gardiens des épaves | 6 lanternes nacrées, perle |
| Mer | Loup (capitainerie) | Les cordages de Loup | 16 fibres nacrées, 8 planches d'aube | 2 perles |
| Jardins | Colombe (Sylvécime) | Le four de Colombe | 4 farines, 3 fruits de géant | miel, pains |
| Jardins | Aude (Sylvécime) | Les fils d'Aude | 10 fibres, 4 pétales roses, 4 bleus | 3 cristaux |
| Jardins | Perrin (Sylvécime) | Les simples de Perrin | 8 herbes de l'aube, 4 fleurs de rosée | 3 bouillons |
| Jardins | Guetteur (Sylvécime) | Le guet de Sylvécime | vaincre 4 rôdeurs des racines | fruits, perles |
| Désert | Horloger (Méridiane) | Les rouages de l'horloger | 4 éclats résonnants, 2 cristaux | 3 perles |
| Désert | Idir (Méridiane) | La caravane d'Idir | observatoire des heures, jardin fertile, cour des heures | 6 dattes, perle |
| Désert | Naïma (Méridiane) | Les jarres de Naïma | 8 sables d'opale, 4 grès des heures | dattes, perles |
| Désert | Ayoub (Méridiane) | Les sentinelles d'Ayoub | vaincre 3 sentinelles de sable, 3 scorpions | 3 perles |
| Montagnes | Gunnar (forge de Verréole) | Le métal de Gunnar | 8 pierres de verre, 4 éclats résonnants | 4 cristaux |
| Montagnes | Luthière (atelier) | L'accord de la luthière | pont qui chante + 6 éclats résonnants | perles, thé |
| Montagnes | Maître des bains | Les bains de Verréole | 4 thés des cimes, 6 pierres de verre taillées | thé, perle |
| Montagnes | Garde (palais des miroirs) | La garde de verre | vaincre 2 colosses, 3 échos résonnants | 4 cristaux, perle |
| Couronne | Boulanger (Haute-Garde) | Le pain de la cour | 6 farines, 2 miels de rosée | 6 pains de la cour |
| Couronne | Maîtresse d'école | L'école de Haute-Garde | Sanctuaire, marché flottant, serre ancienne | perles, pains |
| Couronne | Rolande (place d'armes) | La patrouille de Rolande | vaincre 3 sentinelles de la Couronne, 2 spectres | 2 lanternes royales, pains |
| Couronne | Elouan (quartier des portes) | Les lanternes du quartier | 4 lanternes royales (quartiers ouverts) | 3 perles |
| Capitale | Sidonie (palais) | Les cuisines du palais | 4 pains de la cour, 4 poissons-lanternes, 2 ragoûts (palais ouvert) | pains, ragoûts |
| Capitale | Teodor (ambassades) | Les présents des régions | 3 perles, 3 fruits de géant, 3 thés | 3 lanternes royales |
| Capitale | Maud (archives) | La chronique de Maud | navire perdu, serre ancienne, observatoire des heures, pont qui chante | 3 perles |

## Arrivées (13 types)

| Arrivée | Échelle | Sites | Particularité |
|---|---|---|---|
| La maison égarée, La caisse tombée, La charrette renversée, Le puits d'ailleurs, Le kiosque à musique | petite / moyenne | Prairies (3 sites) | Émile et son four ; coffres de voyage ; phonographe d'ailleurs |
| La barque échouée | moyenne | rive du lac | Ysolde, recrutable |
| Le radeau des naufragés | moyenne | îlot de la Mer (900 / −650) | Siméon, naufragé d'une mer d'en bas |
| La serre tombée | petite | Jardins (960 / 400) | légumes d'ailleurs |
| La tente du voyageur | petite | Désert (260 / 1150) | carnet de route |
| La cloche de verre | petite | Montagnes (−1060 / 560) | cloche à sonner |
| La bibliothèque perdue | moyenne | Couronne (−720 / −760) | chronique d'une ville sans nom, traité des plis du ciel |
| Le fragment du belvédère, La cité arrivée (Brumeval) | majeure | royaume (deux sites) | premier acte royal ; acte V |

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
| Sites d'Arrivée du royaume | −700 / 86 / −1060 (belvédère) ; −1060 / 86 / −700 (Brumeval, porte de relais −1060 / 87 / −692) |
| Chantier naval et navire royal | 1030 / 64 / −800 (pont) ; porte de relais à bord 1030 / 65 / −786 |
| Île des ruines | 1300 / 70 / −1000 |
| Lisière des brumes | −900 / 86 / −1380 (lanterne au sommet de la tour, −900 / 100 / −1380) |
| Palais (silhouette céleste) | cap nord-ouest, non chargé |
