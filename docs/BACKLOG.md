# Backlog persistant

Ordre conseillé par le cahier des charges : 1 socle → 2 Prairies/Clairval/manoir/première quête/premiers pouvoirs → 3 tranche de qualité (mini-aventure, monture, Arrivée, combat) → 4 domaine/journal/passages → 5 régions restantes → 6 prétendants/campagne/couronnement → 7 pouvoirs supérieurs/règne → 8 complétion/optimisation.

État : étapes 1, 2 et 3 réalisées (tranche « Le Passage », puis monture, Arrivées, mini-aventures et systèmes réutilisables) ; étape 4 (domaine) en cours.

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

### C. Domaine (étape 4)
- [ ] Projets de restauration du manoir : aile ouest, serre, puits, forge royale ; aperçu d'emprise, détection de conflits avec les constructions du joueur, préservation des inventaires.
- [ ] Recrutement : forgeronne, éleveur, jardinière, cartographe, cuisinière, capitaine (rencontres réelles ; Gaspard boulanger comme premier recrutement).
- [ ] Salle des passages : réseau de portes entre relais découverts (branche Portes).
- [ ] Journal : carte, mode indications discrètes, suivi d'une quête à la fois (déjà) + archives.

### D. Régions (étape 5)
- [ ] Mer suspendue (volumes d'eau stables, Port-Nacre, navigation, plongée), Jardins des Géants (Sylvécime, verticalité), Désert des Heures (Méridiane, mécanismes temporels locaux), Montagnes de Verre (Verréole, forges), Couronne Immobile (quartiers éveillables, cité de Vaëron).
- [ ] Par région : matériau, cuisine, créatures, déplacement, personnages, aventures, récompenses ; transitions naturelles.
- [ ] Routes procédurales suivant le relief entre les sites (actuellement : chemins sur les plateaux seulement).

### E. Prétendants, campagne, couronnement (étape 6)
- [ ] Aélis, Kaël, Orsane, Sahir : rencontres, aventures communes, duels (arènes maîtrisées), reconnaissances alternatives (tournoi, négociation, expédition, duel rituel).
- [ ] Vaëron : cité fortifiée visitable, collaboration puis rivalité, duel final à plusieurs phases, conclusion.
- [ ] Actes II–IV, explication de la disparition du roi, couronnement jouable, premier acte royal (fragment attiré, nouveau territoire).

### F. Pouvoirs (étape 7)
- [ ] Arsenal : lance, arc, bouclier, parade, changement d'arme en combo, armes satellites.
- [ ] Portes : porte directionnelle, attaque d'angle, sauvetage d'allié, liaisons persistantes.
- [ ] Décrets : « Déposez les armes », « Tenez bon », « Que le chemin s'ouvre », « Restez », « Élevez-vous » (symboles, animations, sons propres).
- [ ] Garde astrale, Main des Limbes, Pactes royaux.
- [ ] Techniques emblématiques : Jugement des constellations, Marche du Palais, Audience du Roi, Avènement.
- [ ] Sélection radiale facultative, tutoriel progressif, options (densité d'effets, secousses, flashes).

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
