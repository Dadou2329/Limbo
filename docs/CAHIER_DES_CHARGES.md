LES LIMBES — MASTER PROMPT POUR CLAUDE OPUS 5.5
Dimension d’aventure complète pour Minecraft 1.21.1 — Fabric
Exploration merveilleuse • Prétendants au trône • Grande magie royale • Royaume vivant

Tu es à la fois directeur créatif, game designer, scénariste, développeur Minecraft Fabric, concepteur de génération de monde, artiste technique et animateur. Je veux que tu développes réellement un mod ambitieux appelé « Les Limbes ». Ce document constitue le cahier des charges de création et de réalisation. Ne réponds pas uniquement par des idées ou un plan : construis le projet, ses systèmes, son contenu, ses ressources et ses outils de validation dans l’environnement dont tu disposes.

Le projet est vaste : organise sa réalisation en étapes cohérentes et jouables, conserve un suivi persistant et poursuis le développement sans réduire silencieusement l’ambition. Une première tranche jouable constitue une étape de production, jamais un substitut à la dimension complète. Ne prétends pas qu’une fonctionnalité existe si tu n’en as créé que le nom, une commande ou un écran.

Tout le contenu destiné au joueur doit être en français : interfaces, dialogues, quêtes, descriptions, tutoriels et messages. Les dialogues utilisent des phrases simples, naturelles et complètes. Les personnages parlent comme des personnes compréhensibles, sans proverbes artificiels ni énigmes dans chaque réplique.

======================================================================
1. LA PROMESSE DU JEU
======================================================================

Le joueur découvre une immense dimension mystérieuse et magnifique où arrivent des lieux, objets, créatures et peuples disparus d’autres mondes. Il s’y construit un foyer, voyage entre des royaumes, développe une grande magie royale et affronte cinq prétendants au trône. Il finit par devenir roi des Limbes. Son couronnement ouvre une véritable période de règne, avec de nouveaux territoires, de nouvelles aventures et des pouvoirs supérieurs.

Les trois envies principales doivent se nourrir mutuellement :
— Explorer pour découvrir des merveilles, des histoires, des ressources et des personnes.
— Gagner en puissance pour atteindre de nouveaux lieux et affronter les prétendants.
— Ramener ses découvertes chez soi pour faire grandir son domaine, puis son royaume.

IMPORTANT : les Limbes ne sont PAS une dimension d’horreur. Aucune direction artistique fondée sur des jumpscares, la torture, des cadavres, des silhouettes qui poursuivent le joueur ou un désespoir permanent. Le mystère doit susciter la curiosité et l’émerveillement. Il existe des dangers, des ennemis, des conflits et des combats grandioses, mais aussi des villages accueillants, de l’humour, des lieux paisibles et des personnages attachants. Le joueur doit avoir envie d’y vivre.

L’ambition est celle d’une grande extension d’aventure : beaucoup de contenu substantiel, des mécaniques différentes, des régions mémorables et des transformations visibles. Évite les dizaines d’objets presque identiques et les biomes qui ne changent que la couleur de l’herbe.

======================================================================
2. SOCLE TECHNIQUE ET LIVRABLES
======================================================================

Cible : Minecraft Java 1.21.1, Fabric. Ne change pas de version ou de loader sans raison bloquante clairement expliquée. Vérifie les versions réellement compatibles du JDK, du loader, de Fabric API, des mappings, de Loom et de toute bibliothèque retenue dans leurs sources officielles. Ne devine pas des signatures d’API. Évite l’accumulation de dépendances ; documente celles qui sont réellement nécessaires. Les shaders externes doivent rester facultatifs.

Fournis :
— Le projet source complet et organisé, les fichiers Gradle et les ressources.
— Un JAR compilé lorsque l’environnement permet réellement de le produire.
— Les assets originaux, modèles, animations, textures, effets et sons nécessaires ; leurs sources lorsque disponibles.
— Un guide d’installation, un guide de démarrage sans spoiler et une documentation de développement.
— Un catalogue du contenu réellement implémenté, ses conditions d’accès et ses tests.
— Un état d’avancement honnête distinguant fonctionnel, partiel et restant à réaliser.

La dimension doit être accessible depuis une nouvelle sauvegarde de survie normale, sans téléchargement obligatoire d’une carte externe et sans commande administrateur. Les villes et lieux scénarisés peuvent utiliser des structures originales construites sur mesure, intégrées à une génération procédurale contrôlée. Prévois des outils internes pour construire, inspecter et tester ces structures.

Le solo doit être complet. L’architecture doit prendre en compte le multijoueur et le serveur dédié dès le départ : aucune classe cliente chargée sur le serveur, aucune décision de progression validée uniquement côté client.

======================================================================
3. IDENTITÉ DU MONDE ET ENTRÉE DANS LES LIMBES
======================================================================

Tout ce qui disparaît ailleurs peut finir ici. Un navire perdu rejoint une mer suspendue. Une bibliothèque oubliée réapparaît dans une montagne. Une civilisation disparue a poursuivi son existence dans les Limbes. Beaucoup d’habitants sont nés ici et n’ont jamais connu d’autre monde.

Ce principe doit produire une culture propre aux Limbes : des habitants ont adapté, réparé et assemblé ces héritages au fil des générations. Chaque région doit posséder une cohérence architecturale et sociale, pas ressembler à un assemblage aléatoire de bâtiments.

