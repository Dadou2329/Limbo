"""Génère 27 quêtes secondaires (JSON) et branche leurs dialogues (offre, rappel, remise) chez leurs donneurs."""
import json, pathlib
ROOT = pathlib.Path(__file__).resolve().parents[2] / "src" / "main" / "resources" / "data" / "les_limbes"
LIMBES = "les_limbes:limbes"
LIEUX = {k: v for k, v in [(p.stem, json.loads(p.read_text(encoding="utf-8"))) for p in (ROOT / "lieu").glob("*.json")]}

def lieu(cle, nom=None):
    d = LIEUX[cle]; return {"nom": nom or d["nom"], "pos": d["pos"], "dimension": LIMBES}
def collecter(i, item, n, texte): return {"id": i, "type": "collecter", "cible": item, "nombre": n, "texte": texte}
def vaincre(i, ent, n, texte): return {"id": i, "type": "vaincre", "cible": "les_limbes:" + ent, "nombre": n, "texte": texte}
def atteindre(i, cle, texte, rayon=None):
    d = LIEUX[cle]; return {"id": i, "type": "atteindre", "pos": d["pos"], "rayon": rayon or max(10, d.get("rayon", 12)), "dimension": LIMBES, "texte": texte}
def parler(role, texte): return {"id": "retour", "type": "parler", "cible": role, "manuel": True, "texte": texte}
def lb(x): return "les_limbes:" + x

Q = []
def quete(id, titre, acte, giver, lieu_cle, description, etapes, offre, intro, fin_texte, remise, recompense, xp, offre_dans=("accueil",), prereq=(), lieu_nom=None):
    Q.append(dict(id=id, titre=titre, acte=acte, giver=giver, lieu=lieu(lieu_cle, lieu_nom), description=description, etapes=etapes, offre=offre, intro=intro,
                  fin_texte=fin_texte, remise=remise, recompense=recompense, xp=xp, offre_dans=offre_dans, prereq=list(prereq)))

# ------------------------------------------------------------------ Prairies
quete("les_lanternes_du_pont", "Les lanternes du pont", 2, "brann", "clairval",
      "Brann veut éclairer le grand pont du Sanctuaire pour ceux qui arrivent de nuit : six lanternes nacrées, qu'il montera lui-même sur les piles.",
      [{"id": "lanternes", "resume": "Réunir six lanternes nacrées pour Brann (cristal de nacre et fibres).", "objectifs": [collecter("lanternes", lb("lanterne_nacree"), 6, "Réunir six lanternes nacrées")]},
       {"id": "retour", "resume": "Rapporter les lanternes à Brann, à la forge de Clairval.", "objectifs": [parler("brann", "Rapporter les lanternes à Brann")]}],
      "Le pont est sombre, la nuit. Que puis-je faire ?",
      ["Six lanternes nacrées : du cristal de nacre, des fibres, une bougie de lumière. Je les monte sur les piles du pont, et plus personne n'arrivera dans le noir."],
      ["Six lanternes ! Je grimpe les poser ce soir. Tenez, du cristal pour vos propres outils : un forgeron paie ses dettes."],
      "Voici les six lanternes.", [(lb("cristal_de_nacre"), 4)], 2, offre_dans=("forge", "metal"))
quete("la_patrouille_de_gaspard", "La patrouille de Gaspard", 2, "gaspard", "relais_royal",
      "Gaspard n'ose plus s'éloigner du relais : des Veilleurs égarés rôdent encore sur la route de l'est. Cinq de moins, et il dormira.",
      [{"id": "veilleurs", "resume": "Chasser cinq Veilleurs égarés autour du relais royal et sur la route de l'est.", "objectifs": [vaincre("veilleurs", "veilleur_egare", 5, "Vaincre cinq Veilleurs égarés")]},
       {"id": "retour", "resume": "Revenir dire à Gaspard que la route est sûre.", "objectifs": [parler("gaspard", "Rassurer Gaspard")]}],
      "Vous dormez mal, Gaspard ?",
      ["Ils reviennent la nuit, les grands de pierre. Pas jusqu'ici, mais sur la route. Cinq de moins et je recommencerai à porter le pain à Clairval."],
      ["Cinq ? Vous avez compté ? Alors je repars demain avec ma charrette. Prenez cette lanterne royale : elle vient du relais d'avant, elle ne s'éteint jamais."],
      "La route est sûre.", [(lb("lanterne_royale"), 1), (lb("pain_d_emile"), 2)], 3, offre_dans=("apres", "clairval"))
