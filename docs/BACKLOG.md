# Backlog persistant

Ordre conseillé par le cahier des charges : 1 socle → 2 Prairies/Clairval/manoir/première quête/premiers pouvoirs → 3 tranche de qualité (mini-aventure, monture, Arrivée, combat) → 4 domaine/journal/passages → 5 régions restantes → 6 prétendants/campagne/couronnement → 7 pouvoirs supérieurs/règne → 8 complétion/optimisation.

État : les huit étapes sont réalisées (version 1.0.0). Ce qui reste relève de la vérification « en main » par de vrais joueurs et de l'enrichissement au-delà du cahier des charges.

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
- [x] Musiques originales synthétisées (six pièces régionales) à la place des pistes vanilla.

### C. Domaine (étape 4) — réalisée
- [x] Dix projets (aile ouest, forge royale, écuries, serres, maison des explorateurs, galerie, salle des passages, chambres, jardin, marché) : aperçu d'emprise, détection des constructions du joueur, construction autour des blocs occupés, inventaires rendus.
- [x] Recrutement par rencontres : Sabine, Loïc, Hanna, Théo, Marguerite, Ysolde (+ Émile) ; installation dans l'annexe bâtie ; affûtage de l'épée.
- [x] Salle des passages : portes de relais, découverte en voyage, voyage vérifié.
- [x] Journal : onglets Domaine, Carte, Options ; indications discrètes ; indices de tutoriel.
- [x] Rendu des nouveaux éléments vérifié par la campagne de captures de fin de projet (voir `docs/captures/`).
- [ ] Scènes de vie des compagnons (horaires, gestes) et services des marchands (échanges) : au-delà du cahier des charges.
- [ ] Multijoueur : le domaine est partagé par le monde (un manoir commun) ; domaines par groupe et permissions : au-delà du cahier des charges.

### D. Régions (étape 5) — réalisée
- [x] Source de biomes régionale (Voronoï pondéré, transitions), relief par région, 18 biomes, matériaux et blocs scénarisés (sève ascendante, cadran des heures, pavé horaire, pont de verre, cristal résonnant), Mer suspendue à volume d'eau stable, routes pavées sur le relief.
- [x] Villes : Port-Nacre, Sylvécime, Méridiane, Verréole, Haute-Garde ; quinze lieux forts (phare, marché flottant, navire perdu, sanctuaire des bancs, quai, serre, pépinière, arbre creux, clairière, observatoire des heures, jardin des heures, bains, atelier, pont qui chante, quartier des portes) ; 63 personnages ; 9 relais.
- [x] Cinq prétendants introduits avec une aventure chacun ; 30 créatures dont 6 montures (pacte, saut, nage, vol, sifflet des montures).
- [x] Villes et biomes des régions capturés en fin de projet ; animations non jugées à l'œil (images fixes).
- [ ] Capitale et palais sur le plateau réservé (−900/86/−900) : étape 6.
- [ ] Mécanismes temporels de Méridiane à étendre (quartier figé qui s'anime selon l'heure) et plongée outillée dans la Mer (respiration, lumière) : au-delà du cahier des charges.