L’ancien roi a disparu depuis longtemps. Les régions vivent encore, mais les grandes routes royales se sont éteintes et la capitale est partiellement endormie. Le trône peut reconnaître un nouveau souverain. Cinq personnalités influentes cherchent à l’obtenir, chacune pour une raison compréhensible.

Crée une entrée accessible relativement tôt dans une partie : découverte d’un petit vestige dans l’Overworld, courte enquête guidée, restauration d’un passage. Aucun objet de fin de jeu vanilla obligatoire pour commencer. Le joueur doit comprendre comment entrer puis revenir. Le premier passage conduit à une zone d’accueil sûre avec un moyen de retour fiable.

Première arrivée : prairie argentée sous un ciel nacré, poissons lumineux dans l’air, pont monumental reliant des îles et silhouette du palais au loin. Une personne accueille le joueur et lui propose une première aide concrète. Le spectacle doit rester jouable et contrôlable, sans longue cinématique imposée.

Le palais royal doit être perceptible dès les premières heures. Utilise si nécessaire une représentation céleste ou distante cohérente avec sa position réelle, sans charger continuellement des milliers de chunks. Explique visuellement pourquoi on ne peut pas encore accéder à son cœur. Empêche le contournement de la progression par une simple tour de blocs ou un vol, tout en autorisant la découverte extérieure et sans murs invisibles arbitraires.

======================================================================
4. GÉOGRAPHIE : SIX GRANDES RÉGIONS
======================================================================

Crée un monde assez vaste pour plusieurs dizaines d’heures de découvertes. Mélange terrain explorable, sous-biomes, structures, routes et lieux uniques. Réserve des positions persistantes aux grandes villes et étapes critiques. Les routes de progression et les moyens de transport doivent rester accessibles quelle que soit la seed.

A. LES PRAIRIES DE L’AUBE
Herbes argentées, eaux claires, moulins flottants, bosquets lumineux, vallons et grands animaux paisibles. Région d’accueil, avec Clairval, le manoir du joueur et de nombreux secrets accessibles.
Sous-biomes : plaines nacrées, vergers suspendus, vallées des lanternes.
Contenu : ancien relais royal à restaurer, course de montures, moulin immobilisé dans l’air, ruines sous une colline, caravane à accompagner, lac à passages révélés au crépuscule.
Ressources : fibres nacrées, bois d’aube, pierre claire et herbes de cuisine.

B. LA MER SUSPENDUE
De véritables volumes d’eau retenus dans le ciel, avec des îles, des falaises, des ports et des voies navigables. Prévois une solution technique stable aux limites d’eau pour éviter des cascades et mises à jour de fluides incontrôlées. Le joueur doit pouvoir naviguer et plonger réellement.
Ville : Port-Nacre. Activités : bateau personnalisable, plongée, épaves, cargaisons, pêche spéciale et expéditions sur les routes de Kaël.
Sous-biomes : récifs célestes, lagons inversés, archipel des épaves.
Lieux forts : phare donnant sur le dessous de l’océan, marché flottant, navire perdu monumental, sanctuaire traversé par des bancs lumineux.

C. LES JARDINS DES GÉANTS
Arbres immenses, racines formant des routes, fleurs monumentales, cultures et maisons perchées. Ville : Sylvécime, liée à Aélis.
Sous-biomes : canopée habitée, jardins de rosée, racines profondes.
Gameplay : exploration verticale, élevage, cultures extraordinaires, ascenseurs végétaux, restauration de sources et parcours dans la canopée.
Lieux forts : serre ancienne, pépinière royale, arbre creux avec plusieurs étages aménagés et clairière accueillant un géant paisible.

D. LE DÉSERT DES HEURES
Dunes claires et cités dont certains quartiers conservent des époques différentes. Ville : Méridiane, liée à Sahir. La manipulation du temps est locale et explicite : ne tente pas de rembobiner arbitrairement toute la sauvegarde.
Sous-biomes : dunes d’opale, oasis des cadrans, terrasses anciennes.
Gameplay : alterner l’état de mécanismes scénarisés, retrouver des ateliers, explorer des routes apparaissant à certaines heures, résoudre des énigmes lisibles et participer au marché des objets arrivés d’ailleurs.
Lieux forts : observatoire, quartier figé pendant une fête, jardin redevenant fertile et caravansérail construit autour d’une horloge immense.

E. LES MONTAGNES DE VERRE
Sommets translucides, galeries minérales, forges, monastères et voies suspendues. Ville : Verréole, liée à Orsane.
Sous-biomes : aiguilles de verre, vallées résonnantes, cavernes de cristal.
Gameplay : techniques de combat, artisanat royal, prospection, remontées de cristal et expéditions d’altitude.
Lieux forts : grande forge, bains alimentés par des sources chaudes, palais de miroirs, atelier d’instruments et pont dont les éléments se reforment au passage.

F. LA COURONNE IMMOBILE
Palais central, capitale endormie et anneaux de paysages suspendus. On en réveille les accès puis les quartiers à mesure que la campagne progresse.
Quartiers : portes et jardins, grand marché, ambassades, ateliers, archives, résidences, place des cérémonies et palais.
La capitale doit avoir des rues secondaires, cours, terrasses et intérieurs visitables. Le palais doit rester monumental tout en proposant des espaces compréhensibles et utiles.
Vaëron possède avant le couronnement sa propre cité fortifiée, distincte du palais central, située sur un territoire extérieur de cette région. Le joueur peut la visiter tôt dans l’histoire.