quete("les_trois_ciels", "Les trois ciels", 3, "lisbeth", "observatoire",
      "Lisbeth veut comparer le ciel des Prairies à celui des autres régions : relever le ciel depuis l'observatoire des heures, le phare du Dessous et l'atelier d'instruments.",
      [{"id": "releves", "resume": "Se rendre à l'observatoire des heures (Désert), au phare du Dessous (Mer) et à l'atelier d'instruments (Montagnes).",
        "objectifs": [atteindre("heures", "observatoire_des_heures", "Relever le ciel à l'observatoire des heures"), atteindre("phare", "phare_du_dessous", "Relever le ciel au phare du Dessous"),
                      atteindre("atelier", "atelier_d_instruments", "Relever le ciel à l'atelier d'instruments")]},
       {"id": "retour", "resume": "Rapporter les trois relevés à Lisbeth.", "objectifs": [parler("lisbeth", "Rapporter les relevés à Lisbeth")]}],
      "Le ciel est-il le même partout ?",
      ["Je ne sais pas, justement. Allez voir pour moi : l'observatoire des heures au sud, le phare du Dessous à l'est, l'atelier d'instruments dans les montagnes. Regardez le ciel, et revenez me dire."],
      ["Trois ciels, trois plis différents. Les Arrivées ne tombent pas au hasard, elles suivent les plis. Vous m'avez donné du travail pour dix ans. Prenez ces perles, elles brillent comme vos trois ciels."],
      "Voici ce que j'ai vu.", [(lb("perle_de_nacre"), 3)], 4, offre_dans=("veille",))
quete("les_cartes_d_ysolde", "Les cartes d'Ysolde", 3, "ysolde", "manoir",
      "Ysolde veut compléter ses cartes de la Mer suspendue : le quai des Falaises, le sanctuaire des bancs sous l'eau et le navire perdu.",
      [{"id": "releves", "resume": "Relever le quai des Falaises, le sanctuaire des bancs et le navire perdu pour les cartes d'Ysolde.",
        "objectifs": [atteindre("quai", "quai_des_falaises", "Relever le quai des Falaises"), atteindre("bancs", "sanctuaire_des_bancs", "Plonger jusqu'au sanctuaire des bancs"),
                      atteindre("navire", "navire_perdu", "Relever le navire perdu")]},
       {"id": "retour", "resume": "Rapporter les relevés à Ysolde, au manoir.", "objectifs": [parler("ysolde", "Rapporter les relevés à Ysolde")]}],
      "Vos cartes de la Mer, où en sont-elles ?",
      ["Pleines de blancs. Le quai des Falaises, le sanctuaire des bancs sous l'eau, le navire perdu : allez-y, comptez vos pas, et je trace."],
      ["Trois blancs de moins. Un jour ces cartes serviront à votre navire royal, j'en suis sûre. Prenez des perles : la Mer vous les doit."],
      "Voici mes relevés.", [(lb("perle_de_nacre"), 3)], 4, offre_dans=("recrutee", "domaine", "merci"))
# ------------------------------------------------------------------ Mer suspendue
quete("les_filets_de_maude", "Les filets de Maude", 3, "poissonniere", "port_nacre",
      "Maude, la poissonnière de Port-Nacre, manque de poissons-lanternes pour la criée : huit, pêchés dans les lagons.",
      [{"id": "poissons", "resume": "Pêcher huit poissons-lanternes dans la Mer suspendue.", "objectifs": [collecter("poissons", lb("poisson_lanterne"), 8, "Pêcher huit poissons-lanternes")]},
       {"id": "retour", "resume": "Livrer les poissons à Maude, à la criée de Port-Nacre.", "objectifs": [parler("poissonniere", "Livrer les poissons à Maude")]}],
      "La criée est bien vide, Maude.",
      ["Les bancs se sont éloignés depuis que les routes dorment. Huit poissons-lanternes, et je rouvre la criée. Une ligne, un peu de patience, et de l'eau qui brille."],
      ["Huit ! Et des beaux. La criée rouvre demain. Prenez des perles, c'est ce qu'on trouve dans les plus gros."],
      "Voici les poissons.", [(lb("perle_de_nacre"), 3)], 2)
quete("la_plongee_d_isaure", "La plongée d'Isaure", 3, "plongeuse", "port_nacre",
      "Isaure, la plongeuse, veut du corail de nacre du sanctuaire des bancs pour réparer les parures de la capitainerie.",
      [{"id": "corail", "resume": "Plonger jusqu'au sanctuaire des bancs et remonter quatre coraux de nacre.",
        "objectifs": [atteindre("sanctuaire", "sanctuaire_des_bancs", "Plonger jusqu'au sanctuaire des bancs"), collecter("corail", lb("corail_de_nacre"), 4, "Remonter quatre coraux de nacre")]},
       {"id": "retour", "resume": "Rapporter le corail à Isaure.", "objectifs": [parler("plongeuse", "Rapporter le corail à Isaure")]}],
      "Vous plongez où, d'habitude ?",
      ["Au sanctuaire des bancs, sous l'eau, droit au sud du port. Le corail de nacre y pousse serré. Quatre morceaux, et je refais les parures de la capitainerie. Respirez bien avant."],
      ["Du vrai corail des bancs. Vous avez le souffle long. Tenez, du corail d'azur : il luit sous l'eau, vous retrouverez toujours le chemin."],
      "Voici le corail.", [(lb("corail_azur"), 4)], 3)
