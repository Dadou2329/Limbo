# Catalogue du contenu réellement implémenté (version 0.5.0)

Légende : **F** fonctionnel · **P** partiel · **—** absent (voir `BACKLOG.md`).

## Accès et monde

| Élément | État | Accès / conditions | Vérification |
|---|---|---|---|
| Carnet du voyageur (livre français) | F | première connexion | GameTest quêtes ; reçu en main à la première connexion (capture client) |
| Boussole nacrée (indication en barre d'action) | F | recette boussole + 4 améthystes | JUnit directions |
| Vestige des Limbes (Overworld, structure composée, cave cachée, journal, porte dormante) | F | random_spread, biomes de prairies/forêts/savanes/taïgas | gabarit chargé et pièce générée (GameTest) ; localisable sur serveur dédié (AutoTest) |
| Porte des Limbes (dormante → réveil avec 3 éclats, voile connecté ≤ 32 blocs) | F | clic droit avec éclats | GameTest réveil/idempotence |
| Voyage Overworld ↔ Limbes, retour exact au point de départ | F | marcher dans le voile | traversée observée en capture client ; GameTest de la porte |
| Dimension des Limbes : terrain suspendu, îles flottantes, brumes, lacs, cristaux | F | — | AutoTest serveur dédié (génération réelle) ; captures client |
| **Six régions** (source de biomes régionale `les_limbes:regions`, relief par région, transitions de 12 % entre régions, sous-biomes par bruit) : Prairies de l'Aube, Mer suspendue, Jardins des Géants, Désert des Heures, Montagnes de Verre, Couronne Immobile | F | centres : Prairies 0/0 ; Mer 1000/−750 ; Jardins 1050/400 ; Désert 150/1250 ; Montagnes −1150/500 ; Couronne −800/−800 | JUnit (régions aux sites, Prairies inchangées), AutoTest (biome, eau et profondeur de la Mer, plateau de Verréole, sols du Désert et des Jardins, plateau de Haute-Garde) |
| 18 biomes (3 par région) avec sols, végétation, ambiances et apparitions propres | F | — | chargement sur serveur dédié ; apparitions vérifiées par l'AutoTest |
| Mer suspendue : volume d'eau stable (y 63) sur un fond à 41–46, îles, rebords garantis au-dessus de l'eau, coraux de nacre et d'azur lumineux sous l'eau | F | région Mer | AutoTest (eau, profondeur ≥ 6, biome) |
| Routes pavées suivant le relief entre les sites (13 tronçons, lampadaires) | F | — | AutoTest (pavés au sud de Clairval) |
| Ciel nacré, constellations, silhouette du palais, nuages bas | F | client | captures client |
| Protection des brumes (retour au dernier lieu sûr, chute lente) | F | y < 12 | non vérifié en jeu |
| Sanctuaire de l'Arrivée (île, esplanade, arche de retour, Maëlle, grand pont) | F | position fixe 0/82/0 | AutoTest |
| Clairval (place, fontaine, auberge, forge, 3 maisons, moulin, chemins, jardins) | F | position fixe 0/66/140 | AutoTest |
| Manoir du joueur et ses dix annexes de projets | F | clé donnée par Orielle | AutoTest (projet réel) |
| Ancien relais royal (ruine, écurie, Gaspard, 3 Veilleurs) | F | position fixe 150/66/60 | AutoTest |
| Observatoire de Clairval (Lisbeth situe les Arrivées) | F | 30/66/112 | AutoTest |
| Lac du Crépuscule (dalles de rosée horaires, îlot, meule royale) | F | −60/66/20 | GameTest, AutoTest |
| Moulin de Clairval (ailes animées, meule blé → farine) | F | quête « Le moulin immobilisé » | GameTest, AutoTest |
| Arrivées (types en données, sites réservés, vérification, annonce, archivage) ; « La maison égarée », « La caisse tombée », « La barque échouée » | F | automatique après l'acte I | GameTest, AutoTest |
| **Port-Nacre** (île-ville de Kaël : capitainerie, quais, criée, marché aux perles, porte de relais) | F | 950/66/−750, région Mer | AutoTest (Kaël, porte de relais `port_nacre`) |
| Phare du Dessous, marché flottant (Nérine : troc perle → sablier), navire perdu (cale, journal de bord), sanctuaire des bancs (poissons-lunes), quai des Falaises (bout de la route) | F | région Mer | gabarits générés, lieux découvrables ; Nérine présente (AutoTest) |
| **Sylvécime** (ville d'Aélis dans les géants, sève ascendante pour monter, porte de relais) | F | 1050/72/350 | AutoTest (Aélis, porte `sylvecime`) |
| Serre ancienne (Oriane, source tarie), pépinière royale, arbre creux, clairière du géant (géant endormi) | F | région Jardins | AutoTest (Oriane, géant des jardins présent) |
| **Méridiane** (ville de Sahir : archives, horloge, quartier figé, cadrans des heures, pavés horaires, porte de relais) | F | 150/66/1250 | AutoTest (Sahir, porte `meridiane`) |
| Observatoire des heures, jardin des heures (Ismaël, sablier d'opale) | F | région Désert | AutoTest (Ismaël) |
| **Verréole** (cité d'Orsane sur son plateau : palais des miroirs, forge de Gunnar, bains, ponts de verre qui se forment à l'approche, porte de relais) | F | −1150/96/500 | AutoTest (Orsane, porte `verreole`) |
| Bains des sources, atelier d'instruments, pont qui chante (cristaux résonnants qui sonnent sous les pas) | F | région Montagnes | gabarits générés ; bloc testé |
| **Haute-Garde** (cité de Vaëron : murs, école, infirmerie, marché, place d'armes, intendant Aubin, porte de relais) | F | −610/82/−610 | AutoTest (Aubin, porte `haute_garde`, plateau) |
| Quartier des portes (Elouan, premier quartier de la capitale) | F | −900/78/−720 | AutoTest (Elouan) |
| **Capitale endormie** : place des cérémonies (dallage, allées de pont royal, estrade et trône, quatre arches à portes royales), palais (grande porte royale, salle du trône, salle du conseil, archives, cuisines et service, appartements sur deux étages, jardin intérieur, deux tours), grand marché, ambassades des régions, tour des archives (chronique du roi disparu), ateliers, résidences ; quinze personnages ; éveil par niveaux (quartiers → palais → couronnement : lanternes rallumées, ponts reformés) | F | plateau −900/86/−900 ; Elouan (acte II), Aldric (acte IV), couronnement | AutoTest (dallage, grande porte fermée puis ouverte au niveau 2, personnages), JUnit |
| **Duels et épreuves** (8) : la danse des racines (Aélis), le pont du Vent-Debout (Kaël, navire volant et escales), la salle des miroirs (Orsane, reflets et points de rupture), les heures suspendues (Sahir), le tournoi des heures (trois épreuves, champion), le roi sans couronne (Vaëron, légions, bannières, forteresse, épée solaire) ; arènes à position fixe ; barre de boss ; phases annoncées ; épreuves amicales sans mort ; reprise libre | F | dialogues des prétendants et de Rachid | GameTest (début, phase avec renfort et racines, victoire, nettoyage), AutoTest (épreuve d'Aélis sur serveur dédié), JUnit (validité et références) |
| **Campagne des actes II à IV** : Le seigneur de Clairval, Le Prétendant, Les routes éteintes, quatre quêtes de reconnaissance, L'expédition de l'archipel, La Couronne (vérité sur le roi, conflit avec Vaëron, duel final), Le couronnement (délégations, ville illuminée, couronne, premier acte royal : fragment du belvédère et sceau du roi) | F | enchaînement automatique après l'acte I et le domaine | JUnit (validité, références, objectifs marqués), GameTest (duel), AutoTest (éveil de la capitale) |
| Arrivée majeure « Le fragment du belvédère » (terrasse du palais d'autrefois, oratoire du sceau, Veilleurs), sites réservés du royaume | F | premier acte royal | JUnit (définition, site, gabarit) |
| Raie céleste : pacte (3 fleurs de rosée), monte, vol, endurance, sifflet, recréation | F | Prairies | GameTest, AutoTest |
| **Cinq montures régionales** : tortue des lagons (nage en surface avec son cavalier), scarabée de sève, gazelle d'opale (rapide), griffon de verre (vol), cerf royal ; pacte par trois appâts régionaux, saut à la barre d'espace, marche d'un bloc, sifflet des montures (rappel, recréation, changement de monture accroupi) | F | biomes des régions | GameTest (pacte, monte, rappel de la tortue), AutoTest (création de chaque espèce) |
| Projets du domaine (10), recrutement de six compagnons, salle des passages, galerie des merveilles | F | Orielle au manoir | GameTest, AutoTest |
| Réseau de relais : 9 relais (Sanctuaire, Clairval, relais royal, manoir, Port-Nacre, Sylvécime, Méridiane, Verréole, Haute-Garde) ; découverte par la porte, le lieu ou le dialogue d'accueil | F | salle des passages | GameTest, AutoTest (portes générées avec leur identifiant) |
| Lieux d'intérêt découverts (30) : journal, carte, notification | F | automatique | GameTest, AutoTest |
| Journal : Quêtes, Pouvoirs, Domaine, Carte, Options | F | touche J | captures (étapes 1–3) |

## Blocs (59 états de bloc) et objets

Prairies : pierre nacrée (brute, taillée, polie, sculptée, escalier, dalle, muret), cristal de nacre, terre des Limbes, herbe argentée, bois d'aube complet, fibres nacrées, herbe de l'aube, fleur de rosée, lanterne nacrée, Porte des Limbes, dalle de rosée, meules, ailes de moulin, vitrine, porte de relais.
Mer : sable de nacre, corail de nacre et corail d'azur (lumineux). Jardins : bûche, planches, feuilles (fruits) et herbe des géants, pétales roses et bleus, sève ascendante (colonne qui soulève). Désert : sable d'opale, grès des heures (brut, taillé), pierre des heures, cadran des heures (quatre quarts du jour, signal redstone), pavé horaire (n'existe qu'à certaines heures). Montagnes : pierre de verre (brute, taillée), verre de montagne, cristal résonnant (note sous les pas, éclats), pont de verre (se forme à l'approche), courant cristallin. Couronne : pierre de couronne (brute, taillée, polie, sculptée), lanterne royale, lanterne royale éteinte, porte royale (niveaux d'éveil), pont royal.

Objets : éclat de nacre, boussole nacrée, fibres, herbe et bouillon de l'aube, fragment de couronne, Épée du Souverain, clé du manoir, sifflet des montures, farine, pain d'Émile, journal de bord, ragoût du domaine, poisson-lanterne, perle de nacre, fruit de géant, miel de rosée, dattes des heures, sablier d'opale, thé des cimes, éclat résonnant, pain de la cour, sceau du roi disparu, 38 œufs d'apparition. Recettes : 37 (artisanat et tailleur de pierre). Pêche dans les Limbes : poissons-lanternes.

## Créatures (44 types, dont 6 montures et 6 boss)

| Région | Créatures | Montures |
|---|---|---|
| Prairies | poisson-lune, cerf-lanterne, Veilleur égaré (coups annoncés), habitants | raie céleste (vol) |
| Mer suspendue | crabe de nacre (perles), méduse céleste (lumineuse), poisson-lanterne (bancs, pêche), gardien des épaves (annoncé) | tortue des lagons (nage) |
| Jardins des Géants | géant des jardins (200 PV, paisible, fait trembler le sol), libellule de rosée, escargot des racines, rôdeur des racines (annoncé) | scarabée de sève |
| Désert des Heures | fennec des dunes, scorpion des heures (venin), lézard-cadran (se dresse à midi), sentinelle de sable (tireur : éclats ralentissants) | gazelle d'opale |
| Montagnes de Verre | bouquetin de verre (marches de 1,5 bloc), papillon de cristal, colosse de cristal (lourd, éclats résonnants), écho résonnant (tireur : onde qui repousse) | griffon de verre (vol) |
| Couronne Immobile | colombe astrale, chat de palais, sentinelle de la Couronne (cuirassée), spectre de la cour (tireur : faiblesse) | cerf royal |

| Duels | boss à phases : Aélis (lance végétale, racines, fleurs de vie, envolées), Kaël (portes dans le dos, escales), Orsane (reflets, armure aux points de rupture), Sahir (salves suspendues, fenêtres de vulnérabilité, doubles), Vaëron (épée solaire, légions protégées par des bannières, forteresse), champion des heures ; renforts : chevalier de bois blanc, reflet de verre, double des heures, légionnaire astral, champion des heures ; soutiens : fleur de vie, point de rupture, bannière astrale | — |

Tous les ennemis annoncent leurs attaques (préparation visible, même signal que le Veilleur) ; les tireurs se préparent puis lancent un éclat qu'on peut éviter. Chaque espèce est une fiche (`LimbesEspeces`) servie par une classe d'archétype ; modèles et textures sont produits par `tools/models/modeles_regions.py`.

## Personnages et dialogues (98 dialogues)

Prairies : Maëlle, Thibault, Gaspard, Orielle, Brann, Vaëron, Lisbeth, Rosalie, Émile, Sabine, Loïc, Hanna, Théo, Marguerite, Ysolde, marchands, habitants. Mer : Kaël (prince des routes), Nérine (marché flottant), capitainerie, pêcheurs, gardien du phare… Jardins : Aélis, Oriane (serre), jardiniers, apicultrice… Désert : Sahir, Ismaël (jardin des heures), archivistes, horloger… Montagnes : Orsane, Gunnar (forge), maître des bains, luthier… Couronne : Aubin (intendant de Haute-Garde), Elouan (quartier des portes), Anselme, boulanger, maître d'armes, maîtresse d'école… Capitale : Aldric (chambellan), Tiphaine (héraut), Maud (archiviste royale), Bertrand (conseiller), Sidonie (intendante), trois ambassadeurs, deux marchands, deux artisans. Épreuves : Rachid (maître des jeux), le capitaine Lio et Mina (expédition). Personnages essentiels (invulnérables, réapparition) : 35 rôles.

## Quêtes (12 principales, 13 secondaires, 75 étapes)

Principales : `le_passage`, `l_etranger` (acte I) ; `le_seigneur_de_clairval` (II) ; `le_pretendant`, `les_routes_eteintes`, `la_danse_des_racines`, `le_pont_du_vent_debout`, `l_expedition_de_l_archipel`, `la_salle_des_miroirs`, `le_tournoi_des_heures` (III) ; `la_couronne`, `le_couronnement` (IV). Secondaires des Prairies : `le_moulin_immobilise`, `la_maison_egaree`, `les_mains_de_sabine`, `loic_et_les_raies`, `le_jardin_de_hanna`, `les_cartes_de_theo`, `la_table_de_marguerite`, `la_barque_echouee`. Régionales (acte III, une par prétendant ou cité) : `la_cargaison_perdue` (Kaël : plonger jusqu'au navire perdu), `la_source_tarie` (Aélis et Oriane : cristal dans le bassin, choix de l'eau partagée ou gardée), `le_jardin_des_heures` (Sahir et Ismaël : sablier d'opale troqué à Nérine), `le_pont_qui_chante` (Orsane : trois éclats résonnants pris sur le pont), `les_murs_de_haute_garde` (Aubin : pains de la cour et lanternes royales).

## Pouvoirs (3)

Épée du Souverain, Pas court, Décret « Reculez » ; majesté, emplacements, fragment visible. (Les 21 techniques suivantes : étape 7.)

## Assets

- 304 textures originales générées (blocs, objets, 40 modèles d'entités peints automatiquement, 78 apparences d'habitants, particules, interface).
- 32 sons originaux synthétisés.
- 62 gabarits de structures (Prairies, 20 sites régionaux, 5 arènes et campement, 7 quartiers de la capitale, fragment du belvédère), 4 particules, 18 biomes, 13 routes.
- Modèles d'entités pilotés par données (`tools/models`) : 40 modèles (os, cubes à UV automatiques, animations, peintures).
- Musique : pistes vanilla en attendant des compositions originales.

## Compteurs par rapport aux objectifs de la version complète

| Objectif complet | Réalisé |
|---|---|
| 6 régions × 3 sous-biomes | **6 régions, 18 sous-biomes** |
| 6 centres habités + cité de Vaëron | **6 : Clairval, Port-Nacre, Sylvécime, Méridiane, Verréole + Haute-Garde (cité de Vaëron)** ; quartier des portes de la capitale |
| 40 lieux d'intérêt | **40 déclarés** (découverte, carte) + 10 annexes du manoir |
| 5 prétendants | **5 complets** : rencontres, aventure, reconnaissance (négociation, expédition, duel rituel, tournoi), duel à phases, conclusion ; duel final de Vaëron |
| 30 quêtes principales / 40 secondaires | 12 / 13 (actes I à IV complets ; acte V à l'étape 7) |
| 12 types d'Arrivées | 4 (dont une majeure : le fragment du belvédère) |
| 24 créatures / 5 montures | **44 / 6** (dont 6 boss et 8 renforts/soutiens de duel) |
| 24 techniques royales | 3 |
| 10 épreuves majeures | **9** : relais royal, danse des racines, pont du Vent-Debout, salle des miroirs, heures suspendues, tournoi (3 épreuves), duel final de Vaëron |