Pour CHAQUE région, développe un matériau utile, une cuisine, plusieurs créatures, un mode de déplacement ou d’interaction, des personnages, des aventures et des récompenses propres. Prévois aussi des transitions naturelles entre les régions.

======================================================================
5. LE DOMAINE DU JOUEUR ET LE ROYAUME
======================================================================

Le joueur reçoit tôt un manoir vieillissant près de Clairval. Il s’agit d’un vrai lieu qu’il peut explorer et habiter immédiatement, avec chambre, cuisine, réserve, bibliothèque, jardin et espaces à restaurer. Pas un cube vide ni un terrain sans rien.

Étapes visibles : manoir et village affaibli ; domaine restauré ; bourg animé ; résidence de prétendant ; domaine royal relié à la capitale. Le palais central s’ajoute ensuite au domaine, qui conserve sa valeur sentimentale et ses activités.

La gestion doit être simple : choisir un projet, réunir des ressources ou terminer une aventure, valider puis voir une transformation concrète. Aucune obligation de surveiller sans cesse faim, salaires ou bonheur de dizaines d’habitants. Les artisans travaillent sans transformer le jeu en tableur.

Installations obligatoires :
— Forge royale : amélioration et personnalisation des armes invoquées.
— Écuries et volières : hébergement, équipement et entraînement des montures.
— Serres : plantes régionales, cuisine et alchimie.
— Maison des explorateurs : cartes, rumeurs, compagnons et préparatifs d’expédition.
— Galerie des merveilles : objets exposables avec histoire et provenance.
— Salle des passages : réseau de portes débloquées en voyage.
— Chambres de compagnons : pièces personnalisées et scènes de vie.
— Auberge, marché et ateliers : marchands et services utiles.
— Jardins et espaces de détente : fontaines, bains, belvédères et petits événements.

Le recrutement passe par de vraies rencontres. Prévois notamment une forgeronne, un éleveur, une jardinière, un cartographe, une cuisinière et un capitaine d’expédition, avec noms, caractères, petites histoires et plusieurs interactions après recrutement.

Respecte les constructions du joueur. Les améliorations ne doivent jamais écraser silencieusement ses blocs ou coffres. Délimite les emprises, montre un aperçu, détecte les conflits et propose de déplacer le projet ou d’ignorer la zone occupée. Préserve les inventaires. Le joueur peut décorer librement les espaces prévus à cet effet.

En multijoueur : le monde possède un état partagé pour la capitale et le couronnement ; chaque joueur conserve sa progression magique et son domaine ou rejoint volontairement celui d’un groupe. Prévois permissions et rôles. Tous les participants doivent pouvoir accomplir leur parcours et accéder aux récompenses essentielles après un couronnement collectif. Aucun premier joueur ne doit verrouiller le contenu pour les suivants.

======================================================================
6. LA GRANDE MAGIE ROYALE
======================================================================

Le joueur possède une identité magique forte, pas une sélection de classes élémentaires sans rapport. Palette : or pâle, bleu profond, verre lumineux et constellations. Les premiers sorts révèlent un fragment de couronne flottant ; la maîtrise complète assemble une véritable couronne et un manteau céleste.

Les manifestations majeures évoquent l’architecture royale : portes, escaliers, colonnes, armes monumentales, bannières et salle du trône. Les animations du corps, les sons et les effets doivent rendre chaque action identifiable.

Six branches, toutes accessibles dans une même sauvegarde :

1) L’ARSENAL DU SOUVERAIN
Épée polyvalente avec enchaînements et parade ; lance pour charge, percée et rappel ; arc pour tirs chargés et marquage ; bouclier pour interception et contre-attaque. Chaque arme a son modèle, sa gestuelle et ses usages.
Progression : invocation simple, techniques avancées, changement d’arme en combo, armes satellites puis arsenal collectif pendant l’Avènement. Les armes satellites utilisent un nombre borné de cibles et de projectiles.

2) LES PORTES DU PALAIS
Pas court, porte directionnelle, attaque venant d’un angle marqué, sauvetage d’un allié et liaisons persistantes entre relais découverts. Une porte doit vérifier sa destination, la collision et l’accès aux chunks ; aucun envoi dans un bloc ou au-dessus du vide. Les passages interdimensionnels respectent les règles de progression et permissions.

3) LES DÉCRETS ROYAUX
« Reculez » : vague de repoussement.
« Déposez les armes » : interruption temporaire d’ennemis compatibles ; les boss résistent partiellement plutôt que perdre toutes leurs mécaniques.
« Tenez bon » : protection des alliés dans une zone.
« Que le chemin s’ouvre » : activation de mécanismes et passages royaux dédiés.
« Restez » : entrave brève, avec jauge de résistance sur les adversaires importants.
« Élevez-vous » : ascension contrôlée ou ralentissement de chute.
Chaque décret possède un symbole, une animation et un signal sonore propres. Les répliques parlées doivent être facultatives et jamais répétées de façon envahissante.

4) LA GARDE ASTRALE
Chevaliers, archers et protecteur. Ordres simples : suivre, défendre, concentrer les attaques, revenir. Progression de petits effectifs vers une garde plus impressionnante. Des silhouettes décoratives peuvent renforcer les ultimes, avec une distinction claire entre spectacle et unités réellement actives. Évite des centaines d’entités dotées d’une IA complète.

5) LA MAIN DES LIMBES
Saisir un objet compatible, attirer un ennemi léger, interrompre un projectile, déplacer un élément de puzzle et former un passage temporaire. Version supérieure : immense main couronnée reproduisant les gestes du joueur. Ne supprime ni ne déplace arbitrairement coffres, bâtiments ou entités d’autres joueurs.