quete("le_phare_d_anselme", "Le phare d'Anselme", 3, "anselme", "phare_du_dessous",
      "Anselme garde le phare du Dessous, mais des gardiens des épaves montent sur le rebord la nuit. Trois de moins, et la lampe tiendra.",
      [{"id": "gardiens", "resume": "Abattre trois gardiens des épaves autour du phare du Dessous.", "objectifs": [vaincre("gardiens", "gardien_des_epaves", 3, "Vaincre trois gardiens des épaves")]},
       {"id": "retour", "resume": "Revenir au phare rassurer Anselme.", "objectifs": [parler("anselme", "Rassurer Anselme")]}],
      "La lampe tremble, Anselme.",
      ["Ce sont les gardiens des épaves. Ils montent du Dessous quand la lampe faiblit, et la lampe faiblit parce qu'ils montent. Trois de moins, et je la remonte à plein."],
      ["La lampe est à plein. Vous les avez entendus retomber ? Moi oui. Prenez ces lanternes, elles sont de la même huile que la mienne."],
      "Les gardiens sont retombés.", [(lb("lanterne_nacree"), 6), (lb("perle_de_nacre"), 1)], 3)
quete("les_cordages_de_loup", "Les cordages de Loup", 3, "second_de_kael", "port_nacre",
      "Loup, le second de Kaël, regrée le Vent-Debout : il lui manque des fibres nacrées et des planches d'aube.",
      [{"id": "materiaux", "resume": "Réunir seize fibres nacrées et huit planches d'aube pour Loup.",
        "objectifs": [collecter("fibres", lb("fibres_nacrees"), 16, "Réunir seize fibres nacrées"), collecter("planches", lb("planches_d_aube"), 8, "Réunir huit planches d'aube")]},
       {"id": "retour", "resume": "Livrer les cordages et les planches à Loup, à la capitainerie.", "objectifs": [parler("second_de_kael", "Livrer les matériaux à Loup")]}],
      "Le Vent-Debout a besoin de quelque chose ?",
      ["De cordages, toujours. Seize fibres nacrées des Prairies pour les tresser, huit planches d'aube pour le pont. Kaël commande, moi je grée."],
      ["Des fibres des Prairies et du bois d'aube : le Vent-Debout tiendra une saison de plus. Prenez ces perles, Kaël ne regarde jamais la caisse."],
      "Voici les fibres et les planches.", [(lb("perle_de_nacre"), 2)], 2)
# ------------------------------------------------------------------ Jardins des Géants
quete("le_four_de_colombe", "Le four de Colombe", 3, "boulangere_sylve", "sylvecime",
      "Colombe, la boulangère de Sylvécime, veut essayer un pain aux fruits de géant : de la farine du moulin de Clairval et des fruits des grands arbres.",
      [{"id": "ingredients", "resume": "Apporter quatre farines et trois fruits de géant à Colombe.",
        "objectifs": [collecter("farine", lb("farine"), 4, "Moudre quatre farines"), collecter("fruits", lb("fruit_de_geant"), 3, "Cueillir trois fruits de géant")]},
       {"id": "retour", "resume": "Apporter les ingrédients à Colombe, dans la canopée de Sylvécime.", "objectifs": [parler("boulangere_sylve", "Apporter les ingrédients à Colombe")]}],
      "Qu'est-ce qui cuit, Colombe ?",
      ["Rien de nouveau depuis dix ans, voilà le problème. On m'a parlé d'un moulin à Clairval. Quatre farines, trois fruits de géant, et je tente un pain qui ferait rougir Aélis."],
      ["Il a monté ! Il a monté comme un arbre. Goûtez, et prenez du miel pour le manger : c'est ma façon de dire merci."],
      "Voici la farine et les fruits.", [(lb("miel_de_rosee"), 4), (lb("pain_d_emile"), 2)], 2)
quete("les_fils_d_aude", "Les fils d'Aude", 3, "tisserande", "sylvecime",
      "Aude, la tisserande, veut tisser une bannière pour la salle d'Aélis : des fibres nacrées et des pétales des deux couleurs.",
      [{"id": "fils", "resume": "Réunir dix fibres nacrées, quatre pétales roses et quatre pétales bleus.",
        "objectifs": [collecter("fibres", lb("fibres_nacrees"), 10, "Réunir dix fibres nacrées"), collecter("roses", lb("petale_rose"), 4, "Cueillir quatre pétales roses"),
                      collecter("bleus", lb("petale_bleu"), 4, "Cueillir quatre pétales bleus")]},
       {"id": "retour", "resume": "Apporter les fils et les pétales à Aude.", "objectifs": [parler("tisserande", "Apporter les fils à Aude")]}],
      "Que tissez-vous, Aude ?",
      ["Une bannière pour la salle d'Aélis, si je trouve de quoi. Dix fibres nacrées pour la trame, quatre pétales roses et quatre bleus pour la couleur. Les pétales des grandes fleurs, pas les petites."],
      ["Regardez la couleur que ça donne, mouillé. Je la tends ce soir. Prenez du cristal de nacre : j'en garde pour les fuseaux, mais vous en ferez mieux."],
      "Voici les fils et les pétales.", [(lb("cristal_de_nacre"), 3)], 2)
