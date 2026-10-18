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

	/** Rayon (blocs) des zones aplanies ; cohérent avec les fichiers de densité. */
	public static final int RAYON_APLANI_VILLAGE = 70;
	public static final int RAYON_APLANI_SITE = 34;

	private LimbesSites() {
	}
}