6) LES PACTES ROYAUX
Rencontrer, comprendre puis lier des créatures légendaires. Elles deviennent de véritables montures ou alliées avec commandes, animation d’arrivée et interactions au repos. Elles ne sont pas obtenues depuis un simple menu sans rencontre préalable.

Réserve de majesté : ressource magique qui se régénère, avec coût et récupération faciles à lire. Les techniques de base restent fréquentes et agréables ; les grandes techniques disposent de contraintes plus fortes. On doit se sentir puissant, sans attendre plusieurs minutes entre chaque action intéressante.

Interface : quelques capacités équipées, touches reconfigurables, sélection radiale facultative, tutoriel progressif et aperçu clair des améliorations. Évite vingt raccourcis obligatoires. Définis un fonctionnement fiable en vue à la première et à la troisième personne.

======================================================================
7. TECHNIQUES EMBLÉMATIQUES ET TRANSFORMATION
======================================================================

LE JUGEMENT DES CONSTELLATIONS
Une carte céleste se déploie au-dessus du champ de bataille. Des étoiles deviennent des lances qui frappent en séquence ; une arme royale géante termine l’attaque sur la cible marquée. Préparation visible, ciblage compréhensible, dégâts réellement appliqués et conclusion sonore forte. La grande silhouette céleste ne nécessite pas un véritable objet physique de centaines de blocs.

LA MARCHE DU PALAIS
Des marches lumineuses se forment sous les pas pour courir dans l’air. Une évolution transforme ce trajet en pont temporaire pour des compagnons. Définis précisément sa durée et ses règles de disparition ; personne ne doit tomber brutalement parce qu’un timer invisible expire. Ajoute avertissement et protection de chute.

L’AUDIENCE DU ROI — TECHNIQUE SIGNATURE
Des portes monumentales s’ouvrent, un trône apparaît, des colonnes et bannières dessinent une salle à ciel ouvert. Phrase emblématique facultative : « Le roi tient audience. » Le joueur continue à se déplacer et combattre librement.
Dans le domaine d’audience, les gardes prennent position, les alliés sont protégés et les décrets obtiennent des variantes. La scène s’adapte à l’espace ; elle ne remplace pas définitivement le terrain et ne détruit pas une ville. Fournis un comportement lisible dans les grottes et lieux étroits.

L’AVÈNEMENT — GRANDE TRANSFORMATION
La couronne s’assemble, un manteau de constellations apparaît, l’arsenal évolue et les mouvements laissent des passages de lumière. Elle modifie plusieurs mécaniques : esquive transformée en pas royal, combos prolongés, décret renforcé, garde améliorée et version supérieure d’une technique emblématique. Ce doit être plus qu’un bonus de statistiques et un effet de potion.
La transformation normale se débloque autour du couronnement. Une évolution exclusive à la fin de règne peut être inventée, à condition de rester cohérente et d’apporter une nouvelle manière de jouer.

Spectacle : modèles animés, trajectoires, impacts, déformation visuelle locale, son spatialisé, traces et réactions du décor autorisé. Ne remplace pas tout cela par une quantité excessive de particules. Les effets puissants doivent être visibles à distance, tout en permettant de lire la cible et les attaques adverses.

Destruction du terrain désactivée par défaut pour la magie spectaculaire. Prévois une option explicite pour des zones sauvages compatibles. Les zones de ville, domaines et structures critiques restent protégées. Les ruines et fissures temporaires utilisent une solution réversible fiable, sans duplication ni perte de blocs.

======================================================================
8. CINQ PRÉTENDANTS, CINQ PERSONNAGES CENTRAUX
======================================================================

Ces personnages vivent dans le monde, ont des proches, des projets et des raisons de vouloir gouverner. Le joueur les rencontre à plusieurs reprises avant leurs grands duels. Leur progression scénarisée dépend d’étapes de campagne, sans compte à rebours réel qui punirait un joueur prenant son temps.

AÉLIS, LA DAME DES JARDINS
Aimée à Sylvécime, généreuse mais prudente. Elle veut des terres fertiles et refuse de rouvrir certaines zones qu’elle estime dangereuses. Elle doit parfois apprendre à faire confiance aux explorateurs.
Magie : arbres immenses, chevaliers de bois blanc, fleurs absorbant des sorts et lance végétale changeante.
Aventure commune : retrouver une source ancienne et choisir comment alimenter les jardins et un village isolé. Les choix doivent avoir des conséquences visibles sans condamner arbitrairement une communauté.
Duel : jardin poussant pendant le combat, racines formant des passages, fleurs modifiant les zones et arbre soulevant l’arène. Prévois une géométrie réellement praticable et des transitions sans blocage.
Après reconnaissance : restauration de terres, jardins royaux et expéditions botaniques.

KAËL, LE PRINCE DES ROUTES
Aventurier chaleureux, connu des ports. Il veut relier tous les territoires, parfois sans mesurer ce qu’il réveille.
Magie : portails, lames spatiales, déplacements fulgurants et navire volant.
Aventure : rouvrir une ancienne route et secourir une expédition coincée dans un archipel mobile.
Duel : combat sur son navire traversant des portes, escales sur des îles et retours au pont. Utilise des scènes et espaces de combat maîtrisés si les limitations de déplacement de structures l’exigent. Le résultat doit rester une vraie séquence jouable.
Après reconnaissance : réseau de voyages, capitainerie et construction du navire royal.