quete("les_simples_de_perrin", "Les simples de Perrin", 3, "herboriste", "sylvecime",
      "Perrin, l'herboriste, prépare un onguent pour les guetteurs : de l'herbe de l'aube des Prairies et des fleurs de rosée.",
      [{"id": "simples", "resume": "Cueillir huit herbes de l'aube et quatre fleurs de rosée pour Perrin.",
        "objectifs": [collecter("herbes", lb("herbe_de_l_aube"), 8, "Cueillir huit herbes de l'aube"), collecter("fleurs", lb("fleur_de_rosee"), 4, "Cueillir quatre fleurs de rosée")]},
       {"id": "retour", "resume": "Apporter les simples à Perrin.", "objectifs": [parler("herboriste", "Apporter les simples à Perrin")]}],
      "Vous cherchez des plantes, Perrin ?",
      ["Celles des Prairies, qui ne poussent pas dans l'ombre des géants : huit herbes de l'aube, quatre fleurs de rosée. Mon onguent pour les guetteurs n'attend que ça."],
      ["Fraîches encore. Mon onguent sera prêt demain. Prenez ces bouillons de l'aube, j'en fais trop : une herbe de chez vous, ça réchauffe même ici."],
      "Voici les simples.", [(lb("bouillon_de_l_aube"), 3)], 2)
quete("le_guet_de_sylvecime", "Le guet de Sylvécime", 3, "guetteur_sylve", "sylvecime",
      "Le guetteur de Sylvécime voit des rôdeurs des racines monter vers la ville la nuit. Quatre de moins, et les enfants redescendront jouer.",
      [{"id": "rodeurs", "resume": "Chasser quatre rôdeurs des racines sous Sylvécime.", "objectifs": [vaincre("rodeurs", "rodeur_des_racines", 4, "Vaincre quatre rôdeurs des racines")]},
       {"id": "retour", "resume": "Revenir au guet annoncer que les racines sont calmes.", "objectifs": [parler("guetteur_sylve", "Rendre compte au guetteur")]}],
      "Qu'est-ce que vous guettez ?",
      ["Les rôdeurs des racines. Ils montent la nuit, lents, puis ils frappent. Quatre de moins et je laisse les enfants redescendre jouer sous les géants."],
      ["Quatre. J'ai compté les silences. Les enfants descendront demain. Prenez des fruits et des perles : le guet n'a rien d'autre, mais il a ça."],
      "Les racines sont calmes.", [(lb("fruit_de_geant"), 3), (lb("perle_de_nacre"), 2)], 3)
# ------------------------------------------------------------------ Désert des Heures
quete("les_rouages_de_l_horloger", "Les rouages de l'horloger", 3, "horloger", "meridiane",
      "L'horloger de Méridiane répare la grande horloge : il lui faut des éclats résonnants des Montagnes et du cristal de nacre.",
      [{"id": "pieces", "resume": "Réunir quatre éclats résonnants et deux cristaux de nacre pour l'horloger.",
        "objectifs": [collecter("eclats", lb("eclat_resonnant"), 4, "Réunir quatre éclats résonnants"), collecter("cristaux", lb("cristal_de_nacre"), 2, "Réunir deux cristaux de nacre")]},
       {"id": "retour", "resume": "Apporter les pièces à l'horloger de Méridiane.", "objectifs": [parler("horloger", "Apporter les pièces à l'horloger")]}],
      "L'horloge est arrêtée ?",
      ["Depuis que le roi est parti, à la minute près. Il me faut quatre éclats résonnants des Montagnes pour l'échappement, deux cristaux de nacre pour le balancier. Je remonterai le reste."],
      ["Écoutez. Vous entendez ? Elle repart. Je vous dois une heure de ma vie, au moins. Prenez ces perles, et ce sablier que j'avais en trop."],
      "Voici les pièces.", [(lb("perle_de_nacre"), 3)], 3)
quete("la_caravane_d_idir", "La caravane d'Idir", 3, "caravanier", "meridiane",
      "Idir, le caravanier, veut rouvrir sa route du désert : l'observatoire des heures, le jardin redevenu fertile et la cour des heures.",
      [{"id": "etapes", "resume": "Reconnaître la route d'Idir : observatoire des heures, jardin fertile, cour des heures.",
        "objectifs": [atteindre("observatoire", "observatoire_des_heures", "Atteindre l'observatoire des heures"), atteindre("jardin", "jardin_fertile", "Atteindre le jardin fertile"),
                      atteindre("cour", "cour_des_heures", "Atteindre la cour des heures")]},
       {"id": "retour", "resume": "Revenir dire à Idir que la route est praticable.", "objectifs": [parler("caravanier", "Rendre compte à Idir")]}],
      "Où va votre caravane, Idir ?",
      ["Nulle part depuis trois ans. Reconnaissez ma route pour moi : l'observatoire des heures à l'ouest, le jardin fertile au nord-est, la cour des heures à l'est. Si vous revenez, elle est bonne."],
      ["Praticable ! Les dromadaires vont enfin se dérouiller les pattes. Prenez des dattes, les meilleures de la caravane."],
      "La route est bonne.", [(lb("dattes_des_heures"), 6), (lb("perle_de_nacre"), 1)], 3)