### E. Prétendants, campagne, couronnement (étape 6) — réalisée
- [x] Système de duels en données (arène, boss, renforts, phases à seuils, actions de victoire et d'échec, barre de boss, blocs temporaires toujours retirés, échec sans mort en épreuve amicale, reprise libre) et boss à phases pour les cinq prétendants et le champion des heures.
- [x] Reconnaissances différentes : Aélis (négociation si l'eau a été partagée, sinon danse des racines), Kaël (expédition de l'archipel ou pont du Vent-Debout), Orsane (duel rituel de la salle des miroirs), Sahir (tournoi des heures en trois épreuves) ; tous les duels restent jouables ensuite en épreuves amicales.
- [x] Campagne : actes II (Le seigneur de Clairval), III (Le Prétendant, Les routes éteintes), IV (La Couronne : palais ouvert, vérité sur le roi, conflit puis duel final contre Vaëron à trois phases, conclusion où il garde Haute-Garde), couronnement jouable (délégations réellement aidées, ville illuminée, ponts reformés, couronne assemblée, contrôle rendu aussitôt) et premier acte royal (fragment du belvédère attiré, sceau du roi).
- [x] Capitale endormie : place des cérémonies, palais (trône, conseil, archives, cuisines, appartements, jardin, tours), marché, ambassades, archives, ateliers, résidences ; éveil par niveaux ; quinze personnages.
- [x] Boss, arènes et capitale capturés en fin de projet.
- [ ] Scènes de vie des délégations et dialogues de félicitations plus nombreux : au-delà du cahier des charges.

### F. Pouvoirs et règne (étape 7) — réalisée
- [x] Arsenal : parade royale, lance (allonge) et charge, arc sans flèches (éclats de lumière, tension), marque royale, bouclier (rempart), changement d'arme en combo, armes satellites.
- [x] Portes : porte directionnelle (franchit murs et dénivelés), attaque d'angle (dans le dos, arme l'arc de l'épée), sauvetage d'un allié, liaison royale persistante (accroupi : fixer ; debout : revenir).
- [x] Décrets : « Déposez les armes » (cibles perdues, coups interrompus, éclats dissipés), « Tenez bon », « Que le chemin s'ouvre » (pont de lumière à disparition planifiée), « Restez », « Élevez-vous » ; sons et effets propres.
- [x] Garde astrale (chevalier, archer, protecteur ; budget de trois, une minute, ordres suivre / tenir / attaquer, aucun tir ami), Main des Limbes (saisir, passage, main couronnée qui brise les blocs d'arène), Pactes royaux (appel de monture, pacte de combat).
- [x] Emblématiques : Jugement des constellations (sept étoiles), Marche du Palais (Portes libres), Audience du Roi (tout ce qui menace traîné devant le souverain), Avènement (trente secondes : couronne et manteau visibles de tous, majesté quadruplée, recharges divisées par deux, lames satellites ; état public niveau 3).
- [x] Quatre emplacements (R, V, G, B), sélection radiale (X) par branches, onglet Pouvoirs du journal par branche avec défilement ; déblocages progressifs par la campagne (forge de Sabine, reconnaissances, actes, couronnement, sceau, frontières).
- [x] Acte V : la cité arrivée (Brumeval, Arrivée majeure avec maire et habitants, porte de relais), le grand tournoi (cinq épreuves), le navire royal (chantier naval, structure posée par dialogue, porte de relais à bord, île des ruines et baleine des hauteurs), les frontières (lisière des brumes, l'ancien roi, vagues, choix) ; Lisbeth prépare une Arrivée.
- [x] Garde astrale, pont de lumière, sélection radiale et Avènement capturés en fin de projet.
- [ ] Tutoriel progressif plus guidé (une indication par nouvelle technique) : au-delà du message de déblocage et du journal.

### H. Complétion (étape 8) — réalisée
- [x] Quarante quêtes secondaires (vingt-sept nouvelles, une par habitant notable des régions et de la capitale), générées depuis une spécification lisible (`tools/datagen/gen_quetes_secondaires.py`).
- [x] Treize types d'Arrivées sur onze sites réservés dans les six régions (huit nouvelles : charrette, puits, kiosque, radeau et Siméon, serre, tente, cloche, bibliothèque).
- [x] Blocs de décor (tapis, vase, guirlande) avec recettes.
- [x] Six musiques originales par région (synthèse), en flux.
- [x] Budgets serveur configurables (`config/les_limbes.json`) et profil graphique client.
- [x] Validation finale : JUnit, GameTests, AutoTest, campagne de captures client (34 images).

### G. Technique
- [x] Budgets explicites (Arrivées simultanées, gardes et durée, éclats par monde, rayon de surveillance) et profil graphique (bas, normal, élevé).
- [ ] Sauvegarde versionnée des structures de données avec migrations (version 1 posée).
- [ ] Option de destruction de terrain réversible pour les zones sauvages.
- [ ] Datagen Fabric pour remplacer une partie du script Python (facultatif).

## Dette connue / limites honnêtes
- Rendu client et interface : observés uniquement par captures fixes (Xvfb, rendu logiciel) ; les animations, les particules en mouvement, les sons et les musiques n'ont pas été jugés à l'œil ni à l'oreille.
- Musiques : synthèse procédurale (nappes, cloches, basse) ; des compositions humaines les remplaceraient avantageusement, le branchement par région est en place.
- Le navire royal ne navigue pas : il est bâti au chantier et relie l'archipel par sa porte de relais.
- Le vestige Overworld est localisé par l'auto-test sur serveur dédié (structure réellement générée), mais son intégration au relief n'a pas été regardée.
- Textures d'entités : régénérées à l'identique désormais (les générateurs utilisaient `hash()` de Python, aléatoire d'une exécution à l'autre ; remplacé par crc32).
- Les chemins hors plateaux (vers le manoir et le relais) ne sont pas encore tracés sur le relief ; les marqueurs de quête guident.
- Textures procédurales : cohérentes mais perfectibles (lisibilité de la pierre sculptée, feuilles d'aube claires).