ORSANE, LA REINE DE VERRE
Souveraine exigeante, compétente et respectée de Verréole. Elle a protégé sa région et croit qu’une organisation stricte pourrait préserver tous les Limbes.
Magie : armures translucides, miroirs déviant les attaques et colosses de cristal.
Aventure : descendre dans les forges et résoudre une crise touchant les voies de cristal ; montrer aussi la vie de ses artisans.
Duel : salle de miroirs qui se recompose, reflets avec indices identifiables puis armure colossale à points de rupture. Ses leurres ne doivent pas rendre le combat incompréhensible.
Après reconnaissance : ateliers, architecture et protections de la capitale.

SAHIR, LE GARDIEN DES HEURES
Calme, a connu l’ancien roi. Il protège les héritages du passé et craint les transformations qui effaceraient l’histoire des habitants.
Magie : ralentissements locaux, rappel d’armes et doubles reproduisant des gestes passés.
Aventure : traverser des quartiers conservés à différentes époques et sauver un lieu sans effacer les habitants actuels.
Duel : attaques suspendues, trajectoires annoncées puis reprises, fenêtres pour avancer. Les pouvoirs temporels simulent des comportements contrôlés ; aucun ralentissement du tick serveur entier.
Après reconnaissance : archives, observatoire et exploration de l’histoire royale.

VAËRON, LE ROI SANS COURONNE
Grand rival principal et favori initial. Il protège déjà plusieurs cités. Il estime que ses années de travail lui ont mérité le trône.
Il peut sauver le joueur au début, l’accueillir et lui faire visiter sa cité. Ses habitants l’apprécient pour des raisons que l’on voit : routes sûres, logements et institutions utiles. Ne le transforme pas soudainement en caricature maléfique pour justifier le combat.
Magie : légions astrales, bannières protectrices, épée solaire et forteresse invoquée.
Relation : respect, collaboration, désaccord croissant puis rivalité ouverte. Plusieurs dialogues et aventures doivent construire cette évolution.
Duel final : sa forteresse et ses légions font face à l’Audience du joueur. Plusieurs phases, objectifs tactiques et transformations spectaculaires. La victoire doit dépendre aussi de la maîtrise du joueur, pas uniquement des statistiques.
Après la défaite : il peut reconnaître le nouveau souverain et garder une responsabilité importante. Une conclusion alternative peut modifier votre relation, mais ne bloque pas les systèmes essentiels du règne.

La reconnaissance des régions n’est pas une série de cinq assassinats. Tournoi, négociation après une aventure, expédition et duel rituel peuvent apporter des soutiens différents. Tous les grands combats doivent rester accessibles, même si certains deviennent des épreuves amicales selon les choix.

======================================================================
9. CAMPAGNE ET COURONNEMENT
======================================================================

Écris une campagne complète, avec scènes, objectifs, dialogues, récompenses et conditions de déclenchement. Respecte cette ossature et développe les transitions :

ACTE I — L’ÉTRANGER
Entrée, accueil, découverte de Clairval, premier sauvetage, fragment de couronne et attribution du manoir. Rencontre de Vaëron. Premiers pouvoirs et première Arrivée.

ACTE II — LE SEIGNEUR DE CLAIRVAL
Restaurer les installations principales, recruter plusieurs habitants, découvrir deux régions et ouvrir des relais. Rencontrer les cinq prétendants au fil d’événements cohérents. Premier accès aux quartiers extérieurs de la capitale.

ACTE III — LE PRÉTENDANT
Grandes aventures régionales et soutiens, développement de l’arsenal, relations entre prétendants, nouvelles zones de transport et révélations sur les routes éteintes. Ordre partiellement libre, objectifs explicites et adaptations des dialogues.

ACTE IV — LA COURONNE
Réveil des derniers accès, conflit avec Vaëron, grande épreuve finale puis préparation du couronnement. La disparition du précédent roi doit recevoir une explication satisfaisante, reliée aux Arrivées et à la fonction du souverain, sans virer à l’horreur. Le joueur ne remplace pas une catastrophe par une autre sans le savoir.

COURONNEMENT JOUABLE
Les délégations arrivent ; des personnages réellement aidés sont présents. Les ponts se reforment, la ville s’illumine et le joueur traverse les lieux qu’il a réveillés. Courts dialogues personnalisés, cérémonie puis contrôle immédiatement rendu. Pas de dix minutes de scène imposée.
Premier acte royal : attirer un fragment perdu vers un emplacement sûr et ouvrir un nouveau territoire. Ce fragment possède du contenu réel à explorer.

ACTE V — LE RÈGNE
Campagne supplémentaire avec au moins trois aventures substantielles : accueillir une cité arrivée récemment, organiser un grand tournoi avec les anciens rivaux et mener une expédition à bord du navire royal. Une aventure finale aux frontières des Limbes conclut ce cycle sans supprimer les activités libres.

Le journal montre toujours l’objectif actif, la prochaine action et les indications d’emplacement. Prévois carte, boussole ou marqueur en jeu configurable avec distance et nom du lieu ; ne laisse jamais le joueur chercher un PNJ critique dans une dimension immense sans piste fiable. Permets le suivi d’une quête à la fois et un mode à indications plus discrètes.

======================================================================
10. LES ARRIVÉES : ÉVÉNEMENTS ET DÉCOUVERTES
======================================================================