quete("les_jarres_de_naima", "Les jarres de Naïma", 3, "potiere", "meridiane",
      "Naïma, la potière, veut tourner des jarres pour les archives : du sable d'opale et du grès des heures, pris loin de la ville.",
      [{"id": "terre", "resume": "Réunir huit sables d'opale et quatre grès des heures pour Naïma.",
        "objectifs": [collecter("sable", lb("sable_d_opale"), 8, "Ramasser huit sables d'opale"), collecter("gres", lb("gres_des_heures"), 4, "Tailler quatre grès des heures")]},
       {"id": "retour", "resume": "Apporter le sable et le grès à Naïma.", "objectifs": [parler("potiere", "Apporter la terre à Naïma")]}],
      "Que tournez-vous, Naïma ?",
      ["Des jarres pour les archives, si vous m'apportez de quoi : huit sables d'opale pris dans les dunes, quatre grès des heures. La terre d'ici tient les siècles."],
      ["De la bonne terre. Les jarres cuiront cette nuit. Prenez des dattes et une perle : c'est tout ce qu'une potière a de rond à offrir."],
      "Voici le sable et le grès.", [(lb("dattes_des_heures"), 4), (lb("perle_de_nacre"), 2)], 2)
quete("les_sentinelles_d_ayoub", "Les sentinelles d'Ayoub", 3, "guide_des_dunes", "meridiane",
      "Ayoub, le guide des dunes, ne peut plus mener personne : sentinelles de sable et scorpions tiennent la route de l'ouest.",
      [{"id": "dunes", "resume": "Abattre trois sentinelles de sable et trois scorpions des heures dans les dunes.",
        "objectifs": [vaincre("sentinelles", "sentinelle_de_sable", 3, "Vaincre trois sentinelles de sable"), vaincre("scorpions", "scorpion_des_heures", 3, "Vaincre trois scorpions des heures")]},
       {"id": "retour", "resume": "Revenir à Méridiane rendre compte à Ayoub.", "objectifs": [parler("guide_des_dunes", "Rendre compte à Ayoub")]}],
      "Vous ne guidez plus, Ayoub ?",
      ["Plus depuis les sentinelles. Elles se lèvent du sable et tirent, et les scorpions finissent le travail. Trois de chaque, et je reprends le bâton."],
      ["Les dunes respirent mieux. Je reprends le bâton demain, et je raconterai qui a nettoyé la route. Prenez ces perles, des caravanes d'avant."],
      "La route de l'ouest est libre.", [(lb("perle_de_nacre"), 3)], 3)
# ------------------------------------------------------------------ Montagnes de Verre
quete("le_metal_de_gunnar", "Le métal de Gunnar", 3, "maitre_forgeron", "verreole",
      "Gunnar, maître forgeron de Verréole, veut fondre un alliage de verre : pierre de verre et éclats résonnants.",
      [{"id": "metal", "resume": "Réunir huit pierres de verre et quatre éclats résonnants pour Gunnar.",
        "objectifs": [collecter("pierre", lb("pierre_de_verre"), 8, "Extraire huit pierres de verre"), collecter("eclats", lb("eclat_resonnant"), 4, "Réunir quatre éclats résonnants")]},
       {"id": "retour", "resume": "Apporter le métal à la forge de Gunnar.", "objectifs": [parler("maitre_forgeron", "Apporter le métal à Gunnar")]}],
      "Que forgez-vous, Gunnar ?",
      ["Un alliage que personne n'a réussi : du verre qui ne casse pas. Huit pierres de verre, quatre éclats résonnants pour le chant. Si ça tient, Orsane aura une épée qui ne se brise jamais."],
      ["Ça tient. Ça chante quand on le frappe, mais ça tient. Prenez du cristal de nacre : pour votre lame à vous."],
      "Voici le métal.", [(lb("cristal_de_nacre"), 4)], 3)
quete("l_accord_de_la_luthiere", "L'accord de la luthière", 3, "luthiere", "atelier_d_instruments",
      "La luthière de l'atelier d'instruments cherche l'accord du pont qui chante : six éclats résonnants, et le pont écouté de près.",
      [{"id": "accord", "resume": "Écouter le pont qui chante et rapporter six éclats résonnants.",
        "objectifs": [atteindre("pont", "pont_qui_chante", "Écouter le pont qui chante"), collecter("eclats", lb("eclat_resonnant"), 6, "Réunir six éclats résonnants")]},
       {"id": "retour", "resume": "Rapporter les éclats à la luthière.", "objectifs": [parler("luthiere", "Rapporter les éclats à la luthière")]}],
      "Sur quoi travaillez-vous ?",
      ["Sur l'accord du pont qui chante. Chaque cristal y sonne une note ; six éclats, pris sur place après l'avoir écouté, et je reconstruis sa gamme dans un instrument."],
      ["La gamme du pont, en six éclats. Écoutez... c'est lui. Prenez ces perles, et revenez quand l'instrument sera fini."],
      "Voici les six éclats.", [(lb("perle_de_nacre"), 2), (lb("the_des_cimes"), 2)], 3)
