package fr.leslimbes.world.dimension;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Positions persistantes des lieux critiques dans la dimension des Limbes.
 *
 * <p>Ces coordonnées sont indépendantes de la graine : les structures correspondantes sont
 * placées par {@code les_limbes:fixed} et le terrain est aplani autour par la fonction de densité
 * {@code les_limbes:aplanir} (voir data/les_limbes/worldgen). Les fichiers JSON doivent rester
 * cohérents avec ces constantes ; un test unitaire le vérifie.</p>
 */
public final class LimbesSites {
	/** Altitude du sol des Prairies autour des sites aplanis. */
	public static final int SOL_PRAIRIES = 66;

	/** Sanctuaire de l'Arrivée : île flottante au-dessus de l'origine. */
	public static final BlockPos SANCTUAIRE = new BlockPos(0, 82, 0);
	/** Point d'arrivée des joueurs (devant l'arche de retour, côté sud), orientés vers le sud. */
	public static final Vec3d ARRIVEE = new Vec3d(0.5, 83.0, -2.5);
	/** Arche de retour dans le Sanctuaire (centre du voile, au nord de l'esplanade). */
	public static final BlockPos ARCHE_RETOUR = new BlockPos(0, 84, -8);
	/** Pied du grand pont, sur la terre ferme. */
	public static final BlockPos PIED_DU_PONT = new BlockPos(0, SOL_PRAIRIES, 56);

	/** Clairval : place centrale du village (fontaine). */
	public static final BlockPos CLAIRVAL = new BlockPos(0, SOL_PRAIRIES, 140);
	/** Clairval : dalle libre au nord de la fontaine (téléportations). */
	public static final BlockPos CLAIRVAL_ARRIVEE = new BlockPos(0, SOL_PRAIRIES, 136);
	/** Manoir du joueur : entrée principale. */
	public static final BlockPos MANOIR = new BlockPos(-120, SOL_PRAIRIES, 150);
	/** Ancien relais royal : lieu du premier sauvetage. */
	public static final BlockPos RELAIS_ROYAL = new BlockPos(150, SOL_PRAIRIES, 60);

	/** Observatoire de Clairval (tour de Lisbeth), au nord-est de la place. */
	public static final BlockPos OBSERVATOIRE = new BlockPos(30, SOL_PRAIRIES, 112);
	/** Sites réservés aux Arrivées dans les Prairies (terrain aplani, jamais construits par le jeu). */
	public static final BlockPos ARRIVEE_PRAIRIES_1 = new BlockPos(70, SOL_PRAIRIES, 210);
	public static final BlockPos ARRIVEE_PRAIRIES_2 = new BlockPos(-80, SOL_PRAIRIES, 80);
	public static final BlockPos ARRIVEE_PRAIRIES_3 = new BlockPos(220, SOL_PRAIRIES, 150);
	/** Lac du Crépuscule : ses dalles n'émergent qu'au crépuscule. */
	public static final BlockPos LAC_DU_CREPUSCULE = new BlockPos(-60, SOL_PRAIRIES, 20);
	public static final int RAYON_APLANI_ARRIVEE = 16;

	// --- Mer suspendue (centre de région 950 / −750) ---------------------------------------------
	/** Port-Nacre : île-ville au cœur de la Mer suspendue. */
	public static final BlockPos PORT_NACRE = new BlockPos(950, 66, -750);
	public static final BlockPos MARCHE_FLOTTANT = new BlockPos(800, 63, -600);
	public static final BlockPos NAVIRE_PERDU = new BlockPos(1150, 40, -950);
	public static final BlockPos SANCTUAIRE_DES_BANCS = new BlockPos(1050, 44, -560);
	public static final BlockPos PHARE_DU_DESSOUS = new BlockPos(1440, 84, -750);
	public static final BlockPos QUAI_DES_FALAISES = new BlockPos(560, 82, -360);
	// --- Jardins des Géants (1050 / 350) ---------------------------------------------------------
	public static final BlockPos SYLVECIME = new BlockPos(1050, 72, 350);
	public static final BlockPos SERRE_ANCIENNE = new BlockPos(900, 72, 520);
	public static final BlockPos PEPINIERE_ROYALE = new BlockPos(1220, 72, 230);
	public static final BlockPos ARBRE_CREUX = new BlockPos(1050, 72, 620);
	public static final BlockPos CLAIRIERE_DU_GEANT = new BlockPos(1280, 72, 480);
	// --- Désert des Heures (150 / 1250) ----------------------------------------------------------
	public static final BlockPos MERIDIANE = new BlockPos(150, 66, 1250);
	public static final BlockPos OBSERVATOIRE_DES_HEURES = new BlockPos(0, 66, 1420);
	public static final BlockPos JARDIN_FERTILE = new BlockPos(380, 66, 1130);
	// --- Montagnes de Verre (−1150 / 500) --------------------------------------------------------
	public static final BlockPos VERREOLE = new BlockPos(-1150, 96, 500);
	public static final BlockPos BAINS_DES_SOURCES = new BlockPos(-1000, 90, 380);
	public static final BlockPos ATELIER_D_INSTRUMENTS = new BlockPos(-1320, 100, 620);
	public static final BlockPos PONT_QUI_CHANTE = new BlockPos(-1150, 104, 680);
	// --- Couronne Immobile (−900 / −900) ---------------------------------------------------------
	/** Haute-Garde : la cité fortifiée de Vaëron, sur l'anneau extérieur. */
	public static final BlockPos HAUTE_GARDE = new BlockPos(-610, 82, -610);
	/** Quartier des portes et des jardins : premier quartier de la capitale endormie. */
	public static final BlockPos QUARTIER_DES_PORTES = new BlockPos(-900, 78, -720);
	/** Centre de la capitale et du palais (structures des étapes suivantes). */
	public static final BlockPos CAPITALE = new BlockPos(-900, 86, -900);
	/** Place des cérémonies, au cœur de la capitale (couronnement, duel final). */
	public static final BlockPos PLACE_DES_CEREMONIES = new BlockPos(-900, 86, -900);
	/** Palais royal, au nord de la place. */
	public static final BlockPos PALAIS = new BlockPos(-900, 86, -960);
	/** Arènes des épreuves des prétendants. */
	public static final BlockPos ARENE_AELIS = new BlockPos(1150, 72, 300);
	public static final BlockPos ARENE_KAEL = new BlockPos(1000, 90, -850);
	public static final BlockPos ARENE_ORSANE = new BlockPos(-1230, 96, 500);
	public static final BlockPos ARENE_SAHIR = new BlockPos(230, 66, 1330);
	/** Campement de l'expédition coincée dans l'archipel (épreuve de Kaël). */
	public static final BlockPos CAMPEMENT_DE_L_ARCHIPEL = new BlockPos(1250, 66, -650);
	/** Étape 7, le règne : chantier naval au large de Port-Nacre (pont du navire royal), île des ruines, lisière des brumes, site de Brumeval. */
	public static final BlockPos CHANTIER_NAVAL = new BlockPos(1030, 64, -800);
	public static final BlockPos ILE_DES_RUINES = new BlockPos(1300, 70, -1000);
	public static final BlockPos LISIERE_DES_BRUMES = new BlockPos(-900, 86, -1380);
	public static final BlockPos BRUMEVAL = new BlockPos(-1060, 86, -700);

	/** Rayon (blocs) des zones aplanies ; cohérent avec les fichiers de densité. */
	public static final int RAYON_APLANI_VILLAGE = 70;
	public static final int RAYON_APLANI_SITE = 34;

	private LimbesSites() {
	}
}