Parfois, le ciel se plisse, une lumière apparaît et quelque chose rejoint les Limbes. Les habitants possèdent des observatoires pour repérer ces phénomènes.

Exemples à réaliser : maison avec un occupant désorienté ; navire avec cargaison et journal ; jardin de graines inconnues ; tour mécanique ; troupe de voyageurs cherchant un foyer ; fragment de territoire déclenchant une expédition.

Trois échelles : petites découvertes, aventures moyennes et Arrivées majeures écrites. Conçois des événements aux mécaniques différentes, pas seulement des coffres avec un nom.

Contraintes : jamais d’atterrissage sur une construction ou un joueur ; utilise des emplacements réservés, des zones vierges vérifiées ou des espaces d’expédition dédiés. Fréquence réglable, limite d’événements simultanés et sauvegarde de leur état. Les grands événements restent disponibles jusqu’à leur résolution. Les petites occasions temporaires reviennent et ne contiennent pas d’objet indispensable unique.

Après couronnement, le roi peut sélectionner une expédition pour préparer une Arrivée et choisir un emplacement compatible. Il ne génère pas indéfiniment des chunks et des entités sans limite. Prévois un archivage des événements terminés et un catalogue de nouveaux fragments.

======================================================================
11. FAUNE, MONTURES ET COMPAGNONS
======================================================================

Créatures essentielles :
— Raies célestes : nage aérienne souple, approche avec une nourriture spécifique, transport entre îles.
— Cerfs-lanternes : sentiers lumineux au crépuscule vers des secrets ; comportement sauvage paisible.
— Tortues-jardins : petit écosystème porté, accueil au domaine et cultures particulières.
— Baleines des hauteurs : migrations régionales, spectacle à distance et expédition vers des ruines sur leur dos. Choisis une technique adaptée pour les zones habitables, avec embarquement stable et sauvegarde cohérente.
— Griffons de verre : vol rapide, combat monté et variations liées aux cristaux utilisés pour leur croissance.
— Petits ramasseurs : objets égarés près de leurs terriers ; compagnon aidant à retrouver de l’équipement perdu, sans supprimer ni dupliquer les inventaires.

Complète avec faune ambiante, prédateurs, gardiens construits, créatures de récifs et êtres rares. Toutes les créatures ne sont pas hostiles. Les grands animaux doivent avoir des comportements au repos et des transitions animées.

Vol : décollage, atterrissage, inertie, endurance raisonnable, collision et protection contre les chutes lors d’un démontage involontaire. Prévois rappel de monture et récupération si elle reste bloquée. Une monture essentielle ne doit pas disparaître définitivement à cause d’un bug ou d’un chunk déchargé.

Compagnons humains : quelques alliés développés, sélection d’un petit groupe, ordres simples, dialogues liés aux lieux et réactions à l’histoire. Prévois un état à terre et une récupération plutôt qu’une mort définitive accidentelle de tous les personnages de quête.

======================================================================
12. ACTIVITÉS ET RICHESSE DU CONTENU
======================================================================

Prévois de vraies boucles secondaires :
— Navigation, personnalisation de bateau, cartes d’épaves et plongée.
— Courses et parcours de montures, avec récompenses et variantes de trajet.
— Jardinage, recettes régionales et plats apportant des effets utiles.
— Forge et personnalisation visuelle des armes royales.
— Archéologie d’objets perdus, indices et collections exposables.
— Pêche d’espèces propres aux volumes d’eau suspendus.
— Énigmes spatiales et temporelles utilisant les pouvoirs du joueur.
— Tournois avec plusieurs épreuves et adversaires aux styles différents.
— Commerce simple entre les régions et amélioration de relais.
— Festivals, marchés, scènes de vie et petites histoires d’habitants.

Objectifs de contenu pour la version complète :
— 6 grandes régions, avec au moins 3 sous-biomes chacune.
— 6 centres habités principaux, plus la cité de Vaëron et plusieurs petits établissements.
— Au moins 40 lieux d’intérêt conçus, avec objectifs ou découvertes variés.
— 5 grands prétendants et leurs rencontres, aventures, duels et conclusions.
— Au moins 30 quêtes principales comprenant les actes de règne et 40 quêtes secondaires distinctes.
— Au moins 12 types d’Arrivées, dont plusieurs aventures majeures.
— Au moins 24 espèces ou types de créatures réellement différents, dont 5 montures ou alliés apprivoisables.
— Au moins 24 techniques royales jouables, en plus de variantes de maîtrise clairement identifiées.
— Au moins 10 épreuves ou affrontements majeurs, dont les 5 prétendants ; les autres peuvent être gardiens, tournois ou rencontres légendaires non létales.
— Un ensemble généreux de blocs de construction, meubles, plantes et objets exposables, avec de véritables usages de décoration.

Ces nombres fixent une cible de production, pas une invitation à gonfler les comptes. Une recoloration, une étape de dialogue et un même sort avec des dégâts supérieurs ne comptent pas comme trois nouveaux contenus. Utilise des modèles de données et outils de création pour rendre ce volume maintenable. N’annonce pas la version complète tant que ces objectifs et leur qualité ne sont pas réellement atteints.

======================================================================
13. ARCHITECTURE ET DÉCORATION : EXIGENCE MAJEURE
======================================================================

Les villes, maisons, boutiques et palais doivent être beaux à visiter à hauteur de joueur. Évite les grandes salles vides avec un objet dans un coin. Chaque pièce doit avoir une fonction lisible, des meubles adaptés, des détails de vie et une circulation praticable.