quete("les_bains_de_verreole", "Les bains de Verréole", 3, "baigneur", "bains_des_sources",
      "Le maître des bains manque de thé des cimes et de pierre taillée pour rouvrir le grand bassin.",
      [{"id": "bains", "resume": "Apporter quatre thés des cimes et six pierres de verre taillées au maître des bains.",
        "objectifs": [collecter("the", lb("the_des_cimes"), 4, "Réunir quatre thés des cimes"), collecter("pierre", lb("pierre_de_verre_taillee"), 6, "Tailler six pierres de verre")]},
       {"id": "retour", "resume": "Apporter le tout aux bains des sources.", "objectifs": [parler("baigneur", "Apporter le thé et la pierre au maître des bains")]}],
      "Le grand bassin est fermé ?",
      ["Fissuré, et sans thé pour les baigneurs. Six pierres de verre taillées pour la margelle, quatre thés des cimes pour la salle de repos. Rouvrir un bain, c'est rouvrir une ville."],
      ["Le bassin fume à nouveau. Vous êtes le premier à y tremper les pieds, c'est la règle. Prenez du thé pour la route."],
      "Voici le thé et la pierre.", [(lb("the_des_cimes"), 3), (lb("perle_de_nacre"), 1)], 2)
quete("la_garde_de_verre", "La garde de verre", 3, "garde_de_verre", "verreole",
      "La garde de Verréole ne tient plus les cols : colosses de cristal et échos résonnants descendent vers le plateau.",
      [{"id": "cols", "resume": "Abattre deux colosses de cristal et trois échos résonnants autour de Verréole.",
        "objectifs": [vaincre("colosses", "colosse_de_cristal", 2, "Vaincre deux colosses de cristal"), vaincre("echos", "echo_resonnant", 3, "Vaincre trois échos résonnants")]},
       {"id": "retour", "resume": "Revenir au palais des miroirs rendre compte à la garde.", "objectifs": [parler("garde_de_verre", "Rendre compte à la garde")]}],
      "Les cols sont tenus ?",
      ["Par eux, pas par nous. Deux colosses de cristal au nord, lents et lourds, et des échos qui repoussent tout ce qui approche. Nettoyez les cols et Orsane le saura."],
      ["Les cols sont à nous. La reine le saura ce soir. Prenez du cristal : la garde en tire ses lames."],
      "Les cols sont libres.", [(lb("cristal_de_nacre"), 4), (lb("perle_de_nacre"), 1)], 4)
# ------------------------------------------------------------------ Couronne Immobile
quete("le_pain_de_la_cour", "Le pain de la cour", 3, "boulanger_garde", "haute_garde",
      "Le boulanger de Haute-Garde veut refaire le pain de la cour tel qu'autrefois : de la farine de Clairval et du miel de rosée.",
      [{"id": "ingredients", "resume": "Apporter six farines et deux miels de rosée au boulanger de Haute-Garde.",
        "objectifs": [collecter("farine", lb("farine"), 6, "Moudre six farines"), collecter("miel", lb("miel_de_rosee"), 2, "Réunir deux miels de rosée")]},
       {"id": "retour", "resume": "Apporter les ingrédients au boulanger de Haute-Garde.", "objectifs": [parler("boulanger_garde", "Apporter les ingrédients au boulanger")]}],
      "Votre pain, c'est celui de la cour ?",
      ["Presque. Il manque la farine du moulin de Clairval, six mesures, et deux miels de rosée des Prairies. Avec ça, je refais le pain qu'on servait au roi."],
      ["Sentez. C'est celui-là. Vaëron en mangera sans rien dire, mais il en reprendra. Prenez les premières miches."],
      "Voici la farine et le miel.", [(lb("pain_de_la_cour"), 6)], 2)
quete("l_ecole_de_haute_garde", "L'école de Haute-Garde", 3, "maitresse_d_ecole", "haute_garde",
      "La maîtresse d'école veut des récits pour ses élèves : le Sanctuaire de l'Arrivée, le marché flottant et la serre ancienne, vus de vos yeux.",
      [{"id": "recits", "resume": "Visiter le Sanctuaire de l'Arrivée, le marché flottant et la serre ancienne pour en rapporter des récits.",
        "objectifs": [atteindre("sanctuaire", "sanctuaire", "Visiter le Sanctuaire de l'Arrivée"), atteindre("marche", "marche_flottant", "Visiter le marché flottant"),
                      atteindre("serre", "serre_ancienne", "Visiter la serre ancienne")]},
       {"id": "retour", "resume": "Revenir à l'école raconter.", "objectifs": [parler("maitresse_d_ecole", "Raconter aux élèves")]}],
      "Que lisent vos élèves ?",
      ["Des murs. Ils ne connaissent que les murs de Haute-Garde. Allez voir pour eux le Sanctuaire de l'Arrivée, le marché flottant de la Mer, la serre ancienne des Jardins, et revenez raconter."],
      ["Ils ont écouté sans bouger, ce qui n'arrive jamais. Demain ils dessineront le marché flottant. Prenez ces perles : la classe a fait une collecte."],
      "J'ai des récits pour eux.", [(lb("perle_de_nacre"), 2), (lb("pain_de_la_cour"), 2)], 3)
