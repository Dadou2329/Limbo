# Backlog persistant

Ordre conseillé par le cahier des charges : 1 socle → 2 Prairies/Clairval/manoir/première quête/premiers pouvoirs → 3 tranche de qualité (mini-aventure, monture, Arrivée, combat) → 4 domaine/journal/passages → 5 régions restantes → 6 prétendants/campagne/couronnement → 7 pouvoirs supérieurs/règne → 8 complétion/optimisation.

État : étapes 1 à 7 réalisées (tranche « Le Passage » ; monture, Arrivées, mini-aventures ; domaine ; six régions ; prétendants, campagne des actes II à IV, duels, capitale, couronnement et premier acte royal ; trente-trois techniques royales en sept branches et le règne de l'acte V) ; étape 8 (complétion, optimisation, validation finale) à suivre.

## Prochaines actions exécutables (par priorité)

### A. Vérification en jeu de la tranche 1 (bloquant avant d'empiler)
- [x] Rendu vérifié par captures automatiques (`./gradlew runCapture`, client sous Xvfb) : blocs et arbres d'aube, Sanctuaire, Clairval, manoir, relais, Veilleur, cerf-lanterne, HUD, journal (deux onglets), dialogue de Maëlle, traversée de l'arche, ciel nacré. Corrections faites à cette occasion : avertissement « réglages expérimentaux » (mixins), ciel qui ne se dessinait pas, texte du journal flouté, barre de pouvoirs sur les cœurs, feuilles illisibles.
- [ ] Jouer « Le Passage » et « L'Étranger » de bout en bout avec un vrai joueur (clavier/souris) : enchaînement des dialogues, objectifs, récompenses ; corriger ce qui ne se comporte pas comme prévu.
- [ ] Vérifier les animations en mouvement (cerf, poisson, Veilleur) et les échelles ; ajuster les pivots. Vérifier le poisson-lune de près (non cadré dans les captures).
- [ ] Écouter les sons synthétisés en jeu (aucun périphérique audio dans l'environnement de développement) ; ajuster volumes et hauteurs.
- [ ] Jouer le combat du relais : équilibrer santé/dégâts des Veilleurs, fenêtre d'esquive.
- [ ] Serveur dédié avec deux joueurs : synchronisation, dialogues simultanés, réapparition des PNJ.

### B. Étape 3 (tranche de qualité) — réalisée
- [x] Monture : raie céleste (pacte par nourriture, vol au regard, endurance, rappel, recréation, chute lente).
- [x] Système d'Arrivées (types et sites en données, vérification de zone, limite simultanée, annonce, effet de ciel, expiration, archivage) ; « La maison égarée » et « La caisse tombée » ; observatoire de Lisbeth.
- [x] Mini-aventures : « Le moulin immobilisé » et le Lac du Crépuscule (dalles de rosée horaires).
- [x] Fragment de couronne visible par tous (attachement synchronisé `etat_public`).
- [x] Modèles d'entités pilotés par données (JSON + textures peintes par cube) pour toutes les créatures à venir.
- [ ] Musiques originales (ou libres documentées) à la place des pistes vanilla (étape 8).

### C. Domaine (étape 4) — réalisée
- [x] Dix projets (aile ouest, forge royale, écuries, serres, maison des explorateurs, galerie, salle des passages, chambres, jardin, marché) : aperçu d'emprise, détection des constructions du joueur, construction autour des blocs occupés, inventaires rendus.
- [x] Recrutement par rencontres : Sabine, Loïc, Hanna, Théo, Marguerite, Ysolde (+ Émile) ; installation dans l'annexe bâtie ; affûtage de l'épée.
- [x] Salle des passages : portes de relais, découverte en voyage, voyage vérifié.
- [x] Journal : onglets Domaine, Carte, Options ; indications discrètes ; indices de tutoriel.
- [ ] Rendu des nouveaux éléments (vitrine, porte de relais, écrans de chantier et de relais, carte) à vérifier en captures client (prévu à la fin de l'étape suivante).
- [ ] Scènes de vie des compagnons (horaires, gestes) et services des marchands (échanges) : étape 8.
- [ ] Multijoueur : le domaine est partagé par le monde (un manoir commun) ; domaines par groupe et permissions : étape 8.

### D. Régions (étape 5) — réalisée
- [x] Source de biomes régionale (Voronoï pondéré, transitions), relief par région, 18 biomes, matériaux et blocs scénarisés (sève ascendante, cadran des heures, pavé horaire, pont de verre, cristal résonnant), Mer suspendue à volume d'eau stable, routes pavées sur le relief.
- [x] Villes : Port-Nacre, Sylvécime, Méridiane, Verréole, Haute-Garde ; quinze lieux forts (phare, marché flottant, navire perdu, sanctuaire des bancs, quai, serre, pépinière, arbre creux, clairière, observatoire des heures, jardin des heures, bains, atelier, pont qui chante, quartier des portes) ; 63 personnages ; 9 relais.
- [x] Cinq prétendants introduits avec une aventure chacun ; 30 créatures dont 6 montures (pacte, saut, nage, vol, sifflet des montures).
- [ ] Vérifier en captures client le rendu des 26 modèles (proportions, animations de marche et d'annonce), des villes et des biomes (prévu en fin de projet).
- [ ] Capitale et palais sur le plateau réservé (−900/86/−900) : étape 6.
- [ ] Mécanismes temporels de Méridiane à étendre (quartier figé qui s'anime selon l'heure) et plongée outillée dans la Mer (respiration, lumière) : étape 8.

### E. Prétendants, campagne, couronnement (étape 6) — réalisée
- [x] Système de duels en données (arène, boss, renforts, phases à seuils, actions de victoire et d'échec, barre de boss, blocs temporaires toujours retirés, échec sans mort en épreuve amicale, reprise libre) et boss à phases pour les cinq prétendants et le champion des heures.
- [x] Reconnaissances différentes : Aélis (négociation si l'eau a été partagée, sinon danse des racines), Kaël (expédition de l'archipel ou pont du Vent-Debout), Orsane (duel rituel de la salle des miroirs), Sahir (tournoi des heures en trois épreuves) ; tous les duels restent jouables ensuite en épreuves amicales.
- [x] Campagne : actes II (Le seigneur de Clairval), III (Le Prétendant, Les routes éteintes), IV (La Couronne : palais ouvert, vérité sur le roi, conflit puis duel final contre Vaëron à trois phases, conclusion où il garde Haute-Garde), couronnement jouable (délégations réellement aidées, ville illuminée, ponts reformés, couronne assemblée, contrôle rendu aussitôt) et premier acte royal (fragment du belvédère attiré, sceau du roi).
- [x] Capitale endormie : place des cérémonies, palais (trône, conseil, archives, cuisines, appartements, jardin, tours), marché, ambassades, archives, ateliers, résidences ; éveil par niveaux ; quinze personnages.
- [ ] Vérifier en captures client le rendu des boss, des arènes, de la capitale et de la cérémonie (fin de projet).
- [ ] Scènes de vie des délégations et dialogues de félicitations plus nombreux (étape 8).

### F. Pouvoirs et règne (étape 7) — réalisée
- [x] Arsenal : parade royale, lance (allonge) et charge, arc sans flèches (éclats de lumière, tension), marque royale, bouclier (rempart), changement d'arme en combo, armes satellites.
- [x] Portes : porte directionnelle (franchit murs et dénivelés), attaque d'angle (dans le dos, arme l'arc de l'épée), sauvetage d'un allié, liaison royale persistante (accroupi : fixer ; debout : revenir).
- [x] Décrets : « Déposez les armes » (cibles perdues, coups interrompus, éclats dissipés), « Tenez bon », « Que le chemin s'ouvre » (pont de lumière à disparition planifiée), « Restez », « Élevez-vous » ; sons et effets propres.
- [x] Garde astrale (chevalier, archer, protecteur ; budget de trois, une minute, ordres suivre / tenir / attaquer, aucun tir ami), Main des Limbes (saisir, passage, main couronnée qui brise les blocs d'arène), Pactes royaux (appel de monture, pacte de combat).
- [x] Emblématiques : Jugement des constellations (sept étoiles), Marche du Palais (Portes libres), Audience du Roi (tout ce qui menace traîné devant le souverain), Avènement (trente secondes : couronne et manteau visibles de tous, majesté quadruplée, recharges divisées par deux, lames satellites ; état public niveau 3).
- [x] Quatre emplacements (R, V, G, B), sélection radiale (X) par branches, onglet Pouvoirs du journal par branche avec défilement ; déblocages progressifs par la campagne (forge de Sabine, reconnaissances, actes, couronnement, sceau, frontières).
- [x] Acte V : la cité arrivée (Brumeval, Arrivée majeure avec maire et habitants, porte de relais), le grand tournoi (cinq épreuves), le navire royal (chantier naval, structure posée par dialogue, porte de relais à bord, île des ruines et baleine des hauteurs), les frontières (lisière des brumes, l'ancien roi, vagues, choix) ; Lisbeth prépare une Arrivée.
- [ ] Vérifier en captures client le rendu des armes royales, de la garde, des effets des décrets et de l'Avènement (couronne, manteau), de la sélection radiale (fin de projet).
- [ ] Tutoriel progressif plus guidé (une indication par nouvelle technique) : étape 8.

### G. Technique
- [ ] Budgets explicites (PNJ actifs, invocations, pathfinding) et profils graphiques.
- [ ] Sauvegarde versionnée des structures de données avec migrations (version 1 posée).
- [ ] Option de destruction de terrain réversible pour les zones sauvages.
- [ ] Datagen Fabric pour remplacer une partie du script Python (facultatif).

## Dette connue / limites honnêtes
- Rendu client et interface : observés uniquement par captures fixes (Xvfb, rendu logiciel) ; les animations, les particules en mouvement et les sons n'ont pas été jugés à l'œil ni à l'oreille.
- Le vestige Overworld est localisé par l'auto-test sur serveur dédié (structure réellement générée), mais son intégration au relief n'a pas été regardée.
- Textures d'entités : régénérées à l'identique désormais (les générateurs utilisaient `hash()` de Python, aléatoire d'une exécution à l'autre ; remplacé par crc32).
- Les chemins hors plateaux (vers le manoir et le relais) ne sont pas encore tracés sur le relief ; les marqueurs de quête guident.
- Textures procédurales : cohérentes mais perfectibles (lisibilité de la pierre sculptée, feuilles d'aube claires).