Une auberge comprend salle commune, cuisine, réserve, chambres distinctes, sanitaires ou bains adaptés à son univers. Une forge possède poste de travail, outils, stockage, commandes en cours et logement si pertinent. Un palais possède salles de conseil, appartements, cuisines, service, jardins, archives et espaces pour les visiteurs ; pas uniquement un trône au fond d’une halle.

Travaille les volumes, profondeurs de façade, encadrements, sols, plafonds, éclairage, textiles, plantes, objets et transitions entre espaces. Privilégie les grands repères architecturaux, quelques détails moyens et de petits accessoires bien placés. Respecte les ouvertures et le passage des PNJ.

Crée des meubles et éléments personnalisés lorsque les blocs vanilla ne suffisent pas. Ne construis pas chaque petit détail comme une entité active coûteuse. Utilise blocs, modèles et rendus adaptés.

Les améliorations doivent se voir : échafaudages ou travaux intermédiaires facultatifs, puis nouveaux commerces, jardins et habitants. Les PNJ suivent des routines raisonnables : travail, repas, déplacements et repos, avec interactions accessibles même si l’horaire exact n’est pas idéal.

======================================================================
14. ASSETS, ANIMATIONS, VFX, AUDIO ET INTERFACE
======================================================================

Crée des assets originaux cohérents avec Minecraft, tout en visant une qualité très supérieure à un assemblage de textures provisoires. Utilise Blockbench, Blender ou des outils de génération lorsque cela apporte un vrai résultat et qu’ils sont disponibles. Ne te limite pas aux assets vanilla par facilité. Respecte un pipeline d’export adapté aux rendus et animations réellement utilisés.

Priorités : prétendants reconnaissables, couronnes évolutives, armes invoquées, garde astrale, montures, grandes créatures, portes royales, mobilier et architecture régionale.

Animations : repos et déplacement, attaques anticipées, impacts, esquive, parade, invocation, transformation, montée et descente de monture, interactions importantes. Les boss doivent annoncer leurs actions avec des gestes que le joueur peut apprendre.

Les grandes attaques possèdent préparation, montée en puissance, action, impact et dissipation. Associe des formes animées et un son au bon moment. Une attaque gigantesque doit avoir une présence réelle dans la scène sans masquer continuellement l’écran.

Son : ambiances régionales, résonances des cristaux, ports, ateliers, faune et signatures des pouvoirs. Musiques originales ou légalement utilisables avec sources documentées ; aucune extraction de contenus protégés. Le jeu reste compréhensible sans doublage. Les sous-titres et signaux visuels couvrent les informations utiles.

Interface : journal illustré, carte, progression royale, gestion du domaine, relations des prétendants et sélection de capacités. Montre des actions compréhensibles plutôt que des termes de développement. Les menus expliquent ce que le joueur obtient et comment l’utiliser.

Options : densité des effets, secousses de caméra, flashes, volume des voix et musique, marqueurs, qualité de rendu et taille de l’interface. Les secousses et flashes doivent pouvoir être désactivés. La qualité réduite conserve les télégraphes de combat.

Si je fournis ensuite des images de référence, respecte autant que possible leur direction artistique, palette, richesse d’aménagement et silhouette. Signale les adaptations nécessaires au moteur sans abandonner leur identité. Aucun fichier de référence n’accompagne automatiquement ce prompt : ne prétends pas en avoir consulté.

======================================================================
15. PROGRESSION, CONFORT ET PERSISTANCE
======================================================================

— Déblocages majoritairement liés aux aventures, exploration, pactes et restaurations. Évite le grind de centaines de monstres identiques.
— Progression de puissance très sensible : les premières menaces deviennent faciles, puis de nouveaux adversaires demandent de maîtriser les possibilités acquises. N’annule pas tout progrès par un ajustement automatique uniforme.
— Une mort ne remet pas à zéro des heures de campagne. Prévois points de retour, récupération et protection des objets de quête.
— Quêtes conçues pour résister à une déconnexion, une mort de PNJ, un objet perdu, une destination temporairement inaccessible ou un ordre d’exploration différent.
— Positions des villes et personnages clés persistantes ; réapparition contrôlée des acteurs essentiels.
— Distingue progression personnelle, relations, permissions de domaine et événements partagés.
— Sauvegarde versionnée des quêtes, constructions, montures, factions, Arrivées et restaurations. Ne réinitialise pas une région à chaque redémarrage.
— Objets critiques récupérables sans duplication de récompenses. Actions de quête idempotentes et transactions fiables.
— Commandes de diagnostic et réparation réservées à l’administration, accompagnées de retours clairs. Elles ne remplacent pas les interactions de survie.

======================================================================
16. ARCHITECTURE LOGICIELLE ET PERFORMANCE
======================================================================

Organise le code par responsabilités : dimension et génération, structures, quêtes, campagne, pouvoirs, combat, IA, montures, domaine, Arrivées, réseau, sauvegarde, interface et rendu. Utilise un namespace cohérent, par exemple les_limbes. Privilégie des définitions de contenu dans des données validées lorsque cela facilite l’ajout d’aventures et de variantes.

Serveur autoritaire pour dégâts, coûts, téléportation, progression, inventaires et construction. Valide tous les paquets et borne leurs paramètres. Synchronise l’état utile et les déclenchements d’effets ; n’envoie pas des milliers de particules individuelles à chaque tick.

Budgets explicites pour PNJ actifs, invocation, pathfinding, événements et génération de structures. Mise à jour espacée ou suspension des routines hors distance utile. Limite le chargement forcé des chunks, libère les tickets et nettoie les entités temporaires.