quete("la_patrouille_de_rolande", "La patrouille de Rolande", 3, "sergent", "haute_garde",
      "Rolande, sergente de Haute-Garde, veut nettoyer les anneaux : sentinelles de la Couronne déréglées et spectres de la cour.",
      [{"id": "anneaux", "resume": "Abattre trois sentinelles de la Couronne et deux spectres de la cour sur les anneaux.",
        "objectifs": [vaincre("sentinelles", "sentinelle_de_la_couronne", 3, "Vaincre trois sentinelles de la Couronne"), vaincre("spectres", "spectre_de_la_cour", 2, "Vaincre deux spectres de la cour")]},
       {"id": "retour", "resume": "Revenir à la place d'armes rendre compte à Rolande.", "objectifs": [parler("sergent", "Rendre compte à Rolande")]}],
      "Besoin d'une épée de plus, sergente ?",
      ["Toujours. Les sentinelles de la Couronne ne reconnaissent plus personne et les spectres de la cour rôdent sur les anneaux. Trois sentinelles, deux spectres, et mes patrouilles repartent."],
      ["Rapport entendu. Les patrouilles repartent à l'aube. Prenez ces lanternes royales : on les a reprises aux sentinelles, elles vous reviennent."],
      "Les anneaux sont nettoyés.", [(lb("lanterne_royale"), 2), (lb("pain_de_la_cour"), 2)], 4)
quete("les_lanternes_du_quartier", "Les lanternes du quartier", 3, "elouan", "quartier_des_portes",
      "Elouan veut rallumer le quartier des portes avant la capitale : quatre lanternes royales pour ses lampadaires.",
      [{"id": "lanternes", "resume": "Réunir quatre lanternes royales pour Elouan.", "objectifs": [collecter("lanternes", lb("lanterne_royale"), 4, "Réunir quatre lanternes royales")]},
       {"id": "retour", "resume": "Apporter les lanternes à Elouan, au quartier des portes.", "objectifs": [parler("elouan", "Apporter les lanternes à Elouan")]}],
      "Le quartier est bien sombre.",
      ["Les lampadaires sont là, les lanternes non. Quatre lanternes royales, de celles qui ne s'éteignent pas, et le quartier des portes sera le premier éclairé de la capitale."],
      ["Quatre lanternes, quatre lampadaires. Ce soir le quartier brille avant la place. Prenez ces perles, les jardins en ont retrouvé dans les bassins."],
      "Voici les lanternes.", [(lb("perle_de_nacre"), 3)], 3, offre_dans=("ouverts",), prereq=[{"drapeau_monde": "capitale_niveau_1"}])
# ------------------------------------------------------------------ Capitale
quete("les_cuisines_du_palais", "Les cuisines du palais", 4, "intendante_du_palais", "palais_royal",
      "Sidonie, l'intendante, veut rallumer les cuisines du palais pour la cour qui revient : pain de la cour, poissons-lanternes et ragoût du domaine.",
      [{"id": "vivres", "resume": "Apporter quatre pains de la cour, quatre poissons-lanternes et deux ragoûts du domaine à Sidonie.",
        "objectifs": [collecter("pain", lb("pain_de_la_cour"), 4, "Réunir quatre pains de la cour"), collecter("poissons", lb("poisson_lanterne"), 4, "Réunir quatre poissons-lanternes"),
                      collecter("ragout", lb("ragout_du_domaine"), 2, "Préparer deux ragoûts du domaine")]},
       {"id": "retour", "resume": "Livrer les vivres aux cuisines du palais.", "objectifs": [parler("intendante_du_palais", "Livrer les vivres à Sidonie")]}],
      "Les cuisines tournent pour qui, Sidonie ?",
      ["Pour personne, et ça me rend folle. Quatre pains de la cour de Haute-Garde, quatre poissons-lanternes de la Mer, deux ragoûts du domaine de Clairval : trois régions dans une marmite, et la cour aura faim."],
      ["Trois régions dans une marmite. Sentez ça. Les ambassadeurs en redemanderont. Prenez ces miches, les premières du nouveau four."],
      "Voici les vivres.", [(lb("pain_de_la_cour"), 6), (lb("ragout_du_domaine"), 2)], 3, offre_dans=("accueil", "couronne"), prereq=[{"drapeau_monde": "capitale_niveau_2"}])
quete("les_presents_des_regions", "Les présents des régions", 4, "ambassadeur_mer", "quartier_des_ambassades",
      "Teodor, ambassadeur de la Mer, veut réunir des présents des trois régions voisines pour l'ambassade : perles, fruits de géant et thé des cimes.",
      [{"id": "presents", "resume": "Réunir trois perles de nacre, trois fruits de géant et trois thés des cimes.",
        "objectifs": [collecter("perles", lb("perle_de_nacre"), 3, "Réunir trois perles de nacre"), collecter("fruits", lb("fruit_de_geant"), 3, "Réunir trois fruits de géant"),
                      collecter("the", lb("the_des_cimes"), 3, "Réunir trois thés des cimes")]},
       {"id": "retour", "resume": "Apporter les présents à l'ambassade de la Mer.", "objectifs": [parler("ambassadeur_mer", "Apporter les présents à Teodor")]}],
      "L'ambassade manque de quelque chose ?",
      ["De tout, mais surtout de présents à offrir aux autres ambassades : trois perles de la Mer, trois fruits des Jardins, trois thés des Montagnes. La diplomatie commence par une table."],
      ["Lune a pleuré sur les fruits et Hadrien a bu le thé sans un mot, ce qui chez lui est un éloge. Prenez ces lanternes royales : un présent pour un présent."],
      "Voici les présents.", [(lb("lanterne_royale"), 3)], 4, offre_dans=("accueil", "couronne"), prereq=[{"drapeau_monde": "capitale_niveau_1"}])