Pour les architectures géantes et combats spectaculaires, combine éléments réels interactifs et représentations visuelles adaptées. Décris honnêtement les limites techniques ; ne rends pas le jeu instable pour satisfaire littéralement une taille annoncée.

Pour bateau volant, baleine habitable et arènes mobiles, choisis une solution viable pour cette version : déplacement contrôlé d’une scène, espace d’expédition dédié ou autre technique testée. Le joueur doit retrouver la sensation promise et des interactions fiables. Évite de promettre un moteur universel de structures mobiles sans l’implémenter.

Le mod doit pouvoir tourner sans shaders et sans ordinateur exceptionnel. Propose des profils graphiques, mesure les performances et rapporte la configuration réelle des essais. N’invente pas de chiffres de FPS.

======================================================================
17. MÉTHODE DE RÉALISATION
======================================================================

Commence par inspecter le projet et les outils disponibles. Établis un document de vision synthétique, un catalogue de contenu, un schéma de progression et un backlog persistant. Puis implémente réellement.

Ordre conseillé :
1. Socle compilable, accès à la dimension, retour sûr et sauvegarde.
2. Prairies, Clairval, manoir, premiers PNJ, une quête complète et premiers pouvoirs.
3. Tranche de qualité : une mini-aventure, une monture, une Arrivée et un combat représentatif avec assets et effets aboutis.
4. Domaine, journal, réseau de passages et systèmes réutilisables.
5. Régions restantes, villes et contenu d’exploration.
6. Cinq prétendants, relations, campagne, duels et couronnement.
7. Pouvoirs supérieurs, règne, navire royal et nouveaux territoires.
8. Complétion du contenu, décoration, optimisation et validation finale.

Construis tôt les outils qui rendent le contenu répétable à produire : validation de dialogues, inspection de structures, génération d’assets simples, visualisation des zones réservées et réinitialisation de scènes de test. Les modèles de quêtes doivent permettre des aventures différentes et pas uniquement des objectifs de collecte.

Ne passe pas toute une session à planifier sans produire. Ne t’arrête pas à une démonstration avec trois blocs et un portail. Si la session se termine, écris un état précis et les prochaines actions exécutables pour reprendre sans perdre de travail.

======================================================================
18. VALIDATION ET CRITÈRES D’ACCEPTATION
======================================================================

Valide chaque système avant d’y empiler davantage de contenu. Tests significatifs : logique des quêtes et choix ; sauvegarde/rechargement ; autorité serveur ; coûts et téléportation ; génération d’emplacements critiques ; protection des constructions. Vérifie aussi les ressources manquantes et les références de données.

Scénarios à jouer ou, si l’environnement ne le permet pas, à documenter comme non vérifiés :
— Nouvelle sauvegarde : découvrir le passage, entrer, recevoir un objectif et revenir dans l’Overworld.
— Restaurer le domaine et conserver inventaires et constructions après redémarrage.
— Trouver chaque région et personnage critique sur plusieurs seeds.
— Accomplir les grandes aventures dans plusieurs ordres.
— Utiliser les pouvoirs avec collisions, eau, hauteur, lieux étroits et changement de dimension.
— Monter, voyager, se déconnecter et rappeler une monture.
— Terminer chaque duel, échouer, recommencer et récupérer la progression correctement.
— Achever la campagne, assister au couronnement et accéder aux aventures du règne.
— Ajouter un joueur après le couronnement et lui permettre un parcours complet.
— Tester serveur dédié et deux joueurs au minimum lorsque possible.
— Vérifier les performances dans une ville, pendant une Arrivée et pendant deux grandes techniques simultanées.

Compile avec la chaîne réellement installée. Si une dépendance, un réseau ou un outil bloque une étape, conserve les erreurs exactes utiles, corrige ce qui peut l’être et distingue clairement ce qui a été testé de ce qui reste à vérifier. Une compilation réussie ne prouve pas à elle seule qu’un combat ou une campagne fonctionne.

Au terme de chaque étape, donne brièvement : résultat jouable, fichiers et systèmes livrés, vérifications effectuées et prochaine étape. À la livraison complète, fournis des instructions permettant de retrouver le contenu sans commandes de développement.

======================================================================
19. CE QUI DOIT GUIDER TOUTES TES DÉCISIONS
======================================================================

Je veux être émerveillé par les Limbes. Je veux découvrir des lieux où j’ai envie de rester, rencontrer des gens que je reconnaîtrai plus tard et sentir mon domaine devenir un véritable foyer.

Je veux des rivaux crédibles, des aventures différentes d’une région à l’autre et une ascension royale que je vis réellement.

Je veux une magie spectaculaire : des armes monumentales, des portes dans le ciel, une cour invoquée, une transformation magnifique et des attaques dont on comprend la puissance à l’image et au son.

Je veux devenir roi pour continuer à jouer : voyager avec ma cour, accueillir des cités, développer mon palais, organiser des événements et découvrir ce qui attend aux frontières.

Ajoute tes propres trouvailles pour me surprendre : secrets, lieux, créatures, techniques et petites scènes de vie. Elles doivent renforcer cette vision et être réellement réalisées, sans remplacer les éléments demandés.

Commence maintenant par inspecter ton environnement, définir la structure du projet et réaliser la première étape jouable. Conserve ensuite ce cahier des charges comme référence jusqu’à la réalisation de la dimension complète.