quete("la_chronique_de_maud", "La chronique de Maud", 4, "archiviste_royale", "archives_royales",
      "Maud, l'archiviste royale, achève la chronique du roi disparu : quatre lieux où il passa, à revoir pour les décrire.",
      [{"id": "lieux", "resume": "Revoir le navire perdu, la serre ancienne, l'observatoire des heures et le pont qui chante pour la chronique.",
        "objectifs": [atteindre("navire", "navire_perdu", "Revoir le navire perdu"), atteindre("serre", "serre_ancienne", "Revoir la serre ancienne"),
                      atteindre("observatoire", "observatoire_des_heures", "Revoir l'observatoire des heures"), atteindre("pont", "pont_qui_chante", "Revoir le pont qui chante")]},
       {"id": "retour", "resume": "Décrire les quatre lieux à Maud, à la tour des archives.", "objectifs": [parler("archiviste_royale", "Décrire les lieux à Maud")]}],
      "Votre chronique avance ?",
      ["Elle bute sur les lieux. Le roi est passé par le navire perdu, la serre ancienne, l'observatoire des heures et le pont qui chante, et personne ne sait plus les décrire. Allez les voir pour moi."],
      ["Quatre pages de plus, et les bonnes. La chronique sera reliée avant l'hiver. Prenez ces perles : les archives les gardaient pour rien."],
      "J'ai revu les quatre lieux.", [(lb("perle_de_nacre"), 3)], 5, prereq=[{"drapeau_monde": "capitale_niveau_1"}])

assert len(Q) == 27, len(Q)
# ------------------------------------------------------------------ écriture des quêtes
for q in Q:
    etapes = []
    for e in q["etapes"]:
        etapes.append({"id": e["id"], "resume": e["resume"], "lieu": q["lieu"], "objectifs": e["objectifs"]})
    fin = [{"type": "experience", "niveaux": q["xp"]}, {"type": "drapeau", "nom": q["id"] + "_terminee"}]
    doc = {"titre": q["titre"], "acte": q["acte"], "description": q["description"], "etapes": etapes, "fin": fin}
    (ROOT / "quest" / f"{q['id']}.json").write_text(json.dumps(doc, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
# ------------------------------------------------------------------ dialogues
for q in Q:
    p = ROOT / "dialogue" / f"{q['giver']}.json"
    d = json.loads(p.read_text(encoding="utf-8"))
    qid = q["id"]; derniere = len(q["etapes"]) - 1
    noeud_fin, noeud_cours = f"{qid}_fin", f"{qid}_cours"
    # offre dans les nœuds choisis
    for n in q["offre_dans"]:
        node = d["noeuds"][n]
        node.setdefault("choix", []).insert(0, {"texte": q["offre"], "si": [{"quete_inconnue": qid}] + q["prereq"], "actions": [{"type": "demarrer_quete", "quete": qid}], "suivant": noeud_cours})
        assert len(node["choix"]) <= 4, (qid, n, len(node["choix"]))
    # conditions de remise : objets de la dernière étape de collecte (si présents)
    objets = []
    for e in q["etapes"]:
        for o in e["objectifs"]:
            if o["type"] == "collecter":
                objets.append(o)
    si_remise = [{"objet": {"id": o["cible"], "nombre": o["nombre"]}} for o in objets]
    actions_remise = [{"type": "retirer", "objet": o["cible"], "nombre": o["nombre"]} for o in objets]
    actions_remise += [{"type": "donner", "objet": item, "nombre": n} for item, n in q["recompense"]]
    actions_remise.append({"type": "objectif", "quete": qid, "objectif": "retour"})
    d["noeuds"][noeud_cours] = {"texte": q["intro"], "choix": [{"texte": "J'y vais."}]}
    choix_fin = [{"texte": q["remise"], "si": si_remise, "actions": actions_remise}] if si_remise else [{"texte": q["remise"], "actions": actions_remise}]
    choix_fin.append({"texte": "Pas tout de suite."})
    d["noeuds"][noeud_fin] = {"texte": q["fin_texte"], "choix": choix_fin}
    # entrées de début : avant le premier nœud d'offre
    index = min(i for i, e in enumerate(d["debut"]) if e.get("noeud") in q["offre_dans"])
    d["debut"].insert(index, {"noeud": noeud_cours, "si": [{"quete_active": qid}]})
    d["debut"].insert(index, {"noeud": noeud_fin, "si": [{"quete_active": qid}, {"quete_etape": {qid: f"={derniere}"}}]})
    p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(len(Q), "quêtes secondaires écrites ;", len(list((ROOT / "quest").glob("*.json"))), "quêtes au total")
